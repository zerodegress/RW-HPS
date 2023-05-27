/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.data.player

import net.rwhps.server.core.Call
import net.rwhps.server.data.global.Data
import net.rwhps.server.net.netconnectprotocol.realize.GameVersionServer
import net.rwhps.server.struct.Seq
import net.rwhps.server.util.I18NBundle
import net.rwhps.server.util.IsUtil
import java.util.*
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

/**
 * Player Manager
 *
 * @property maxPlayerSize Int
 * @property amTeam Boolean
 * @property sharedControlPlayer Int
 * @property playerGroup Seq<Player>
 * @property playerAll Seq<Player>
 * @property playerData Array<Player?>
 * @property moveLock ReentrantLock
 *
 * @author RW-HPS/Dr
 */
class PlayerManage(private val maxPlayerSize: Int) {
    /** 混战分配  */
    var amTeam = false

    /** 共享控制  */
    var sharedControlPlayer: Short = 0

    /** Online players   */
    @JvmField
    val playerGroup = Seq<Player>(16,true)

    /** ALL players  */
    @JvmField
    val playerAll = Seq<Player>(16,true)

    /** 队伍数据  */
    private val playerData = arrayOfNulls<Player?>(maxPlayerSize)
    /** 队伍切换锁 */
    private val moveLock = ReentrantLock(true)

    fun addPlayer(con: GameVersionServer, uuid: String, name: String, i18NBundle: I18NBundle = Data.i18NBundle): Player {
        val player = Player(con, uuid, name, i18NBundle)
        if (Data.configServer.OneAdmin) {
            if (!player.headlessDevice && playerGroup.size == 1) {
                player.isAdmin = true
            }
        }

        if (Data.core.admin.isAdmin(player)) {
            val data = Data.core.admin.playerAdminData.get(player.uuid)
            player.isAdmin = data.admin
            player.superAdmin = data.superAdmin
        }

        autoPlayerTeam(player)
        playerGroup.add(player)
        playerAll.add(player)
        return player
    }

    fun removePlayerArray(player: Player) {
        removePlayerArray(player.site)
    }
    fun removePlayerArray(size: Int) {
        moveLock.withLock {
            playerData[size] = null
        }
    }
    fun setPlayerArray(size: Int, player: Player?) {
        moveLock.withLock {
            playerData[size] = player

        }
    }
    fun getPlayerArray(size: Int): Player? {
        return playerData[size]
    }

    fun containsName(name: String): Player? {
        for (player in playerData) {
            if (player!= null && player.name == name) {
                return player
            }
        }
        return null
    }

    fun runPlayerArrayDataRunnable(skipHd: Boolean = false, run: (Player?) -> Unit) {
        for (player in playerData) {
            if (player != null && player.headlessDevice && skipHd) {
                // S K I P
            } else {
                run(player)
            }
        }
    }

    fun getPlayersNameOnTheSameTeam(team: Int): Seq<String> {
        val result = Seq<String>()
        for (player in playerData) {
            if (player!= null && player.team == team) {
                result.add(player.name)
            }
        }
        return result
    }

    /**
     * 计算 共享 控制校验和
     */
    fun updateControlIdentifier() {
        moveLock.withLock {
            var int3 = 0
            for (i in 0 until maxPlayerSize) {
                val player1 = playerData[i]
                if (player1 != null && player1.controlThePlayer) {
                    if (player1.controlThePlayer) {
                        int3 = int3 or (1 shl i)
                    }
                }
            }
            sharedControlPlayer = int3.toShort()
        }
    }

    fun cleanPlayerAllData() {
        playerGroup.eachAll { it.kickPlayer("CleanAllPlayer")}
        playerAll.eachAll { it.kickPlayer("CleanAllPlayer")}

        Arrays.fill(playerData, null)
        playerAll.clear()
        playerGroup.clear()
    }




    /* TEAM  */

    private fun autoPlayerTeam(player: Player) {
        moveLock.withLock {
            if (player.headlessDevice) {
                playerData[Data.configServer.MaxPlayer] = player
                player.site = Data.configServer.MaxPlayer
                player.team = -3
                player.start = true
                return
            }
            if (amTeam) {
                for (i in 0 until maxPlayerSize) {
                    if (playerData[i] == null) {
                        playerData[i] = player
                        player.site = i
                        player.team = i
                        return
                    }
                }
            } else {
                for (i in 0 until maxPlayerSize) {
                    if (playerData[i] == null) {
                        playerData[i] = player
                        player.site = i
                        player.team = if (IsUtil.isTwoTimes(i + 1)) 1 else 0
                        return
                    }
                }
            }
        }
    }

    fun amYesPlayerTeam() {
        moveLock.withLock {
            for (i in 0 until maxPlayerSize) {
                if (playerData[i] != null) {
                    playerData[i]!!.team = i
                }
            }
        }
    }

    fun amNoPlayerTeam() {
        moveLock.withLock {
            for (i in 0 until maxPlayerSize) {
                if (playerData[i] != null) {
                    playerData[i]!!.team = if (IsUtil.isTwoTimes(i + 1)) 1 else 0
                }
            }
        }
    }

    fun movePlayerPosition(oldLocationIn: Int, newLocationIn: Int, newTeam: Int, admin: Boolean = false) {
        moveLock.withLock {
            val oldIndex = oldLocationIn - 1
            val newIndex = newLocationIn - 1

            if (newIndex >= 0) {
                /* 位置不能过限 */
                if (oldIndex < maxPlayerSize && newIndex < maxPlayerSize) {
                    val od = getPlayerArray(oldIndex)
                    val nw = getPlayerArray(newIndex)
                    if (od == null) {
                        return
                    }
                    if (nw == null) {
                        removePlayerArray(oldIndex)
                        od.site = newIndex
                        if (newTeam > -1) {
                            od.team = newTeam
                        }
                        setPlayerArray(newIndex,od)
                    } else {
                        if (admin) {
                            od.site = newIndex
                            nw.site = oldIndex
                            if (newTeam > -1) {
                                od.team = newTeam
                            }
                            setPlayerArray(newIndex,od)
                            setPlayerArray(oldIndex,nw)
                        }
                    }
                    Call.sendTeamData()
                }
            } else if (newIndex == -3) {
                /* 观战 */
                val od = getPlayerArray(oldIndex) ?: return
                od.team = -3
                Call.sendTeamData()
            }
        }
    }
}