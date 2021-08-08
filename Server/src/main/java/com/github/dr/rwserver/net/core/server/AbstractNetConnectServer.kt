package com.github.dr.rwserver.net.core.server

import com.github.dr.rwserver.data.Player
import com.github.dr.rwserver.io.Packet
import com.github.dr.rwserver.util.log.Log
import com.github.dr.rwserver.util.zip.gzip.GzipEncoder
import org.intellij.lang.annotations.JdkConstants
import org.jetbrains.annotations.Nls
import java.io.IOException

/**
 * 只提供接口 不实现
 * 作为游戏CoreNet的分布实现 为GameServer提供多样的版本支持
 * @author Dr
 * @date 2021/7/31/ 14:14
 */
interface AbstractNetConnectServer {
    /**
     * 获取玩家
     * @return Player
     */
    val player: Player?

    /**
     * 发送系统命名的消息包
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
     * 获取玩家的信息并注册
     * @param p Packet包
     * @return 注册状态
     * @throws IOException err
     */
    @JdkConstants.BoxLayoutAxis
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
     * 诱骗客户端发送Save包
     */
    fun getGameSave()

    /**
     * 发送重连包
     * @param packet ByteBuf
     */
    fun sendGameSave(packet: Packet)


    fun reConnect() {
        try {
            sendKick("不支持重连")
        } catch (e: IOException) {
            Log.error("(", e)
        }
    }
}