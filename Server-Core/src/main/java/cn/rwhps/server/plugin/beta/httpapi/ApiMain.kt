package cn.rwhps.server.plugin.beta.httpapi

import cn.rwhps.server.net.http.WebData
import cn.rwhps.server.plugin.Plugin
import cn.rwhps.server.plugin.beta.httpapi.ConfigHelper.config
import cn.rwhps.server.plugin.beta.httpapi.handlers.*
import cn.rwhps.server.util.log.Log

class ApiMain : Plugin() {
    override fun onEnable() {
        val configFile = pluginDataFileUtil.toFile("HttpApi.json")
        ConfigHelper.init(configFile)
        if (!config.enabled) return
        if (config.token == "defaultToken") Log.warn("Please change the token,don't use the default token!")
        WebData.addWebGetInstance("${config.path}/get/auth", AuthCookieHandler()) //懒得用post
        WebData.addWebGetInstance("${config.path}/get/about", AboutHandler())
        WebData.addWebGetInstance("${config.path}/get/info", InfoHandler())
        WebData.addWebGetInstance("${config.path}/get/gameinfo", GameInfoHandler())
        WebData.addWebGetInstance("${config.path}/get/plugins", PluginsHandler())
        WebData.addWebGetInstance("${config.path}/get/mods", ModsHandler())
        Log.info("HttpApi server started with token ${config.token}")
    }

    override fun onDisable() {
        WebData.removeWebGetInstance("${config.path}/get/about")
        WebData.removeWebGetInstance("${config.path}/get/info")
        WebData.removeWebGetInstance("${config.path}/get/gameinfo")
        WebData.removeWebGetInstance("${config.path}/get/plugins")
        WebData.removeWebGetInstance("${config.path}/get/mods")
    }
}