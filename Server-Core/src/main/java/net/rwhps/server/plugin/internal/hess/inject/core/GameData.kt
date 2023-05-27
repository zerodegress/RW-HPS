/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.plugin.internal.hess.inject.core

import net.rwhps.server.game.simulation.core.AbstractGameData
import net.rwhps.server.util.file.FileName.getFileNameNoSuffix
import net.rwhps.server.util.log.Log

/**
 * @author RW-HPS/Dr
 */
internal class GameData : AbstractGameData {
    override fun saveGame() {
        GameEngine.gameEngine.ca.b(getFileNameNoSuffix(GameEngine.data.room.replayFileName),false)
        Log.clog("Save: ${getFileNameNoSuffix(GameEngine.data.room.replayFileName)}.rwsave")
    }
}