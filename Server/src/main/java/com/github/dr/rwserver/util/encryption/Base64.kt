package com.github.dr.rwserver.util.encryption

import com.github.dr.rwserver.data.global.Data
import java.util.Base64

object Base64 {
    /**
     *
     * @param str Base64字符串
     * @return 解密后
     */
    @JvmStatic
    fun decode(str: String): ByteArray {
        return Base64.getDecoder().decode(str)
    }

    @JvmStatic
    fun decodeString(str: String): String {
        return String(Base64.getDecoder().decode(str), Data.UTF_8)
    }

    /**
     *
     * @return 加密后
     */
    @JvmStatic
    fun encode(bytes: ByteArray): String {
        return Base64.getEncoder().encodeToString(bytes)
    }

    @JvmStatic
    fun encode(str: String): String {
        return Base64.getEncoder().encodeToString(str.toByteArray(Data.UTF_8))
    }

    @JvmStatic
    fun isBase64(`val`: String): Boolean {
        try {
            val key = Base64.getDecoder().decode(`val`)
            val str = String(key)
            val result = Base64.getEncoder().encodeToString(str.toByteArray())
            if (result.equals(`val`, ignoreCase = true)) {
                return true
            }
        } catch (e: Exception) {
        }
        return false
    }
}