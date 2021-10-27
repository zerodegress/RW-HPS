/*
 * Copyright 2020-2021 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.util

import com.github.dr.rwserver.data.global.Data
import java.net.InetAddress
import java.net.UnknownHostException
import java.nio.charset.StandardCharsets

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
            val hex = Integer.toHexString(aByte.toInt() and 0xFF)
            if (hex.length < 2) {
                sb.append(0)
            }
            sb.append(hex).append(" ")
        }
        return sb.toString()
    }

    /**
     * 获取本机内网ip
     * @return
     * @throws UnknownHostException
     */
    @Throws(UnknownHostException::class)
    fun getPrivateIp(): String? {
        return InetAddress.getLocalHost().hostAddress
    }
}