package com.github.dr.rwserver.net.netconnectprotocol

import com.github.dr.rwserver.data.Player
import com.github.dr.rwserver.data.global.NetStaticData
import com.github.dr.rwserver.io.GameOutputStream
import com.github.dr.rwserver.io.Packet
import com.github.dr.rwserver.net.GroupNet
import com.github.dr.rwserver.net.core.server.AbstractNetConnect
import com.github.dr.rwserver.net.game.ConnectSrver
import com.github.dr.rwserver.net.game.ConnectionAgreement
import com.github.dr.rwserver.util.PacketType
import com.github.dr.rwserver.util.Time.concurrentMillis
import com.github.dr.rwserver.util.log.Log
import com.github.dr.rwserver.util.zip.gzip.GzipEncoder
import java.io.IOException

/**
 * 作为[AbstractNetConnect] 和 协议实现的中间人
 * 目的是为了多协议支持
 * 共用协议放在本处
 * @author Dr
 */
 abstract class AbstractGameVersion(@JvmField protected val connectionAgreement: ConnectionAgreement): AbstractNetConnect {
    /** 错误次数  */
    override var `try` = 0
        protected set

    /** 是否停留在输入界面  */
    override var inputPassword = false
        protected set

    /** 最后一次接受到包的时间  */
    override var lastReceivedTime = concurrentMillis()
        protected set

    override val isConnectServer: Boolean = false
    override var connectServer: ConnectSrver? = null

    /** 玩家是否死亡  */
    @JvmField
    @Volatile
    protected var isDis = false

    /** 是否已经重试过  */
    @Volatile
    override var tryBoolean = false

    /** 玩家  */
    override lateinit var player: Player


    override val ip: String
        get() = connectionAgreement.ip

    override val port: Int
        get() = connectionAgreement.localPort

    override val name: String
        get() = ""

    override fun setCache(packet: Packet) {}
    override fun setCacheA(packet: Packet) {}
    override fun setTry() {
        `try`++
    }

    override fun setLastReceivedTime() {
        tryBoolean = false
        lastReceivedTime = concurrentMillis()
    }

    override fun getConnectionAgreement(): String {
        return connectionAgreement.useAgreement
    }

    override val version: String
        get() = "1.14"

    override fun sendSystemMessage(msg: String) {
        try {
            sendPacket(NetStaticData.protocolData.abstractNetPacket.getSystemMessagePacket(msg))
        } catch (e: IOException) {
            Log.error("[Player] Send System Chat Error", e)
        }
    }

    override fun sendChatMessage(msg: String, sendBy: String, team: Int) {
        try {
            sendPacket(NetStaticData.protocolData.abstractNetPacket.getChatMessagePacket(msg, sendBy, team))
        } catch (e: IOException) {
            Log.error("[Player] Send Player Chat Error", e)
        }
    }

    @Throws(IOException::class)
    override fun sendServerInfo(utilData: Boolean) {
    }

    override fun sendSurrender() {}
    @Throws(IOException::class)
    override fun sendKick(reason: String) {
        val o = GameOutputStream()
        o.writeString(reason)
        sendPacket(o.createPacket(PacketType.PACKET_KICK))
        disconnect()
    }

    override fun ping() {
        try {
            sendPacket(NetStaticData.protocolData.abstractNetPacket.getPingPacket(player))
        } catch (e: IOException) {
            `try`++
        }
    }

    @Throws(IOException::class)
    override fun getGameSaveData(packet: Packet): ByteArray = ByteArray(0)

    @Throws(IOException::class)
    override fun receiveChat(p: Packet) {}

    @Throws(IOException::class)
    override fun receiveCommand(p: Packet) {}

    @Throws(IOException::class)
    override fun sendStartGame() {}

    override fun sendTeamData(gzip: GzipEncoder) {}

    override fun sendRelayServerType(msg: String) {}

    override fun sendRelayServerTypeReply(packet: Packet) {}

    @Throws(IOException::class)
    override fun getPlayerInfo(p: Packet): Boolean {
        return false
    }

    @Throws(IOException::class)
    override fun registerConnection(p: Packet) {
    }

    @Throws(IOException::class)
    override fun sendErrorPasswd() {
    }

    override fun getGameSave() {}

    override fun sendGameSave(packet: Packet) {
        sendPacket(packet)
    }

    override fun sendPacket(packet: Packet) {
        try {
            connectionAgreement.send(packet)
        } catch (e: Exception) {
            Log.error("[UDP] SendError - 本消息单独出现无妨 连续多次出现请debug", e)
            disconnect()
        }
    }

    protected fun close(groupNet: GroupNet?) {
        try {
            connectionAgreement.close(groupNet)
        } catch (e: Exception) {
            Log.error("Close Connect", e)
        }
    }
}