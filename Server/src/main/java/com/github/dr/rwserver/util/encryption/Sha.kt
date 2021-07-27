package com.github.dr.rwserver.util.encryption

import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.lang.StringBuffer

class Sha {
    fun sha256(strText: String): String {
        return toSha(strText, "SHA-256")
    }

    fun sha512(strText: String): String {
        return toSha(strText, "SHA-512")
    }

    fun sha256Array(strText: String): ByteArray {
        return toShaArray(strText, "SHA-256")
    }

    fun sha512Array(strText: String): ByteArray {
        return toShaArray(strText, "SHA-512")
    }

    private fun toShaArray(strText: String, strType: String): ByteArray {
        // 是否是有效字符串
        try {
            val messageDigest = MessageDigest.getInstance(strType)
            messageDigest.update(strText.toByteArray())
            return messageDigest.digest()
        } catch (e: NoSuchAlgorithmException) {
            e.printStackTrace()
        }
        return ByteArray(0)
    }

    private fun toSha(strText: String, strType: String): String {
        // 返回值
        var strResult = ""

        val byteBuffer = toShaArray(strText, strType)
        val strHexString = StringBuffer()
        for (b in byteBuffer) {
            val hex = Integer.toHexString(0xff and b.toInt())
            if (hex.length == 1) {
                strHexString.append('0')
            }
            strHexString.append(hex)
            strResult = strHexString.toString()
        }
        return strResult
    }
}