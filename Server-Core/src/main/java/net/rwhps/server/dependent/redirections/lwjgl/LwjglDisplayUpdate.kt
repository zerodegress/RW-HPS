/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.dependent.redirections.lwjgl

import net.rwhps.asm.api.replace.RedirectionReplace
import net.rwhps.asm.data.MethodTypeInfoValue
import net.rwhps.server.util.annotations.mark.AsmMark

/**
 * Since Rusted Warfare [org.lwjgl.opengl.Display.update] is just a while(True) loop which calls [org.newdawn.slick.AppGameContainer.gameLoop] and [Thread.yield]. Once we
 * heavy load on the CPU. This [Redirection] fixes that by sleeping for a
 * set amount of time, configurable by the SystemProperty [LwjglClassProperties.DISPLAY_UPDATE].
 *
 * @author Dr (dr@der.kim)
 */
@AsmMark.ClassLoaderCompatible
object LwjglDisplayUpdate : RedirectionReplace {
    val DESC = MethodTypeInfoValue("org/lwjgl/opengl/Display", "update", "(Z)V", LwjglDisplayUpdate::class.java)

    private val time = getTime()

    override fun invoke(obj: Any, desc: String, type: Class<*>, vararg args: Any?) {
        // we could scale this with the refresh rate?
        if (time != 0L) {
            Thread.sleep(time)
        }
    }


    private fun getTime(): Long {
        return try {
            System.getProperty(LwjglClassProperties.AppGameContainer_UPDATE, "0").toLong()
        } catch (nfe: NumberFormatException) {
            10L
        }
    }

}