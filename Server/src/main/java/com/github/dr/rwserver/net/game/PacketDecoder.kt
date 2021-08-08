package com.github.dr.rwserver.net.game

import com.github.dr.rwserver.io.Packet
import com.github.dr.rwserver.util.log.Log.error
import io.netty.buffer.ByteBuf
import io.netty.channel.ChannelHandlerContext
import io.netty.handler.codec.ByteToMessageDecoder
import io.netty.util.ReferenceCountUtil

/**
 * @author Dr
 */
internal class PacketDecoder : ByteToMessageDecoder() {
    companion object {
        private const val HEADER_SIZE = 8
        private const val MAX_CONTENT_LENGTH = 52428800
    }

    @Throws(Exception::class)
    override fun decode(ctx: ChannelHandlerContext, bufferIn: ByteBuf?, out: MutableList<Any>) {
        if (bufferIn == null) {
            return
        }

        val readableBytes = bufferIn.readableBytes()
        if (readableBytes < HEADER_SIZE) {
            return
        }
        /*
        val addSock = ctx.channel().remoteAddress().toString()
        val ip = addSock.substring(1, addSock.indexOf(':'))
        if (NetStaticData.blackList.containsBlackList(ip)) {
            error("Black")
            ReferenceCountUtil.release(bufferIn)
            ctx.close()
        }
        */
        //消息长度

        // 50MB
        if (readableBytes > MAX_CONTENT_LENGTH) {
            error("MAX Packet")
            ReferenceCountUtil.release(bufferIn)
            /*
            NetStaticData.blackList.addBlackList(ip)
            debug("Add BlackList", ip)
             */
            ctx.close()
            return
        }
        val readerIndex = bufferIn.readerIndex()
        val contentLength = bufferIn.readInt()
        val type = bufferIn.readInt()
        if (bufferIn.readableBytes() < contentLength) {
            bufferIn.readerIndex(readerIndex)
            return
        }
        val b = ByteArray(contentLength)
        bufferIn.readBytes(b)
        out.add(Packet(type, b))
    }
}