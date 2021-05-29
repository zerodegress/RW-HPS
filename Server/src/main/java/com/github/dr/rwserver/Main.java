package com.github.dr.rwserver;

import com.github.dr.rwserver.command.ClientCommands;
import com.github.dr.rwserver.command.LogCommands;
import com.github.dr.rwserver.command.ServerCommands;
import com.github.dr.rwserver.core.Core;
import com.github.dr.rwserver.core.Initialization;
import com.github.dr.rwserver.core.ex.Event;
import com.github.dr.rwserver.core.ex.Threads;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.data.plugin.PluginManage;
import com.github.dr.rwserver.func.StrCons;
import com.github.dr.rwserver.game.EventType;
import com.github.dr.rwserver.net.Administration;
import com.github.dr.rwserver.net.netconnectprotocol.GameVersionPacket;
import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.util.Convert;
import com.github.dr.rwserver.util.encryption.Autograph;
import com.github.dr.rwserver.util.file.FileUtil;
import com.github.dr.rwserver.util.file.LoadConfig;
import com.github.dr.rwserver.util.game.CommandHandler;
import com.github.dr.rwserver.util.game.Events;
import com.github.dr.rwserver.util.log.Log;

import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author Dr
 */
public class Main {

	/*
	 * TODO 防逆向
	 * 设置多个检查点, 定期检查, 如果发现问题就加密或混淆部分数据
	 */

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

		Data.core.settings.load();
		Data.core.load();

		Data.config = new LoadConfig(Data.Plugin_Data_Path,"Config.json");

		/* 命令加载 */
		new ServerCommands(Data.SERVERCOMMAND);
		new ClientCommands(Data.CLIENTCOMMAND);
		new LogCommands(Data.LOGCOMMAND);
		Log.clog(Data.localeUtil.getinput("server.load.command"));

		/* Event加载 */
		new Event();
		Log.clog(Data.localeUtil.getinput("server.load.events"));

		/* 初始化插件 */
		new Initialization();

		/* Plugin */
		PluginManage.init(FileUtil.File(Data.Plugin_Plugins_Path));
		PluginManage.runRegisterClientCommands(Data.CLIENTCOMMAND);
		PluginManage.runRegisterServerCommands(Data.SERVERCOMMAND);

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

		/* 默认直接启动服务器 */
		Data.SERVERCOMMAND.handleMessage("start",(StrCons) Log::clog);
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

	public static void loadNetCore() {
		Data.core.admin.setNetConnectPacket(new Administration.NetConnectPacketData(new GameVersionPacket(),151));
		try {
			DataOutputStream stream = Data.utilData.stream;
			stream.writeInt(1);
			Seq<String> list = Convert.castSeq(FileUtil.readFileData(true, new InputStreamReader(Main.class.getResourceAsStream("/unitData"), StandardCharsets.UTF_8)),String.class);
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

	public static void loadUnitList() {
		if(Data.core.unitBase64.size() > 0) {
			try {
				Data.utilData.buffer.reset();
				DataOutputStream stream = Data.utilData.stream;
				stream.writeInt(1);
				stream.writeInt(Data.core.unitBase64.size());

				String[] unitdata;
				for (String str : Data.core.unitBase64) {
					unitdata = str.split("%#%");
					stream.writeUTF(unitdata[0]);
					stream.writeInt(Integer.parseInt(unitdata[1]));
					stream.writeBoolean(true);
					if (unitdata.length > 2) {
						stream.writeBoolean(true);
						stream.writeUTF(unitdata[2]);
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