package com.github.dr.rwserver.util

import com.github.dr.rwserver.data.global.Data
import java.lang.StringBuffer
import java.nio.charset.StandardCharsets
import kotlin.experimental.and

/**
 * @author Dr
 */
object ExtractUtil {
    @JvmStatic
    fun stringToUtf8(string: String): String {
        // 用指定编码转换String为byte[]:
        return String(string.toByteArray(StandardCharsets.ISO_8859_1), Data.UTF_8)
    }

    /**
     * 合并byte数组
     */
    @JvmStatic
    fun unitByteArray(byte1: ByteArray, byte2: ByteArray): ByteArray {
        val unitByte = ByteArray(byte1.size + byte2.size)
        System.arraycopy(byte1, 0, unitByte, 0, byte1.size)
        System.arraycopy(byte2, 0, unitByte, byte1.size, byte2.size)
        return unitByte
    }

    @JvmStatic
	fun hexToByteArray(inHex: String): ByteArray {
        var inHex = inHex
        inHex = inHex.replace(" ", "")
        var hexLength = inHex.length
        val result: ByteArray
        if (hexLength % 2 != 1) {
            //偶数
            result = ByteArray(hexLength / 2)
        } else {
            //奇数
            hexLength++
            result = ByteArray(hexLength / 2)
            inHex = "0$inHex"
        }
        var j = 0
        var i = 0
        while (i < hexLength) {
            result[j] = inHex.substring(i, i + 2).toInt(16).toByte()
            j++
            i += 2
        }
        return result
    }

    @JvmStatic
	fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuffer()
        for (aByte in bytes) {
            val hex = Integer.toHexString((aByte and 0xFF.toByte()).toInt())
            if (hex.length < 2) {
                sb.append(0)
            }
            sb.append(hex)
        }
        return sb.toString()
    }
}