package com.github.dr.rwserver.game;

import com.github.dr.rwserver.data.Player;
import com.github.dr.rwserver.data.global.Data;

import static com.github.dr.rwserver.util.IsUtil.isTwoTimes;

/**
 * @author Dr
 */
public class Team {
    public static void autoPlayerTeam(Player player) {
        // 管他的 先把值赋了 避免并发下OVER
        if (Data.game.amTeam) {
            for (int i=0,len=Data.game.maxPlayer;i<len;i++) {
                if (Data.game.playerData[i] == null) {
                    Data.game.playerData[i] = player;
                    player.site=i;
                    player.team=i;
                    return;
                }
            }
        } else {
            //如果按正常的来 那么需要For+For嵌套 Time：O(N*2)
            //使用数组 不存在即为null Time： O(1)
            for (int i=0,len=Data.game.maxPlayer;i<len;i++) {
                if (Data.game.playerData[i] == null) {
                    Data.game.playerData[i] = player;
                    player.site=i;
                    player.team=isTwoTimes((i+1)) ? 1 : 0;
                    return;
                }
            }
        }
    }
}
