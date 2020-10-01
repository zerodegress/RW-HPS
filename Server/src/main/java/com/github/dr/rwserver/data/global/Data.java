package com.github.dr.rwserver.data.global;

import com.github.dr.rwserver.core.Application;
import com.github.dr.rwserver.core.ex.Vote;
import com.github.dr.rwserver.data.Player;
import com.github.dr.rwserver.game.Rules;
import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.util.CommandHandler;
import com.github.dr.rwserver.util.LocaleUtil;
import com.github.dr.rwserver.util.zip.gzip.GzipEncoder;
import io.netty.channel.Channel;

import java.io.File;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Dr
 */
public class Data {

	public static final String Plugin_Data_Path                 = "/data";
	public static final String Plugin_Cache_Path 				= "/data/cache";
	public static final String Plugin_Lib_Path 					= "/data/lib";
	public static final String Plugin_Log_Path 					= "/data/log";
	public static final String Plugin_Maps_Path 				= "/data/maps";
	public static final String Plugin_Plugins_Path 				= "/data/plugins";

	public static final Charset UTF_8 = StandardCharsets.UTF_8;
	public static final LocaleUtil localeUtil = new LocaleUtil("zh_CN");

	/** 
	 * 插件默认变量
	 */

	/** 自定义包名 */
	public static final String SERVER_ID = "com.corrodinggames.rwhps";
	public static final String SERVER_CORE_VERSION = "1.2.0.4";
	//public static final int SERVER_VERSION2 = 1.13.6;
	/** 单位数据缓存 */
	public static final GzipEncoder utilData = GzipEncoder.getGzipStream("customUnits",false);
	/** */
	public static final int SERVER_MAX_TRY = 3;

	/** 服务端 客户端命令 */
	public static final CommandHandler SERVERCOMMAND = new CommandHandler("");
	public static final CommandHandler CLIENTCOMMAND = new CommandHandler("/");
	/** */
	public static final List<File> MapsList = new ArrayList<File>();
	public static final Map<String,String> MapsMap = new HashMap<String,String>();

	/** 在线玩家 */
	public static final Seq<Player> playerGroup = new Seq<Player>(16);
	/** ALL */
	public static final Seq<Player> playerAll = new Seq<Player>(16);

	public static final Application core = new Application();

	/** 
	 * 可控变量
	 */

	public static Rules game;
	public static Vote Vote = null;

	public static boolean watch = false;
	public static Channel serverChannel;
}