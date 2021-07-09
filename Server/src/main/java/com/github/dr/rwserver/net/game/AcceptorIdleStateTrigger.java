package com.github.dr.rwserver.net.game;

import com.github.dr.rwserver.net.core.AbstractNetConnect;
import com.github.dr.rwserver.util.log.Log;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import static com.github.dr.rwserver.net.game.TimeoutDetection.checkTimeoutDetection;

@ChannelHandler.Sharable
class AcceptorIdleStateTrigger extends ChannelInboundHandlerAdapter {
    private final StartNet startNet;

    protected AcceptorIdleStateTrigger(StartNet startNet) {
        this.startNet = startNet;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        Log.debug("断开一个链接",ctx.channel().id().asLongText());
        startNet.clear(ctx);
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.WRITER_IDLE) {
                Channel channel = ctx.channel();
                AbstractNetConnect con = startNet.OVER_MAP.get(channel.id().asLongText());
                if (checkTimeoutDetection(startNet,con)) {
                    startNet.clear(ctx);
                }
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }

}
