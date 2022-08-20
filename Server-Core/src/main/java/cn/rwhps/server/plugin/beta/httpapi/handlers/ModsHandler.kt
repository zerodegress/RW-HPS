package cn.rwhps.server.plugin.beta.httpapi.handlers

import cn.rwhps.server.data.mods.ModManage
import cn.rwhps.server.net.http.SendWeb
import cn.rwhps.server.plugin.beta.httpapi.responses.BaseResp
import cn.rwhps.server.plugin.beta.httpapi.responses.ModsResp
import cn.rwhps.server.util.inline.toPrettyPrintingJson

class ModsHandler : BaseHandler() {
    override fun get(getUrl: String, data: String, send: SendWeb) {
        super.get(getUrl, data, send)
        val mods: ArrayList<ModsResp> = arrayListOf()
        ModManage.getModsList().each {
            mods.add(ModsResp(it))
        }
        send(BaseResp(data = mods).toPrettyPrintingJson())
    }
}