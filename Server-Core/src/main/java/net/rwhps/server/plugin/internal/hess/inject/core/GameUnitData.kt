/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.plugin.internal.hess.inject.core

import com.corrodinggames.rts.game.units.custom.ag
import com.corrodinggames.rts.game.units.custom.l
import com.corrodinggames.rts.game.units.y
import com.corrodinggames.rts.gameFramework.w
import net.rwhps.server.core.Call
import net.rwhps.server.game.simulation.core.AbstractGameUnitData
import net.rwhps.server.struct.ObjectMap
import net.rwhps.server.struct.OrderedMap
import net.rwhps.server.util.log.Log

/**
 * @author RW-HPS/Dr
 */
internal class GameUnitData : AbstractGameUnitData {
    override fun reloadUnitData() {
        ag.h()
    }

    @Suppress("UNCHECKED_CAST")
    override fun getUnitData(coreName: String): OrderedMap<String, ObjectMap<String, Int>> {
        val modsData = OrderedMap<String, ObjectMap<String, Int>>()
        val gameUnitDataList: List<l> = l.c as List<l>

        for(data in gameUnitDataList) {
            val group = data.t() ?: coreName
            if (modsData.containsKey(group)) {
                modsData.get(group).put(data.M,data.H)
            } else {
                val cache = OrderedMap<String, Int>()
                modsData.put(group,cache)
                cache.put(data.M,data.H)
            }
        }
        return modsData
    }

    fun kill() {
        for (fastUnit in w.er.a()) {
            if (fastUnit is y) {
                w.er.remove(fastUnit)
                GameEngine.gameEngine.bU.a(fastUnit)
                Log.clog(fastUnit.r().i())
                Call.sendSystemMessage("删掉了一个单位: ${fastUnit.r().i()}")
            }
        }
        // 让 Core 完成记载
        Thread.sleep(100)

        Call.sendSync(false)
    }
}