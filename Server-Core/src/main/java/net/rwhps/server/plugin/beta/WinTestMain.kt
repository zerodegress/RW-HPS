/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.plugin.beta

import net.rwhps.server.data.EventManage
import net.rwhps.server.data.global.Data
import net.rwhps.server.data.plugin.PluginData
import net.rwhps.server.game.event.core.EventListenerHost
import net.rwhps.server.game.event.game.ServerGameOverEvent
import net.rwhps.server.plugin.Plugin
import net.rwhps.server.util.ExtractUtils
import net.rwhps.server.util.annotations.core.EventListenerHandler
import net.rwhps.server.util.file.FileUtils

/**
 * @author RW-HPS/Dr
 */
class WinTestMain: Plugin() {
    override fun registerEvents(eventManage: EventManage) {
        eventManage.registerListener(object: EventListenerHost {
            @EventListenerHandler
            fun registerGameOverEvent(gameOverEvent: ServerGameOverEvent) {
                if (gameOverEvent.gameOverData == null) {
                    return
                }

                ExtractUtils.tryRunTest {
                    gameOverEvent.gameOverData.run {
                        val data = PluginData()
                        data.setFileUtil(FileUtils.getFolder(Data.Plugin_Data_Path).toFile("T.bin"))
                        data.setData("All Player", allPlayerList)
                        data.setData("Win Player", winPlayerList)
                        data.setData("Map Name", mapName)
                        data.setData("Player Data", playerData)
                        data.setData("RePlay Size", FileUtils.getFolder(Data.Plugin_RePlays_Path).toFile(replayName).readFileByte())
                        data.save()
                    }
                }
            }
        })
    }
}