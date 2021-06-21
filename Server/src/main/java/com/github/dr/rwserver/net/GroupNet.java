package com.github.dr.rwserver.net;

import com.github.dr.rwserver.io.Packet;
import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.util.log.Log;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Dr
 */
public class GroupNet {
    private final ChannelGroup CHANNEL_GROUP = new DefaultChannelGroup("ChannelGroups", GlobalEventExecutor.INSTANCE);
    private final ExecutorService SINGLE_TCP_THREAD_EXECUTOR = Executors.newSingleThreadExecutor();
    private final ExecutorService SINGLE_UDP_THREAD_EXECUTOR = Executors.newSingleThreadExecutor();
    private final Seq<ConnectionAgreement> PROTOCOL = new Seq<>(8);

    public GroupNet(long a) {
    }
    /**
     * 加入一个用户到群发列表
     * @param channel 通道
     */
    public void add(Channel channel) {
        CHANNEL_GROUP.add(channel);
    }
    /**
     * 加入一个用户到群发列表
     * @param connectionAgreement UDP通道
     */
    public void add(ConnectionAgreement connectionAgreement) {
        PROTOCOL.add(connectionAgreement);
    }

    public void broadcast(Object msg) {
        broadcast(msg,null);
    }

        /**
         * 群发一条消息
         * @param msg 消息
         */
    public void broadcast(Object msg,final String str) {
        SINGLE_TCP_THREAD_EXECUTOR.execute(() -> CHANNEL_GROUP.writeAndFlush(msg));
        SINGLE_UDP_THREAD_EXECUTOR.execute(() -> {
            PROTOCOL.each(e -> {
                try {
                    e.send((Packet) msg);
                } catch (IOException ioException) {
                    PROTOCOL.remove(e);
                    try {
                        e.close(this);
                    } catch (IOException exception) {
                        Log.error("[Server] UDP GrepNet Error Close", e);
                    }
                }
            });
        });
    }

    /**
     * 删除一个TCP通道
     * @param channel Channel
     */
    public void remove(Channel channel) {
        CHANNEL_GROUP.remove(channel);
    }

    /**
     * 删除一个UDP通道
     * @param connectionAgreement Protocol
     */
    public void remove(ConnectionAgreement connectionAgreement) {
        PROTOCOL.remove(connectionAgreement);
    }

    /**
     * 刷新操作
     * @return 是否成功
     */
    public ChannelGroup flush() {
        return CHANNEL_GROUP.flush();
    }

    public void disconnect() {
        CHANNEL_GROUP.disconnect();
    }
}
