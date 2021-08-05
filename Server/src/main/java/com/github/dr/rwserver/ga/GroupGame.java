package com.github.dr.rwserver.ga;

import com.github.dr.rwserver.data.Player;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.game.EventType;
import com.github.dr.rwserver.game.Rules;
import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.util.game.Events;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GroupGame {

    public static final String GID="gid";

    public static int currId=0;

    public static Map<Integer, Rules> games=new ConcurrentHashMap<>();
    public static List<Player> prePlayers(){
        return playersByGid(Data.playerAll,currId);
    }
    private static List<Player> playersByGid(Seq<Player> players,int gid){
        List<Player> sPlayers = new ArrayList<>();
        players.eachBooleanIfs(x->x.groupId==gid,sPlayers::add);
        return sPlayers;
    }
    public static synchronized void incrId(){currId++;};

    public static void gameOverCheck(int gid){
        if(games.get(gid).isStartGame&&playersByGid(Data.playerGroup,gid).isEmpty()){
            Events.fire(new EventType.GameOverEvent(gid));
        }
    }
}
