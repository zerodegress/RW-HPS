package com.github.dr.rwserver.core;

import com.github.dr.rwserver.core.ex.Threads;
import com.github.dr.rwserver.data.Player;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.game.EventType;
import com.github.dr.rwserver.game.GameCommand;
import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.util.Events;
import com.github.dr.rwserver.util.LocaleUtil;
import com.github.dr.rwserver.util.zip.gzip.GzipEncoder;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

/**
 * @author Dr
 */
public class Call {
    LocaleUtil localeUtil = Data.localeUtil;

    public static void sendMessage(Player player, String text) {
        Data.playerGroup.each(e -> e.sendMessage(player,text));
    }

    public static void sendTeamMessage(int team,Player player, String text) {
        Data.playerGroup.eachs(e -> e.team == team,p -> p.sendMessage(player,"[TEAM] "+text));
    }

    public static void sendSystemTeamMessage(int team,String text) {
        Data.playerGroup.eachs(e -> e.team == team,p -> p.sendSystemMessage("[TEAM] "+text));
    }

    public static void sendSystemMessage(String text) {
        Data.playerGroup.each(e -> e.sendSystemMessage(text));
    }

    public static void sendTeamData() {
        GzipEncoder enc = Data.game.connectNet.getTeamData();
        enc.closeGzip();
        Data.playerGroup.each(p -> p.con.sendTeamData(enc));
    }

    public static void sendPlayerPing() {
        Data.playerGroup.each(p -> Threads.newThreadPlayerHeat(() -> p.con.ping()));
    }

    public static void upDataGameData() {
        Data.playerGroup.each(p -> p.con.upServerInfo());
    }

    public static void killAllPlayer() {
        Data.playerGroup.each(p -> p.con.sendKick("Gameover"));
    }

    public static void testPreparationPlayer() {
        new Timer().schedule(new RandyTask(), 0, 100);
    }

    private static class RandyTask extends TimerTask {

        private int loadTime = 0;
        private boolean start =true;
        @Override
        public void run() {
            Data.playerGroup.each(p -> {
                if (!p.start) {
                    loadTime += 100;
                    if (loadTime > 3000) {
                        if (start) {
                            start = false;
                            Call.sendSystemMessage(Data.localeUtil.getinput("start.testNo"));
                        }
                        Data.game.gameTask = Threads.newThreadService2(new SendGameTickCommand(),0,Data.game.tickTimeB, TimeUnit.MILLISECONDS);
                        cancel();
                    }
                    return;
                }
            });
            if (start) {
                start = false;
                Call.sendSystemMessage(Data.localeUtil.getinput("start.testYes"));
            }
            Data.game.gameTask = Threads.newThreadService2(new SendGameTickCommand(),0,Data.game.tickTimeA, TimeUnit.MILLISECONDS);
            cancel();
        }
    }

    private static class SendGameTickCommand implements Runnable {
        private int time = 0;
        private boolean a = true;
        private boolean b = true;
        @Override
        public void run() {
            time += 10;
            if (Data.playerGroup.size() == 0) {
                Events.fire(new EventType.GameOverEvent());
                return;
            }
            if (Data.playerGroup.size() <= 1) {
                if (a) {
                    a = false;
                    Call.sendSystemMessage(Data.localeUtil.getinput("gameOver.oneMin"));
                }
                if (b) {
                    b = false;
                    Data.game.gameOver = Threads.newThreadService(() -> {
                        Events.fire(new EventType.GameOverEvent());
                    },1, TimeUnit.MINUTES);
                }
            } else {
                if (Data.game.gameOver != null) {
                    a = true;
                }
            }
            if (Data.game.gameCommandCache.size() == 0) {
                Threads.newThreadPlayer1(() -> Data.playerGroup.each(p -> p.con.sendTick(time)));
            } else {
                int size = Data.game.gameCommandCache.size();
                Seq<GameCommand> comm = new Seq<GameCommand>(size);
                for (int i=0;i<size;i++) {
                    comm.add(Data.game.gameCommandCache.poll());
                }
                Threads.newThreadPlayer1(() -> Data.playerGroup.each(p -> p.con.sendGameTickCommands(time,comm)));
            }
        }
    }
}
