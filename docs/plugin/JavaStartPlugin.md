# RW-HPS
```
/*
* Copyright 2020-2021 Dr.
*
*  此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
*  Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
*
*  https://github.com/deng-rui/RW-HPS/blob/master/LICENSE
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
        Events.on(PlayerJoin.class, e -> {
            try {
                e.player.con.sendKick("This Plugin Demo");
            } catch (IOException ioException) {
                Log.error("[Player] Send Kick Player Error",ioException);
            }
        });
    }
    
    @Override
    /** 注册要在服务器端使用的任何命令，例如从控制台 */
    public void registerServerCommands(CommandHandler handler){
        handler.<StrCons>register("hi", "#This Command Text", (arg, log) -> {
            log.get("Hi RW-HPS");
        });
        
        handler.<StrCons>register("hi1", "<TEXT>", "#This Command Text", (arg, log) -> {
            log.get(arg[0]);
        });
        
        handler.<StrCons>register("hi2", "<TEXT...>", "#This Command Text", (arg, log) -> {
            log.get(arg);
        });
        
        handler.<StrCons>register("hi3", "[TEXT]", "#This Command Text", (arg, log) -> {
            log.get((TEXT == null )? "null!" : arg[0]);
        });
    }
    
    @Override
    /** 注册要在客户端使用的任何命令，例如来自游戏内玩家 */
    public void registerClientCommands(CommandHandler handler){
        handler.<Player>register("maps", "[page]", "#This Command Text", (args, player) -> {
        });

        ...~
    }
}
```