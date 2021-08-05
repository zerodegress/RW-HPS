package com.github.dr.rwserver.net.game

import com.github.dr.rwserver.data.Player
import com.github.dr.rwserver.ga.GroupGame
import com.github.dr.rwserver.io.Packet
import com.github.dr.rwserver.net.GroupNet
import com.github.dr.rwserver.net.udp.ReliableSocket
import com.github.dr.rwserver.util.RandomUtil.generateStr
import io.netty.channel.Channel
import io.netty.channel.ChannelHandlerContext
import java.io.DataOutputStream
import java.io.IOException
import java.net.InetSocketAddress
import java.net.Socket
import java.util.*

/**
 * @author Dr
 * @date 2021年2月2日星期二 06:31:11
 */
class ConnectionAgreement {
    private val protocolType: ((packet: Packet) -> Unit)
    private val startNet: StartNet
    private val channelHandlerContext: ChannelHandlerContext?
    private val objectOutStream: Any
    private val udpDataOutputStream: DataOutputStream?
    val useAgreement: String
    @JvmField
    val ip: String
    internal val localPort: Int
    val id: String

    /**
     * TCP Send
     * @param channel Netty-Channel
     */
    constructor(channelHandlerContext: ChannelHandlerContext, channel: Channel, startNet: StartNet) {
        protocolType = { packet: Packet -> channel.writeAndFlush(packet) }
        this.startNet = startNet
        this.channelHandlerContext = channelHandlerContext
        objectOutStream = channel
        udpDataOutputStream = null
        useAgreement = "TCP"
        ip = convertIp(channel.remoteAddress().toString())
        localPort = (channel.localAddress() as InetSocketAddress).port
        id = generateStr(5)
    }
    fun  bindGroup(gid:Int){
        val c:Channel= objectOutStream as Channel;
        c.attr(GroupGame.G_KEY).set(gid);
    }
    /**
     * UDP Send
     * @param socket Socket
     * @throws IOException Error
     */
    constructor(socket: Socket, startNet: StartNet) {
        val socketStream = DataOutputStream(socket.getOutputStream())
        protocolType = { packet: Packet ->
            socketStream.writeInt(packet.bytes.size)
            socketStream.writeInt(packet.type)
            socketStream.write(packet.bytes)
            socketStream.flush()
        }
        this.startNet = startNet
        channelHandlerContext = null
        objectOutStream = socket
        udpDataOutputStream = socketStream
        useAgreement = "UDP"
        ip = convertIp(socket.remoteSocketAddress.toString())
        localPort = socket.localPort
        id = generateStr(5)
    }

    constructor() {
        protocolType = {}
        startNet = StartNet()
        channelHandlerContext = null
        objectOutStream = ""
        udpDataOutputStream = null
        useAgreement = "Test"
        ip = ""
        localPort = 0
        id = generateStr(5)
    }

    fun add(groupNet: GroupNet,gid: Int) {
        if (objectOutStream is Channel) {
            val channel=objectOutStream as Channel
            channel.attr(GroupGame.G_KEY).set(gid)
            groupNet.add(channel)
        } else if (objectOutStream is ReliableSocket) {
            groupNet.add(this)
        }
    }

    /**
     * 接管Send逻辑
     * 整合不同协议的发送逻辑
     * @param packet MsgPacket
     * @throws IOException Error
     */
    @Throws(IOException::class)
    fun send(packet: Packet) {
        protocolType(packet)
    }

    @Throws(IOException::class)
    fun close(groupNet: GroupNet?) {
        if (groupNet != null) {
            if (objectOutStream is Channel) {
                groupNet.remove(objectOutStream as Channel?)
            } else if (objectOutStream is ReliableSocket) {
                groupNet.remove(this)
            }
        }
        if (objectOutStream is Channel) {
            objectOutStream.close()
            channelHandlerContext!!.close()
            startNet.OVER_MAP.remove(objectOutStream.id().asLongText())
        } else if (objectOutStream is ReliableSocket) {
            udpDataOutputStream!!.close()
            objectOutStream.close()
            startNet.OVER_MAP.remove(objectOutStream.remoteSocketAddress.toString())
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) {
            return true
        }
        return if (other == null || javaClass != other.javaClass) {
            false
        } else id == (other as ConnectionAgreement).id
    }

    override fun hashCode(): Int {
        return Objects.hash(id)
    }

    private fun convertIp(ipString: String): String {
        return ipString.substring(1, ipString.indexOf(':'))
    }
}