/*
 * Copyright 2020-2024 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.plugin.internal.headless.inject.core.link

import com.corrodinggames.rts.game.n
import com.corrodinggames.rts.gameFramework.j.CustomServerSocket
import com.corrodinggames.rts.gameFramework.j.ad
import com.corrodinggames.rts.gameFramework.j.c
import net.rwhps.server.data.global.Data
import net.rwhps.server.game.event.game.ServerHessStartPort
import net.rwhps.server.game.headless.core.link.AbstractLinkGameNet
import net.rwhps.server.plugin.internal.headless.inject.core.GameEngine
import net.rwhps.server.util.inline.findField
import net.rwhps.server.util.log.Log
import java.io.IOException
import com.corrodinggames.rts.gameFramework.j.ao as ServerAcceptRunnable

/**
 * @author Dr (dr@der.kim)
 */
internal class LinkGameNet: AbstractLinkGameNet {
    private var lastUsePort = 0
    private var lastUsePasswd: String? = ""

    override fun newConnect(ip: String, name: String) {
        try {
            //val settingsEngine = GameEngine.settingsEngine
            val netEngine = GameEngine.netEngine

            //settingsEngine.lastNetworkPlayerName = name

            //val playerName = settingsEngine.lastNetworkPlayerName

            netEngine.y = name
            val kVar2 = ad.b(ip, false)
            netEngine.a(kVar2)
            val it: Iterator<*> = netEngine.aM.iterator()
            while (it.hasNext()) {
                (it.next() as c).i = true
            }
        } catch (e2: IOException) {
            Log.error("[GameCore] NewConnect Error", e2)
        }
    }

    override fun startHeadlessServer(port: Int, passwd: String?) {
        val netEngine = GameEngine.netEngine
        GameEngine.settingsEngine.apply {
            setValueDynamic("networkPort", port.toString())
            setValueDynamic("udpInMultiplayer", false.toString())
            setValueDynamic("saveMultiplayerReplays", Data.configServer.saveRePlayFile.toString())
        }

        netEngine.m = port
        netEngine.y = Data.headlessName

        GameEngine.data.room.run {
            roomID = "Port: $port"

            try {
                GameEngine.root.hostStartWithPasswordAndMods(false, passwd, true)

                // 设置新的监听
                val tcpRunnable = CustomServerSocket(GameEngine.netEngine)
                tcpRunnable.a(false)

                // SocketServer
                GameEngine.netEngine::class.java.findField("aE", ServerAcceptRunnable::class.java)!!
                    .set(GameEngine.netEngine, tcpRunnable)
                // SocketServer Thread
                GameEngine.netEngine::class.java.findField("aD", Thread::class.java)!!
                    .set(GameEngine.netEngine, Thread(tcpRunnable).apply { start() })

                // 通过特性来干掉自带的 HOST
                // 新建一个 Player[]
                n.F()
                // 进行扩展
                n.b(Data.configServer.maxPlayer, true)
                // 避免同步爆炸, 给自身设置一个隐藏队伍
                GameEngine.netEngine.z.k = -1

                // 不能在启动前设置, 因为每次会变
                // 设置客户端UUID, 避免Admin/ban等不能持久化
                GameEngine.settingsEngine.setValueDynamic("networkServerId", Data.core.serverConnectUuid)

                lastUsePort = port
                lastUsePasswd = passwd

                GameEngine.data.eventManage.fire(ServerHessStartPort())
            } catch (e: Exception) {
                Log.error(e)
            }
        }
    }

    override fun closeHeadlessServer() {
        GameEngine.data.room.call.killAllPlayer()

        // 恢复
        GameEngine.netEngine.z = null

        GameEngine.root.multiplayer.disconnect("Close Server")
    }

    override fun reBootServer(run: () -> Unit) {
        if (lastUsePort == 0) {
            run()
            startHeadlessServer()
        } else {
            closeHeadlessServer()
            run()
            startHeadlessServer(lastUsePort, lastUsePasswd)
        }
    }
}