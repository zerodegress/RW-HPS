package com.github.dr.rwserver.core;

import com.github.dr.rwserver.core.thread.Threads;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.data.plugin.PluginData;
import com.github.dr.rwserver.data.plugin.PluginManage;
import com.github.dr.rwserver.net.Administration;
import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.util.file.FileUtil;
import com.github.dr.rwserver.util.log.Log;

import java.util.UUID;

import static com.github.dr.rwserver.util.IsUtil.isBlank;
import static com.github.dr.rwserver.util.RandomUtil.generateStr;

/**
 * @author Dr
 */
public final class Application {
    public final PluginData pluginData;
    /** 服务器唯一UUID */
    public String serverConnectUuid;
    public String serverToken;
    public Seq<String> unitBase64;
    public Administration admin;
    public boolean upServerList = false;

    public String serverName = "m3ua";
    public float defIncome = 1f;

    public Application() {
        pluginData = new PluginData();
        serverToken = generateStr(40);
    }

    public final void load() {
        pluginData.setFileUtil(FileUtil.getFolder(Data.Plugin_Data_Path).toFile("Settings.bin"));
        admin = new Administration(pluginData);
        serverConnectUuid = pluginData.getData("serverConnectUuid", UUID.randomUUID().toString());
        unitBase64 = pluginData.getData("unitBase64", new Seq<>());

        Threads.addSavePool(() -> {
            pluginData.setData("serverConnectUuid",serverConnectUuid);
            pluginData.setData("unitBase64",unitBase64);
        });
    }

    public void save() {
        // 先执行自己的保存
        Threads.runSavePool();
        // 保存自己
        pluginData.save();
        // 保存Plugin
        PluginManage.runOnDisable();
    }

    public final long getJavaHeap() {
        return Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory();
    }

    public final long getJavaTotalMemory() {
        return Runtime.getRuntime().totalMemory();
    }

    public final long getJavaFreeMemory() {
        return Runtime.getRuntime().freeMemory();
    }

    public final String getJavaVendor() {
        return get("java.vendor");
    }

    public final String getJavaVersion() {
        return get("java.version");
    }

    public final String getOsName() {
        return get("os.name");
    }

    public final boolean isJavaVersionAtLeast(final float requiredVersion) {
        final String version = get("java.version");
        return (isBlank(version) ? 0f : Float.parseFloat(version)) >= requiredVersion;
    }

    public final boolean isWindows() {
        final String os = get("os.name");
        return (isBlank(os) || os.toLowerCase().contains("windows"));
    }

    public final long getPid() {
        return ProcessHandle.current().pid();
    }


    /**
     * 取得系统属性，如果因为Java安全的限制而失败，则将错误打在Log中，然后返回
     * @param name  属性名
     * @return 属性值或null
     * @see System String
     * @see System String
     */
    private String get(final String name) {
        String value = null;
        try {
            value = System.getProperty(name);
        } catch (SecurityException e) {
            Log.error("Security level limit",e);
        }
        if (null == value) {
            try {
                value = System.getenv(name);
            } catch (SecurityException e) {
                Log.error("Security level limit",e);
            }
        }
        return value;
    }
}
