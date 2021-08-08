package com.github.dr.rwserver.net.game

import com.github.dr.rwserver.io.Packet
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.MessageToByteEncoder

internal class PacketEncoder : MessageToByteEncoder<Packet>() {
    @Throws(Exception::class)
    override fun encode(p1: ChannelHandlerContext, msg: Packet, out: ByteBuf) {
        out.writeInt(msg.bytes.size)
        out.writeInt(msg.type)
        out.writeBytes(msg.bytes)
    }
}