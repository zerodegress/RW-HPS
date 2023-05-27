/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.data.player

import net.rwhps.server.data.global.Data
import net.rwhps.server.game.simulation.core.AbstractPlayerData
import net.rwhps.server.net.core.server.AbstractNetConnectServer
import net.rwhps.server.struct.Seq
import net.rwhps.server.util.I18NBundle

/**
 * @author RW-HPS/Dr
 */
class PlayerHessManage {
    /** Online players   */
    @JvmField
    val playerGroup = Seq<AbstractPlayer>(16,true)

    /** ALL players  */
    @JvmField
    val playerAll = Seq<AbstractPlayer>(16,true)

    fun addAbstractPlayer(con: AbstractNetConnectServer, playerData: AbstractPlayerData, i18NBundle: I18NBundle = Data.i18NBundle): AbstractPlayer {
        var player: AbstractPlayer? = null
        var resultFlag = true

        playerAll.eachAllFind({ i: AbstractPlayer -> i.connectHexID == playerData.connectHexID }) { e: AbstractPlayer ->
            e.con = con
            resultFlag = false
            player = e
        }

        if (resultFlag) {
            player = AbstractPlayer(con, i18NBundle, playerData)
            if (Data.configServer.OneAdmin) {
                var hasAutoAdmin = false
                playerGroup.eachFind({ it.isAdmin && it.autoAdmin }) { hasAutoAdmin = true }

                if (!hasAutoAdmin) {
                    player!!.isAdmin = true
                    player!!.autoAdmin = true
                }
            }

            if (Data.core.admin.isAdmin(player!!)) {
                val data = Data.core.admin.playerAdminData.get(player!!.connectHexID)
                player!!.isAdmin = data.admin
                player!!.superAdmin = data.superAdmin
            }

            playerAll.add(player!!)
        }

        playerGroup.add(player!!)
        return player!!
    }

    fun runPlayerArrayDataRunnable(run: (AbstractPlayer) -> Unit) {
        for (player in playerAll) {
            run(player)
        }
    }

    fun getPlayersNameOnTheSameTeam(team: Int): Seq<String> {
        val result = Seq<String>()
        for (player in playerAll) {
            if (player.team == team) {
                result.add(player.name)
            }
        }
        return result
    }

    fun getPlayerArray(site: Int): AbstractPlayer? {
        for (player in playerAll) {
            if (player.site == site) {
                return player
            }
        }
        return null
    }

    fun cleanPlayerAllData() {
        playerGroup.eachAll { it.kickPlayer("CleanAllPlayer") }
        playerAll.eachAll {
            try {
                it.kickPlayer("CleanAllPlayer")
            } catch (_: Exception) {
                // kick
            }
        }

        playerAll.clear()
        playerGroup.clear()
    }
}