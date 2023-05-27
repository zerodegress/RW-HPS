/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.dependent.redirections.slick

import net.rwhps.asm.api.Redirection
import net.rwhps.server.data.global.Data
import net.rwhps.server.dependent.redirections.lwjgl.LwjglClassProperties
import net.rwhps.server.util.ReflectionUtils
import net.rwhps.server.util.alone.annotations.AsmMark
import net.rwhps.server.util.inline.findMethod
import net.rwhps.server.util.inline.toClassAutoLoader
import java.lang.reflect.Method

/**
 * Since Rusted Warfare [org.newdawn.slick.AppGameContainer.start] is just a while(True) loop which calls [org.newdawn.slick.AppGameContainer.gameLoop] and [Thread.yield]. Once we
 * heavy load on the CPU. This [Redirection] fixes that by sleeping for a
 * set amount of time, configurable by the SystemProperty [LwjglClassProperties.DISPLAY_UPDATE].
 *
 * @author RW-HPS/Dr
*/
@AsmMark.ClassLoaderCompatible
internal class AppGameContainerUpdate @JvmOverloads constructor(private val time: Long = getTime()) : Redirection {

    override fun invoke(obj: Any, desc: String, type: Class<*>?, vararg args: Any) {
        val classAppGameContainer = SilckClassPathProperties.AppGameContainer.toClassAutoLoader(obj)!!
        val methodSetup: Method = classAppGameContainer.findMethod("setup").also { ReflectionUtils.makeAccessible(it) }!!
        val methodGetDelta: Method = classAppGameContainer.findMethod("getDelta").also { ReflectionUtils.makeAccessible(it) }!!
        val methodGameLoop: Method = classAppGameContainer.findMethod("gameLoop").also { ReflectionUtils.makeAccessible(it) }!!

        try {
            methodSetup.invoke(obj)
            methodGetDelta.invoke(obj)

            while (!Data.exitFlag) {
                Thread.sleep(time)
                methodGameLoop.invoke(obj)
            }
        } finally {
            classAppGameContainer.findMethod("destroy")!!.invoke(obj)
        }
        // There is no direct exit here because it is called by Core.exit
    }

    companion object {
        const val DESC = "Lorg/newdawn/slick/AppGameContainer;start()V"

        private fun getTime(): Long {
            return try {
                // 经过测试 最佳的 sleep 是比 游戏Tick 稍快
                // 但是存在问题 例如玩家刚进入 就开始游戏 那么数据将来不及更新导致错误
                System.getProperty(LwjglClassProperties.AppGameContainer_UPDATE, "10").toLong()
            } catch (nfe: NumberFormatException) {
                10L
            }
        }
    }
}