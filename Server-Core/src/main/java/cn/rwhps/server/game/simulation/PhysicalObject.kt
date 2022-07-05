/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.game.simulation

abstract class PhysicalObject : RenderedObject() {
    @JvmField var cType: CollisionEngine.CollisionType? = null
    @JvmField var collisionId = 0
    @JvmField var halfObjectHeight = 0
    @JvmField var halfObjectWidth = 0
    @JvmField var objectHeight = 0
    @JvmField var objectWidth = 0

    // com.corrodinggames.rts.gameFramework.GameObject
    override fun remove() {
        super.remove()
    }
}