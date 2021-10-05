/*
 * Copyright 2020-2021 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.util

import com.github.dr.rwserver.util.IsUtil.notIsBlank
import com.github.dr.rwserver.util.log.Log
import java.util.regex.Matcher
import java.util.regex.Pattern


/**
 * @author Dr
 */
object StringFilteringUtil {
    private val pattern = Pattern.compile("[1-9][0-9]{4,14}")

    private fun findFirstGroup(matcher: Matcher): String {
        matcher.find()
        return matcher.group(0)
    }

    @JvmStatic
    fun findMatchString(str: String, regEx: String): String {
        return try {
            val pattern = Pattern.compile(regEx)
            val matcher = pattern.matcher(str)
            findFirstGroup(matcher)
        } catch (e: java.lang.Exception) {
            Log.error("[Find Match] Error",e)
            ""
        }
    }


    @JvmStatic
    fun getkeys(textRuselt: String, keys: String, numbero: Int, numbert: Int): String {
        var tkk = ""
        // 去除返回数据空格
        val text = removeAllisBlank(textRuselt)
        if (notIsBlank(textRuselt)) {
            val matchString: String = findMatchString(text, keys)
            // 提取目标
            tkk = matchString.substring(numbero, matchString.length - numbert)
        }
        return tkk
    }

    @JvmStatic
    fun removeAllisBlank(s: String): String {
        var result = ""
        if ("" != s) {
            result = s.replace("[　*| *| *|//s*]*".toRegex(), "")
        }
        return result
    }

    @JvmStatic
    fun trim(s: String): String {
        var result = ""
        if ("" != s) {
            result = s.replace("^[　*| *| *|//s*]*".toRegex(), "").replace("[　*| *| *|//s*]*$".toRegex(), "")
        }
        return result
    }

    @JvmStatic
    fun removeAllEn(s: String): String {
        var result = ""
        if ("" != s) {
            result = s.replace("[^(A-Za-z)]".toRegex(), "")
        }
        return result
    }

    @JvmStatic
    fun removeAllCn(s: String): String {
        var result = ""
        if ("" != s) {
            result = s.replace("[^(\\u4e00-\\u9fa5)]".toRegex(), "")
        }
        return result
    }

    @JvmStatic
    fun readQQ(str: String): String {
        val matcher = pattern.matcher(str)
        return if (matcher.find()) {
            matcher.group(0)
        } else ""
    }

    @JvmStatic
	fun cutting(str: String, length: Int): String {
        return if (str.length < length) {
            str
        } else str.substring(0, length)
    }


    /***
     * 把中文替换为指定字符<br></br>
     * 注意:一次只匹配一个中文字符
     * @param source
     * @param replacement
     * @return
     */
    @JvmStatic
    fun replaceChinese(source: String, replacement: String): String {
        val reg = "[\u4e00-\u9fa5]"
        val pat = Pattern.compile(reg)
        val mat = pat.matcher(source)
        return mat.replaceAll(replacement)
    }

    class StringMatcherData(patternString: String, text: String) {
        private val matcher: Matcher = Pattern.compile(patternString).matcher(text)

        fun getString(position: Int): String {
            val result = matcher.group(position)
            return if (IsUtil.notIsBlank(result)) result else ""
        }

        fun getInt(position: Int): Int {
            val result = matcher.group(position)
            return if (IsUtil.notIsBlank(result)) result.toInt() else 0
        }

        init {
            matcher.find()
        }
    }
}