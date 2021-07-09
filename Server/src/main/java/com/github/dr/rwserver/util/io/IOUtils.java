package com.github.dr.rwserver.util.io;

public class IOUtils {
    /**
     * 空字节数组.
     */
    public static final byte[] EMPTY_BYTE_ARRAY = {};

    /**
     * 表示文件结束（或流）
     */
    public static final int EOF = -1;

    /**
     * 返回指定大小的新字节数组
     * @param size 大小
     * @return 指定大小的新字节数组
     */
    public static byte[] byteArray(final int size) {
        return new byte[size];
    }
}
