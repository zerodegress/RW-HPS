/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.core;

import net.rwhps.server.core.thread.CallTimeTask;
import net.rwhps.server.core.thread.Threads;
import net.rwhps.server.core.thread.TimeTaskData;
import net.rwhps.server.data.HessModuleManage;
import net.rwhps.server.data.global.Data;
import net.rwhps.server.data.global.NetStaticData;
import net.rwhps.server.func.StrCons;
import net.rwhps.server.net.NetService;
import net.rwhps.server.net.core.IRwHps;
import net.rwhps.server.util.log.Log;
import org.jetbrains.annotations.NotNull;

/**
 * @author RW-HPS/Dr
 */
@SuppressWarnings("deprecation")
public class NetServer {

    static String userId;

    public static void closeServer() {
        if (Data.INSTANCE.getGame() != null) {
            Data.INSTANCE.setExitFlag(true);

            TimeTaskData.INSTANCE.stopCallTickTask();

            Call.killAllPlayer("Server Close");

            NetStaticData.netService.eachAll(NetService::stop);
            NetStaticData.netService.clear();
            NetStaticData.INSTANCE.setServerNetType(IRwHps.NetType.NullProtocol);
            Threads.closeNet();
            //Threads.newThreadCoreNet();

            if (NetStaticData.INSTANCE.getServerNetType() == IRwHps.NetType.ServerProtocol) {
                HessModuleManage.INSTANCE.getHps().getGameHessData().clean();
            }

            Threads.closeTimeTask(CallTimeTask.AutoCheckTask);
            Threads.closeTimeTask(CallTimeTask.CallPingTask);
            Threads.closeTimeTask(CallTimeTask.CallTeamTask);
            Threads.closeTimeTask(CallTimeTask.PlayerAfkTask);
            Threads.closeTimeTask(CallTimeTask.GameOverTask);
            Threads.closeTimeTask(CallTimeTask.AutoStartTask);
            Threads.closeTimeTask(CallTimeTask.AutoUpdateMapsTask);

            Data.SERVER_COMMAND.handleMessage("uplist remove", new StrCons() {
                @Override
                public void get(@NotNull String t) {
                    Log.clog(t);
                }
            });

            Data.INSTANCE.getGame().getPlayerManage().playerGroup.clear();
            Data.INSTANCE.getGame().getPlayerManage().playerAll.clear();

            System.gc();
            Log.clog("Server closed");
        }
    }

    public static void reLoadServer() {
        if (Data.vote!= null) {
            Data.vote.stopVote();
        }
        TimeTaskData.INSTANCE.stopCallTickTask();
        Threads.closeTimeTask(CallTimeTask.GameOverTask);
        Threads.closeTimeTask(CallTimeTask.AutoCheckTask);


        Call.killAllPlayer();
        Data.INSTANCE.getGame().re();
        Data.INSTANCE.getGame().setStartGame(false);

        synchronized (net.udp.Data.waitData) {
            net.udp.Data.waitData.notify();
        }

        Log.clog("[Server Gameover completed]");
    }
}
