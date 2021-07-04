package com.github.dr.rwserver.core;

import com.github.dr.rwserver.core.ex.Threads;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.data.global.NetStaticData;
import com.github.dr.rwserver.net.game.StartNet;
import com.github.dr.rwserver.util.file.FileUtil;
import com.github.dr.rwserver.util.log.Log;

import java.util.concurrent.TimeUnit;

/**
 * @author Dr
 */
public class NetServer {

    public static void closeServer() {
        if (Data.game != null) {
            NetStaticData.startNet.each(StartNet::stop);
            Data.game.ping.cancel(true);
            Data.game.team.cancel(true);
            Data.game.ping = null;
            Data.game.team = null;
            Data.game = null;
            Data.playerGroup.clear();
            Data.playerAll.clear();
            Log.clog("Server closed");
        }
    }

    public static void reLoadServer() {
        if (Data.game.gameTask != null) {
            Data.game.gameTask.cancel(true);
            Data.game.gameTask = null;
        }
        if (Data.game.gameOver != null) {
            Data.game.gameOver.cancel(true);
            Data.game.gameOver = null;
        }
        if (Data.game.ping != null) {
            Data.game.ping.cancel(true);
            Data.game.ping = null;
        }
        if (Data.game.winOrLoseCheck != null) {
            Data.game.winOrLoseCheck.cancel(true);
            Data.game.winOrLoseCheck = null;
        }
        Call.killAllPlayer();
        Data.playerGroup.clear();
        Data.playerAll.clear();
        Data.game.re();
        Data.game.ping = Threads.newThreadService2(Call::sendPlayerPing,0,2, TimeUnit.SECONDS);
        Data.game.isStartGame = false;
        FileUtil fileUtil = FileUtil.file(Data.Plugin_Log_Path).toPath("Log.txt");
        fileUtil.writeFile(Log.getLogCache(), fileUtil.getFile().length() <= 1024 * 1024);

        Log.clog("Server Gameover completed");
    }
}
