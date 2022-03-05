/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */
package com.github.dr.rwserver.util.threads

import com.github.dr.rwserver.core.thread.Threads.newThreadService2
import com.github.dr.rwserver.core.thread.TimeTaskData
import com.github.dr.rwserver.data.global.NetStaticData
import com.github.dr.rwserver.data.global.Relay.RelayData
import com.github.dr.rwserver.struct.IntMap
import com.github.dr.rwserver.util.Time.concurrentSecond
import java.io.IOException
import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicReference

/**
 * @author Dr
 */
object ServerUploadData {
    private val bindRelayPort = IntMap<RelayData>()
    private val HTTPCore = ThreadPoolExecutor(10, 15, 1, TimeUnit.MINUTES, LinkedBlockingDeque(1000))

    init {
        TimeTaskData.ServerUploadDataTask = newThreadService2(ServerUploadData::sendPostUE, 20, 60, TimeUnit.SECONDS)
        TimeTaskData.ServerUploadData_CheckTimeTask = newThreadService2({
            val time = concurrentSecond() - 10 * 60
            bindRelayPort.iterator().forEachRemaining { e: IntMap.Entry<RelayData> ->
                if (e.value.makeTime < time) {
                    sendPostRM(e.key)
                    try {
                        e.value.relay.admin!!.sendPacket(NetStaticData.protocolData.abstractNetPacket.getSystemMessagePacket("您已被取消UP ServerList"))
                    } catch (_: IOException) {
                    }
                }
            }
        }, 30, 30, TimeUnit.SECONDS)
    }

    fun updateCustom(relayData: RelayData) {
        HTTPCore.execute { relayData.proxyData!!.doPost(relayData.up, NetStaticData.httpSynchronize) }
    }

    /**
     * 注册RelayProxy
     * @param id
     * @param relayData
     */
    @JvmStatic
    fun registerRelayData(id: Int, relayData: RelayData) {
        bindRelayPort.put(id, relayData)
        HTTPCore.execute {
            relayData.proxyData!!.doPost(relayData.add, NetStaticData.httpSynchronize)
            relayData.proxyData.doPost(relayData.portCheck, NetStaticData.httpSynchronize)
        }
    }

    /**
     * 根据id获取
     * @param id
     * @return
     */
    @JvmStatic
    fun getRelayData(id: Int): RelayData? {
        return bindRelayPort[id]
    }

    fun getRelayDataPort(port: Int): RelayData? {
        val result = AtomicReference<RelayData>(null)
        bindRelayPort.values().toArray().each({ e: RelayData -> e.port == port }) { newValue: RelayData -> result.set(newValue) }
        return result.get()
    }

    private fun sendPostUE() {
        bindRelayPort.values().forEachRemaining { e: RelayData -> HTTPCore.execute { e.proxyData!!.doPost(e.up, NetStaticData.httpSynchronize) } }
    }

    @JvmStatic
    fun sendPostRM(id: Int) {
        val relayData = bindRelayPort[id] ?: return
        HTTPCore.execute { relayData.proxyData!!.doPost(relayData.rmList, NetStaticData.httpSynchronize) }
        bindRelayPort.remove(id)
        relayData.proxyData!!.clean()
    }
}