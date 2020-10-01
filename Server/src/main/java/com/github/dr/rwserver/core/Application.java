package com.github.dr.rwserver.core;

import com.github.dr.rwserver.data.global.Settings;
import com.github.dr.rwserver.net.Administration;

import java.util.UUID;

import static com.github.dr.rwserver.util.RandomUtil.generateStr;

/**
 * @author Dr
 */
public class Application {
    public final Settings settings;
    /** 服务器唯一UUID */
    public final String serverConnectUuid;
    public String serverToken;
    public final Administration admin;
    public boolean upServerList = false;

    public String serverName = "RW-HPS";
    public float defIncome = 1f;

    public Application() {
        settings = new Settings();
        admin = new Administration(settings);
        serverToken = generateStr(40);
        serverConnectUuid = settings.getString("serverConnectUuid", UUID.randomUUID().toString());
    }

    public void save() {
        settings.put("serverConnectUuid",serverConnectUuid);
        admin.save(settings);
        settings.saveData();
    }

    public long getJavaHeap() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    public long getJavaTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    public long getJavaFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }
}
