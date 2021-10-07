package com.github.dr.rwserver.game

import com.github.dr.rwserver.core.Call
import com.github.dr.rwserver.core.NetServer
import com.github.dr.rwserver.core.thread.Threads
import com.github.dr.rwserver.core.thread.Threads.getIfScheduledFutureData
import com.github.dr.rwserver.core.thread.Threads.newThreadService
import com.github.dr.rwserver.data.Player
import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.ga.GroupGame
import com.github.dr.rwserver.game.EventType.GameOverEvent
import com.github.dr.rwserver.net.Administration.PlayerInfo
import com.github.dr.rwserver.net.core.AbstractNetConnect
import com.github.dr.rwserver.plugin.event.AbstractEvent
import com.github.dr.rwserver.util.Time.millis
import com.github.dr.rwserver.util.game.Events
import com.github.dr.rwserver.util.log.Log
import com.github.dr.rwserver.util.log.Log.clog
import com.github.dr.rwserver.util.log.Log.debug
import com.github.dr.rwserver.util.log.Log.error
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * @author Dr
 */
class Event : AbstractEvent {
    override fun registerServerLoadEvent() {
        Data.core.admin.addChatFilter { player: Player, message: String? ->
            if (player.muteTime > millis()) {
                return@addChatFilter null
            }
            message
        }
        Log.run {
            info("ServerConnectUuid", Data.core.serverConnectUuid)
            info("TOKEN", Data.core.serverToken)
            info("bannedIPs", Data.core.admin.bannedIPs)
            info("bannedUUIDs", Data.core.admin.bannedUUIDs)
        }
    }

    override fun registerPlayerJoinEvent(player: Player) {
//        if (Data.core.admin.bannedUUIDs.contains(player.uuid)) {
//            try {
//                player.con.sendKick(player.localeUtil.getinput("kick.ban"))
//            } catch (ioException: IOException) {
//                error("[Player] Send Kick Player Error", ioException)
//            }
//            return
//        }
//        if (Data.core.admin.playerDataCache.containsKey(player.uuid)) {
//            val info = Data.core.admin.playerDataCache[player.uuid]
//            if (info.timesKicked > millis()) {
//                try {
//                    player.con.sendKick(player.localeUtil.getinput("kick.you.time"))
//                } catch (ioException: IOException) {
//                    error("[Player] Send Kick Player Error", ioException)
//                }
//            } else {
//                player.muteTime = info.timeMute
//            }
//        }
        //////
        var hasAdmin=AtomicBoolean();
        Data.playerGroup.each { p->if(p.groupId==player.groupId&&p.isAdmin){
            hasAdmin.set(true)
        }
        }
        if(!hasAdmin.get()){
            player.isAdmin = true
            Call.upDataGameData(player.groupId)
            Call.sendMessageLocal(player, "give.ok", player.name)
        }
    }

    override fun registerPlayerReJoinEvent(player: Player) {
        var gid=player.groupId
        if (GroupGame.games[gid]!!.isStartGame) {
            val players = GroupGame.playersByGid(Data.playerGroup, gid)
            if (players.isEmpty()) {
                clog("组" + gid + "玩家全部离开，结束游戏")
                Events.fire(GameOverEvent(gid))
            } else if (players.size > 1 && !getIfScheduledFutureData("Gameover-t$gid")) {
                clog("取消，组" + gid + "进入1分钟结束倒计时")
                Threads.removeScheduledFutureData("Gameover$gid")
            } else {
                if (players.stream().map { p: Player -> p.team }.distinct().toArray().size > 1) {
                    clog("取消，组" + gid + "进入2分钟结束倒计时")
                    Threads.removeScheduledFutureData("Gameover-t$gid")
                }
            }
        }
    }

    override fun registerPlayerConnectPasswdCheckEvent(abstractNetConnect: AbstractNetConnect, passwd: String): Array<String> {
        if ("" != Data.game.passwd) {
            if (passwd != Data.game.passwd) {
                try {
                    abstractNetConnect.sendErrorPasswd()
                } catch (ioException: IOException) {
                    debug("Event Passwd", ioException)
                } finally {
                    return arrayOf("true", "")
                }
            }
        }
        return arrayOf("false", "")
    }

    override fun registerPlayerLeaveEvent(player: Player) {
        if (GroupGame.gU(player.groupId).oneAdmin && player.isAdmin) {
            try {
                val p = GroupGame.playerGroup(player.groupId).get(0)
                p.isAdmin = true
                Call.upDataGameData(player.groupId)
                player.isAdmin = false
                Call.sendSystemMessage("give.ok",player.groupId, p.name)
            } catch (ignored: IndexOutOfBoundsException) {
            }
        }
//        Data.core.admin.playerDataCache.put(player.uuid, PlayerInfo(player.uuid, player.kickTime, player.muteTime))

        if (GroupGame.gU(player.groupId)?.isStartGame == true) {
            player.sharedControl = true
            var int3 = 0
            for (i in 0 until GroupGame.gU(player.groupId).maxPlayer) {
                val player1 = GroupGame.gU(player.groupId).playerData[i]
                if (player1 != null) {
                    if (player1.sharedControl || Data.game.sharedControl) {
                        int3 = int3 or 1 shl i
                    }
                }
            }
            GroupGame.games.get(player.groupId)?.sharedControlPlayer =int3
            Call.sendSystemMessage("player.dis",player.groupId, player.name)
            Call.sendTeamData(player.groupId)
        } else {
            Call.sendTeamData(player.groupId)
//            Call.sendSystemMessage("player.disNoStart",player.groupId, player.name)
        }
    }

    override fun registerGameStartEvent() {
        Data.core.admin.playerDataCache.clear()
    }

    override fun registerGameOverEvent(gid:Int) {
        if (GroupGame.games.get(gid)?.maps?.mapData != null) {
            GroupGame.games.get(gid)?.maps!!.mapData!!.clean()
        }
        NetServer.reLoadServer(gid)
        System.gc()
    }

    override fun registerPlayerBanEvent(player: Player) {
        Data.core.admin.bannedUUIDs.add(player.uuid)
        Data.core.admin.bannedIPs.add(player.con.ip)
        try {
            player.con.sendKick(player.localeUtil.getinput("kick.ban"))
        } catch (ioException: IOException) {
            error("[Player] Send Kick Player Error", ioException)
        }
        Call.sendSystemMessage("ban.yes",player.groupId, player.name)
    }

    override fun registerPlayerIpBanEvent(player: Player) {
        Data.core.admin.bannedIPs.add(player.con.ip)
        try {
            player.con.sendKick("kick.ban")
        } catch (ioException: IOException) {
            error("[Player] Send Kick Player Error", ioException)
        }
        Call.sendSystemMessage("ban.yes",player.groupId, player.name)
    }
}