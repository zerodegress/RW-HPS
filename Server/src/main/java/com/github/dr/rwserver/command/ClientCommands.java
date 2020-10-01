package com.github.dr.rwserver.command;

import com.github.dr.rwserver.core.Call;
import com.github.dr.rwserver.core.NetServer;
import com.github.dr.rwserver.core.ex.Threads;
import com.github.dr.rwserver.core.ex.Vote;
import com.github.dr.rwserver.data.Player;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.game.EventType;
import com.github.dr.rwserver.util.CommandHandler;
import com.github.dr.rwserver.util.Events;
import com.github.dr.rwserver.util.LocaleUtil;
import com.github.dr.rwserver.util.file.FileUtil;
import com.github.dr.rwserver.util.log.Log;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import static com.github.dr.rwserver.game.GameMaps.MapType;
import static com.github.dr.rwserver.util.IsUtil.*;

/**
 * @author Dr
 */
public class ClientCommands {
	private final LocaleUtil localeUtil = Data.localeUtil;

	public ClientCommands(CommandHandler handler) {
		handler.<Player>register("help", "Displays this command list !", (args, player) -> {
			player.sendSystemMessage(localeUtil.getinput("help.info"));
		});

		handler.<Player>register("map", "<MapNumber...>", "...", (args, player) -> {
			if (isAdmin(player)) {
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
					Data.game.maps.MapType = MapType.defaultMap;
					Call.upDataGameData();
				} else {
					if (Data.MapsList.size() == 0) {
						return;
					}
					if (notIsNumeric(args[0])) {
						player.sendSystemMessage(localeUtil.getinput("err.noNumber"));
						return;
					}
					Data.game.maps.MapType = MapType.customMap;
					final File file = Data.MapsList.get(Integer.parseInt(args[0]));
					if (file == null) {
						return;
					}
					Data.game.maps.mapSize = (int)file.length();
					try {
						final FileUtil data = new FileUtil(file);
						Data.game.maps.bytesMap = data.readFileByte();
					} catch (Exception e) {
						Log.error(e);
						Data.game.maps.MapType = MapType.defaultMap;
						Data.game.maps.bytesMap = null;
						return;
					}
					Data.game.maps.mapName = file.getName().substring(0, file.getName().length()-4);
					Data.game.maps.mapPlayer = "";
				}
			}
		});

		handler.<Player>register("maps", "[page]", "Displays this maps list !", (args, player) -> {
			if (Data.game.isStartGame) {
				player.sendSystemMessage(localeUtil.getinput("err.startGame"));
				return;
			}
			if (Data.MapsList.size() == 0) {
				return;
			}
			if (Data.Pro) {
				StringBuilder response = new StringBuilder();
				int i = 0;
				for (File file : Data.MapsList) {
					response.append(localeUtil.getinput("maps.info",i,file.getName().substring(0, file.getName().length()-4))).append("\n");
					i++;
				}
				player.sendSystemMessage(response.toString());
			} else {
				File file = Data.MapsList.get(0);
				player.sendSystemMessage(localeUtil.getinput("maps.info","0",file.getName().substring(0, file.getName().length()-4)));
			}

		});

