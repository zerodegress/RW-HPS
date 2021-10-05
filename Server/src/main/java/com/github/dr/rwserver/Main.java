/*
 * Copyright 2020-2021 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver;

import com.github.dr.rwserver.command.ClientCommands;
import com.github.dr.rwserver.command.LogCommands;
import com.github.dr.rwserver.command.ServerCommands;
import com.github.dr.rwserver.core.Core;
import com.github.dr.rwserver.core.Initialization;
import com.github.dr.rwserver.core.thread.Threads;
import com.github.dr.rwserver.custom.UpListCustom;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.data.global.NetStaticData;
import com.github.dr.rwserver.data.plugin.PluginEventManage;
import com.github.dr.rwserver.data.plugin.PluginManage;
import com.github.dr.rwserver.dependent.LibraryManager;
import com.github.dr.rwserver.func.StrCons;
import com.github.dr.rwserver.game.Event;
import com.github.dr.rwserver.game.EventType;
import com.github.dr.rwserver.io.GameOutputStream;
import com.github.dr.rwserver.net.netconnectprotocol.GameVersionPacket;
import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.util.encryption.Autograph;
import com.github.dr.rwserver.util.encryption.Base64;
import com.github.dr.rwserver.util.file.FileUtil;
import com.github.dr.rwserver.util.file.LoadConfig;
import com.github.dr.rwserver.util.game.CommandHandler;
import com.github.dr.rwserver.util.game.Events;
import com.github.dr.rwserver.util.io.IoReadConversion;
import com.github.dr.rwserver.util.log.Log;

import java.io.BufferedReader;
import java.util.Objects;
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
	public static void main(String[] args) {
		final Initialization initialization = new Initialization();

		Log.set("ALL");
		Log.setCopyPrint(true);
		Logger.getLogger("io.netty").setLevel(Level.OFF);

		System.out.println(Data.localeUtil.getinput("server.login"));
		Log.clog("Load ing...");

		/* 防止修改签名 */
		/* CP #1 */
		if (!new Autograph().verify(Main.class.getProtectionDomain().getCodeSource().getLocation())) {
			Log.skipping("The server was modified and refused to start");
			Core.mandatoryExit();
		}

		FileUtil.setFilePath((args.length > 0) ? Base64.decodeString(args[0]) : null);

		Data.core.load();

		//loadCoreJar((args.length > 1) ? Base64.decodeString(args[1]) : null);

		Log.clog(Data.localeUtil.getinput("server.hi"));

		Data.config = new LoadConfig(Data.Plugin_Data_Path,"Config.json");

		/* 命令加载 */
		new ServerCommands(Data.SERVER_COMMAND);
		new ClientCommands(Data.CLIENT_COMMAND);
		new LogCommands(Data.LOG_COMMAND);
		Log.clog(Data.localeUtil.getinput("server.load.command"));

		/* Event加载 */
		PluginEventManage.add(new Event());
		Log.clog(Data.localeUtil.getinput("server.load.events"));

		/* 初始化Plugin */
		PluginManage.init(FileUtil.getFolder(Data.Plugin_Plugins_Path));
		PluginManage.runOnEnable();
		PluginManage.runRegisterClientCommands(Data.CLIENT_COMMAND);
		PluginManage.runRegisterServerCommands(Data.SERVER_COMMAND);
		PluginManage.runRegisterEvents();

		/* Core Net */
		loadNetCore();

		/* Load Save Unit */
		loadUnitList();

		/* 按键监听 */
		Threads.newThreadCore(Main::buttonMonitoring);

		/* 加载完毕 */
		Events.fire(new EventType.ServerLoadEvent());

		/* 初始化Plugin Init */
		PluginManage.runInit();

		Log.clog(Data.localeUtil.getinput("server.load.end"));
		Log.clog(Data.localeUtil.getinput("server.loadPlugin",PluginManage.getLoadSize()));

		/* 默认直接启动服务器 */
		Data.SERVER_COMMAND.handleMessage("start",(StrCons) Log::clog);

		new UpListCustom(Data.SERVER_COMMAND);
	}

	private static void loadCoreJar(String libPath) {
		LibraryManager lib;
		if (notIsBlank(libPath)) {
			lib = new LibraryManager(libPath);
		} else {
			lib = new LibraryManager(true,Data.Plugin_Lib_Path);
		}
		lib.importLib("io.netty","netty-all","4.1.67.Final");
		//lib.importLib("com.ip2location","ip2location-java","8.5.0");
		lib.loadToClassLoader();
		lib.removeOldLib();
	}

	@SuppressWarnings("InfiniteLoopStatement")
	private static void buttonMonitoring() {
		BufferedReader bufferedReader = IoReadConversion.streamBufferRead(System.in);
		int count = 0;
		while (true) {
			try {
				String str = bufferedReader.readLine();
				CommandHandler.CommandResponse response = Data.SERVER_COMMAND.handleMessage(str, (StrCons) Log::clog);
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
			} catch (Exception e) {
				Log.clog("Error");

				/* nohup Error */
				if (10 < count++) {
					try {
						bufferedReader.close();
					} catch (Exception ignored) {}
					return;
				}
			}
		}
	}

	public static void loadNetCore() {
		NetStaticData.protocolData.setNetConnectPacket(new GameVersionPacket(),"2.0.0");
		try {
			GameOutputStream stream = Data.utilData;
			stream.reset();
			stream.writeInt(1);
			Seq<String> list = FileUtil.readFileListString(Objects.requireNonNull(Main.class.getResourceAsStream("/unitData-114")));
			stream.writeInt(list.size());
			String[] unitData;
			for (String str : list) {
				unitData = str.split("%#%");
				stream.writeString(unitData[0]);
				stream.writeInt(Integer.parseInt(unitData[1]));
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

	public static void loadUnitList() {
		if(Data.core.unitBase64.size() > 0) {
			try {
				//Data.utilData.buffer.reset();
				GameOutputStream stream = Data.utilData;
				stream.reset();
				stream.writeInt(1);
				stream.writeInt(Data.core.unitBase64.size());

				String[] unitData;
				for (String str : Data.core.unitBase64) {
					unitData = str.split("%#%");
					stream.writeString(unitData[0]);
					stream.writeInt(Integer.parseInt(unitData[1]));
					stream.writeBoolean(true);
					if (unitData.length > 2) {
						stream.writeBoolean(true);
						stream.writeString(unitData[2]);
					} else {
						stream.writeBoolean(false);
					}
					stream.writeLong(0);
					stream.writeLong(0);
				}
				Log.clog("Load Mod Ok.");
			} catch (Exception exp) {
				Log.error("[Server] Load Setting Unit List Error",exp);
			}
		}
	}
}