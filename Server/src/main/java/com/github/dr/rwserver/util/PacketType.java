package com.github.dr.rwserver.util;

/**
 * @author Miku
 * By.Dr
 */
public class PacketType {
	public static final int PACKET_SERVER_DEBUG = 2000;
	//Server Commands
	
	
	public static final int PACKET_REGISTER_CONNECTION = 161;
	public static final int PACKET_TEAM_LIST = 115;
	public static final int PACKET_HEART_BEAT = 108;
	public static final int PACKET_SEND_CHAT = 141;
	public static final int PACKET_SERVER_INFO = 106;
	public static final int PACKET_KICK = 150;
	public static final int PACKET_A = 30;
	//Client Commands
	
	public static final int PACKET_PREREGISTER_CONNECTION = 160;
	public static final int PACKET_HEART_BEAT_RESPONSE = 109;
	public static final int PACKET_ADD_CHAT = 140;
	public static final int PACKET_PLAYER_INFO = 110;
	public static final int PACKET_DISCONNECT = 111;
	public static final int PACKET_ACCEPT_START_GAME = 112;
	public static final int PACKET_ACCEPT_BUTTON_GAME = 20;

	//Game Commands
	
	public static final int PACKET_ADD_GAMECOMMAND = 20;
	public static final int PACKET_TICK = 10;
	public static final int PACKET_SYNC = 35;
    public static final int PACKET_START_GAME = 120;
	public static final int PACKET_PASSWD_ERROR = 113;

}
