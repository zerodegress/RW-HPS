package com.github.dr.rwserver.net.core

import com.github.dr.rwserver.data.Player
import com.github.dr.rwserver.io.Packet
import com.github.dr.rwserver.net.game.ConnectionAgreement
import com.github.dr.rwserver.util.log.Log
import org.jetbrains.annotations.Nls
import kotlin.Throws
import java.io.IOException
import com.github.dr.rwserver.util.zip.gzip.GzipEncoder
import org.intellij.lang.annotations.JdkConstants.BoxLayoutAxis

/**
 * 只提供接口 不实现
 * 作为游戏CoreNet的实现 为GameServer提供多样的版本支持
 * @author Dr
 * @date 2020/9/5 13:31
 */
interface AbstractNetConnect {
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
     * 获取玩家
     * @return Player
     */
    val player: Player?

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
     * 获取系统命名的消息包
     * SERVER: ...
     * @param msg The message
     */
    fun sendSystemMessage(@Nls msg: String)

    /**
     * 发送用户名命名的消息
     * @param msg String
     * @param sendBy String
     * @param team Int
     */
    fun sendChatMessage(@Nls msg: String, sendBy: String, team: Int)

    /**
     * 发送服务器消息
     * @param utilData 是否发送UnitData
     * @throws IOException Error
     */
    @Throws(IOException::class)
    fun sendServerInfo(utilData: Boolean)

    /**
     * 自杀
     */
    fun sendSurrender()

    /**
     * 踢出玩家
     * @param reason 发送原因
     * @throws IOException Error
     */
    @Throws(IOException::class)
    fun sendKick(@Nls reason: String)

    /**
     * Ping
     */
    fun ping()

    /**
     * 提取GameSave包
     * @param packet packet
     * @return 包
     * @throws IOException Error
     */
    @Throws(IOException::class)
    fun getGameSaveData(packet: Packet): ByteArray

    /**
     * 接受语言包
     * @param p Packet
     * @throws IOException Error
     */
    @Throws(IOException::class)
    fun receiveChat(p: Packet)

    /**
     * 接受位移包
     * @param p Packet
     * @throws IOException Error
     */
    @Throws(IOException::class)
    fun receiveCommand(p: Packet)

    /**
     * 发送游戏开始包
     * @throws IOException Error
     */
    @Throws(IOException::class)
    fun sendStartGame()

    /**
     * 发送队伍包
     * @param gzip GzipPacket
     */
    fun sendTeamData(gzip: GzipEncoder)

    /**
     * Server类型
     * @param msg RelayID
     */
    fun sendRelayServerType(msg: String)

    /**
     * 类型回复
     */
    fun sendRelayServerTypeReply(packet: Packet)

    /**
     * 获取玩家的信息并注册
     * @param p Packet包
     * @return 注册状态
     * @throws IOException err
     */
    @BoxLayoutAxis
    @Throws(IOException::class)
    fun getPlayerInfo(p: Packet): Boolean

    /**
     * 注册连接
     * @param p Packet包
     * @throws IOException err
     */
    @Throws(IOException::class)
    fun registerConnection(p: Packet)

    /**
     * 密码错误
     * @throws IOException err
     */
    @Throws(IOException::class)
    fun sendErrorPasswd()

    /**
     * 断开连接
     */
    fun disconnect()

    /**
     * 诱骗客户端发送Save包
     */
    fun getGameSave()

    /**
     * 发送重连包
     * @param packet ByteBuf
     */
    fun sendGameSave(packet: Packet)


    fun reConnect():Boolean {
        try {
            sendKick("不支持重连")
        } catch (e: IOException) {
            Log.error("(", e)
        }
        return false
    }

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