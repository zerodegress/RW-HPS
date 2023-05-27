/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.dependent.redirections.game

import net.rwhps.server.data.HessModuleManage
import net.rwhps.server.dependent.redirections.MainRedirections
import net.rwhps.server.util.alone.annotations.AsmMark
import net.rwhps.server.util.alone.annotations.GameSimulationLayer

/**
 * Network blocking and proxy
 *
 * @author RW-HPS/Dr
 */
@AsmMark.ClassLoaderCompatible
class NetPacketRedirections : MainRedirections {
    override fun register() {
        // Intercept packet processing (Ineffective, there are alternatives)
        @GameSimulationLayer.GameSimulationLayer_KeyWords("filtered packet")
        redirect("com/corrodinggames/rts/gameFramework/j/ad00", arrayOf("b","(Lcom/corrodinggames/rts/gameFramework/j/au;)Z")) { _: Any?, _: String?, _: Class<*>?, args: Array<Any> ->
            return@redirect HessModuleManage.hps.gameFast.filteredPacket(args[0])
        }

        // Invalidate the listing service that comes with the game
        @GameSimulationLayer.GameSimulationLayer_KeyWords("StartCreateOnMasterServer")
        redirect("com/corrodinggames/rts/gameFramework/j/n", arrayOf("b","()V"))
        redirect("com/corrodinggames/rts/gameFramework/j/n", arrayOf("c","()V"))
        redirect("com/corrodinggames/rts/gameFramework/j/n", arrayOf("d","()V"))

        // Invalidate the RUDP service that comes with the game
        @GameSimulationLayer.GameSimulationLayer_KeyWords("ReliableServerSocket")
        redirectClass("a/a/d")

        // Remove the official Socket Launcher
        redirectClass("com/corrodinggames/rts/gameFramework/j/ao")
        redirectClass("com/corrodinggames/rts/gameFramework/j/d")
        redirectClass("com/corrodinggames/rts/gameFramework/j/e")

    }
}