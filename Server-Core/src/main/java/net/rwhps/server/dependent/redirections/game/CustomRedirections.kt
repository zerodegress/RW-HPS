/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.dependent.redirections.game

import net.rwhps.server.data.global.Data
import net.rwhps.server.dependent.redirections.MainRedirections
import net.rwhps.server.util.alone.annotations.AsmMark
import net.rwhps.server.util.alone.annotations.GameSimulationLayer
import net.rwhps.server.util.inline.findField
import net.rwhps.server.util.inline.findMethod
import net.rwhps.server.util.inline.toClassAutoLoader
import net.rwhps.server.util.log.Log

/**
 * @author RW-HPS/Dr
 */
@AsmMark.ClassLoaderCompatible
class CustomRedirections : MainRedirections {
    override fun register() {
        /* 屏蔽游戏错误捕获 */
        @GameSimulationLayer.GameSimulationLayer_KeyWords("setDefaultUncaughtExceptionHandler")
        redirect("com/corrodinggames/rts/gameFramework/l", arrayOf("aq","()V"))

        /* 屏蔽游戏设置保存 */
        @GameSimulationLayer.GameSimulationLayer_KeyWords("preferences.ini")
        redirect("com/corrodinggames/rts/gameFramework/SettingsEngine", arrayOf("saveToFileSystem","()Z")) { obj: Any, _: String?, _: Class<*>?, _: Array<Any?>? ->
            "com.corrodinggames.rts.gameFramework.l".toClassAutoLoader(obj)!!.findMethod("b", String::class.java)!!.invoke(null, "Saving settings: RW-HPS(ASM)")
            return@redirect true
        }
        redirect("com/corrodinggames/rts/gameFramework/SettingsEngine", arrayOf("loadFromFileSystem","()V"))

        /* UUID 覆盖 */
        @GameSimulationLayer.GameSimulationLayer_KeyWords("serverUUID==null")
        redirect("com/corrodinggames/rts/gameFramework/j/ad", arrayOf("Z","()Ljava/lang/String;")) { _: Any?, _: String?, _: Class<*>?, _: Array<Any?>? ->
            return@redirect Data.core.serverHessUuid
        }

        /* MOD启用器覆盖(强制启用除自带以外) */
        @GameSimulationLayer.GameSimulationLayer_KeyWords("Loading mod selection")
        redirect("com/corrodinggames/rts/gameFramework/i/a", arrayOf("f","()V")) { obj: Any, _: String?, _: Class<*>?, _: Array<Any?>? ->
            Log.debug("[Hess] Try Loading mod selection")
            val modDataClass = "com.corrodinggames.rts.gameFramework.i.b".toClassAutoLoader(obj)!!

            val modNameField = modDataClass.findField("c",String::class.java)!!
            val modEnableField = modDataClass.findField("f", Boolean::class.javaPrimitiveType)!!

            ("com.corrodinggames.rts.gameFramework.i.a".toClassAutoLoader(obj)!!.findField("e", ArrayList::class.java)!!.get(obj) as ArrayList<*>).forEach {
                // 过滤原版单位
                if (modNameField[it] != "mega_builders") {
                    modEnableField.setBoolean(it,false)
                }
            }
        }
    }
}