/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.plugin.beta.http

import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpResponseStatus
import net.rwhps.server.data.global.Data
import net.rwhps.server.data.json.Json
import net.rwhps.server.net.core.web.WebGet
import net.rwhps.server.net.core.web.WebPost
import net.rwhps.server.net.http.AcceptWeb
import net.rwhps.server.net.http.SendWeb
import net.rwhps.server.struct.ObjectIntMap
import net.rwhps.server.struct.ObjectMap
import net.rwhps.server.util.RandomUtils
import net.rwhps.server.util.Time
import net.rwhps.server.util.algorithms.digest.DigestUtils

/**
 * @date  2023/6/27 11:07
 * @author  RW-HPS/Dr
 */
class Authentication(private val messageForwardingCenter: MessageForwardingCenter) {
    private val cookiesData = ObjectIntMap<String>()

    fun registerAuthenticationCenter() {
        Data.webData.addWebGetInstance("${RwHpsWebApiMain.url}/api/AuthCookie", object: WebGet() {
            override fun get(accept: AcceptWeb, send: SendWeb) = registerCookie(stringUrlDataResolveToJson(accept), accept, send)
        })
        Data.webData.addWebGetInstance("${RwHpsWebApiMain.url}/api/get/**", object: WebGet() {
            override fun get(accept: AcceptWeb, send: SendWeb) {
                val cookies = headResolveToCookie(accept)
                if (authentication(cookies, send)) {
                    messageForwardingCenter.getCategorize.get(accept, send)
                }
            }
        })

        Data.webData.addWebPostInstance("${RwHpsWebApiMain.url}/api/AuthCookie", object: WebPost() {
            override fun post(accept: AcceptWeb, send: SendWeb) = registerCookie(stringPostDataResolveToJson(accept), accept, send)
        })
        Data.webData.addWebPostInstance("${RwHpsWebApiMain.url}/api/post/**", object: WebPost() {
            override fun post(accept: AcceptWeb, send: SendWeb) {
                val cookies = headResolveToCookie(accept)
                if (authentication(cookies, send)) {
                    messageForwardingCenter.postCategorize.post(accept, send)
                }
            }
        })
    }

    private fun registerCookie(json: Json, accept: AcceptWeb, send: SendWeb) {
        if (accept.getHeaders(HttpHeaderNames.ORIGIN) != null) {
            // 允许跨域
            send.setHead(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, accept.getHeaders(HttpHeaderNames.ORIGIN)!!)
            send.setHead(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS,"true")
        }

        if (json.getString("passwd") == Data.config.WebToken) {
            val cValue = DigestUtils.sha256Hex(RandomUtils.getRandomString(32))
            send.setCookie(RwHpsWebApiMain.cookieName, cValue, 86400, RwHpsWebApiMain.url)
            cookiesData[cValue] = Time.concurrentSecond()+86400
            send.setData("Hi RW-HPS !")
            send.send()
        } else {
            send.status = HttpResponseStatus.FORBIDDEN
            send.setData("[Forbidden] Input error")
            send.send()
        }
    }

    private fun authentication(cookies: ObjectMap<String, String>, send: SendWeb): Boolean {
        if (cookies.isEmpty()) {
            send.status = HttpResponseStatus.UNAUTHORIZED
            send.setData("[Unauthorized] Please certify")
            send.send()
            return false
        }

        if (cookiesData[cookies[RwHpsWebApiMain.cookieName, ""], 0] < Time.concurrentSecond()) {
            send.status = HttpResponseStatus.UNAUTHORIZED
            send.setData("[Unauthorized] The cookies expires")
            send.send()
            return false
        }
        return true
    }
}