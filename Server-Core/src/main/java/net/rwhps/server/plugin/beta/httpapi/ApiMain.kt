package net.rwhps.server.plugin.beta.httpapi

import net.rwhps.server.net.http.WebData
import net.rwhps.server.plugin.Plugin
import net.rwhps.server.plugin.beta.httpapi.ConfigHelper.config
import net.rwhps.server.plugin.beta.httpapi.handlers.get.*
import net.rwhps.server.plugin.beta.httpapi.handlers.post.CommandHandler
import net.rwhps.server.plugin.beta.httpapi.handlers.ws.InfoWsHandler
import net.rwhps.server.util.log.Log

class ApiMain : Plugin() {
    override fun onEnable() {
        val configFile = pluginDataFileUtil.toFile("HttpApi.json")
        ConfigHelper.init(configFile)
        if (!config.enabled) return
        if (config.token == "defaultToken") Log.warn("Please change the token,don't use the default token!")

        // POST
        //WebData.addWebPostInstance("${config.path}/post/auth", AuthCookieHandler())
        WebData.addWebPostInstance("${config.path}/post/command", CommandHandler())

        // GET
        WebData.addWebGetInstance("${config.path}/get/about", AboutHandler())
        WebData.addWebGetInstance("${config.path}/get/info", InfoHandler())
        WebData.addWebGetInstance("${config.path}/get/gameinfo", GameInfoHandler())
        WebData.addWebGetInstance("${config.path}/get/plugins", PluginsHandler())
        WebData.addWebGetInstance("${config.path}/get/mods", ModsHandler())

        // WS
        WebData.addWebSocketInstance("${WebData.WS_URI}/plugin/httpApi/info", InfoWsHandler())
        Log.info("HttpApi server started with token ${config.token}")
    }

    override fun onDisable() {
        // POST
        //WebData.removeWebPostInstance("${config.path}/get/auth")
        WebData.removeWebPostInstance("${config.path}/get/command")
        // GET
        WebData.removeWebGetInstance("${config.path}/get/about")
        WebData.removeWebGetInstance("${config.path}/get/info")
        WebData.removeWebGetInstance("${config.path}/get/gameinfo")
        WebData.removeWebGetInstance("${config.path}/get/plugins")
        WebData.removeWebGetInstance("${config.path}/get/mods")
        // WS
        WebData.removeWebSocketInstance("${WebData.WS_URI}/plugin/httpApi/info")
    }
}