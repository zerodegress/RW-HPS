package com.github.dr.rwserver.game;

import com.github.dr.rwserver.data.Player;
import com.github.dr.rwserver.ga.GroupGame;

import static com.github.dr.rwserver.util.IsUtil.isTwoTimes;

/**
 * @author Dr
 */
public class Team {
    public static void autoPlayerTeam(Player player) {
        if (GroupGame.gU(player.groupId).amTeam) {
            for (int i=0,len=GroupGame.gU(player.groupId).maxPlayer;i<len;i++) {
                if (GroupGame.gU(player.groupId).playerData[i] == null) {
                    GroupGame.gU(player.groupId).playerData[i] = player;
                    player.site=i;
                    player.team=i;
                    return;
                }
            }
        } else {
            for (int i=0,len=GroupGame.gU(player.groupId).maxPlayer;i<len;i++) {
                if (GroupGame.gU(player.groupId).playerData[i] == null) {
                    GroupGame.gU(player.groupId).playerData[i] = player;
                    player.site=i;
                    player.team=isTwoTimes((i+1)) ? 1 : 0;
                    return;
                }
            }
        }
    }

    public static void amYesPlayerTeam(int gid) {
        for (int i=0,len=GroupGame.gU(gid).maxPlayer;i<len;i++) {
            if (GroupGame.gU(gid).playerData[i] != null) {
                GroupGame.gU(gid).playerData[i].team=i;
            }
        }
    }

    public static void amNoPlayerTeam(int gid) {
        for (int i=0,len=GroupGame.gU(gid).maxPlayer;i<len;i++) {
            if (GroupGame.gU(gid).playerData[i] != null) {
                GroupGame.gU(gid).playerData[i].team=isTwoTimes((i+1)) ? 1 : 0;
            }
        }
    }
}
