/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.plugin.event

import net.rwhps.server.data.event.GameOverData
import net.rwhps.server.data.player.AbstractPlayer
import net.rwhps.server.game.GameUnitType

/**
 * Hess服务器的事件接口
 *
 * @author RW-HPS/Dr
 */
interface AbstractEvent {
    /**
     * Hess服务器完成开放 [同步-Synchronization]
     */
    fun registerServerHessStartPort() { /* Optional use of plugins */ }

    /**
     * 玩家加入 [同步-Synchronization]
     * @param player Player
     */
    fun registerPlayerJoinEvent(player: AbstractPlayer) { /* Optional use of plugins */ }

    /**
     * 玩家离开时 [异步-ASync]
     * @param player Player
     */
    fun registerPlayerLeaveEvent(player: AbstractPlayer) { /* Optional use of plugins */ }

    /**
     * 玩家发言时 [异步-ASync]
     * @param player
     * @param message
     */
    fun registerPlayerChatEvent(player: AbstractPlayer, message: String) { /* Optional use of plugins */ }

    /** 开始游戏 [同步-ASync]  */
    fun registerGameStartEvent() { /* Optional use of plugins */ }

    /** 结束游戏 [同步-ASync]  */
    fun registerGameOverEvent(gameOverData: GameOverData?) { /* Optional use of plugins */ }

    /** 玩家被ban [异步-ASync]  */
    fun registerPlayerBanEvent(player: AbstractPlayer) { /* Optional use of plugins */ }

    /** 玩家被解除ban [异步-ASync]  */
    fun registerPlayerUnbanEvent(player: AbstractPlayer) { /* Optional use of plugins */ }

    /** 玩家被banIp [异步-ASync]  */
    fun registerPlayerIpBanEvent(player: AbstractPlayer) { /* Optional use of plugins */ }

    /** 玩家被解banIp [异步-ASync]  */
    fun registerPlayerIpUnbanEvent(ip: String) { /* Optional use of plugins */ }

    /** 玩家建造/操作单位，返回false忽略该操作 [同步-Sync]  */
    fun registerPlayerOperationUnitEvent(player: AbstractPlayer, gameActions: GameUnitType.GameActions, gameUnits: GameUnitType.GameUnits, x: Float, y: Float): Boolean {
        /* Optional use of plugins */
        return true
    }
}