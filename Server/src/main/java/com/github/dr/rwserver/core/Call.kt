/*
 * Copyright 2020-2021 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.core

import com.github.dr.rwserver.core.thread.Threads.getIfScheduledFutureData
import com.github.dr.rwserver.core.thread.Threads.newThreadService
import com.github.dr.rwserver.core.thread.Threads.newThreadService2
import com.github.dr.rwserver.core.thread.Threads.removeScheduledFutureData
import com.github.dr.rwserver.data.Player
import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.data.global.NetStaticData
import com.github.dr.rwserver.game.EventType.GameOverEvent
import com.github.dr.rwserver.game.GameCommand
import com.github.dr.rwserver.struct.Seq
import com.github.dr.rwserver.util.game.Events
import com.github.dr.rwserver.util.log.Log.error
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.stream.IntStream

/**
 * @author Dr
 */
object Call {
    @JvmStatic
    fun sendMessage(player: Player, text: String) {
        try {
            NetStaticData.groupNet.broadcast(NetStaticData.protocolData.abstractNetPacket.getChatMessagePacket(text, player.name, player.team))
        } catch (e: IOException) {
            error("[ALL] Send Player Chat Error", e)
        }
    }

    @JvmStatic
    fun sendMessageLocal(player: Player, text: String, vararg obj: Any) {
        Data.playerGroup.each { e: Player ->
            e.sendMessage(player, e.localeUtil.getinput(text, *obj))
        }
    }

    @JvmStatic
    fun sendTeamMessage(team: Int, player: Player, text: String) {
        Data.playerGroup.eachBooleanIfs({ e: Player -> e.team == team }) { p: Player ->
            p.sendMessage(player, "[TEAM] $text")
        }
    }

    @JvmStatic
    fun sendSystemTeamMessageLocal(team: Int, text: String, vararg obj: Any) {
        Data.playerGroup.eachBooleanIfs({ e: Player -> e.team == team }) { p: Player ->
            p.sendSystemMessage("[TEAM] " + p.localeUtil.getinput(text, *obj))
        }
    }

    @JvmStatic
    fun sendSystemMessage(text: String) {
        try {
            NetStaticData.groupNet.broadcast(NetStaticData.protocolData.abstractNetPacket.getSystemMessagePacket(text))
        } catch (e: IOException) {
            error("[ALL] Send System Chat Error", e)
        }
    }

    @JvmStatic
    fun sendSystemMessageLocal(text: String, vararg obj: Any) {
        Data.playerGroup.each { e: Player -> e.sendSystemMessage(e.localeUtil.getinput(text, *obj))
        }
    }

    @JvmStatic
    fun sendSystemMessage(text: String, vararg obj: Any) {
        Data.playerGroup.each { e: Player ->
            e.sendSystemMessage(e.localeUtil.getinput(text, *obj))
        }
    }

    @JvmStatic
    fun sendTeamData() {
        if (Data.game.reConnectBreak) {
            return
        }
        try {
            val enc = NetStaticData.protocolData.abstractNetPacket.getTeamDataPacket()
            Data.playerGroup.each { e: Player -> e.con!!.sendTeamData(enc) }
        } catch (e: IOException) {
            error("[ALL] Send Team Error", e)
        }
    }

    @JvmStatic
    fun sendPlayerPing() {
        Data.playerGroup.each { e: Player -> e.con!!.ping() }
    }

    @JvmStatic
    fun upDataGameData() {
        Data.playerGroup.each { e: Player ->
            try {
                e.con!!.sendServerInfo(false)
            } catch (err: IOException) {
                error("[ALL] Send System Info Error", err)
            }
        }
    }

    @JvmStatic
    fun killAllPlayer() {
        Data.playerGroup.each { e: Player ->
            try {
                e.con!!.sendKick("Game Over")
            } catch (err: IOException) {
                error("[ALL] Kick All Player Error", e)
            }
        }
    }

    @JvmStatic
    fun disAllPlayer() {
        Data.playerGroup.each { e: Player -> e.con!!.disconnect() }
    }

    @JvmStatic
    fun testPreparationPlayer() {
        Timer().schedule(RandyTask(), 0, 100)
    }

    fun gameOverTask() {
        Timer().schedule(RandyTask(), 0, 100)
    }

    private class RandyTask : TimerTask() {
        private var loadTime = 0
        private val loadTimeMaxTry = 30
        private var start = true
        override fun run() {
            Data.playerGroup.each { p: Player ->
                if (!p.start) {
                    loadTime += 1
                    start = false
                    if (loadTime > loadTimeMaxTry) {
                        if (start) {
                            sendSystemMessageLocal("start.testNo")
                        }
                        newThreadService2(SendGameTickCommand(), 0, 150, TimeUnit.MILLISECONDS, "GameTask")
                        cancel()
                    }
                }
            }
            if (start) {
                start = false
                sendSystemMessageLocal("start.testYes")
            }
            newThreadService2(SendGameTickCommand(), 0, 150, TimeUnit.MILLISECONDS, "GameTask")
            cancel()
        }
    }

    private class SendGameTickCommand : Runnable {
        private var time = 0
        private var oneSay = true
        private var gameover = true
        override fun run() {
            // 检测人数是否符合Gameover
            if (Data.playerGroup.size() == 0) {
                if (gameover) {
                    Events.fire(GameOverEvent())
                    gameover = false
                }
            }
            if (Data.playerGroup.size() <= 1) {
                if (oneSay) {
                    oneSay = false
                    sendSystemMessageLocal("gameOver.oneMin")
                    newThreadService({
                        if (Thread.currentThread().isInterrupted) {
                            // 有一个中断请求，可能是一个取消请求
                            // 停止做正在做的事情并终止。
                            return@newThreadService
                        }
                        Events.fire(GameOverEvent())
                    }, 1, TimeUnit.MINUTES, "Gameover")
                }
            } else {
                if (getIfScheduledFutureData("Gameover")) {
                    oneSay = true
                    gameover = true
                    removeScheduledFutureData("Gameover")
                }
            }
            if (Data.game.reConnectBreak) {
                return
            }
            time += 10
            when (val size = Data.game.gameCommandCache.size) {
                0 -> {
                    try {
                        NetStaticData.groupNet.broadcast(NetStaticData.protocolData.abstractNetPacket.getTickPacket(time))
                    } catch (e: IOException) {
                        error("[ALL] Send Tick Failed", e)
                    }
                }
                1 -> {
                    val gameCommand = Data.game.gameCommandCache.poll()
                    try {
                        NetStaticData.groupNet.broadcast(NetStaticData.protocolData.abstractNetPacket.getGameTickCommandPacket(time, gameCommand))
                    } catch (e: IOException) {
                        error("[ALL] Send Game Tick Error", e)
                    }
                }
                else -> {
                    val comm = Seq<GameCommand>(size)
                    IntStream.range(0, size).mapToObj { Data.game.gameCommandCache.poll() }.forEach { value: GameCommand -> comm.add(value) }
                    try {
                        NetStaticData.groupNet.broadcast(
                            NetStaticData.protocolData.abstractNetPacket.getGameTickCommandsPacket(
                                time,
                                comm
                            )
                        )
                    } catch (e: IOException) {
                        error("[ALL] Send Game Ticks Error", e)
                    }
                }
            }
        }
    }
}