package com.github.dr.rwserver.command;

import com.github.dr.rwserver.core.thread.Threads;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.game.EventType;
import com.github.dr.rwserver.struct.IntSet;
import com.github.dr.rwserver.util.game.Events;
import com.github.dr.rwserver.util.log.Log;

import java.util.concurrent.TimeUnit;

/**
 * @author Dr
 */
public class BetaGame {
    protected static void CheckGameWin() {
        Threads.newThreadService2(() -> {
            final long time = System.currentTimeMillis();
            final int time2 = Data.game.winOrLoseTime >> 2;
            IntSet intSet = new IntSet(16);
            Data.playerGroup.eachBooleanIfs(d -> !d.dead,e -> {
                    long breakTime = time - e.lastMoveTime;
                    if (breakTime > Data.game.winOrLoseTime) {
                        e.con.sendSurrender();
                    } else if (breakTime > time2) {
                        e.sendSystemMessage(e.localeUtil.getinput("winOrLose.time"));
                    }
                    intSet.add(e.team);
            });
            if (intSet.size <= 1) {
                final int winTeam = intSet.iterator().next();
                Data.playerGroup.eachBooleanIfs(p -> (p.team == winTeam), c -> Log.info(c.name));
                Events.fire(new EventType.GameOverEvent());
            }
        }, 10, 10, TimeUnit.SECONDS,".");
    }
}
