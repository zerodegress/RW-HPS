package com.github.dr.rwserver.core.ex;

import com.github.dr.rwserver.Main;
import com.github.dr.rwserver.core.Call;
import com.github.dr.rwserver.core.NetServer;
import com.github.dr.rwserver.data.Player;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.game.EventType.*;
import com.github.dr.rwserver.net.Administration;
import com.github.dr.rwserver.util.Events;
import com.github.dr.rwserver.util.LocaleUtil;
import com.github.dr.rwserver.util.log.Log;

import static com.github.dr.rwserver.util.DateUtil.getLocalTimeFromU;

/**
 * @author Dr
 */
public class Event {
    private final LocaleUtil localeUtil = Data.localeUtil;
    public Event() {
        Events.on(ServerLoadEvent.class, e -> {
            Data.core.admin.addChatFilter((player, message) -> {
                if (player.muteTime > getLocalTimeFromU()) {
                    return null;
                }
                return message;
            });
            Log.info("ServerConnectUuid",Data.core.serverConnectUuid);
            Log.info("TOKEN",Data.core.serverToken);
            Log.info("bannedIPs",Data.core.admin.bannedIPs);
            Log.info("bannedUUIDs",Data.core.admin.bannedUUIDs);

            Log.clog(localeUtil.getinput("server.loadPlugin", Main.data.size()));
        });

        Events.on(PlayerJoin.class, e -> {
            if (Data.core.admin.bannedUUIDs.contains(e.player.uuid)) {
                e.player.con.sendKick(localeUtil.getinput("kick.ban"));
                return;
            }
            if (Data.core.admin.playerDataCache.containsKey(e.player.uuid)) {
                Administration.PlayerInfo info = Data.core.admin.playerDataCache.get(e.player.uuid);
                if (info.timesKicked < getLocalTimeFromU()) {
                    e.player.con.sendKick(localeUtil.getinput("kick.you.time"));
                } else {
                    e.player.muteTime = info.timeMute;
                }  
            }
        });

        Events.on(PlayerLeave.class, e -> {
            if (Data.game.oneAdmin && e.player.isAdmin) {
                try {
                    Player p = Data.playerGroup.get(0);
                    p.isAdmin = true;
                    p.con.sendServerInfo();
                    Call.sendSystemMessage(Data.localeUtil.getinput("give.ok",p.name));
                } catch (Exception ignored) {}
            }
            Data.core.admin.playerDataCache.put(e.player.uuid,new Administration.PlayerInfo(e.player.uuid,e.player.kickTime,e.player.muteTime));
        });

        Events.on(GameOverEvent.class, e -> {
            NetServer.reLoadServer();
        });

        Events.on(GameStartEvent.class, e -> {
            Data.core.admin.playerDataCache.clear();
        });

        Events.on(PlayerLeave.class, e -> {
            if (Data.game.isStartGame) {
                e.player.sharedControl = true;
                Call.sendSystemMessage(localeUtil.getinput("player.dis",e.player.name));
                Call.sendTeamData();
            } else {
                Call.sendSystemMessage(Data.localeUtil.getinput("player.disNoStart",e.player.name));
            }
        });

        Events.on(PlayerBanEvent.class,e -> {
            Data.core.admin.bannedUUIDs.add(e.player.uuid);
            Data.core.admin.bannedIPs.add(e.player.ip);
            e.player.con.sendKick(localeUtil.getinput("kick.ban"));
            Call.sendSystemMessage(localeUtil.getinput("ban.yes",e.player.name));
        });

        //Events.on(PlayerUnbanEvent.class,e -> {});

        Events.on(PlayerIpBanEvent.class,e -> {
            Data.core.admin.bannedIPs.add(e.player.ip);
            e.player.con.sendKick("kick.ban");
            Call.sendSystemMessage(localeUtil.getinput("ban.yes",e.player.name));
        });

        //Events.on(PlayerIpUnbanEvent.class,e -> {});
    }
}
