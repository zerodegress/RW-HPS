/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.plugin.beta.gamepanel.http

import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.data.player.Player
import com.github.dr.rwserver.func.StrCons
import com.github.dr.rwserver.net.handler.tcp.GamePortWebSocket
import com.github.dr.rwserver.net.http.WebSocket
import com.github.dr.rwserver.util.file.FileUtil.Companion.getFolder
import com.github.dr.rwserver.util.game.CommandHandler
import com.github.dr.rwserver.util.log.Log
import com.github.dr.rwserver.util.log.Log.clog
import com.google.gson.Gson
import io.netty.channel.Channel
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import java.util.function.Consumer
import java.util.regex.Pattern

class PanelWebSocket : WebSocket() {
    private val pwd = "RW-HPS-TEST"
    private val msgPattern = Pattern.compile("-p(.*?) -(.*?) (.*)")
    private val CTL = 1
    private val CONFIG = 2
    private val PING: Int = 3

    private var ws: GamePortWebSocket? = null

    override fun ws(ws: GamePortWebSocket, channel: Channel, msg: String) {
        Log.debug("REC",msg)
        this.ws = ws
        val matcher = msgPattern.matcher(msg)
        if (matcher.find()) {
            if (!ws.hasChannel(channel)) {
                if (pwd != matcher.group(1)) {
                    channel.writeAndFlush(error(100))
                    channel.close()
                    return
                } else {
                    channel.writeAndFlush(TextWebSocketFrame("200"))
                    ws.connected.add(channel)
                }
            }

            var op = -1
            try {
                op = matcher.group(2).toInt()
            } catch (ne: NumberFormatException) {
                channel.writeAndFlush(error(98))
            }
            val tar = matcher.group(3)
            if (op == CTL) {
                handleCmd(channel, tar)
            }
        } else channel.writeAndFlush(error(99))
    }

    private fun sendCmd(conn: Channel) {
        val commandList = Data.SERVER_COMMAND.commandList
        val cmdList = ArrayList<Map<String, String>>()
        commandList.forEach { x: CommandHandler.Command ->
            if (!x.description.startsWith("HIDE")) {
                val cmd = HashMap<String, String>()
                cmd["h"] = x.text
                cmd["p"] = x.paramText
                var des = x.description
                des = if (x.description.startsWith("#")) {
                    des.substring(1)
                } else {
                    Data.i18NBundle.getinput(des)
                }
                cmd["d"] = des
                cmdList.add(cmd)
            }
        }
        conn.writeAndFlush(TextWebSocketFrame("-c " + Gson().toJson(cmdList)))
    }

    private fun handleCmd(conn: Channel, message: String) {
        if (message.startsWith("-rc")) {
            sendCmd(conn)
        } else if (message == "-ts") {
            conn.writeAndFlush(TextWebSocketFrame(gameStateInfo()))
        } else if (message == "-gf") {
            conn.writeAndFlush(TextWebSocketFrame("-gf " + getServerConfigString()))
        } else if (message.startsWith("-sf")) {
            val map: Map<String, Any> = Gson().fromJson<Map<String, Any>>(message.replace("-sf ", ""), Map::class.java)
            map.forEach { (k: String, v: Any) ->
                Data.config.coverField(k, v)
            }
        } else if (message.startsWith("-ac")) {
            val arg = message.substring(4).split(" ".toRegex()).toTypedArray()
            if (arg[0] == "k") {
                Data.game.playerManage.playerGroup.each({e -> e.uuid == arg[1]}) { p: Player ->
                    clog("踢出玩家" + p.name)
                    p.kickPlayer("你已被踢出")
                }
            } else if (arg[0] == "m") {
                Data.game.playerManage.playerGroup.each({e -> e.uuid == arg[1]}) { p: Player ->
                    p.con!!.sendSystemMessage("你已被禁言")
                    p.muteTime = Long.MAX_VALUE
                    clog("禁言玩家" + p.name)
                }
            }
        } else {
            clog("接收到游戏板命令：$message")
            Data.SERVER_COMMAND.handleMessage(message, StrCons {msg:String -> ws!!.broadCast(msg)})
        }
    }

    private fun getServerConfigString(): String {
        Data.config.save()
        return getFolder(Data.Plugin_Data_Path).toFile("Config.json").readFileStringData()
    }

    private val fieldName = arrayOf("玩家昵称","uuid","位置","延迟","队伍","管理","游戏状态","连接地址")


    private fun gameStateInfo(): String {
        Log.debug("HI")

        val info: MutableList<Any> = ArrayList()
        info.add(fieldName)
        val players: MutableList<List<*>>
        val data = HashMap<String, Any>()
        players = ArrayList()
        data["body"] = players
        info.add(data)

        Data.game.playerManage.playerAll.forEach(Consumer { g: Player ->
            val player = ArrayList<String>()
            player.add(g.name)
            player.add(g.uuid)
            player.add(g.site.toString() + "")
            player.add(g.ping.toString() + "")
            player.add(g.team.toString() + "")
            player.add(g.isAdmin.toString() + "")
            player.add(if (g.dead) "已被击败" else if (g.controlThePlayer) "正常" else "断开(被控制)")
            player.add(g.con?.ip + ":" + g.con!!.port)
            players.add(player)
        })
        return "-ts" + Gson().toJson(info)
    }
}