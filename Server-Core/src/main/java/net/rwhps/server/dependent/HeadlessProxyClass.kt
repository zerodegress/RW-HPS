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
import net.rwhps.asm.agent.AsmCore
import net.rwhps.server.data.global.Data
import net.rwhps.server.dependent.redirections.FileLoaderRedirections
import net.rwhps.server.dependent.redirections.lwjgl.LwjglRedirections
import net.rwhps.server.dependent.redirections.slick.SlickRedirections
import net.rwhps.server.game.event.EventGlobalType
import net.rwhps.server.struct.ObjectMap
import net.rwhps.server.util.GameModularLoadClass
import net.rwhps.server.util.ReflectionUtils
import net.rwhps.server.util.alone.annotations.AsmMark
import net.rwhps.server.util.file.FileUtil
import net.rwhps.server.util.game.Events
import net.rwhps.server.util.inline.findMethod
import net.rwhps.server.util.inline.readAsClassBytes
import net.rwhps.server.util.inline.toClassAutoLoader
import net.rwhps.server.util.log.Log
import java.io.File

@AsmMark.ClassLoaderCompatible
class HeadlessProxyClass : AgentAttachData() {
    init {
        initProxyClass()
    }

    private fun initProxyClass() {
        /* 注册无头Lwjgl */
        LwjglRedirections().register()
        /* 注册无头Slick */
        SlickRedirections().register()
        /* 注册无头FileSystem */
        FileLoaderRedirections().register()

        // 直接空实现
        AsmCore.allMethod.add("org/newdawn/slick/util/DefaultLogSystem")
        AsmCore.allMethod.add("com/LibRocket")
        AsmCore.allMethod.add("com/corrodinggames/librocket/scripts/ScriptEngine")
        // 这两个 因为 [LibRocket] 是空 所以被调用时 会 NPE
        AsmCore.addPartialMethod("com/corrodinggames/librocket/b" , arrayOf("closeDocument","(Lcom/ElementDocument;)V"))
        AsmCore.addPartialMethod("com/corrodinggames/librocket/b" , arrayOf("closeActiveDocument","()V"))

        /* 恢复-Root */
        AsmCore.addPartialMethod("com/corrodinggames/librocket/scripts/Root" , arrayOf("resume","()V")) { obj: Any, _: String?, _: Class<*>?, _: Array<Any?>? ->
            val rootClass = "com.corrodinggames.librocket.scripts.Root".toClassAutoLoader(obj)
            ReflectionUtils.findField(rootClass,"guiEngine")!!.also {
                ReflectionUtils.makeAccessible(it)
                it.get(obj)?.let {
                    "com.corrodinggames.librocket.a".toClassAutoLoader(obj)!!.findMethod("f")!!.invoke(it)
                }
            }
            return@addPartialMethod null
        }
        /* 恢复-Root */
        AsmCore.addPartialMethod("com/corrodinggames/librocket/scripts/Root" , arrayOf("resumeNonMenu","()V")) { obj: Any, _: String?, _: Class<*>?, _: Array<Any?>? ->
            val rootClass = "com.corrodinggames.librocket.scripts.Root".toClassAutoLoader(obj)
            ReflectionUtils.findField(rootClass,"guiEngine")!!.also {
                ReflectionUtils.makeAccessible(it)
                it.get(obj)?.let {
                    "com.corrodinggames.librocket.a".toClassAutoLoader(obj)!!.findMethod("a",java.lang.Boolean::class.java)!!.invoke(it,false)
                }
            }
            return@addPartialMethod null
        }


        /* 取代游戏自己打印的 */
        AsmCore.addPartialMethod("android/util/Log", arrayOf("a","(ILjava/lang/String;Ljava/lang/String;)I")) { obj: Any, _: String?, _: Class<*>?, args: Array<Any?>? ->
            args?.let {
                args[2]?.let {
                    val classIn = obj as Class<*>

                    if (Data.config.Log == "ALL") {
                        System.out.println("[${classIn.classLoader}]  :  "+it.toString())
                    }

                    val load = classIn.classLoader
                    val loadID = load.toString()

                    val loadFlagGame = GameInitStatus.loadStatus.get(loadID) {
                        GameInitStatus.NoLoad
                    }

                    if (it.toString().contains("----- Game init finished in")) {
                        GameInitStatus.loadStatus.put(loadID,GameInitStatus.LoadEndMods)
                    }
                    if (it.toString().contains("Saving settings") && loadFlagGame == GameInitStatus.LoadEndMods) {
                        GameInitStatus.loadStatus.put(loadID,GameInitStatus.LoadEndGames)

                        (load as GameModularLoadClass).also {
                            // Here, several intermediate signal transmission modules are directly injected into this loader
                            // Because this loader only has Game-lib.jar
                            val pkg = "net.rwhps.server.game.simulation.gameFramework"
                            FileUtil(File(FileUtil.getJarPath())).zipDecoder.getZipAllBytes().each { k, v ->
                                if (k.startsWith(pkg.replace(".","/"))) {
                                    val name = k.replace(".class","")
                                    load.loadClassBytes(name.replace("/","."),v)
                                }
                            }
                            load.loadClassBytes("$pkg.GameEngine", byteArrayOf())!!.findMethod("init")!!.invoke(null)
                        }

                        Events.fire(EventGlobalType.GameLibLoadEvent(loadID))
                    }
                    if (it.toString().startsWith("Replay: Recording replay to:")) {
                        Log.clog("Save Replay to: {0}",it.toString().replace("Replay: Recording replay to:",""))
                    }
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

    companion object {
        private var loadFlagGame = GameInitStatus.NoLoad
    }
}