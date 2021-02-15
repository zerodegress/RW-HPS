package com.github.dr.rwserver.core;

import com.github.dr.rwserver.core.ex.Threads;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.util.encryption.Md5;
import com.github.dr.rwserver.util.encryption.Sha;
import com.github.dr.rwserver.util.file.FileUtil;
import com.github.dr.rwserver.util.log.Log;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

import static com.github.dr.rwserver.net.HttpRequest.doPostRw;
import static com.github.dr.rwserver.util.DateUtil.getLocalTimeFromU;
import static com.github.dr.rwserver.util.RandomUtil.generateStr;
import static com.github.dr.rwserver.util.StringFilteringUtil.cutting;

/**
 * @author Dr
 */
public class NetServer {


    public static void closeServer() {
        if (Data.game != null) {
            if (Data.game.natStartGame != null) {
                Data.serverChannelB.close();
                Data.serverChannelB = null;
                Data.game.ping.cancel(true);
                Data.game.team.cancel(true);
                Data.game.ping = null;
                Data.game.team = null;
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
        if (Data.game.ping != null) {
            Data.game.ping.cancel(true);
            Data.game.ping = null;
        }
        if (Data.game.winOrLoseCheck != null) {
            Data.game.winOrLoseCheck.cancel(true);
            Data.game.winOrLoseCheck = null;
        }
        Call.killAllPlayer();
        Data.playerGroup.clear();
        Data.playerAll.clear();
        Data.game.re();
        Data.game.ping = Threads.newThreadService2(Call::sendPlayerPing,0,2, TimeUnit.SECONDS);
        Data.game.isStartGame = false;
        if (Data.core.upServerList) {
            NetServer.upServerList();
        }
        FileUtil fileUtil = FileUtil.File(Data.Plugin_Log_Path).toPath("Log.txt");
        if (fileUtil.getFile().length() > 1024*1024) {
            fileUtil.writeFile(Log.getLogCache(),false);
        } else {
            fileUtil.writeFile(Log.getLogCache(),true);
        }

        Log.clog("Server Gameover completed");
    }

    public static void addServerList() {
        Data.core.serverToken = generateStr(40);
        final StringBuffer sb = new StringBuffer();
        final Sha sha = new Sha();
        final long time = getLocalTimeFromU();
        final String userId = "u_"+Data.core.serverConnectUuid;
        sb.append("action=add")
            .append("&user_id=").append(userId)
            .append("&game_name=RW-HPS")
            .append("&_1=").append(time)
            .append("&tx2=").append(reup(sha,"_"+userId + 5))
            .append("&tx3=").append(reup(sha,"_"+userId + (5+time)))
            .append("&game_version=151")
            .append("&game_version_string=1.14")
            .append("&game_version_beta=false")
            .append("&private_token=").append(Data.core.serverToken)
            .append("&private_token_2=").append(Md5.md5Formant(Md5.md5Formant(Data.core.serverToken)))
            .append("&confirm=").append(Md5.md5Formant("a"+Md5.md5Formant(Data.core.serverToken)))
            .append("&password_required=").append(!"".equals(Data.game.passwd))
            .append("&created_by=").append(Data.core.serverName)
            .append("&private_ip=10.0.0.1")
            .append("&port_number=").append(Data.game.port)
            .append("&game_map=").append(Data.game.maps.mapName)
            //.append("&game_map=").append(Data.game.subtitle)
            .append("&game_mode=skirmishMap")
            .append("&game_status=battleroom")
            .append("&player_count=").append("1")
            .append("&max_player_count=").append(Data.game.maxPlayer);
        boolean S1 = doPostRw("http://gs1.corrodinggames.com/masterserver/1.4/interface",sb.toString()).contains(Data.core.serverConnectUuid);
        boolean S4 = doPostRw("http://gs4.corrodinggames.net/masterserver/1.4/interface",sb.toString()).contains(Data.core.serverConnectUuid);
        boolean O1 = doPostRw("http://gs1.corrodinggames.com/masterserver/1.4/interface","action=self_info&port="+Data.game.port+"&id="+userId).contains("true");
        //doPostRw("http://gs1.corrodinggames.com/masterserver/1.4/interface","action=self_info&port="+Data.game.port).contains("true");
        boolean O4 = doPostRw("http://gs4.corrodinggames.net/masterserver/1.4/interface","action=self_info&port="+Data.game.port+"&id="+userId).contains("true");
        //doPostRw("http://gs4.corrodinggames.net/masterserver/1.4/interface","action=self_info&port="+Data.game.port).contains("true");

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
        final StringBuffer sb = new StringBuffer();
        final String userId = "u_"+Data.core.serverConnectUuid;
        sb.append("action=update")
            .append("&id=").append(userId)
            .append("&game_name=RW-HPS")
            .append("&private_token=").append(Data.core.serverToken)
            .append("&password_required=").append(!"".equals(Data.game.passwd))
            .append("&created_by=").append(Data.core.serverName)
            .append("&private_ip=127.0.0.1")
            .append("&port_number=").append(Data.game.port)
            .append("&game_map=").append(Data.game.maps.mapName)
            //.append("&game_map=").append(Data.game.subtitle)
            .append("&game_mode=skirmishMap")
            .append("&game_status=").append(stat)
            .append("&player_count=").append(Data.playerGroup.size())
            .append("&max_player_count=").append(Data.game.maxPlayer);
        doPostRw("http://gs1.corrodinggames.com/masterserver/1.4/interface", sb.toString());
        doPostRw("http://gs4.corrodinggames.net/masterserver/1.4/interface", sb.toString());
    }

    private static String reup(Sha sha,String str) {
        byte[] bytes = sha.sha256Arry(str);
        return cutting(String.format("%0" + (bytes.length * 2) + "X", new BigInteger(1, bytes)),4);
    }
}
