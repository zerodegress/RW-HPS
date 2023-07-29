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
import com.corrodinggames.rts.game.units.am
import com.corrodinggames.rts.gameFramework.j.al
import com.corrodinggames.rts.gameFramework.l
import net.rwhps.server.data.MapManage
import net.rwhps.server.data.event.GameOverData
import net.rwhps.server.data.global.Data
import net.rwhps.server.data.global.NetStaticData
import net.rwhps.server.game.simulation.core.AbstractGameHessData
import net.rwhps.server.game.simulation.core.AbstractPlayerData
import net.rwhps.server.io.packet.Packet
import net.rwhps.server.net.core.IRwHps
import net.rwhps.server.struct.ObjectMap
import net.rwhps.server.struct.Seq
import net.rwhps.server.util.PacketType
import net.rwhps.server.util.Time
import net.rwhps.server.util.WaitResultUtils
import net.rwhps.server.util.log.Log
import net.rwhps.server.util.log.exp.ImplementedException
import com.corrodinggames.rts.gameFramework.j.`as` as GameNetOutStream

/**
 * @author RW-HPS/Dr
 */
internal class GameHessData: AbstractGameHessData {

    override val tickHess: Int get() = GameEngine.gameEngine.bx
    override val tickNetHess: Int get() = GameEngine.netEngine.X

    override var useMod: Boolean
        get() = GameEngine.netEngine.o
        set(value) {
            GameEngine.netEngine.o = value
        }

    override fun getGameCheck(): Packet {
        val netEngine = GameEngine.netEngine
        val arVar = GameNetOutStream()
        arVar.a(tickHess)
        arVar.a(netEngine.am.a)
        arVar.a(netEngine.am.b.size)
        val it: Iterator<*> = netEngine.am.b.iterator()
        while (it.hasNext()) {
            arVar.a((it.next() as al).b)
        }
        return Packet(PacketType.SYNC_CHECK, arVar.b(PacketType.SYNC_CHECK.typeInt).c)
    }

    override fun getWin(position: Int): Boolean {
        val teamData: n = n.k(position) ?: return false

        return !teamData.b() && !teamData.G && !teamData.F && !teamData.E
    }

    private fun getWin(player: n?): Boolean {
        val teamData: n = player ?: return false
        return !teamData.b() && !teamData.G && !teamData.F && !teamData.E
    }

    override fun getGameOverData(): GameOverData? {
        var lastWinTeam: Int = -1
        var lastWinCount = 0

        for (position in 0 until Data.configServer.maxPlayer) {
            val player: n = n.k(position) ?: continue
            if (getWin(player) && player.r != lastWinTeam) {
                lastWinTeam = player.r
                lastWinCount++
            }
        }

        if (lastWinCount == 1) {
            val winPlayer = Seq<String>().apply {
                for (position in 0 until Data.configServer.maxPlayer) {
                    val player: n = n.k(position) ?: continue
                    if (player.r == lastWinTeam) {
                        add(player.v)
                    }
                }
            }
            val allPlayer = Seq<String>()

            val statusData = ObjectMap<String, ObjectMap<String, Int>>().apply {
                for (position in 0 until Data.configServer.maxPlayer) {
                    val player: n = n.k(position) ?: continue
                    put(player.v, PrivateClass_Player(player).let {
                        ObjectMap<String, Int>().apply {
                            put("unitsKilled", it.unitsKilled)
                            put("buildingsKilled", it.buildingsKilled)
                            put("experimentalsKilled", it.experimentalsKilled)
                            put("unitsLost", it.unitsLost)
                            put("buildingsLost", it.buildingsLost)
                            put("experimentalsLost", it.experimentalsLost)
                        }
                    })
                    allPlayer.add(player.v)
                }
            }

            return GameOverData(
                    Time.concurrentSecond() - GameEngine.data.room.startTime,
                    allPlayer,
                    winPlayer,
                    MapManage.maps.mapName,
                    statusData,
                    GameEngine.data.room.replayFileName
            )
        } else {
            return null
        }
    }

    override fun getPlayerBirthPointXY() {
        for (player in GameEngine.data.room.playerManage.playerGroup) {
            n.k(player.site).let {
                var flagA = false
                var flagB = false
                var x: Float? = null
                var y: Float? = null
                var x2: Float? = null
                var y2: Float? = null

                for (amVar in am.bF()) {
                    if ((amVar is am) && !amVar.bV && amVar.bX == it) {
                        if (amVar.bO && !flagA) {
                            flagA = true
                            x = amVar.eo
                            y = amVar.ep
                        }
                        if (amVar.bP && !flagB) {
                            flagB = true
                            x2 = amVar.eo
                            y2 = amVar.ep
                        }
                    }
                }

                if (x == null) {
                    x = x2
                    y = y2
                }
                Log.clog("Position ${player.site} , $x $y")
            }
        }
    }


    override fun clean() {
        if (NetStaticData.ServerNetType != IRwHps.NetType.ServerProtocol) {
            return
        }
        val gameEngine: l = GameEngine.gameEngine
        gameEngine.bX.b("exited")
        InterruptedException().printStackTrace()
        Thread.sleep(100)
    }

    override fun getPlayerData(site: Int): AbstractPlayerData {
        return PrivateClass_Player(WaitResultUtils.waitResult { n.k(site) } ?: throw ImplementedException.PlayerImplementedException(
                "[PlayerData-New] Player is invalid"
        ))
    }
}
