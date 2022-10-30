/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.dependent

import cn.rwhps.lwjgl.headless.agent.LwjglAgent
import cn.rwhps.lwjgl.headless.api.Redirection
import cn.rwhps.lwjgl.headless.redirections.LwjglRedirections
import cn.rwhps.server.game.event.EventGlobalType
import cn.rwhps.server.util.file.FileUtil
import cn.rwhps.server.util.game.Events

class HeadlessProxyClass : AgentAttachData() {
    init {
        initProxyClass()
    }

    private fun initProxyClass() {
        LwjglAgent.partialMethod["android/util/Log"] = arrayOf("a","(ILjava/lang/String;Ljava/lang/String;)I")

        // 设置 重定向文件PATH类
        LwjglAgent.partialMethod["com/corrodinggames/rts/gameFramework/e/c"] = arrayOf("f","()Ljava/lang/String;")
        LwjglAgent.partialMethod["com/corrodinggames/rts/gameFramework/e/c"] = arrayOf("b","()Ljava/lang/String;")

        // 设置 使指定方法无效
        LwjglAgent.partialMethod["com/corrodinggames/librocket/scripts/Root"] = arrayOf("resume","()V")
        LwjglAgent.partialMethod["com/corrodinggames/librocket/scripts/Root"] = arrayOf("resumeNonMenu","()V")

        // 重定向部分文件系统
        val filePath = FileUtil.defaultFilePath+"data/"
        LwjglRedirections.customRedirection["Lcom/corrodinggames/rts/gameFramework/e/c;f()Ljava/lang/String;"] =
            Redirection { _: Any?, _: String?, _: Class<*>?, _: Array<Any?>? -> filePath }
        LwjglRedirections.customRedirection["Lcom/corrodinggames/rts/gameFramework/e/c;b()Ljava/lang/String;"] =
            Redirection { _: Any?, _: String?, _: Class<*>?, _: Array<Any?>? -> filePath }

        LwjglRedirections.customRedirection["Landroid/util/Log;a(ILjava/lang/String;Ljava/lang/String;)I"] =
            Redirection { _: Any?, _: String?, _: Class<*>?, args: Array<Any?>? ->
                args?.let {
                    args[2]?.let {
                        //Log.clog(it.toString())
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

        LwjglAgent.agentmain("", this.instrumentation)
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