/*
 * Copyright 2020-2024 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.net

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.ChannelOption
import io.netty.channel.EventLoopGroup
import io.netty.channel.ServerChannel
import io.netty.channel.epoll.Epoll
import io.netty.channel.epoll.EpollEventLoopGroup
import io.netty.channel.epoll.EpollServerSocketChannel
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.nio.NioServerSocketChannel
import net.rwhps.server.data.global.Data
import net.rwhps.server.data.global.NetStaticData
import net.rwhps.server.net.core.AbstractNet
import net.rwhps.server.net.core.web.AbstractNetWeb
import net.rwhps.server.net.handler.tcp.StartGameNetTcp
import net.rwhps.server.net.http.WebData
import net.rwhps.server.struct.list.Seq
import net.rwhps.server.util.ReflectionUtils
import net.rwhps.server.util.concurrent.threads.GetNewThreadPool.getEventLoopGroup
import net.rwhps.server.util.internal.net.rudp.ReliableServerSocket
import net.rwhps.server.util.log.Log
import net.rwhps.server.util.log.Log.clog
import net.rwhps.server.util.log.Log.error
import java.net.BindException


/**
 * NetGameServer Service
 * Open interfaces at least to the outside world, and try to integrate internally as much as possible
 *
 * @author Dr (dr@der.kim)
 */
class NetService {
    private val closeSeq = Seq<() -> Unit>(4)
    private val start: AbstractNet
    private var errorIgnore = false

    /** 工作线程数 */
    var workThreadCount = 0

    constructor(abstractNet: AbstractNet = StartGameNetTcp()) {
        this.start = abstractNet
        setWebData()
    }

    constructor(abstractNetClass: Class<out AbstractNet>) {
        val startNet: AbstractNet? = try {
            ReflectionUtils.accessibleConstructor(abstractNetClass).newInstance()
        } catch (e: Exception) {
            Log.fatal("[StartNet Load Error] Use default implementation", e)
            null
        }
        this.start = startNet ?: StartGameNetTcp()
        setWebData()
    }

    init {
        NetStaticData.netService.add(this)
    }

    fun setWebData(data: WebData = Data.webData): NetService {
        if (start is AbstractNetWeb) {
            start.setWebData(data)
        }
        return this
    }

    /**
     * Start the Game Server on the specified port
     * @param port Port
     */
    fun openPort(port: Int) {
        openPort(port, 1, 0)
    }

    /**
     * Start the Game Server in the specified port range
     *
     * @param port MainPort
     * @param startPort Start Port
     * @param endPort End Port
     */
    fun openPort(port: Int, startPort: Int, endPort: Int) {
        Data.config.save()

        clog(Data.i18NBundle.getinput("server.start.open"))
        val bossGroup: EventLoopGroup = getEventLoopGroup(0)
        val workerGroup: EventLoopGroup = getEventLoopGroup(workThreadCount)

        val runClass: Class<out ServerChannel> = if (Epoll.isAvailable()) {
            EpollServerSocketChannel::class.java
        } else {
            NioServerSocketChannel::class.java
        }

        if (workerGroup is NioEventLoopGroup) {
            workerGroup.setIoRatio(Data.configNet.nettyIoRatio)
        } else if (workerGroup is EpollEventLoopGroup) {
            workerGroup.setIoRatio(Data.configNet.nettyIoRatio)
        }

        try {
            val serverBootstrapTcp = ServerBootstrap()
            serverBootstrapTcp.group(bossGroup, workerGroup).channel(runClass)

            with (serverBootstrapTcp) {
                childOption(ChannelOption.TCP_NODELAY, true)
                childOption(ChannelOption.SO_KEEPALIVE, true)
            }

            serverBootstrapTcp.childHandler(start)

            clog(Data.i18NBundle.getinput("server.start.openPort"))

            val channelFutureTcp = serverBootstrapTcp.bind(port)
            for (i in startPort .. endPort) {
                serverBootstrapTcp.bind(i)
            }

            val start = channelFutureTcp.channel()
            closeSeq.add {
                channelFutureTcp.channel().close()
                bossGroup.shutdownGracefully()
                workerGroup.shutdownGracefully()
            }
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
     * Start the Game Server in the specified port range
     *
     * @param port MainPort
     */
    fun openPortRUDP(port: Int) {
        clog(Data.i18NBundle.getinput("server.start.open"))
        try {
            ReliableServerSocket(port).use {
                closeSeq.add {
                    it.close()
                }
                while (!it.isClosed) {
                    it.accept()
                }
            }
        } catch (e: InterruptedException) {
            if (!errorIgnore) error("[TCP Start Error]", e)
        } catch (bindError: BindException) {
            if (!errorIgnore) error("[Port Bind Error]", bindError)
        } catch (e: Exception) {
            if (!errorIgnore) error("[NET Error]", e)
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
        closeSeq.eachAll {
            it()
        }
        errorIgnore = false
    }

    companion object {
        const val minLowWaterMark = 512 * 1024

        /** Maximum accepted single package size */
        const val maxPacketSizt = 50000000

        /** Packet header data length */
        const val headerSize = 8
    }
}