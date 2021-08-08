package com.github.dr.rwserver.net.core.server

import com.github.dr.rwserver.io.Packet
import com.github.dr.rwserver.net.game.ConnectSrver
import com.github.dr.rwserver.net.game.ConnectionAgreement

/**
 * 只提供接口 不实现
 * 作为游戏CoreNet的实现 为GameServer提供多样的版本支持
 * @author Dr
 * @date 2020/9/5 13:31
 */
interface AbstractNetConnectEx {
    /*
     * TODO : AntiCheats
     */
    /**
     * 获取版本协议
     * @param connectionAgreement ConnectionAgreement连接
     * @return 协议
     */
    fun getVersionNet(connectionAgreement: ConnectionAgreement): AbstractNetConnect

    /**
     * 获取链接是否被转发
     */
    val isConnectServer: Boolean
    var connectServer: ConnectSrver?

    /**
     * 设置一个缓存数据包
     * @param packet v包
     */
    fun setCache(packet: Packet)

    /**
     * 设置一个缓存数据包
     * @param packet v包
     */
    fun setCacheA(packet: Packet)

    /**
     * 获取连接IP
     * @return IP
     */
    val ip: String

    /**
     * 获取使用的本地端口
     * @return Port
     */
    val port: Int

    /**
     * 获取玩家名字
     * @return 玩家名字
     */
    val name: String

    /**
     * 尝试次数+1
     */
    fun setTry()

    /**
     * 获取尝试次数
     * @return 尝试次数
     */
    val `try`: Int

    /**
     * 设置尝试
     * //@param getTryBoolean 状态
     * 获取尝试状态
     * @return Boolean
     */
    var tryBoolean: Boolean

    /**
     * 获取是否在输入密码
     * @return 值
     */
    val inputPassword: Boolean

    /**
     * 设置最后的接受数据时间
     */
    fun setLastReceivedTime()

    /**
     * 获取最后的发言时间
     * @return Time
     */
    val lastReceivedTime: Long

    /**
     * 获取连接协议
     * @return 协议
     */
    fun getConnectionAgreement(): String

    /**
     * 服务端可支持的版本
     * @return 版本号
     */
    val version: String

    /**
     * 断开连接
     */
    fun disconnect()

    /**
     * 发送包
     * @param packet 数据
     */
    fun sendPacket(packet: Packet)

    /**
     * Debug 特殊开发 暂不开放
     * @param packet Packet
     */
    fun debug(packet: Packet) {}

    /**
     * Debug 特殊开发 暂不开放
     * @param str String
     */
    fun sendDebug(str: String) {}
}