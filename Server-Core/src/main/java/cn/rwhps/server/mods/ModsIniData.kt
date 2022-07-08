/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.mods

import cn.rwhps.server.struct.Seq
import cn.rwhps.server.util.io.IoReadConversion.streamBufferRead
import cn.rwhps.server.util.log.Log
import java.io.*
import java.math.BigInteger
import java.nio.charset.StandardCharsets
import java.security.MessageDigest
import java.security.NoSuchAlgorithmException
import java.util.function.Function
import java.util.regex.Pattern

/**
 * Mods INI 解析器
 * @author RW-HPS/Dr
 */
class ModsIniData {
    /** 提取在[ ] 中间  */
    private val g = Pattern.compile("\\s*\\[([^]]*)]\\s*")

    /** 分割 =  */
    private val h = Pattern.compile("\\s*([^=:]*)[=:](.*)")
    private val a = Pattern.compile("\\$\\{([^}]*)}")
    private val b = Pattern.compile("[A-Za-z_][A-Za-z_.\\d]*")

    val modFileData = LinkedHashMap<String, LinkedHashMap<String, String>>()

    @Throws(IOException::class)
    constructor(inputStream: InputStream) {
        readModFile(streamBufferRead(inputStream))
    }

    @Throws(IOException::class)
    constructor(bytes: ByteArray) {
        readModFile(BufferedReader(InputStreamReader(ByteArrayInputStream(bytes))))
    }

    fun getMd5(): Int {
        return try {
            val messageDigest = MessageDigest.getInstance("MD5")
            for (str in modFileData.keys) {
                val map = modFileData[str]!!
                for (str1 in map.keys) {
                    val str2 = str + ":" + str1 + ":" + map[str1]
                    messageDigest.update(str2.toByteArray(StandardCharsets.UTF_8))
                }
            }
            BigInteger(1, messageDigest.digest()).toInt()
        } catch (noSuchAlgorithmException: NoSuchAlgorithmException) {
            throw RuntimeException(noSuchAlgorithmException)
        }
    }

    @Throws(IOException::class)
    private fun readModFile(paramBufferedReaderIn: BufferedReader) {
        paramBufferedReaderIn.use { paramBufferedReader ->
            var str2: String? = null
            var lineString: String?
            while (paramBufferedReader.readLine().also { lineString = it } != null) {
                if (lineString!!.startsWith(" ")) {
                    lineString = lineString!!.substring(1)
                }
                if (lineString!!.trim().startsWith("#") || lineString!!.trim().isBlank()) {
                    continue
                }
                if (lineString!!.contains("[")) {
                    val matcher1 = g.matcher(lineString!!)
                    if (matcher1.matches()) {
                        str2 = matcher1.group(1).trim { it <= ' ' }
                        continue
                    }
                }
                if (str2 != null && str2.startsWith("comment_")) {
                    continue
                }
                val matcher = h.matcher(lineString!!)
                if (matcher.matches()) {
                    if (str2 == null) {
                        Log.error("INI","This line is not in a [section]: \" $lineString \"")
                        continue
                    }
                    val str3 = matcher.group(1).trim { it <= ' ' }
                    val str4 = matcher.group(2).trim { it <= ' ' }
                    val linkedHashMap = modFileData.computeIfAbsent(str2, Function { LinkedHashMap() })
                    linkedHashMap[str3] = str4
                }
            }
        }
    }

    fun getName(): String {
        return this.modFileData["core"]!!["name"]!!
    }

    /**
     * 给指定节加入一个值
     * @param name String
     * @param k String
     * @param def String?
     * @return String?
     */
    fun getValue(name: String, k: String , def: String? = null): String? {
        val data = this.modFileData[name]
        if (data != null) {
            if (data.contains(k)) {
                return data[k]
            }
            return def
        }
        return def
    }

    fun addValue(name: String, key: String , value: String) {
        val linkedHashMap = modFileData.computeIfAbsent(name) { LinkedHashMap() }

        if (linkedHashMap[key] == null) {
            linkedHashMap[key] = value
        }
    }

    /**
     * 强制给指定节加入值
     * @param name String
     * @param key String
     * @param value String
     */
    fun addValue0(name: String, key: String , value: String) {
        val linkedHashMap = modFileData.computeIfAbsent(name) { LinkedHashMap() }

        linkedHashMap[key] = value
    }

    fun addModsConfig(modsData: ModsIniData) {
        modsData.modFileData.forEach { (k: String, v: LinkedHashMap<String, String>) ->
            if (getValue(k,"@copyFrom_skipThisSection","false").toBoolean()) {
                return@forEach
            }

            if (modFileData[k] == null) {
                modFileData[k] = LinkedHashMap()
            }

            v.forEach { (k1: String, v1: String) ->
                if (modFileData[k]!![k1] == null) {
                    modFileData[k]!![k1] = v1
                }
            }
        }
    }

    // 找到每个模块具有特定名字的模块 然后写入List
    fun checkEachModuleValue(moduleName: String): Seq<String> {
        val result = Seq<String>()
        modFileData.forEach {(k: String, v: LinkedHashMap<String, String>) ->
            if (v.contains(moduleName)) {
                result.add(k)
            }
        }
        return result
    }

    // 找到每个模块具有特定名字的模块 然后写入List
    fun checkForKeysStartingWithASpecificName(startWith: String): Seq<String> {
        val result = Seq<String>()
        modFileData.forEach {(moduleName: String, v: LinkedHashMap<String, String>) ->
            v.forEach { (key: String, value: String?) ->
                if (key.startsWith(startWith)) {
                    if ("IGNORE" != value) {
                        result.add(moduleName)
                    }
                }
            }
        }
        return result
    }

    // 找到特点模块具有特定名字的模块 然后写入List
    fun checkForASpecificModuleAndStartWithASpecificCharacter(moduleName: String, startWith: String): Seq<String> {
        val result = Seq<String>()
        modFileData[moduleName]?.forEach { (key: String, value: String) ->
            if (key.startsWith(startWith)) {
                if ("IGNORE" != value) {
                    result.add(key)
                }
            }
        }
        return result
    }
}