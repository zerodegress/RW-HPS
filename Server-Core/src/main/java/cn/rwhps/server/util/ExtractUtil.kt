/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.util

import cn.rwhps.server.data.global.Data
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets


/**
 * @author RW-HPS/Dr
 */
object ExtractUtil {
    private val defCharset: Charset = Charset.defaultCharset()
    @JvmStatic
    fun stringDefToUtf8(string: String): String {
        // 用指定编码转换String为byte[]:
        return String(string.toByteArray(defCharset), Data.UTF_8)
    }

    @JvmStatic
    fun stringGbkToUtf8(string: String): String {
        // 用指定编码转换String为byte[]:
        return String(string.toByteArray(StandardCharsets.ISO_8859_1), Data.UTF_8)
    }

    private fun byteToUnsignedInt(data: Byte): Int {
        return data.toInt() and 0xff
    }

    fun isUTF8(pBuffer: ByteArray): Boolean {
        var IsUTF8 = true
        var IsASCII = true
        val size = pBuffer.size
        var i = 0
        while (i < size) {
            val value = byteToUnsignedInt(pBuffer[i])
            if (value < 0x80) {
                // (10000000): 值小于 0x80 的为 ASCII 字符
                if (i >= size - 1) {
                    if (IsASCII) {
                        // 假设纯 ASCII 字符不是 UTF 格式
                        IsUTF8 = false
                    }
                    break
                }
                i++
            } else if (value < 0xC0) {
                // (11000000): 值介于 0x80 与 0xC0 之间的为无效 UTF-8 字符
                IsUTF8 = false
                break
            } else if (value < 0xE0) {
                // (11100000): 此范围内为 2 字节 UTF-8 字符
                IsASCII = false
                if (i >= size - 1) {
                    break
                }
                val value1 = byteToUnsignedInt(pBuffer[i + 1])
                if (value1 and 0xC0 != 0x80) {
                    IsUTF8 = false
                    break
                }
                i += 2
            } else if (value < 0xF0) {
                IsASCII = false
                // (11110000): 此范围内为 3 字节 UTF-8 字符
                if (i >= size - 2) {
                    break
                }
                val value1 = byteToUnsignedInt(pBuffer[i + 1])
                val value2 = byteToUnsignedInt(pBuffer[i + 2])
                if (value1 and 0xC0 != 0x80 || value2 and 0xC0 != 0x80) {
                    IsUTF8 = false
                    break
                }
                i += 3
            } else if (value < 0xF8) {
                IsASCII = false
                // (11111000): 此范围内为 4 字节 UTF-8 字符
                if (i >= size - 3) {
                    break
                }
                val value1 = byteToUnsignedInt(pBuffer[i + 1])
                val value2 = byteToUnsignedInt(pBuffer[i + 2])
                val value3 = byteToUnsignedInt(pBuffer[i + 3])
                if (value1 and 0xC0 != 0x80 || value2 and 0xC0 != 0x80 || value3 and 0xC0 != 0x80) {
                    IsUTF8 = false
                    break
                }
                i += 3
            } else {
                IsUTF8 = false
                break
            }
        }
        return IsUTF8
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
        var inHexRemoveAir = inHex.replace(" ", "")
        var hexLength = inHexRemoveAir.length
        val result: ByteArray
        if (hexLength % 2 != 1) {
            //偶数
            result = ByteArray(hexLength / 2)
        } else {
            //奇数
            hexLength++
            result = ByteArray(hexLength / 2)
            inHexRemoveAir = "0$inHexRemoveAir"
        }
        var j = 0
        var i = 0
        while (i < hexLength) {
            result[j] = inHexRemoveAir.substring(i, i + 2).toInt(16).toByte()
            j++
            i += 2
        }
        return result
    }

    @JvmStatic
    fun bytesToHex(byte: Byte): String {
        return bytesToHex(byteArrayOf(byte))
    }

    @JvmStatic
	fun bytesToHex(bytes: ByteArray): String {
        val sb = StringBuffer()
        for (aByte in bytes) {
            val hex = Integer.toHexString(aByte.toInt() and 0xFF)
            if (hex.length < 2) {
                sb.append(0)
            }
            sb.append(hex).append(" ")
        }
        return sb.toString()
    }
}