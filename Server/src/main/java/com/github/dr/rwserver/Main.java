package com.github.dr.rwserver;

import com.github.dr.rwserver.command.ClientCommands;
import com.github.dr.rwserver.command.ServerCommands;
import com.github.dr.rwserver.core.Core;
import com.github.dr.rwserver.core.Initialization;
import com.github.dr.rwserver.core.ex.Event;
import com.github.dr.rwserver.core.ex.Threads;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.dependent.LibraryManager;
import com.github.dr.rwserver.func.StrCons;
import com.github.dr.rwserver.game.EventType;
import com.github.dr.rwserver.mods.PluginsLoad;
import com.github.dr.rwserver.net.Administration;
import com.github.dr.rwserver.net.netconnectprotocol.GameVersion151;
import com.github.dr.rwserver.net.netconnectprotocol.GameVersion151Packet;
import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.util.CommandHandler;
import com.github.dr.rwserver.util.Convert;
import com.github.dr.rwserver.util.Events;
import com.github.dr.rwserver.util.encryption.Autograph;
import com.github.dr.rwserver.util.encryption.Base64;
import com.github.dr.rwserver.util.file.FileUtil;
import com.github.dr.rwserver.util.file.LoadConfig;
import com.github.dr.rwserver.util.log.Log;

import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.github.dr.rwserver.util.IsUtil.notIsBlank;

/**
 * @author Dr
 */
public class Main {

	/*
	 * TODO 防逆向
	 * 设置多个检查点, 定期检查, 如果发现问题就加密或混淆部分数据
	 */

	public static Seq<PluginsLoad.PluginData> data;

	public static void main(String[] args) {
		Log.set("ALL");
		Log.setPrint(true);
		Logger.getLogger("io.netty").setLevel(Level.OFF);

		System.out.println(Data.localeUtil.getinput("server.login"));

		Log.clog(Data.localeUtil.getinput("server.hi"));

		/* 防止修改签名 */
		/* CP #1 */
		if (!new Autograph().verify(Main.class.getProtectionDomain().getCodeSource().getLocation())) {
			Log.skipping("The server was modified and refused to start");
			Core.mandatoryExit();
		}

		FileUtil.path = (args.length > 0) ? Base64.decodeString(args[0]) : null;

		Data.core.settings.loadData();
		Data.core.load();

		loadCoreJar((args.length > 1) ? Base64.decodeString(args[1]) : null);

		Data.config = new LoadConfig(Data.Plugin_Data_Path,"Config.json");

		/* 命令加载 */
		new ServerCommands(Data.SERVERCOMMAND);
		new ClientCommands(Data.CLIENTCOMMAND);
		Log.clog(Data.localeUtil.getinput("server.load.command"));

		/* Event加载 */
		new Event();
		Log.clog(Data.localeUtil.getinput("server.load.events"));

		/* 初始化插件 */
		new Initialization();

		/* Plugin */
		data =  new PluginsLoad(FileUtil.File(Data.Plugin_Plugins_Path)).loadJar();

		/* Core Net */
		loadNetCore();

		/* 按键监听 */
		Threads.newThreadCore(Main::buttonMonitoring);

		/* 加载完毕 */
		Events.fire(new EventType.ServerLoadEvent());

		/* 初始化Plugin Init */
		data.each(e -> e.main.init());

		/* 默认直接启动服务器 */
		Data.SERVERCOMMAND.handleMessage("start",(StrCons) Log::clog);
	}

	private static void loadCoreJar(String libPath) {
		LibraryManager lib;
		if (notIsBlank(libPath)) {
			lib = new LibraryManager(libPath);
		} else {
			lib = new LibraryManager(true,Data.Plugin_Lib_Path);
		}
		lib.importLib("io.netty","netty-all","4.1.59.Final");
		lib.importLib("com.ip2location","ip2location-java","8.5.0");
		lib.importLib("com.alibaba","fastjson","1.2.58");
		lib.loadToClassLoader();
		lib.removeOldLib();
	}

	@SuppressWarnings("InfiniteLoopStatement")
	private static void buttonMonitoring() {
		Scanner scanner = new Scanner(System.in);
		while (true) {
			String str = scanner.nextLine();
			CommandHandler.CommandResponse response = Data.SERVERCOMMAND.handleMessage(str,(StrCons) Log::clog);
			if (response != null && response.type != CommandHandler.ResponseType.noCommand) {
				if (response.type != CommandHandler.ResponseType.valid) {
					String text;
					if (response.type == CommandHandler.ResponseType.manyArguments) {
						text = "Too many arguments. Usage: " + response.command.text + " " + response.command.paramText;
					} else if (response.type == CommandHandler.ResponseType.fewArguments) {
						text = "Too few arguments. Usage: " + response.command.text + " " + response.command.paramText;
					} else {
						text = "Unknown command. Check help";
					}
					Log.clog(text);
				}
			}
		}
	}

	private static void loadNetCore() {
		Data.core.admin.setNetConnectPacket(new Administration.NetConnectPacketData(new GameVersion151Packet(),151));
		Data.core.admin.setNetConnectProtocol(new Administration.NetConnectProtocolData(new GameVersion151(null,null),151));
		try {
			DataOutputStream stream = Data.utilData.stream;
			stream.writeInt(1);
			Seq<String> list = Convert.castSeq(FileUtil.readFileData(true, new InputStreamReader(Main.class.getResourceAsStream("/unitData"), Data.UTF_8)),String.class);
			stream.writeInt(list.size());
			String[] unitdata;
			for (String str : list) {
				unitdata = str.split("%#%");
				stream.writeUTF(unitdata[0]);
				stream.writeInt(Integer.parseInt(unitdata[1]));
				stream.writeBoolean(true);
				stream.writeBoolean(false);
				stream.writeLong(0);
				stream.writeLong(0);
			}
		} catch (Exception e) {
			Log.error(e);
		}
		Log.clog("Load OK 1.14 Protocol");
	}
}