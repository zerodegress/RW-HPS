/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *  
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.struct

/**
 * 主要方法(复用)
 *
 * @param K
 * @param V
 * @property map Map<K, V>
 * @property size Int
 * @property entries MutableSet<MutableEntry<K, V>>
 * @property keys MutableSet<K>
 * @property values MutableCollection<V>
 * @constructor
 *
 * @author RW-HPS/Dr
 */
@Suppress("PLATFORM_CLASS_MAPPED_TO_KOTLIN")
abstract class BaseMap<K,V>(protected val map: java.util.Map<K,V>) :
    MutableMap<K,V>, Map<K,V>
{
    override val size: Int get() = map.size()

    override fun get(key: K): V? = map.get(key)
    override fun put(key: K, value: V): V? = map.put(key, value)
    override fun putAll(from: Map<out K, V>) = map.putAll(from)

    override val entries: MutableSet<MutableMap.MutableEntry<K, V>> get() = map.entrySet()
    override val keys: MutableSet<K> get() = map.keySet()
    override val values: MutableCollection<V> get() = map.values()

    override fun remove(key: K): V? = map.remove(key)
    override fun containsValue(value: V): Boolean = map.containsValue(value)
    override fun containsKey(key: K): Boolean = map.containsKey(key)

    override fun isEmpty(): Boolean = map.isEmpty
    override fun clear() = map.clear()

    fun toArrayKey(): Seq<K> =  Seq<K>(size).also { keys.forEach { value-> it.add(value) } }
    fun toArrayValues(): Seq<V> =  Seq<V>(size).also { values.forEach { value-> it.add(value) } }

    override fun toString(): String = map.toString()
}
/**
 * Pain will come with the blade
 * Pain will wake up the despondent crowd in this dormant world somehow
 */