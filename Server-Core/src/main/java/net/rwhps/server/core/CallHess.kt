/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.core

import net.rwhps.server.data.global.ServerRoom
import net.rwhps.server.data.player.AbstractPlayer

class CallHess(private val serverRoom: ServerRoom) {
    fun sendSystemMessage(text: String) {
        serverRoom.playerManage.playerGroup.eachAll { e: AbstractPlayer -> e.sendSystemMessage(text) }
    }

    fun sendSystemMessageLocal(text: String, vararg obj: Any) {
        serverRoom.playerManage.playerGroup.eachAll { e: AbstractPlayer -> e.sendSystemMessage(e.i18NBundle.getinput(text, *obj)) }
    }

    fun sendSystemMessage(text: String, vararg obj: Any) {
        serverRoom.playerManage.playerGroup.eachAll { e: AbstractPlayer -> e.sendSystemMessage(e.i18NBundle.getinput(text, *obj)) }
    }
}