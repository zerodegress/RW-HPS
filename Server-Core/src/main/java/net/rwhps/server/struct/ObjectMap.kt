/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.struct

import it.unimi.dsi.fastutil.objects.Object2ObjectMaps
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap

/**
 * @date  2023/6/14 20:36
 * @author  RW-HPS/Dr
 */
class ObjectMap<K, V> : BaseMap<K, V> {
    @JvmOverloads constructor(threadSafety: Boolean = false): this(16, threadSafety)

    @Suppress("UNCHECKED_CAST", "PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    @JvmOverloads constructor(capacity: Int, threadSafety: Boolean = false): super(
        Object2ObjectOpenHashMap<K,V>(capacity).let {
            if (threadSafety) {
                Object2ObjectMaps.synchronize<K,V>(it, it)
            } else {
                it
            }
        } as java.util.Map<K, V>, threadSafety
    )

    companion object {
        fun <K,V> of(vararg values: Any?): ObjectMap<K,V> {
            val map = ObjectMap<K,V>()
            for (i in 0 until (values.size / 2)) {
                map[(values[i * 2] as K)] = (values[i * 2 + 1] as V)
            }
            return map
        }
    }
}