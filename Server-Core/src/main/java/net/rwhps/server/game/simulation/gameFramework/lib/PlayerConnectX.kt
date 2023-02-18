/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.game.simulation.gameFramework.lib

import com.corrodinggames.rts.gameFramework.j.NetEnginePackaging
import com.corrodinggames.rts.gameFramework.j.ad
import com.corrodinggames.rts.gameFramework.j.au
import com.corrodinggames.rts.gameFramework.j.c
import net.rwhps.server.core.thread.CallTimeTask
import net.rwhps.server.core.thread.Threads
import net.rwhps.server.data.HessModuleManage
import net.rwhps.server.data.global.Data
import net.rwhps.server.data.global.ServerRoom
import net.rwhps.server.data.player.AbstractPlayer
import net.rwhps.server.game.simulation.gameFramework.GameEngine
import net.rwhps.server.game.simulation.gameFramework.PrivateClass_Player
import net.rwhps.server.game.simulation.gameFramework.net.GameVersionServer
import net.rwhps.server.game.simulation.gameFramework.net.socket.HessSocket
import net.rwhps.server.io.GameInputStream
import net.rwhps.server.io.GameOutputStream
import net.rwhps.server.net.core.ConnectionAgreement
import net.rwhps.server.util.PacketType
import net.rwhps.server.util.log.Log
import java.util.concurrent.TimeUnit
import com.corrodinggames.rts.gameFramework.j.c as PlayerConnect

class PlayerConnectX(
    val netEngine: ad,
    val connectionAgreement: ConnectionAgreement
) : PlayerConnect(netEngine, HessSocket(connectionAgreement)) {

    val netEnginePackaging: NetEnginePackaging = NetEnginePackaging(netEngine, this)
    var room: ServerRoom = HessModuleManage.hessLoaderMap[this.javaClass.classLoader.toString()].room
    var player: AbstractPlayer? = null
    lateinit var serverConnect: GameVersionServer

    @Volatile
    private var closeFlag = false

    @Synchronized
    @Strictfp
    override fun a(p0: Boolean, p1: Boolean, p2: String?) {
        if (closeFlag) {
            return
        }
        closeFlag = true
        super.a(p0, p1, p2)
        serverConnect.disconnect()
    }

    override fun d() {
        // Register BIO
    }

    override fun a(packetHess: au) {
        if (player == null) {
            if (this.e() != "<null>") {
                player = room.playerManage.addAbstractPlayer(serverConnect,PrivateClass_Player(z))
                serverConnect.player = player!!
                Log.debug("[${room.roomID}] Join Player: ${player!!.name}")

//                com.corrodinggames.rts.game.n.k(player!!.site).r = player!!.site
//                GameEngine.netEngine.az = "/SD/rusted_warfare_maps/环湖 10P.tmx"
//                GameEngine.netEngine.ay.b = "环湖 10P.tmx"
//                room.mapName = "环湖 10P.tmx"
//                GameEngine.netEngine.ay.a = ai.b
//                GameEngine.netEngine.L()
//                player!!.sendSystemMessage("服务器地图锁定 [环湖 10P], 不允许更换")

                if (!Threads.containsTimeTask(CallTimeTask.CallTeamTask)) {
                    Threads.newTimedTask(CallTimeTask.CallTeamTask, 0, 1, TimeUnit.SECONDS) {
                        GameEngine.netEngine.e(null as c?)
                        GameEngine.netEngine.L()
                    }
                }

                player!!.sendSystemMessage("RW-HPS 2.0 Forward-looking Server")
                player!!.sendSystemMessage("RW-HPS 2.0 前瞻服务器")
            }
        }

        when (packetHess.b) {
            PacketType.START_GAME.typeInt -> room.isStartGame = true
            PacketType.SERVER_INFO.typeInt -> {
                GameInputStream(packetHess.c).use {
                    val o = GameOutputStream()
                    it.skip(it.readShort().toLong())
                    o.writeString(Data.SERVER_ID)

                    o.transferToFixedLength(it,8)

                    val length = it.readShort()
                    o.writeShort(length)
                    o.transferToFixedLength(it,length.toInt())

                    o.transferToFixedLength(it,15)

                    /* Admin Ui */
                    it.skip(1)
                    o.writeBoolean(player!!.isAdmin)
                    o.transferTo(it)
                    packetHess.c = o.getPacketBytes()
                }
            }
        }
        connectionAgreement.send(netEnginePackaging.transformPacket(packetHess))
    }

    override fun f(): String {
        return connectionAgreement.ip
    }

    override fun g(): String {
        return connectionAgreement.ip
    }
}