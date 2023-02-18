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
import net.rwhps.server.data.player.PlayerHessManage
import net.rwhps.server.net.HttpRequestOkHttp.doPostRw
import net.rwhps.server.util.RandomUtil.getRandomString
import net.rwhps.server.util.StringFilteringUtil.cutting
import net.rwhps.server.util.Time
import net.rwhps.server.util.Time.utcMillis
import net.rwhps.server.util.encryption.digest.DigestUtil.md5Hex
import net.rwhps.server.util.encryption.digest.DigestUtil.sha256
import net.rwhps.server.util.log.Log
import net.rwhps.server.util.log.Log.clog
import java.math.BigInteger
import java.util.*
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
                            Log.debug(it.name,it.unitsKilled)
                            lastWinTeam = it.team
                            lastWinCount++
                        }

                    }
                    if (lastWinCount == 1) {
                        closeTimeTask(CallTimeTask.AutoCheckTask)

                        val winPlayer = playerManage.getPlayersNameOnTheSameTeam(lastWinTeam)

                        val last = winPlayer.toArray(String::class.java).contentToString()
                        lastWin = last
                        clog("[$roomID] Last Win Player: {0}", last)
                        call.sendSystemMessageLocal("survive.player", last)

                        if (!Threads.containsTimeTask(CallTimeTask.GameOverTask)) {
                            call.sendSystemMessageLocal("gameOver.forced")
                            Threads.newCountdown(CallTimeTask.GameOverTask, 1, TimeUnit.MINUTES) { gr() }
                        }
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
    var mapName = "[Not Up To Date]"
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
    }

    val test = Thread() {
        NetServerNew().addServerList()
    }

    inner class NetServerNew {
        var userId: String? = null
        var serverToken: String? = null

        fun addServerList() {
            serverToken = getRandomString(40)
            val sb = StringBuilder()
            val time = utcMillis
            userId = "u_" + UUID.randomUUID()
            sb.append("action=add")
                .append("&user_id=").append(userId)
                .append("&game_name=RW-HPS")
                .append("&_1=").append(time)
                .append("&tx2=").append(reup("_" + userId + 5))
                .append("&tx3=").append(reup("_" + userId + (5 + time)))
                .append("&game_version=176")
                .append("&game_version_string=1.15")
                .append("&game_version_beta=false")
                .append("&private_token=").append(serverToken)
                .append("&private_token_2=").append(md5Hex(md5Hex(serverToken!!)))
                .append("&confirm=").append(md5Hex("a" + md5Hex(serverToken!!)))
                .append("&password_required=").append(false)
                .append("&created_by=").append("RW-HPS 2.0")
                .append("&private_ip=10.0.0.1")
                .append("&port_number=${Data.config.Port}")
                .append("&game_map=").append("RW-HPS 2.X Test (Support AI)")
                .append("&game_mode=skirmishMap")
                .append("&game_status=battleroom")
                .append("&player_count=").append(playerManage.playerGroup.size)
                .append("&max_player_count=").append(10)
            val S1 = doPostRw("http://gs1.corrodinggames.com/masterserver/1.4/interface", sb.toString()).contains(userId!!)
            val S4 = doPostRw("http://gs4.corrodinggames.net/masterserver/1.4/interface", sb.toString()).contains(userId!!)
            val O1 = doPostRw("http://gs1.corrodinggames.com/masterserver/1.4/interface", "action=self_info&port=" + Data.config.Port + "&id=" + userId + "&tx3=" + reup("-" + userId + "54")).contains("true")
            val O4 = doPostRw("http://gs4.corrodinggames.net/masterserver/1.4/interface", "action=self_info&port=" + Data.config.Port + "&id=" + userId + "&tx3=" + reup("-" + userId + "54")).contains("true")
            if (S1 || S4) {
                if (S1 && S4) {
                    clog(Data.i18NBundle.getinput("err.yesList"))
                } else {
                    clog(Data.i18NBundle.getinput("err.ynList"))
                }
            } else {
                clog(Data.i18NBundle.getinput("err.noList"))
            }
            if (O1 || O4) {
                clog(Data.i18NBundle.getinput("err.yesOpen"))
            } else {
                clog(Data.i18NBundle.getinput("err.noOpen"))
            }
            newTimedTask(CallTimeTask.UpServerListNewTask, 1, 1, TimeUnit.MINUTES) {
                upServerListNew()
            }
        }

        fun upServerListNew() {
            var stat = "battleroom"
            if (isStartGame) {
                stat = "ingame"
            }
            val sb = StringBuilder()
            sb.append("action=update")
                .append("&id=").append(userId)
                .append("&game_name=RW-HPS")
                .append("&private_token=").append(serverToken)
                .append("&password_required=").append(false)
                .append("&created_by=").append("RW-HPS 2.0")
                .append("&private_ip=10.0.0.1")
                .append("&port_number=${Data.config.Port}")
                .append("&game_map=").append("RW-HPS 2.X Test (Support AI)")
                .append("&game_mode=skirmishMap")
                .append("&game_status=").append(stat)
                .append("&player_count=").append(playerManage.playerGroup.size)
                .append("&max_player_count=").append(10)
            doPostRw("http://gs1.corrodinggames.com/masterserver/1.4/interface", sb.toString())
            doPostRw("http://gs4.corrodinggames.net/masterserver/1.4/interface", sb.toString())
        }

        fun removeServerList() {
            val sb = StringBuilder()
            sb.append("action=remove")
                .append("&id=").append(userId)
                .append("&private_token=").append(serverToken)
            doPostRw("http://gs1.corrodinggames.com/masterserver/1.4/interface", sb.toString())
            doPostRw("http://gs4.corrodinggames.net/masterserver/1.4/interface", sb.toString())
        }

        private fun reup(str: String): String {
            val bytes = sha256(str)
            return cutting(String.format("%0" + bytes.size * 2 + "X", BigInteger(1, bytes)), 4)
        }
    }
}