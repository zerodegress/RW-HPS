/*
 * Copyright 2020-2021 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.net.web.api

import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.data.json.Json
import com.github.dr.rwserver.data.json.Json.Companion.toJson
import com.github.dr.rwserver.func.StrCons
import com.github.dr.rwserver.struct.ObjectMap
import com.github.dr.rwserver.util.encryption.Base64.encode
import com.shareData.chainMarket.i.Central

/**
 * @author Dr
 */
@Central(url = "/api")
class WebApi {
    @Central(url = "/runServerCommand")
    fun runServerCommand(message: String?, map: Map<Any?, Any?>?): String {
        val json = Json(message!!)
        val str = StringBuilder(8)
        Data.SERVER_COMMAND.handleMessage(json.getData("Command"), StrCons { e: String? -> str.append(e).append("\n") })
        val runServerCommand = ObjectMap<String, Any>(4)
        runServerCommand.put("State", "0")
        runServerCommand.put("Result", encode(str.toString()))
        return encode(toJson(runServerCommand))
    }

    @Central(url = "/runPid")
    fun runPid(message: String?, map: Map<Any?, Any?>?): String {
        val runPid = ObjectMap<String, Any>(4)
        runPid.put("State", "0")
        runPid.put("result", Data.core.pid.toString())
        return encode(toJson(runPid))
    }
}