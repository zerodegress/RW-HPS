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

@AsmMark.ClassLoaderCompatible
class NetPacketRedirections : MainRedirections {
    override fun register() {
        // 拦截包处理
        @GameSimulationLayer.GameSimulationLayer_KeyWords("filtered packet")
        redirect("com/corrodinggames/rts/gameFramework/j/ad00", arrayOf("b","(Lcom/corrodinggames/rts/gameFramework/j/au;)Z")) { _: Any?, _: String?, _: Class<*>?, args: Array<Any> ->
            return@redirect HessModuleManage.hps.gameFast.filteredPacket(args[0])
        }

        // 无效化游戏自带的列表服务
        @GameSimulationLayer.GameSimulationLayer_KeyWords("StartCreateOnMasterServer")
        redirect("com/corrodinggames/rts/gameFramework/j/n", arrayOf("b","()V"))
        redirect("com/corrodinggames/rts/gameFramework/j/n", arrayOf("c","()V"))
        redirect("com/corrodinggames/rts/gameFramework/j/n", arrayOf("d","()V"))

        //ListOperate_Join_GetRoomList
        redirect("com/corrodinggames/rts/gameFramework/j/q")
        //ListOperate_AddRoom
        redirect("com/corrodinggames/rts/gameFramework/j/y")
        //ListOperate_UpdateRoom
        redirect("com/corrodinggames/rts/gameFramework/j/aa")
        //SendErrorLogToGameDev
        redirect("com/corrodinggames/rts/gameFramework/j/v")

        // 无效化游戏自带的RUDP服务
        @GameSimulationLayer.GameSimulationLayer_KeyWords("ReliableServerSocket")
        redirectClass("a/a/d")
    }
}