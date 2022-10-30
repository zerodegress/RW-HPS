/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.game.simulation.gameFramework

import cn.rwhps.server.data.global.Data
import cn.rwhps.server.io.packet.Packet
import cn.rwhps.server.util.PacketType
import com.corrodinggames.rts.game.n
import com.corrodinggames.rts.gameFramework.j.al
import com.corrodinggames.rts.gameFramework.l
import com.corrodinggames.rts.gameFramework.j.`as` as GameNetOutStream

object GameData {
    @JvmStatic
    fun getGameData(): Packet {
        val gameEngine: l = GameEngine.gameEngine
        val arVar = GameNetOutStream()
        arVar.c(0)
        arVar.a(gameEngine.bx)
        arVar.a(gameEngine.by)
        arVar.a(Data.game.income)
        arVar.a(1.0f)
        arVar.a(false)
        arVar.a(false)
        arVar.e("gameSave")
        gameEngine.ca.a(arVar)
        arVar.a("gameSave")
        return Packet(35,arVar.b(35).c)
    }

    @JvmStatic
    fun getGameCheck(): Packet {
        val netEngine = GameEngine.netEngine
        val arVar = GameNetOutStream()
        arVar.a(netEngine.ah)
        arVar.a(netEngine.am.a)
        arVar.a(netEngine.am.b.size)
        val it: Iterator<*> = netEngine.am.b.iterator()
        while (it.hasNext()) {
            arVar.a((it.next() as al).b)
        }
        return Packet(PacketType.SYNC_CHECK,arVar.b(PacketType.SYNC_CHECK.typeInt).c)
    }

    @JvmStatic
    fun getWin(team: Int): Boolean {
        val teamData: n = n.k(team) ?: return false

        // [is victorious!]
        return !teamData.b() && !teamData.G && !teamData.F && !teamData.E
    }

    @JvmStatic
    fun clean() {
        val gameEngine: l = GameEngine.gameEngine
        gameEngine.bX.b("exited");
    }
}
