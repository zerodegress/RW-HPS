package net.rwhps.server.plugin.beta.httpapi.handlers

import io.netty.handler.codec.http.HttpHeaderNames
import net.rwhps.server.net.http.AcceptWeb
import net.rwhps.server.net.http.SendWeb
import net.rwhps.server.net.http.WebPost
import net.rwhps.server.plugin.beta.httpapi.ConfigHelper.config
import net.rwhps.server.plugin.beta.httpapi.responses.BaseResp
import net.rwhps.server.util.inline.toPrettyPrintingJson

open class BasePostHandler(private val needAuth: Boolean = true) : WebPost() {
    override fun post(accept: AcceptWeb, send: SendWeb) {
        val param = stringResolveToJson(accept)
        if (accept.getHeaders(HttpHeaderNames.ORIGIN) != null) {
            send.setHead("Access-Control-Allow-Origin", accept.getHeaders(HttpHeaderNames.ORIGIN)!!) // 允许跨域
            send.setHead("Access-Control-Allow-Credentials","true")
        }
        if (needAuth && param.getString("token") == config.token) {
            send(send,BaseResp(code = 403, reason = "invalid token").toPrettyPrintingJson())
            return
        }
    }

    protected fun send(send: SendWeb, data: String) {
        send.setData(data)
        send.send()
    }
}