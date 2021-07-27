package com.github.dr.rwserver.plugin.center;

import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.data.json.Json;
import com.github.dr.rwserver.func.StrCons;
import com.github.dr.rwserver.plugin.GetVersion;
import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.util.file.FileUtil;
import com.github.dr.rwserver.util.game.CommandHandler;
import org.jetbrains.annotations.NotNull;

import static com.github.dr.rwserver.net.HttpRequestOkHttp.doGet;
import static com.github.dr.rwserver.net.HttpRequestOkHttp.downUrl;

public class PluginCenter {
    public static final PluginCenter pluginCenter = new PluginCenter();

    private static final String url = "https://plugin.data.der.kim/";
    private final CommandHandler PluginCommand = new CommandHandler("");

    private PluginCenterData pluginCenterData;

    public PluginCenter() {
        this.pluginCenterData = new PluginCenterData(url+"PluginData");
        register();
    }

    public String getPluginData() {
        return this.pluginCenterData.getPluginData();
    }

    public void command(String str,StrCons log) {
        CommandHandler.CommandResponse response = PluginCommand.handleMessage(str,log);
        if (response.type != CommandHandler.ResponseType.valid) {
            String text;
            if (response.type == CommandHandler.ResponseType.manyArguments) {
                text = "Too many arguments. Usage: " + response.command.text + " " + response.command.paramText;
            } else if (response.type == CommandHandler.ResponseType.fewArguments) {
                text = "Too few arguments. Usage: " + response.command.text + " " + response.command.paramText;
            } else {
                text = "Unknown command. Check plugin help";
            }
            log.get(text);
        }
    }

    private final void register() {
        PluginCommand.<StrCons>register("help","", (arg, log) -> {
            //log.get("plugin updata  更新插件列表");
            log.get("plugin updatalist  更新插件列表");
            log.get("plugin install PluginID  安装指定id的插件");
        });

        PluginCommand.<StrCons>register("updatelist","", (arg, log) -> {
            this.pluginCenterData = new PluginCenterData(url+"PluginData");
            log.get("更新插件列表完成");
        });

        PluginCommand.<StrCons>register("list","", (arg, log) -> {
            log.get(this.pluginCenterData.getPluginData());
        });

        PluginCommand.<StrCons>register("install","<PluginID>","", (arg, log) -> {
            Json json = pluginCenterData.getJson(Integer.parseInt(arg[0]));
            if (!new GetVersion(Data.SERVER_CORE_VERSION).getIfVersion(json.getData("supportedVersions"))) {
                log.get("Plugin版本不兼容 Plugin名字为: {0}",json.getData("name"));
            } else {
                downUrl(url+json.getData("name")+".jar", FileUtil.toFolder(Data.Plugin_Plugins_Path).toPath(json.getData("name")+".jar").getFile());
                log.get("安装完成 请重启服务器");
            }
        });
    }


    private static class PluginCenterData {
        private final Seq<Json> pluginCenterData;

        private PluginCenterData(@NotNull final String url) {
            this.pluginCenterData = new Json(doGet(url)).getArraySeqData();
        }

        private String getPluginData() {
            final StringBuilder stringBuilder = new StringBuilder();
            Json json;
            for (int i = 0;i < pluginCenterData.size(); i++) {
                json = pluginCenterData.get(i);
                stringBuilder.append("ID: ").append(i).append("  ")
                        .append("Name: ").append(json.getData("name")).append("  ")
                        .append("Description: ").append(json.getData("description")).append(Data.LINE_SEPARATOR);
            }
            return stringBuilder.toString();
        }

        private Json getJson(int i) {
            return pluginCenterData.get(i);
        }
    }

}
