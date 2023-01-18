/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.game.simulation.core

import net.rwhps.server.io.packet.Packet
import net.rwhps.server.util.alone.annotations.GameSimulationLayer
import net.rwhps.server.util.log.exp.ImplementedException

interface AbstractGameData {
    fun checkHess(name: String): Boolean

    @GameSimulationLayer.GameSimulationLayer_KeyWords("gameSave")
    fun getGameData(): Packet

    @GameSimulationLayer.GameSimulationLayer_KeyWords("30")
    fun getGameCheck(): Packet

    @GameSimulationLayer.GameSimulationLayer_KeyWords("checkSumSize!")
    fun verifyGameSync(packet: Packet): Boolean

    @GameSimulationLayer.GameSimulationLayer_KeyWords("is victorious!")
    fun getWin(team: Int): Boolean

    @GameSimulationLayer.GameSimulationLayer_KeyWords("aiDifficulty is locked")
    fun getPlayerBirthPointXY()


    @GameSimulationLayer.GameSimulationLayer_KeyWords("exited!")
    fun clean()

    fun getDefPlayerData(): AbstractPlayerData

    @Throws(ImplementedException.PlayerImplementedException::class)
    fun getPlayerData(site: Int): AbstractPlayerData
}