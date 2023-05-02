/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.data.plugin

import net.rwhps.server.game.event.EventGlobalType.*
import net.rwhps.server.game.event.EventType.*
import net.rwhps.server.plugin.event.AbstractEvent
import net.rwhps.server.plugin.event.AbstractGlobalEvent
import net.rwhps.server.struct.Seq
import net.rwhps.server.util.game.Events
import net.rwhps.server.util.threads.GetNewThreadPool

/**
 * New plugin event manager
 * @author RW-HPS/Dr
 */
internal class PluginEventManage {
    companion object {
        private val pluginEventData = Seq<AbstractEvent>(8)
        private val pluginGlobalEventData = Seq<AbstractGlobalEvent>(8)
        private val executorService = GetNewThreadPool.getNewFixedThreadPool(5, "PluginEventASync-")
        @JvmStatic
        fun add(abstractEvent: AbstractEvent) {
            pluginEventData.add(abstractEvent)
        }
        @JvmStatic
        fun add(abstractGlobalEvent: AbstractGlobalEvent) {
            pluginGlobalEventData.add(abstractGlobalEvent)
        }

        private fun registerEventAll() {
            /* Sync */
            Events.add(ServerHessStartPort::class.java) {
                pluginEventData.eachAll(AbstractEvent::registerServerHessStartPort)
            }

            /* Sync */
            Events.add(PlayerJoinEvent::class.java) { e: PlayerJoinEvent ->
                pluginEventData.eachAll { p: AbstractEvent ->
                    // 当前一个断开了链接 那么没必要执行后面的事件
                    if (e.player.con == null) {
                        return@eachAll
                    }
                    p.registerPlayerJoinEvent(e.player)
                }
            }
            /* Sync */
            Events.add(PlayerLeaveEvent::class.java) { e: PlayerLeaveEvent ->
                pluginEventData.eachAll { p: AbstractEvent ->
                    p.registerPlayerLeaveEvent(e.player)
                }
            }
            /* ASync */
            Events.add(PlayerChatEvent::class.java) { e: PlayerChatEvent ->
                executorService.execute {
                    pluginEventData.eachAll { p: AbstractEvent ->
                        p.registerPlayerChatEvent(e.player, e.message)
                    }
                }
            }
            /* Sync */
            Events.add(GameStartEvent::class.java) {
                pluginEventData.eachAll(AbstractEvent::registerGameStartEvent)
            }
            /* Sync */
            Events.add(GameOverEvent::class.java) { e: GameOverEvent ->
                pluginEventData.eachAll { p: AbstractEvent ->
                    p.registerGameOverEvent(e.gameOverData)
                }

            }
            /* ASync */
            Events.add(PlayerBanEvent::class.java) { e: PlayerBanEvent ->
                executorService.execute {
                    pluginEventData.eachAll { p: AbstractEvent ->
                        p.registerPlayerBanEvent(e.player)
                    }
                }
            }
            /* ASync */
            Events.add(PlayerUnbanEvent::class.java) { e: PlayerUnbanEvent ->
                executorService.execute {
                    pluginEventData.eachAll { p: AbstractEvent ->
                        p.registerPlayerUnbanEvent(e.player)
                    }
                }
            }
            /* ASync */
            Events.add(PlayerIpBanEvent::class.java) { e: PlayerIpBanEvent ->
                executorService.execute {
                    pluginEventData.eachAll { p: AbstractEvent ->
                        p.registerPlayerIpBanEvent(e.player)
                    }
                }
            }
            /* ASync */
            Events.add(PlayerIpUnbanEvent::class.java) { e: PlayerIpUnbanEvent ->
                executorService.execute {
                    pluginEventData.eachAll { p: AbstractEvent ->
                        p.registerPlayerIpUnbanEvent(e.ip)
                    }
                }
            }
            /* Sync */
            Events.add(PlayerOperationUnitEvent::class.java) { e: PlayerOperationUnitEvent ->
                pluginEventData.eachAll { p: AbstractEvent ->
                    e.resultStatus = p.registerPlayerOperationUnitEvent(e.player, e.gameActions, e.gameUnits, e.x, e.y)
                }
            }
        }

        private fun registerGlobalEventAll() {
            /* Sync */
            Events.add(GameLibLoadEvent::class.java) { e: GameLibLoadEvent ->
                pluginGlobalEventData.eachAll { obj: AbstractGlobalEvent ->
                    obj.registerGameLibLoadEvent(e.loadID)
                }
            }

            /* Sync */
            // 不应该 ASync 避免部分配置未加载
            Events.add(ServerLoadEvent::class.java) {
                pluginGlobalEventData.eachAll(AbstractGlobalEvent::registerServerLoadEvent)
            }

            Events.add(ServerStartTypeEvent::class.java) { e: ServerStartTypeEvent ->
                pluginGlobalEventData.eachAll { obj: AbstractGlobalEvent ->
                    obj.registerServerStartTypeEvent(e.serverNetType)
                }
            }

            /* Sync */
            Events.add(NewConnectEvent::class.java) { e: NewConnectEvent ->
                pluginGlobalEventData.eachAll { obj: AbstractGlobalEvent ->
                    if (obj.registerNewConnectEvent(e.connectionAgreement)) {
                        e.result = true
                    }
                }
            }

            /* Sync */
            Events.add(NewCloseEvent::class.java) { e: NewCloseEvent ->
                pluginGlobalEventData.eachAll { obj: AbstractGlobalEvent ->
                    obj.registerNewCloseEvent(e.connectionAgreement)
                }
            }
        }
    }

    init {
        registerEventAll()
        registerGlobalEventAll()
    }
}