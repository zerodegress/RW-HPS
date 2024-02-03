/*
 * Copyright 2020-2024 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.plugin.internal.headless.inject.net.socket

import com.corrodinggames.rts.gameFramework.j.ad
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import net.rwhps.server.data.global.Data
import net.rwhps.server.util.file.plugin.PluginManage
import net.rwhps.server.game.event.global.NetConnectNewEvent
import net.rwhps.server.io.packet.Packet
import net.rwhps.server.net.core.AbstractNet
import net.rwhps.server.net.core.ConnectionAgreement
import net.rwhps.server.net.core.INetServerHandler
import net.rwhps.server.plugin.internal.headless.inject.lib.PlayerConnectX
import net.rwhps.server.plugin.internal.headless.inject.net.GameVersionServer
import net.rwhps.server.plugin.internal.headless.inject.net.TypeHessRwHps
import net.rwhps.server.util.log.Log

/**
 * @author Dr (dr@der.kim)
 */
@ChannelHandler.Sharable
class NewServerHessHandler(private val netEngine: ad, abstractNet: AbstractNet): INetServerHandler(abstractNet) {
    @Throws(Exception::class)
    override fun channelRead0(ctx: ChannelHandlerContext, msg: Any?) {
        if (msg == null) {
            return
        }

        try {
            if (msg is Packet) {
                val attr = abstractNet.getTypeConnect(ctx.channel())
                var type = attr.get()

                if (type == null) {
                    val connectionAgreement = ConnectionAgreement(ctx, abstractNet.nettyChannelData)

                    val playerConnect = PlayerConnectX(netEngine, connectionAgreement)
                    playerConnect.h = false // UDP
                    playerConnect.i = false // UDP
                    playerConnect.d()

                    netEngine.aM.add(playerConnect)

                    type = TypeHessRwHps(GameVersionServer(playerConnect))
                    attr.setIfAbsent(type)
                    type.setData(playerConnect)

                    val newConnectEvent = NetConnectNewEvent(connectionAgreement)
                    PluginManage.runGlobalEventManage(newConnectEvent).await()
                    if (newConnectEvent.result) {
                        type.abstractNetConnect.disconnect()
                        return
                    }
                }

                type.processConnect(msg)
            }
        } catch (ss: Exception) {
            Log.error(ss)
        }
    }

    @Deprecated("Deprecated in Netty")
    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable?) {
        val sb = StringBuilder()
        val stack = cause!!.stackTrace

        for (ste in stack) {
            val className = "${ste.className}.${ste.methodName}"
            if (!className.contains("net.rwhps.server.util.log.Log")) {
                sb.append("[").append(ste.fileName).append("] : ").append(ste.methodName).append(" : ").append(ste.lineNumber)
                    .append(Data.LINE_SEPARATOR)
                break
            }
        }
        Log.error(sb.toString())
    }
}