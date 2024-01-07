/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.plugin.beta.http.data

import net.rwhps.server.game.HessModuleManage
import net.rwhps.server.game.event.game.ServerGameOverEvent.GameOverData
import net.rwhps.server.struct.map.ObjectMap
import net.rwhps.server.struct.list.Seq
import net.rwhps.server.struct.list.SeqSave
import net.rwhps.server.util.SystemUtils

/**
 * @date  2023/6/27 16:03
 * @author Dr (dr@der.kim)
 */
internal object GetData {
    const val ServerTypeClose = "{ \"Status\": \"Unsupported protocols, only Server is supported\" }"
    val consoleCache = SeqSave(20)
    val agentConsole = Seq<String>()
    val agentConsoleLog = ObjectMap<String, (String) -> Unit>()

    data class GameOverPositive(
        val gameOverData: GameOverData
    ) {
        companion object {
            val data = SeqSave(10)
        }
    }

    data class SystemInfo(
        val system: String = SystemUtils.osName,
        val arch: String = SystemUtils.osArch,
        val jvmName: String = SystemUtils.javaName,
        val jvmVersion: String = SystemUtils.javaVersion
    )

    data class GameInfo(
        val income: Float = HessModuleManage.hps.gameLinkData.income,
        val noNukes: Boolean = HessModuleManage.hps.gameLinkData.nukes,
        val credits: Int = HessModuleManage.hps.gameLinkData.credits,
        val sharedControl: Boolean = HessModuleManage.hps.gameLinkData.sharedcontrol,
        val startGame: Boolean = HessModuleManage.hps.room.isStartGame,
        val players: List<String> = Seq<String>().apply {
            HessModuleManage.hps.room.playerManage.playerAll.eachAll {
                add(it.playerInfo)
            }
        }
    )
}