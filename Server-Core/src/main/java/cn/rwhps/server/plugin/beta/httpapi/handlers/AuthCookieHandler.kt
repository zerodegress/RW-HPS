package cn.rwhps.server.plugin.beta.httpapi.handlers

import cn.rwhps.server.net.http.SendWeb
import cn.rwhps.server.plugin.beta.httpapi.ConfigHelper.config
import cn.rwhps.server.plugin.beta.httpapi.responses.BaseResp
import cn.rwhps.server.util.encryption.Sha
import cn.rwhps.server.util.inline.toPrettyPrintingJson

class AuthCookieHandler : BaseHandler(false) {
    override fun get(getUrl: String, data: String, send: SendWeb) {
        super.get(getUrl, data, send)
        if (param.getData("token") != config.token) {
            send(BaseResp(code = 403, reason = "invalid token").toPrettyPrintingJson())
            return
        }
        send.addCookie("token", Sha.sha256(config.token + config.salt), 31536000) // 1年
        send(BaseResp(data = "success").toPrettyPrintingJson())
    }
}