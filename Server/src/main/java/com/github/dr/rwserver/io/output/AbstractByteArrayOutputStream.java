package com.github.dr.rwserver.io.output;

import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.util.io.IOUtils;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

import static com.github.dr.rwserver.util.io.IOUtils.EOF;

/**
 * 实现输出流的基类，其中数据写入字节数组。缓冲区会随着数据的增长而自动增长
 * 忽略 {@link AbstractByteArrayOutputStream} 无效中的方法 {@link java.io.ByteArrayOutputStream#toString(int)}
 * 可以在流关闭后调用生成一个 {@code IOException}.
 * {@link AbstractByteArrayOutputStream}是替代实现的基础 原来的实现是{@link java.io.ByteArrayOutputStream}
 * {@link java.io.ByteArrayOutputStream}在开始时只分配 32 个字节
 * 与{@link java.io.ByteArrayOutputStream}不同的是它没有重新分配整个内存块，但分配额外的缓冲区
 * 这样就不需要对缓冲区进行垃圾收集，而且内容也没有复制到新得到缓冲区
 */
public abstract class AbstractByteArrayOutputStream extends OutputStream {

    /** 默认缓冲区大小 */
    protected static final int DEFAULT_SIZE = 512;

    /** 缓冲区列表，它会增长并且永远不会减少 */
    private final Seq<byte[]> buffers = new Seq<>(2);
    /** 当前缓冲区的索引 */
    private int currentBufferIndex;
    /** 填充缓冲区中所有的总字节数 */
    private int filledBufferSum;
    /** 当前缓冲区 */
    private byte[] currentBuffer;
    /** 写入的总字节数 */
    protected int count;
    /** 缓冲区是否可以在重置后重新使用的标志 */
    private boolean reuseBuffers = true;

    /**
     * 通过分配一个新的或回收一个现有的使新缓冲区可用
     * @param newCount  缓冲区的大小（如果创建一个）
     */
    protected void needNewBuffer(final int newCount) {
        if (currentBufferIndex < buffers.size() - 1) {
            //回收旧缓冲区
            filledBufferSum += currentBuffer.length;
            currentBufferIndex++;
            currentBuffer = buffers.get(currentBufferIndex);
        } else {
            //创建新缓冲区
            final int newBufferSize;
            if (currentBuffer == null) {
                newBufferSize = newCount;
                filledBufferSum = 0;
            } else {
                newBufferSize = Math.max(
                        currentBuffer.length << 1,
                        newCount - filledBufferSum);
                filledBufferSum += currentBuffer.length;
            }

            currentBufferIndex++;
            currentBuffer = new byte[newBufferSize];
            buffers.add(currentBuffer);
        }
    }

    /**
     * 将字节写入字节数组
     * @param b 要写入的字节
     * @param off 起始偏移
     * @param len 要写入的字节数
     */
    @Override
    public abstract void write(@NotNull final byte[] b, final int off, final int len);

    /**
     * 将数组写入字节数组
     * @param b 要写入的数组
     */
    public abstract void writeBytes(@NotNull final byte[] b);

    /**
     * 将字节写入字节数组
     * @param b 要写入的字节
     * @param off 起始偏移
     * @param len 要写入的字节数
     */
    protected void writeImpl(@NotNull final byte[] b, final int off, final int len) {
        final int newCount = count + len;
        int remaining = len;
        int inBufferPos = count - filledBufferSum;
        while (remaining > 0) {
            final int part = Math.min(remaining, currentBuffer.length - inBufferPos);
            System.arraycopy(b, off + len - remaining, currentBuffer, inBufferPos, part);
            remaining -= part;
            if (remaining > 0) {
                needNewBuffer(newCount);
                inBufferPos = 0;
            }
        }
        count = newCount;
    }

    /**
     * 写一个字节到数组
     * @param b 要写入的字节
     */
    @Override
    public abstract void write(final int b);

    /**
     * 写一个字节到数组
     * @param b 要写入的字节
     */
    protected void writeImpl(final int b) {
        int inBufferPos = count - filledBufferSum;
        if (inBufferPos == currentBuffer.length) {
            needNewBuffer(count + 1);
            inBufferPos = 0;
        }
        currentBuffer[inBufferPos] = (byte) b;
        count++;
    }


    /**
     * 将指定输入流的全部内容写入这个字节流 输入流中的字节直接读入到这个流的内部缓冲区.
     * @param in 要从中读取的输入流
     * @return 从输入流读取的总字节数(和写入这个流的字节数)
     * @throws IOException 如果在读取输入流时发生IO错误
     */
    public abstract int write(@NotNull final InputStream in) throws IOException;

