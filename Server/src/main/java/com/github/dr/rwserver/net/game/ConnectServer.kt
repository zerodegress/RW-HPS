package com.github.dr.rwserver.net.game

import com.github.dr.rwserver.data.global.NetStaticData
import com.github.dr.rwserver.io.Packet
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
class ConnectServer(private val ip: String, private val port: Int, private val connectionAgreement: ConnectionAgreement) {
    private val channel: Channel


    init {
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
                    ch.pipeline().addLast(AcceptServerData(connectionAgreement))
                }
            })
            //连接服务器
            val future: ChannelFuture = client.connect(ip, port).sync()

            channel = future.channel()
            future.channel().writeAndFlush(NetStaticData.protocolData.abstractNetPacket.getPlayerConnectPacket())

            //当通道关闭了，就继续往下走
            future.channel().closeFuture().sync()
        } finally {
            group.shutdownGracefully()
        }
    }

    fun send(packet: Packet) {
        channel.writeAndFlush(packet)
    }

    private class AcceptServerData(private val connectionAgreement: ConnectionAgreement): ChannelInboundHandlerAdapter() {
        @Throws(java.lang.Exception::class)
        override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
            if (msg is Packet) {
                connectionAgreement.send(msg)
            }
        }
    }
}