package cn.rwhps.server.plugin.beta.httpapi

import cn.rwhps.server.net.http.WebData
import cn.rwhps.server.plugin.Plugin
import cn.rwhps.server.plugin.beta.httpapi.ConfigHelper.config
import cn.rwhps.server.plugin.beta.httpapi.handlers.get.*
import cn.rwhps.server.plugin.beta.httpapi.handlers.post.AuthCookieGetHandler
import cn.rwhps.server.plugin.beta.httpapi.handlers.post.CommandGetHandler
import cn.rwhps.server.util.log.Log

class ApiMain : Plugin() {
    override fun onEnable() {
        val configFile = pluginDataFileUtil.toFile("HttpApi.json")
        ConfigHelper.init(configFile)
        if (!config.enabled) return
        if (config.token == "defaultToken") Log.warn("Please change the token,don't use the default token!")

        // POST
        WebData.addWebPostInstance("${config.path}/post/auth", AuthCookieGetHandler())
        WebData.addWebPostInstance("${config.path}/post/command", CommandGetHandler())

        // GET
        WebData.addWebGetInstance("${config.path}/get/about", AboutGetHandler())
        WebData.addWebGetInstance("${config.path}/get/info", InfoGetHandler())
        WebData.addWebGetInstance("${config.path}/get/gameinfo", GameInfoGetHandler())
        WebData.addWebGetInstance("${config.path}/get/plugins", PluginsGetHandler())
        WebData.addWebGetInstance("${config.path}/get/mods", ModsGetHandler())
        Log.info("HttpApi server started with token ${config.token}")
    }

    override fun onDisable() {
        // POST
        WebData.removeWebPostInstance("${config.path}/get/auth")
        WebData.removeWebPostInstance("${config.path}/get/command")
        // GET
        WebData.removeWebGetInstance("${config.path}/get/about")
        WebData.removeWebGetInstance("${config.path}/get/info")
        WebData.removeWebGetInstance("${config.path}/get/gameinfo")
        WebData.removeWebGetInstance("${config.path}/get/plugins")
        WebData.removeWebGetInstance("${config.path}/get/mods")
    }
}