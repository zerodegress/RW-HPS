/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.data.totalizer

import net.rwhps.server.util.Time

/**
 * @author RW-HPS/Dr
 */
class TimeAndNumber(
    private val timeOut: Int,
    private val conutMax: Int
) {
    private var startTime = 0
    var count: Int = 0
        set(value) {
            if (field == 0) {
                startTime = Time.concurrentSecond()
            }
            field = value
        }

    fun checkStatus(): Boolean {
        if (count < conutMax) {
            return true
        } else {
            if ((Time.concurrentSecond() - startTime) > timeOut) {
                reset()
                return true
            }
            return false
        }
    }

    fun reset() {
        startTime = 0
        count = 0
    }
}
