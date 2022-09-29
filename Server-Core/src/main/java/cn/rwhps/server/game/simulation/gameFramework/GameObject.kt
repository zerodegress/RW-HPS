/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.game.simulation.gameFramework

//import cn.rwhps.server.game.simulation.units.OrderableUnit
import cn.rwhps.server.game.simulation.SyncedObject
import cn.rwhps.server.struct.Seq
import cn.rwhps.server.util.log.Log
import java.util.concurrent.ConcurrentLinkedQueue
//import cn.rwhps.server.game.simulation.units.Unit as Unit1


abstract class GameObject() : SyncedObject {

    @JvmField var deleted = false
    @JvmField var drawLayer = 2
    @JvmField val id = getNextUnitId()

    init {
        gameObjectList.add(this)
        fastGameObjectList.add(this)
    }

    abstract fun draw(f: Float)

    abstract fun drawIcon(f: Float): Boolean

    abstract fun drawInterface(f: Float)

    abstract fun drawOver(f: Float)

    abstract fun drawUnder(f: Float)

    abstract fun update(f: Float)

    open fun remove() {
        gameObjectList.remove(this)
        fastGameObjectList.remove(this)
        this.deleted = true
    }

    companion object {
        val gameObjectList = ConcurrentLinkedQueue<GameObject>()
        val fastGameObjectList = Seq<GameObject>()
        private var unitId = 0L

        fun getFromId(id: Long): GameObject? {
            if (id == -1L) {
                return null
            }

            var result: GameObject? = null

            fastGameObjectList.eachFind( { it.id == id }) { gameObject: GameObject -> result = gameObject }

            if (result == null) {
                Log.error("ReportDesync","getFromId:$id was not found")
                return null
            }
            return result
        }

/*
        fun getUnitFromId(id: Long): Unit1? {
            return getFromId(id) as Unit1?
        }

        fun getOrderableUnitFromId(id: Long): OrderableUnit? {
            return getFromId(id) as OrderableUnit?
        }*/

        fun getNextUnitId(): Long {
            return unitId++
        }
    }
}