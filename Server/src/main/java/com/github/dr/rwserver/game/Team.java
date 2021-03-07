package com.github.dr.rwserver.game;

import com.github.dr.rwserver.data.Player;
import com.github.dr.rwserver.data.global.Data;

import static com.github.dr.rwserver.util.IsUtil.isTwoTimes;

/**
 * @author Dr
 */
public class Team {
    public static void autoPlayerTeam(Player player) {
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

    public static void amYesPlayerTeam() {
        for (int i=0,len=Data.game.maxPlayer;i<len;i++) {
            if (Data.game.playerData[i] != null) {
                Data.game.playerData[i].team=i;
            }
        }
    }

    public static void amNoPlayerTeam() {
        for (int i=0,len=Data.game.maxPlayer;i<len;i++) {
            if (Data.game.playerData[i] != null) {
                Data.game.playerData[i].team=isTwoTimes((i+1)) ? 1 : 0;
            }
        }
    }
}
