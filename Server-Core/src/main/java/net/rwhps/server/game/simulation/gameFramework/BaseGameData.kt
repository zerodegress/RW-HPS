package net.rwhps.server.game.simulation.gameFramework

import net.rwhps.server.io.packet.Packet

interface BaseGameData {
    fun getGameData(): Packet

    fun getGameCheck(): Packet

    fun getWin(team: Int): Boolean

    fun clean()
}