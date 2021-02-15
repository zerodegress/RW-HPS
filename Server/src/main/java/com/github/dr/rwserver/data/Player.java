package com.github.dr.rwserver.data;

import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.game.Team;
import com.github.dr.rwserver.net.AbstractNetConnect;
import com.github.dr.rwserver.net.AbstractNetPacket;
import com.github.dr.rwserver.util.LocaleUtil;

import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Objects;

/**
 * @author Administrator
 */
public final class Player {
	/**
	 * 玩家 Ip
	 */
	public final String ip;
	/**
	 * 玩家连接UUID
	 */
	public final String uuid;
	public String name;
	public AbstractNetConnect con;
	/** is Admin */
	public boolean isAdmin = false;
	/** 队伍序号 */
	public int team;
	/** 列表位置 */
	public int site;
	/***/
	private final int credits = Data.game.credits;
	/** 共享控制 */
	public boolean sharedControl = false;
	/**  */
	public boolean start = false;
	/**  TRY */
	public volatile boolean isTry = false;
	/**  */
	public final LocaleUtil localeUtil;
	/** 玩家是否死亡 */
	public boolean dead = false;
	/** 最后一次移动时间 */
	public volatile long lastMoveTime = 0;
	public long muteTime = 0;
	public long kickTime = 0;
	public long timeTemp;
	public int ping = 50;

	public boolean noSay = false;
	public boolean watch = false;

	private final AbstractNetPacket PACKET = Data.game.connectPacket;

	public Player(AbstractNetConnect con, final String ip, final String uuid, final String name, final LocaleUtil localeUtil) {
		this.con = con;
		this.ip = ip;
		this.uuid = uuid;
		this.name = name;
		this.localeUtil = localeUtil;
	}

	public static Player addPlayer(AbstractNetConnect con, final String ip, final String uuid, final String name, final LocaleUtil localeUtil) {
		Player player = new Player(con, ip, uuid, name, localeUtil);
		if (Data.game.oneAdmin) {
			if (Data.playerGroup.size() == 0) {
				player.isAdmin = true;
			}
		} else {
			if (Data.core.admin.playerData.contains(player.uuid)) {
				player.isAdmin = true;
			}
		}
		Team.autoPlayerTeam(player);
		Data.playerGroup.add(player);
		Data.playerAll.add(player);
		return player;
	}

	public final void sendSystemMessage(final String text) {
		con.sendSystemMessage(text);
	}

	public final void sendMessage(Player player, String text) {
		con.sendChatMessage(text, player.name, player.team);
	}

	public final void clear() {
		con = null;
	}

	public final void writePlayer(DataOutputStream stream) throws IOException {
		if (Data.game.isStartGame) {
			stream.writeByte(site);
			stream.writeInt(ping);
			stream.writeBoolean(Data.game.sharedControl);
			stream.writeBoolean(sharedControl);
			return;
		}
		stream.writeByte(site);
		stream.writeInt(credits);
		stream.writeInt(team);
		stream.writeBoolean(true);
		stream.writeUTF(name);

		stream.writeBoolean(false);

		/** -1 N/A ; -2 -  ; -99 HOST */
		stream.writeInt(ping);

		stream.writeLong(System.currentTimeMillis());
		/** MS */
		stream.writeBoolean(false);
		stream.writeInt(0);

		stream.writeInt(site);
		stream.writeByte(0);
		/* 共享控制 */
		stream.writeBoolean(Data.game.sharedControl);
		/* 是否掉线 */
		stream.writeBoolean(sharedControl);
		/* 是否投降 */
		stream.writeBoolean(false);
		stream.writeBoolean(false);
		stream.writeInt(-9999);

		stream.writeBoolean(false);
		// 延迟后显示 （HOST)
		stream.writeInt(isAdmin ? 1 : 0);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof Player) {
			return uuid.equals(((Player)o).uuid);
		}
		return uuid.equals(o.toString());
	}

	@Override
	public int hashCode() {
		return Objects.hash(uuid);
	}
}
