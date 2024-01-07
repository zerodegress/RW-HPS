/*
 *
 *  * Copyright 2020-2023 RW-HPS Team and contributors.
 *  *
 *  * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *  *
 *  * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 *
 */

package net.rwhps.server.game.enums

import net.rwhps.server.struct.map.IntMap
import net.rwhps.server.util.inline.ifNullResult
import net.rwhps.server.util.log.exp.VariableException

/**
 * @date 2023/8/6 20:07
 * @author Dr (dr@der.kim)
 */
enum class GamePingActions {
    NORMAL,
    ATTACK,
    DEFEND,
    NUKE,
    BUILD,
    UPGRADE,
    OK,
    NO,
    HAPPY,
    SAD,
    RETREAT,
    UNKNOWN;

    companion object {
        private val actionMap: IntMap<GamePingActions> = IntMap(entries.size)

        init {
            entries.forEach {
                if (actionMap.containsKey(it.ordinal)) {
                    throw VariableException.RepeatAddException("[GameUnitType -> GameActions]")
                }
                actionMap[it.ordinal] = it
            }
        }

        // 进行全匹配 查看是否在游戏内置列表中
        fun from(type: String?): GamePingActions = entries.find { it.name == type?.removePrefix("c_6_")?.uppercase() }.ifNullResult(UNKNOWN) { it }

        fun from(type: Int): GamePingActions = actionMap[type].ifNullResult(UNKNOWN) { it }
    }
}