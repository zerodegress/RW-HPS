package com.github.dr.rwserver.core;

import com.github.dr.rwserver.core.ex.Threads;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.util.encryption.Md5;
import com.github.dr.rwserver.util.log.Log;

import java.util.concurrent.TimeUnit;

import static com.github.dr.rwserver.net.HttpRequest.doPostRw;
import static com.github.dr.rwserver.util.RandomUtil.generateStr;

/**
 * @author Dr
 */
public class NetServer {


    public static void closeServer() {
        if (Data.game != null) {
            if (Data.game.natStartGame != null) {
                Data.serverChannel.close();
                Data.game.natStartGame = null;
            }
            Data.game = null;
            Data.playerGroup.clear();
            Data.playerAll.clear();
            Log.clog("Server closed");
        }
    }

    public static void reLoadServer() {
        if (Data.game.gameTask != null) {
            Data.game.gameTask.cancel(true);
            Data.game.gameTask = null;
        }
        if (Data.game.gameOver != null) {
            Data.game.gameOver.cancel(true);
            Data.game.gameOver = null;
        }
        Call.killAllPlayer();
        Data.playerGroup.clear();
        Data.playerAll.clear();
        Data.game.re();
        Data.game.ping = Threads.newThreadService2(() -> {
            Call.sendPlayerPing();
        },0,3000, TimeUnit.MILLISECONDS);
        Data.game.isStartGame = false;
        Log.clog("Server Gameover completed");
    }

    public static void addServerList() {
        Data.core.serverToken = generateStr(40);
        StringBuffer sb = new StringBuffer();
        sb.append("action=add")
            .append("&user_id=u_"+Data.core.serverConnectUuid)
            .append("&game_name=RW-HPS")
            .append("&game_version=136")
            .append("&game_version_string=1.13.3")
            .append("&private_token=").append(Data.core.serverToken)
            .append("&private_token_2=").append(Md5.md5Formant(Md5.md5Formant(Data.core.serverToken)))
            .append("&confirm=").append(Md5.md5Formant("a"+Md5.md5Formant(Data.core.serverToken)))
            .append("&password_required=").append(!"".equals(Data.game.passwd))
            .append("&created_by=").append(Data.core.serverName)
            .append("&private_ip=10.0.0.1")
            .append("&port_number=").append(Data.game.port)
            .append("&game_map=").append(Data.game.mapName)
            .append("&game_mode=skirmishMap")
            .append("&game_status=battleroom")
            .append("&player_count=").append(Data.playerGroup.size())
            .append("&max_player_count=").append(Data.game.maxPlayer);
        boolean S1 = doPostRw("http://gs1.corrodinggames.com/masterserver/1.3/interface",sb.toString()).contains(Data.core.serverConnectUuid);
        boolean S4 = doPostRw("http://gs4.corrodinggames.net/masterserver/1.3/interface",sb.toString()).contains(Data.core.serverConnectUuid);
        boolean O1 = doPostRw("http://gs1.corrodinggames.com/masterserver/1.3/interface","action=self_info&port="+Data.game.port).contains("true");
        doPostRw("http://gs1.corrodinggames.com/masterserver/1.3/interface","action=self_info&port="+Data.game.port).contains("true");
        boolean O4 = doPostRw("http://gs4.corrodinggames.net/masterserver/1.3/interface","action=self_info&port="+Data.game.port).contains("true");
        doPostRw("http://gs4.corrodinggames.net/masterserver/1.3/interface","action=self_info&port="+Data.game.port).contains("true");

        if (S1 || S4) {
            if (S1 && S4) {
                Log.clog(Data.localeUtil.getinput("err.yesList"));
            } else {
                Log.clog(Data.localeUtil.getinput("err.ynList"));
            }
        } else {
            Log.clog(Data.localeUtil.getinput("err.noList"));
        }

        if (O1 || O4) {
            Log.clog(Data.localeUtil.getinput("err.yesOpen"));
        } else {
            Log.clog(Data.localeUtil.getinput("err.noOpen"));
        }
    }

    public static void upServerList() {
        String stat = "battleroom";
        if (Data.game.isStartGame) {
            stat = "ingame";
        }
        StringBuffer sb = new StringBuffer();
        sb.append("action=update")
                .append("&user_id=u_" + Data.core.serverConnectUuid)
                .append("&game_name=RW-HPS")
                .append("&game_version=136")
                .append("&game_version_string=1.13.3")
                .append("&private_token=").append(Data.core.serverToken)
                .append("&private_token_2=").append(Md5.md5Formant(Md5.md5Formant(Data.core.serverToken)))
                .append("&confirm=").append(Md5.md5Formant("a" + Md5.md5Formant(Data.core.serverToken)))
                .append("&password_required=").append("".equals(Data.game.passwd))
                .append("&created_by=").append(Data.core.serverName)
                .append("&private_ip=127.0.0.1")
                .append("&port_number=").append(Data.game.port)
                .append("&game_map=").append(Data.game.mapName)
                .append("&game_mode=skirmishMap")
                .append("&game_status=").append(stat)
                .append("&player_count=").append(Data.playerGroup.size())
                .append("&max_player_count=").append(Data.game.maxPlayer);
        doPostRw("http://gs1.corrodinggames.com/masterserver/1.3/interface", sb.toString());
        doPostRw("http://gs4.corrodinggames.net/masterserver/1.3/interface", sb.toString());
    }
}
