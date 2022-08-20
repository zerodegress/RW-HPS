package cn.rwhps.server.plugin.beta.httpapi.handlers

import cn.rwhps.server.net.http.SendWeb
import cn.rwhps.server.plugin.beta.httpapi.responses.AboutResp
import cn.rwhps.server.plugin.beta.httpapi.responses.BaseResp
import cn.rwhps.server.util.inline.toPrettyPrintingJson

class AboutHandler : BaseHandler() {
    override fun get(getUrl: String, data: String, send: SendWeb) {
        super.get(getUrl, data, send)
        send(BaseResp(data = AboutResp()).toPrettyPrintingJson())
    }
}