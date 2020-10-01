package com.github.dr.rwserver.command;

import com.github.dr.rwserver.Main;
import com.github.dr.rwserver.core.Call;
import com.github.dr.rwserver.core.Core;
import com.github.dr.rwserver.core.NetServer;
import com.github.dr.rwserver.core.ex.Threads;
import com.github.dr.rwserver.data.Player;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.game.EventType;
import com.github.dr.rwserver.game.Rules;
import com.github.dr.rwserver.net.Administration;
import com.github.dr.rwserver.net.Net;
import com.github.dr.rwserver.util.CommandHandler;
import com.github.dr.rwserver.util.Convert;
import com.github.dr.rwserver.util.Events;
import com.github.dr.rwserver.util.LocaleUtil;
import com.github.dr.rwserver.util.encryption.Base64;
import com.github.dr.rwserver.util.file.FileUtil;
import com.github.dr.rwserver.util.file.LoadConfig;
import com.github.dr.rwserver.util.log.Log;

import java.io.DataOutputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.dr.rwserver.net.HttpRequest.doGet;
import static com.github.dr.rwserver.util.DateUtil.getLocalTimeFromU;

/**
 * @author Dr
 */
public class ServerCommands {
    private static LocaleUtil localeUtil = Data.localeUtil;

    public ServerCommands(CommandHandler handler) {
        handler.register("help", "Help", arg -> {
            Log.clog("Commands:");
            for(CommandHandler.Command command : handler.getCommandList()){
                Log.clog("   " + command.text + (command.paramText.isEmpty() ? "" : " ") + command.paramText + " - " + command.description);
            }
        });

        handler.register("start","[ConfigNumber]", "Starting the server will re-read the configuration file", arg -> {
            LoadConfig config = null;

            if (arg.length > 0) {
                config = new LoadConfig(Data.Plugin_Data_Path,"/"+arg[0]+".ini");
            } else {
                config = new LoadConfig(Data.Plugin_Data_Path,"/Config.ini");
            }
            Log.set(config.readString("log","WARN").toUpperCase());

            Rules game = new Rules(config);
            Data.game = game;

            Threads.newThreadService2(() -> {
                Call.sendTeamData();
            },0,2, TimeUnit.SECONDS);
            Data.game.ping = Threads.newThreadService2(() -> {
                 Call.sendPlayerPing();
            },0,3000, TimeUnit.MILLISECONDS);
            Threads.newThreadCore(() -> {
                Data.game.natStartGame = new Net.NetStartGame();
                Data.game.natStartGame.StartGame(Data.game.port, Data.game.passwd);
            });
        });

        handler.register("say", "<text...>","Say text", arg -> {
            StringBuilder response = new StringBuilder(arg[0]);
            for(int i=1,lens=arg.length;i<lens;i++) {
                response.append(" ").append(arg[i]);
            }
            Call.sendSystemMessage(response.toString().replace("<>",""));
        });

        handler.register("give", "<PlayerSerialNumber...>","Forced transfer of administrative rights", arg -> {
            Data.playerGroup.each(p -> p.isAdmin,i -> {
                Player player = Data.game.playerData[Integer.parseInt(arg[0])-1];
                if (player != null) {
                    i.isAdmin = false;
                    player.isAdmin = true;
                    try {
                        i.con.sendServerInfo();
                        player.con.sendServerInfo();
                    } catch (Exception e) {
                        Log.error("ServerGive",e);
                    }
                    Call.sendMessage(player,localeUtil.getinput("give.ok",player.name));
                }
            });
        });

        handler.register("restart", "...", arg -> {
            NetServer.closeServer();
            Data.SERVERCOMMAND.handleMessage("start");
        });

        handler.register("gameover", "End the game", arg -> {
            Events.fire(new EventType.GameOverEvent());
        });

        handler.register("clearbanip", "Clean up all banned ip", arg -> {
            Data.core.admin.bannedIPs.clear();
        });

        handler.register("admin", "<add/remove> <PlayerSite>", "Make an online user admin", arg -> {
            if(Data.game.isStartGame){
                Log.clog(localeUtil.getinput("err.startGame"));
                return;
            }

            if(!("add".equals(arg[0]) || "remove".equals(arg[0]))){
                Log.clog("Second parameter must be either 'add' or 'remove'.");
                return;
            }

            boolean add = "add".equals(arg[0]);

            Administration.PlayerInfo target;
            int site = Integer.parseInt(arg[1])-1;
            Player player = Data.game.playerData[site];
            if(player != null){
                if(add){
                    Data.core.admin.addAdmin(player.uuid);
                }else{
                    Data.core.admin.removeAdmin(player.uuid);
                }
                if(player != null) {
                    player.isAdmin = add;
                }
                player.con.upServerInfo();
                Call.sendTeamData();
                Log.clog("Changed admin status of player: {0}", player.name);
            }
        });

        handler.register("clearbanuuid", "Clean up all banned uuids", arg -> {
            Data.core.admin.bannedUUIDs.clear();
        });

        handler.register("clearbanall", "Clean up all banned", arg -> {
            Data.core.admin.bannedIPs.clear();
            Data.core.admin.bannedUUIDs.clear();
        });

        handler.register("ban", "<PlayerSerialNumber>", "...", arg -> {
            int site = Integer.parseInt(arg[0])-1;
            if (Data.game.playerData[site] != null) {
                Events.fire(new EventType.PlayerBanEvent(Data.game.playerData[site]));
            }
        });

        handler.register("mute", "<PlayerSerialNumber> <Time(s)>","...", arg -> {
            int site = Integer.parseInt(arg[0])-1;
            if (Data.game.playerData[site] != null) {
                Data.game.playerData[site].muteTime = getLocalTimeFromU(Long.parseLong(arg[1])*1000L);
            }
        });

        handler.register("kick", "<PlayerSerialNumber>", "Kick player", arg -> {
            int site = Integer.parseInt(arg[0])-1;
            if (Data.game.playerData[site] != null) {
                Data.game.playerData[site].con.sendKick(localeUtil.getinput("kick.you"));
            }
        });

        handler.register("isafk", "<off/on>", "Y/N Afk", arg -> {
            if (Data.game.oneAdmin) {
                Data.game.isAfk = "on".equals(arg[0]);
            }
        });

        handler.register("clearmuteall", "...", arg -> {
            Data.playerGroup.each(e -> {
                e.muteTime = 0;
            });
        });

        handler.register("plugins", "...", arg -> {
            Main.data.each(e -> Log.clog(localeUtil.getinput("plugin.info",e.name,e.description,e.author,e.version)));
        });

        handler.register("players", "List all players currently in game.", arg -> {
            if(Data.playerGroup.size() == 0){
                Log.clog("No players are currently in the server.");
            }else{
                Log.clog("Players: {0}", Data.playerGroup.size());
                StringBuilder data = new StringBuilder();
                for(Player player : Data.playerGroup){
                    //data.delete( 0, data.length() );
                    data.append("\n")
                        .append(player.name)
                        .append(" / ")
                        .append("ID: ").append(player.uuid)
                        .append(" / ")
                        .append("IP: ").append(player.ip)
                        .append(" / ")
                        .append("Admin: ").append(player.isAdmin);
                }
                Log.clog(data.toString());
            }
        });

        handler.register("kill", "<PlayerSerialNumber>", "kill player", arg -> {
            if (Data.game.isStartGame) {
                int site = Integer.parseInt(arg[0])-1;
                if (Data.game.playerData[site] != null) {
                    Data.game.playerData[site].con.surrender();
                }
            } else {
                Log.clog(localeUtil.getinput("err.noStartGame"));
            }
        });

        handler.register("clearmuteall", "...", arg -> {
            Data.playerGroup.each(e -> {
                e.muteTime = 0;
            });
        });

        handler.register("upserverlist", "...", arg -> {
            Threads.newThreadCore(() -> {
                Data.core.upServerList = true;
                NetServer.addServerList();
            });
        });

        handler.register("stop", "...", arg -> {
            Log.clog("Stop Server. end");
            NetServer.closeServer();
        });

        handler.register("version","Server Version", arg -> {
            Log.clog(localeUtil.getinput("status.versionS",Data.core.getJavaHeap()/1024/1024,Data.SERVER_CORE_VERSION));
        });

        handler.register("exit", "...", arg -> {
            Core.exit();
        });
    }
}
