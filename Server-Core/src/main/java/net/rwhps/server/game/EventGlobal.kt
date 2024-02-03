/*
 * Copyright 2020-2024 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.game

import net.rwhps.server.core.thread.Threads
import net.rwhps.server.game.manage.HeadlessModuleManage
import net.rwhps.server.data.global.Data
import net.rwhps.server.data.global.NetStaticData
import net.rwhps.server.game.player.PlayerHess
import net.rwhps.server.util.file.plugin.PluginManage
import net.rwhps.server.game.event.core.EventListenerHost
import net.rwhps.server.game.event.global.NetConnectNewEvent
import net.rwhps.server.game.event.global.ServerHessLoadEvent
import net.rwhps.server.game.event.global.ServerLoadEvent
import net.rwhps.server.game.event.global.ServerStartTypeEvent
import net.rwhps.server.net.Administration
import net.rwhps.server.net.NetService
import net.rwhps.server.net.api.WebGetRelayInfo
import net.rwhps.server.net.core.IRwHps
import net.rwhps.server.net.handler.tcp.StartHttp
import net.rwhps.server.net.http.WebData
import net.rwhps.server.util.CLITools
import net.rwhps.server.util.Time
import net.rwhps.server.util.annotations.core.EventListenerHandler


/**
 * @author Dr (dr@der.kim)
 */
@Suppress("UNUSED", "UNUSED_PARAMETER")
class EventGlobal: EventListenerHost {
    @EventListenerHandler
    fun registerServerHessLoadEvent(serverHessLoadEvent: ServerHessLoadEvent) {
        if (serverHessLoadEvent.loadID == HeadlessModuleManage.hpsLoader) {
            // 不支持多端 :(
            // 多端请自行兼容
            serverHessLoadEvent.gameModule.eventManage.registerListener(Event())
            PluginManage.runRegisterEvents(serverHessLoadEvent.loadID, serverHessLoadEvent.gameModule.eventManage)

            PluginManage.runRegisterServerClientCommands(serverHessLoadEvent.gameModule.room.clientHandler)
        }
    }

    @EventListenerHandler
    fun registerServerLoadEvent(serverLoadEvent: ServerLoadEvent) {
        Data.core.admin.addChatFilter(object: Administration.ChatFilter {
            override fun filter(player: PlayerHess, message: String?): String? {
                if (player.muteTime > Time.millis()) {
                    return null
                }
                return message
            }
        })
    }

    @EventListenerHandler
    fun registerServerStartTypeEvent(serverStartTypeEvent: ServerStartTypeEvent) {
        when (serverStartTypeEvent.serverNetType) {
            IRwHps.NetType.ServerProtocol, IRwHps.NetType.ServerProtocolOld, IRwHps.NetType.ServerTestProtocol -> {
            }
            IRwHps.NetType.RelayProtocol, IRwHps.NetType.RelayMulticastProtocol -> {
                if (Data.config.autoUpList) {
                    Data.SERVER_COMMAND.handleMessage("uplist add", Data.defPrint)
                }

                Threads.newThreadCore {
                    val webData = WebData()
                    webData.addWebGetInstance("/api/getRelayInfo", WebGetRelayInfo())
                    NetService(StartHttp::class.java).setWebData(webData).openPort(4994)
                }
            }
            else -> {}
        }

        if (Data.config.cmdTitle.isBlank()) {
            CLITools.setConsoleTitle("[RW-HPS] Port: ${Data.config.port}, Run Server: ${NetStaticData.ServerNetType.name}")
        } else {
            CLITools.setConsoleTitle(Data.config.cmdTitle)
        }
    }

    @EventListenerHandler
    fun registerNetConnectNewEvent(netConnectNewEvent: NetConnectNewEvent) {
        if (Data.core.admin.bannedIP24.contains(netConnectNewEvent.connectionAgreement.ipLong24)) {
            netConnectNewEvent.result = true
        }
    }
}