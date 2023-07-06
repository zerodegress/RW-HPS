/*
 *
 *  * Copyright 2020-2023 RW-HPS Team and contributors.
 *  *
 *  * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 *  * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *  *
 *  * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 *
 */

package net.rwhps.server.util.plugin

import net.rwhps.server.data.global.Data
import net.rwhps.server.struct.OrderedMap
import java.net.URLDecoder
import kotlin.io.path.Path
import kotlin.io.path.pathString

/**
 * @date 2023/7/6 14:54
 * @author RW-HPS/Dr
 */
object ScriptResUtils {
    val defPath = Path("/")
    private lateinit var scriptFileSystem: OrderedMap<String, ByteArray>

    fun setFileSystem(scriptFileSystem: OrderedMap<String, ByteArray>) {
        this.scriptFileSystem = scriptFileSystem
    }

    /**
     * 提取出插件名, 然后进入绝对路径查找文件
     *
     * @param path String
     * @param filePath String
     * @return ByteArray?
     */
    @JvmStatic
    fun getPluginFileBytes(path: String, filePath: String): ByteArray? {
        try {
            val pluginUrl = path.split(defPath.toAbsolutePath().pathString.replace("\\", "/"))[1].split("/")[0]
            val pluginName = URLDecoder.decode(pluginUrl, Data.UTF_8)
            val bytes = scriptFileSystem["/$pluginName/$filePath"] ?: return null
            return bytes
        } catch (_: Exception) {
            return null
        }
    }
}