package cn.rwhps.server.plugin.beta.httpapi.handlers.get

import cn.rwhps.server.net.http.SendWeb
import cn.rwhps.server.plugin.beta.httpapi.handlers.BaseGetHandler
import cn.rwhps.server.plugin.beta.httpapi.responses.BaseResp
import cn.rwhps.server.plugin.beta.httpapi.responses.GameInfoResp
import cn.rwhps.server.util.inline.toPrettyPrintingJson

class GameInfoGetHandler : BaseGetHandler() {
    override fun get(getUrl: String, data: String, send: SendWeb) {
        super.get(getUrl, data, send)
        send(BaseResp(data = GameInfoResp()).toPrettyPrintingJson())
    }
}