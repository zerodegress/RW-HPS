/*
 * Copyright 2020-2021 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.command

import com.github.dr.rwserver.command.ex.Vote
import com.github.dr.rwserver.core.Call.sendMessageLocal
import com.github.dr.rwserver.core.Call.sendSystemMessageLocal
import com.github.dr.rwserver.core.Call.sendTeamData
import com.github.dr.rwserver.core.Call.sendTeamMessage
import com.github.dr.rwserver.core.Call.testPreparationPlayer
import com.github.dr.rwserver.core.Call.upDataGameData
import com.github.dr.rwserver.core.thread.Threads.getIfScheduledFutureData
import com.github.dr.rwserver.core.thread.Threads.newThreadService
import com.github.dr.rwserver.core.thread.Threads.removeScheduledFutureData
import com.github.dr.rwserver.data.Player
import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.data.global.Data.LINE_SEPARATOR
import com.github.dr.rwserver.game.EventType.GameStartEvent
import com.github.dr.rwserver.game.GameMaps.MapType
import com.github.dr.rwserver.game.Team.amNoPlayerTeam
import com.github.dr.rwserver.game.Team.amYesPlayerTeam
import com.github.dr.rwserver.util.IsUtil.isNumeric
import com.github.dr.rwserver.util.IsUtil.notIsBlank
import com.github.dr.rwserver.util.IsUtil.notIsNumeric
import com.github.dr.rwserver.util.Time.getTimeFutureMillis
import com.github.dr.rwserver.util.game.CommandHandler
import com.github.dr.rwserver.util.game.Events
import com.github.dr.rwserver.util.log.Log.error
import java.io.IOException
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author Dr
 */
class ClientCommands(handler: CommandHandler) {
    private val localeUtil = Data.localeUtil
    private fun isAdmin(player: Player): Boolean {
        if (player.isAdmin) {
            return true
        }
        player.sendSystemMessage(player.localeUtil.getinput("err.noAdmin"))
        return false
    }

