/*
 * Copyright 2020-2024 RW-HPS Team and contributors.
 *  
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.plugin.internal.headless.inject.core

import net.rwhps.server.game.headless.core.AbstractGameFunction
import net.rwhps.server.util.inline.findField
import net.rwhps.server.util.log.Log

/**
 *
 *
 * @date 2023/12/22 17:17
 * @author Dr (dr@der.kim)
 */
class GameFunction : AbstractGameFunction {
    override fun suspendMainThreadOperations(run: Runnable) {
        val running = GameEngine.appGameContainerObject::class.java.findField("paused", Boolean::class.javaPrimitiveType)!!
        running.setBoolean(GameEngine.appGameContainerObject, true)
        try {
            run.run()
        } catch (e: Exception) {
            Log.error("Hess MainThreadOperations", e)
        }
        running.setBoolean(GameEngine.appGameContainerObject, false)
    }
}