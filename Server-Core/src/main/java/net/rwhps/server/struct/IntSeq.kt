/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.struct

import it.unimi.dsi.fastutil.ints.IntArrayList
import it.unimi.dsi.fastutil.ints.IntLists

/**
 * @date  2023/5/26 14:28
 * @author  RW-HPS/Dr
 */
class IntSeq: BaseSeq<Int> {
    @JvmOverloads constructor(threadSafety: Boolean = false): this(16, threadSafety)

    @Suppress("UNCHECKED_CAST", "PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    @JvmOverloads constructor(capacity: Int, threadSafety: Boolean = false): super(
        IntArrayList(capacity).let {
            if (threadSafety) {
                IntLists.synchronize(it, it)
            } else {
                it
            }
        } as java.util.List<Int>
    )

    @Suppress("UNCHECKED_CAST", "PLATFORM_CLASS_MAPPED_TO_KOTLIN")
    @JvmOverloads constructor(array: IntArray, threadSafety: Boolean = false): super(
        IntArrayList(array).let {
            if (threadSafety) {
                IntLists.synchronize(it, it)
            } else {
                it
            }
        } as java.util.List<Int>
    )
}