package com.github.dr.rwserver.net;

import com.github.dr.rwserver.core.ex.Threads;
import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.util.log.Log;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.io.IOException;

/**
 * @author Dr
 */
public class GroupNet {
    private static final ChannelGroup CHANNEL_GROUP = new DefaultChannelGroup("ChannelGroups", GlobalEventExecutor.INSTANCE);
    private static final Seq<Protocol> PROTOCOL = new Seq<>(8);

    /**
     * 加入一个用户到群发列表
     * @param channel 通道
     */
    public static void add(Channel channel) {
        CHANNEL_GROUP.add(channel);
    }
    /**
     * 加入一个用户到群发列表
     * @param protocol UDP通道
     */
    public static void add(Protocol protocol) {
        PROTOCOL.add(protocol);
    }

    /**
     * 群发一条消息
     * @param msg 消息
     */
    public static void broadcast(Object msg) {
        Threads.newThreadPlayer2(() -> PROTOCOL.each(e -> {
            try {
                e.send((ByteBuf) msg);
            } catch (IOException ioException) {
                PROTOCOL.remove(e);
                try {
                    e.close();
                } catch (IOException exception) {
                    Log.error("[Server] UDP GrepNet Error Close",e);
                }
            }
        }));
        CHANNEL_GROUP.writeAndFlush(msg);
    }

    /**
     * 删除一个TCP通道
     * @param channel Channel
     */
    public static void remove(Channel channel) {
        CHANNEL_GROUP.remove(channel);
    }

    /**
     * 删除一个UDP通道
     * @param protocol Protocol
     */
    public static void remove(Protocol protocol) {
        PROTOCOL.remove(protocol);
    }

    /**
     * 刷新操作
     * @return 是否成功
     */
    public static ChannelGroup flush() {
        return CHANNEL_GROUP.flush();
    }
}
