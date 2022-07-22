/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.util

import cn.rwhps.server.game.replay.block.PointF
import java.util.*
import kotlin.math.atan2

object CommonUtils {
    val realRandom = Random()

    fun square(value: Float): Float {
        return kotlin.math.sqrt(value.toDouble()).toFloat()
    }

    fun toZero(value: Float, diff: Float): Float {
        if (value > diff) {
            return value - diff
        }
        return if (value < -diff) {
            value + diff
        } else 0.0f
    }

    fun toValue(value: Float, target: Float, diff: Float): Float {
        if (value > target + diff) {
            return value - diff
        }
        return if (value < target - diff) value + diff else target
    }

    fun limit(value: Float, limit: Float): Float {
        return if (value > limit) limit else if (value < -limit) -limit else value
    }

    fun limit(value: Float, min: Float, max: Float): Float {
        return if (value > max) max else if (value < min) min else value
    }

    fun limitInt(value: Int, min: Int, max: Int): Int {
        return if (value > max) max else if (value < min) min else value
    }

    fun roundDown(value: Float): Int {
        if (value > 0.0f) {
            return value.toInt()
        }
        return if (value < 0.0f) {
            value.toInt() - 1
        } else 0
    }

    fun distanceSq(x: Float, y: Float, x2: Float, y2: Float): Float {
        return (x - x2) * (x - x2) + (y - y2) * (y - y2)
    }

    fun distance(x: Float, y: Float, x2: Float, y2: Float): Float {
        return kotlin.math.sqrt(((x - x2) * (x - x2) + (y - y2) * (y - y2)).toDouble()).toFloat()
    }

    fun fixDir(dirIn: Float, mode: Boolean): Float {
        var dir = dirIn
        if (!mode) {
            while (true) {
                if (dir <= 180.0f && dir >= -180.0f) {
                    break
                }
                if (dir > 180.0f) {
                    dir -= 360.0f
                }
                if (dir < -180.0f) {
                    dir += 360.0f
                }
            }
        } else {
            while (true) {
                if (dir in (0.0f..360.0f)) {
                    break
                }
                if (dir > 360.0f) {
                    dir -= 360.0f
                }
                if (dir < 0.0f) {
                    dir += 360.0f
                }
            }
        }
        return dir
    }

    fun getRotationDir(source: Float, target: Float, speed: Float): Float {
        val diff = fixDir(fixDir(target, true) - fixDir(source, true), false)
        return if (diff > speed) speed else if (diff < -speed) -speed else diff
    }

    fun getDirection(x: Float, y: Float, tx: Float, ty: Float): Float {
        return Math.toDegrees(atan2((ty - y).toDouble(), (tx - x).toDouble())).toFloat()
    }

    fun lineIntersectLine(v1: PointF, v2: PointF, v3: PointF, v4: PointF): Boolean {
        val denom = (v4.y - v3.y) * (v2.x - v1.x) - (v4.x - v3.x) * (v2.y - v1.y)
        val numerator = (v4.x - v3.x) * (v1.y - v3.y) - (v4.y - v3.y) * (v1.x - v3.x)
        val numerator2 = (v2.x - v1.x) * (v1.y - v3.y) - (v2.y - v1.y) * (v1.x - v3.x)
        if (denom == 0.0f) {
            return if (numerator == 0.0f && numerator2 == 0.0f) false else false
        }
        val ua = numerator / denom
        val ub = numerator2 / denom
        return ua in 0.0f..1.0f && ub >= 0.0f && ub <= 1.0f
    }

    fun rnd(min: Float, max: Float): Float {
        return (Math.random() * (max - min) + min).toFloat()
    }

    fun sameRnd(min: Float, max: Float): Float {
        return (Math.random() * (max - min) + min).toFloat()
    }

    fun realRnd(min: Float, max: Float): Float {
        return realRandom.nextFloat() * (max - min) + min
    }

    fun realRand(max: Int): Int {
        return realRandom.nextInt(max)
    }

    fun cos(dir: Float): Float {
        return StrictMath.cos(StrictMath.toRadians(dir.toDouble())).toFloat()
    }

    fun sin(dir: Float): Float {
        return StrictMath.sin(StrictMath.toRadians(dir.toDouble())).toFloat()
    }

    fun abs(`val`: Float): Float {
        return if (`val` < 0.0f) -`val` else `val`
    }

    fun abs(`val`: Int): Int {
        return if (`val` < 0) -`val` else `val`
    }

    fun close(`val`: Float, val2: Float): Boolean {
        return abs(`val` - val2) < 0.05f
    }
}