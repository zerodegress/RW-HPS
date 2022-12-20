/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.net.api

import net.rwhps.server.net.HttpRequestOkHttp
import net.rwhps.server.net.http.AcceptWeb
import net.rwhps.server.net.http.SendWeb
import net.rwhps.server.net.http.WebPost
import net.rwhps.server.struct.IntMap
import net.rwhps.server.util.encryption.Base64

class WebPostRelayHttp : WebPost() {
    override fun post(accept: AcceptWeb, send: SendWeb) {
        val json = this.stringResolveToJson(accept)
        when (json.getString("type")) {
            "add" -> {
                map.put(json.getString("port").toInt(),json.getString("id"))
            }
            "post" -> {
                HttpRequestOkHttp.doPost(json.getString("url"), Base64.decodeString(json.getString("data")))
            }
            "remove" -> {
                map.remove(json.getString("port").toInt())
            }
        }
        send.setData("OK")
        send.send()
    }

    companion object {
        val map = IntMap<String>()
    }
}