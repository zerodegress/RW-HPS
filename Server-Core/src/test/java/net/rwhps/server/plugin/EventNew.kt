/*
 *
 *  * Copyright 2020-2023 RW-HPS Team and contributors.
 *  *
 *  * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *  *
 *  * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 *
 */

package net.rwhps.server.plugin

import net.rwhps.server.data.EventGlobalManage
import net.rwhps.server.data.EventManage
import net.rwhps.server.game.event.EventListener
import net.rwhps.server.game.event.global.NetConnectNewEvent
import net.rwhps.server.net.core.ConnectionAgreement
import net.rwhps.server.util.annotations.core.EventListenerHandler
import org.junit.jupiter.api.Test

class EventNew {
    @Test
    fun new() {
        val a = EventGlobalManage()
        a.registerListener(TestA())

        a.fire(NetConnectNewEvent(ConnectionAgreement()))
    }


    class TestA : EventListener {
        @EventListenerHandler
        fun aaa(event: NetConnectNewEvent) {
            println(event.connectionAgreement.ip == "")
        }
    }
}