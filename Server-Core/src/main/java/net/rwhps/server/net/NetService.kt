/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.net

import io.netty.bootstrap.ServerBootstrap
import io.netty.buffer.PooledByteBufAllocator
import io.netty.channel.*
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import net.rwhps.server.data.global.Data
import net.rwhps.server.data.global.NetStaticData
import net.rwhps.server.net.core.AbstractNet
import net.rwhps.server.net.handler.rudp.StartGameNetUdp
import net.rwhps.server.net.handler.tcp.StartGameNetTcp
import net.rwhps.server.net.handler.tcp.StartGamePortDivider
import net.rwhps.server.struct.Seq
import net.rwhps.server.util.ReflectionUtils
import net.rwhps.server.util.log.Log
import net.rwhps.server.util.log.Log.clog
import net.rwhps.server.util.log.Log.error
import net.rwhps.server.util.threads.GetNewThreadPool.getEventLoopGroup
import java.net.BindException
import java.net.ServerSocket

/**
 * NetGameServer Service
 * Open interfaces at least to the outside world, and try to integrate internally as much as possible
 *
 * @author RW-HPS/Dr
 */
class NetService {
    private val connectChannel = Seq<Channel>(4)
    private var serverSocket: ServerSocket? = null
    private var startGameNetUdp: StartGameNetUdp? = null
    private val start: AbstractNet
    private var errorIgnore = false

    constructor(abstractNet: AbstractNet = if (Data.config.WebGameBypassPort) StartGamePortDivider() else StartGameNetTcp()) {
        this.start = abstractNet
    }

    constructor(abstractNetClass: Class<out AbstractNet>) {
        val startNet: AbstractNet? =
            try {
                ReflectionUtils.accessibleConstructor(abstractNetClass).newInstance()
            } catch (e: Exception) {
                Log.fatal("[StartNet Load Error] Use default implementation",e)
                null
            }
        this.start = startNet ?:StartGameNetTcp()
    }

    init {
        NetStaticData.netService.add(this)
    }

    /**
     * Start the Game Server on the specified port
     * @param port Port
     */
    fun openPort(port: Int) {
        openPort(port,1,0)
    }

    /**
     * Start the Game Server in the specified port range
     *
     * @param port MainPort
     * @param startPort Start Port
     * @param endPort End Port
     */
    fun openPort(port: Int,startPort:Int,endPort:Int) {
        Data.config.save()

        clog(Data.i18NBundle.getinput("server.start.open"))
        val bossGroup: EventLoopGroup = getEventLoopGroup()
        val workerGroup: EventLoopGroup = getEventLoopGroup()
        val runClass: Class<out ServerChannel>

        if (Epoll.isAvailable()) {
            runClass = EpollServerSocketChannel::class.java
        } else {
            runClass = NioServerSocketChannel::class.java
            clog("无法使用Epoll 效率可能略低")
        }
        try {
            val serverBootstrapTcp = ServerBootstrap()
            serverBootstrapTcp.group(bossGroup, workerGroup)
                .channel(runClass)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childOption(ChannelOption.TCP_NODELAY, true)
                .childOption(ChannelOption.SO_KEEPALIVE, true)
                // Tuned sending, compatible with 200Mbps
                .childOption(ChannelOption.SO_RCVBUF,2048 * 1024)
                .childOption(ChannelOption.SO_SNDBUF,4096 * 1024)
                // Corresponds to the largest packet in the decoder, because there will be cases where the received [PacketType.PACKET_FORWARD_CLIENT_TO] size is 50M
                .option(ChannelOption.WRITE_BUFFER_WATER_MARK, WriteBufferWaterMark(minLowWaterMark,maxPacketSizt))
                .childHandler(start)

            clog(Data.i18NBundle.getinput("server.start.openPort"))

            val channelFutureTcp = serverBootstrapTcp.bind(port)
            for (i in startPort..endPort) {
                serverBootstrapTcp.bind(i)
            }

            val start = channelFutureTcp.channel()
            connectChannel.add(start)
            clog(Data.i18NBundle.getinput("server.start.end"))

            /*
             * No Fix DeadLock :(
             * io.netty.util.concurrent.DefaultPromise.await(DefaultPromise.java:253)
             */
            start.closeFuture().sync()
        } catch (e: InterruptedException) {
            if (!errorIgnore) error("[TCP Start Error]", e)
        } catch (bindError: BindException) {
            if (!errorIgnore) error("[Port Bind Error]", bindError)
        } catch (e: Exception) {
            if (!errorIgnore) error("[NET Error]", e)
        } finally {
            start.close()
            bossGroup.shutdownGracefully()
            workerGroup.shutdownGracefully()
        }
    }

    /**
     * Get the number of connections
     * @return Int
     */
    fun getConnectSize(): Int {
        return start.getConnectSize()
    }

    fun stop() {
        errorIgnore = true
        connectChannel.eachAll { obj: Channel -> obj.close().sync() }
        errorIgnore = false
    }

    companion object {
        const val minLowWaterMark = 512 * 1024
        /** Maximum accepted single package size */
        const val maxPacketSizt = 50 * 1024 * 1024
        /** Packet header data length */
        const val headerSize = 8
    }
}