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
import net.rwhps.server.core.thread.Threads.closeTimeTask
import net.rwhps.server.data.MapManage
import net.rwhps.server.data.event.GameOverData
import net.rwhps.server.data.player.PlayerHessManage
import net.rwhps.server.data.temp.ServerCacheFlag
import net.rwhps.server.game.event.EventType
import net.rwhps.server.util.Time
import net.rwhps.server.util.game.Events
import net.rwhps.server.util.log.Log.clog

/**
 * @author RW-HPS/Dr
 */
class ServerRoom {
    lateinit var roomID: String

    val playerManage = PlayerHessManage()
    val call = CallHess(this)

    var isStartGame = false
        set(value) {
            if (checkGameStatusFlag && value) {
                checkGameStatusFlag = false

                startTime = Time.concurrentSecond()
                endTime = Time.concurrentSecond()+Data.configServer.MaxGameIngTime

                closeTimeTask(CallTimeTask.CallTeamTask)

                call.startCheckThread()
            }
            field = value
        }

    // Start Time
    var startTime = 0
    /** End Time */
    var endTime = 0
        private set

    var isAfk = true

    // FLAG
    var flagData = ServerCacheFlag()
    var forcedReturn = false
    var checkGameStatusFlag = true


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

    internal var gameOverData: GameOverData? = null


    var closeServer: ()->Unit = {}
    var startServer: ()->Unit = {}

    internal fun gr() {
        if (forcedReturn) {
            return
        }
        forcedReturn = true

        if (Data.vote != null) {
            Data.vote!!.stopVote()
        }
        closeTimeTask(CallTimeTask.GameOverTask)
        closeTimeTask(CallTimeTask.AutoCheckTask)

        clog("[$roomID] Gameover")

        closeServer()
        //
        isStartGame = false

        flagData = ServerCacheFlag()
        checkGameStatusFlag = true

        playerManage.cleanPlayerAllData()

        //val file = FileUtil.getFolder(Data.Plugin_RePlays_Path).toFile(replayFileName)
        //file.delete()

        startTime = 0

        forcedReturn = false

        Events.fire(EventType.GameOverEvent(gameOverData))

        gameOverData = null

        startServer()
    }
}