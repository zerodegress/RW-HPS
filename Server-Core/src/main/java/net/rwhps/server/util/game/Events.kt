/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */
package net.rwhps.server.util.game

import net.rwhps.server.struct.ObjectMap
import net.rwhps.server.struct.Seq

/**
 * @author RW-HPS/Dr
 */
object Events {
    private val EVENTS = ObjectMap<Any, Seq<(Any)->Unit>>()

    @Suppress("UNCHECKED_CAST")
    fun <T> add(type: Class<T>, listener: (T)->Unit) {
        EVENTS[type, { Seq() }].add(listener as (Any)->Unit)
    }

    fun add(type: Any, listener: ()->Unit) {
        EVENTS[type, { Seq() }].add { listener() }
    }

    fun <T> remove(type: Class<T>) {
        EVENTS[type, { Seq() }].clear()
    }

    inline fun <reified T> fire(type: T) {
        fire(T::class.java, type)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T> fire(type1: Class<*>, type: T) {
        if (EVENTS[type] != null) {
            EVENTS[type]?.eachAll { e: (Any)->Unit -> e(type!!) }
        }
        if (EVENTS[type1] != null) {
            EVENTS[type1]?.eachAll { e: (Any)->Unit -> e(type!!) }
        }
    }
}