package cn.rwhps.server.plugin.beta.httpapi.handlers.get

import cn.rwhps.server.data.plugin.PluginManage
import cn.rwhps.server.net.http.SendWeb
import cn.rwhps.server.plugin.PluginsLoad
import cn.rwhps.server.plugin.beta.httpapi.handlers.BaseGetHandler
import cn.rwhps.server.plugin.beta.httpapi.responses.BaseResp
import cn.rwhps.server.plugin.beta.httpapi.responses.PluginsResp
import cn.rwhps.server.util.inline.toPrettyPrintingJson

class PluginsHandler : BaseGetHandler() {
    override fun get(getUrl: String, data: String, send: SendWeb) {
        super.get(getUrl, data, send)
        val plugins: ArrayList<PluginsResp> = arrayListOf()
        PluginManage.run { e: PluginsLoad.PluginLoadData? ->
            plugins.add(PluginsResp(name = e!!.name, desc = e.description, version = e.version, author = e.author))
        }
        send(BaseResp(data = plugins).toPrettyPrintingJson())
    }
}