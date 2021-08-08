package com.github.dr.rwserver.data;

import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.game.Team;
import com.github.dr.rwserver.net.core.server.AbstractNetConnect;
import com.github.dr.rwserver.util.IsUtil;
import com.github.dr.rwserver.util.LocaleUtil;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

/**
 * @author Administrator
 */
public final class Player {
	/** 玩家连接UUID */
	public final String uuid;
	/** 玩家名字 */
	public final String name;
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

	public long lastMessageTime = 0;
	public String lastSentMessage = "";

	public boolean noSay = false;
	public boolean watch = false;

	public Player(AbstractNetConnect con, final String uuid, final String name, final LocaleUtil localeUtil) {
		this.con = con;
		this.uuid = uuid;
		this.name = name;
		this.localeUtil = localeUtil;
	}

	public static Player addPlayer(AbstractNetConnect con, final String uuid, final String name, final LocaleUtil localeUtil) {
		Player player = new Player(con, uuid, name, localeUtil);
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

	public final void sendSystemMessage(@NotNull @Nls final String text) {
		con.sendSystemMessage(text);
	}

	public final void sendMessage(Player player, @NotNull @Nls String text) {
		con.sendChatMessage(text, player.name, player.team);
	}

	/**
	 * 玩家在本地服务器的数据转移到新的服务器
	 * 此时 本地服务器只做转发 玩家数据与本地无关 玩家将不存在{@link Data}的PlayerGroup和PlayerAll中
	 * 玩家 ⇄ 本地服务器 ⇄ 新服务器
	 * @param ip
	 * @param port
	 */
	public final void playerJumpsToAnotherServer(@NotNull final String ip,final int port) {
		if (!IsUtil.isDomainName(ip)) {
			throw new RuntimeException("Error Domain");
		}
	}

	public final void clear() {
		con = null;
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
