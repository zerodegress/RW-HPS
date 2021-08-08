package com.github.dr.rwserver.net.game

import com.github.dr.rwserver.data.Player
import com.github.dr.rwserver.data.global.NetStaticData
import com.github.dr.rwserver.io.GameInputStream
import com.github.dr.rwserver.io.Packet
import com.github.dr.rwserver.net.core.server.AbstractNetConnect
import io.netty.bootstrap.Bootstrap
import io.netty.channel.*
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioSocketChannel

/**
 * 创建一个转发器
 * @property ip ip
 * @property port port
 * @constructor
 */
class ConnectSrver(private val ip: String, private val port: Int, private val abstractNetConnect: AbstractNetConnect) {
    private lateinit var channel: Channel


    init {
        Thread() {
            val client = Bootstrap()
            //第1步 定义线程组，处理读写和链接事件，没有了accept事件
            val group: EventLoopGroup = NioEventLoopGroup()
            try {
                client.group(group)
                //第2步 绑定客户端通道
                client.channel(NioSocketChannel::class.java)
                //第3步 给NIoSocketChannel初始化handler， 处理读写事件
                client.handler(object : ChannelInitializer<NioSocketChannel>() {
                    //通道是NioSocketChannel
                    @Throws(Exception::class)
                    override fun initChannel(ch: NioSocketChannel) {
                        ch.pipeline().addLast(PacketDecoder())
                        ch.pipeline().addLast(PacketEncoder())
                        ch.pipeline().addLast(AcceptServerData(this@ConnectSrver,abstractNetConnect))
                    }
                })
                //连接服务器
                val future: ChannelFuture = client.connect(ip, port).sync()

                this@ConnectSrver.channel = future.channel()
                future.channel().writeAndFlush(NetStaticData.protocolData.abstractNetPacket.getPlayerConnectPacket())

                //当通道关闭了，就继续往下走
                future.channel().closeFuture().sync()
            } finally {
                group.shutdownGracefully()
            }
        }.start()

        abstractNetConnect.connectServer = this
    }

    fun send(packet: Packet) {
        channel.writeAndFlush(packet)
    }

    private class AcceptServerData(private val connectSrver: ConnectSrver,private val abstractNetConnect: AbstractNetConnect): ChannelInboundHandlerAdapter() {
        @Throws(java.lang.Exception::class)
        override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
            if (msg is Packet) {
                if (msg.type == 161) {
                    val stream = GameInputStream(msg)
                    stream.readString()
                    stream.readInt()
                    stream.readInt()
                    stream.readInt()
                    stream.readString()
                    stream.readString()
                    val player = abstractNetConnect.player!!
                    connectSrver.send(NetStaticData.protocolData.abstractNetPacket.getPlayerRegisterPacket(
                        player.name,
                        player.uuid,
                        "",
                        stream.readInt()
                    ))
                } else {
                    abstractNetConnect.sendPacket(msg)
                }
            }
        }
    }
}