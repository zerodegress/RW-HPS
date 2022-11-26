/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.game.event

import cn.rwhps.server.net.core.ConnectionAgreement
import cn.rwhps.server.net.core.IRwHps

class EventGlobalType {
    /** 服务器初始化  */
    class GameLibLoadEvent

    /** 服务器初始化  */
    class ServerLoadEvent

    /** 启动了新协议  */
    class ServerStartTypeEvent(val serverNetType: IRwHps.NetType)

    class NewConnectEvent(val connectionAgreement: ConnectionAgreement) {
        var result = false
    }

    class NewCloseEvent(val connectionAgreement: ConnectionAgreement)
}