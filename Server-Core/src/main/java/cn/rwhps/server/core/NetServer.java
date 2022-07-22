/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.core;

import cn.rwhps.server.core.thread.CallTimeTask;
import cn.rwhps.server.core.thread.Threads;
import cn.rwhps.server.core.thread.TimeTaskData;
import cn.rwhps.server.data.global.Data;
import cn.rwhps.server.data.global.NetStaticData;
import cn.rwhps.server.func.StrCons;
import cn.rwhps.server.net.StartNet;
import cn.rwhps.server.util.RandomUtil;
import cn.rwhps.server.util.StringFilteringUtil;
import cn.rwhps.server.util.Time;
import cn.rwhps.server.util.encryption.Md5;
import cn.rwhps.server.util.encryption.Sha;
import cn.rwhps.server.util.file.FileUtil;
import cn.rwhps.server.util.log.Log;

import java.math.BigInteger;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static cn.rwhps.server.net.HttpRequestOkHttp.doPostRw;

/**
 * @author RW-HPS/Dr
 */
public class NetServer {

    static String userId;

    public static void closeServer() {
        if (Data.game != null) {
            TimeTaskData.INSTANCE.stopCallTickTask();
            Call.disAllPlayer();
            NetStaticData.startNet.each(StartNet::stop);
            NetStaticData.startNet.clear();
            Threads.closeNet();
            //Threads.newThreadCoreNet();


            Threads.closeTimeTask(CallTimeTask.CallPingTask);
            Threads.closeTimeTask(CallTimeTask.CallTeamTask);
            Threads.closeTimeTask(CallTimeTask.PlayerAfkTask);
            Threads.closeTimeTask(CallTimeTask.GameOverTask);
            Threads.closeTimeTask(CallTimeTask.AutoStartTask);
            Threads.closeTimeTask(CallTimeTask.AutoUpdateMapsTask);

            Data.SERVER_COMMAND.handleMessage("uplist remove",  (StrCons) Log::clog);

            Data.game.getPlayerManage().playerGroup.clear();
            Data.game.getPlayerManage().playerAll.clear();
            Data.game = null;
            System.gc();
            Log.clog("Server closed");
        }
    }

    public static void reLoadServer() {
        if (Data.vote!= null) {
            Data.vote.stopVote();
        }
        Call.killAllPlayer();
        Data.game.re();
        Data.game.setStartGame(false);

        synchronized (net.udp.Data.waitData) {
            net.udp.Data.waitData.notify();
        }

        FileUtil fileUtil = FileUtil.getFolder(Data.Plugin_Log_Path).toFile("Log.txt");
        fileUtil.writeFile(Log.getLogCache(), fileUtil.getFile().length() <= 1024 * 1024);

        Log.clog("[Server Gameover completed]");
    }
}
