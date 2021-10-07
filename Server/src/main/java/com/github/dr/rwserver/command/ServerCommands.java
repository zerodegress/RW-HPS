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
import com.github.dr.rwserver.ga.GroupGame;
import com.github.dr.rwserver.game.EventType;
import com.github.dr.rwserver.net.game.cal.CalUt;
import com.github.dr.rwserver.net.game.cal.ChannelInfo;
import com.github.dr.rwserver.net.game.ConnectionAgreement;
import com.github.dr.rwserver.net.game.StartNet;
import com.github.dr.rwserver.net.netconnectprotocol.GameVersionPacket;
import com.github.dr.rwserver.net.netconnectprotocol.GameVersionServer;
import com.github.dr.rwserver.net.netconnectprotocol.TypeRwHps;
import com.github.dr.rwserver.plugin.center.PluginCenter;
import com.github.dr.rwserver.util.LocaleUtil;
import com.github.dr.rwserver.util.Time;
import com.github.dr.rwserver.util.file.LoadConfig;
import com.github.dr.rwserver.util.game.CommandHandler;
import com.github.dr.rwserver.util.game.Events;
import com.github.dr.rwserver.util.log.Log;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.dr.rwserver.data.global.Data.LINE_SEPARATOR;

/**
 * @author Dr
 */
public class ServerCommands {
    private static final LocaleUtil localeUtil = Data.localeUtil;

    public ServerCommands(CommandHandler handler) {
        registerCore(handler);
        registerCorex(handler);
        registerInfo(handler);
        registerPlayerCommand(handler);

        handler.<StrCons>register("log", "[a...]","serverCommands.exit", (arg, log) -> {
            Data.LOGCOMMAND.handleMessage(arg[1],null);
        });

        handler.<StrCons>register("logg", "<1> <2>","serverCommands.exit", (arg, log) -> {
            Data.LOGCOMMAND.handleMessage(arg[1]+" "+arg[2],null);
        });

        handler.<StrCons>register("kc", "<1>","serverCommands.exit", (arg, log) -> {
            int site = Integer.parseInt(arg[1])-1;
            Player player = GroupGame.games.get(arg[0]).playerData[site];
            player.con.disconnect();
        });
    }

