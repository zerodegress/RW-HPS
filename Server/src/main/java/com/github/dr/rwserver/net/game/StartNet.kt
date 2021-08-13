/*
 * Copyright 2020-2021 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.net.game

import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.data.global.NetStaticData
import com.github.dr.rwserver.net.core.AbstractNet
import com.github.dr.rwserver.net.core.server.AbstractNetConnect
import com.github.dr.rwserver.struct.OrderedMap
import com.github.dr.rwserver.struct.Seq
import com.github.dr.rwserver.util.IsUtil
import com.github.dr.rwserver.util.log.Log.clog
import com.github.dr.rwserver.util.log.Log.error
import com.shareData.chainMarket.HttpServer
import com.shareData.chainMarket.constant.HttpsSetting
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.util.concurrent.DefaultEventExecutorGroup
import io.netty.util.concurrent.EventExecutorGroup
import net.udp.ReliableServerSocket
import net.udp.ReliableSocket
import java.net.InetSocketAddress
import java.net.ServerSocket

/**
 * @author Dr
 * 遵守NetGameServer服务对外最少开放接口,尽量内部整合
 */
class StartNet {
    private val connectChannel = Seq<Channel>(4)
    private var serverSocket: ServerSocket? = null
    private var startGameNetUdp: StartGameNetUdp? = null
    private val start: AbstractNet
    @JvmField
    val OVER_MAP = OrderedMap<String, AbstractNetConnect>(16)

    internal val ioGroup: EventExecutorGroup = DefaultEventExecutorGroup(32)

    constructor() {
        start = StartGameNetTcp(this)
    }

    constructor(abstractNet: AbstractNet) {
        start = abstractNet
    }

    /**
     * 在指定端口启动Game Server
     * @param port 端口
     */
    // 不想过多if 但runClass的都是可控
    fun openPort(port: Int) {
        clog(Data.localeUtil.getinput("server.start.open"))
        val bossGroup: EventLoopGroup
        val workerGroup: EventLoopGroup
        val runClass: Class<out ServerChannel>
        if (Data.core.isWindows) {
            bossGroup = NioEventLoopGroup(4)
            workerGroup = NioEventLoopGroup()
            runClass = NioServerSocketChannel::class.java
            clog("运行在Windows 或许效率会略低")
        } else {
            bossGroup = EpollEventLoopGroup(4)
            workerGroup = EpollEventLoopGroup()
            runClass = EpollServerSocketChannel::class.java
        }
        try {
            val serverBootstrapTcp = ServerBootstrap()
            serverBootstrapTcp.group(bossGroup, workerGroup)
                .channel(runClass)
                .localAddress(InetSocketAddress(port))
                .childOption(ChannelOption.TCP_NODELAY, true) //.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                //.childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childHandler(start)
            clog(Data.localeUtil.getinput("server.start.openPort"))
            val channelFutureTcp = serverBootstrapTcp.bind(port).sync()
            val start = channelFutureTcp.channel()
            connectChannel.add(start)
            clog(Data.localeUtil.getinput("server.start.end"))
            Data.config.setObject("runPid", Data.core.pid)
            Data.config.save()
            if (Data.game.webApi) {
                startWebApi()
            }
            start.closeFuture().sync()
        } catch (e: InterruptedException) {
            error("[TCP Start Error]", e)
        } finally {
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
        }
    }

    fun openPort() {
        val bossGroup = EpollEventLoopGroup(4)
        val workerGroup = EpollEventLoopGroup()
        try {
            val serverBootstrapTcp = ServerBootstrap()
            serverBootstrapTcp.group(bossGroup, workerGroup)
                .channel(EpollServerSocketChannel::class.java)
                //.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childHandler(StartGameNetTcp(this))
            val nn = serverBootstrapTcp.bind(5200)
            for (i in 5201..5500) {
                serverBootstrapTcp.bind(i)
            }
            val start = nn.channel()
            connectChannel.add(start)
            clog(Data.localeUtil.getinput("server.start.openPort"))
            start.closeFuture().sync()
        } catch (e: InterruptedException) {
            error("[TCP Start Error]", e)
        } finally {
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
        }
    }

    fun startUdp(port: Int) {
        startGameNetUdp = StartGameNetUdp(this, NetStaticData.protocolData.abstractNetConnect, NetStaticData.protocolData.typeConnect)
        try {
            ReliableServerSocket(port).use { serverSocket ->
                this.serverSocket = serverSocket
                do {
                    val socket = serverSocket.accept() as ReliableSocket
                    startGameNetUdp!!.run(socket as ReliableServerSocket.ReliableClientSocket)
                } while (true)
            }
        } catch (ignored: Exception) {
            error("UDP Start Error", ignored)
        }
    }

    private fun startWebApi() {
        val httpServer = HttpServer()
        HttpsSetting.sslEnabled = Data.game.webApiSsl
        HttpsSetting.keystorePath = Data.game.webApiSslKetPath
        HttpsSetting.certificatePassword = Data.game.webApiSslPasswd
        HttpsSetting.keystorePassword = Data.game.webApiSslPasswd
        httpServer.start(
            Data.game.webApiPort, "com.github.dr.rwserver.net.web.api", 1024,
            null, null
        )
    }

    fun stop() {
        connectChannel.each { obj: Channel -> obj.close() }
    }

    fun clear(ctx: ChannelHandlerContext) {
        val channel = ctx.channel()
        try {
            val attr = channel.attr(NewServerHandler.NETTY_CHANNEL_KEY)
            val con = attr.get()
            if (con != null) {
                con.disconnect()
            } else {
                channel.close()
                ctx.close()
            }
        } finally {
            OVER_MAP.remove(channel.id().asLongText())
        }
    }

    fun updateNet() {
        if (IsUtil.notIsBlank(start)) {
            start.updateNet()
        }
        if (IsUtil.notIsBlank(serverSocket)) {
            startGameNetUdp!!.update()
        }
    }
}