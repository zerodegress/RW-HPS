package com.github.dr.rwserver.net.game;

import com.github.dr.rwserver.data.global.NetStaticData;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleStateHandler;

import java.util.concurrent.TimeUnit;

@ChannelHandler.Sharable
class StartGameNetTcp extends ChannelInitializer<SocketChannel> {
    private final AcceptorIdleStateTrigger idleStateTrigger;
    private final StartNet startNet;

    private NewServerHandler newServerHandler = null;

    protected StartGameNetTcp(StartNet startNet) {
        this.idleStateTrigger = new AcceptorIdleStateTrigger(startNet);
        this.startNet = startNet;
        newServerHandler = new NewServerHandler(startNet,NetStaticData.protocolData.abstractNetConnect,NetStaticData.protocolData.typeConnect);
    }

    public final void updateNet() {
        newServerHandler.update(NetStaticData.protocolData.abstractNetConnect,NetStaticData.protocolData.typeConnect);
    }

    @Override
    protected void initChannel(SocketChannel socketChannel) throws Exception {
        ChannelPipeline pipeline = socketChannel.pipeline();
        pipeline.addLast(new IdleStateHandler(0, 5, 0, TimeUnit.SECONDS));
        pipeline.addLast(idleStateTrigger);
        pipeline.addLast(new PacketDecoder());
        pipeline.addLast(new PacketEncoder());
        pipeline.addLast(newServerHandler);
    }
}
