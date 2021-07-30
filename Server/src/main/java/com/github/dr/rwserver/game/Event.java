package com.github.dr.rwserver.game;

import com.github.dr.rwserver.core.Call;
import com.github.dr.rwserver.core.NetServer;
import com.github.dr.rwserver.data.Player;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.game.EventType.*;
import com.github.dr.rwserver.net.Administration;
import com.github.dr.rwserver.util.Time;
import com.github.dr.rwserver.util.game.Events;
import com.github.dr.rwserver.util.log.Log;

import java.io.IOException;

/**
 * @author Dr
 */
public class Event {

    public Event() {
        Events.on(ServerLoadEvent.class, e -> {
            Data.core.admin.addChatFilter((player, message) -> {
                if (player.muteTime > Time.millis()) {
                    return null;
                }
                return message;
            });

            Log.info("ServerConnectUuid",Data.core.serverConnectUuid);
            Log.info("TOKEN",Data.core.serverToken);
            Log.info("bannedIPs",Data.core.admin.bannedIPs);
            Log.info("bannedUUIDs",Data.core.admin.bannedUUIDs);

            //Log.clog(Data.localeUtil.getinput("server.loadPlugin", Main.data.size()));
        });

        Events.on(PlayerConnectPasswdCheckEvent.class, e -> {
            if (!"".equals(Data.game.passwd)) {
                if (!e.passwd.equals(Data.game.passwd)) {
                    try {
                        e.abstractNetConnect.sendErrorPasswd();
                    } catch (IOException ioException) {
                        Log.debug("Event Passwd",e);
                    } finally {
                        e.result = true;
                    }
                }
            }
        });

        Events.on(PlayerJoinEvent.class, e -> {
            if (Data.core.admin.bannedUUIDs.contains(e.player.uuid)) {
                try {
                    e.player.con.sendKick(e.player.localeUtil.getinput("kick.ban"));
                } catch (IOException ioException) {
                    Log.error("[Player] Send Kick Player Error",ioException);
                }
                return;
            }
            if (Data.core.admin.playerDataCache.containsKey(e.player.uuid)) {
                Administration.PlayerInfo info = Data.core.admin.playerDataCache.get(e.player.uuid);
                if (info.timesKicked > Time.millis()) {
                    try {
                        e.player.con.sendKick(e.player.localeUtil.getinput("kick.you.time"));
                    } catch (IOException ioException) {
                        Log.error("[Player] Send Kick Player Error",ioException);
                    }
                } else {
                    e.player.muteTime = info.timeMute;
                }  
            }
        });

        Events.on(PlayerLeaveEvent.class, e -> {
            if (Data.game.oneAdmin && e.player.isAdmin) {
                try {
                    Player p = Data.playerGroup.get(0);
                    p.isAdmin = true;
                    Call.upDataGameData();
                    e.player.isAdmin = false;
                    Call.sendSystemMessage("give.ok", p.name);
                } catch (IndexOutOfBoundsException ignored) {}
            }
            Data.core.admin.playerDataCache.put(e.player.uuid,new Administration.PlayerInfo(e.player.uuid,e.player.kickTime,e.player.muteTime));
        });

        Events.on(GameOverEvent.class, e -> {
            if (Data.game.maps.mapData != null) {
                Data.game.maps.mapData.clean();
            }

            NetServer.reLoadServer();
            System.gc();
        });

        Events.on(GameStartEvent.class, e -> Data.core.admin.playerDataCache.clear());

        Events.on(PlayerLeaveEvent.class, e -> {
            if (Data.game.isStartGame) {
                e.player.sharedControl = true;
                int int3 = 0;
                for (int i = 0; i < Data.game.maxPlayer; i++) {
                    Player player1 = Data.game.playerData[i];
                    if (player1 != null) {
                        if (player1.sharedControl || Data.game.sharedControl) {
                            int3 = (int3 | 1 << i);
                        }
                    }
                }
                Data.game.sharedControlPlayer = int3;
                Call.sendSystemMessage("player.dis",e.player.name);
                Call.sendTeamData();
            } else {
//               Call.sendSystemMessage("player.disNoStart",e.player.name);
            }
        });

        Events.on(PlayerBanEvent.class,e -> {
            Data.core.admin.bannedUUIDs.add(e.player.uuid);
            Data.core.admin.bannedIPs.add(e.player.con.getIp());
            try {
                e.player.con.sendKick(e.player.localeUtil.getinput("kick.ban"));
            } catch (IOException ioException) {
                Log.error("[Player] Send Kick Player Error",ioException);
            }
            Call.sendSystemMessage("ban.yes",e.player.name);
        });

        Events.on(PlayerUnbanEvent.class,e -> {});

        Events.on(PlayerIpBanEvent.class,e -> {
            Data.core.admin.bannedIPs.add(e.player.con.getIp());
            try {
                e.player.con.sendKick("kick.ban");
            } catch (IOException ioException) {
                Log.error("[Player] Send Kick Player Error",ioException);
            }
            Call.sendSystemMessage("ban.yes",e.player.name);
        });

        //Events.on(PlayerIpUnbanEvent.class,e -> {});
    }
}
