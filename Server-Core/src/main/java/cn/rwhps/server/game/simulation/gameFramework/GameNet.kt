/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.game.simulation.gameFramework

import cn.rwhps.server.data.global.Data
import cn.rwhps.server.util.log.Log
import com.corrodinggames.rts.gameFramework.j.ad
import com.corrodinggames.rts.gameFramework.j.c
import java.io.IOException

object GameNet {
    fun newConnect() {
        try {
            val settingsEngine = GameEngine.settingsEngine
            val netEngine = GameEngine.netEngine

            settingsEngine.lastNetworkPlayerName = Data.headlessName
            val playerName = settingsEngine.lastNetworkPlayerName
            /*
            val c = a.a().c()
            if (c != null && str == null) {
                str = f.a(c.replace(" ", "_"), 20).toString()
            }
            */
            netEngine.y = playerName
            val kVar2 = ad.b("127.0.0.1:${Data.config.Port}",false)
            netEngine.a(kVar2)
            val it: Iterator<*> = netEngine.aM.iterator()
            while (it.hasNext()) {
                (it.next() as c).i = true
            }
        } catch (e2: IOException) {
            Log.error("[GameCore] NewConnect Error",e2)
        }
    }
}