    init {
        handler.register("help", "clientCommands.help") { _: Array<String>?, player: Player ->
            val str = StringBuilder(16)
            for (command in handler.commandList) {
                if (command.description.startsWith("#")) {
                    str.append("   ").append(command.text).append(if (command.paramText.isEmpty()) "" else " ")
                        .append(command.paramText).append(" - ").append(command.description.substring(1))
                } else {
                    if ("HIDE" == command.description) {
                        continue
                    }
                    str.append("   ").append(command.text).append(if (command.paramText.isEmpty()) "" else " ")
                        .append(command.paramText).append(" - ").append(player.localeUtil.getinput(command.description))
                        .append(LINE_SEPARATOR)
                }
            }
            player.sendSystemMessage(str.toString())
        }
        handler.register("map", "<MapNumber...>", "clientCommands.map") { args: Array<String>, player: Player ->
            if (isAdmin(player)) {
                if (Data.game.isStartGame || Data.game.mapLock) {
                    return@register
                }
                val response = StringBuilder(args[0])
                var i = 1
                val lens = args.size
                while (i < lens) {
                    response.append(" ").append(args[i])
                    i++
                }
                val inputMapName =
                    response.toString().replace("'", "").replace(" ", "").replace("-", "").replace("_", "")
                val mapPlayer = Data.MapsMap[inputMapName]
                if (mapPlayer != null) {
                    val data = mapPlayer.split("@").toTypedArray()
                    Data.game.maps.mapName = data[0]
                    Data.game.maps.mapPlayer = data[1]
                    Data.game.maps.mapType = MapType.defaultMap
                } else {
                    if (Data.game.mapsData.size == 0) {
                        return@register
                    }
                    if (notIsNumeric(inputMapName)) {
                        player.sendSystemMessage(localeUtil.getinput("err.noNumber"))
                        return@register
                    }
                    val name = Data.game.mapsData.keys().toSeq()[inputMapName.toInt()]
                    val data = Data.game.mapsData[name]
                    Data.game.maps.mapData = data
                    Data.game.maps.mapType = data.mapType
                    Data.game.maps.mapName = name
                    Data.game.maps.mapPlayer = ""
                    player.sendSystemMessage(player.localeUtil.getinput("map.custom.info"))
                }
                upDataGameData()
            }
        }
        handler.register("maps", "[page]", "clientCommands.maps") { _: Array<String>?, player: Player ->
            if (Data.game.isStartGame) {
                player.sendSystemMessage(localeUtil.getinput("err.startGame"))
                return@register
            }
            if (Data.game.mapsData.size == 0) {
                return@register
            }
            val response = StringBuilder()
            val i = AtomicInteger(0)
            response.append(localeUtil.getinput("maps.top"))
            Data.game.mapsData.keys().forEach { k: String ->
                response.append(localeUtil.getinput("maps.info", i.get(), k)).append(LINE_SEPARATOR)
                i.getAndIncrement()
            }
            player.sendSystemMessage(response.toString())
        }
        handler.register("afk", "clientCommands.afk") { _: Array<String>?, player: Player ->
            if (!Data.game.isAfk) {
                player.sendSystemMessage(localeUtil.getinput("ban.comm", "afk"))
                return@register
            }
            if (player.isAdmin) {
                player.sendSystemMessage(localeUtil.getinput("afk.adminNo"))
            } else {
                if (Data.game.isStartGame) {
                    player.sendSystemMessage(localeUtil.getinput("err.startGame"))
                    return@register
                }
                if (getIfScheduledFutureData("AfkCountdown")) {
                    return@register
                }
                val admin = AtomicBoolean(true)
                Data.playerGroup.each({ p: Player -> p.isAdmin },{ _: Player -> admin.set(false) })
                if (admin.get() && Data.game.oneAdmin) {
                    player.isAdmin = true
                    upDataGameData()
                    sendSystemMessageLocal("afk.end.noAdmin", player.name)
                    return@register
                }
                newThreadService({
                    Data.playerGroup.each(
                        { p: Player -> p.isAdmin }) { i: Player ->
                        i.isAdmin = false
                        player.isAdmin = true
                        upDataGameData()
                        sendSystemMessageLocal("afk.end.ok", player.name)
                        removeScheduledFutureData("AfkCountdown")
                    }
                }, 30, TimeUnit.SECONDS, "AfkCountdown")
                sendMessageLocal(player, "afk.start", player.name)
            }
        }
        handler.register("give", "<PlayerSerialNumber>", "clientCommands.give") { args: Array<String>, player: Player ->
            if (isAdmin(player)) {
                if (Data.game.isStartGame) {
                    player.sendSystemMessage(localeUtil.getinput("err.startGame"))
                    return@register
                }
                if (notIsNumeric(args[0])) {
                    player.sendSystemMessage(localeUtil.getinput("err.noNumber"))
                    return@register
                }
                val playerSite = args[0].toInt() - 1
                val newAdmin = Data.game.playerData[playerSite]
                if (notIsBlank(newAdmin)) {
                    player.isAdmin = false
                    newAdmin.isAdmin = true
                    upDataGameData()
                    sendMessageLocal(player, "give.ok", player.name)
                } else {
                    sendMessageLocal(player, "give.noPlayer", player.name)
                }
            }
        }
        handler.register("nosay", "<on/off>", "clientCommands.noSay") { args: Array<String>, player: Player ->
            player.noSay = "on" == args[0]
            player.sendSystemMessage(localeUtil.getinput("server.noSay", if (player.noSay) "开启" else "关闭"))
        }
        handler.register("am", "<on/off>", "clientCommands.am") { args: Array<String>, player: Player ->
            Data.game.amTeam = "on" == args[0]
            if (Data.game.amTeam) {
                amYesPlayerTeam()
            } else {
                amNoPlayerTeam()
            }
            player.sendSystemMessage(localeUtil.getinput("server.amTeam", if (Data.game.amTeam) "开启" else "关闭"))
        }
        handler.register("income", "<income>", "clientCommands.income") { args: Array<String>, player: Player ->
            if (isAdmin(player)) {
                if (Data.game.isStartGame) {
                    player.sendSystemMessage(player.localeUtil.getinput("err.startGame"))
                    return@register
                }
                Data.game.income = args[0].toFloat()
                upDataGameData()
            }
        }
        handler.register(
            "status",
            "clientCommands.status"
        ) { _: Array<String>?, player: Player ->
            player.sendSystemMessage(
                player.localeUtil.getinput(
                    "status.version",
                    Data.playerGroup.size(),
                    Data.core.admin.bannedIPs.size(),
                    Data.SERVER_CORE_VERSION,
                    player.con!!.version
                )
            )
        }
        handler.register("kick", "<PlayerSerialNumber>", "clientCommands.kick") { args: Array<String>, player: Player ->
            if (Data.game.isStartGame) {
                player.sendSystemMessage(player.localeUtil.getinput("err.startGame"))
                return@register
            }
            if (isAdmin(player)) {
                if (notIsNumeric(args[0])) {
                    player.sendSystemMessage(player.localeUtil.getinput("err.noNumber"))
                    return@register
                }
                val site = args[0].toInt() - 1
                if (Data.game.playerData[site] != null) {
                    Data.game.playerData[site].kickTime = getTimeFutureMillis(60 * 1000L)
                    try {
                        Data.game.playerData[site].con!!.sendKick(localeUtil.getinput("kick.you"))
                    } catch (e: IOException) {
                        error("[Player] Send Kick Player Error", e)
                    }
                }
            }
        }
        handler.register("i", "<i...>", "HIDE") { _: Array<String>?, _: Player -> }

        /* QC */
        handler.register("credits", "<money>", "HIDE") { args: Array<String>, player: Player ->
            if (isAdmin(player)) {
                if (notIsNumeric(args[0])) {
                    player.sendSystemMessage(localeUtil.getinput("err.noNumber"))
                    return@register
                }
                when (args[0].toInt()) {
                    0 -> Data.game.credits = 1
                    1000 -> Data.game.credits = 2
                    2000 -> Data.game.credits = 3
                    5000 -> Data.game.credits = 4
                    10000 -> Data.game.credits = 5
                    50000 -> Data.game.credits = 6
                    100000 -> Data.game.credits = 7
                    200000 -> Data.game.credits = 8
                    4000 -> Data.game.credits = 0
                    else -> {
                    }
                }
                upDataGameData()
            }
        }
        handler.register("nukes", "<boolean>", "HIDE") { args: Array<String>, player: Player ->
            if (isAdmin(player)) {
                Data.game.noNukes = !java.lang.Boolean.parseBoolean(args[0])
                upDataGameData()
            }
        }
        handler.register(
            "addai",
            "HIDE"
        ) { _: Array<String>?, player: Player -> player.sendSystemMessage(player.localeUtil.getinput("err.nosupr")) }
        handler.register("fog", "<type>", "HIDE") { args: Array<String>, player: Player ->
            if (isAdmin(player)) {
                Data.game.mist = if ("off" == args[0]) 0 else if ("basic" == args[0]) 1 else 2
                upDataGameData()
            }
        }
        handler.register("sharedcontrol", "<boolean>", "HIDE") { args: Array<String>, player: Player ->
            if (isAdmin(player)) {
                Data.game.sharedControl = java.lang.Boolean.parseBoolean(args[0])
                upDataGameData()
            }
        }
        handler.register("startingunits", "<type>", "HIDE") { args: Array<String>, player: Player ->
            if (isAdmin(player)) {
                if (notIsNumeric(args[0])) {
                    player.sendSystemMessage(player.localeUtil.getinput("err.noNumber"))
                    return@register
                }
                //Data.game.initUnit = (type == 1) ? 1 : (type == 2) ? 2 : (type ==3) ? 3 : (type == 4) ? 4 : 100;
                Data.game.initUnit = args[0].toInt()
                upDataGameData()
            }
        }
        handler.register("start", "clientCommands.start") { _: Array<String>?, player: Player ->
            if (isAdmin(player)) {
                if (getIfScheduledFutureData("AfkCountdown")) {
                    removeScheduledFutureData("AfkCountdown")
                    sendMessageLocal(player, "afk.clear", player.name)
                }
                if (Data.game.startMinPlayerSize > Data.playerGroup.size()) {
                    player.sendSystemMessage(player.localeUtil.getinput("start.playerNo", Data.game.startMinPlayerSize))
                    return@register
                }
                if (getIfScheduledFutureData("GamePing")) {
                    removeScheduledFutureData("GamePing")
                }
                if (Data.game.maps.mapData != null) {
                    Data.game.maps.mapData!!.readMap()
                }
                Data.playerGroup.each { e: Player ->
                    try {
                        e.con!!.sendStartGame()
                        e.lastMoveTime = System.currentTimeMillis()
                    } catch (err: IOException) {
                        error("Start Error", err)
                    }
                }
                if (Data.game.winOrLose) {
                }
                Data.game.isStartGame = true
                var int3 = 0
                for (i in 0 until Data.game.maxPlayer) {
                    val player1 = Data.game.playerData[i]
                    if (player1 != null) {
                        if (player1.sharedControl || Data.game.sharedControl) {
                            int3 = int3 or 1 shl i
                        }
                    }
                }
                Data.game.sharedControlPlayer = int3
                testPreparationPlayer()
                Events.fire(GameStartEvent())
            }
        }
        handler.register("t", "<text...>", "clientCommands.t") { args: Array<String>, player: Player ->
            val response = StringBuilder(args[0])
            var i = 1
            val lens = args.size
            while (i < lens) {
                response.append(" ").append(args[i])
                i++
            }
            sendTeamMessage(player.team, player, response.toString())
        }
        handler.register("surrender", "clientCommands.surrender") { _: Array<String>?, player: Player ->
            if (Data.game.isStartGame) {
                /*
				if (isBlank(Data.Vote)) {
					Data.Vote = new Vote(player,"surrender");
				} else {
					Data.Vote.toVote(player,"y");
				}*/
                player.con!!.sendSurrender()
            } else {
                player.sendSystemMessage(player.localeUtil.getinput("err.noStartGame"))
            }
        }
        handler.register("teamlock", "clientCommands.teamlock") { args: Array<String>, player: Player ->
            if (isAdmin(player)) {
                Data.game.lockTeam = "on" == args[0]
            }
        }
        handler.register("killme", "clientCommands.killMe") { _: Array<String>?, player: Player ->
            if (Data.game.isStartGame) {
                player.con!!.sendSurrender()
            } else {
                player.sendSystemMessage(player.localeUtil.getinput("err.noStartGame"))
            }
        }
        handler.register(
            "vote",
            "<gameover/kick> [player-site]",
            "clientCommands.vote"
        ) { args: Array<String>, player: Player ->
            when (args[0].lowercase(Locale.getDefault())) {
                "gameover" -> Data.Vote = Vote(player, args[0])
                "kick" -> if (args.size > 1 && isNumeric(args[1])) {
                    Data.Vote = Vote(player, args[0], args[1])
                } else {
                    player.sendSystemMessage(player.localeUtil.getinput("err.commandError"))
                }
                else -> player.sendSystemMessage(player.localeUtil.getinput("err.command"))
            }
        }
        handler.register(
            "move",
            "<PlayerSerialNumber> <ToSerialNumber> <?>",
            "HIDE"
        ) { args: Array<String>, player: Player ->
            if (Data.game.isStartGame) {
                player.sendSystemMessage(player.localeUtil.getinput("err.startGame"))
                return@register
            }
            if (isAdmin(player)) {
                if (notIsNumeric(args[0]) && notIsNumeric(args[1]) && notIsNumeric(args[2])) {
                    player.sendSystemMessage(player.localeUtil.getinput("err.noNumber"))
                    return@register
                }
                val oldSite = args[0].toInt() - 1
                val newSite = args[1].toInt() - 1
                //int newSite = 0;
                val team = args[2].toInt()
                if (oldSite < Data.game.maxPlayer && newSite < Data.game.maxPlayer) {
                    val od = Data.game.playerData[oldSite]
                    if (newSite > -2) {
                        if (Data.game.playerData[newSite] == null) {
                            Data.game.playerData[oldSite] = null
                            od.site = newSite
                            if (team > -1) {
                                od.team = team
                            }
                            Data.game.playerData[newSite] = od
                        } else {
                            val nw = Data.game.playerData[newSite]
                            od.site = newSite
                            nw.site = oldSite
                            if (team > -1) {
                                od.team = team
                            }
                            Data.game.playerData[newSite] = od
                            Data.game.playerData[oldSite] = nw
                        }
                    }
                    sendTeamData()
                }
            }
        }
        handler.register("self_move", "<ToSerialNumber> <?>", "HIDE") { args: Array<String>, player: Player ->
            if (Data.game.isStartGame) {
                player.sendSystemMessage(player.localeUtil.getinput("err.startGame"))
                return@register
            }
            if (Data.game.lockTeam) {
                return@register
            }
            if (notIsNumeric(args[0]) && notIsNumeric(args[1])) {
                player.sendSystemMessage(player.localeUtil.getinput("err.noNumber"))
                return@register
            }
            val newSite = args[0].toInt() - 1
            val team = args[1].toInt()
            if (newSite < Data.game.maxPlayer) {
                if (Data.game.playerData[newSite] == null) {
                    Data.game.playerData[player.site] = null
                    player.site = newSite
                    if (team > -1) {
                        player.team = team
                    }
                    Data.game.playerData[newSite] = player
                    sendTeamData()
                }
            }
        }
        handler.register("team", "<PlayerSiteNumber> <ToTeamNumber>", "HIDE") { args: Array<String>, player: Player ->
            if (Data.game.isStartGame) {
                player.sendSystemMessage(player.localeUtil.getinput("err.startGame"))
                return@register
            }
            if (isAdmin(player)) {
                if (notIsNumeric(args[0]) && notIsNumeric(args[1])) {
                    player.sendSystemMessage(player.localeUtil.getinput("err.noNumber"))
                    return@register
                }
                val playerSite = args[0].toInt() - 1
                val newSite = args[1].toInt() - 1
                if (playerSite < Data.game.maxPlayer && newSite < Data.game.maxPlayer) {
                    if (newSite > -1) {
                        if (notIsBlank(Data.game.playerData[playerSite])) {
                            Data.game.playerData[playerSite].team = newSite
                        }
                    }
                    sendTeamData()
                }
            }
        }
        handler.register("self_team", "<ToTeamNumber>", "HIDE") { args: Array<String>, player: Player ->
            if (Data.game.isStartGame) {
                player.sendSystemMessage(player.localeUtil.getinput("err.startGame"))
                return@register
            }
            if (Data.game.lockTeam) {
                return@register
            }
            if (notIsNumeric(args[0])) {
                player.sendSystemMessage(player.localeUtil.getinput("err.noNumber"))
                return@register
            }
            val newSite = args[0].toInt() - 1
            if (newSite < Data.game.maxPlayer) {
                player.team = newSite
                sendTeamData()
            }
        }
    }
}