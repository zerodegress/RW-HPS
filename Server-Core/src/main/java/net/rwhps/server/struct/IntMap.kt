/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.struct

import it.unimi.dsi.fastutil.ints.Int2ObjectMap
import it.unimi.dsi.fastutil.ints.Int2ObjectMaps
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectSet

class IntMap<V>: MutableMap<Int,V> {
    private val map: BaseIntMap<V>

    @JvmOverloads constructor(threadSafety: Boolean = false): this(16, threadSafety)
    @JvmOverloads constructor(capacity: Int, threadSafety: Boolean = false) {
        map = Int2ObjectOpenHashMap<V>(capacity).let { if (threadSafety) ThreadSafety((Int2ObjectMaps.synchronize<V>(it, it) as Int2ObjectMaps.SynchronizedMap<V>), it) else ThreadUnsafe(it) }
    }

    override val size: Int get() = map.size
    override fun get(key: Int): V? = map.get(key)
    override fun put(key: Int, value: V): V? = map.put(key, value)
    override fun putAll(from: Map<out Int, V>) = map.putAll(from)
    override val entries: MutableSet<MutableMap.MutableEntry<Int, V>> get() = map.entries
    override val keys: MutableSet<Int> get() = map.keys
    override val values: MutableCollection<V> get() = map.values
    override fun remove(key: Int): V? = map.remove(key)
    override fun containsValue(value: V): Boolean = map.containsValue(value)
    override fun containsKey(key: Int): Boolean = map.containsKey(key)
    override fun isEmpty(): Boolean = map.isEmpty()
    override fun clear() = map.clear()
    fun toArrayKey(): Seq<Int> =  map.toArrayKey()
    fun toArrayValues(): Seq<V> =  map.toArrayValues()

    companion object {
        private abstract class BaseIntMap<V>(private val map: Int2ObjectMap<V>) {
            //Int2ObjectOpenHashMap
            val size: Int get() = map.size

            fun get(key: Int): V? = map.get(key)
            fun put(key: Int, value: V): V? = map.put(key, value)
            fun putAll(from: Map<out Int, V>) = map.putAll(from)

            @Suppress("UNCHECKED_CAST")
            val entries: MutableSet<MutableMap.MutableEntry<Int, V>> get() = map.int2ObjectEntrySet() as ObjectSet<MutableMap.MutableEntry<Int, V>>
            val keys: MutableSet<Int> get() = map.keys
            val values: MutableCollection<V> get() = map.values
            fun remove(key: Int): V? = map.remove(key)
            fun containsValue(value: V): Boolean = map.containsValue(value)
            fun containsKey(key: Int): Boolean = map.containsKey(key)
            fun isEmpty(): Boolean = map.isEmpty()

            fun clear() = map.clear()
            fun toArrayKey(): Seq<Int> =  Seq<Int>(size).also { keys.forEach { value-> it.add(value) } }
            fun toArrayValues(): Seq<V> =  Seq<V>(size).also { values.forEach { value-> it.add(value) } }
        }

        private class ThreadUnsafe<V>(private val map: Int2ObjectOpenHashMap<V>): BaseIntMap<V>(map) {
        }
        private class ThreadSafety<V>(private val map: Int2ObjectMaps.SynchronizedMap<V>, private val lock: Int2ObjectMap<V>): BaseIntMap<V>(map) {
        }
    }
}