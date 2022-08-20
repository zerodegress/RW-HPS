package cn.rwhps.server.plugin.beta.httpapi.handlers

import cn.rwhps.server.net.http.SendWeb
import cn.rwhps.server.plugin.beta.httpapi.responses.BaseResp
import cn.rwhps.server.plugin.beta.httpapi.responses.GameInfoResp
import cn.rwhps.server.util.inline.toPrettyPrintingJson

class GameInfoHandler : BaseHandler() {
    override fun get(getUrl: String, data: String, send: SendWeb) {
        super.get(getUrl, data, send)
        send(BaseResp(data = GameInfoResp()).toPrettyPrintingJson())
    }
}