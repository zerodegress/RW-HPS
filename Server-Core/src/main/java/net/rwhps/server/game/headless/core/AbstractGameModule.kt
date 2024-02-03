/*
 * Copyright 2020-2024 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.game.headless.core

import net.rwhps.server.game.event.EventManage
import net.rwhps.server.game.headless.core.link.AbstractLinkGameData
import net.rwhps.server.game.headless.core.link.AbstractLinkGameFunction
import net.rwhps.server.game.headless.core.link.AbstractLinkGameNet
import net.rwhps.server.game.headless.core.scripts.AbstractScriptMultiPlayer
import net.rwhps.server.game.headless.core.scripts.AbstractScriptRoot
import net.rwhps.server.game.room.ServerRoom

/**
 * 通过这里的稳定接口来调用游戏内部实现
 *
 * @property useClassLoader 获取加载接口实现类的 [ClassLoader]]
 * @property eventManage EventManage
 * @property gameHessData AbstractGameHessData
 * @property gameLinkNet AbstractGameNet
 * @property gameUnitData AbstractGameUnitData
 * @property gameFast AbstractGameFast
 * @property gameLinkFunction AbstractGameLinkFunction
 * @property gameLinkData AbstractGameLinkData
 * @property room ServerRoom
 */
interface AbstractGameModule {
    val useClassLoader: ClassLoader

    val eventManage: EventManage

    val gameFast: AbstractGameFast
    val gameHessData: AbstractGameHessData
    val gameUnitData: AbstractGameUnitData
    val gameFunction: AbstractGameFunction

    val gameScriptMultiPlayer: AbstractScriptMultiPlayer
    val gameScriptRoot: AbstractScriptRoot

    val gameLinkFunction: AbstractLinkGameFunction
    val gameLinkData: AbstractLinkGameData
    val gameLinkNet: AbstractLinkGameNet
    val room: ServerRoom
}