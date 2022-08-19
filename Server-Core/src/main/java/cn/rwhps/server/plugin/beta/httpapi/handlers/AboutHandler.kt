package cn.rwhps.server.plugin.beta.httpapi.handlers

import cn.rwhps.server.plugin.beta.httpapi.responses.AboutResp
import cn.rwhps.server.plugin.beta.httpapi.responses.BaseResp
import com.sun.net.httpserver.HttpExchange

class AboutHandler : BaseHandler() {
    override fun handle(exchange: HttpExchange) {
        super.handle(exchange)
        os.write(gson.toJson(BaseResp(data = AboutResp())).toByteArray())
        close()
    }
}