# RW-HPS
```
/*
* Copyright 2020-2021 Dr.
*
*  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
*  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
*
*  https://github.com/mamoe/mirai/blob/master/LICENSE
   */
package dr.rwhps.plugin.test;

import com.github.dr.rwserver.plugin.Plugin;
import com.github.dr.rwserver.util.CommandHandler;


/**
 * @author Dr
 */
public class Main extends Plugin {
    @Override
    public void init(){
    
    }
    
    @Override
    /** 注册要在服务器端使用的任何命令，例如从控制台 */
    public void registerServerCommands(CommandHandler handler){
        handler.<StrCons>register("hi", "serverCommands.upserverlist", (arg, log) -> {
        });
    }
    
    @Override
    /** 注册要在客户端使用的任何命令，例如来自游戏内玩家 */
    public void registerClientCommands(CommandHandler handler){
        handler.<Player>register("maps", "[page]", "clientCommands.maps", (args, player) -> {
		});
    }
}
```