/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.game

import net.rwhps.server.core.thread.CallTimeTask
import net.rwhps.server.core.thread.Threads
import net.rwhps.server.data.HessModuleManage
import net.rwhps.server.data.MapManage
import net.rwhps.server.data.event.GameOverData
import net.rwhps.server.data.global.Data
import net.rwhps.server.data.player.AbstractPlayer
import net.rwhps.server.net.Administration.PlayerInfo
import net.rwhps.server.plugin.event.AbstractEvent
import net.rwhps.server.util.Time.millis
import net.rwhps.server.util.inline.coverConnect
import net.rwhps.server.util.log.Log
import net.rwhps.server.util.log.Log.error
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * @author RW-HPS/Dr
 */
class Event : AbstractEvent {
    override fun registerServerHessStartPort() {
        HessModuleManage.hps.gameDataLink.maxUnit = Data.configServer.MaxUnit
        HessModuleManage.hps.gameDataLink.income = Data.configServer.DefIncome

        if (Data.config.AutoUpList) {
            Data.SERVER_COMMAND.handleMessage("uplist add", Data.defPrint)
        }
    }

    override fun registerPlayerJoinEvent(player: AbstractPlayer) {
        if (player.name.isBlank() || player.name.length > 30) {
            player.kickPlayer(player.getinput("kick.name.failed"))
            return
        }

        if (Data.core.admin.bannedUUIDs.contains(player.connectHexID)) {
            try {
                player.kickPlayer(player.i18NBundle.getinput("kick.ban"))
            } catch (ioException: IOException) {
                error("[Player] Send Kick Player Error", ioException)
            }
            return
        }

        if (Data.core.admin.playerDataCache.containsKey(player.connectHexID)) {
            val info = Data.core.admin.playerDataCache[player.connectHexID]
            if (info.timesKicked > millis()) {
                try {
                    player.kickPlayer(player.i18NBundle.getinput("kick.you.time"))
                } catch (ioException: IOException) {
                    error("[Player] Send Kick Player Error", ioException)
                }
                return
            } else {
                player.muteTime = info.timeMute
            }
        }

        HessModuleManage.hps.room.call.sendSystemMessage(Data.i18NBundle.getinput("player.ent", player.name))
        Log.clog("&c"+Data.i18NBundle.getinput("player.ent", player.name))

        if (Data.configServer.AutoStartMinPlayerSize != -1 &&
            HessModuleManage.hps.room.playerManage.playerGroup.size >= Data.configServer.AutoStartMinPlayerSize &&
            !Threads.containsTimeTask(CallTimeTask.AutoStartTask)) {
            var flagCount = 60
            Threads.newTimedTask(CallTimeTask.AutoStartTask,0,1,TimeUnit.SECONDS){
                if (HessModuleManage.hps.room.isStartGame) {
                    Threads.closeTimeTask(CallTimeTask.AutoStartTask)
                    return@newTimedTask
                }

                flagCount--

                if (flagCount > 0) {
                    if ((flagCount - 5) > 0) {
                        HessModuleManage.hps.room.call.sendSystemMessage(Data.i18NBundle.getinput("auto.start",flagCount))
                    }
                    return@newTimedTask
                }

                Threads.closeTimeTask(CallTimeTask.AutoStartTask)
                Threads.closeTimeTask(CallTimeTask.PlayerAfkTask)

                Data.CLIENT_COMMAND.register("start",null) { HessModuleManage.hps.room.playerManage.playerGroup.find { it.isAdmin } }
            }
        }

        if (Data.configServer.EnterAd.isNotBlank()) {
            player.sendSystemMessage(Data.configServer.EnterAd)
        }
        // ConnectServer("127.0.0.1",5124,player.con)
    }

    override fun registerPlayerLeaveEvent(player: AbstractPlayer) {
        if (Data.configServer.OneAdmin &&
            player.isAdmin &&
            player.autoAdmin &&
            HessModuleManage.hps.room.playerManage.playerGroup.size > 0) {
                HessModuleManage.hps.room.playerManage.playerGroup.eachFind({ !it.isAdmin }) {
                    it.isAdmin = true
                    it.autoAdmin = true
                    player.isAdmin = false
                    player.autoAdmin = false
                    HessModuleManage.hps.room.call.sendSystemMessage("give.ok", it.name)
                }
        }

        Data.core.admin.playerDataCache.put(player.connectHexID, PlayerInfo(player.connectHexID, player.kickTime, player.muteTime))

        if (HessModuleManage.hps.room.isStartGame) {
            HessModuleManage.hps.room.call.sendSystemMessage("player.dis", player.name)
        } else {
            HessModuleManage.hps.room.call.sendSystemMessage("player.disNoStart", player.name)
        }
        Log.clog("&c" + Data.i18NBundle.getinput("player.dis", player.name))

        if (Data.configServer.AutoStartMinPlayerSize != -1 &&
            HessModuleManage.hps.room.playerManage.playerGroup.size <= Data.configServer.AutoStartMinPlayerSize &&
            Threads.containsTimeTask(CallTimeTask.AutoStartTask)
        ) {
            Threads.closeTimeTask(CallTimeTask.AutoStartTask)
        }
    }

    override fun registerGameStartEvent() {
        Data.core.admin.playerDataCache.clear()

        if (Data.configServer.StartAd.isNotBlank()) {
            HessModuleManage.hps.room.call.sendSystemMessage(Data.configServer.StartAd)
        }

        Log.clog("[Start New Game]")
    }

    override fun registerGameOverEvent(gameOverData: GameOverData?) {
        MapManage.maps.mapData?.clean()

        System.gc()
    }

    override fun registerPlayerBanEvent(player: AbstractPlayer) {
        Data.core.admin.bannedUUIDs.add(player.connectHexID)
        Data.core.admin.bannedIPs.add(player.con!!.coverConnect().ip)
        try {
            player.kickPlayer(player.i18NBundle.getinput("kick.ban"))
        } catch (ioException: IOException) {
            error("[Player] Send Kick Player Error", ioException)
        }
        HessModuleManage.hps.room.call.sendSystemMessage("ban.yes", player.name)
    }

    override fun registerPlayerIpBanEvent(player: AbstractPlayer) {
        Data.core.admin.bannedIPs.add(player.con!!.coverConnect().ip)
        try {
            player.kickPlayer("kick.ban")
        } catch (ioException: IOException) {
            error("[Player] Send Kick Player Error", ioException)
        }
        HessModuleManage.hps.room.call.sendSystemMessage("ban.yes", player.name)
    }
}