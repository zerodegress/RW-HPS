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
import net.rwhps.server.util.file.FileUtil
import net.rwhps.server.util.inline.toGson
import net.rwhps.server.util.inline.toPrettyPrintingJson
import net.rwhps.server.util.log.Log
import java.lang.reflect.Field

/**
 * @author RW-HPS/Dr
 */
data class BaseServerExConfig(
    val EnterAd: String = "",
    val StartAd: String = "",
    val MaxPlayerAd: String = "",
    val StartPlayerAd: String = "",
) {
    fun save() {
        fileUtil.writeFile(this.toPrettyPrintingJson())
    }

    fun coverField(name: String,value: Any): Boolean {
        try {
            val field: Field = ReflectionUtils.findField(this::class.java, name) ?:return false
            field.isAccessible = true
            field.set(this,value)
            field.isAccessible = false
        } catch (e: Exception) {
            Log.error("Cover Gameover error", e)
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
        val fileUtil = FileUtil.getFolder(Data.Plugin_Data_Path).toFile("ConfigServerEx.json")

        @JvmStatic
        fun stringToClass(): BaseServerExConfig {
            val config: BaseServerExConfig = BaseServerExConfig::class.java.toGson(fileUtil.readFileStringData())

            // PATH
            config.allName().eachAll {
                val data = System.getProperties().getProperty("rwhps.configx.$it")
                if (data != null) {
                    if (config.coverField(it,data)) {
                        Log.debug("Set OK $it = $data")
                    } else {
                        Log.debug("Set ERROR $it = $data")
                    }
                }
            }

            return config
        }
    }
}
