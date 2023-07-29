/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.dependent.redirections.slick

import net.rwhps.asm.agent.AsmData
import net.rwhps.asm.redirections.DefaultRedirections
import net.rwhps.server.dependent.redirections.MainRedirections
import net.rwhps.server.util.annotations.NeedHelp
import net.rwhps.server.util.annotations.mark.AsmMark
import net.rwhps.server.util.classload.GameModularLoadClass
import net.rwhps.server.util.inline.accessibleConstructor
import net.rwhps.server.util.inline.findMethod
import net.rwhps.server.util.inline.readAsClassBytes
import net.rwhps.server.util.inline.toClass

//关闭傻逼格式化
//@formatter:off

/**
 * @author RW-HPS/Dr
 */
@NeedHelp
@AsmMark.ClassLoaderCompatible
class SlickRedirections: MainRedirections {
    override fun register() {
        AsmData.addClassIgnore("org/newdawn/slick/opengl/renderer/VBORenderer")

        AsmData.addPartialMethod("org/newdawn/slick/AppGameContainer", arrayOf("start", "()V"), AppGameContainerUpdate())

        // Remove game mouse cursor
        AsmData.addPartialMethod("org/newdawn/slick/AppGameContainer", arrayOf("setMouseCursor", "(Lorg/newdawn/slick/Image;II)V"), DefaultRedirections.NULL)
        AsmData.addPartialMethod("org/newdawn/slick/AppletGameContainer", arrayOf("setMouseCursor", "(Lorg/newdawn/slick/Image;II)V"), DefaultRedirections.NULL)

        // Replace version number
        AsmData.addPartialMethod("org/newdawn/slick/GameContainer", arrayOf("getBuildVersion", "()I")) { obj: Any, _: String, _: Class<*>, _: Array<out Any?> ->
            val classIn = obj as Class<*>
            SilckClassPathProperties.SlickLog.toClass(classIn.classLoader)!!.findMethod("info", String::class.java)!!
                .invoke(null, "Slick Build : RW-HPS-Headless-Slick RwGame#84")
            0
        }

        // Disable sound loader
        AsmData.addClassIgnore("org/newdawn/slick/Sound")
        redirect("Lorg/newdawn/slick/Sound;playing()Z", DefaultRedirections.BOOLEANT)

        // Replace default graphics processor
        AsmData.addPartialMethod("org/newdawn/slick/opengl/pbuffer/GraphicsFactory", arrayOf("createGraphics", "(Lorg/newdawn/slick/Image;)Lorg/newdawn/slick/Graphics;")) { obj: Any, _: String, _: Class<*>, args: Array<out Any?> ->
            val classIn = obj as Class<*>
            val classLoader = classIn.classLoader
            return@addPartialMethod SilckClassPathProperties.Graphics.toClass(classLoader)!!.cast(
                    if (classLoader is GameModularLoadClass) {
                        classLoader.loadClassBytes(
                                SilckClassPathProperties.DrGraphics, SilckClassPathProperties.DrGraphics.readAsClassBytes()
                        )!!
                    } else {
                        SilckClassPathProperties.DrGraphics.toClass(classLoader)!!
                    }.accessibleConstructor(SilckClassPathProperties.Image.toClass(classLoader)!!).newInstance(args[0])
            )
        }

        // Set the texture length and width
        AsmData.addPartialMethod("org/newdawn/slick/opengl/TextureImpl", arrayOf("getTextureHeight", "()I"), DefaultRedirections.INT)
        AsmData.addPartialMethod("org/newdawn/slick/opengl/TextureImpl", arrayOf("getTextureWidth", "()I"), DefaultRedirections.INT)
    }
}