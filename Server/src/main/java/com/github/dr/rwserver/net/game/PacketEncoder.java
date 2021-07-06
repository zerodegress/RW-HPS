package com.github.dr.rwserver.net.game;

import com.github.dr.rwserver.io.Packet;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class PacketEncoder extends MessageToByteEncoder<Packet> {
    @Override
    protected void encode(ChannelHandlerContext p1, Packet msg, ByteBuf out) throws Exception {
        out.writeInt(msg.bytes.length);
        out.writeInt(msg.type);
        out.writeBytes(msg.bytes);
    }
}