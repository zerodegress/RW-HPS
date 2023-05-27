/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.data.event

import net.rwhps.server.struct.ObjectMap
import net.rwhps.server.struct.Seq

/**
 * @author RW-HPS/Dr
 */
data class GameOverData(
    val gameTime: Int,
    val allPlayerList: Seq<String>,
    val winPlayerList: Seq<String>,
    val mapName: String,
    val playerData: ObjectMap<String,ObjectMap<String,Int>>,
    val replayName: String
) {
    override fun toString(): String {
        return """
            allPlayerList=$allPlayerList
            winPlayerList=$winPlayerList
            mapName=$mapName
            playerData=$playerData
            replayName=$replayName
            """.trimMargin()
    }
}