package com.github.dr.rwserver.io;

import com.github.dr.rwserver.util.zip.gzip.GzipEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author Dr
 */
public class GameOutputStream {
	ByteArrayOutputStream buffer = new ByteArrayOutputStream();
	DataOutputStream stream = new DataOutputStream(buffer);

    public ByteBuf createPacket() {
        try {
            this.stream.flush();
            this.buffer.flush();
            byte[] bytes = this.buffer.toByteArray();
            int len = bytes.length;
            /* 不申请堆外内存 易OOM */
            ByteBuf byteBuf = Unpooled.buffer(len,len);
            byteBuf.writeBytes(bytes);
            return byteBuf;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

	public ByteBuf createPacket(int type) {
        try {
            this.stream.flush();
            this.buffer.flush();
            byte[] bytes = this.buffer.toByteArray();
            int len = bytes.length + 8;
            /* 不申请堆外内存 易OOM */
            ByteBuf byteBuf = Unpooled.buffer(len,len);
            byteBuf.writeInt(bytes.length);
            byteBuf.writeInt(type);
            byteBuf.writeBytes(bytes);
            return byteBuf;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void writeByte(int val) throws IOException {
        this.stream.writeByte(val);
    }

    public void writeBoolean(boolean val) throws IOException {
        this.stream.writeBoolean(val);
    }

    public void writeInt(int val) throws IOException {
        this.stream.writeInt(val);
    }

    public void writeFloat(float val) throws IOException {
        this.stream.writeFloat(val);
    }

    public void writeLong(long val) throws IOException {
        this.stream.writeLong(val);
    }

    public void writeString(String val) throws IOException {
        this.stream.writeUTF(val);
    }  

    public void flushEncodeData(GzipEncoder enc) throws IOException{
        this.writeString(enc.str);
        this.writeInt(enc.buffer.size());
        enc.buffer.writeTo(this.stream);
        stream.flush();
    }

    public void flushMapData(int mapSize,byte[] bytes) throws IOException{
        this.writeInt(mapSize);
        this.stream.write(bytes);
    }

    @Override
    public String toString() {
        return "GameOutputStream{" +
                "buffer=" + buffer +
                ", stream=" + stream +
                '}';
    }
}
