/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.game.simulation.core

import net.rwhps.server.data.event.GameOverData
import net.rwhps.server.data.player.Player
import net.rwhps.server.io.packet.Packet
import net.rwhps.server.util.alone.annotations.GameSimulationLayer
import net.rwhps.server.util.log.exp.ImplementedException

interface AbstractGameHessData {

    @GameSimulationLayer.GameSimulationLayer_KeyWords("got remoteSyncFrame for")
    val tickHess: Int
    val tickNetHess: Int

    var useMod: Boolean

    @GameSimulationLayer.GameSimulationLayer_KeyWords("gameSave")
    fun getGameData(fastSync: Boolean = false): Packet

    @GameSimulationLayer.GameSimulationLayer_KeyWords("30")
    fun getGameCheck(): Packet

    @GameSimulationLayer.GameSimulationLayer_KeyWords("checkSumSize!")
    fun verifyGameSync(player: Player, packet: Packet): Boolean

    /**
     * 获取位子上玩家是否存活
     * 
     * @param position Position
     * @return Boolean
     */
    @GameSimulationLayer.GameSimulationLayer_KeyWords("is victorious!")
    fun getWin(position: Int): Boolean

    /**
     * 获取服务器Gameover信息
     *
     * @param team Team
     * @return Boolean
     */
    @GameSimulationLayer.GameSimulationLayer_KeyWords("is victorious!")
    fun getGameOverData(): GameOverData?

    @GameSimulationLayer.GameSimulationLayer_KeyWords("aiDifficulty is locked")
    fun getPlayerBirthPointXY()


    @GameSimulationLayer.GameSimulationLayer_KeyWords("exited!")
    fun clean()

    fun getDefPlayerData(): AbstractPlayerData {
        return object: AbstractPlayerData {
            private val error: ()->Nothing get() = throw ImplementedException.PlayerImplementedException("[Player] No Bound PlayerData")

            override fun updateDate() {}
            override val survive get() = error()
            override val unitsKilled get() = error()
            override val buildingsKilled get() = error()
            override val experimentalsKilled get() = error()
            override val unitsLost get() = error()
            override val buildingsLost get() = error()
            override val experimentalsLost get() = error()
            override var credits: Int = 0
            override val name get() = error()
            override val connectHexID get() = error()
            override var site = 0
            override var team = 0
            override var startUnit = 0
            override var color = 0
        }
    }

    @Throws(ImplementedException.PlayerImplementedException::class)
    fun getPlayerData(site: Int): AbstractPlayerData
}