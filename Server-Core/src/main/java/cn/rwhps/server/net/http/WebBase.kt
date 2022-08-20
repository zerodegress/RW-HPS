package cn.rwhps.server.net.http

import cn.rwhps.server.data.json.Json

abstract class WebBase {
    protected fun stringResolveToJson(data: String) : Json {
        if (data.isEmpty()) {
            return Json(LinkedHashMap<String, String>())
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