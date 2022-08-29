/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.struct

import it.unimi.dsi.fastutil.longs.Long2IntMap
import it.unimi.dsi.fastutil.longs.Long2IntMaps
import it.unimi.dsi.fastutil.longs.Long2IntOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectSet

class LongIntMap @JvmOverloads constructor(capacity: Int, threadSafety: Boolean = false) : MutableMap<Long,Int> {
    private val map: BaseLongMap = Long2IntOpenHashMap(capacity).let { if (threadSafety) ThreadSafety((Long2IntMaps.synchronize(it, it) as Long2IntMaps.SynchronizedMap), it) else ThreadUnsafe(it) }

    override val size: Int get() = map.size
    override fun get(key: Long): Int = map.get(key)
    override fun put(key: Long, value: Int): Int = map.put(key, value)
    override fun putAll(from: Map<out Long, Int>) = map.putAll(from)
    override val entries: MutableSet<MutableMap.MutableEntry<Long, Int>> get() = map.entries
    override val keys: MutableSet<Long> get() = map.keys
    override val values: MutableCollection<Int> get() = map.values
    override fun remove(key: Long): Int = map.remove(key)
    override fun containsValue(value: Int): Boolean = map.containsValue(value)
    override fun containsKey(key: Long): Boolean = map.containsKey(key)
    override fun isEmpty(): Boolean = map.isEmpty()
    override fun clear() = map.clear()
    fun toArrayKey(): Seq<Long> =  map.toArrayKey()
    fun toArrayValues(): Seq<Int> =  map.toArrayValues()

    companion object {
        private abstract class BaseLongMap(private val map: Long2IntMap) {
            val size: Int get() = map.size

            fun get(key: Long): Int = map.get(key)
            fun put(key: Long, value: Int): Int = map.put(key, value)
            fun putAll(from: Map<out Long, Int>) = map.putAll(from)

            @Suppress("UNCHECKED_CAST")
            val entries: MutableSet<MutableMap.MutableEntry<Long, Int>> get() = map.long2IntEntrySet() as ObjectSet<MutableMap.MutableEntry<Long, Int>>
            val keys: MutableSet<Long> get() = map.keys
            val values: MutableCollection<Int> get() = map.values
            fun remove(key: Long): Int = map.remove(key)
            fun containsValue(value: Int): Boolean = map.containsValue(value)
            fun containsKey(key: Long): Boolean = map.containsKey(key)
            fun isEmpty(): Boolean = map.isEmpty()

            fun clear() = map.clear()
            fun toArrayKey(): Seq<Long> =  Seq<Long>(size).also { keys.forEach { value-> it.add(value) } }
            fun toArrayValues(): Seq<Int> =  Seq<Int>(size).also { values.forEach { value-> it.add(value) } }
        }

        private class ThreadUnsafe(map: Long2IntOpenHashMap): BaseLongMap(map)
        private class ThreadSafety(map: Long2IntMaps.SynchronizedMap, private val lock: Long2IntMap): BaseLongMap(map)
    }
}