/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.util


/**
 * @author RW-HPS/Dr
 */
object GameOtherUtil {
    @JvmStatic
    fun getBetaVersion(version: Int): Boolean {
        return version in 152 .. 175
    }

    @JvmStatic
    fun getPoint(floats: Map<Float, Float>): FloatArray {
        var xOut = 0F
        var yOut = 0F
        floats.forEach { (x, y) ->
            if (xOut == 0F && yOut == 0F) {
                xOut = x
                yOut = y
            } else {
                xOut += x
                yOut += y

                xOut /= 2
                yOut /= 2
            }
        }
        return floatArrayOf(xOut,yOut)
    }
}