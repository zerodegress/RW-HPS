/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.data.bean

import net.rwhps.server.data.global.Data
import net.rwhps.server.struct.Seq
import net.rwhps.server.util.ReflectionUtils
import net.rwhps.server.util.file.FileUtils
import net.rwhps.server.util.inline.toGson
import net.rwhps.server.util.inline.toPrettyPrintingJson
import net.rwhps.server.util.log.Log
import java.lang.reflect.Field

/**
 * Server-Protocol configuration file
 *
 * Save data for serialization and deserialization
 * @author RW-HPS/Dr
 */
data class BeanServerConfig(
    val enterAd: String = "",
    val startAd: String = "",
    val maxPlayerAd: String = "",
    val startPlayerAd: String = "",

    /** 密码 */
    val passwd: String = "",

    /** 服务器最大人数 */
    val maxPlayer: Int = 10,
    /** 服务器最大游戏时间 (s) 2*60*60 (-1 为禁用) */
    val maxGameIngTime: Int = 7200,
    val maxOnlyAIGameIngTime: Int = 3600,
    /** 服务器最小Start人数 (-1 为禁用) */
    val startMinPlayerSize: Int = -1,
    /** 服务器最小AutoStart人数 (-1 为禁用) */
    val autoStartMinPlayerSize: Int = 4,
    /** 最大发言长度 */
    val maxMessageLen: Int = 40,
    /** 最大单位数 */
    val maxUnit: Int = 200,
    /** 默认倍率 */
    val defIncome: Float = 1f,
    /** 点石成金 */
    val turnStoneIntoGold: Boolean = false,

    /** only Admin */
    val oneAdmin: Boolean = true,
    /** 是否保存 RePlay */
    val saveRePlayFile: Boolean = true,
) {
    private fun checkValue() {
        // 拒绝最大玩家数超过最小开始玩家数
        if (maxPlayer < startMinPlayerSize) {
            Log.warn("MaxPlayer < StartMinPlayerSize , Reset !")
            coverField("StartMinPlayerSize", 0)
        }
        if (maxPlayer > 100) {
            Log.warn("MaxPlayer > GameMaxPlayerSize , Reset !")
            //coverField("MaxPlayer",100)
        }
    }

    fun save() {
        fileUtils.writeFile(this.toPrettyPrintingJson())
    }

    fun coverField(name: String, value: Any): Boolean {
        try {
            val field: Field = ReflectionUtils.findField(this::class.java, name) ?: return false
            field.isAccessible = true
            field[this] = value
            field.isAccessible = false
        } catch (e: Exception) {
            Log.error("Cover $name error", e)
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
        val fileUtils = FileUtils.getFolder(Data.Plugin_Data_Path).toFile("ConfigServer.json")

        @JvmStatic
        fun stringToClass(): BeanServerConfig {
            val config: BeanServerConfig = BeanServerConfig::class.java.toGson(fileUtils.readFileStringData())

            // PATH
            config.allName().eachAll {
                val data = System.getProperties().getProperty("rwhps.server.config.$it")
                if (data != null) {
                    if (config.coverField(it, data)) {
                        Log.debug("Set OK $it = $data")
                    } else {
                        Log.debug("Set ERROR $it = $data")
                    }
                }
            }

            config.checkValue()

            return config
        }
    }
}
