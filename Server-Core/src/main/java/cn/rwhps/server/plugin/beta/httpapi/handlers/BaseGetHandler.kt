package cn.rwhps.server.plugin.beta.httpapi.handlers

import cn.rwhps.server.data.json.Json
import cn.rwhps.server.net.http.SendWeb
import cn.rwhps.server.net.http.WebGet
import cn.rwhps.server.plugin.beta.httpapi.ConfigHelper.config
import cn.rwhps.server.plugin.beta.httpapi.CookieParser.toCookie
import cn.rwhps.server.plugin.beta.httpapi.responses.BaseResp
import cn.rwhps.server.util.encryption.Sha
import cn.rwhps.server.util.inline.toPrettyPrintingJson
import io.netty.handler.codec.http.HttpHeaderNames

open class BaseGetHandler(needAuth: Boolean = true) : WebGet() {
    private lateinit var remote: SendWeb
    private var needAuth = true
    lateinit var param: Json

    init {
        this.needAuth = needAuth
    }

    override fun get(getUrl: String, data: String, send: SendWeb) {
        param = stringResolveToJson(data)
        remote = send
        if (remote.request.headers().get(HttpHeaderNames.ORIGIN) != null) {
            remote.setHead("Access-Control-Allow-Origin", remote.request.headers().get(HttpHeaderNames.ORIGIN)) // 允许跨域
            remote.setHead("Access-Control-Allow-Credentials","true")
        }
        if (needAuth && (remote.request.headers().get(HttpHeaderNames.COOKIE)?.toCookie()?.get("token") != Sha.sha256(config.token + config.salt))) {
            send(BaseResp(code = 403, reason = "invalid cookie").toPrettyPrintingJson())
            return
        }
    }

    protected fun send(data: String) {
        remote.setData(data)
        remote.send()
    }
}