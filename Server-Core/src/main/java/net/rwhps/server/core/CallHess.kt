/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.core

import net.rwhps.server.core.thread.CallTimeTask
import net.rwhps.server.core.thread.Threads
import net.rwhps.server.data.HessModuleManage
import net.rwhps.server.data.global.Data
import net.rwhps.server.data.global.ServerRoom
import net.rwhps.server.data.player.AbstractPlayer
import net.rwhps.server.util.Time
import net.rwhps.server.util.log.Log
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * @author RW-HPS/Dr
 */
class CallHess(private val serverRoom: ServerRoom) {
    fun sendSystemMessage(text: String) {
        serverRoom.playerManage.playerGroup.eachAll { e: AbstractPlayer -> e.sendSystemMessage(text) }
    }

    fun sendSystemMessageLocal(text: String, vararg obj: Any) {
        serverRoom.playerManage.playerGroup.eachAll { e: AbstractPlayer -> e.sendSystemMessage(e.i18NBundle.getinput(text, *obj)) }
    }

    fun sendSystemTeamMessageLocal(team: Int, text: String, vararg obj: Any) {
        serverRoom.playerManage.playerGroup.eachAllFind({ e: AbstractPlayer -> e.team == team }) { p: AbstractPlayer -> p.sendSystemMessage("[TEAM] " + p.i18NBundle.getinput(text, *obj)) }
    }

    fun sendSystemMessage(text: String, vararg obj: Any) {
        serverRoom.playerManage.playerGroup.eachAll { e: AbstractPlayer -> e.sendSystemMessage(e.i18NBundle.getinput(text, *obj)) }
    }

    @JvmOverloads
    fun killAllPlayer(msg: String = "Game Over") {
        serverRoom.playerManage.playerGroup.eachAll { e: AbstractPlayer ->
            try {
                e.kickPlayer(msg)
            } catch (err: IOException) {
                Log.error("[ALL] Kick All Player Error", e)
            }
        }
    }

    fun startCheckThread() {
        var aiEndTime = 0
        Threads.newTimedTask(CallTimeTask.AutoCheckTask, 0, 1, TimeUnit.SECONDS) {
            if ((Data.configServer.MaxGameIngTime != -1 && Time.concurrentSecond() > serverRoom.endTime)) {
                if (serverRoom.flagData.forcedCloseSendMsg) {
                    sendSystemMessageLocal("gameOver.forced")
                }
                serverRoom.flagData.forcedCloseSendMsg = false
                if (Time.concurrentSecond() > Data.game.endTime + 60) {
                    serverRoom.gr()
                    return@newTimedTask
                }
            }

            if (serverRoom.flagData.ai) {
                if (serverRoom.flagData.aiWarn) {
                    serverRoom.flagData.aiWarn = false
                    sendSystemMessageLocal("gameOver.ai")
                }
                if (serverRoom.playerManage.playerGroup.size == 0) {
                    if (aiEndTime != 0) {
                        aiEndTime = Time.concurrentSecond()+Data.configServer.MaxOnlyAIGameIngTime
                    } else if (Data.configServer.MaxOnlyAIGameIngTime != -1 && Time.concurrentSecond() > aiEndTime) {
                        serverRoom.gr()
                        return@newTimedTask
                    }
                } else {
                    aiEndTime = 0
                }
            } else {
                when (serverRoom.playerManage.playerGroup.size) {
                    0 -> serverRoom.gr()
                    1 -> if (serverRoom.flagData.oneSay) {
                        serverRoom.flagData.oneSay = false
                        sendSystemMessageLocal("gameOver.oneMin")
                        Threads.newCountdown(CallTimeTask.GameOverTask, 1, TimeUnit.MINUTES) { serverRoom.gr() }
                    }

                    else -> {
                        if (Threads.containsTimeTask(CallTimeTask.GameOverTask)) {
                            serverRoom.flagData.oneSay = true
                            Threads.closeTimeTask(CallTimeTask.GameOverTask)
                        }
                    }
                }
            }

            if (serverRoom.flagData.sendGameStatusFlag) {
                serverRoom.gameOverData = HessModuleManage.hps.gameHessData.getGameOverData() ?: return@newTimedTask

                serverRoom.flagData.sendGameStatusFlag = false

                val last = serverRoom.gameOverData!!.winPlayerList.toArray(String::class.java).contentToString()

                Log.clog("[${serverRoom.roomID}] Last Win Player: {0}", last)
                sendSystemMessageLocal("survive.player", last)
            }
        }
    }
}