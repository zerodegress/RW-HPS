package com.github.dr.rwserver.io;

import com.github.dr.rwserver.util.zip.gzip.GzipDecoder;
import org.jetbrains.annotations.NotNull;

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

    public GameInputStream(@NotNull Packet packet) {
        this.buffer = new ByteArrayInputStream(packet.bytes);
		this.stream = new DataInputStream(this.buffer);
    }

    public GameInputStream(@NotNull byte[] bytes) {
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

    @NotNull
    public String readString() throws IOException {
        return this.stream.readUTF();
    }

    @NotNull
    public String isReadString() throws IOException {
        if (this.readBoolean()) {
            return this.readString();
        }
        return "";
    }

    @NotNull
    public byte[] readStreamBytes() throws IOException {
        return this.buffer.readNBytes(this.readInt());
    }

    @NotNull
    public byte[] readStreamBytesNew() throws IOException {
        this.readInt();
        return readStreamBytes();
    }

    @NotNull
    public DataInputStream getDecodeStream(boolean bl) throws IOException{
        this.readString();
        byte[] bytes = readStreamBytes();
        GzipDecoder coder = new GzipDecoder(bl,bytes);
        return coder.stream;
    }

    @NotNull
    public DataInputStream getStream() throws IOException{
        byte[] bytes = readStreamBytesNew();
        GzipDecoder coder = new GzipDecoder(false,bytes);
        return coder.stream;
    }

    @NotNull
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
