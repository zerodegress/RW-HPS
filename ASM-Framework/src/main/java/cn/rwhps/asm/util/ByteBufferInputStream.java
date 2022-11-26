/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.asm.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;

/**
 * {@link InputStream} wrapping a {@link ByteBuffer}. Note that every read will
 * advance the buffer.
 */
// TODO: we should/could also read without advancing the buffer
public class ByteBufferInputStream extends InputStream {
    private final ByteBuffer buffer;

    public ByteBufferInputStream(ByteBuffer buffer) {
        this.buffer = buffer;
    }

    @Override
    public int read() throws IOException {
        if (!buffer.hasRemaining()) {
            return -1;
        }

        return buffer.get() & 0xFF;
    }

    @Override
    @SuppressWarnings("NullableProblems")
    public int read(byte[] bytes, int off, int len) throws IOException {
        if (!buffer.hasRemaining()) {
            return -1;
        }

        len = Math.min(len, buffer.remaining());
        buffer.get(bytes, off, len);
        return len;
    }

    @Override
    public int available() {
        return buffer.remaining();
    }

}
