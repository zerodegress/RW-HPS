package com.github.dr.rwserver.core;

import com.github.dr.rwserver.core.ex.Threads;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.util.encryption.Sha;
import com.github.dr.rwserver.util.file.FileUtil;
import com.github.dr.rwserver.util.log.Log;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import static com.github.dr.rwserver.util.StringFilteringUtil.cutting;

/**
 * @author Dr
 */
public class NetServer {

    static String userId;

    public static void closeServer() {
        if (Data.game != null) {
            if (Data.game.natStartGame != null) {
                Data.serverChannelB.close();
                Data.serverChannelB = null;
                Data.game.ping.cancel(true);
                Data.game.team.cancel(true);
                Data.game.ping = null;
                Data.game.team = null;
                Data.game.natStartGame = null;
            }
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
        if (Data.core.upServerList) {
            NetServer.upServerList();
        }
        FileUtil fileUtil = FileUtil.File(Data.Plugin_Log_Path).toPath("Log.txt");
        fileUtil.writeFile(Log.getLogCache(), fileUtil.getFile().length() <= 1024 * 1024);

        Log.clog("Server Gameover completed");
    }

    public static String addServerList() {
        // NO 违反守则(Violation of the code)
        return "";
    }

    public static void addServerList(boolean uuid) {
        // NO 违反守则(Violation of the code)
    }
    public static void upServerList() {
        // NO 违反守则(Violation of the code)
    }

    public static void upServerListNew() {
        // NO 违反守则(Violation of the code)
    }

    public static void removeServerList() {
        // NO 违反守则(Violation of the code)
    }

    private static String reup(Sha sha,String str) {
        byte[] bytes = sha.sha256Arry(str);
        return cutting(String.format("%0" + (bytes.length * 2) + "X", new BigInteger(1, bytes)),4);
    }
}
