package com.github.dr.rwserver.command;

import com.github.dr.rwserver.Main;
import com.github.dr.rwserver.core.Call;
import com.github.dr.rwserver.core.Core;
import com.github.dr.rwserver.core.NetServer;
import com.github.dr.rwserver.core.thread.Threads;
import com.github.dr.rwserver.data.Player;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.data.global.NetStaticData;
import com.github.dr.rwserver.data.plugin.PluginManage;
import com.github.dr.rwserver.func.StrCons;
import com.github.dr.rwserver.game.EventType;
import com.github.dr.rwserver.game.Rules;
import com.github.dr.rwserver.net.game.StartNet;
import com.github.dr.rwserver.net.netconnectprotocol.*;
import com.github.dr.rwserver.util.LocaleUtil;
import com.github.dr.rwserver.util.Time;
import com.github.dr.rwserver.util.game.CommandHandler;
import com.github.dr.rwserver.util.game.Events;
import com.github.dr.rwserver.util.log.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.dr.rwserver.data.global.Data.LINE_SEPARATOR;

/**
 * @author Dr
 */
public class ServerCommands {
    private static final LocaleUtil localeUtil = Data.localeUtil;

    public ServerCommands(CommandHandler handler) {
        handler.<StrCons>register("help", "serverCommands.help", (arg, log) -> {
            log.get("Commands:");
            for(CommandHandler.Command command : handler.getCommandList()){
                if (command.description.startsWith("#")) {
                    log.get("   " + command.text + (command.paramText.isEmpty() ? "" : " ") + command.paramText + " - " + command.description.substring(1));
                } else {
                    log.get("   " + command.text + (command.paramText.isEmpty() ? "" : " ") + command.paramText + " - " + Data.localeUtil.getinput(command.description));
                }
            }
        });

        handler.<StrCons>register("start", "serverCommands.start", (arg, log) -> {
            if (Data.serverChannelB != null) {
                log.get("The server is not closed, please close");
                return;
            }
            Log.set(Data.config.readString("log","WARN").toUpperCase());

            Data.game = new Rules(Data.config);
            Data.game.init();
            Threads.newThreadService2(Call::sendTeamData,0,2, TimeUnit.SECONDS,"GameTeam");
            Threads.newThreadService2(Call::sendPlayerPing,0,2, TimeUnit.SECONDS,"GamePing");
            NetStaticData.protocolData.setTypeConnect(new TypeRwHps());
            NetStaticData.protocolData.setNetConnectProtocol(new GameVersionServer(null),151);
            Threads.newThreadCore(() -> {
                StartNet startNet = new StartNet();
                NetStaticData.startNet.add(startNet);
                startNet.openPort(Data.game.port);
            });
            if (Data.config.readBoolean("UDPSupport",false)) {
                Threads.newThreadCore(() -> {
                    try {
                        StartNet startNet = new StartNet();
                        NetStaticData.startNet.add(startNet);
                        startNet.startUdp(Data.game.port);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });

        handler.<StrCons>register("say", "<text...>","serverCommands.say", (arg, log) -> {
            StringBuilder response = new StringBuilder(arg[0]);
            for(int i=1,lens=arg.length;i<lens;i++) {
                response.append(" ").append(arg[i]);
            }
            Call.sendSystemMessage(response.toString().replace("<>",""));
        });

        handler.<StrCons>register("giveadmin", "<PlayerSerialNumber...>","serverCommands.giveadmin", (arg, log) -> {
            Data.playerGroup.each(p -> p.isAdmin,i -> {
                Player player = Data.game.playerData[Integer.parseInt(arg[0])-1];
                if (player != null) {
                    i.isAdmin = false;
                    player.isAdmin = true;
                    Call.upDataGameData();
                    Call.sendMessage(player,localeUtil.getinput("give.ok",player.name));
                }
            });
        });

        handler.<StrCons>register("restart", "serverCommands.restart", (arg, log) -> {
            NetServer.closeServer();
            Data.SERVERCOMMAND.handleMessage("start");
        });

        handler.<StrCons>register("gameover", "serverCommands.gameover", (arg, log) -> {
            Events.fire(new EventType.GameOverEvent());
        });

        handler.<StrCons>register("clearbanip", "serverCommands.clearbanip", (arg, log) -> {
            Data.core.admin.bannedIPs.clear();
        });

        handler.<StrCons>register("admin", "<add/remove> <PlayerSite>", "serverCommands.admin", (arg, log) -> {
            if(Data.game.isStartGame){
                log.get(localeUtil.getinput("err.startGame"));
                return;
            }

            if(!("add".equals(arg[0]) || "remove".equals(arg[0]))){
                log.get("Second parameter must be either 'add' or 'remove'.");
                return;
            }

            boolean add = "add".equals(arg[0]);

            int site = Integer.parseInt(arg[1])-1;
            Player player = Data.game.playerData[site];
            if(player != null){
                if(add){
                    Data.core.admin.addAdmin(player.uuid);
                }else{
                    Data.core.admin.removeAdmin(player.uuid);
                }
                player.isAdmin = add;
                try {
                    player.con.sendServerInfo(false);
                } catch (IOException e) {
                    Log.error("[Player] Send Server Info Error",e);
                }
                Call.sendTeamData();
                log.get("Changed admin status of player: {0}", player.name);
            }
        });

        handler.<StrCons>register("clearbanuuid", "serverCommands.clearbanuuid", (arg, log) -> {
            Data.core.admin.bannedUUIDs.clear();
        });

        handler.<StrCons>register("clearbanall", "serverCommands.clearbanall", (arg, log) -> {
            Data.core.admin.bannedIPs.clear();
            Data.core.admin.bannedUUIDs.clear();
        });

        handler.<StrCons>register("ban", "<PlayerSerialNumber>", "serverCommands.ban", (arg, log) -> {
            int site = Integer.parseInt(arg[0])-1;
            if (Data.game.playerData[site] != null) {
                Events.fire(new EventType.PlayerBanEvent(Data.game.playerData[site]));
            }
        });

        handler.<StrCons>register("mute", "<PlayerSerialNumber> [Time(s)]","serverCommands.mute", (arg, log) -> {
            int site = Integer.parseInt(arg[0])-1;
            if (Data.game.playerData[site] != null) {
                //Data.game.playerData[site].muteTime = getLocalTimeFromU(Long.parseLong(arg[1])*1000L);
                Data.game.playerData[site].muteTime = Time.getTimeFutureMillis(43200 * 1000L);
            }
        });

        handler.<StrCons>register("kick", "<PlayerSerialNumber> [time]", "serverCommands.kick", (arg, log) -> {
            int site = Integer.parseInt(arg[0])-1;
            if (Data.game.playerData[site] != null) {
                Data.game.playerData[site].kickTime = (arg.length > 1) ? Time.getTimeFutureMillis(Integer.parseInt(arg[1]) * 1000L) : Time.getTimeFutureMillis(60 * 1000L);
                try {
                    Data.game.playerData[site].con.sendKick(localeUtil.getinput("kick.you"));
                } catch (IOException e) {
                    Log.error("[Player] Send Kick Player Error",e);
                }
            }
        });

        handler.<StrCons>register("isafk", "<off/on>", "serverCommands.isAfk", (arg, log) -> {
            if (Data.game.oneAdmin) {
                Data.game.isAfk = "on".equals(arg[0]);
            }
        });

        handler.<StrCons>register("maplock", "<off/on>", "serverCommands.isAfk", (arg, log) -> {
            Data.game.mapLock = "on".equals(arg[0]);
        });

        handler.<StrCons>register("plugins", "serverCommands.plugins", (arg, log) -> {
            PluginManage.run(e -> log.get(localeUtil.getinput("plugin.info",e.name,e.description,e.author,e.version)));
        });

        handler.<StrCons>register("players", "serverCommands.players", (arg, log) -> {
            if(Data.playerGroup.size() == 0){
                log.get("No players are currently in the server.");
            }else{
                log.get("Players: {0}", Data.playerGroup.size());
                StringBuilder data = new StringBuilder();
                for(Player player : Data.playerGroup){
                    data.append(LINE_SEPARATOR)
                        .append(player.name)
                        .append(" / ")
                        .append("ID: ").append(player.uuid)
                        .append(" / ")
                        .append("IP: ").append(player.con.getIp())
                        .append(" / ")
                        .append("Protocol: ").append(player.con.getConnectionAgreement())
                        .append(" / ")
                        .append("Admin: ").append(player.isAdmin);
                }
                log.get(data.toString());
            }
        });

        handler.<StrCons>register("kill", "<PlayerSerialNumber>", "serverCommands.kill", (arg, log) -> {
            if (Data.game.isStartGame) {
                int site = Integer.parseInt(arg[0])-1;
                if (Data.game.playerData[site] != null) {
                    Data.game.playerData[site].con.sendSurrender();
                }
            } else {
                log.get(localeUtil.getinput("err.noStartGame"));
            }
        });

        handler.<StrCons>register("clearmuteall", "serverCommands.clearmuteall", (arg, log) -> {
            Data.playerGroup.each(e -> {
                e.muteTime = 0;
            });
        });

        handler.<StrCons>register("cleanmods", "serverCommands.cleanmods", (arg, log) -> {
            Data.core.unitBase64.clear();
            Data.core.save();
            Main.loadNetCore();
        });

        handler.<StrCons>register("reloadmaps", "serverCommands.reloadmaps", (arg, log) -> {
            int size = Data.game.mapsData.size;
            Data.game.mapsData.clear();
            Data.game.checkMaps();
            log.get("Reload {0}:{1}",size,Data.game.mapsData.size);
        });

        handler.<StrCons>register("maps", "serverCommands.clearmuteall", (arg, log) -> {
            StringBuilder response = new StringBuilder();
            final AtomicInteger i = new AtomicInteger(0);
            Data.game.mapsData.each((k,v) -> {
                response.append(localeUtil.getinput("maps.info", i.get(),k)).append(LINE_SEPARATOR);
                i.getAndIncrement();
            });
            log.get(response.toString());
        });

        handler.<StrCons>register("stop", "serverCommands.stop", (arg, log) -> {
            log.get("Stop Server. end");
            NetServer.closeServer();
        });

        handler.<StrCons>register("version","serverCommands.version", (arg, log) -> {
            log.get(localeUtil.getinput("status.versionS",Data.core.getJavaHeap()/1024/1024,Data.SERVER_CORE_VERSION));
        });

        handler.<StrCons>register("exit", "serverCommands.exit", (arg, log) -> {
            Core.exit();
        });
    }
}
