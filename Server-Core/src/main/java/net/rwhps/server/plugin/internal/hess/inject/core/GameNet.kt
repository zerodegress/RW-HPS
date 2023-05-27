/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.plugin.internal.hess.inject.core

import com.corrodinggames.rts.game.n
import com.corrodinggames.rts.gameFramework.j.CustomServerSocket
import com.corrodinggames.rts.gameFramework.j.ad
import com.corrodinggames.rts.gameFramework.j.c
import net.rwhps.server.data.global.Data
import net.rwhps.server.game.event.EventType
import net.rwhps.server.game.simulation.core.AbstractGameNet
import net.rwhps.server.util.game.Events
import net.rwhps.server.util.inline.findField
import net.rwhps.server.util.log.Log
import java.io.IOException
import com.corrodinggames.rts.gameFramework.j.ao as ServerAcceptRunnable

/**
 * @author RW-HPS/Dr
 */
internal class GameNet : AbstractGameNet {
    override fun newConnect(ip: String, name: String) {
        try {
            //val settingsEngine = GameEngine.settingsEngine
            val netEngine = GameEngine.netEngine

            //settingsEngine.lastNetworkPlayerName = name

            //val playerName = settingsEngine.lastNetworkPlayerName

            netEngine.y = name
            val kVar2 = ad.b(ip,false)
            netEngine.a(kVar2)
            val it: Iterator<*> = netEngine.aM.iterator()
            while (it.hasNext()) {
                (it.next() as c).i = true
            }
        } catch (e2: IOException) {
            Log.error("[GameCore] NewConnect Error",e2)
        }
    }

    override fun startHessPort(port: Int, passwd: String?, name: String) {
//        for (a in (GameEngine.gameEngine.bZ::class.java.findField("e")!![GameEngine.gameEngine.bZ] as ArrayList<b>)) {
//            a.U.iterator().forEach {
//                Log.clog(it.toString())
//            }
//        }

        val netEngine = GameEngine.netEngine
        GameEngine.settingsEngine.networkPort = port
        GameEngine.settingsEngine.udpInMultiplayer = false
        GameEngine.settingsEngine.saveMultiplayerReplays = Data.configServer.SaveRePlayFile
        netEngine.m = port
        netEngine.y = name

        GameEngine.data.room.run {
            roomID = "Port: $port"
            startServer = {
                try {
                    GameEngine.root.hostStartWithPasswordAndMods(false, passwd, true)

                    val tcp = GameEngine.netEngine::class.java.findField("aE", ServerAcceptRunnable::class.java)!!

                    // 设置新的监听
                    val tcpRunnable = CustomServerSocket(GameEngine.netEngine)
                    tcpRunnable.a(false)
                    tcp.set(GameEngine.netEngine, tcpRunnable)
                    GameEngine.netEngine::class.java.findField("aD", Thread::class.java)!!
                        .set(GameEngine.netEngine, Thread(tcpRunnable).apply { start() })

                    n.b(Data.configServer.MaxPlayer, true)

                    // 隐藏 Hess(HOST)
                    n.k(0).run {
                        netEngine.a(this, -3)
                        I()
                    }
                    // 避免同步爆炸
                    GameEngine.netEngine.z.k = -3

                    Events.fire(EventType.ServerHessStartPort())

                } catch (e: Exception) {
                    Log.error(e)
                }
            }.also { it() }
        }
    }
}