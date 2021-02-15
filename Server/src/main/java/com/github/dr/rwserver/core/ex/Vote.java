package com.github.dr.rwserver.core.ex;

import com.github.dr.rwserver.core.Call;
import com.github.dr.rwserver.data.Player;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.game.EventType;
import com.github.dr.rwserver.util.Events;
import com.github.dr.rwserver.util.log.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.github.dr.rwserver.util.IsUtil.isBlank;

/**
 * @author Dr
 * @Date 2020:?
 */
public class Vote {
    private final Player player;
    private Player target;
    private final String type;
    private final String name;
    private int require;
    private int reciprocal;
    private ScheduledFuture voteTime;
    private ScheduledFuture countDown;
    private Runnable endYesMsg;
    private Runnable endNoMsg;
    private Runnable teamVoteIng;
    private boolean isTeam = false;
    private final List<String> playerList = new ArrayList<>();
    private int y = 0;

    public Vote(Player player, String type, String name){
        this.player = player;
        this.type = type.toLowerCase();
        this.name = name;
        preprocessing();
    }

    public Vote(Player player, String type){
        this.player = player;
        this.type = type.toLowerCase();
        this.name = null;
        preprocessing();
    }


    public void toVote(Player playerplayer, String playerpick) {
        if (playerList.contains(playerplayer.uuid)) {
            playerplayer.sendSystemMessage(playerplayer.localeUtil.getinput("vote.rey"));
            return;
        }
        final String accapt = "y";
        final String noAccapt = "n";
        if (accapt.equals(playerpick)) {
            if (isTeam) {
                if (playerplayer.team == player.team) {
                    y++;
                    playerList.add(playerplayer.uuid);
                    playerplayer.sendSystemMessage(playerplayer.localeUtil.getinput("vote.y"));
                } else {
                    playerplayer.sendSystemMessage(playerplayer.localeUtil.getinput("vote.team"));
                }
            } else {
                y++;
                playerList.add(playerplayer.uuid);
                playerplayer.sendSystemMessage(playerplayer.localeUtil.getinput("vote.y"));
            }
        } else if (noAccapt.equals(playerpick)) {
            if (isTeam) {
                if (playerplayer.team == player.team) {
                    playerList.add(playerplayer.uuid);
                    playerplayer.sendSystemMessage(playerplayer.localeUtil.getinput("vote.n"));
                } else {
                    playerplayer.sendSystemMessage(playerplayer.localeUtil.getinput("vote.team"));
                }
            } else {
                playerList.add(playerplayer.uuid);
                playerplayer.sendSystemMessage(playerplayer.localeUtil.getinput("vote.n"));
            }
        }
    }


    private void preprocessing() {
        // 预处理
        switch(type){
            case "gameover" :
                normalDistribution();
                break;
            case "surrender" :
                isTeam = true;
                teamOnly();
                break;
            case "kick" :
                target = Data.game.playerData[Integer.parseInt(name)];
                if(target == null) {
                    player.sendSystemMessage(player.localeUtil.getinput("vote.kick.err",name));
                } else {
                    if (target.isAdmin) {
                        player.sendSystemMessage(player.localeUtil.getinput("vote.err.admin",name));
                        clearUp();
                    } else {
                        normalDistribution();
                    }
                }
                break;
            default :
                player.sendSystemMessage(player.localeUtil.getinput("vote.end.err",type+" "+(isBlank(name)?"":name)));
                clearUp();
                break;
        }
    }

    /**
     * 正常投票
      */
    private void normalDistribution() {
        require = Data.playerGroup.size();
        endNoMsg = () -> Call.sendSystemMessageLocal("vote.done.no",type+" "+(isBlank(name)?"":name), y, require);
        endYesMsg = () -> Call.sendSystemMessageLocal("vote.ok");
        teamVoteIng = () -> Call.sendSystemMessage("vote.ing",reciprocal);
        start(() -> Call.sendSystemMessage("vote.start",player.name,type+" "+(isBlank(name)?"":name)));
    }

    /**
     * 团队投票
     */
    private void teamOnly() {
        Data.playerGroup.eachs(e -> e.team == player.team,p -> require++);
        endNoMsg = () -> Call.sendSystemTeamMessageLocal(player.team, "vote.done.no", type + " " + (isBlank(name) ? "" : name), y, require);
        endYesMsg = () -> Call.sendSystemTeamMessageLocal(player.team, "vote.ok");
        teamVoteIng = () -> Call.sendSystemTeamMessageLocal(player.team,"vote.ing",reciprocal);
        start(() -> Call.sendSystemTeamMessageLocal(player.team,"vote.start",player.name,type+" "+(isBlank(name)?"":name)));
    }


    private void start(Runnable run){
        final int temp = require;
        if(temp == 1){
            player.sendSystemMessage("vote.no1");
            require = 1;
        } else if(temp <= 3) {
            require = 2;
        } else {
            require = (int) Math.ceil((double) temp / 2);
        }
        if (isTeam) {
            reciprocal = 60;
            countDown = Threads.newThreadService2(() -> {
                        reciprocal = reciprocal-10;
                        teamVoteIng.run();
            },10,10, TimeUnit.SECONDS);
        }

        voteTime=Threads.newThreadService(() -> {
            if (countDown != null) {
                countDown.cancel(true);
            }
            end();
        },58,TimeUnit.SECONDS);
        playerList.add(player.uuid);
        y++;
        if (playerList.size() >= require) {
            forceEnd();
        } else {
            run.run();
        }
    }


    private void end() {
        if (playerList.size() >= require) {
            this.endYesMsg.run();
            switch(type){
                case "kick" :
                    kick();
                    break;
                case "gameover" :
                    gameover();
                    break;
                case "surrender" :
                    surrender();
                    break;
                default:
                    break;
            }
        } else {
            endNoMsg.run();
        }
        clearUp();
    }

    /**
     * 清理引用
     */
    private void clearUp() {
        playerList.clear();
        target = null;
        isTeam = false;
        endNoMsg = null;
        endYesMsg = null;
        teamVoteIng = null;
        countDown=null;
        voteTime=null;
        Data.Vote = null;
        System.gc();
    }

    private void kick() {
        Call.sendSystemMessage("kick.player", target.name);
        try {
            target.con.sendKick(target.localeUtil.getinput("kick.you"));
        } catch (IOException e) {
            Log.error("[Player] Send Kick Player Error",e);
        }
    }

    private void gameover() {
        Events.fire(new EventType.GameOverEvent());
    }

    private void surrender() {
        Data.playerGroup.eachs(e -> e.team == player.team,p -> p.con.sendSurrender());
    }


    private void inspectEnd() {
        if (playerList.size() >= require) {
            if (countDown != null) {
                countDown.cancel(true);
            }
            voteTime.cancel(true);
            end();
        }
    }


    private void forceEnd() {
        if (countDown != null) {
            countDown.cancel(true);
        }
        voteTime.cancel(true);
        end();
    }
}
