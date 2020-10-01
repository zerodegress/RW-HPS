package com.github.dr.rwserver;

import com.github.dr.rwserver.command.ClientCommands;
import com.github.dr.rwserver.command.ServerCommands;
import com.github.dr.rwserver.core.Initialization;
import com.github.dr.rwserver.core.ex.Event;
import com.github.dr.rwserver.core.ex.Threads;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.game.EventType;
import com.github.dr.rwserver.mods.PluginsLoad;
import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.util.CommandHandler;
import com.github.dr.rwserver.util.Events;
import com.github.dr.rwserver.util.file.FileUtil;
import com.github.dr.rwserver.util.log.Log;

import java.util.Scanner;

/**
 * @author Dr
 */
public class Main {

	public static Seq<PluginsLoad.PluginData> data;

	public static void main(String[] args) {
		System.out.println(Data.localeUtil.getinput("server.login"));

		Log.set("ALL");

		Log.clog(Data.localeUtil.getinput("server.hi"));

		/** 命令加载 */
		new ServerCommands(Data.SERVERCOMMAND);
		new ClientCommands(Data.CLIENTCOMMAND);
		Log.clog(Data.localeUtil.getinput("server.load.command"));

		/** Event加载 */
		new Event();
		Log.clog(Data.localeUtil.getinput("server.load.events"));

		/** 初始化插件 */
		new Initialization();

		/** Plugin */
		data =  new PluginsLoad(FileUtil.File(Data.Plugin_Plugins_Path)).loadJar();

		Threads.newThreadCore(() -> {
			Scanner scanner = new Scanner(System.in);
			while (true) {
				String str = scanner.nextLine();
				CommandHandler.CommandResponse response = Data.SERVERCOMMAND.handleMessage(str);
				if (response != null && response.type != CommandHandler.ResponseType.noCommand) {
					if(response.type != CommandHandler.ResponseType.valid){
						String text;
						if(response.type == CommandHandler.ResponseType.manyArguments){
							text = "Too many arguments. Usage: " + response.command.text + " " + response.command.paramText;
						}else if(response.type == CommandHandler.ResponseType.fewArguments){
							text = "Too few arguments. Usage: " + response.command.text + " " + response.command.paramText;
						}else{
							text = "Unknown command. Check help";
						}
						Log.clog(text);
					}
				}
			}
		});

		Events.fire(new EventType.ServerLoadEvent());

		data.each(e -> e.main.init());

		Data.SERVERCOMMAND.handleMessage("start");
	}

}