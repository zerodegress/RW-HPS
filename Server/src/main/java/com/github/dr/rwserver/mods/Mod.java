package com.github.dr.rwserver.mods;

import com.github.dr.rwserver.util.CommandHandler;

/**
 * @author Dr
 */
public class Mod {
    /** 创建所有插件并注册命令后调用 */
    public void init(){
    }

    /** 注册要在服务器端使用的任何命令，例如从控制台 */
    public void registerServerCommands(CommandHandler handler){
    }

    /** 注册要在客户端使用的任何命令，例如来自游戏内玩家 */
    public void registerClientCommands(CommandHandler handler){
    }
}
