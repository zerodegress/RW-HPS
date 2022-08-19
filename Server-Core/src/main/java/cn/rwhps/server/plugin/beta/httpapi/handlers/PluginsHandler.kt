package cn.rwhps.server.plugin.beta.httpapi.handlers

import cn.rwhps.server.data.plugin.PluginManage
import cn.rwhps.server.plugin.PluginsLoad
import cn.rwhps.server.plugin.beta.httpapi.responses.BaseResp
import cn.rwhps.server.plugin.beta.httpapi.responses.PluginsResp
import com.sun.net.httpserver.HttpExchange

class PluginsHandler : BaseHandler() {
    override fun handle(exchange: HttpExchange) {
        super.handle(exchange)
        val plugins: ArrayList<PluginsResp> = arrayListOf()
        PluginManage.run { e: PluginsLoad.PluginLoadData? ->
            plugins.add(PluginsResp(name = e!!.name, desc = e.description, version = e.version, author = e.author))
        }
        os.write(gson.toJson(BaseResp(data = plugins)).toByteArray())
        close()
    }
}