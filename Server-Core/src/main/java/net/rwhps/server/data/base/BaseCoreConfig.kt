/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.data.base

import net.rwhps.server.data.global.Data
import net.rwhps.server.struct.Seq
import net.rwhps.server.util.ReflectionUtils
import net.rwhps.server.util.SystemUtil
import net.rwhps.server.util.file.FileUtil
import net.rwhps.server.util.inline.toGson
import net.rwhps.server.util.inline.toPrettyPrintingJson
import net.rwhps.server.util.log.Log.debug
import net.rwhps.server.util.log.Log.error
import java.lang.reflect.Field


/**
 * The server is primarily controlled
 *
 * Save data for serialization and deserialization
 * @author RW-HPS/Dr
 */
data class BaseCoreConfig(
    /** Default startup command */
    val DefStartCommand: String = "start",

    val Log: String = "WARN",

    /** Port */
    val Port: Int = 5123,

    val ServerName: String = "RW-HPS",
    val Subtitle: String = "",
    /** Automatically after starting UPLIST */
    val AutoUpList: Boolean = false,

    /** ip多语言支持 */
    val IpCheckMultiLanguageSupport: Boolean = false,

    /** Single user relay disable pop-up selection */
    val SingleUserRelay: Boolean = false,
    /** Default mods configuration for single user relay */
    val SingleUserRelayMod: Boolean = false,

    /** GamePort HttpPort 共用 */
    val WebGameBypassPort: Boolean = false,

    /** 单独起 HTTP 服务 */
    val WebService: Boolean = false,
    /** 单独的 Port */
    val SeparateWebPort: Int = 5124,
    val SSL: Boolean = false,
    val SSLPasswd: String = "RW-HPS",

    val AutoReLoadMap: Boolean = false,

    val Turnstoneintogold: Boolean = false,

    var RunPid: Long = 0
) {
    fun save() {
        RunPid = SystemUtil.pid
        fileUtil.writeFile(this.toPrettyPrintingJson())
    }

    fun coverField(name: String,value: Any): Boolean {
        try {
            val field: Field = ReflectionUtils.findField(this::class.java, name) ?:return false
            field.isAccessible = true
            field.set(this,value)
            field.isAccessible = false
        } catch (e: Exception) {
            error("Cover Gameover error", e)
        }
        return true
    }

    private fun allName(): Seq<String> {
        val allName = Seq<String>()
        val fields = this.javaClass.declaredFields
        for (field in fields) {
            // 过滤Kt生成的和不能被覆盖的
            if (field.name != "Companion" && field.name != "fileUtil")
            allName.add(field.name)
        }
        return allName
    }

    companion object {
        val fileUtil = FileUtil.getFolder(Data.Plugin_Data_Path).toFile("Config.json")

        @JvmStatic
        fun stringToClass(): BaseCoreConfig {

            val config: BaseCoreConfig = BaseCoreConfig::class.java.toGson(fileUtil.readFileStringData())

            // PATH
            config.allName().eachAll {
                val data = System.getProperties().getProperty("rwhps.config.$it")
                if (data != null) {
                    if (config.coverField(it,data)) {
                        debug("Set OK $it = $data")
                    } else {
                        debug("Set ERROR $it = $data")
                    }
                }
            }

            return config
        }
    }
}