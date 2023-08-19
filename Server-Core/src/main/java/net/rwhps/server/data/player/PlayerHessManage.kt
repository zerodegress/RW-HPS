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
import net.rwhps.server.func.StrCons
import net.rwhps.server.game.simulation.core.AbstractPlayerData
import net.rwhps.server.net.core.server.AbstractNetConnectServer
import net.rwhps.server.struct.Seq
import net.rwhps.server.util.I18NBundle
import net.rwhps.server.util.IsUtils

/**
 * Hess 下的玩家管理器
 *
 * 不需要用户手动创建, 在每个 Hess Server自带.
 *
 * RW-HPS 单例下使用(java) :
 * HessModuleManage.INSTANCE.getHps().getRoom().getPlayerManage()
 *
 * @author RW-HPS/Dr
 */
class PlayerHessManage {
    /** Online players   */
    @JvmField
    val playerGroup = Seq<PlayerHess>(16, true)

    /** ALL players  */
    @JvmField
    val playerAll = Seq<PlayerHess>(16, true)

    fun addAbstractPlayer(
        con: AbstractNetConnectServer, playerData: AbstractPlayerData, i18NBundle: I18NBundle = Data.i18NBundle
    ): PlayerHess {
        var player: PlayerHess? = null
        var resultFlag = true

        playerAll.eachAllFind({ i: PlayerHess -> i.connectHexID == playerData.connectHexID }) { e: PlayerHess ->
            e.con = con
            resultFlag = false
            player = e
        }

        if (resultFlag) {
            player = PlayerHess(con, i18NBundle, playerData)
            if (Data.configServer.oneAdmin) {
                var hasAutoAdmin = false
                playerGroup.eachFind({ it.isAdmin && it.autoAdmin }) { hasAutoAdmin = true }

                if (!hasAutoAdmin) {
                    player!!.isAdmin = true
                    player!!.autoAdmin = true
                }
            }

            if (Data.core.admin.isAdmin(player!!)) {
                val data = Data.core.admin.playerAdminData[player!!.connectHexID]!!
                player!!.isAdmin = data.admin
                player!!.superAdmin = data.superAdmin
            }

            playerAll.add(player!!)
        }

        playerGroup.add(player!!)
        return player!!
    }

    fun findPlayer(writeConsole: StrCons, findIn: String): PlayerHess? {
        var conTg: PlayerHess? = null

        var findNameIn: String? = null
        var findPositionIn: Int? = null

        if (IsUtils.isNumeric(findIn)) {
            findPositionIn = findIn.toInt() - 1
        } else {
            findNameIn = findIn
        }

        findNameIn?.let { findName ->
            var count = 0
            playerGroup.eachAll {
                if (it.name.contains(findName, ignoreCase = true)) {
                    conTg = it
                    count++
                }
            }
            if (count > 1) {
                writeConsole("目标不止一个, 请不要输入太短的玩家名")
                return@let
            }
            if (conTg == null) {
                writeConsole("找不到玩家")
                return@let
            }
        }

        findPositionIn?.let { findPosition ->
            playerGroup.eachAll {
                if (it.site == findPosition) {
                    conTg = it
                }
            }
            if (conTg == null) {
                writeConsole("找不到玩家")
                return@let
            }
        }

        return conTg
    }

    fun runPlayerArrayDataRunnable(run: (PlayerHess) -> Unit) {
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

    fun getPlayerArray(site: Int): PlayerHess? {
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