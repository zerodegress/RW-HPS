/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.net.handler.tcp

import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.handler.timeout.IdleState
import io.netty.handler.timeout.IdleStateEvent
import io.netty.util.AttributeKey
import net.rwhps.server.net.core.TypeConnect
import net.rwhps.server.net.handler.rudp.TimeoutDetection
import java.net.SocketException
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author RW-HPS/Dr
 */
@Sharable
open class AcceptorIdleStateTrigger : ChannelInboundHandlerAdapter() {
    val connectNum: AtomicInteger = AtomicInteger()

    @Throws(Exception::class)
    override fun channelActive(ctx: ChannelHandlerContext) {
        ctx.fireChannelActive()
    }

    @Throws(java.lang.Exception::class)
    override fun channelRegistered(ctx: ChannelHandlerContext?) {
        try {
            super.channelRegistered(ctx)
        } finally {
            connectNum.incrementAndGet()
        }
    }

    @Throws(java.lang.Exception::class)
    override fun channelUnregistered(ctx: ChannelHandlerContext?) {
        try {
            super.channelUnregistered(ctx)
        } finally {
            connectNum.decrementAndGet()
        }
    }

    @Throws(Exception::class)
    override fun channelInactive(ctx: ChannelHandlerContext) {
        //debug("break a link", ctx.channel().id().asLongText())
        clear(ctx)
    }

    @Throws(Exception::class)
    override fun userEventTriggered(ctx: ChannelHandlerContext, evt: Any) {
        if (evt is IdleStateEvent) {
            if (evt.state() == IdleState.WRITER_IDLE) {
                val con: TypeConnect? = ctx.channel().attr(NewServerHandler.NETTY_CHANNEL_KEY).get()
                if (TimeoutDetection.checkTimeoutDetection(con)) {
                    clear(ctx)
                }
            }
        } else {
            super.userEventTriggered(ctx, evt)
        }
    }

    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable?) {
        // The remote host forcibly closed an existing connection
        if (cause is SocketException) {
            clear(ctx)
        } else {
//            cause?.let {
//                Log.error(Log.resolveTrace(it))
//            }
        }
    }

    protected open fun getAttributeKey(): AttributeKey<TypeConnect> {
        return NewServerHandler.NETTY_CHANNEL_KEY
    }

    /**
     * Clean up connections and release resources
     * @param ctx ChannelHandlerContext
     */
    internal fun clear(ctx: ChannelHandlerContext) {
        val channel = ctx.channel()
        try {
            val con = channel.attr(getAttributeKey()).get()
            if (con != null) {
                con.abstractNetConnect.disconnect()
            } else {
                channel.close()
                ctx.close()
            }
        } finally {
            //OVER_MAP.remove(channel.id().asLongText())
        }
    }
}