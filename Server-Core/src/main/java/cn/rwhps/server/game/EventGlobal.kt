/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.game

import cn.rwhps.server.core.Initialization
import cn.rwhps.server.data.global.Data
import cn.rwhps.server.data.mods.ModManage
import cn.rwhps.server.data.player.Player
import cn.rwhps.server.net.Administration
import cn.rwhps.server.plugin.event.AbstractGlobalEvent
import cn.rwhps.server.util.Time
import cn.rwhps.server.util.log.Log


class EventGlobal : AbstractGlobalEvent {
    override fun registerGameLibLoadEvent() {
        /* Load Mod */
        Log.clog(Data.i18NBundle.getinput("server.loadMod", ModManage.load()))
        ModManage.loadUnits()

        Log.clog("Load Game Core END !")
    }

    override fun registerServerLoadEvent() {
        Data.core.admin.addChatFilter(object : Administration.ChatFilter {
            override fun filter(player: Player, message: String?): String? {
                if (player.muteTime > Time.millis()) {
                    return null
                }
                return message
            }
        })

        Initialization.loadService()
    }
}