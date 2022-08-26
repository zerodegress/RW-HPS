package cn.rwhps.server.plugin.beta.httpapi.handlers.get

import cn.rwhps.server.net.http.AcceptWeb
import cn.rwhps.server.net.http.SendWeb
import cn.rwhps.server.plugin.beta.httpapi.handlers.BaseGetHandler
import cn.rwhps.server.plugin.beta.httpapi.responses.AboutResp
import cn.rwhps.server.plugin.beta.httpapi.responses.BaseResp
import cn.rwhps.server.util.inline.toPrettyPrintingJson

class AboutHandler : BaseGetHandler() {
    override fun get(accept: AcceptWeb, send: SendWeb) {
        super.get(accept, send)
        send(send,BaseResp(data = AboutResp()).toPrettyPrintingJson())
    }
}