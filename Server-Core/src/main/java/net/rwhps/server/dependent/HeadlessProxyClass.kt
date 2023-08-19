/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.dependent

import net.rwhps.asm.agent.AsmAgent
import net.rwhps.asm.agent.AsmData
import net.rwhps.server.data.global.Data
import net.rwhps.server.data.plugin.PluginManage
import net.rwhps.server.dependent.redirections.game.CustomRedirections
import net.rwhps.server.dependent.redirections.game.FileLoaderRedirections
import net.rwhps.server.dependent.redirections.game.NetPacketRedirections
import net.rwhps.server.dependent.redirections.lwjgl.LwjglRedirections
import net.rwhps.server.dependent.redirections.slick.SlickRedirections
import net.rwhps.server.game.HessModuleManage
import net.rwhps.server.game.event.global.ServerHessLoadEvent
import net.rwhps.server.plugin.internal.hess.service.data.HessClassPathProperties
import net.rwhps.server.struct.ObjectMap
import net.rwhps.server.util.ReflectionUtils
import net.rwhps.server.util.annotations.mark.AsmMark
import net.rwhps.server.util.inline.findMethod
import net.rwhps.server.util.inline.toClassAutoLoader
import net.rwhps.server.util.log.Log

/**
 * What could it be
 */
/**
 * 注册 Hess 的 ASM
 *
 * @author RW-HPS
 */
@AsmMark.ClassLoaderCompatible
class HeadlessProxyClass: AgentAttachData() {
    init {
        initProxyClass()
    }

    private fun initProxyClass() {/* Register headless Lwjgl */
        LwjglRedirections().register()/* Register headless Slick */
        SlickRedirections().register()/* Register headless FileSystem */
        FileLoaderRedirections().register()/* Register some dependent features */
        CustomRedirections().register()/* Register for network blocking */
        NetPacketRedirections().register()

        // 直接空实现 因为意义不大
        AsmData.addClassIgnore("org/newdawn/slick/util/DefaultLogSystem")
        AsmData.addClassIgnore("com/LibRocket")
        AsmData.addClassIgnore("com/corrodinggames/librocket/scripts/ScriptEngine")
        // 这两个 因为 [LibRocket] 如果是空的话, 会被……游戏调用导致 NPE, 所以我们要覆盖掉方法
        //需要空实现
        AsmData.addPartialMethod("com/corrodinggames/librocket/b", arrayOf("closeDocument", "(Lcom/ElementDocument;)V"))
        AsmData.addPartialMethod("com/corrodinggames/librocket/b", arrayOf("closeActiveDocument", "()V"))

        /* 恢复-Root */
        AsmData.addPartialMethod(
                "com/corrodinggames/librocket/scripts/Root", arrayOf("resume", "()V")
        ) { obj: Any?, _: String, _: Class<*>, _: Array<out Any?> ->
            val rootClass = "com.corrodinggames.librocket.scripts.Root".toClassAutoLoader(obj!!)
            ReflectionUtils.findField(rootClass, "guiEngine")!!.also {
                ReflectionUtils.makeAccessible(it)
                it[obj]?.let { correspondingObject ->
                    "com.corrodinggames.librocket.a".toClassAutoLoader(obj)!!.findMethod("f")!!.invoke(correspondingObject)
                }
            }
            return@addPartialMethod null
        }/* 恢复-Root */
        AsmData.addPartialMethod(
                "com/corrodinggames/librocket/scripts/Root", arrayOf("resumeNonMenu", "()V")
        ) { obj: Any?, _: String, _: Class<*>, _: Array<out Any?> ->
            val rootClass = "com.corrodinggames.librocket.scripts.Root".toClassAutoLoader(obj!!)
            ReflectionUtils.findField(rootClass, "guiEngine")!!.also {
                ReflectionUtils.makeAccessible(it)
                it[obj]?.let { correspondingObject ->
                    "com.corrodinggames.librocket.a".toClassAutoLoader(obj)!!.findMethod("a", java.lang.Boolean::class.java)!!
                        .invoke(correspondingObject, false)
                }
            }
            return@addPartialMethod null
        }


        /* 取代游戏自己打印的 */
        AsmData.addPartialMethod(
                "android/util/Log", arrayOf("a", "(ILjava/lang/String;Ljava/lang/String;)I")
        ) { obj: Any?, _: String, _: Class<*>, args: Array<out Any?> ->
            args[2]?.let {
                val classIn = obj as Class<*>

                val msg = it.toString()

                if (Data.config.log == "ALL") {
                    println("[${classIn.classLoader}]  :  " + msg)
                }

                val load = classIn.classLoader
                val loadID = load.toString()

                val loadFlagGame = GameInitStatus.loadStatus[loadID, {
                    GameInitStatus.NoLoad
                }]

                if (msg.contains("----- Game init finished in")) {
                    GameInitStatus.loadStatus[loadID] = GameInitStatus.LoadEndMods
                }
                if (msg.contains("Saving settings") && loadFlagGame == GameInitStatus.LoadEndMods) {
                    GameInitStatus.loadStatus[loadID] = GameInitStatus.LoadEndGames

                    // Enable the interface
                    "${HessClassPathProperties.CorePath}.GameEngine".toClassAutoLoader(load)!!.findMethod("init")!!.invoke(null)

                    PluginManage.runGlobalEventManage(ServerHessLoadEvent(loadID, HessModuleManage.hessLoaderMap[loadID]!!))
                }

                if (msg.startsWith("Replay: Recording replay to:")) {
                    Log.clog("Save Replay to: {0}", msg.replace("Replay: Recording replay to:", "").trim().also { replayFileName ->
                        HessModuleManage.hessLoaderMap[loadID]!!.room.replayFileName = replayFileName
                    })
                }
            }
            0
        }


        AsmAgent.agentmain(this.instrumentation)
    }


    /**
     * 游戏无头加载判断
     */
    private enum class GameInitStatus {
        NoLoad,
        LoadEndMods,
        LoadEndGames;

        companion object {
            val loadStatus = ObjectMap<String, GameInitStatus>()
        }
    }
}