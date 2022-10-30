/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.game.simulation.gameFramework

import cn.rwhps.server.struct.ObjectMap
import cn.rwhps.server.struct.OrderedMap
import com.corrodinggames.rts.game.units.custom.l

object GameUnitData {
    @Suppress("UNCHECKED_CAST")

    fun getUnitData(coreName: String): OrderedMap<String, ObjectMap<String, Int>> {
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
}