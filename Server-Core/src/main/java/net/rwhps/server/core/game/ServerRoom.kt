/*
 *
 *  * Copyright 2020-2023 RW-HPS Team and contributors.
 *  *
 *  * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *  *
 *  * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 *  
 */

package net.rwhps.server.core.game

import net.rwhps.server.core.CallHess
import net.rwhps.server.core.thread.CallTimeTask
import net.rwhps.server.core.thread.Threads.closeTimeTask
import net.rwhps.server.data.global.Data
import net.rwhps.server.data.player.PlayerHessManage
import net.rwhps.server.data.temp.ServerCacheFlag
import net.rwhps.server.game.MapManage
import net.rwhps.server.game.event.game.ServerGameOverEvent
import net.rwhps.server.game.event.game.ServerGameOverEvent.GameOverData
import net.rwhps.server.game.simulation.core.AbstractGameModule
import net.rwhps.server.util.Time
import net.rwhps.server.util.game.CommandHandler
import net.rwhps.server.util.log.Log.clog

/**
 * @author Dr (dr@der.kim)
 */
class ServerRoom(private val gameModule: AbstractGameModule) {
    lateinit var roomID: String

    /**
     * 服务器客户端的命令系统
     *
     * 为多端的准备
     */
    val clientHandler = CommandHandler("/")

    val playerManage = PlayerHessManage()
    val call = CallHess(gameModule)

    var isStartGame = false
        set(value) {
            if (checkGameStatusFlag && value) {
                checkGameStatusFlag = false

                startTime = Time.concurrentSecond()
                endTime = Time.concurrentSecond() + Data.configServer.maxGameIngTime

                closeTimeTask(CallTimeTask.CallTeamTask)

                call.startCheckThread()

//                val testAClass: Class<*> = (HessModuleManage.hps.useClassLoader as GameModularLoadClass).findClass("net.rwhps.server.plugin.internal.hess.inject.ex.FFA_X")!!
//                val mainMethod: Method = testAClass.getDeclaredMethod("start")
//                mainMethod.invoke(null)
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
    private var forcedReturn = false
    private var checkGameStatusFlag = true


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


    var closeServer: () -> Unit = {}
    var startServer: () -> Unit = {}

    internal fun cleanThread() {
        if (Data.vote != null) {
            Data.vote!!.stopVote()
        }
        closeTimeTask(CallTimeTask.GameOverTask)
        closeTimeTask(CallTimeTask.AutoCheckTask)
    }

    internal fun cleanData() {
        isStartGame = false

        flagData = ServerCacheFlag()
        checkGameStatusFlag = true

        startTime = 0

        forcedReturn = false

        gameModule.eventManage.fire(ServerGameOverEvent(gameOverData)).await()
        gameOverData = null

        MapManage.maps.mapData?.clean()
    }

    internal fun gr() {
        gameModule.gameFunction.suspendMainThreadOperations {
            if (forcedReturn) {
                return@suspendMainThreadOperations
            }
            forcedReturn = true

            cleanThread()

            clog("[$roomID] Gameover")

            closeServer()

            cleanData()
            playerManage.cleanPlayerAllData()

            startServer()
        }
    }
}