package cn.rwhps.server.plugin.beta.httpapi

import cn.rwhps.server.plugin.Plugin
import cn.rwhps.server.plugin.beta.httpapi.ConfigHelper.config
import cn.rwhps.server.plugin.beta.httpapi.handlers.*
import cn.rwhps.server.util.log.Log
import com.sun.net.httpserver.HttpServer
import java.net.InetSocketAddress

class ApiMain : Plugin() {
    lateinit var server: HttpServer

    override fun onEnable() {
        val configFile = pluginDataFileUtil.toFile("HttpApi.json")
        ConfigHelper.init(configFile)
        if (!config.enabled) return
        if (config.token == "defaultToken") Log.warn("Please change the token,don't use the default token!")
        server = HttpServer.create(InetSocketAddress(config.listen, config.port), 0)
        server.createContext("/about", AboutHandler())
        server.createContext("/info", InfoHandler())
        server.createContext("/gameinfo", GameInfoHandler())
        server.createContext("/plugins", PluginsHandler())
        server.createContext("/mods", ModsHandler())
        server.start()
        Log.info("HttpApi server started on ${config.listen}:${config.port} with token ${config.token}")
    }

    override fun onDisable() {
        server.stop(0)
    }
}