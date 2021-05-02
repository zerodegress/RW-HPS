package com.github.dr.rwserver.plugin;

import com.github.dr.rwserver.util.game.CommandHandler;

/**
 * @author Dr
 */
public class Plugin {

    /** 最先执行 可以进行Plugin的数据读取  -1 */
    public void onEnable() {
    }

    /** 注册要在服务器端使用的任何命令，例如从控制台 -2 */
    public void registerServerCommands(CommandHandler handler){
    }

    /** 注册要在客户端使用的任何命令，例如来自游戏内玩家 -3 */
    public void registerClientCommands(CommandHandler handler){
    }

    /** 注册事件 -4 */
    public void registerEvents(){
    }

    /** 创建所有插件并注册命令后调用 -5 */
    public void init(){
    }

    /** Server退出时执行 可以进行Plugin的数据保存  -6 */
    public void onDisable() {
    }
}
