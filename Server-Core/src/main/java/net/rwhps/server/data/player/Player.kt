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
import net.rwhps.server.data.plugin.Value
import net.rwhps.server.net.netconnectprotocol.realize.GameVersionServer
import net.rwhps.server.struct.ObjectMap
import net.rwhps.server.util.I18NBundle
import net.rwhps.server.util.Time

/**
 *
 * @author RW-HPS/Dr
 */
class Player(
    var conServer: GameVersionServer?,
    /** Player connection UUID  */
    @JvmField val uuid: String,
    /** Player name  */
    override val name: String,
    /**   */
    override val i18NBundle: I18NBundle,
) : AbstractPlayer(conServer, i18NBundle) {
    var headlessDevice = checkHess(uuid)

    /** Team number  */
    override var team = 0
        set(value) { watch = (value == -3) ; field = value }
    /** List position  */
    override var site = 0
        set(value) { color = value; field = value }

    /** */
    override var startUnit = Data.game.initUnit
    /** */
    var color = 0
        set(value) {
            field = value % 10
        }
    /** Ping */
    var ping = 50

    /** (Markers)  */
    @Volatile var start = false
    var watch = false
        private set

    /** 点石成金 */
    var turnStoneIntoGold = Data.config.Turnstoneintogold

    var syncAllSumFlag = false
    var lastSyncTick = -999

    /** Shared control  */
    @Volatile var sharedControl = false
    val controlThePlayer: Boolean
        get() {
            return sharedControl || if (con == null || Time.concurrentSecond()-lastMoveTime > 120) {
                true
            } else {
                conServer!!.isDis
            }
        }

    var lastVoteTime: Int = 0

    private val customData = ObjectMap<String, Value<*>>()

    // 买得起吗
    fun canBuy(price: Int): Boolean {
        return credits >= price
    }


    fun isEnemy(other: Player): Boolean {
        return this.team != other.team
    }

    companion object {
        @JvmStatic
        fun checkHess(uuid: String): Boolean {
            return uuid == Data.core.serverHessUuid
        }
    }
}