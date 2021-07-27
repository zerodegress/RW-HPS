package com.github.dr.rwserver.util

import com.github.dr.rwserver.struct.OrderedMap
import com.github.dr.rwserver.util.file.FileUtil
import com.github.dr.rwserver.util.io.IoReadConversion
import kotlin.Throws
import java.io.IOException
import com.github.dr.rwserver.util.log.Log
import java.io.InputStream
import java.io.InputStreamReader
import java.lang.Exception
import java.text.MessageFormat
import java.util.*

/**
 * @author Dr
 * @Date ?
 */
class LocaleUtil {
    private val languageData = OrderedMap<String, String>()

    constructor(fileUtil: FileUtil) {
        try {
            load(fileUtil.readInputsStream())
        } catch (e: Exception) {
            Log.fatal("[Load Language Error]", e)
        }
    }

    constructor(inputStream: InputStream) {
        try {
            load(IoReadConversion.fileToReadStream(inputStream))
        } catch (e: Exception) {
            Log.fatal("[Load Language Error]", e)
        }
    }

    @Throws(IOException::class)
    fun addLanguageData(fileUtil: FileUtil) {
        load(fileUtil.readInputsStream())
    }

    private fun load(inputStreamReader: InputStreamReader) {
        try {
            val p = Properties()
            p.load(inputStreamReader)
            p.forEach { key: Any, value: Any -> languageData.put(key as String, value as String) }
        } catch (e: Exception) {
        }
    }

    private fun language(input: String, params: Array<Any?>?): String {
        val text = languageData[input]
        return if (IsUtil.notIsBlank(text)) {
            if (IsUtil.isBlank(params)) text else MessageFormat(text).format(params)
        } else {
            Log.warn("Translation missing, please check", input)
            "$input : Key is invalid."
        }
    }

    /**
     * 传多参
     * @param      input   语言目标
     * @param      params  替换参
     * @return     文本
     */
    fun getinput(input: String, vararg params: Any?): String {
        val ps = arrayOfNulls<Any>(params.size)
        System.arraycopy(params, 0, ps, 0, params.size)
        return core(input, ps)
    }

    /**
     * 传一数组
     * @param      input  语言目标
     * @param      ps     Object替换组
     * @return     文本
     */
    fun getinputt(input: String, ps: Array<Any?>?): String {
        return core(input, ps)
    }

    private fun core(input: String, params: Array<Any?>?): String {
        return params?.let { language(input, it) } ?: language(input, null)
    }
}