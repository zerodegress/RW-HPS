package com.github.dr.rwserver.util

import java.util.*

object RandomUtil {
    private const val ALLCHAR = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val LETTERCHAR = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
    private const val NUMBERCHAR = "123456789"

    /**
     * 产生len长度的随机字符串
     * @param len
     * @return
     */
    @JvmStatic
    fun generateStr(len: Int): String {
        val sb = StringBuffer()
        val random = Random()
        for (i in 0 until len) {
            sb.append(ALLCHAR[random.nextInt(ALLCHAR.length)])
        }
        return sb.toString()
    }

    /**
     * 产生len长度的随机数字
     * @param len
     * @return
     */
    @JvmStatic
    fun generateInt(len: Int): Int {
        val sb = StringBuffer()
        val random = Random()
        for (i in 0 until len) {
            sb.append(NUMBERCHAR[random.nextInt(NUMBERCHAR.length)])
        }
        return sb.toString().toInt()
    }

    /**
     * 产生Str+len长度的随机数字
     * @param len
     * @return
     */
    @JvmStatic
    fun generateStrInt(str: String?, len: Int): String {
        val sb = StringBuffer(str)
        val random = Random()
        for (i in 0 until len) {
            sb.append(NUMBERCHAR[random.nextInt(NUMBERCHAR.length)])
        }
        return sb.toString()
    }

    /**
     * 返回一个定长的随机纯字母字符串(只包含大小写字母)
     * @param length 随机字符串长度
     * @return 随机字符串
     */
    @JvmStatic
    fun generateMixStr(length: Int): String {
        val sb = StringBuffer()
        val random = Random()
        for (i in 0 until length) {
            sb.append(LETTERCHAR[random.nextInt(LETTERCHAR.length)])
        }
        return sb.toString()
    }

    /**
     * 返回一个定长的随机纯大写字母字符串(只包含大小写字母)
     * @param length 随机字符串长度
     * @return 随机字符串
     */
    @JvmStatic
    fun generateLowerStr(length: Int): String {
        return generateMixStr(length).lowercase(Locale.getDefault())
    }

    /**
     * 返回一个定长的随机纯小写字母字符串(只包含大小写字母)
     * @param length 随机字符串长度
     * @return 随机字符串
     */
    @JvmStatic
    fun generateUpperStr(length: Int): String {
        return generateMixStr(length).uppercase(Locale.getDefault())
    }
}