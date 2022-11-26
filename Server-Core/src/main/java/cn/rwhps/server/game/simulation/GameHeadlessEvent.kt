/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.game.simulation

import cn.rwhps.server.core.Call
import cn.rwhps.server.core.thread.CallTimeTask
import cn.rwhps.server.core.thread.Threads
import cn.rwhps.server.data.player.Player
import cn.rwhps.server.game.simulation.gameFramework.GameData
import cn.rwhps.server.game.simulation.gameFramework.GameNet
import cn.rwhps.server.game.simulation.pivatedata.PrivateClass_Player
import cn.rwhps.server.plugin.event.AbstractEvent
import cn.rwhps.server.util.log.Log
import java.util.concurrent.TimeUnit

class GameHeadlessEvent : AbstractEvent {
    override fun registerGameStartEvent() {
        Threads.newTimedTask(CallTimeTask.CallCheckTask,0,2, TimeUnit.SECONDS,Call::sendCheckData)
    }

    override fun registerGameOverEvent() {
        Threads.closeTimeTask(CallTimeTask.CallCheckTask)
        Log.debug("Stop CallCheckTask")

        Call.killAllPlayer()

        GameData.clean()
        Log.clog("Stop GameHeadless")
        GameNet.newConnect()
        Log.clog("ReRun GameHeadless")
    }

    override fun registerPlayerJoinEvent(player: Player) {
        player.playerPrivateData = PrivateClass_Player.getPlayerData(player.site)
    }
}