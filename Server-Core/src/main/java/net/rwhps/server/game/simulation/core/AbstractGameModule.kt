/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.game.simulation.core

import net.rwhps.server.data.global.ServerRoom

/**
 * 通过这里的稳定接口来调用游戏内部实现
 *
 * @property useClassLoader 获取加载接口实现类的 [ClassLoader]]
 * @property gameHessData AbstractGameHessData
 * @property gameNet AbstractGameNet
 * @property gameUnitData AbstractGameUnitData
 * @property gameFast AbstractGameFast
 * @property room ServerRoom
 */
interface AbstractGameModule {
    val useClassLoader: ClassLoader

    val gameHessData: AbstractGameHessData
    val gameNet: AbstractGameNet
    val gameUnitData: AbstractGameUnitData
    val gameFast: AbstractGameFast

    val gameData: AbstractGameData
    val gameDataLink: AbstractGameLinkData
    val room: ServerRoom
}