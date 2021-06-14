package com.github.dr.rwserver.io;

import com.github.dr.rwserver.util.zip.gzip.GzipDecoder;

import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.DataInputStream;
import java.io.IOException;

/**
 * @author Dr
 */
public class GameInputStream implements Closeable {
	public final ByteArrayInputStream buffer;
    public final DataInputStream stream;

    public GameInputStream(Packet packet) {
        this.buffer = new ByteArrayInputStream(packet.bytes);
		this.stream = new DataInputStream(this.buffer);
    }

    public GameInputStream(byte[] bytes) {
        this.buffer = new ByteArrayInputStream(bytes);
        this.stream = new DataInputStream(this.buffer);
    }

    public int readByte() throws IOException {
        return this.stream.readByte();
    }

    public boolean readBoolean() throws IOException {
        return this.stream.readBoolean();
    }

    public int readInt() throws IOException {
        return this.stream.readInt();
    }

    public short readShort() throws IOException {
        return this.stream.readShort();
    }

    public float readFloat() throws IOException {
        return this.stream.readFloat();
    }

    public long readLong() throws IOException {
        return this.stream.readLong();
    }

    public String readString() throws IOException {
        return this.stream.readUTF();
    }

    public String isReadString() throws IOException {
        if (this.readBoolean()) {
            return this.readString();
        }
        return "";
    }

    public byte[] readStreamBytes() throws IOException {
        int n2;
        int n3 = this.readInt();
        byte[] bytes = new byte[n3];
        for (int i2 = 0; i2 < n3 && (n2 = this.stream.read(bytes, i2, n3 - i2)) != -1; i2 += n2) {
        }
        return bytes;
    }

    public byte[] readStreamBytesNew() throws IOException {
        int n2;
        this.readInt();
        int n3 = this.readInt();
        byte[] bytes = new byte[n3];
        for (int i2 = 0; i2 < n3 && (n2 = this.stream.read(bytes, i2, n3 - i2)) != -1; i2 += n2) {
        }
        return bytes;
    }

    public DataInputStream getDecodeStream(boolean bl) throws IOException{
        this.readString();
        byte[] bytes = readStreamBytes();
        GzipDecoder coder = new GzipDecoder(bl,bytes);
        return coder.stream;
    }

    public DataInputStream getStream() throws IOException{
        byte[] bytes = readStreamBytesNew();
        GzipDecoder coder = new GzipDecoder(false,bytes);
        return coder.stream;
    }

    public byte[] getDecodeBytes() throws IOException{
        try {
            this.readString();
            return readStreamBytes();
        } finally {
            close();
        }
    }

    @Override
    public String toString() {
        return "GameInputStream{" +
                "buffer=" + buffer +
                ", stream=" + stream +
                '}';
    }

    @Override
    public void close() throws IOException {
        buffer.close();
        stream.close();
    }
}
