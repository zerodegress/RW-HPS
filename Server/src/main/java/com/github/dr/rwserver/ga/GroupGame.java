package com.github.dr.rwserver.ga;

import com.github.dr.rwserver.data.Player;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.game.EventType;
import com.github.dr.rwserver.game.Rules;
import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.util.game.Events;
import io.netty.util.AttributeKey;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

public class GroupGame {

    public static final AttributeKey<Integer> G_KEY=AttributeKey.valueOf("gid");
    public static final List<ReentrantLock> MOVE_LOCK=new ArrayList<>();
    public static List<String> blackUUIDList=new ArrayList<>();
    private static int currId=0;

    public static Map<Integer, Rules> games=new ConcurrentHashMap<>();
    public static List<Player> prePlayers(){
        return playersByGid(Data.playerAll,currId);
    }
    public static List<Player> playersByGid(Seq<Player> players,int gid){
        List<Player> sPlayers = new ArrayList<>();
        players.eachBooleanIfs(x->x.groupId==gid,sPlayers::add);
        return sPlayers;
    }
    public static List<Player> playerGroup(int gid){
        return playersByGid(Data.playerGroup,gid);
    }
    public static List<Player> allPlayer(int gid){
        return playersByGid(Data.playerAll,gid);
    }

    public static void gameOverCheck(int gid){
        if(games.get(gid).isStartGame&&playersByGid(Data.playerGroup,gid).isEmpty()){
            Events.fire(new EventType.GameOverEvent(gid));
        }
    }

    public static void removePlayer(Seq<Player> players,int gid){
        Iterator<Player> iterator = players.iterator();
        while (iterator.hasNext()){
            if(iterator.next().groupId==gid) iterator.remove();
        }
    }
    public static Rules gU(int gid){
        return games.get(gid);
    }

    public synchronized static int newPlayerGroupId(){
        Iterator<Integer> iterator = games.keySet().iterator();
        while (iterator.hasNext()){
            Integer gid = iterator.next();
            Rules rules = games.get(gid);
            if(!rules.isStartGame&&playerGroup(gid).size()<rules.maxPlayer) return gid;
        }
        Rules rules = new Rules(Data.config);
        rules.init();
        MOVE_LOCK.add(new ReentrantLock(true));
        games.put(++currId,rules);
        return currId;
    }
}
