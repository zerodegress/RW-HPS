package com.github.dr.rwserver.io;

import java.util.Arrays;

/**
 * @author Dr
 */
public class Packet {
    public byte[] bytes;
    public int type;

    public Packet(int type) {
        this.type = type;
    }

    public Packet(int type,byte[] bytes) {
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