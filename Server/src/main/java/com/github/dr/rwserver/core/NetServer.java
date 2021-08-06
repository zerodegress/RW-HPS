package com.github.dr.rwserver.core;

import com.github.dr.rwserver.core.thread.Threads;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.data.global.NetStaticData;
import com.github.dr.rwserver.ga.GroupGame;
import com.github.dr.rwserver.game.Rules;
import com.github.dr.rwserver.net.game.StartNet;
import com.github.dr.rwserver.util.log.Log;

import java.util.concurrent.TimeUnit;

/**
 * @author Dr
 */
public class NetServer {

    static String userId;

    public static void closeServer() {
        if (Data.game != null) {
            NetStaticData.startNet.each(StartNet::stop);
            Threads.removeScheduledFutureData("GamePing");
            Threads.removeScheduledFutureData("GameTeam");
            Data.game = null;
            Data.playerGroup.clear();
            Data.playerAll.clear();
            Log.clog("Server closed");
        }
    }

    public static void reLoadServer(int gid) {
        if (Threads.getIfScheduledFutureData("GameTask"+gid)) {
            Threads.removeScheduledFutureData("GameTask"+gid);
        }
//        if (Threads.getIfScheduledFutureData("GamePing"+gid)) {
//            Threads.removeScheduledFutureData("GamePing");
//        }
//        if (Threads.getIfScheduledFutureData("GameWinOrLoseCheck")) {
//            Threads.removeScheduledFutureData("GameWinOrLoseCheck");
//        }
//        if (Threads.getIfScheduledFutureData("Gameover"+gid)) {
//            Threads.removeScheduledFutureData("Gameover"+gid);
//        }
        Call.killPlayers(gid);
        GroupGame.removePlayer(Data.playerAll,gid);
        GroupGame.removePlayer(Data.playerGroup,gid);
        GroupGame.games.get(gid).re();
        Threads.newThreadService2(Call::sendPlayerPing,0,2, TimeUnit.SECONDS,"GamePing");
//        FileUtil fileUtil = FileUtil.getFolder(Data.Plugin_Log_Path).toFile("Log.txt");
//        fileUtil.writeFile(Log.getLogCache(), fileUtil.getFile().length() <= 1024 * 1024);
        Data.game = new Rules(Data.config);
        Data.game.init();
        Log.clog("Server Gameover completed");
    }
}
