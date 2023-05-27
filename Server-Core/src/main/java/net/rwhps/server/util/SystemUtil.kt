/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.util

import net.rwhps.server.util.log.Log
import java.lang.management.ManagementFactory
import java.nio.charset.Charset
import java.util.*

/**
 * @author RW-HPS/Dr
 */
object SystemUtil {
    val defaultEncoding: Charset
        get() = Charset.forName(get("sun.stdout.encoding") ?:Charset.defaultCharset().name())

    val javaHeap: Long
        get() = Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()
    val javaTotalMemory: Long
        get() = Runtime.getRuntime().totalMemory()
    val javaFreeMemory: Long
        get() = Runtime.getRuntime().freeMemory()
    val availableProcessors: Int
        get() = Runtime.getRuntime().availableProcessors()
    val javaVendor: String
        get() = get("java.vendor")!!
    val javaVersion: String
        get() = get("java.version")!!
    val osName: String
        get() = get("os.name")!!
    val osArch: String
        get() = get("os.arch")!!

    fun isJavaVersionAtLeast(requiredVersion: Float): Boolean {
        val version = get("java.version")!!.split(".")[0]
        return (if (IsUtil.isBlank(version)) 0f else version.toFloat()) >= requiredVersion
    }

    val isWindows: Boolean
        get() {
            val os = get("os.name")!!
            return IsUtil.isBlank(os) || os.lowercase(Locale.getDefault()).contains("windows")
        }
    val pid: Long
        // 唯一的到Java11理由
        get() = ProcessHandle.current().pid()


    /**
     * 用于替代 Java11 中的 GetPid
     * @return Long
     */
    @Deprecated("NotUsed", ReplaceWith("ProcessHandle.current().pid()"))
    private fun jvmPid(): Long {
        return try {
            var pid = ManagementFactory.getRuntimeMXBean().name
            val indexOf = pid.indexOf('@')
            if (indexOf > 0) {
                pid = pid.substring(0, indexOf)
            }
            pid.toLong()

        } catch (e: Exception) {
            -1
        }
    }

    /**
     * 取得系统属性，如果因为Java安全的限制而失败，则将错误打在Log中，然后返回
     * @param name  属性名
     * @return 属性值或null
     * @see System String
     * @see System String
     */
    private operator fun get(name: String): String? {
        var value: String? = null
        try {
            value = System.getProperty(name)
        } catch (e: SecurityException) {
            Log.error("Security level limit", e)
        }
        if (null == value) {
            try {
                value = System.getenv(name)
            } catch (e: SecurityException) {
                Log.error("Security level limit", e)
            }
        }
        return value
    }
}