package com.github.dr.rwserver.data.global;

import com.github.dr.rwserver.core.Application;
import com.github.dr.rwserver.command.ex.Vote;
import com.github.dr.rwserver.data.Player;
import com.github.dr.rwserver.game.Rules;
import com.github.dr.rwserver.struct.ObjectMap;
import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.util.game.CommandHandler;
import com.github.dr.rwserver.util.LocaleUtil;
import com.github.dr.rwserver.util.file.LoadConfig;
import com.github.dr.rwserver.util.zip.gzip.GzipEncoder;
import com.ip2location.IP2Location;
import io.netty.channel.Channel;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Dr
 */
public class Data {

	public static final String Plugin_Data_Path                 = "/data";
	public static final String Plugin_Save_Path                 = "/data/save";
	public static final String Plugin_Cache_Path 				= "/data/cache";
	public static final String Plugin_Lib_Path 					= "/data/lib";
	public static final String Plugin_Log_Path 					= "/data/log";
	public static final String Plugin_Maps_Path 				= "/data/maps";
	public static final String Plugin_Plugins_Path 				= "/data/plugins";

	public static final Charset UTF_8 = StandardCharsets.UTF_8;
	/*
	 * 插件默认变量
	 */

	/** 自定义包名 */
	public static final String SERVER_ID = "com.github.dr.rwserver";
	public static final String SERVER_CORE_VERSION = "1.3.2";
	public static final int SERVER_CORE_VERSION_INT = 200;
	/** 单位数据缓存 */
	public static final GzipEncoder utilData = GzipEncoder.getGzipStream("customUnits",false);
	/** */
	public static final String LINE_SEPARATOR = System.getProperty("line.separator");

	/** 服务端 客户端命令 */
	public static final CommandHandler SERVERCOMMAND = new CommandHandler("");
	public static final CommandHandler LOGCOMMAND = new CommandHandler("!");
	public static final CommandHandler CLIENTCOMMAND = new CommandHandler("/");
	/** */
	public static final Map<String,String> MapsMap = new HashMap<>();

	/** 在线玩家 */
	public static final Seq<Player> playerGroup = new Seq<>(16);
	/** ALL */
	public static final Seq<Player> playerAll = new Seq<>(16);

	public static final Application core = new Application();
	public static final ObjectMap<String,LocaleUtil> localeUtilMap = new ObjectMap<>(8);

	public static LoadConfig config;
	public static IP2Location ip2Location = null;

    /**
	 * 可控变量
	 */
	public static LocaleUtil localeUtil = new LocaleUtil("zh_CN");

	public static Rules game;
	public static Vote Vote = null;

	public static Channel serverChannelB = null;
}