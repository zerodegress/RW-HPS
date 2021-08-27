#!/bin/bash
#**********************************************************************************#
#* 请注意 如果您对Linux了解过少 请谨慎使用                                               #
#* Please note that if you know too little about Linux, please use it with caution #
#*      避免无法控制您的行为而导致不可估量的损失                                          #
#*      Avoid immeasurable losses due to uncontrollable actions                    #
#**********************************************************************************#



#**********************************************************************************#
#* 这是一个在Linux启用RPS的脚本                                                        #
#* irqbalance 是Linux内核自带的一个中断调节服务                                         #
#*     它根据网络负载情况自动将网卡中断和CPU进行亲和请设置                                  #
#*     事实上它就是 自动的 smp irq affinity 它有很好的动态调节效果                         #
#*     但是对于大量小包的网络环境，irqbalance几乎是无效的。                                #
#* RPS/RFS 是充分利用CPU的多核性能 具体可以自行查找资料                                    #
#***********************************************************************************#

#**********************************************************************************#
#* 1. auto set SMP Irq Affinity                                                    #
#*    1.1  配置文件                                                                 #
#*         /proc/irq/X/smp_affinity                                                #
#*    1.2  配置参数                                                                 #
#*         CPU位图值                                                                #
#*                                                                                 #
#* 2. auto set RPS/RFS                                                             #
#*    2.1 配置文件                                                                  #
#*         /sys/class/net/<dev>/queues/rx-<n>/rps_cpus                             #
#*         /sys/class/net/<dev>/queues/rx-<n>/rps_flow_cnt                         #
#*         /proc/sys/net/core/rps_sock_flow_entries                                #
#*    2.2 配置参数                                                                  #
#*         rps_cpus = CPU位图值                                                     #
#*         rps_sock_flow_entries = 32768                                           #
#*         单队列网卡：rps_flow_cnt = rps_sock_flow_entries                          #
#*         多队列网卡：rps_flow_cnt = rps_sock_flow_entries / N  (N等于网卡队列数)      #
#* 3. irqbalance                                                                    #
#*         启动：systemctl start irqbalance                                          #
#*         停止：systemctl stop irqbalance                                           #
#***********************************************************************************#


# 默认使用的网卡匹配前缀是eth 若不同请修改

