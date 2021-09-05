/*
 * Copyright 2020-2021 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.custom

import com.github.dr.rwserver.core.thread.Threads.newThreadCore
import com.github.dr.rwserver.core.thread.Threads.newThreadService2
import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.func.StrCons
import com.github.dr.rwserver.net.HttpRequestOkHttp
import com.github.dr.rwserver.util.IsUtil.notIsBlank
import com.github.dr.rwserver.util.ReExp
import com.github.dr.rwserver.util.game.CommandHandler
import com.github.dr.rwserver.util.log.Log
import com.github.dr.rwserver.util.log.Log.clog
import com.github.dr.rwserver.util.log.Log.error
import okhttp3.FormBody
import java.util.concurrent.TimeUnit

class UpListCustom(handler: CommandHandler) {
    private val tk = Data.config.readString("token", "")
    private var upServerList = false

    init {
        handler.removeCommand("upserverlist")
        handler.removeCommand("upserverlistnew")
        if (notIsBlank(tk)) {
            handler.register("upserverlist", "serverCommands.upserverlist") { _: Array<String>?, log: StrCons ->
                if (!upServerList) {
                    newThreadCore {
                        upServerList = true
                        uplist()
                    }
                } else {
                    log["已上传 不需要再次上传"]
                }
            }
        } else {
            handler.register("upserverlist", "serverCommands.upserverlist") { _: Array<String?>?, log: StrCons ->
                log["无Tonken"]
            }
        }
    }

    private fun uplist() {
        val formBody = FormBody.Builder()

        formBody.add("Token",tk)

        formBody.add("Passwd", notIsBlank(Data.game.passwd).toString())
        formBody.add("ServerName",Data.core.serverName)
        formBody.add("Port", Data.game.port.toString())
        formBody.add("MapName",Data.game.maps.mapName)
        formBody.add("PlayerSize",Data.playerGroup.size().toString())
        formBody.add("PlayerMaxSize",Data.game.maxPlayer.toString())

        val resultUpList = HttpRequestOkHttp.doPost("https://api.data.der.kim/UpList/upList?Status=add", formBody)

        if (resultUpList.startsWith("[-1]")) {
            error("[Get UPLIST Data Error] Please Check API/Token")
            return
        }

        val resultCheckPort = HttpRequestOkHttp.doPost("https://api.data.der.kim/UpList/upList?Status=port", "Token=$tk&Port="+Data.game.port.toString())

        HttpRequestOkHttp.doPostRw("http://gs1.corrodinggames.com/masterserver/1.4/interface", resultUpList)
        HttpRequestOkHttp.doPostRw("http://gs4.corrodinggames.net/masterserver/1.4/interface", resultUpList)



        val checkPortGs1 = HttpRequestOkHttp.doPostRw("http://gs1.corrodinggames.com/masterserver/1.4/interface", resultCheckPort).contains("true")
        val checkPortGs4 = HttpRequestOkHttp.doPostRw("http://gs4.corrodinggames.net/masterserver/1.4/interface", resultCheckPort).contains("true")
        if (checkPortGs1 || checkPortGs4) {
            clog(Data.localeUtil.getinput("err.yesOpen"))
        } else {
            clog(Data.localeUtil.getinput("err.noOpen"))
        }

        newThreadService2({
            val pingdata = object : ReExp() {
                @Throws(Exception::class)
                override fun runs(): Any {
                    val formBodyUpdate = FormBody.Builder()

                    formBodyUpdate.add("Token",tk)

                    formBodyUpdate.add("Passwd", notIsBlank(Data.game.passwd).toString())
                    formBodyUpdate.add("ServerName",Data.core.serverName)
                    formBodyUpdate.add("Port", Data.game.port.toString())
                    formBodyUpdate.add("MapName",Data.game.maps.mapName)
                    formBodyUpdate.add("GameStatus",if (Data.game.isStartGame) "ingame" else "battleroom")
                    formBodyUpdate.add("PlayerSize",Data.playerGroup.size().toString())
                    formBodyUpdate.add("PlayerMaxSize",Data.game.maxPlayer.toString())

                    val result0 = HttpRequestOkHttp.doPost("https://api.data.der.kim/UpList/upList?Status=update", formBodyUpdate)
                    HttpRequestOkHttp.doPostRw("http://gs1.corrodinggames.com/masterserver/1.4/interface", result0)
                    HttpRequestOkHttp.doPostRw("http://gs4.corrodinggames.net/masterserver/1.4/interface", result0)

                    return "Y"
                }

                override fun defruns(): Any? {
                    return null
                }
            }.setSleepTime(10).setRetryFreq(3).execute()
            if (pingdata == null) {
                Log.warn("错误 请检查网络")
            }
        }, 40, 40, TimeUnit.SECONDS, "UPLIST")
    }
}