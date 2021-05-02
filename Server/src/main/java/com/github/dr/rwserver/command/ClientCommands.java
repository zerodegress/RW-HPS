package com.github.dr.rwserver.command;

import com.github.dr.rwserver.core.Call;
import com.github.dr.rwserver.core.NetServer;
import com.github.dr.rwserver.core.ex.Threads;
import com.github.dr.rwserver.core.ex.Vote;
import com.github.dr.rwserver.data.Player;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.game.EventType;
import com.github.dr.rwserver.game.GameMaps;
import com.github.dr.rwserver.game.Team;
import com.github.dr.rwserver.struct.IntSet;
import com.github.dr.rwserver.util.LocaleUtil;
import com.github.dr.rwserver.util.Time;
import com.github.dr.rwserver.util.game.CommandHandler;
import com.github.dr.rwserver.util.game.Events;
import com.github.dr.rwserver.util.log.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static com.github.dr.rwserver.data.global.Data.LINE_SEPARATOR;
import static com.github.dr.rwserver.game.GameMaps.MapType;
import static com.github.dr.rwserver.util.IsUtil.*;

/**
 * @author Dr
 */
public class ClientCommands {
	private final LocaleUtil localeUtil = Data.localeUtil;

	public ClientCommands(CommandHandler handler) {
		handler.<Player>register("help", "clientCommands.help", (args, player) -> {
			StringBuilder str = new StringBuilder(16);
			for(CommandHandler.Command command : handler.getCommandList()){
				if (command.description.startsWith("#")) {
					str.append("   ").append(command.text).append(command.paramText.isEmpty() ? "" : " ").append(command.paramText).append(" - ").append(command.description.substring(1));
				} else {
					if ("HIDE".equals(command.description)) {
						continue;
					}
					str.append("   ").append(command.text).append(command.paramText.isEmpty() ? "" : " ").append(command.paramText).append(" - ").append(player.localeUtil.getinput(command.description))
							.append(LINE_SEPARATOR);
				}
			}
			player.sendSystemMessage(str.toString());
		});

		handler.<Player>register("map", "<MapNumber...>", "clientCommands.map", (args, player) -> {
			if (isAdmin(player)) {
				if (Data.game.isStartGame || Data.game.mapLock) {
					return;
				}
				final StringBuilder response = new StringBuilder(args[0]);
				for(int i=1,lens=args.length;i<lens;i++) {
					response.append(" ").append(args[i]);
				}
				final String inputMapName = response.toString().replace("'", "").replace(" ", "").replace("-", "").replace("_", "");
				final String mapPlayer = Data.MapsMap.get(inputMapName);
				if (mapPlayer != null) {
					String[] data = mapPlayer.split("@");
					Data.game.maps.mapName = data[0];
					Data.game.maps.mapPlayer = data[1];
					Data.game.maps.mapType = MapType.defaultMap;
				} else {
					if (Data.game.mapsData.size == 0) {
						return;
					}
					if (notIsNumeric(inputMapName)) {
						player.sendSystemMessage(localeUtil.getinput("err.noNumber"));
						return;
					}
					String name = Data.game.mapsData.keys().toSeq().get(Integer.parseInt(inputMapName));
					GameMaps.MapData data = Data.game.mapsData.get(name);
					Data.game.maps.mapData = data;
					Data.game.maps.mapType = data.mapType;
					Data.game.maps.mapName = name;
					Data.game.maps.mapPlayer = "";
					player.sendSystemMessage(player.localeUtil.getinput("map.custom.info"));
				}
				Call.upDataGameData();
			}
		});

		handler.<Player>register("maps", "[page]", "clientCommands.maps", (args, player) -> {
			if (Data.game.isStartGame) {
				player.sendSystemMessage(localeUtil.getinput("err.startGame"));
				return;
			}
			if (Data.game.mapsData.size == 0) {
				return;
			}
			StringBuilder response = new StringBuilder();
			final AtomicInteger i = new AtomicInteger(0);
			response.append(localeUtil.getinput("maps.top"));
			Data.game.mapsData.each((k,v) -> {
				response.append(localeUtil.getinput("maps.info", i.get(),k)).append(LINE_SEPARATOR);
				i.getAndIncrement();
			});
			player.sendSystemMessage(response.toString());
		});

		handler.<Player>register("afk", "clientCommands.afk", (args, player) -> {
			if (!Data.game.isAfk) {
				player.sendSystemMessage(localeUtil.getinput("ban.comm","afk"));
				return;
			}
			if (player.isAdmin) {
				player.sendSystemMessage(localeUtil.getinput("afk.adminNo"));
			} else {
				if (Data.game.isStartGame) {
					player.sendSystemMessage(localeUtil.getinput("err.startGame"));
					return;
				}
				if (Data.game.afk != null) {
					return;
				}
				AtomicBoolean admin = new AtomicBoolean(true);
				Data.playerGroup.each(p -> p.isAdmin,e -> admin.set(false));
				if (admin.get()&&Data.game.oneAdmin)  {
					player.isAdmin = true;
					Call.upDataGameData();
					Call.sendSystemMessageLocal("afk.end.noAdmin",player.name);
					return;
				}
				Data.game.afk = Threads.newThreadService(() -> Data.playerGroup.each(p -> p.isAdmin, i -> {
					i.isAdmin = false;
					player.isAdmin = true;
					Call.upDataGameData();
					Call.sendSystemMessageLocal("afk.end.ok",player.name);
					Data.game.afk = null;
				}),30, TimeUnit.SECONDS);
				Call.sendMessageLocal(player,"afk.start",player.name);
			}
		});

		handler.<Player>register("give", "<PlayerSerialNumber>", "clientCommands.give", (args, player) -> {
			if (isAdmin(player)) {
				if (Data.game.isStartGame) {
					player.sendSystemMessage(localeUtil.getinput("err.startGame"));
					return;
				}
				if (notIsNumeric(args[0])) {
					player.sendSystemMessage(localeUtil.getinput("err.noNumber"));
					return;
				}
				final int playerSite = Integer.parseInt(args[0])-1;
				final Player newAdmin = Data.game.playerData[playerSite];
				if (notIsBlank(newAdmin)) {
					player.isAdmin = false;
					newAdmin.isAdmin = true;
					Call.upDataGameData();
					Call.sendMessageLocal(player,"give.ok",player.name);
				}else{
					Call.sendMessageLocal(player,"give.noPlayer",player.name);
				}
			}
		});

		handler.<Player>register("nosay", "<on/off>", "clientCommands.noSay", (args, player) -> {
			player.noSay = "on".equals(args[0]);
			player.sendSystemMessage(localeUtil.getinput("server.noSay",(player.noSay) ? "开启" : "关闭"));
		});

		handler.<Player>register("am", "<on/off>", "clientCommands.am", (args, player) -> {
			Data.game.amTeam = "on".equals(args[0]);
			if (Data.game.amTeam) {
				Team.amYesPlayerTeam();
			} else {
				Team.amNoPlayerTeam();
			}
			player.sendSystemMessage(localeUtil.getinput("server.amTeam",(Data.game.amTeam) ? "开启" : "关闭"));
		});

		handler.<Player>register("income", "<income>", "clientCommands.income", (args, player) -> {
			if (isAdmin(player)) {
				if (Data.game.isStartGame) {
					player.sendSystemMessage(player.localeUtil.getinput("err.startGame"));
					return;
				}
				Data.game.income = Float.parseFloat(args[0]);
				Call.upDataGameData();
			}
		});

		handler.<Player>register("status", "clientCommands.status", (args, player) -> player.sendSystemMessage(player.localeUtil.getinput("status.version",Data.playerGroup.size(),Data.core.admin.bannedIPs.size(),Data.SERVER_CORE_VERSION)));

		handler.<Player>register("kick", "<PlayerSerialNumber>", "clientCommands.kick", (args, player) -> {
			if (Data.game.isStartGame) {
				player.sendSystemMessage(player.localeUtil.getinput("err.startGame"));
				return;
			}
			if (isAdmin(player)) {
				if (notIsNumeric(args[0])) {
					player.sendSystemMessage(player.localeUtil.getinput("err.noNumber"));
					return;
				}
				final int site = Integer.parseInt(args[0])-1;
				if (Data.game.playerData[site] != null) {
					Data.game.playerData[site].kickTime = Time.getTimeFutureMillis(60 * 1000L);
					try {
						Data.game.playerData[site].con.sendKick(localeUtil.getinput("kick.you"));
					} catch (IOException e) {
						Log.error("[Player] Send Kick Player Error",e);
					}
				}
			}
		});

		handler.<Player>register("i", "<i...>","HIDE", (args, player) -> {
		});

		/* QC */
		handler.<Player>register("credits", "<money>", "HIDE", (args, player) -> {
			if (isAdmin(player)) {
				if (notIsNumeric(args[0])) {
					player.sendSystemMessage(localeUtil.getinput("err.noNumber"));
					return;
				}
				switch (Integer.parseInt(args[0])) {
					case 0:
						Data.game.credits = 1;
						break;
					case 1000:
						Data.game.credits = 2;
						break;
					case 2000:
						Data.game.credits = 3;
						break;
					case 5000:
						Data.game.credits = 4;
						break;
					case 10000:
						Data.game.credits = 5;
						break;
					case 50000:
						Data.game.credits = 6;
						break;
					case 100000:
						Data.game.credits = 7;
						break;
					case 200000:
						Data.game.credits = 8;
						break;
					case 4000:
						Data.game.credits = 0;
						break;
					default:
						break;

				}
				Call.upDataGameData();
			}
		});

		handler.<Player>register("nukes", "<boolean>", "HIDE", (args, player) -> {
			if (isAdmin(player)) {
				Data.game.noNukes = !Boolean.parseBoolean(args[0]);
				Call.upDataGameData();
			}
		});

		handler.<Player>register("addai", "HIDE", (args, player) -> {
			player.sendSystemMessage(player.localeUtil.getinput("err.nosupr"));
		});

		handler.<Player>register("fog", "<type>", "HIDE", (args, player) -> {
			if (isAdmin(player)) {
				Data.game.mist = "off".equals(args[0]) ? 0 : "basic".equals(args[0]) ? 1 : 2;
				Call.upDataGameData();
			}
		});

		handler.<Player>register("sharedcontrol", "<boolean>", "HIDE", (args, player) -> {
			if (isAdmin(player)) {
				Data.game.sharedControl = Boolean.parseBoolean(args[0]);
				Call.upDataGameData();
			}
		});

		handler.<Player>register("startingunits", "<type>", "HIDE", (args, player) -> {
			if (isAdmin(player)) {
				if (notIsNumeric(args[0])) {
					player.sendSystemMessage(player.localeUtil.getinput("err.noNumber"));
					return;
				}
				//Data.game.initUnit = (type == 1) ? 1 : (type == 2) ? 2 : (type ==3) ? 3 : (type == 4) ? 4 : 100;
				Data.game.initUnit = Integer.parseInt(args[0]);
				Call.upDataGameData();
			}
		});

		handler.<Player>register("start", "clientCommands.start", (args, player) -> {
			if (isAdmin(player)) {
				if (Data.game.afk != null) {
					Data.game.afk.cancel(true);
					Call.sendMessageLocal(player, "afk.clear", player.name);
				}
				if (Data.game.ping != null) {
					Data.game.ping.cancel(true);
					Data.game.ping = null;
				}
				if (Data.game.maps.mapData != null) {
					Data.game.maps.mapData.readMap();
				}
				Data.playerGroup.each(e -> {
					try {
						e.con.sendStartGame();
						e.lastMoveTime = System.currentTimeMillis();
					} catch (IOException err) {
						Log.error("Start Error",err);
					}
				});

				if (Data.game.winOrLose) {
					Data.game.winOrLoseCheck = Threads.newThreadService2(() -> {
						final long time = System.currentTimeMillis();
						final int time2 = Data.game.winOrLoseTime >> 2;
						IntSet intSet = new IntSet(16);
						Data.playerGroup.each(e -> {
							if (!e.dead) {
								long breakTime = time-e.lastMoveTime;
								if (breakTime > Data.game.winOrLoseTime) {
									e.con.sendSurrender();
								} else if (breakTime > time2) {
									e.sendSystemMessage(e.localeUtil.getinput("winOrLose.time"));
								}
								intSet.add(e.team);
							}
						});
						if (intSet.size <= 1) {
							final int winTeam = intSet.iterator().next();
							Data.playerGroup.eachs(p -> (p.team == winTeam),c -> Log.info(c.name));
							Events.fire(new EventType.GameOverEvent());
						}
					},10,10, TimeUnit.SECONDS);
				}
				Data.game.isStartGame = true;
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
				Call.testPreparationPlayer();
				Events.fire(new EventType.GameStartEvent());
				if (Data.core.upServerList) {
					NetServer.upServerList();
				}
			}
		});

		handler.<Player>register("t", "<text...>","clientCommands.t", (args, player) -> {
			final StringBuilder response = new StringBuilder(args[0]);
			for(int i=1,lens=args.length;i<lens;i++) {
				response.append(" ").append(args[i]);
			}
			Call.sendTeamMessage(player.team,player,response.toString());
		});

		handler.<Player>register("upserverlist", "clientCommands.upserverlist", (args, player)-> {
			// NO 违反守则(Violation of the code)
		});

		handler.<Player>register("surrender", "clientCommands.surrender", (args, player) -> {
			if (Data.game.isStartGame) {
				if (isBlank(Data.Vote)) {
					Data.Vote = new Vote(player,"surrender");
				} else {
					Data.Vote.toVote(player,"y");
				}
			} else {
				player.sendSystemMessage(player.localeUtil.getinput("err.noStartGame"));
			}
		});

		handler.<Player>register("teamlock", "clientCommands.teamlock", (args, player) -> {
			if (isAdmin(player)) {
				Data.game.lockTeam = "on".equals(args[0]);
			}
		});

		handler.<Player>register("killme", "clientCommands.killMe", (args, player) -> {
			if (Data.game.isStartGame) {
				player.con.sendSurrender();
			} else {
				player.sendSystemMessage(player.localeUtil.getinput("err.noStartGame"));
			}
		});

		handler.<Player>register("vote","<gameover/kick> [player-site]","clientCommands.vote", (args, player) -> {
			switch(args[0].toLowerCase()) {
				case "gameover":
					Data.Vote = new Vote(player, args[0]);
					break;
				case "kick":
					if (args.length > 1 && isNumeric(args[1])) {
						Data.Vote = new Vote(player, args[0], args[1]);
					} else {
						player.sendSystemMessage(player.localeUtil.getinput("err.commandError"));
					}
					break;
				default:
					player.sendSystemMessage(player.localeUtil.getinput("err.command"));
					break;
			}
		});

		handler.<Player>register("move", "<PlayerSerialNumber> <ToSerialNumber> <?>","HIDE", (args, player) -> {
			if (Data.game.isStartGame) {
				player.sendSystemMessage(player.localeUtil.getinput("err.startGame"));
				return;
			}
			if (isAdmin(player)) {
				if (notIsNumeric(args[0]) && notIsNumeric(args[1]) && notIsNumeric(args[2])) {
					player.sendSystemMessage(player.localeUtil.getinput("err.noNumber"));
					return;
				}
				int oldSite = Integer.parseInt(args[0])-1;
				int newSite = Integer.parseInt(args[1])-1;
				int team = Integer.parseInt(args[2]);
				if (oldSite < Data.game.maxPlayer && newSite < Data.game.maxPlayer) {
					final Player od = Data.game.playerData[oldSite];
					if (newSite > -2) {
						if (Data.game.playerData[newSite] == null) {
							Data.game.playerData[oldSite] = null;
							od.site = newSite;
							if (team >-1) {
								od.team = team;
							}
							Data.game.playerData[newSite] = od;
						} else {
							final Player nw = Data.game.playerData[newSite];
							od.site = newSite;
							nw.site = oldSite;
							if (team >-1) {
								od.team = team;
							}
							Data.game.playerData[newSite] = od;
							Data.game.playerData[oldSite] = nw;
						}
					}
					Call.sendTeamData();
				}
			}
		});

		handler.<Player>register("self_move", "<ToSerialNumber> <?>","HIDE", (args, player) -> {
			if (Data.game.isStartGame) {
				player.sendSystemMessage(player.localeUtil.getinput("err.startGame"));
				return;
			}
			if (Data.game.lockTeam) {
				return;
			}
			if (notIsNumeric(args[0]) && notIsNumeric(args[1])) {
				player.sendSystemMessage(player.localeUtil.getinput("err.noNumber"));
				return;
			}
			int newSite = Integer.parseInt(args[0])-1;
			int team = Integer.parseInt(args[1]);
			if (newSite < Data.game.maxPlayer) {
				if (Data.game.playerData[newSite] == null) {
					Data.game.playerData[player.site] = null;
					player.site = newSite;
					if (team >-1) {
						player.team = team;
					}
					Data.game.playerData[newSite] = player;
					Call.sendTeamData();
				}
			}
		});

		handler.<Player>register("team", "<PlayerSiteNumber> <ToTeamNumber>","HIDE", (args, player) -> {
			if (Data.game.isStartGame) {
				player.sendSystemMessage(player.localeUtil.getinput("err.startGame"));
				return;
			}
			if (isAdmin(player)) {
				if (notIsNumeric(args[0]) && notIsNumeric(args[1])) {
					player.sendSystemMessage(player.localeUtil.getinput("err.noNumber"));
					return;
				}
				int playerSite = Integer.parseInt(args[0])-1;
				int newSite = Integer.parseInt(args[1])-1;
				if (playerSite < Data.game.maxPlayer && newSite < Data.game.maxPlayer) {
					if (newSite > -1) {
						if (notIsBlank(Data.game.playerData[playerSite])) {
							Data.game.playerData[playerSite].team = newSite;
						}
					}
					Call.sendTeamData();
				}
			}
		});

		handler.<Player>register("self_team", "<ToTeamNumber>","HIDE", (args, player) -> {
			if (Data.game.isStartGame) {
				player.sendSystemMessage(player.localeUtil.getinput("err.startGame"));
				return;
			}
			if (Data.game.lockTeam) {
				return;
			}
			if (notIsNumeric(args[0])) {
				player.sendSystemMessage(player.localeUtil.getinput("err.noNumber"));
				return;
			}
			int newSite = Integer.parseInt(args[0])-1;
			if (newSite < Data.game.maxPlayer) {
				player.team = newSite;
				Call.sendTeamData();
			}
		});


	}

	private boolean isAdmin(Player player) {
		if (player.isAdmin) {
			return true;
		}
		player.sendSystemMessage(player.localeUtil.getinput("err.noAdmin"));
		return false;
	}
}