package com.github.dr.rwserver.game;

import com.github.dr.rwserver.data.Player;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.net.AbstractNetConnect;
import com.github.dr.rwserver.net.Administration;
import com.github.dr.rwserver.net.Net;
import com.github.dr.rwserver.util.file.FileUtil;
import com.github.dr.rwserver.util.file.LoadConfig;
import com.github.dr.rwserver.util.log.Log;

import java.io.File;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledFuture;

/**
 * @author Dr
 */
public class Rules {
    /** Port */
    public int port = 5123;
    /** 支持版本 */
    public final int version;
    public final AbstractNetConnect connectNet;
    /** 是否已启动游戏 */
    public boolean isStartGame = false;
    /** 倍数 */
    public float income = 1f;
    /** 初始钱 */
    public int credits = 0;
    /** 最大玩家 */
    public int maxPlayer;
    /** Map Data */
    public final GameMaps maps = new GameMaps();
    /** nukes */
    public boolean noNukes = false;
    /** 初始单位 */
    public int initUnit = 1;
    /** 迷雾 */
    public int mist = 2;
    /** 共享控制 */
    public boolean sharedControl = false;
    /** Passwd */
    public String passwd = "";
    /** Cache */
    public LinkedBlockingQueue<GameCommand> gameCommandCache = new LinkedBlockingQueue<>();
    /** 混战分配 */
    public boolean amTeam = false;
    /** 队伍数据 */
    public Player[] playerData;
    /** */
    public Net.NetStartGame natStartGame;
    /** */
    public int maxMessageLen = 30;
    /** maxUnit */
    public final int maxUnit;
    /** AFK */
    public boolean isAfk = true;
    /** */
    public boolean oneAdmin = true;
    public final int tickTimeA;
    public final int tickTimeB;


    public ScheduledFuture afk = null;
    public ScheduledFuture gameOver = null;
    public ScheduledFuture gameTask = null;
    public ScheduledFuture ping = null;

    public Rules(LoadConfig config) {
        int port = config.readInt("port",5123);
        init(config.readInt("maxPlayer",10),port);
        passwd = config.readString("passwd","");
        if (config.readBoolean("readMap",false)) {
            List<File> list = FileUtil.File(Data.Plugin_Maps_Path).getFileList();
            for (File file : list) {
                if (file.getName().endsWith("tmx")) {
                    Data.MapsList.add(file);
                }
            }
            Log.clog(Data.localeUtil.getinput("server.load.maps"));
        }
        maxMessageLen = config.readInt("maxMessageLen",40);
        maxUnit = config.readInt("maxUnit",200);
        int tick = config.readInt("tickSpeed",0);
        Data.core.defIncome = config.readFloat("defIncome",0f);
        tickTimeA = (tick > 0) ? 100 : 200;
        tickTimeB = (tick > 0) ? 100 : 150;
        Data.core.serverName = config.readString("serverName","RW-HPS");

        Administration.NetConnectProtocolData protocol = Data.core.admin.getNetConnectProtocol();
        connectNet = protocol.protocol;
        version = protocol.version;

        oneAdmin = config.readBoolean("oneAdmin",true);
    }

    public void init(int maxPlayer,int port) {
        this.maxPlayer = maxPlayer;
        playerData = new Player[maxPlayer];
        this.port = port;
        income = Data.core.defIncome;
    }

    public void re() {
        gameCommandCache.clear();
        playerData = null;
        afk = null;
        gameOver = null;
        gameTask = null;
        playerData = new Player[maxPlayer];
        income = Data.core.defIncome;
        //Core.upLog();
        System.gc();
    }
}
