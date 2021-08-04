package com.github.dr.rwserver.io

import com.github.dr.rwserver.io.output.DisableSyncByteArrayOutputStream
import com.github.dr.rwserver.util.zip.gzip.GzipEncoder
import java.io.DataOutputStream
import java.io.IOException

/**
 * @author Dr
 */
class GameOutputStream {
    val buffer = DisableSyncByteArrayOutputStream()
    val stream = DataOutputStream(buffer)

    fun createPacket(): Packet {
        try {
            stream.use { buffer.use {
                stream.flush()
                buffer.flush()
                return Packet(0, buffer.toByteArray())
            }}
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun createPacket(type: Int): Packet {
        try {
            stream.use { buffer.use {
                stream.flush()
                buffer.flush()
                return Packet(type, buffer.toByteArray())
            }}
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun createPackets(type: Int): Packet {
        try {
            stream.use { buffer.use {
                stream.flush()
                buffer.flush()
                return Packet(type, buffer.toByteArray())
            }}
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    fun getPacketBytes(): ByteArray {
        try {
            stream.use { buffer.use {
                stream.flush()
                buffer.flush()
                return buffer.toByteArray()
            }}
        } catch (e: IOException) {
            throw RuntimeException(e)
        }
    }

    @Throws(IOException::class)
    fun writeByte(value: Int) {
        stream.writeByte(value)
    }

    @Throws(IOException::class)
    fun writeBytes(value: ByteArray) {
        buffer.writeBytes(value)
    }

    @Throws(IOException::class)
    fun writeBoolean(value: Boolean) {
        stream.writeBoolean(value)
    }

    @Throws(IOException::class)
    fun writeInt(value: Int) {
        stream.writeInt(value)
    }

    @Throws(IOException::class)
    fun writeShort(value: Short) {
        stream.writeShort(value.toInt())
    }

    @Throws(IOException::class)
    fun writeFloat(value: Float) {
        stream.writeFloat(value)
    }

    @Throws(IOException::class)
    fun writeLong(value: Long) {
        stream.writeLong(value)
    }

    @Throws(IOException::class)
    fun writeString(value: String) {
        stream.writeUTF(value)
    }

    @Throws(IOException::class)
    fun writeIsString(gameInputStream: GameInputStream) {
        if (gameInputStream.readBoolean()) {
            writeBoolean(true)
            writeString(gameInputStream.readString())
        } else {
            writeBoolean(false)
        }
    }

    @Throws(IOException::class)
    fun flushData(inputStream: GameInputStream) {
        inputStream.buffer.transferTo(buffer)
    }

    @Throws(IOException::class)
    fun flushEncodeData(enc: GzipEncoder) {
        enc.close()
        writeString(enc.str!!)
        writeInt(enc.buffer.size())
        enc.buffer.writeTo(stream)
        stream.flush()
    }

    @Throws(IOException::class)
    fun flushMapData(mapSize: Int, bytes: ByteArray) {
        writeInt(mapSize)
        stream.write(bytes)
    }
}