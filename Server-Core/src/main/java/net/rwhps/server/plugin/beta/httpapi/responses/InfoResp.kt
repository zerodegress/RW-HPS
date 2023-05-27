package net.rwhps.server.plugin.beta.httpapi.responses

import net.rwhps.server.data.global.Data
import net.rwhps.server.data.global.NetStaticData

data class InfoResp(
    val isRunning: Boolean = NetStaticData.netService.size > 0,
    val serverPort: Int = Data.config.Port,
    val online: Int = Data.game.playerManage.playerGroup.size,
    val maxOnline: Int = Data.configServer.MaxPlayer,
    val serverMap: String = Data.game.maps.mapName,
    val serverSubtitle: String = Data.config.Subtitle,
    val serverName: String = Data.config.ServerName,
    val needPassword: Boolean = Data.game.passwd.isNotBlank(),
    val gameStarted: Boolean = Data.game.isStartGame
)