interface=""
if [ $# -gt 1 ];then
    for arg in $@
    do
        if `echo ${arg} | grep -qE '^eth[0-9]+-[0-9]+$'`
        then
            interface="${interface} ${arg%-*}"
        fi
    done
fi
interface=`ls /sys/class/net | egrep "^eth[0-9]+$"`
cpunum=`head -n1 /proc/interrupts|awk '{print NF}'`
tmp_smpfile="/tmp/smpfile"


# 方案1： 仅设置网卡中断亲和性，NIC中断和CPU核绑定
function onlysetup_smp_affinity() {
    clean_all
    echo ""
    echo "startting setup balance rule 2: use only smp affinity"
    [ -f ${tmp_smpfile} ] && rm -f ${tmp_smpfile}
    for ifname in ${interface}
    do
        cat /proc/interrupts | grep ${ifname} | while read line
        do
            irqnum=`echo ${line} | awk -F ':' '{print $1}' | awk '{print $1}'`
            ifinfo=`echo ${line} | awk '{print $NF}'`
            echo "${ifinfo}:${irqnum}:/proc/irq/${irqnum}/smp_affinity_list" >> ${tmp_smpfile}
        done
    done
    declare -i cpu
    declare -i count=0
    cat ${tmp_smpfile} | while read line
    do
        ifinfo=`echo ${line} | awk -F ':' '{print $1}'`
        irqnum=`echo ${line} | awk -F ':' '{print $2}'`
        setfile=`echo ${line} | awk -F ':' '{print $3}'`
        cpu=$[${count}%${cpunum}]
        echo ${cpu} > ${setfile}
        echo "SMP Affinity: bind ${ifinfo} irq[${irqnum}] on CPU${cpu}"
        count=$[${count} + 1]
    done
    [ -f ${tmp_smpfile} ] && rm -f ${tmp_smpfile}
}

# 方案2：仅设置RPS，通过软件方式实现CPU负载均衡
function onlysetup_rps_rfs() {
    clean_all
    echo ""
    echo "startting setup balance rule 3: use only rps/rfs"
    declare -i rfc_flow_cnt=0
    declare -i rps_sock_flow_entries=32768
    rps_cpus=`printf "%x" $((2**$cpunum-1))`
    for ifname in ${interface}
    do
        queuenum=`ls /sys/class/net/"${ifname}"/queues/ | grep rx | wc -l`
        if test ${queuenum} -eq 1
        then
            rfc_flow_cnt=${rps_sock_flow_entries}
        else
            rfc_flow_cnt=$[${rps_sock_flow_entries}/${queuenum}]
        fi
        for rxdir in /sys/class/net/"${ifname}"/queues/rx-*
        do
            echo ${rps_cpus} >${rxdir}/rps_cpus
            echo ${rfc_flow_cnt} >${rxdir}/rps_flow_cnt
        done
        echo "RPS/RFS: set ${ifname} rps_cpus = ${rps_cpus}, rps_flow_cnt = ${rfc_flow_cnt}"
    done
    echo ${rps_sock_flow_entries} > /proc/sys/net/core/rps_sock_flow_entries
    echo "RPS/RFS: set rps_sock_flow_entries = ${rps_sock_flow_entries}"
}

#方案3：仅启用irqbalance，系统自动调节NIC负载均衡
function only_irqbalance() {
    clean_all
    echo ""
    echo "startting setup balance rule 4 : start service irqbalance"
    systemctl start irqbalance
}

#方案3：SMP中断亲和性与RPS配合使用
#方案说明：先执行方案1，使NIC硬中断大致在CPU间均衡；再针对单队列网卡，设置RPS负载均衡所有CPU。（多队列网卡，不启用RPS）
function smp_affinity_work_with_rps_rfs() {
    echo ""
    echo "startting setup balance rule 1 : use both smp and rps"
    declare -i rfc_flow_cnt=32768
    declare -i rps_sock_flow_entries=32768
    declare -i cpu
    declare -i count=0
    rps_cpus=`printf "%x" $((2**$cpunum-1))`
    [ -f ${tmp_smpfile} ] && rm -f ${tmp_smpfile}
    for ifname in ${interface}
    do
        cat /proc/interrupts | grep ${ifname} | while read line
        do
            irqnum=`echo ${line} | awk -F ':' '{print $1}' | awk '{print $1}'`
            ifinfo=`echo ${line} | awk '{print $NF}'`
            echo "${ifinfo}:${irqnum}:/proc/irq/${irqnum}/smp_affinity_list" >> ${tmp_smpfile}
        done
    done
    cat ${tmp_smpfile} | while read line
    do
        ifinfo=`echo ${line} | awk -F ':' '{print $1}'`
        irqnum=`echo ${line} | awk -F ':' '{print $2}'`
        setfile=`echo ${line} | awk -F ':' '{print $3}'`
        cpu=$[${count}%${cpunum}]
        echo ${cpu} > ${setfile}
        echo "SMP Affinity: bind ${ifinfo} irq[${irqnum}] on CPU${cpu}"
        count=$[${count} + 1]
    done
    [ -f ${tmp_smpfile} ] && rm -f ${tmp_smpfile}
    for ifname in ${interface}
    do
        queuenum=`ls /sys/class/net/"${ifname}"/queues/ | grep rx | wc -l`
        if test ${queuenum} -ne 1
        then
            echo "RPS/RFS: ${ifname} is ${queuenum}-queue device , RPS disable"
            continue
        fi
        for rxdir in /sys/class/net/"${ifname}"/queues/rx-*
        do
            echo ${rps_cpus} >${rxdir}/rps_cpus
            echo ${rfc_flow_cnt} >${rxdir}/rps_flow_cnt
        done
        echo "RPS/RFS: set ${ifname} rps_cpus = ${rps_cpus}, rps_flow_cnt = ${rfc_flow_cnt}"
    done
    echo ${rps_sock_flow_entries} > /proc/sys/net/core/rps_sock_flow_entries
    echo "RPS/RFS: set rps_sock_flow_entries = ${rps_sock_flow_entries}"
}

function clean_smp_affinity() {
    [ -f ${tmp_smpfile} ] && rm -f ${tmp_smpfile}
    for ifname in ${interface}
    do
        cat /proc/interrupts | grep ${ifname} | while read line
        do
            irqnum=`echo ${line} | awk -F ':' '{print $1}' | awk '{print $1}'`
            ifinfo=`echo ${line} | awk '{print $NF}'`
            echo "${ifinfo}:/proc/irq/${irqnum}/smp_affinity_list" >> ${tmp_smpfile}
        done
    done
    cat ${tmp_smpfile} | while read line
    do
        setfile=`echo ${line} | awk -F ':' '{print $2}'`
        ifinfo=`echo ${line} | awk -F ':' '{print $1}'`
        echo 0 > ${setfile}
    done
    [ -f ${tmp_smpfile} ] && rm -f ${tmp_smpfile}
    echo "clean rule 2 : smp affinity over"
}

function clean_rps_rfs() {
    for ifname in ${interface}
    do
        for rxdir in /sys/class/net/"${ifname}"/queues/rx-*
        do
            echo 0 >${rxdir}/rps_cpus
            echo 0 >${rxdir}/rps_flow_cnt
        done
    done
    echo 0 > /proc/sys/net/core/rps_sock_flow_entries
    echo "clean rule 3 : rps/rfs over"
}

function clean_irqbalance() {
    systemctl stop irqbalance
    echo "clean rule 4 : stop irqbalance over"
}

function clean_all() {
    echo "cleanning all rules ..."
    clean_irqbalance
    clean_rps_rfs
    clean_smp_affinity
}

function help() {
    echo "Usage: $0 <auto|smp|rps|irqbalance|cleanall>"
    echo "Setup  balance between NIC and CPU on multi-nic multi-cpu system"
    echo ""
    echo "   auto           rule 1: use both rps and smp affinity"
    echo "   smp            rule 2: only use smp affinity to bind NIC irq on CPU"
    echo "   rps            rule 3: only use rps to bind skb to CPU by soft way"
    echo "   irqbalance     rule 4: only use system service irqbalance by system self"
    echo "   cleanall       clean all rules"
}

case $1 in
    smp)
        onlysetup_smp_affinity;;
    rps)
        onlysetup_rps_rfs;;
    irqbalance)
        only_irqbalance;;
    auto)
        smp_affinity_work_with_rps_rfs;;
    cleanall)
        clean_all;;
    *)
        help;;
esac
