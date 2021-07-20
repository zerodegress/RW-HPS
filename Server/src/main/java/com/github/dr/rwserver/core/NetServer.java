package com.github.dr.rwserver.core;

import com.github.dr.rwserver.core.thread.Threads;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.data.global.NetStaticData;
import com.github.dr.rwserver.data.json.Json;
import com.github.dr.rwserver.net.game.StartNet;
import com.github.dr.rwserver.util.Time;
import com.github.dr.rwserver.util.encryption.Md5;
import com.github.dr.rwserver.util.encryption.Sha;
import com.github.dr.rwserver.util.file.FileUtil;
import com.github.dr.rwserver.util.log.Log;

import java.math.BigInteger;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.github.dr.rwserver.net.HttpRequestOkHttp.doPost;
import static com.github.dr.rwserver.net.HttpRequestOkHttp.doPostRw;
import static com.github.dr.rwserver.util.RandomUtil.generateStr;
import static com.github.dr.rwserver.util.StringFilteringUtil.cutting;

/**
 * @author Dr
 */
public class NetServer {

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

    public static void reLoadServer() {
        if (Threads.getIfScheduledFutureData("GameTask")) {
            Threads.removeScheduledFutureData("GameTask");
        }
        if (Threads.getIfScheduledFutureData("GamePing")) {
            Threads.removeScheduledFutureData("GamePing");
        }
        if (Threads.getIfScheduledFutureData("GameWinOrLoseCheck")) {
            Threads.removeScheduledFutureData("GameWinOrLoseCheck");
        }
        Call.killAllPlayer();
        Data.playerGroup.clear();
        Data.playerAll.clear();
        Data.game.re();
        Threads.newThreadService2(Call::sendPlayerPing,0,2, TimeUnit.SECONDS,"GamePing");
        Data.game.isStartGame = false;
        FileUtil fileUtil = FileUtil.file(Data.Plugin_Log_Path).toPath("Log.txt");
        fileUtil.writeFile(Log.getLogCache(), fileUtil.getFile().length() <= 1024 * 1024);

        Log.clog("Server Gameover completed");
    }
}
