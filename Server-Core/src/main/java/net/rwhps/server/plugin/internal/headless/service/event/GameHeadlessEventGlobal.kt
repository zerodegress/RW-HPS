/*
 * Copyright 2020-2024 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.plugin.internal.headless.service.event

import net.rwhps.server.data.global.Data
import net.rwhps.server.data.global.NetStaticData
import net.rwhps.server.game.event.core.EventListenerHost
import net.rwhps.server.game.event.global.ServerHessLoadEvent
import net.rwhps.server.game.manage.HeadlessModuleManage
import net.rwhps.server.game.manage.ModManage
import net.rwhps.server.net.core.IRwHps
import net.rwhps.server.plugin.internal.headless.HessMain
import net.rwhps.server.util.annotations.core.EventListenerHandler
import net.rwhps.server.util.log.Log

/**
 * @author Dr (dr@der.kim)
 */
@Suppress("UNUSED")
class GameHeadlessEventGlobal: EventListenerHost {

    @EventListenerHandler
    fun registerGameLibLoadEvent(serverHessLoadEvent: ServerHessLoadEvent) {
        if (HeadlessModuleManage.hpsLoader != serverHessLoadEvent.loadID) {
            Log.clog(Data.i18NBundle.getinput("server.headless.oks", serverHessLoadEvent.loadID))
            return
        }

        NetStaticData.ServerNetType = IRwHps.NetType.ServerProtocol
        HessMain.serverServerCommands.handleMessage("startnetservice false")

        /* Load Mod */
        Log.clog(Data.i18NBundle.getinput("server.loadMod", ModManage.load()))
        Log.clog(Data.i18NBundle.getinput("server.headless.ok"))

        if (Data.configServer.modsLoadErrorPrint) {
            Log.clog("[${serverHessLoadEvent.loadID}]: ${serverHessLoadEvent.gameModule.gameUnitData.getRwModLoadInfo().joinToString(Data.LINE_SEPARATOR)}")
        }

        var passwd: String? = null
        if (Data.configServer.passwd.isNotBlank()) {
            passwd = Data.configServer.passwd
        }
        HeadlessModuleManage.hps.gameLinkNet.startHeadlessServer(Data.config.port, passwd)
        Log.clog(Data.i18NBundle.getinput("server.headless.run", serverHessLoadEvent.loadID))
    }
}