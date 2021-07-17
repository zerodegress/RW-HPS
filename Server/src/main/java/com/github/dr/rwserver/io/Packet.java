package com.github.dr.rwserver.io;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;

/**
 * @author Dr
 */
public class Packet {
    public final byte[] bytes;
    public final int type;

    public Packet(int type,@NotNull byte[] bytes) {
        this.type = type;
        this.bytes = bytes;
    }

    @Override
    public String toString() {
        return "Packet{" +
                "bytes=" + Arrays.toString(bytes) +
                ", type=" + type +
                '}';
    }
}