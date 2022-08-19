package cn.rwhps.server.plugin.beta.httpapi.handlers

import cn.rwhps.server.plugin.beta.httpapi.responses.BaseResp
import cn.rwhps.server.plugin.beta.httpapi.responses.InfoResp
import com.sun.net.httpserver.HttpExchange

class InfoHandler : BaseHandler() {
    override fun handle(exchange: HttpExchange) {
        super.handle(exchange)
        os.write(gson.toJson(BaseResp(data = InfoResp())).toByteArray())
        close()
    }
}