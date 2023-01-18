/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.game.simulation.gameFramework

import com.corrodinggames.rts.game.n
import net.rwhps.server.game.simulation.core.AbstractPlayerData

internal class PrivateClass_Player(private val playerData: n) : AbstractPlayerData {
    override val survive get() = (!playerData.b() && !playerData.G && !playerData.F && !playerData.E)

    private val gameStatistics = GameEngine.gameStatistics.a(playerData)
    /** 单位击杀数 */
    override val unitsKilled get() = gameStatistics.c
    /** 建筑毁灭数 */
    override val buildingsKilled get() = gameStatistics.d
    /** 单实验单位击杀数 */
    override val experimentalsKilled get() = gameStatistics.e
    /** 单位被击杀数 */
    override val unitsLost get() = gameStatistics.f
    /** 建筑被毁灭数 */
    override val buildingsLost get() = gameStatistics.g
    /** 单实验单位被击杀数 */
    override val experimentalsLost get() = gameStatistics.h

    /** 玩家的资金 */
    override var credits
        get() = playerData.o.toInt()
        set(value) { playerData.o = value.toDouble() }
}