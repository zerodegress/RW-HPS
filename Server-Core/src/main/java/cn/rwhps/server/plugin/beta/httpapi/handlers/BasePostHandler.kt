package cn.rwhps.server.plugin.beta.httpapi.handlers

import cn.rwhps.server.data.json.Json
import cn.rwhps.server.net.http.SendWeb
import cn.rwhps.server.net.http.WebPost
import cn.rwhps.server.plugin.beta.httpapi.ConfigHelper
import cn.rwhps.server.plugin.beta.httpapi.CookieParser.toCookie
import cn.rwhps.server.plugin.beta.httpapi.responses.BaseResp
import cn.rwhps.server.util.encryption.Sha
import cn.rwhps.server.util.inline.toPrettyPrintingJson
import io.netty.handler.codec.http.HttpHeaderNames

open class BasePostHandler(needAuth: Boolean = true) : WebPost() {
    private lateinit var remote: SendWeb
    private var needAuth = true
    lateinit var param: Json

    init {
        this.needAuth = needAuth
    }

    override fun post(postUrl: String, urlData: String, data: String, send: SendWeb) {
        param = stringResolveToJson(data, send)
        remote = send
        if (remote.request.headers().get(HttpHeaderNames.ORIGIN) != null) {
            remote.appendHeaders["Access-Control-Allow-Origin"] = arrayListOf(remote.request.headers().get(HttpHeaderNames.ORIGIN)) // 允许跨域
            remote.appendHeaders["Access-Control-Allow-Credentials"] = arrayListOf("true")
        }
        if (needAuth && (remote.request.headers().get(HttpHeaderNames.COOKIE)?.toCookie()?.get("token") != Sha.sha256(
                ConfigHelper.config.token + ConfigHelper.config.salt))) {
            send(BaseResp(code = 403, reason = "invalid cookie").toPrettyPrintingJson())
            return
        }
    }

    protected fun send(data: String) {
        remote.setData(data)
        remote.send()
    }
}