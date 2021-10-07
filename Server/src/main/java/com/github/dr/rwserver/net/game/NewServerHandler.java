package com.github.dr.rwserver.net.game;

import com.github.dr.rwserver.core.Call;
import com.github.dr.rwserver.core.thread.Threads;
import com.github.dr.rwserver.data.Player;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.ga.GroupGame;
import com.github.dr.rwserver.game.EventType;
import com.github.dr.rwserver.io.Packet;
import com.github.dr.rwserver.net.core.AbstractNetConnect;
import com.github.dr.rwserver.net.core.TypeConnect;
import com.github.dr.rwserver.net.game.cal.CalUt;
import com.github.dr.rwserver.net.game.cal.ChannelInfo;
import com.github.dr.rwserver.util.game.Events;
import com.github.dr.rwserver.util.log.Log;
import io.netty.channel.*;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.github.dr.rwserver.net.game.cal.CalUt.channelInfos;

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
                    channelInfos.put(channel.id(),new ChannelInfo(channel,System.currentTimeMillis()));
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
        loseDeal(ctx);
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        if(!cause.getMessage().contains("Connection reset")){
            cause.printStackTrace();
        }
        loseDeal(ctx);
        super.exceptionCaught(ctx,cause);
    }
    private void loseDeal(ChannelHandlerContext ctx){
        if(ctx.channel().hasAttr(NETTY_CHANNEL_KEY)){
            AbstractNetConnect con=ctx.channel().attr(NETTY_CHANNEL_KEY).get();
            if(con!=null){
                con.disconnect();
            }
        }
        if(ctx.channel().hasAttr(GroupGame.G_KEY)) {
            int gid=ctx.channel().attr(GroupGame.G_KEY).get();
            if(GroupGame.games.get(gid).isStartGame){
                List<Player> players = GroupGame.playersByGid(Data.playerGroup, gid);
                if(players.isEmpty()){
                    Log.clog("组"+gid+"玩家全部离开，结束游戏");
                    Events.fire(new EventType.GameOverEvent(gid));
                }else if (players.size()==1&&!Threads.getIfScheduledFutureData("Gameover-t"+gid)){
                    Call.sendSystemMessageLocal("gameOver.oneMin",gid);
                    Log.clog("组"+gid+"进入1分钟结束倒计时");
                    Threads.newThreadService(() -> Events.fire(new EventType.GameOverEvent(gid)),1, TimeUnit.MINUTES,"Gameover"+gid);
                }else {
                    if(players.stream().map(p->p.team).distinct().toArray().length==1){
                        Call.sendSystemMessageLocal("gameOver.oneTeam",gid);
                        Log.clog("队伍离开，组"+gid+"进入2分钟结束倒计时");
                        Threads.newThreadService(() -> Events.fire(new EventType.GameOverEvent(gid)),2, TimeUnit.MINUTES,"Gameover-t"+gid);
                    }
                }
            }
        }
        CalUt.leave(ctx.channel().id());
    }
}
