package com.github.dr.rwserver.net.game;

import com.github.dr.rwserver.io.Packet;
import com.github.dr.rwserver.net.ConnectionAgreement;
import com.github.dr.rwserver.net.core.AbstractNetConnect;
import com.github.dr.rwserver.net.core.TypeConnect;
import com.github.dr.rwserver.util.log.Log;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.ReferenceCountUtil;

@ChannelHandler.Sharable
class NewServerHandler extends SimpleChannelInboundHandler<Object> {
    private final StartNet startNet;
    private AbstractNetConnect abstractNetConnect;
    private TypeConnect typeConnect;

    protected NewServerHandler(StartNet startNet,AbstractNetConnect abstractNetConnect,TypeConnect typeConnect) {
        this.startNet = startNet;
        this.abstractNetConnect = abstractNetConnect;
        this.typeConnect = typeConnect;
    }

    protected void update(AbstractNetConnect abstractNetConnect,TypeConnect typeConnect) {
        this.abstractNetConnect = abstractNetConnect;
        this.typeConnect = typeConnect;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            //Log.error(ctx.channel().id().asLongText());
            if (msg instanceof Packet) {
                final Packet p = (Packet) msg;
                final Channel channel = ctx.channel();
                AbstractNetConnect con = startNet.OVER_MAP.get(channel.id().asLongText());
                if (con == null) {
                    con = abstractNetConnect.getVersionNet(channel.id().asLongText());
                    startNet.OVER_MAP.put(channel.id().asLongText(), con);
                    con.setConnectionAgreement(new ConnectionAgreement(channel));
                }
                final AbstractNetConnect finalCon = con;
                ctx.executor().execute(() -> {
                    try {
                        typeConnect.typeConnect(finalCon, p);
                    } catch (Exception e) {
                        Log.debug(e);
                        Log.error("未正确处理包，断开"+ctx.channel());
                        startNet.clear(ctx);
                    } finally {
                        ReferenceCountUtil.release(msg);
                    }
                });
            }
        } catch (Exception ss) {
            Log.error((ss));
        }
    }
}
