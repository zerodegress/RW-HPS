package cn.rwhps.server.plugin.beta.httpapi.handlers.get

import cn.rwhps.server.data.mods.ModManage
import cn.rwhps.server.net.http.AcceptWeb
import cn.rwhps.server.net.http.SendWeb
import cn.rwhps.server.plugin.beta.httpapi.handlers.BaseGetHandler
import cn.rwhps.server.plugin.beta.httpapi.responses.BaseResp
import cn.rwhps.server.plugin.beta.httpapi.responses.ModsResp
import cn.rwhps.server.util.inline.toPrettyPrintingJson

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