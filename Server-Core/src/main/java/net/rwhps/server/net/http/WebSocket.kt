/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.net.http

import io.netty.channel.Channel
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import net.rwhps.server.net.handler.tcp.GamePortWebSocket

/**
 * @author RW-HPS/Dr
 */
abstract class WebSocket {
    abstract fun ws(ws: GamePortWebSocket,channel: Channel,msg: String)

    protected fun error(code: Int): TextWebSocketFrame {
        return TextWebSocketFrame("错误：$code")
    }

}