/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.plugin.internal.hess.service.event

import net.rwhps.server.core.Call
import net.rwhps.server.core.thread.CallTimeTask
import net.rwhps.server.core.thread.Threads
import net.rwhps.server.data.HessModuleManage
import net.rwhps.server.data.event.GameOverData
import net.rwhps.server.data.global.Data
import net.rwhps.server.plugin.event.AbstractEvent
import net.rwhps.server.util.log.Log
import java.util.concurrent.TimeUnit

/**
 * @author RW-HPS/Dr
 */
class GameHeadlessEvent : AbstractEvent {
    override fun registerGameStartEvent() {
        Data.game.playerManage.playerAll.eachAll { player ->
            player.playerPrivateData = HessModuleManage.hps.gameHessData.getPlayerData(player.site)
        }

        Threads.newTimedTask(CallTimeTask.CallCheckTask,0,2, TimeUnit.SECONDS,Call::sendCheckData)
    }

    override fun registerGameOverEvent(gameOverData: GameOverData?) {
        Threads.closeTimeTask(CallTimeTask.CallCheckTask)
        Log.debug("Stop CallCheckTask")

        Call.killAllPlayer()

        HessModuleManage.hps.gameHessData.clean()
        Log.clog("Stop GameHeadless")
        HessModuleManage.hps.gameNet.newConnect()
        Log.clog("ReRun GameHeadless")
    }
}