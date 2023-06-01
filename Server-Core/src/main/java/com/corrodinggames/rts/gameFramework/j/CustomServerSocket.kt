/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.corrodinggames.rts.gameFramework.j

import net.rwhps.server.net.NetService
import net.rwhps.server.plugin.internal.hess.inject.core.GameEngine
import net.rwhps.server.plugin.internal.hess.inject.net.socket.StartGameHessNetTcp
import net.rwhps.server.util.inline.findField
import net.rwhps.server.util.inline.ifResult
import net.rwhps.server.util.log.Log
import java.io.Closeable
import com.corrodinggames.rts.gameFramework.j.ao as ServerAcceptRunnable
import com.corrodinggames.rts.gameFramework.l as GameEe

/**
 * 覆写 Game-Lib 的端口监听, 来实现 BIO->NIO
 *
 * @property netEngine NetEngine
 * @property netService NetService
 * @property port Int
 * @constructor
 *
 * @author RW-HPS/Dr
*/
class CustomServerSocket(var1: ad) : ServerAcceptRunnable(var1), Closeable {
    private val netEngine: ad = this::class.java.findField("r",ad::class.java)!!.get(this)!! as ad
    private var netService: NetService? = null
    private var port = 0

    override fun run() {
        if (f) {
            Log.clog("Does not support UDP")
            return
        }
        GameEe.aq()
        Thread.currentThread().name = "NewConnectionWorker-" + (if (f) "udp" else "tcp") + " - " + this.e

        GameEngine.data.room.closeServer = {
            GameEngine.data.room.call.killAllPlayer()

            val site = GameEngine.data.room.playerManage.playerAll.ifResult({ it.size > 0}, { it[0].site }, { 0 })
            // 恢复
            GameEngine.netEngine.z.k = site

            GameEngine.root.multiplayer.disconnect("closeServer")
            GameEngine.root.multiplayer.disconnect("closeServer")
        }

        netService!!.openPort(port)
    }

    override fun b() {
        close()
    }

    override fun a(p0: Boolean) {
        startPort(p0)
    }

    private fun startPort(udp: Boolean) {
        f = udp
        port = netEngine.m
        Log.debug("[ServerSocket] starting socket.. ${if (udp) "udp" else "tcp"} port: $port")
        netService = NetService(StartGameHessNetTcp(netEngine))
    }

    override fun close() {
        Log.debug("[Close]")
        netService!!.stop()
    }
}