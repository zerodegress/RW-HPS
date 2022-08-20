package cn.rwhps.server.plugin.beta.httpapi.handlers

import cn.rwhps.server.data.json.Json
import cn.rwhps.server.net.http.SendWeb
import cn.rwhps.server.net.http.WebGet
import cn.rwhps.server.plugin.beta.httpapi.ConfigHelper.config
import cn.rwhps.server.plugin.beta.httpapi.responses.BaseResp
import cn.rwhps.server.util.inline.toPrettyPrintingJson

open class BaseHandler : WebGet() {
    lateinit var param: Json
    lateinit var remote: SendWeb

    override fun get(getUrl: String, data: String, send: SendWeb) {
        param = stringResolveToJson(data)
        remote = send
        if (param.getData("token") != config.token) {
            send(BaseResp(code = 403, reason = "invalid token").toPrettyPrintingJson())
            return
        }
    }

    protected fun send(data: String) {
        remote.setData(data)
        remote.send()
    }
}