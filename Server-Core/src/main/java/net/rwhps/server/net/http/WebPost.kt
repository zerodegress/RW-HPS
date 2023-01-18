/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.net.http

import net.rwhps.server.data.json.Json
import net.rwhps.server.util.inline.ifNullResult
import net.rwhps.server.util.log.exp.ImplementedException

/**
 * @author RW-HPS/Dr
 */
abstract class WebPost {
    abstract fun post(accept: AcceptWeb, send: SendWeb)

    protected fun stringResolveToJson(accept: AcceptWeb) : Json {
        if (accept.data.isEmpty()) {
            return Json(LinkedHashMap<String, String>())
        }
        val hd = accept.getHeaders("Content-Type").ifNullResult({ it.trim()}) { "" }
        return if (hd.contains("application/x-www-form-urlencoded",true)) {
            val paramArray: Array<String> = accept.data.split("&".toRegex()).toTypedArray()
            val listMap = LinkedHashMap<String, String>()
            for (pam in paramArray) {
                val keyValue = pam.split("=".toRegex()).toTypedArray()
                listMap[keyValue[0]] = keyValue[1]
            }
            Json(listMap)
        } else if (hd.contains("application/json",true)) {
            Json(accept.data)
        } else {
            throw ImplementedException(accept.data)
        }
    }
}