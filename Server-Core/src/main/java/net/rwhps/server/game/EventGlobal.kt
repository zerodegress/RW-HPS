/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.game

import net.rwhps.server.core.Initialization
import net.rwhps.server.data.MapManage
import net.rwhps.server.data.global.Data
import net.rwhps.server.data.player.Player
import net.rwhps.server.net.Administration
import net.rwhps.server.net.core.IRwHps
import net.rwhps.server.plugin.event.AbstractGlobalEvent
import net.rwhps.server.util.Time
import net.rwhps.server.util.log.Log


/**
 * @author RW-HPS/Dr
 */
class EventGlobal : AbstractGlobalEvent {
    override fun registerServerLoadEvent() {
        Data.core.admin.addChatFilter(object : Administration.ChatFilter {
            override fun filter(player: Player, message: String?): String? {
                if (player.muteTime > Time.millis()) {
                    return null
                }
                return message
            }
        })

        try {
            MapManage.checkMaps()
            Log.clog(Data.i18NBundle.getinput("server.load.maps"))
        } catch (exp: Exception) {
            Log.debug("Read Error", exp)
        }

        Initialization.loadService()
    }

    override fun registerServerStartTypeEvent(serverNetType: IRwHps.NetType) {
        if (serverNetType == IRwHps.NetType.RelayProtocol || serverNetType == IRwHps.NetType.RelayMulticastProtocol) {
            if (Data.config.AutoUpList) {
                Data.SERVER_COMMAND.handleMessage("uplist add", Data.defPrint)
            }
        }
    }
}