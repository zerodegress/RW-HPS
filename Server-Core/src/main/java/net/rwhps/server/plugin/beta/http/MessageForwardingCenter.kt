/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.plugin.beta.http

import io.netty.handler.codec.http.HttpHeaderValues
import net.rwhps.server.data.global.Data
import net.rwhps.server.func.StrCons
import net.rwhps.server.game.HessModuleManage
import net.rwhps.server.game.ModManage
import net.rwhps.server.net.core.web.WebGet
import net.rwhps.server.net.core.web.WebPost
import net.rwhps.server.net.http.AcceptWeb
import net.rwhps.server.net.http.SendWeb
import net.rwhps.server.plugin.beta.http.data.GetData
import net.rwhps.server.util.inline.toJson

/**
 * @date  2023/6/27 10:49
 * @author  RW-HPS/Dr
 */
class MessageForwardingCenter {
    val getCategorize = object: WebGet() {
        override fun get(accept: AcceptWeb, send: SendWeb) {
            send.setConnectType(HttpHeaderValues.APPLICATION_JSON)
            when (accept.getUrl.removePrefix("/${RwHpsWebApiMain.name}/api/get/")) {
                "event/GameOver" -> {
                    send.setData(GetData.GameOverPositive.data.data.toList().toJson().toWebStatusJson())
                    GetData.GameOverPositive.data.data.clear()
                }
                "info/SystemInfo" -> send.setData(GetData.SystemInfo().toJson().toWebStatusJson())
                "info/GameInfo" -> send.setData(GetData.GameInfo().toJson().toWebStatusJson())
                "info/ModsInfo" -> send.setData(ModManage.getModsList().toJson().toWebStatusJson())
                else -> send.send404(false)
            }
            send.send()
        }
    }

    val postCategorize = object: WebPost() {
        override fun post(accept: AcceptWeb, send: SendWeb) {
            send.setConnectType(HttpHeaderValues.APPLICATION_JSON)
            when (accept.getUrl.removePrefix("/${RwHpsWebApiMain.name}/api/post/")) {
                "run/ServerCommand" -> {
                    val result = StringBuilder()
                    checkStringPost(accept, send, "runCommand")?.let { command ->
                        Data.SERVER_COMMAND.handleMessage(command, StrCons { result.append(it) })
                        send.setData(result.toString().toWebStatusJson())
                    }
                }
                "run/ClientCommand" -> {
                    val result = StringBuilder()
                    checkStringPost(accept, send, "runCommand")?.let { command ->
                        HessModuleManage.hps.room.clientHandler.handleMessage(command, StrCons { result.append(it) })
                        send.setData(result.toString().toWebStatusJson())
                    }
                }
                else -> send.send404(false)
            }
            send.send()
        }

        private fun checkStringPost(accept: AcceptWeb, send: SendWeb, type: String): String? {
            val json = stringPostDataResolveToJson(accept)
            if (json.getString(type, "").isBlank()) {
                send.setData("".toWebStatusJson("Unknown parameter"))
                send.send()
                return null
            }
            return json.getString(type)
        }
    }

    /**
     * @date  2023/7/1 16:30
     * @author  RW-HPS/Dr
     */
    private data class WebStatus(
        val status: String, val data: String
    )

    private fun String.toWebStatus(status: String = "OK"): WebStatus {
        return WebStatus(status, this)
    }

    private fun String.toWebStatusJson(status: String = "OK"): String {
        return WebStatus(status, this).toJson()
    }
}