package com.github.dr.rwserver.plugin;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.github.dr.rwserver.core.thread.Threads;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.net.HttpRequestOkHttp;
import com.github.dr.rwserver.util.IsUtil;
import com.github.dr.rwserver.util.ReExp;
import com.github.dr.rwserver.util.game.CommandHandler;
import com.github.dr.rwserver.util.log.Log;
import okhttp3.*;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class UpList extends Plugin {
//    private boolean upServerList = false;
    public void onEnable() {
    }

    private  OkHttpClient client=new OkHttpClient();
    private String port;
    private String uid=UUID.randomUUID().toString();
    public void registerServerCommands(CommandHandler handler) {
        handler.removeCommand("upserverlist");
        port= Data.config.readString("connectPort", "5123");
        handler.register("upserverlist", "<off/on>","serverCommands.upserverlist", (arg, log) -> {
            if("on".equals(arg[0]))
                Threads.newThreadCore(this::uplist);
            else {
                Threads.removeScheduledFutureData("UPLIST");
            }
        });
    }

    public void registerClientCommands(CommandHandler handler) {
    }

    private void uplist() {
    }
}
