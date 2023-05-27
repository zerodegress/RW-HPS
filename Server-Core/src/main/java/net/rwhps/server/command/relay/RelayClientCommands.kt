/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.command.relay

import net.rwhps.server.data.global.Data
import net.rwhps.server.data.global.NetStaticData
import net.rwhps.server.data.plugin.PluginManage
import net.rwhps.server.net.netconnectprotocol.internal.relay.fromRelayJumpsToAnotherServer
import net.rwhps.server.net.netconnectprotocol.realize.GameVersionRelay
import net.rwhps.server.util.game.CommandHandler

/**
 * @author RW-HPS/Dr
 */
internal class RelayClientCommands(handler: CommandHandler) {
    private val localeUtil = Data.i18NBundle

    private fun isAdmin(con: GameVersionRelay, sendMsg: Boolean = true): Boolean {
        if (con.relay?.admin === con) {
            return true
        }
        if (sendMsg) {
            sendMsg(con,localeUtil.getinput("err.noAdmin"))
        }
        return false
    }

    init {
        handler.register("help", "clientCommands.help") { _: Array<String>?, con: GameVersionRelay ->
            val str = StringBuilder(16)
            for (command in handler.commandList) {
                if (command.description.startsWith("#")) {
                    str.append("   ").append(command.text).append(if (command.paramText.isEmpty()) "" else " ")
                        .append(command.paramText).append(" - ").append(command.description.substring(1))
                        .append(Data.LINE_SEPARATOR)
                } else {
                    if ("HIDE" == command.description) {
                        continue
                    }
                    str.append("   ").append(command.text).append(if (command.paramText.isEmpty()) "" else " ")
                        .append(command.paramText).append(" - ").append(localeUtil.getinput(command.description))
                        .append(Data.LINE_SEPARATOR)
                }
            }
            sendMsg(con,str.toString())
        }

        handler.register("jump","<ip/id>", "#jump Server") { args: Array<String>, con: GameVersionRelay ->
            if (!isAdmin(con,false)) {
                con.sendPacket(fromRelayJumpsToAnotherServer(args[0]))
            } else {
                sendMsg(con,"You Is ADMIN !")
            }
        }


        handler.register("allmute","#Remove List") { _: Array<String>, con: GameVersionRelay ->
            if (isAdmin(con)) {
                con.relay!!.allmute = !con.relay!!.allmute
                sendMsg(con,"全局禁言状态是 :  ${if (con.relay!!.allmute) "开启" else "关闭"}")
            }
        }

        PluginManage.runRegisterRelayClientCommands(handler)
    }

    private fun sendMsg(con: GameVersionRelay, msg: String) {
        con.sendPacket(NetStaticData.RwHps.abstractNetPacket.getChatMessagePacket(msg,"RELAY-CN",5))
    }
}