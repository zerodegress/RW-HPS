package com.github.dr.rwserver.io

import java.util.*

/**
 * @author Dr
 */
class Packet(@JvmField val type: Int, @JvmField val bytes: ByteArray) {

    override fun toString(): String {
        return "Packet{" +
                "bytes=" + Arrays.toString(bytes) +
                ", type=" + type +
                '}'
    }
}