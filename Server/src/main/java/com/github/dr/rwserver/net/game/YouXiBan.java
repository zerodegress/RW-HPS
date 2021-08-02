package com.github.dr.rwserver.net.game;

import com.github.dr.rwserver.core.Call;
import com.github.dr.rwserver.core.thread.Threads;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.data.global.NetStaticData;
import com.github.dr.rwserver.func.StrCons;
import com.github.dr.rwserver.game.Rules;
import com.github.dr.rwserver.net.netconnectprotocol.GameVersionServer;
import com.github.dr.rwserver.net.netconnectprotocol.TypeRwHps;
import com.github.dr.rwserver.plugin.Plugin;
import com.github.dr.rwserver.util.game.CommandHandler;
import com.github.dr.rwserver.util.log.Log;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class YouXiBan extends Plugin {

    @Override
    public void onEnable() {

    }

    @Override
    public void registerServerCommands(CommandHandler handler) {
        handler.removeCommand("start");
        handler.<StrCons>register("start", "serverCommands.start", (arg, log) -> {
            if (Data.serverChannelB != null) {
                log.get("The server is not closed, please close");
                return;
            }
            Log.set(Data.config.readString("log","WARN").toUpperCase());

            Data.game = new Rules(Data.config);
            Data.game.init();
            Threads.newThreadService2(Call::sendTeamData,0,2, TimeUnit.SECONDS,"GameTeam");
            Threads.newThreadService2(Call::sendPlayerPing,0,2, TimeUnit.SECONDS,"GamePing");
            NetStaticData.protocolData.setTypeConnect(new TypeRwHps());
            NetStaticData.protocolData.setNetConnectProtocol(new GameVersionServer(null),151);
            Threads.newThreadCore(() -> {
                try{
                    gameStartSchema();
                }catch (Exception e){
                    Log.error("游戏板初始失败，使用默认方式开启："+e.getMessage());
                    StartNet startNet = new StartNet();
                    NetStaticData.startNet.add(startNet);
                    startNet.openPort(Data.game.port);
                }
            });
            if (Data.config.readBoolean("UDPSupport",false)) {
                Threads.newThreadCore(() -> {
                    try {
                        StartNet startNet = new StartNet();
                        NetStaticData.startNet.add(startNet);
                        startNet.startUdp(Data.game.port);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
        });
        handler.<StrCons>register("config", "<key> <value>","serverCommands.config", (arg, log) -> {
            Data.config.setConfig(arg[0],arg[1]);
        });
    }
    private void gameStartSchema() throws IllegalAccessException {
        StartNet startNet = new StartNet();
        Optional<Field> any = Arrays.stream(startNet.getClass().getDeclaredFields()).filter(x -> x.getType() == StartGameNetTcp.class).findAny();
        if(any.isPresent()){
            Field sta = any.get();
            sta.setAccessible(true);
            sta.set(startNet,new ChoiceStarGameNet(startNet));
        }else throw new RuntimeException("没有找到：StartGameNetTcp对象");
        NetStaticData.startNet.add(startNet);
        Log.clog("游戏板嵌入成功");
        startNet.openPort(Data.game.port);
    }

    @Override
    public void registerClientCommands(CommandHandler handler) {
    }


    @Override
    public void init() {

    }
}