    private void registerCore(CommandHandler handler) {
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

        handler.<StrCons>register("restart", "serverCommands.restart", (arg, log) -> {
            NetServer.closeServer();
            Data.SERVERCOMMAND.handleMessage("start");
        });

        handler.<StrCons>register("start", "serverCommands.start", (arg, log) -> {
            if (Data.serverChannelB != null) {
                log.get("The server is not closed, please close");
                return;
            }
            Log.set(Data.config.readString("log","WARN").toUpperCase());

//            GroupGame.games.get(arg[0]) = new Rules(Data.config);
//            GroupGame.games.get(arg[0]).init();
//            Threads.newThreadService2(Call::sendTeamData,0,2, TimeUnit.SECONDS,"GameTeam");
//            Threads.newThreadService2(Call::sendPlayerPing,0,2, TimeUnit.SECONDS,"GamePing");

            NetStaticData.protocolData.setTypeConnect(new TypeRwHps());
            NetStaticData.protocolData.setNetConnectProtocol(new GameVersionServer(new ConnectionAgreement()),151);
            NetStaticData.protocolData.setNetConnectPacket(new GameVersionPacket(),"2.0.0");
/*
            NetStaticData.protocolData.setTypeConnect(new TypeRwHpsBeta());
            NetStaticData.protocolData.setNetConnectProtocol(new GameVersionServerBeta(null),157);
            NetStaticData.protocolData.setNetConnectPacket(new GameVersionPacketBeta(),"3.0.0");*/
            //NetStaticData.protocolData.setNetConnectProtocol(new GameVersionFFA(null),151);
            Threads.newThreadCore(() -> {
                StartNet startNet = new StartNet();
                NetStaticData.startNet.add(startNet);
                startNet.openPort(GroupGame.games.get(arg[0]).port);
            });
            if (Data.config.readBoolean("UDPSupport",false)) {
                Threads.newThreadCore(() -> {
                    try {
                        StartNet startNet = new StartNet();
                        NetStaticData.startNet.add(startNet);
                        startNet.startUdp(GroupGame.games.get(arg[0]).port);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });

//        handler.<StrCons>register("startffa", "serverCommands.start", (arg, log) -> {
//            if (Data.serverChannelB != null) {
//                log.get("The server is not closed, please close");
//                return;
//            }
//            Log.set(Data.config.readString("log","WARN").toUpperCase());
//
//            GroupGame.games.get(arg[0]) = new Rules(Data.config);
//
//            GroupGame.games.get(arg[0]).init();
////            Threads.newThreadService2(Call::sendTeamData,0,2, TimeUnit.SECONDS,"GameTeam");
////            Threads.newThreadService2(Call::sendPlayerPing,0,2, TimeUnit.SECONDS,"GamePing");
//
//            NetStaticData.protocolData.setTypeConnect(new TypeRwHps());
//            NetStaticData.protocolData.setNetConnectPacket(new GameVersionPacket(),"2.0.0");
//            NetStaticData.protocolData.setNetConnectProtocol(new GameVersionFFA(new ConnectionAgreement()),151);
//            Threads.newThreadCore(() -> {
//                StartNet startNet = new StartNet();
//                NetStaticData.startNet.add(startNet);
//                startNet.openPort(GroupGame.games.get(arg[0]).port);
//            });
//            if (Data.config.readBoolean("UDPSupport",false)) {
//                Threads.newThreadCore(() -> {
//                    try {
//                        StartNet startNet = new StartNet();
//                        NetStaticData.startNet.add(startNet);
//                        startNet.startUdp(GroupGame.games.get(arg[0]).port);
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                });
//            }
//        });
    }

    private void registerInfo(CommandHandler handler) {
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

        handler.<StrCons>register("maps", "serverCommands.clearmuteall", (arg, log) -> {
            StringBuilder response = new StringBuilder();
            final AtomicInteger i = new AtomicInteger(0);
            GroupGame.games.get(arg[0]).mapsData.each((k,v) -> {
                response.append(localeUtil.getinput("maps.info", i.get(),k)).append(LINE_SEPARATOR);
                i.getAndIncrement();
            });
            log.get(response.toString());
        });
    }

    private void registerPlayerCommand(CommandHandler handler) {
//        handler.<StrCons>register("say", "<text...>","serverCommands.say", (arg, log) -> {
//        StringBuilder response = new StringBuilder(arg[1]);
//        for(int i=1,lens=arg.length;i<lens;i++) {
//            response.append(" ").append(arg[i]);
//        }
//        Call.sendSystemMessage(response.toString().replace("<>",""));
//          });

        handler.<StrCons>register("gameover", "<gid>","serverCommands.gameover", (arg, log) -> {
            Events.fire(new EventType.GameOverEvent(Integer.parseInt(arg[1])));
        });

        handler.<StrCons>register("clearbanip", "serverCommands.clearbanip", (arg, log) -> {
            Data.core.admin.bannedIPs.clear();
        });

        handler.<StrCons>register("admin", "<add/remove> <PlayerSite>", "serverCommands.admin", (arg, log) -> {
            if(GroupGame.games.get(arg[0]).isStartGame){
                log.get(localeUtil.getinput("err.startGame"));
                return;
            }

            if(!("add".equals(arg[1]) || "remove".equals(arg[1]))){
                log.get("Second parameter must be either 'add' or 'remove'.");
                return;
            }

            boolean add = "add".equals(arg[1]);

            int site = Integer.parseInt(arg[2])-1;
            Player player = GroupGame.games.get(arg[0]).playerData[site];
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
                Call.sendTeamData(player.groupId);
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
            int site = Integer.parseInt(arg[1])-1;
            if (GroupGame.games.get(arg[0]).playerData[site] != null) {
                Events.fire(new EventType.PlayerBanEvent(GroupGame.games.get(arg[0]).playerData[site]));
            }
        });

        handler.<StrCons>register("mute", "<PlayerSerialNumber> [Time(s)]","serverCommands.mute", (arg, log) -> {
            int site = Integer.parseInt(arg[1])-1;
            if (GroupGame.games.get(arg[0]).playerData[site] != null) {
                //GroupGame.games.get(arg[0]).playerData[site].muteTime = getLocalTimeFromU(Long.parseLong(arg[2])*1000L);
                GroupGame.games.get(arg[0]).playerData[site].muteTime = Time.getTimeFutureMillis(43200 * 1000L);
            }
        });

        handler.<StrCons>register("kick", "<PlayerSerialNumber> [time]", "serverCommands.kick", (arg, log) -> {
            int site = Integer.parseInt(arg[1])-1;
            if (GroupGame.games.get(arg[0]).playerData[site] != null) {
                GroupGame.games.get(arg[0]).playerData[site].kickTime = (arg.length > 1) ? Time.getTimeFutureMillis(Integer.parseInt(arg[2]) * 1000L) : Time.getTimeFutureMillis(60 * 1000L);
                try {
                    GroupGame.games.get(arg[0]).playerData[site].con.sendKick(localeUtil.getinput("kick.you"));
                } catch (IOException e) {
                    Log.error("[Player] Send Kick Player Error",e);
                }
            }
        });

        handler.<StrCons>register("isafk", "<off/on>", "serverCommands.isAfk", (arg, log) -> {
            if (GroupGame.games.get(arg[0]).oneAdmin) {
                GroupGame.games.get(arg[0]).isAfk = "on".equals(arg[1]);
            }
        });

        handler.<StrCons>register("maplock", "<off/on>", "serverCommands.isAfk", (arg, log) -> {
            GroupGame.games.get(arg[0]).mapLock = "on".equals(arg[1]);
        });

        handler.<StrCons>register("kill", "<PlayerSerialNumber>", "serverCommands.kill", (arg, log) -> {
            if (GroupGame.games.get(arg[0]).isStartGame) {
                int site = Integer.parseInt(arg[1])-1;
                if (GroupGame.games.get(arg[0]).playerData[site] != null) {
                    GroupGame.games.get(arg[0]).playerData[site].con.sendSurrender();
                }
            } else {
                log.get(localeUtil.getinput("err.noStartGame"));
            }
        });

        handler.<StrCons>register("giveadmin", "<PlayerSerialNumber...>","serverCommands.giveadmin", (arg, log) -> {
            Data.playerGroup.each(p -> p.isAdmin,i -> {
                Player player = GroupGame.games.get(arg[0]).playerData[Integer.parseInt(arg[1])-1];
                if (player != null) {
                    i.isAdmin = false;
                    player.isAdmin = true;
                    Call.upDataGameData(player.groupId);
                    Call.sendMessage(player,localeUtil.getinput("give.ok",player.name));
                }
            });
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
            int size = GroupGame.games.get(arg[0]).mapsData.size;
            GroupGame.games.get(arg[0]).mapsData.clear();
            GroupGame.games.get(arg[0]).checkMaps();
            log.get("Reload {0}:{1}",size,GroupGame.games.get(arg[0]).mapsData.size);
        });
    }

    private void registerCorex(CommandHandler handler) {
        handler.<StrCons>register("plugin","<TEXT...>", "serverCommands.upserverlist", (arg, log) -> {
            PluginCenter.pluginCenter.command(arg[1],log);
        });
        handler.<StrCons>register("msg", "<text>","serverCommands.say", (arg, log) -> {
            if(Data.playerGroup.isEmpty()) Log.clog("没有玩家");
            else {
                Data.playerGroup.each(e -> e.sendSystemMessage(arg[0]));
                Log.clog("已发送信息 "+arg[0]);
            }
        });
        handler.<StrCons>register("info","serverCommands.info", (arg, log) -> {
            Log.clog("连接信息：");
            int p=0;
            int online=0;
            for(ChannelInfo xx: CalUt.channelInfos.values()){
                Log.clog(xx.toString());
                if(null!=xx.getP()) {
                    p++;
                    if(Data.playerGroup.contains(xx.getP())) online++;
                }
            }
            Threads.logTasks();
            Log.clog("总计："+ CalUt.channelInfos.size()+"个连接,"+p+"个玩家，"+online+"人在线，"+Data.playerAll.size()+"个总玩家");
        });

        handler.<StrCons>register("outCon","serverCommands.info", (arg, log) -> {
            Log.clog("写出连接：");
            CalUt.flushAll();
        });
        handler.<StrCons>register("throw","serverCommands.info", (arg, log) -> {
            Log.clog("清理连接");
            CalUt.throwGab();
        });
        handler.<StrCons>register("saveCan","serverCommands.saveCan", (arg, log) -> {
            Log.clog("保存配置");
            Data.config.save();
        });

        handler.<StrCons>register("timer", "<f/n>","serverCommands.timer", (arg, log) -> {
            if(Threads.getIfScheduledFutureData("play-time")){
                if(!"-f".equals(arg[1])){
                    Log.clog("游戏计时线任务存在，使用 -f 覆盖");
                    return;
                }
            }
            GameTimeLapse.curr.refresh();
        });
    }
}
