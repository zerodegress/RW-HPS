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

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class UpList extends Plugin {
    private boolean upServerList = false;
    public void onEnable() {
    }

    public void registerServerCommands(CommandHandler handler) {
        handler.removeCommand("upserverlist");
        handler.removeCommand("upserverlistnew");
        String token = Data.config.readString("token", "Zet2bbEDv0@MhvA>~Rz:MHX.:");
        int port = Data.config.readInt("connectPort", 5123);
        if (IsUtil.notIsBlank(token)) {
            handler.register("upserverlist", "serverCommands.upserverlist", (arg, log) -> {
                if (!this.upServerList) {
                    Threads.newThreadCore(() -> {
                        this.upServerList = true;
                        this.uplist(token, port);
                    });
                } else {
                    Log.clog("已上传 不需要再次上传");
                }

            });
        } else {
            handler.register("upserverlist", "serverCommands.upserverlist", (arg, log) -> {
                Log.clog("无Tonken");
            });
        }
    }

    public void registerClientCommands(CommandHandler handler) {
    }

    private void uplist(String tonken, int port) {
        Map<String, String> map = new HashMap<>();
        map.put("Token", tonken);
        map.put("Status", "add");
        map.put("Name", Data.core.serverName);
        map.put("Port", String.valueOf(port));
        map.put("PlayerMaxSzie", String.valueOf(Data.game.maxPlayer));
        map.put("MapName", Data.game.maps.mapName);
        map.put("StartGame", String.valueOf(Data.game.isStartGame));
        map.put("PlayerSize", String.valueOf(Data.playerGroup.size()));
        String result = HttpRequestOkHttp.doPostJson("https://api.der.kim:41567/api/post/uplist", JSONObject.toJSONString(map, new SerializerFeature[]{SerializerFeature.PrettyFormat}));
        JSONObject jsonObject = JSONObject.parseObject(result);
        HttpRequestOkHttp.doPostRw("http://gs1.corrodinggames.com/masterserver/1.4/interface", jsonObject.getString("add"));
        HttpRequestOkHttp.doPostRw("http://gs4.corrodinggames.net/masterserver/1.4/interface", jsonObject.getString("add"));
        boolean O1 = HttpRequestOkHttp.doPostRw("http://gs1.corrodinggames.com/masterserver/1.4/interface", jsonObject.getString("open")).contains("true");
        boolean O4 = HttpRequestOkHttp.doPostRw("http://gs4.corrodinggames.net/masterserver/1.4/interface", jsonObject.getString("open")).contains("true");
        if (!O1 && !O4) {
            Log.clog(Data.localeUtil.getinput("err.noOpen", new Object[0]));
        } else {
            Log.clog(Data.localeUtil.getinput("err.yesOpen", new Object[0]));
        }

        Threads.newThreadService2(() -> {
            Object pingdata = (new ReExp() {
                protected Object runs() throws Exception {
                    Map<String, String> map0 = new HashMap<>();
                    map0.put("Token", tonken);
                    map0.put("Status", "update");
                    map0.put("MapName", Data.game.maps.mapName);
                    map0.put("StartGame", String.valueOf(Data.game.isStartGame));
                    map0.put("PlayerSize", String.valueOf(Data.playerGroup.size()));
                    String result0 = HttpRequestOkHttp.doPostJson("https://api.der.kim:41567/api/post/uplist", JSONObject.toJSONString(map0, new SerializerFeature[]{SerializerFeature.PrettyFormat}));
                    HttpRequestOkHttp.doPostRw("http://gs1.corrodinggames.com/masterserver/1.4/interface", result0);
                    HttpRequestOkHttp.doPostRw("http://gs4.corrodinggames.net/masterserver/1.4/interface", result0);
                    return "Y";
                }

                protected Object defruns() {
                    return null;
                }
            }).setSleepTime(10).setRetryFreq(2).execute();
            if (pingdata == null) {
                Log.warn("错误 请检查网络");
            }

        }, 50, 50, TimeUnit.SECONDS, "UPLIST");
    }
}
