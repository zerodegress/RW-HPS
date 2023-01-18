/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.game.simulation.gameFramework

import com.corrodinggames.rts.gameFramework.j.ad
import com.corrodinggames.rts.gameFramework.l
import net.rwhps.server.data.HessModuleManage
import net.rwhps.server.game.simulation.core.AbstractGameData
import net.rwhps.server.game.simulation.core.AbstractGameModule
import net.rwhps.server.game.simulation.core.AbstractGameNet
import net.rwhps.server.game.simulation.core.AbstractGameUnitData

internal object GameEngine {
    val gameEngine: l = l.B()
    val netEngine: ad = gameEngine.bX

    val settingsEngine = gameEngine.bQ!!

    val gameStatistics = gameEngine.bY!!

    @JvmStatic
    fun init() {
        val loader = GameEngine.javaClass.classLoader
        HessModuleManage.addGameModule(loader.toString(), object: AbstractGameModule {
            override val useClassLoader: ClassLoader = loader
            override val gameData: AbstractGameData = GameData()
            override val gameNet: AbstractGameNet = GameNet()
            override val gameUnitData: AbstractGameUnitData = GameUnitData()
        })
    }
}