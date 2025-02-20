/*
 * Copyright 2020-2024 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.data.bean

import net.rwhps.server.data.global.Data
import net.rwhps.server.struct.list.Seq
import net.rwhps.server.util.ReflectionUtils
import net.rwhps.server.util.SystemUtils
import net.rwhps.server.util.file.FileUtils
import net.rwhps.server.util.inline.toGson
import net.rwhps.server.util.inline.toPrettyPrintingJson
import net.rwhps.server.util.log.Log.debug
import net.rwhps.server.util.log.Log.error
import net.rwhps.server.util.math.RandomUtils
import java.lang.reflect.Field


/**
 * The server is primarily controlled
 *
 * Save data for serialization and deserialization
 * @author Dr (dr@der.kim)
 */
data class BeanCoreConfig(
    val noteEnglish: String = """
        Different protocols use different configuration files, please go to the corresponding file ConfigServer/ConfigRelay
    """.trimIndent(), val noteChina: String = """
        不同协议使用的配置文件不同, 请自行前往对应文件 ConfigServer/ConfigRelay
    """.trimIndent(),

    /** Default startup command */
    val defStartCommand: String = "start",

    val log: String = "WARN",

    val cmdTitle: String = "",

    /** 更新是否使用测试版本 */
    val followBetaVersion: Boolean = false,

    /** Port */
    val port: Int = 5123,

    /** 服务器名称 */
    val serverName: String = "RW-HPS",
    /** 标题, 留空使用地图名 */
    val subtitle: String = "",
    /** Automatically after starting UPLIST */
    val autoUpList: Boolean = false,

    /** ip多语言支持 */
    val ipCheckMultiLanguageSupport: Boolean = true,

    /** Single user relay disable pop-up selection */
    val singleUserRelay: Boolean = false,
    /** Default mods configuration for single user relay */
    val singleUserRelayMod: Boolean = false,

    /** Test : HTTP 鉴权 */
    val webToken: String = RandomUtils.getRandomIetterString(10),
    /** Web HOST 限制 */
    val webHOST: String = "",

    /** Web的 Port, 不为 0 时启用对应服务 */
    val webPort: Int = 0,
    val ssl: Boolean = false,
    val sslPasswd: String = "RW-HPS",

    var runPid: Long = 0
) {
    fun save() {
        runPid = SystemUtils.pid
        fileUtils.writeFile(this.toPrettyPrintingJson())
    }

    fun coverField(name: String, value: Any): Boolean {
        try {
            val field: Field = ReflectionUtils.findField(this::class.java, name) ?: return false
            field.isAccessible = true
            field[this] = value
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
            if (field.name != "Companion" && field.name != "fileUtil") allName.add(field.name)
        }
        return allName
    }

    companion object {
        val fileUtils = FileUtils.getFolder(Data.ServerDataPath).toFile("Config.json")

        @JvmStatic
        fun stringToClass(): BeanCoreConfig {

            val config: BeanCoreConfig = BeanCoreConfig::class.java.toGson(fileUtils.readFileStringData())

            // PATH
            config.allName().eachAll {
                val data = System.getProperties().getProperty("rwhps.config.$it")
                if (data != null) {
                    if (config.coverField(it, data)) {
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