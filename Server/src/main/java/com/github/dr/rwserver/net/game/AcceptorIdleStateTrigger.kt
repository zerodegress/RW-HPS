package com.github.dr.rwserver.net.game

import com.github.dr.rwserver.util.log.Log
import com.github.dr.rwserver.util.log.Log.debug
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
//        debug("断开一个链接", ctx.channel().id().asLongText())
//        startNet.clear(ctx)
        ctx.fireChannelInactive()
    }

    @Throws(Exception::class)
    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
        if (evt is IdleStateEvent) {
            val state = evt.state()
            if (state == IdleState.WRITER_IDLE) {
                val con = ctx.channel().attr(NewServerHandler.NETTY_CHANNEL_KEY).get()
                Log.clog("闲置断开: "+con.ip+" ["+ctx.channel().toString())
                try {
                    Log.clog("闲置组"+con.player?.groupId+"玩家"+con.player?.name+"断开")
                }catch(e:Throwable){}
                ctx.close()
            }
        }
        super.userEventTriggered(ctx, evt)
    }
}