/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.dependent.redirections.lwjgl

import net.rwhps.asm.api.Redirection

/**
 * Since Rusted Warfare Gameloop is just a while(True) loop which calls `org.lwjgl.opengl.Display.update()` and [Thread.yield]. Once we
 * redirect the `update()` call the loop just runs and runs and puts some
 * heavy load on the CPU. This [Redirection] fixes that by sleeping for a
 * set amount of time, configurable by the SystemProperty [LwjglProperties.DISPLAY_UPDATE].
 */
class DisplayUpdater @JvmOverloads constructor(private val time: Long = getTime()) : Redirection {
    @Throws(Throwable::class)
    override fun invoke(obj: Any, desc: String, type: Class<*>?, vararg args: Any): Any? {
        // we could scale this with the refresh rate?
        Thread.sleep(time)
        return null
    }

    companion object {
        const val DESC = "Lorg/lwjgl/opengl/Display;update()V"

        private fun getTime(): Long {
            return try {
                System.getProperty(LwjglProperties.DISPLAY_UPDATE, "100").toLong()
            } catch (nfe: NumberFormatException) {
                100L
            }
        }
    }
}