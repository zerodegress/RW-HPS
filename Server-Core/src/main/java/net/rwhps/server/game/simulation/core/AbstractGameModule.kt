/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.game.simulation.core

import net.rwhps.server.core.game.ServerRoom
import net.rwhps.server.game.event.EventManage

/**
 * 通过这里的稳定接口来调用游戏内部实现
 *
 * @property useClassLoader 获取加载接口实现类的 [ClassLoader]]
 * @property eventManage EventManage
 * @property gameHessData AbstractGameHessData
 * @property gameNet AbstractGameNet
 * @property gameUnitData AbstractGameUnitData
 * @property gameFast AbstractGameFast
 * @property gameLinkFunction AbstractGameLinkFunction
 * @property gameLinkData AbstractGameLinkData
 * @property room ServerRoom
 */
interface AbstractGameModule {
    val useClassLoader: ClassLoader

    val eventManage: EventManage

    val gameHessData: AbstractGameHessData
    val gameNet: AbstractLinkGameNet
    val gameUnitData: AbstractGameUnitData
    val gameFast: AbstractGameFast

    val gameLinkFunction: AbstractLinkGameFunction
    val gameLinkData: AbstractLinkGameData

    val gameFunction: AbstractGameFunction

    val room: ServerRoom
}