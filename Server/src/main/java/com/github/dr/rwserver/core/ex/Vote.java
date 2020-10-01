package com.github.dr.rwserver.core.ex;

import com.github.dr.rwserver.core.Call;
import com.github.dr.rwserver.data.Player;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.game.EventType;
import com.github.dr.rwserver.util.Events;
import com.github.dr.rwserver.util.LocaleUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.github.dr.rwserver.util.IsUtil.isBlank;

/**
 * @author Dr
 * @Date ?
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
    private boolean isteam = false;
    private final List<String> playerList = new ArrayList<String>();
    private int y = 0;
    private final LocaleUtil localeUtil = Data.localeUtil;

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
            playerplayer.sendSystemMessage(localeUtil.getinput("vote.rey"));
            return;
        }
        if ("y".equals(playerpick)) {
            if (isteam) {
                if (playerplayer.site == player.site) {
                    y++;
                    playerList.add(playerplayer.uuid);
                    playerplayer.sendSystemMessage(localeUtil.getinput("vote.y"));
                } else {
                    playerplayer.sendSystemMessage(localeUtil.getinput("vote.team"));
                }
            } else {
                y++;
                playerList.add(playerplayer.uuid);
                playerplayer.sendSystemMessage(localeUtil.getinput("vote.y"));
            }
        } else if ("n".equals(playerpick)) {

        }
    }


    private void preprocessing() {
        // 预处理
        switch(type){
            case "gameover" :
                normalDistribution();
                break;
            case "surrender" :
                isteam = true;
                teamOnly();
                break;
            case "kick" :
                target = Data.game.playerData[Integer.parseInt(name)];
                if(target == null) {
                    player.sendSystemMessage(localeUtil.getinput("vote.kick.err",name));
                } else {
                    if (target.isAdmin) {
                        player.sendSystemMessage(localeUtil.getinput("vote.err.admin",name));
                    } else {
                        normalDistribution();
                    }
                }
                break;
            default :
                player.sendSystemMessage(localeUtil.getinput("vote.end.err",type+" "+(isBlank(name)?"":name)));
                Data.Vote = null;
                System.gc();
                break;
        }
    }

    // 正常投票
    private void normalDistribution() {
        require = Data.playerGroup.size();
        endNoMsg = () -> Call.sendSystemMessage(localeUtil.getinput("vote.done.no",type+" "+(isBlank(name)?"":name), y, require));
        endYesMsg = () -> Call.sendSystemMessage(localeUtil.getinput("vote.ok"));
        start(() -> Call.sendSystemMessage(localeUtil.getinput("vote.start",player.name,type+" "+(isBlank(name)?"":name))));
    }

    // 团队投票
    private void teamOnly() {
        Data.playerGroup.eachs(e -> e.team == player.team,p -> require++);
        endNoMsg = () -> Call.sendSystemTeamMessage(player.team, localeUtil.getinput("vote.done.no", type + " " + (isBlank(name) ? "" : name), y, require));
        endYesMsg = () -> Call.sendSystemTeamMessage(player.team, localeUtil.getinput("vote.ok"));
        start(() -> Call.sendSystemTeamMessage(player.team,localeUtil.getinput("vote.start",player.name,type+" "+(isBlank(name)?"":name))));
    }


    private void start(Runnable run){
        final int temp = require;
        if(temp == 1){
            player.sendSystemMessage(localeUtil.getinput("vote.no1"));
            require = 1;
        } else if(temp <= 3) {
            require = 2;
        } else {
            require = (int) Math.ceil((double) temp / 2);
        }
        if (isteam) {
            reciprocal=60;
            countDown=Threads.newThreadService2(
                    new Runnable() {
                        @Override
                        public void run() {
                            reciprocal = reciprocal-10;
                            Call.sendSystemMessage(localeUtil.getinput("vote.ing",reciprocal));
                        }
                    },
            10,10, TimeUnit.SECONDS);
        }

        voteTime=Threads.newThreadService(
                new Runnable() {
                    @Override
                    public void run() {
                        if (countDown != null) {
                            countDown.cancel(true);
                        }
                        end();
                    }
                },
        58,TimeUnit.SECONDS);
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
        playerList.clear();
        isteam = false;
        // 释放内存
        endNoMsg = null;
        endYesMsg = null;
        countDown=null;
        voteTime=null;
        Data.Vote = null;
    }

    private void kick() {
        Call.sendSystemMessage(localeUtil.getinput("kick.player", target.name));
        target.con.sendKick(localeUtil.getinput("kick.you"));
    }

    private void gameover() {
        Events.fire(new EventType.GameOverEvent());
    }

    private void surrender() {
        Data.playerGroup.eachs(e -> e.team == player.team,p -> p.con.surrender());
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
