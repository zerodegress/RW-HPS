/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.dependent

import cn.rwhps.asm.agent.AsmAgent
import cn.rwhps.asm.api.Redirection
import cn.rwhps.asm.redirections.AsmRedirections
import cn.rwhps.server.dependent.redirections.FileLoaderRedirections
import cn.rwhps.server.dependent.redirections.lwjgl.LwjglRedirections
import cn.rwhps.server.dependent.redirections.slick.SlickRedirections
import cn.rwhps.server.game.event.EventGlobalType
import cn.rwhps.server.util.ReflectionUtils
import cn.rwhps.server.util.game.Events
import cn.rwhps.server.util.log.Log
import com.corrodinggames.librocket.a
import com.corrodinggames.librocket.scripts.Root

class HeadlessProxyClass : AgentAttachData() {
    init {
        initProxyClass()
    }

    private fun initProxyClass() {
        LwjglRedirections().register()
        SlickRedirections().register()
        FileLoaderRedirections().register()

        AsmAgent.allMethod.add("org/newdawn/slick/util/DefaultLogSystem")
        AsmAgent.allMethod.add("com/LibRocket")
        AsmAgent.allMethod.add("com/corrodinggames/librocket/scripts/ScriptEngine")
        AsmAgent.addPartialMethod("com/corrodinggames/librocket/b" , arrayOf("closeDocument","(Lcom/ElementDocument;)V"))
        AsmAgent.addPartialMethod("com/corrodinggames/librocket/b" , arrayOf("closeActiveDocument","()V"))
        AsmAgent.addPartialMethod("com/corrodinggames/librocket/scripts/Root" , arrayOf("resume","()V")) { obj: Any?, _: String?, _: Class<*>?, _: Array<Any?>? ->
            (ReflectionUtils.findField(Root::class.java,"guiEngine")!!.get(obj) as a).f()
        }

        AsmAgent.addPartialMethod("com/corrodinggames/librocket/scripts/Root" , arrayOf("resumeNonMenu","()V")) { obj: Any?, _: String?, _: Class<*>?, _: Array<Any?>? ->
            (ReflectionUtils.findField(Root::class.java,"guiEngine")!!.get(obj) as a).a(false)
        }


        AsmAgent.addPartialMethod("android/util/Log", arrayOf("a","(ILjava/lang/String;Ljava/lang/String;)I"))
        AsmRedirections.customRedirection["Landroid/util/Log;a(ILjava/lang/String;Ljava/lang/String;)I"] =
            Redirection { _: Any?, _: String?, _: Class<*>?, args: Array<Any?>? ->
                args?.let {
                    args[2]?.let {
                        Log.all(it.toString())
                        if (it.toString().contains("----- Game init finished in")) {
                            loadFlagGame = GameInitStatus.LoadEndMods
                        }
                        if (it.toString().contains("Saving settings") && loadFlagGame == GameInitStatus.LoadEndMods) {
                            loadFlagGame = GameInitStatus.LoadEndGames
                            Events.fire(EventGlobalType.GameLibLoadEvent())
                        }
                    }
                }
                0
            }


        AsmAgent.agentmain("", this.instrumentation)
    }

    private enum class GameInitStatus {
        NoLoad,
        LoadEndMods,
        LoadEndGames
    }

    companion object {
        private var loadFlagGame = GameInitStatus.NoLoad
    }
}