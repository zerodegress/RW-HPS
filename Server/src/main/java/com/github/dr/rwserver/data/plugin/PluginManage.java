package com.github.dr.rwserver.data.plugin;

import com.github.dr.rwserver.plugin.PluginsLoad;
import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.util.file.FileUtil;
import com.github.dr.rwserver.util.game.CommandHandler;

public class PluginManage {
    private static Seq<PluginsLoad.PluginData> pluginData;

    public static void init(final FileUtil fileUtil) {
        pluginData = PluginsLoad.resultPluginData(fileUtil);
    }

    /** 最先执行 可以进行Plugin的数据读取  -1 */
    public static void runOnEnable() {
        pluginData.each(e -> e.main.onEnable());
    }

    /** 注册要在服务器端使用的任何命令，例如从控制台 -2 */
    public static void runRegisterServerCommands(CommandHandler handler){
        pluginData.each(e -> e.main.registerServerCommands(handler));
    }

    /** 注册要在客户端使用的任何命令，例如来自游戏内玩家 -3 */
    public static void runRegisterClientCommands(CommandHandler handler){
        pluginData.each(e -> e.main.registerClientCommands(handler));
    }

    /** 注册事件 -4 */
    public static void runRegisterEvents(){
        pluginData.each(e -> e.main.registerEvents());
    }

    /** 创建所有插件并注册命令后调用 -5 */
    public static void runInit(){
        pluginData.each(e -> e.main.init());
    }

    /** Server退出时执行 可以进行Plugin的数据保存  -6 */
    public static void runOnDisable() {
        pluginData.each(e -> e.main.onDisable());
    }

    public static void removePlugin(final String name) {
        pluginData.each(e -> {
            if (e.name.equalsIgnoreCase(name)) {
                pluginData.remove(e);
            }
        });
    }
}
