package cn.rwhps.server.plugin.beta.httpapi.handlers

import cn.rwhps.server.data.mods.ModManage
import cn.rwhps.server.plugin.beta.httpapi.responses.BaseResp
import cn.rwhps.server.plugin.beta.httpapi.responses.ModsResp
import com.sun.net.httpserver.HttpExchange

class ModsHandler : BaseHandler() {
    override fun handle(exchange: HttpExchange) {
        super.handle(exchange)
        val mods: ArrayList<ModsResp> = arrayListOf()
        ModManage.getModsList().each {
            mods.add(ModsResp(it))
        }
        os.write(gson.toJson(BaseResp(data = mods)).toByteArray())
        close()
    }
}