    /**
     * 将指定输入流的全部内容写入这个字节流 输入流中的字节直接读入到这个流的内部缓冲区.
     * @param in 要从中读取的输入流
     * @return 从输入流读取的总字节数(和写入这个流的字节数)
     * @throws IOException 如果在读取输入流时发生IO错误
     */
    protected int writeImpl(@NotNull final InputStream in) throws IOException {
        int readCount = 0;
        int inBufferPos = count - filledBufferSum;
        int n = in.read(currentBuffer, inBufferPos, currentBuffer.length - inBufferPos);
        while (n != EOF) {
            readCount += n;
            inBufferPos += n;
            count += n;
            if (inBufferPos == currentBuffer.length) {
                needNewBuffer(currentBuffer.length);
                inBufferPos = 0;
            }
            n = in.read(currentBuffer, inBufferPos, currentBuffer.length - inBufferPos);
        }
        return readCount;
    }

    /**
     * 返回字节数组的当前大小
     * @return 字节数组的当前大小
     */
    public abstract int size();

    /**
     * 他和 {@link java.io.ByteArrayOutputStream} 一样
     * 此类中的方法可以在流关闭后调用，而不会抛出 {@code IOException}.
     * @throws IOException 不会出现Error（此方法不应声明此异常 但由于向后兼容 现在必须这样做）
     */
    @Override
    public void close() throws IOException {
        this.buffers.clear();
        currentBuffer = null;
    }

    /**
     * @see java.io.ByteArrayOutputStream#reset()
     */
    public abstract void reset();

    /**
     * @see java.io.ByteArrayOutputStream#reset()
     */
    protected void resetImpl() {
        count = 0;
        filledBufferSum = 0;
        currentBufferIndex = 0;
        if (reuseBuffers) {
            currentBuffer = buffers.get(currentBufferIndex);
        } else {
            //扔掉旧的缓冲器
            currentBuffer = null;
            final int size = buffers.get(0).length;
            buffers.clear();
            needNewBuffer(size);
            reuseBuffers = true;
        }
    }

    /**
     * 将此字节流的全部内容写入指定的输出流。
     * @param out  要写入的输出流
     * @throws IOException 如果发生IO错误，例如流已关闭
     * @see java.io.ByteArrayOutputStream#writeTo(OutputStream)
     */
    public abstract void writeTo(@NotNull final OutputStream out) throws IOException;

    /**
     * 将此字节流的全部内容写入指定的输出流。
     * @param out  要写入的输出流
     * @throws IOException 如果发生IO错误，例如流已关闭
     * @see java.io.ByteArrayOutputStream#writeTo(OutputStream)
     */
    protected void writeToImpl(@NotNull final OutputStream out) throws IOException {
        int remaining = count;
        for (final byte[] buf : buffers) {
            final int c = Math.min(buf.length, remaining);
            out.write(buf, 0, c);
            remaining -= c;
            if (remaining == 0) {
                break;
            }
        }
    }

    /**
     * 以字节数组的形式获取此字节流的当前内容 结果是独立这个流的
     * @return 输出这个流的当前内容，为字节数组
     * @see java.io.ByteArrayOutputStream#toByteArray()
     */
    public abstract byte[] toByteArray();

    /**
     * 以字节数组的形式获取此字节流的当前内容 结果是独立这个流的
     * @return 输出这个流的当前内容，为字节数组
     * @see java.io.ByteArrayOutputStream#toByteArray()
     */
    protected byte[] toByteArrayImpl() {
        int remaining = count;
        if (remaining == 0) {
            return IOUtils.EMPTY_BYTE_ARRAY;
        }
        final byte[] newBuf = new byte[remaining];
        int pos = 0;
        for (final byte[] buf : buffers) {
            final int c = Math.min(buf.length, remaining);
            System.arraycopy(buf, 0, newBuf, pos, c);
            pos += c;
            remaining -= c;
            if (remaining == 0) {
                break;
            }
        }
        return newBuf;
    }

    /**
     * 以字符串形式获取此字节流的当前内容 使用JVM平台默认字符集.
     * 不建议使用 因为将被抛弃
     * @return 字节数组转字符串的内容
     * @see java.io.ByteArrayOutputStream#toString()
     */
    @Override
    @Deprecated
    public String toString() {
        return new String(toByteArray(), Charset.defaultCharset());
    }

    /**
     * 以字符串形式获取此字节流的当前内容 使用自定义的字符集.
     * @param enc 自定义的字符集
     * @return 字节数组转字符串的内容
     * @throws UnsupportedEncodingException 如果不支持编码 就会抛出这个异常
     * @see java.io.ByteArrayOutputStream#toString()
     */
    public String toString(@NotNull final String enc) throws UnsupportedEncodingException {
        return new String(toByteArray(), enc);
    }

    /**
     * 以字符串形式获取此字节流的当前内容 使用自定义的字符集.
     * @param charset 自定义的Charset字符集
     * @return 字节数组转字符串的内容
     * @see java.io.ByteArrayOutputStream#toString()
     */
    public String toString(@NotNull final Charset charset) {
        return new String(toByteArray(), charset);
    }

}
