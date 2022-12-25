package net.rwhps.server.game.simulation.pivatedata

import com.corrodinggames.rts.game.n
import net.rwhps.server.game.simulation.gameFramework.GameEngine
import net.rwhps.server.util.WaitResultUtil
import net.rwhps.server.util.alone.annotations.GameSimulationLayer
import net.rwhps.server.util.log.Log
import net.rwhps.server.util.log.exp.ImplementedException

internal open class PrivateClass_Player(private val playerData: n) {
    @GameSimulationLayer.GameSimulationLayer_KeyWords("is victorious!")
    open val survive get() = (!playerData.b() && !playerData.G && !playerData.F && !playerData.E)

    @GameSimulationLayer.GameSimulationLayer_KeyWords("Units Killed")
    private val gameStatistics = GameEngine.gameStatistics.a(playerData)
    /** 单位击杀数 */
    open val unitsKilled get() = gameStatistics.c
    /** 建筑毁灭数 */
    open val buildingsKilled get() = gameStatistics.d
    /** 单实验单位击杀数 */
    open val experimentalsKilled get() = gameStatistics.e
    /** 单位被击杀数 */
    open val unitsLost get() = gameStatistics.f
    /** 建筑被毁灭数 */
    open val buildingsLost get() = gameStatistics.g
    /** 单实验单位被击杀数 */
    open val experimentalsLost get() = gameStatistics.h

    /** 玩家的资金 */
    var credits
        get() = playerData.o.toInt()
        set(value) { playerData.o = value.toDouble()}

    companion object {
        @Throws(ImplementedException.PlayerImplementedException::class)
        internal fun getPlayerData(site: Int): PrivateClass_Player =
            PrivateClass_Player(WaitResultUtil.waitResult { n.k(site) } ?: throw ImplementedException.PlayerImplementedException("[PlayerData-New] Player is invalid"))

        internal val initValue = object: PrivateClass_Player(n.k(-1)) {
            private val error: ()->Nothing get() = throw ImplementedException.PlayerImplementedException("[Player] No Bound PlayerData")
            override val survive get() = error()
            override val unitsKilled get() = error()
            override val buildingsKilled get() = error()
            override val experimentalsKilled get() = error()
            override val unitsLost get() = error()
            override val buildingsLost get() = error()
            override val experimentalsLost get() = error()
        }
    }
}