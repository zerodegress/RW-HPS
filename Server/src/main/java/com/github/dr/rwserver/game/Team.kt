/*
 * Copyright 2020-2021 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.game

import com.github.dr.rwserver.data.Player
import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.util.IsUtil.isTwoTimes

/**
 * @author Dr
 */
object Team {
    fun autoPlayerTeam(player: Player) {
        if (Data.game.amTeam) {
            var i = 0
            val len = Data.game.maxPlayer
            while (i < len) {
                if (Data.game.playerData[i] == null) {
                    Data.game.playerData[i] = player
                    player.site = i
                    player.team = i
                    return
                }
                i++
            }
        } else {
            var i = 0
            val len = Data.game.maxPlayer
            while (i < len) {
                if (Data.game.playerData[i] == null) {
                    Data.game.playerData[i] = player
                    player.site = i
                    player.team = if (isTwoTimes(i + 1)) 1 else 0
                    return
                }
                i++
            }
        }
    }

    @JvmStatic
    fun amYesPlayerTeam() {
        var i = 0
        val len = Data.game.maxPlayer
        while (i < len) {
            if (Data.game.playerData[i] != null) {
                Data.game.playerData[i].team = i
            }
            i++
        }
    }

    @JvmStatic
    fun amNoPlayerTeam() {
        var i = 0
        val len = Data.game.maxPlayer
        while (i < len) {
            if (Data.game.playerData[i] != null) {
                Data.game.playerData[i].team = if (isTwoTimes(i + 1)) 1 else 0
            }
            i++
        }
    }
}