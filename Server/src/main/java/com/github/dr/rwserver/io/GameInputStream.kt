package com.github.dr.rwserver.io

import java.io.Closeable
import java.io.ByteArrayInputStream
import java.io.DataInputStream
import kotlin.Throws
import java.io.IOException
import com.github.dr.rwserver.util.zip.gzip.GzipDecoder

/**
 * @author Dr
 */
class GameInputStream : Closeable {
    @JvmField
    val buffer: ByteArrayInputStream
    @JvmField
    val stream: DataInputStream

    constructor(packet: Packet) {
        buffer = ByteArrayInputStream(packet.bytes)
        stream = DataInputStream(buffer)
    }

    constructor(bytes: ByteArray) {
        buffer = ByteArrayInputStream(bytes)
        stream = DataInputStream(buffer)
    }

    @Throws(IOException::class)
    fun readByte(): Int {
        return stream.readByte().toInt()
    }

    @Throws(IOException::class)
    fun readBoolean(): Boolean {
        return stream.readBoolean()
    }

    @Throws(IOException::class)
    fun readInt(): Int {
        return stream.readInt()
    }

    @Throws(IOException::class)
    fun readShort(): Short {
        return stream.readShort()
    }

    @Throws(IOException::class)
    fun readFloat(): Float {
        return stream.readFloat()
    }

    @Throws(IOException::class)
    fun readLong(): Long {
        return stream.readLong()
    }

    @Throws(IOException::class)
    fun readString(): String {
        return stream.readUTF()
    }

    @Throws(IOException::class)
    fun isReadString(): String {
        return if (readBoolean()) {
                    readString()
               } else {
                   ""
               }
    }

    @Throws(IOException::class)
    fun readStreamBytes(): ByteArray {
        return buffer.readNBytes(readInt())
    }

    @Throws(IOException::class)
    fun readStreamBytesNew(): ByteArray {
        readInt()
        return readStreamBytes()
    }

    @Throws(IOException::class)
    fun getDecodeStream(bl: Boolean): DataInputStream {
        readString()
        val bytes = readStreamBytes()
        val coder = GzipDecoder(bl, bytes)
        return coder.stream
    }

    @Throws(IOException::class)
    fun getStream(): DataInputStream {
        val bytes = readStreamBytesNew()
        val coder = GzipDecoder(false, bytes)
        return coder.stream
    }

    @Throws(IOException::class)
    fun getDecodeBytes(): ByteArray {
        readString()
        return readStreamBytes()
    }


    override fun toString(): String {
        return "GameInputStream{" +
                "buffer=" + buffer +
                ", stream=" + stream +
                '}'
    }

    @Throws(IOException::class)
    override fun close() {
        buffer.close()
        stream.close()
    }
}