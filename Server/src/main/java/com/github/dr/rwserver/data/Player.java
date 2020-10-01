package com.github.dr.rwserver.data;

import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.game.Team;
import com.github.dr.rwserver.net.AbstractNetConnect;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author Administrator
 */
public class Player {
	/** 玩家 Ip */
	public final String ip;
	/** 玩家连接UUID */
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
	private int credits = Data.game.credits;
	/** 共享控制 */
	public boolean sharedControl = false;
	/** */
	public boolean start = false;
	public long muteTime = 0;
	public long kickTime = 0;
	public long timeTemp;
	public int ping = 50;

	public boolean noSay = false;
	public boolean watch = false;

	public Player(AbstractNetConnect con, String ip, String uuid, String name) {
		this.con = con;
		this.ip = ip;
		this.uuid = uuid;
		this.name = name;
	}

	public static Player addPlayer(AbstractNetConnect con, String ip, String uuid, String name) {
		Player player = new Player(con,ip,uuid,name);
		if (Data.game.oneAdmin) {
			if (Data.playerGroup.size() ==0) {
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

	public void sendSystemMessage(String text) {
		con.sendSystemMessage(text);
	}

	public void sendMessage(Player player,String text) {
		con.sendChatMessage(text,player.name,player.team);
	}

	public void clear() {
		con = null;
	}

    public void writePlayer(DataOutputStream stream) throws IOException {
		stream.writeByte(site);
		stream.writeInt(credits);
		stream.writeInt(team);
		stream.writeBoolean(true);
		stream.writeUTF(name);
		stream.writeBoolean(false);

		if(isAdmin){
			/** -1 N/A ; -2 -  ; -99 HOST */
			stream.writeInt(-99);
		}else{
			stream.writeInt(ping);
		}
		stream.writeLong(System.currentTimeMillis());

		/** MS */
		stream.writeBoolean(false);
		stream.writeInt(0);

		stream.writeInt(site);
		stream.writeByte(0);
		/* 共享控制 */
		stream.writeBoolean(Data.game.sharedControl);
		stream.writeBoolean(sharedControl);
		stream.writeBoolean(false);
		stream.writeBoolean(false);
		stream.writeInt(-9999);
    }
}
