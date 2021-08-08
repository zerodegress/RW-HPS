package com.github.dr.rwserver.net.game

import com.github.dr.rwserver.util.log.Log.debug
import com.github.dr.rwserver.util.log.Log.warn
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent

@Sharable
internal class AcceptorIdleStateTrigger(private val startNet: StartNet) : ChannelInboundHandlerAdapter() {
    @Throws(Exception::class)
    override fun channelActive(ctx: ChannelHandlerContext) {
        ctx.fireChannelActive()
    }

    @Throws(Exception::class)
    override fun channelInactive(ctx: ChannelHandlerContext) {
        //warn("断开一个链接", ctx.channel().id().asLongText())
        startNet.clear(ctx)
    }

    @Throws(Exception::class)
    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
        if (evt is IdleStateEvent) {
            if (evt.state() == IdleState.WRITER_IDLE) {
                val con = ctx.channel().attr(NewServerHandler.NETTY_CHANNEL_KEY).get()
                if (TimeoutDetection.checkTimeoutDetection(con)) {
                    startNet.clear(ctx)
                }
            }
        } else {
            super.userEventTriggered(ctx, evt)
        }
    }
}