		handler.<Player>register("afk", "...", (args, player) -> {
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
				Data.game.afk = Threads.newThreadService(() -> {
					Data.playerGroup.each(p -> p.isAdmin, i -> {
						i.isAdmin = false;
						player.isAdmin = true;
						try {
							i.con.sendServerInfo();
							player.con.sendServerInfo();
						} catch (Exception e) {
							Log.error("AFK",e);
						}
						Call.sendMessage(player,localeUtil.getinput("afk.end.ok",player.name));
					});
				},30, TimeUnit.SECONDS);
				Call.sendMessage(player,localeUtil.getinput("afk.start",player.name));
			}
		});

		handler.<Player>register("give", "<PlayerSerialNumber>", "...", (args, player) -> {
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
					try {
						player.con.sendServerInfo();
						player.con.sendServerInfo();
					} catch (Exception e) {
						Log.error("GIVE",e);
					}
					Call.sendMessage(player,localeUtil.getinput("give.ok",player.name));
				}else{
					Call.sendMessage(player,localeUtil.getinput("give.noPlayer",player.name));
				}
			}
		});

		handler.<Player>register("nosay", "<on/off>", "...", (args, player) -> {
			player.noSay = "on".equals(args[0]);
			player.con.sendChatMessage(localeUtil.getinput("server.noSay",(player.noSay) ? "开启" : "关闭"), "SERVER", 5);
		});

		handler.<Player>register("income", "<income>", "...", (args, player) -> {
			if (isAdmin(player)) {
				if (Data.game.isStartGame) {
					player.sendSystemMessage(localeUtil.getinput("err.startGame"));
					return;
				}
				Data.game.income = Float.parseFloat(args[0]);
				Call.upDataGameData();
			}
		});

		handler.<Player>register("status", "...", (args, player) -> {
			player.sendSystemMessage(localeUtil.getinput("status.version",Data.playerGroup.size(),Data.core.admin.bannedIPs.size(),Data.SERVER_CORE_VERSION));
		});

		handler.<Player>register("watch", "...", (args, player) -> {
		});

		handler.<Player>register("autofix", "...", (args, player) -> {
			//
			//
			////
		});

		handler.<Player>register("kick", "<PlayerSerialNumber>", "...", (args, player) -> {
			if (Data.game.isStartGame) {
				player.sendSystemMessage(localeUtil.getinput("err.startGame"));
				return;
			}
			if (isAdmin(player)) {
				if (notIsNumeric(args[0])) {
					player.sendSystemMessage(localeUtil.getinput("err.noNumber"));
					return;
				}
				final int site = Integer.parseInt(args[0])-1;
				if (Data.game.playerData[site] != null) {
					Data.game.playerData[site].con.sendKick(localeUtil.getinput("kick.you"));
				}
			}
		});

		/* QC */
		handler.<Player>register("credits", "<money>", "...", (args, player) -> {
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

		handler.<Player>register("nukes", "<boolean>", "...", (args, player) -> {
			if (isAdmin(player)) {
				Data.game.noNukes = !Boolean.parseBoolean(args[0]);
				Call.upDataGameData();
			}
		});

		handler.<Player>register("addai", "...", (args, player) -> {
			player.sendSystemMessage(localeUtil.getinput("err.nosupr"));
		});

		handler.<Player>register("fog", "<type>", "...", (args, player) -> {
			if (isAdmin(player)) {
				Data.game.mist = "off".equals(args[0]) ? 0 : "basic".equals(args[0]) ? 1 : 2;
				Call.upDataGameData();
			}
		});

		handler.<Player>register("sharedcontrol", "<boolean>", "...", (args, player) -> {
			if (isAdmin(player)) {
				Data.game.sharedControl = Boolean.parseBoolean(args[0]);
				Call.upDataGameData();
			}
		});

		handler.<Player>register("startingunits", "<type>", "...", (args, player) -> {
			if (isAdmin(player)) {
				if (notIsNumeric(args[0])) {
					player.sendSystemMessage(localeUtil.getinput("err.noNumber"));
					return;
				}
				final int type = Integer.parseInt(args[0]);
				Data.game.initUnit = (type == 1) ? 1 : (type == 2) ? 2 : (type ==3) ? 3 : (type == 4) ? 4 : 100;
				Call.upDataGameData();
			}
		});

		handler.<Player>register("start", "...", (args, player) -> {
			if (isAdmin(player)) {
				Data.game.ping.cancel(true);
				Data.game.ping = null;
				if (Data.game.afk != null) {
					Data.game.afk.cancel(true);
					Call.sendMessage(player, Data.localeUtil.getinput("afk.clear", player.name));
				}
				Data.playerGroup.each(e -> {
					try {
						e.con.startGame();
					} catch (IOException ioException) {
						ioException.printStackTrace();
					}
				});
				Data.game.isStartGame = true;
				Call.testPreparationPlayer();
				Events.fire(new EventType.GameStartEvent());
				if (Data.core.upServerList) {
					NetServer.upServerList();
				}
			}
		});

		handler.<Player>register("t", "<text...>","...", (args, player) -> {
			final StringBuilder response = new StringBuilder(args[0]);
			for(int i=1,lens=args.length;i<lens;i++) {
				response.append(" ").append(args[i]);
			}
			Call.sendTeamMessage(player.team,player,response.toString());
		});

		handler.<Player>register("surrender", "...", (args, player) -> {
			if (Data.game.isStartGame) {
				if (isBlank(Data.Vote)) {
					Data.Vote = new Vote(player,"surrender");
				} else {
					Data.Vote.toVote(player,"y");
				}
			} else {
				player.sendSystemMessage(localeUtil.getinput("err.noStartGame"));
			}
		});

		handler.<Player>register("killme", "...", (args, player) -> {
			if (Data.game.isStartGame) {
				player.con.surrender();
			} else {
				player.sendSystemMessage(localeUtil.getinput("err.noStartGame"));
			}
		});

		handler.<Player>register("move", "<PlayerSerialNumber> <ToSerialNumber> <?>","...", (args, player) -> {
			if (Data.game.isStartGame) {
				player.sendSystemMessage(localeUtil.getinput("err.startGame"));
				return;
			}
			if (isAdmin(player)) {
				if (notIsNumeric(args[0]) && notIsNumeric(args[1]) && notIsNumeric(args[2])) {
					player.sendSystemMessage(localeUtil.getinput("err.noNumber"));
					return;
				}
				int oldSite = Integer.parseInt(args[0])-1;
				int newSite = Integer.parseInt(args[1])-1;
				int team = Integer.parseInt(args[2])-1;
				if (oldSite < Data.game.maxPlayer && newSite < Data.game.maxPlayer) {
					final Player od = Data.game.playerData[oldSite];
					if (Data.game.playerData[newSite] == null) {
						Data.game.playerData[oldSite] = null;
						od.site = newSite;
						if (team >-1) {
							od.team = team;
						}
						Data.game.playerData[newSite] = od;
					} else {
						final Player nw = Data.game.playerData[newSite];
						Data.game.playerData[oldSite] = null;
						Data.game.playerData[newSite] = null;
						od.site = newSite;
						nw.site = oldSite;
						if (team >-1) {
							od.team = team;
						}
						Data.game.playerData[newSite] = od;
						Data.game.playerData[oldSite] = nw;
					}
					Call.sendTeamData();
				}
			}
		});

		handler.<Player>register("self_move", "<ToSerialNumber> <?>","...", (args, player) -> {
			if (Data.game.isStartGame) {
				player.sendSystemMessage(localeUtil.getinput("err.startGame"));
				return;
			}
			if (notIsNumeric(args[0]) && notIsNumeric(args[1])) {
				player.sendSystemMessage(localeUtil.getinput("err.noNumber"));
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

		handler.<Player>register("team", "<PlayerSiteNumber> <ToTeamNumber>","...", (args, player) -> {
			if (Data.game.isStartGame) {
				player.sendSystemMessage(localeUtil.getinput("err.startGame"));
				return;
			}
			if (isAdmin(player)) {
				if (notIsNumeric(args[0]) && notIsNumeric(args[1])) {
					player.sendSystemMessage(localeUtil.getinput("err.noNumber"));
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

		handler.<Player>register("self_team", "<ToTeamNumber>","...", (args, player) -> {
			if (Data.game.isStartGame) {
				player.sendSystemMessage(localeUtil.getinput("err.startGame"));
				return;
			}
			if (notIsNumeric(args[0])) {
				player.sendSystemMessage(localeUtil.getinput("err.noNumber"));
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
		player.sendSystemMessage(localeUtil.getinput("err.noAdmin"));
		return false;
	}
}