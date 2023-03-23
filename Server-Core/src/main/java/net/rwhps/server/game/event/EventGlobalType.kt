/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.game.event

import net.rwhps.server.net.core.ConnectionAgreement
import net.rwhps.server.net.core.IRwHps

/**
 * 服务器全局事件
 *
 * @author RW-HPS/Dr
 */
class EventGlobalType {
    /** 服务器初始化  */
    class GameLibLoadEvent(val loadID: String)

    /** 服务器初始化  */
    class ServerLoadEvent

    /** 启动了新协议  */
    class ServerStartTypeEvent(val serverNetType: IRwHps.NetType)

    class NewConnectEvent(val connectionAgreement: ConnectionAgreement) {
        var result = false
    }

    class NewCloseEvent(val connectionAgreement: ConnectionAgreement)
}