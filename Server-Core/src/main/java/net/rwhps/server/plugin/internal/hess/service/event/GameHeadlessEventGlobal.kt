/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.plugin.internal.hess.service.event

import net.rwhps.server.data.HessModuleManage
import net.rwhps.server.data.ModManage
import net.rwhps.server.data.global.Data
import net.rwhps.server.data.global.NetStaticData
import net.rwhps.server.net.core.IRwHps
import net.rwhps.server.plugin.event.AbstractGlobalEvent
import net.rwhps.server.plugin.internal.hess.HessMain
import net.rwhps.server.util.log.Log

/**
 * @author RW-HPS/Dr
 */
class GameHeadlessEventGlobal : AbstractGlobalEvent {

    override fun registerGameLibLoadEvent(loadID: String) {
        if (HessModuleManage.hpsLoader != loadID) {
            Log.clog("Run GameHeadless ID: $loadID , This is not the first time to load, please customize the initialization")
            return
        }

        NetStaticData.ServerNetType = IRwHps.NetType.ServerProtocol
        HessMain.serverServerCommands.handleMessage("startnetservice false")

        /* Load Mod */
        Log.clog(Data.i18NBundle.getinput("server.loadMod", ModManage.load()))
        Log.clog("Load Game Core END !")

        var passwd: String? = null
        if (Data.configServer.Passwd.isNotBlank()) {
            passwd = Data.configServer.Passwd
        }
        HessModuleManage.hps.gameNet.startHessPort(Data.config.Port, passwd)
        Log.clog(Data.i18NBundle.getinput("server.load.end"))
        Log.clog("Run GameHeadless ID: $loadID")
    }
}