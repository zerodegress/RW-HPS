/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.data.global

import net.rwhps.server.core.CallHess
import net.rwhps.server.core.thread.CallTimeTask
import net.rwhps.server.core.thread.Threads
import net.rwhps.server.core.thread.Threads.closeTimeTask
import net.rwhps.server.core.thread.Threads.newTimedTask
import net.rwhps.server.data.MapManage
import net.rwhps.server.data.player.PlayerHessManage
import net.rwhps.server.game.event.EventType
import net.rwhps.server.util.Time
import net.rwhps.server.util.game.Events
import net.rwhps.server.util.log.Log
import net.rwhps.server.util.log.Log.clog
import java.util.concurrent.TimeUnit

class ServerRoom {
    lateinit var roomID: String

    val playerManage = PlayerHessManage()
    val call = CallHess(this)

    var isStartGame = false
        set(value) {
            if (checkGameStatusFlag && value) {
                checkGameStatusFlag = false
                startTime = Time.concurrentSecond()
                closeTimeTask(CallTimeTask.CallTeamTask)

                newTimedTask(CallTimeTask.AutoCheckTask,0,1, TimeUnit.SECONDS) {
                    when (playerManage.playerGroup.size) {
                        0 -> gr()
                        1 -> if (oneSay) {
                            oneSay = false
                            call.sendSystemMessageLocal("gameOver.oneMin")
                            Threads.newCountdown(CallTimeTask.GameOverTask, 1, TimeUnit.MINUTES) { gr() }
                        }
                        else -> {
                            if (Threads.containsTimeTask(CallTimeTask.GameOverTask)) {
                                oneSay = true
                                closeTimeTask(CallTimeTask.GameOverTask)
                            }
                        }
                    }

                    var lastWinTeam: Int = -1
                    var lastWinCount = 0
                    playerManage.runPlayerArrayDataRunnable {
                        if (it.survive && it.team != lastWinTeam) {
                            lastWinTeam = it.team
                            lastWinCount++
                        }

                    }
                    if (lastWinCount == 1 && sendGameStatusFlag) {
                        sendGameStatusFlag = false

                        closeTimeTask(CallTimeTask.AutoCheckTask)

                        val winPlayer = playerManage.getPlayersNameOnTheSameTeam(lastWinTeam)

                        val last = winPlayer.toArray(String::class.java).contentToString()
                        lastWin = last
                        clog("[$roomID] Last Win Player: {0}", last)
                        call.sendSystemMessageLocal("survive.player", last)
                    }
                }
            }
            field = value
        }

    data class GameOverData(
        val allPlayerList: List<String>,
        val winPlayerList: List<String>,
        val mapName: String,
        val playerData: Map<String,Map<String,Int>>,
        val replayName: String
    )

    //
    var startTime = 0
    var isAfk = true

    // FLAG
    private var forcedReturn = false
    private var checkGameStatusFlag = true
    private var sendGameStatusFlag = true
    private var oneSay = true


    private var lastWin = ""
    var mapName: String
        get() {
            return MapManage.maps.mapName
        }
        set(value) {
            MapManage.maps.mapName = value
        }
    var replayFileName = ""
        set(value) {
            field = value

            playerManage.playerAll.eachAll {
                it.updateDate()
            }
        }

    @Volatile
    var closeServer: ()->Unit = {}
    var startServer: ()->Unit = {}

    fun gr() {
        if (forcedReturn) {
            return
        }
        forcedReturn = true
        closeTimeTask(CallTimeTask.GameOverTask)
        closeTimeTask(CallTimeTask.AutoCheckTask)

        Log.clog("[$roomID] Gameover")

        closeServer()
        //
        isStartGame = false
        //
        checkGameStatusFlag = true
        oneSay = true
        //
        sendGameStatusFlag = true

        playerManage.cleanPlayerAllData()

        //val file = FileUtil.getFolder(Data.Plugin_RePlays_Path).toFile(replayFileName)
        //file.delete()

        startTime = 0

        forcedReturn = false
        startServer()

        Events.fire(EventType.GameOverEvent(null))
    }
}