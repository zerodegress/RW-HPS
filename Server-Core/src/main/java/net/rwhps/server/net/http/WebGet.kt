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

/**
 * @author RW-HPS/Dr
 */
abstract class WebGet {
    /**
     * 开发者不应该在类中缓存任何数据
     * 因为 GET/POST 不是长链接 只是一个短链接 当请求发送完毕 那么就会被 Close
     * 不支持 Keep
     * HTTP Service 仅对 本次的调用负责
     * 是线程不安全的
     */
    /**
     * 处理数据
     * @param accept AcceptWeb
     * @param send SendWeb
     */
    abstract fun get(accept: AcceptWeb, send: SendWeb)


    protected fun stringResolveToJson(data: String) : Json {
        if (data.isEmpty()) {
            return Json(LinkedHashMap<String, String>());
        }
        val paramArray: Array<String> = data.split("&".toRegex()).toTypedArray()
        val listMap = LinkedHashMap<String, String>()
        for (pam in paramArray) {
            val keyValue = pam.split("=".toRegex()).toTypedArray()
            listMap[keyValue[0]] = keyValue[1]
        }
        return Json(listMap)
    }
}