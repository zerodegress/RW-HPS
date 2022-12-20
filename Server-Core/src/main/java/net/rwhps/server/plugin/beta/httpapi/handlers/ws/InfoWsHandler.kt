package net.rwhps.server.plugin.beta.httpapi.handlers.ws

import io.netty.channel.Channel
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame
import net.rwhps.server.plugin.beta.httpapi.handlers.BaseWsHandler
import net.rwhps.server.plugin.beta.httpapi.responses.BaseResp
import net.rwhps.server.plugin.beta.httpapi.responses.InfoResp
import net.rwhps.server.util.inline.toPrettyPrintingJson
import java.util.*

class InfoWsHandler : BaseWsHandler() {
    private val timer = Timer()

    override fun run(msg: String, channel: Channel) {
        timer.schedule(object : TimerTask() {
            override fun run() {
                channel.writeAndFlush(TextWebSocketFrame((BaseResp(data = InfoResp())).toPrettyPrintingJson()))
            }
        }, 1000L)
    }
}