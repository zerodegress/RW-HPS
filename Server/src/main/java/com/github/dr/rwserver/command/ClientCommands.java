package com.github.dr.rwserver.command;

import com.github.dr.rwserver.command.ex.Vote;
import com.github.dr.rwserver.core.Call;
import com.github.dr.rwserver.core.thread.Threads;
import com.github.dr.rwserver.data.Player;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.ga.GroupGame;
import com.github.dr.rwserver.game.EventType;
import com.github.dr.rwserver.game.GameMaps;
import com.github.dr.rwserver.game.Rules;
import com.github.dr.rwserver.game.Team;
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
				if ( GroupGame.games.get(player.groupId).isStartGame ||  GroupGame.games.get(player.groupId).mapLock) {
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
					 GroupGame.games.get(player.groupId).maps.mapName = data[0];
					 GroupGame.games.get(player.groupId).maps.mapPlayer = data[1];
					 GroupGame.games.get(player.groupId).maps.mapType = MapType.defaultMap;
				} else {
					if ( GroupGame.games.get(player.groupId).mapsData.size == 0) {
						return;
					}
					if (notIsNumeric(inputMapName)) {
						player.sendSystemMessage(localeUtil.getinput("err.noNumber"));
						return;
					}
					String name =  GroupGame.games.get(player.groupId).mapsData.keys().toSeq().get(Integer.parseInt(inputMapName));
					GameMaps.MapData data =  GroupGame.games.get(player.groupId).mapsData.get(name);
					 GroupGame.games.get(player.groupId).maps.mapData = data;
					 GroupGame.games.get(player.groupId).maps.mapType = data.mapType;
					 GroupGame.games.get(player.groupId).maps.mapName = name;
					 GroupGame.games.get(player.groupId).maps.mapPlayer = "";
					player.sendSystemMessage(player.localeUtil.getinput("map.custom.info"));
				}
				Call.upDataGameData();
			}
		});

		handler.<Player>register("maps", "[page]", "clientCommands.maps", (args, player) -> {
			if ( GroupGame.games.get(player.groupId).isStartGame) {
				player.sendSystemMessage(localeUtil.getinput("err.startGame"));
				return;
			}
			if ( GroupGame.games.get(player.groupId).mapsData.size == 0) {
				return;
			}
			StringBuilder response = new StringBuilder();
			final AtomicInteger i = new AtomicInteger(0);
			response.append(localeUtil.getinput("maps.top"));
			 GroupGame.games.get(player.groupId).mapsData.each((k,v) -> {
				response.append(localeUtil.getinput("maps.info", i.get(),k)).append(LINE_SEPARATOR);
				i.getAndIncrement();
			});
			player.sendSystemMessage(response.toString());
		});

		handler.<Player>register("afk", "clientCommands.afk", (args, player) -> {
			if (! GroupGame.games.get(player.groupId).isAfk) {
				player.sendSystemMessage(localeUtil.getinput("ban.comm","afk"));
				return;
			}
			if (player.isAdmin) {
				player.sendSystemMessage(localeUtil.getinput("afk.adminNo"));
			} else {
				if ( GroupGame.games.get(player.groupId).isStartGame) {
					player.sendSystemMessage(localeUtil.getinput("err.startGame"));
					return;
				}
				if (Threads.getIfScheduledFutureData("AfkCountdown")) {
					return;
				}
				AtomicBoolean admin = new AtomicBoolean(true);
				Data.playerGroup.each(p -> p.isAdmin,e -> admin.set(false));
				if (admin.get()&& GroupGame.games.get(player.groupId).oneAdmin)  {
					player.isAdmin = true;
					Call.upDataGameData();
					Call.sendSystemMessageLocal("afk.end.noAdmin",player.groupId,player.name);
					return;
				}
				Threads.newThreadService(() -> Data.playerGroup.each(p -> p.isAdmin, i -> {
					i.isAdmin = false;
					player.isAdmin = true;
					Call.upDataGameData();
					Call.sendSystemMessageLocal("afk.end.ok",player.groupId,player.name);
					Threads.removeScheduledFutureData("AfkCountdown");
				}),30, TimeUnit.SECONDS,"AfkCountdown");
				Call.sendMessageLocal(player,"afk.start",player.name);
			}
		});

		handler.<Player>register("give", "<PlayerSerialNumber>", "clientCommands.give", (args, player) -> {
			if (isAdmin(player)) {
				if ( GroupGame.games.get(player.groupId).isStartGame) {
					player.sendSystemMessage(localeUtil.getinput("err.startGame"));
					return;
				}
				if (notIsNumeric(args[0])) {
					player.sendSystemMessage(localeUtil.getinput("err.noNumber"));
					return;
				}
				final int playerSite = Integer.parseInt(args[0])-1;
				final Player newAdmin =  GroupGame.games.get(player.groupId).playerData[playerSite];
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
			 GroupGame.games.get(player.groupId).amTeam = "on".equals(args[0]);
			if ( GroupGame.games.get(player.groupId).amTeam) {
				Team.amYesPlayerTeam(player.groupId);
			} else {
				Team.amNoPlayerTeam(player.groupId);
			}
			player.sendSystemMessage(localeUtil.getinput("server.amTeam",( GroupGame.games.get(player.groupId).amTeam) ? "开启" : "关闭"));
		});

		handler.<Player>register("income", "<income>", "clientCommands.income", (args, player) -> {
			if (isAdmin(player)) {
				if ( GroupGame.games.get(player.groupId).isStartGame) {
					player.sendSystemMessage(player.localeUtil.getinput("err.startGame"));
					return;
				}
				float v = Float.parseFloat(args[0]);
				if(v<100) player.con.sendSystemMessage("已锁定不小于100倍金钱");
				else {
					GroupGame.games.get(player.groupId).income =v ;
					Call.upDataGameData();

				}
			}
		});

		handler.<Player>register("status", "clientCommands.status", (args, player) -> player.sendSystemMessage(player.localeUtil.getinput("status.version",Data.playerGroup.size(),Data.core.admin.bannedIPs.size(),Data.SERVER_CORE_VERSION,player.con.getVersion())));

		handler.<Player>register("kick", "<PlayerSerialNumber>", "clientCommands.kick", (args, player) -> {
			if ( GroupGame.games.get(player.groupId).isStartGame) {
				player.sendSystemMessage(player.localeUtil.getinput("err.startGame"));
				return;
			}
			if (isAdmin(player)) {
				if (notIsNumeric(args[0])) {
					player.sendSystemMessage(player.localeUtil.getinput("err.noNumber"));
					return;
				}
				final int site = Integer.parseInt(args[0])-1;
				if ( GroupGame.games.get(player.groupId).playerData[site] != null) {
					 GroupGame.games.get(player.groupId).playerData[site].kickTime = Time.getTimeFutureMillis(60 * 1000L);
					try {
						 GroupGame.games.get(player.groupId).playerData[site].con.sendKick(localeUtil.getinput("kick.you"));
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
						 GroupGame.games.get(player.groupId).credits = 1;
						break;
					case 1000:
						 GroupGame.games.get(player.groupId).credits = 2;
						break;
					case 2000:
						 GroupGame.games.get(player.groupId).credits = 3;
						break;
					case 5000:
						 GroupGame.games.get(player.groupId).credits = 4;
						break;
					case 10000:
						 GroupGame.games.get(player.groupId).credits = 5;
						break;
					case 50000:
						 GroupGame.games.get(player.groupId).credits = 6;
						break;
					case 100000:
						 GroupGame.games.get(player.groupId).credits = 7;
						break;
					case 200000:
						 GroupGame.games.get(player.groupId).credits = 8;
						break;
					case 4000:
						 GroupGame.games.get(player.groupId).credits = 0;
						break;
					default:
						break;

				}
				Call.upDataGameData();
			}
		});

		handler.<Player>register("nukes", "<boolean>", "HIDE", (args, player) -> {
			if (isAdmin(player)) {
				 GroupGame.games.get(player.groupId).noNukes = !Boolean.parseBoolean(args[0]);
				Call.upDataGameData();
			}
		});

		handler.<Player>register("addai", "HIDE", (args, player) -> player.sendSystemMessage(player.localeUtil.getinput("err.nosupr")));

		handler.<Player>register("fog", "<type>", "HIDE", (args, player) -> {
			if (isAdmin(player)) {
				 GroupGame.games.get(player.groupId).mist = "off".equals(args[0]) ? 0 : "basic".equals(args[0]) ? 1 : 2;
				Call.upDataGameData();
			}
		});

		handler.<Player>register("sharedcontrol", "<boolean>", "HIDE", (args, player) -> {
			if (isAdmin(player)) {
				 GroupGame.games.get(player.groupId).sharedControl = Boolean.parseBoolean(args[0]);
				Call.upDataGameData();
			}
		});

		handler.<Player>register("startingunits", "<type>", "HIDE", (args, player) -> {
			if (isAdmin(player)) {
				if (notIsNumeric(args[0])) {
					player.sendSystemMessage(player.localeUtil.getinput("err.noNumber"));
					return;
				}
				// GroupGame.games.get(player.groupId).initUnit = (type == 1) ? 1 : (type == 2) ? 2 : (type ==3) ? 3 : (type == 4) ? 4 : 100;
				 GroupGame.games.get(player.groupId).initUnit = Integer.parseInt(args[0]);
				Call.upDataGameData();
			}
		});

		handler.<Player>register("start", "clientCommands.start", (args, player) -> {
			if (isAdmin(player)) {
				if (Threads.getIfScheduledFutureData("AfkCountdown")) {
					Threads.removeScheduledFutureData("AfkCountdown");
					Call.sendMessageLocal(player, "afk.clear", player.name);
				}
				Rules rules = GroupGame.games.get(player.groupId);
				if ( rules.startMinPlayerSize > GroupGame.playerGroup(player.groupId).size()) {
					player.sendSystemMessage(player.localeUtil.getinput("start.playerNo", rules.startMinPlayerSize));
					return;
				}
//				if (Threads.getIfScheduledFutureData("GamePing")) {
//					Threads.removeScheduledFutureData("GamePing");
//				}
				if ( rules.maps.mapData != null) {
					 rules.maps.mapData.readMap();
				}
				GroupGame.removePlayer(Data.playerAll,player.groupId);
				Data.playerGroup.eachBooleanIfs(p->p.groupId==player.groupId,e -> {
					try {
						e.con.sendStartGame();
						e.lastMoveTime = System.currentTimeMillis();
						Data.playerAll.add(e);
					} catch (IOException err) {
						Log.error("Start Error",err);
					}
				});

				if ( rules.winOrLose) {

				}
				rules.isStartGame = true;
				int int3 = 0;
				for (int i = 0; i <  rules.maxPlayer; i++) {
					Player player1 =  rules.playerData[i];
					if (player1 != null) {
						if (player1.sharedControl ||  rules.sharedControl) {
							int3 = (int3 | 1 << i);
						}
					}
				}
				rules.sharedControlPlayer = int3;
				Call.testPreparationPlayer(player.groupId);
				Events.fire(new EventType.GameStartEvent(player.groupId));
			}
		});

		handler.<Player>register("t", "<text...>","clientCommands.t", (args, player) -> {
			final StringBuilder response = new StringBuilder(args[0]);
			for(int i=1,lens=args.length;i<lens;i++) {
				response.append(" ").append(args[i]);
			}
			Call.sendTeamMessage(player.team,player,response.toString());
		});

		handler.<Player>register("surrender", "clientCommands.surrender", (args, player) -> {
			if ( GroupGame.games.get(player.groupId).isStartGame) {
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
				 GroupGame.games.get(player.groupId).lockTeam = "on".equals(args[0]);
			}
		});

		handler.<Player>register("killme", "clientCommands.killMe", (args, player) -> {
			if ( GroupGame.games.get(player.groupId).isStartGame) {
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
			if ( GroupGame.games.get(player.groupId).isStartGame) {
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
				//int newSite = 0;
				int team = Integer.parseInt(args[2]);
				if (oldSite <  GroupGame.games.get(player.groupId).maxPlayer && newSite <  GroupGame.games.get(player.groupId).maxPlayer) {
					final Player od =  GroupGame.games.get(player.groupId).playerData[oldSite];
					if (newSite > -2) {
						if ( GroupGame.games.get(player.groupId).playerData[newSite] == null) {
							 GroupGame.games.get(player.groupId).playerData[oldSite] = null;
							od.site = newSite;
							if (team > -1) {
								od.team = team;
							}
							 GroupGame.games.get(player.groupId).playerData[newSite] = od;
						} else {
							final Player nw =  GroupGame.games.get(player.groupId).playerData[newSite];
							od.site = newSite;
							nw.site = oldSite;
							if (team >-1) {
								od.team = team;
							}
							 GroupGame.games.get(player.groupId).playerData[newSite] = od;
							 GroupGame.games.get(player.groupId).playerData[oldSite] = nw;
						}
					}
					Call.sendTeamData(player.groupId);
				}
			}
		});

		handler.<Player>register("self_move", "<ToSerialNumber> <?>","HIDE", (args, player) -> {
			if ( GroupGame.games.get(player.groupId).isStartGame) {
				player.sendSystemMessage(player.localeUtil.getinput("err.startGame"));
				return;
			}
			if ( GroupGame.games.get(player.groupId).lockTeam) {
				return;
			}
			if (notIsNumeric(args[0]) && notIsNumeric(args[1])) {
				player.sendSystemMessage(player.localeUtil.getinput("err.noNumber"));
				return;
			}
			int newSite = Integer.parseInt(args[0])-1;
			int team = Integer.parseInt(args[1]);
			if (newSite <  GroupGame.games.get(player.groupId).maxPlayer) {
				if ( GroupGame.games.get(player.groupId).playerData[newSite] == null) {
					 GroupGame.games.get(player.groupId).playerData[player.site] = null;
					player.site = newSite;
					if (team >-1) {
						player.team = team;
					}
					 GroupGame.games.get(player.groupId).playerData[newSite] = player;
					Call.sendTeamData(player.groupId);
				}
			}
		});

		handler.<Player>register("team", "<PlayerSiteNumber> <ToTeamNumber>","HIDE", (args, player) -> {
			if ( GroupGame.games.get(player.groupId).isStartGame) {
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
				if (playerSite <  GroupGame.games.get(player.groupId).maxPlayer && newSite <  GroupGame.games.get(player.groupId).maxPlayer) {
					if (newSite > -1) {
						if (notIsBlank( GroupGame.games.get(player.groupId).playerData[playerSite])) {
							 GroupGame.games.get(player.groupId).playerData[playerSite].team = newSite;
						}
					}
					Call.sendTeamData(player.groupId);
				}
			}
		});

		handler.<Player>register("self_team", "<ToTeamNumber>","HIDE", (args, player) -> {
			if ( GroupGame.games.get(player.groupId).isStartGame) {
				player.sendSystemMessage(player.localeUtil.getinput("err.startGame"));
				return;
			}
			if ( GroupGame.games.get(player.groupId).lockTeam) {
				return;
			}
			if (notIsNumeric(args[0])) {
				player.sendSystemMessage(player.localeUtil.getinput("err.noNumber"));
				return;
			}
			int newSite = Integer.parseInt(args[0])-1;
			if (newSite < GroupGame.games.get(player.groupId).maxPlayer) {
				player.team = newSite;
				Call.sendTeamData(player.groupId);
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