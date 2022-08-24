package cn.rwhps.server.plugin.beta.httpapi.handlers.post

import cn.rwhps.server.net.http.SendWeb
import cn.rwhps.server.plugin.beta.httpapi.handlers.BasePostHandler

class AuthCookieHandler : BasePostHandler(false) {
    // 或许有某天会有用呢?我也不知道
    override fun post(postUrl: String, urlData: String, data: String, send: SendWeb) {
//        super.post(postUrl, urlData, data, send)
//        if (param.getData("token") != config.token) {
//            send(BaseResp(code = 403, reason = "invalid token").toPrettyPrintingJson())
//            return
//        }
//        val result = Sha.sha256(config.token + config.salt)
//        send.addCookie("token", result, 31536000) // 1年
//        send(BaseResp(data = result).toPrettyPrintingJson())
    }
}