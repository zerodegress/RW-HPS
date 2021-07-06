package com.github.dr.rwserver.net.game;

import com.github.dr.rwserver.data.global.NetStaticData;
import com.github.dr.rwserver.io.Packet;
import com.github.dr.rwserver.util.log.Log;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.ReferenceCountUtil;

import java.util.List;

/**
 * @author Dr
 */
class PacketDecoder extends ByteToMessageDecoder {
    private static final int HEADER_SIZE = 8;
    private static final int MAX_CONTENT_LENGTH = 10485760;

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf bufferIn, List<Object> out) throws Exception {
        if (bufferIn == null) {
            return;
        }
        if (bufferIn.readableBytes() < HEADER_SIZE) {
            return;
        }
        final String addSock = ctx.channel().remoteAddress().toString();
        final String ip = addSock.substring(1, addSock.indexOf(':'));
        if (NetStaticData.blackList.containsBlackList(ip)) {
            ReferenceCountUtil.release(bufferIn);
            ctx.close();
        }
        //消息长度

        //final int maxContentLength = 40960;
        // 10MB
        if (bufferIn.readableBytes() > MAX_CONTENT_LENGTH) {
            Log.error("MAX Packet");
            ReferenceCountUtil.release(bufferIn);
            NetStaticData.blackList.addBlackList(ip);
            Log.warn("BlackList", ip);
            ctx.close();
            return;
        }
        final int readerIndex = bufferIn.readerIndex();
        final int contentLength = bufferIn.readInt();
        int type = bufferIn.readInt();
        if (bufferIn.readableBytes() < contentLength) {
            bufferIn.readerIndex(readerIndex);
            return;
        }
        byte[] b = new byte[contentLength];
        bufferIn.readBytes(b);
        out.add(new Packet(type, b));
    }
}
