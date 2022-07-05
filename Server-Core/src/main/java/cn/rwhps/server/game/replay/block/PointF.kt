/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.game.replay.block

class PointF {
    @JvmField var x: Float
    @JvmField var y: Float
    constructor() {
        this.x = 0.00F
        this.y = 0.00F
    }

    constructor(var1: Float, var2: Float) {
        this.x = var1
        this.y = var2
    }

    fun a(var1: Float, var2: Float) {
        this.x = var1
        this.y = var2
    }

    fun a(var1: PointF) {
        this.x = var1.x
        this.y = var1.y
    }

    fun set(var1: Float, var2: Float) {
        this.x += var1
        this.y += var2
    }

    fun describeContents(): Int {
        return 0
    }
}