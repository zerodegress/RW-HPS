/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.dependent.redirections.slick

import net.rwhps.asm.data.MethodTypeInfoValue
import net.rwhps.asm.redirections.replace.def.BasicDataRedirections
import net.rwhps.server.dependent.redirections.MainRedirections
import net.rwhps.server.dependent.redirections.lwjgl.LwjglClassProperties
import net.rwhps.server.util.annotations.mark.AsmMark

/**
 * Since Rusted Warfare [org.newdawn.slick.AppGameContainer.start] is just a while(True) loop which calls [org.newdawn.slick.AppGameContainer.gameLoop] and [Thread.yield]. Once we
 * heavy load on the CPU. This [Redirection] fixes that by sleeping for a
 * set amount of time, configurable by the SystemProperty [LwjglClassProperties.DISPLAY_UPDATE].
 *
 * @author Dr (dr@der.kim)
 */
@AsmMark.ClassLoaderCompatible
internal class AppGameContainerRedirection : MainRedirections {
    override fun register() {
        //addAllReplace("org/newdawn/slick/Graphics")
        addAllReplace("org/newdawn/slick/AngelCodeFont")

        redirectR(MethodTypeInfoValue("org/newdawn/slick/Input","poll", "(II)V"), BasicDataRedirections.NULL)
        // 关闭强制渲染
//        redirectL(MethodTypeInfoValue("com/corrodinggames/rts/java/u","init","(Lorg/newdawn/slick/GameContainer;)V", false)) { obj: Any?, _: String, args: Array<out Any?> ->
//            println("AlwaysRender Start")
////            ExtractUtils.tryRunTest {
////                val gameContainer = "org.newdawn.slick.GameContainer".toClassAutoLoader(obj!!)
////                gameContainer!!.findMethod("setAlwaysRender", Boolean::class.java)!!.invoke(args[0], false)
////            }
//            println("Close AlwaysRender")
//        }
    }
}