package com.github.dr.rwserver.io.output;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 线程安全的版本
 * 继承{@link AbstractByteArrayOutputStream}
 * @author Dr
 */
public class ByteArrayOutputStream extends AbstractByteArrayOutputStream {
    /**
     * 创建新的字节数组输出流 缓冲容量为 {@value AbstractByteArrayOutputStream#DEFAULT_SIZE} 字节 尽管它的大小在必要时会增加
     * 默认为512bytes
     */
    public ByteArrayOutputStream() {
        this(DEFAULT_SIZE);
    }

    /**
     * 创建新的字节数组输出流 其缓冲区容量为指定大小（以字节为单位）。
     * @param size 初始大小
     * @throws IllegalArgumentException 大小是负数
     */
    public ByteArrayOutputStream(final int size) {
        if (size < 0) {
            throw new IllegalArgumentException("Negative initial size: " + size);
        }
        synchronized (this) {
            needNewBuffer(size);
        }
    }

    @Override
    public void write(@NotNull final byte[] b, final int off, final int len) {
        if ((off < 0)
                || (off > b.length)
                || (len < 0)
                || ((off + len) > b.length)
                || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        }
        if (len == 0) {
            return;
        }
        synchronized (this) {
            writeImpl(b, off, len);
        }
    }

    @Override
    public synchronized void writeBytes(@NotNull final byte[] b) {
        write(b, 0, b.length);
    }

    @Override
    public synchronized void write(final int b) {
        writeImpl(b);
    }

    @Override
    public synchronized int write(@NotNull final InputStream in) throws IOException {
        return writeImpl(in);
    }

    @Override
    public synchronized int size() {
        return count;
    }

    @Override
    public synchronized void reset() {
        resetImpl();
    }

    @Override
    public synchronized void writeTo(@NotNull final OutputStream out) throws IOException {
        writeToImpl(out);
    }

    @Override
    public synchronized byte[] toByteArray() {
        return toByteArrayImpl();
    }
}
