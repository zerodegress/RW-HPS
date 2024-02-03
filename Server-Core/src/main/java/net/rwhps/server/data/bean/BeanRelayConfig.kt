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
import net.rwhps.server.util.annotations.mark.PrivateMark
import net.rwhps.server.util.file.FileUtils
import net.rwhps.server.util.inline.toGson
import net.rwhps.server.util.inline.toPrettyPrintingJson
import net.rwhps.server.util.log.Log.debug
import net.rwhps.server.util.log.Log.error
import java.lang.reflect.Field


/**
 * Relay-Protocol configuration file
 *
 * Save data for serialization and deserialization
 * @author Dr (dr@der.kim)
 */
@PrivateMark
data class BeanRelayConfig(
    val mainID: String = "R",
    val mainServer: Boolean = true,
    val upList: Boolean = true,
    val mainServerIP: String = "relay.der.kim",
    val mainServerPort: Int = 4993,
) {

    fun save() {
        fileUtils.writeFile(this.toPrettyPrintingJson())
    }

    private fun coverField(name: String, value: Any): Boolean {
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
        val fileUtils = FileUtils.getFolder(Data.ServerDataPath).toFile("RelayConfig.json")

        @JvmStatic
        fun stringToClass(): BeanRelayConfig {

            val config: BeanRelayConfig = BeanRelayConfig::class.java.toGson(fileUtils.readFileStringData())

            // PATH
            config.allName().eachAll {
                val data = System.getProperties().getProperty("rwhps.relay.config.$it")
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