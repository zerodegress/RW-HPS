package net.rwhps.server.plugin.beta.httpapi.responses

import net.rwhps.server.data.global.Data
import net.rwhps.server.data.player.Player

data class GameInfoResp(
    val income: Float = Data.game.income,
    val noNukes: Boolean = Data.game.noNukes,
    val credits: Int = Data.game.credits,
    val sharedControl: Boolean = Data.game.sharedControl,
    val players: List<Player> = Data.game.playerManage.playerAll.toList()
)