package net.rwhps.server.plugin.beta.httpapi.handlers.get

import net.rwhps.server.data.plugin.PluginManage
import net.rwhps.server.net.http.AcceptWeb
import net.rwhps.server.net.http.SendWeb
import net.rwhps.server.plugin.PluginLoadData
import net.rwhps.server.plugin.beta.httpapi.handlers.BaseGetHandler
import net.rwhps.server.plugin.beta.httpapi.responses.BaseResp
import net.rwhps.server.plugin.beta.httpapi.responses.PluginsResp
import net.rwhps.server.util.inline.toPrettyPrintingJson

class PluginsHandler : BaseGetHandler() {
    override fun get(accept: AcceptWeb, send: SendWeb) {
        super.get(accept, send)
        val plugins: ArrayList<PluginsResp> = arrayListOf()
        PluginManage.run { e: PluginLoadData? ->
            plugins.add(PluginsResp(name = e!!.name, desc = e.description, version = e.version, author = e.author))
        }
        send(send,BaseResp(data = plugins).toPrettyPrintingJson())
    }
}