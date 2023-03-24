/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.struct

import it.unimi.dsi.fastutil.longs.Long2ObjectMap
import it.unimi.dsi.fastutil.longs.Long2ObjectMaps
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectSet

class LongMap<V> @JvmOverloads constructor(capacity: Int, threadSafety: Boolean = false) : MutableMap<Long,V> {
    private val map: BaseLongMap<V> = Long2ObjectOpenHashMap<V>(capacity).let { if (threadSafety) ThreadSafety((Long2ObjectMaps.synchronize<V>(it, it) as Long2ObjectMaps.SynchronizedMap<V>), it) else ThreadUnsafe(it) }

    override val size: Int get() = map.size
    override fun get(key: Long): V? = map.get(key)
    override fun put(key: Long, value: V): V? = map.put(key, value)
    override fun putAll(from: Map<out Long, V>) = map.putAll(from)
    override val entries: MutableSet<MutableMap.MutableEntry<Long, V>> get() = map.entries
    override val keys: MutableSet<Long> get() = map.keys
    override val values: MutableCollection<V> get() = map.values
    override fun remove(key: Long): V? = map.remove(key)
    override fun containsValue(value: V): Boolean = map.containsValue(value)
    override fun containsKey(key: Long): Boolean = map.containsKey(key)
    override fun isEmpty(): Boolean = map.isEmpty()
    override fun clear() = map.clear()
    fun toArrayKey(): Seq<Long> =  map.toArrayKey()
    fun toArrayValues(): Seq<V> =  map.toArrayValues()
    override fun toString(): String = map.toString()

    companion object {
        private abstract class BaseLongMap<V>(private val map: Long2ObjectMap<V>) {
            //Long2ObjectOpenHashMap
            val size: Int get() = map.size

            fun get(key: Long): V? = map.get(key)
            fun put(key: Long, value: V): V? = map.put(key, value)
            fun putAll(from: Map<out Long, V>) = map.putAll(from)

            @Suppress("UNCHECKED_CAST")
            val entries: MutableSet<MutableMap.MutableEntry<Long, V>> get() = map.long2ObjectEntrySet() as ObjectSet<MutableMap.MutableEntry<Long, V>>
            val keys: MutableSet<Long> get() = map.keys
            val values: MutableCollection<V> get() = map.values
            fun remove(key: Long): V? = map.remove(key)
            fun containsValue(value: V): Boolean = map.containsValue(value)
            fun containsKey(key: Long): Boolean = map.containsKey(key)
            fun isEmpty(): Boolean = map.isEmpty()

            fun clear() = map.clear()
            fun toArrayKey(): Seq<Long> =  Seq<Long>(size).also { keys.forEach { value-> it.add(value) } }
            fun toArrayValues(): Seq<V> =  Seq<V>(size).also { values.forEach { value-> it.add(value) } }
            override fun toString(): String = map.toString()
        }

        private class ThreadUnsafe<V>(map: Long2ObjectOpenHashMap<V>): BaseLongMap<V>(map)
        private class ThreadSafety<V>(map: Long2ObjectMaps.SynchronizedMap<V>, private val lock: Long2ObjectMap<V>): BaseLongMap<V>(map)
    }
}