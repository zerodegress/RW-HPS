package net.rwhps.server.plugin.beta.httpapi.handlers.get

import net.rwhps.server.data.ModManage
import net.rwhps.server.net.http.AcceptWeb
import net.rwhps.server.net.http.SendWeb
import net.rwhps.server.plugin.beta.httpapi.handlers.BaseGetHandler
import net.rwhps.server.plugin.beta.httpapi.responses.BaseResp
import net.rwhps.server.plugin.beta.httpapi.responses.ModsResp
import net.rwhps.server.util.inline.toPrettyPrintingJson

class ModsHandler : BaseGetHandler() {
    override fun get(accept: AcceptWeb, send: SendWeb) {
        super.get(accept, send)
        val mods: ArrayList<ModsResp> = arrayListOf()
        ModManage.getModsList().eachAll {
            mods.add(ModsResp(it))
        }
        send(send,BaseResp(data = mods).toPrettyPrintingJson())
    }
}