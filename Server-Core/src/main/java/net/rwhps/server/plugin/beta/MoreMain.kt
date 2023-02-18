/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.plugin.beta

import net.rwhps.server.data.HessModuleManage
import net.rwhps.server.data.global.Data
import net.rwhps.server.func.StrCons
import net.rwhps.server.game.GameStartInit
import net.rwhps.server.plugin.Plugin
import net.rwhps.server.plugin.event.AbstractGlobalEvent
import net.rwhps.server.util.GameModularLoadClass
import net.rwhps.server.util.game.CommandHandler
import net.rwhps.server.util.log.Log

/**
 * Test multi-loading
 *
 * Refer to multi-terminal loading
 *
 * @author RW-HPS/Dr
 */
class MoreMain : Plugin() {
    override fun registerCoreCommands(handler: CommandHandler) {
        handler.register("two", "HIDE") { _: Array<String>?, log: StrCons ->
            val load = GameModularLoadClass(
                Thread.currentThread().contextClassLoader,
                Thread.currentThread().contextClassLoader.parent,
                (HessModuleManage.hps.useClassLoader as GameModularLoadClass).classPathMapData
            )
            GameStartInit.start(load)
        }
    }

    override fun registerGlobalEvents(): AbstractGlobalEvent {
        return object: AbstractGlobalEvent {
            override fun registerGameLibLoadEvent(loadID: String) {
                if (HessModuleManage.hpsLoader != loadID) {
                    Log.clog("GameHeadless ID: $loadID  is initialized and a new server is started")
                    HessModuleManage.hessLoaderMap[loadID].gameNet.startHessPort(Data.config.Port+1)
                    return
                }
            }
        }
    }
}