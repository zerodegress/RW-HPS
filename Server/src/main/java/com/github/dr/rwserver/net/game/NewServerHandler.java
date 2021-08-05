package com.github.dr.rwserver.net.game;

import com.github.dr.rwserver.ga.GroupGame;
import com.github.dr.rwserver.io.Packet;
import com.github.dr.rwserver.net.core.AbstractNetConnect;
import com.github.dr.rwserver.net.core.TypeConnect;
import com.github.dr.rwserver.util.log.Log;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;

@ChannelHandler.Sharable
class NewServerHandler extends SimpleChannelInboundHandler<Object> {
    protected static final AttributeKey<AbstractNetConnect> NETTY_CHANNEL_KEY = AttributeKey.valueOf("User-Net");

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
/*
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof Packet) {
                final Packet p = (Packet) msg;
                final Channel channel = ctx.channel();
                AbstractNetConnect con = startNet.OVER_MAP.get(channel.id().asLongText());
                if (con == null) {
                    con = abstractNetConnect.getVersionNet(channel.id().asLongText());
                    startNet.OVER_MAP.put(channel.id().asLongText(), con);
                    con.setConnectionAgreement(new ConnectionAgreement(ctx,channel,startNet));
                }
                final AbstractNetConnect finalCon = con;
                ctx.executor().execute(() -> {
                    try {
                        typeConnect.typeConnect(finalCon, p);
                    } catch (Exception e) {
                        Log.debug(e);
                        finalCon.disconnect();
                    } finally {
                        ReferenceCountUtil.release(msg);
                    }
                });
            }
        } catch (Exception ss) {
            Log.error(ss);
        }
    }
*/
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof Packet) {
                final Packet p = (Packet) msg;
                final Channel channel = ctx.channel();
                Attribute<AbstractNetConnect> attr = channel.attr(NETTY_CHANNEL_KEY);
                AbstractNetConnect con = attr.get();
                if (con == null) {
                    con = abstractNetConnect.getVersionNet(new ConnectionAgreement(ctx,channel,startNet));
                    attr.setIfAbsent(con);
                }

                final AbstractNetConnect finalCon = con;
                ctx.executor().execute(() -> {
                    try {
                        typeConnect.typeConnect(finalCon, p);
                    } catch (Exception e) {
                        Log.debug(e);
                        finalCon.disconnect();
                    } finally {
                        ReferenceCountUtil.release(msg);
                    }
                });
            }
        } catch (Exception ss) {
            Log.error(ss);
        }
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        int gid = (int) ctx.channel().attr(AttributeKey.valueOf(GroupGame.GID)).get();

        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
        Log.error(new RuntimeException());
        Log.error(cause);
    }
}
