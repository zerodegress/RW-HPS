package cn.rwhps.server.game.simulation.gameFramework

import cn.rwhps.server.io.packet.Packet

interface BaseGameData {
    fun getGameData(): Packet

    fun getGameCheck(): Packet

    fun getWin(team: Int): Boolean

    fun clean()
}