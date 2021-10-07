/*
 * Copyright 2020-2021 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.command;

/**
 * @author Dr
 */
public class BetaGame {
//    protected static void CheckGameWin() {
//        // BETA
//        Threads.newThreadService2(() -> {
//            // 获取当前时间
//            final long time = System.currentTimeMillis();
//            // 获取判断时间
//            final int time2 = Data.game.winOrLoseTime >> 2;
//            IntSet intSet = new IntSet(16);
//
//            // 玩家是否死亡
//            Data.playerGroup.eachBooleanIfs(d -> !d.dead,e -> {
//                // 玩家距离最后一次移动单位的时间
//                long breakTime = time - e.lastMoveTime;
//                // 超过判定时间是警告一次 ; 达到判定的两倍即发送自杀包
//                if (breakTime > Data.game.winOrLoseTime) {
//                    // 直接让玩家自杀
//                    e.con.sendSurrender();
//                } else if (breakTime > time2) {
//                    // 给出提示
//                    e.sendSystemMessage(e.localeUtil.getinput("winOrLose.time"));
//                }
//                intSet.add(e.team);
//            });
//            if (intSet.size <= 1) {
//                final int winTeam = intSet.iterator().next();
//                Data.playerGroup.eachBooleanIfs(p -> (p.team == winTeam), c -> Log.info(c.name));
//                Events.fire(new EventType.GameOverEvent());
//            }
//        }, 10, 10, TimeUnit.SECONDS,".");
//    }
}
