/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.util

import net.rwhps.server.data.global.Data
import net.rwhps.server.util.log.Log
import java.nio.charset.Charset


/**
 * @author RW-HPS/Dr
 */
object ExtractUtil {
    private val defCharset: Charset = Charset.defaultCharset()

    /**
     * 编码字符串，编码为UTF-8
     *
     * @param str 字符串
     * @return 编码后的字节码
     */
    @JvmStatic
    fun utf8Bytes(str: CharSequence): ByteArray {
        return bytes(str, Data.UTF_8)
    }

    /**
     * 编码字符串
     *
     * @param str     字符串
     * @param charset 字符集，如果此字段为空，则解码的结果取决于平台
     * @return 编码后的字节码
     */
    @JvmStatic
    fun bytes(str: CharSequence, charset: String?): ByteArray {
        return bytes(str, if (IsUtil.isBlank(charset)) {
            Charset.defaultCharset()
        } else {
            Charset.forName(charset)
        })
    }

    /**
     * 编码字符串
     *
     * @param str     字符串
     * @param charset 字符集，如果此字段为空，则解码的结果取决于平台
     * @return 编码后的字节码
     */
    @JvmStatic
    @JvmOverloads
    fun bytes(str: CharSequence, charset: Charset? = Charset.defaultCharset()): ByteArray {
        return if (null == charset) {
            str.toString().toByteArray()
        } else {
            str.toString().toByteArray(charset)
        }
    }

    /**
     * 解码字节码
     *
     * @param data    字符串
     * @param charset 字符集，如果此字段为空，则解码的结果取决于平台
     * @return 解码后的字符串
     */
    @JvmStatic
    fun str(data: ByteArray, charset: Charset?): String {
        return if (null == charset) { String(data) } else String(data, charset)
    }

    fun tryRunTest(run: ()->Unit) {
        try {
            run()
        } catch (e: Exception) {
            Log.error(e)
        }
    }
}