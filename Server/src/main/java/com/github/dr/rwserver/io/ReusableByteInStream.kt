package com.github.dr.rwserver.io

import java.io.ByteArrayInputStream

/**
 * @author Dr
 */
class ReusableByteInStream : ByteArrayInputStream(ByteArray(0)) {
    fun position(): Int {
        return pos
    }

    fun setBytes(bytes: ByteArray) {
        pos = 0
        count = bytes.size
        mark = 0
        buf = bytes
    }

    fun setBytes(bytes: ByteArray, offset: Int, length: Int) {
        buf = bytes
        pos = offset
        count = Math.min(offset + length, bytes.size)
        mark = offset
    }
}