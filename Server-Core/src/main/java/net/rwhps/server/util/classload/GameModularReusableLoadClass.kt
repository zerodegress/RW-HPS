/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.util.classload

import net.rwhps.server.data.plugin.PluginData
import net.rwhps.server.util.file.FileUtil

/**
 * Reusable class loader
 *
 * @date  2023/5/26 15:14
 * @author  RW-HPS/Dr
 */
class GameModularReusableLoadClass(mainClassLoader: ClassLoader, jdkClassLoader: ClassLoader) : GameModularLoadClass(mainClassLoader, jdkClassLoader) {
    /** 是否是缓存加载 */
    private var asmCacheFlag = false

    //You don't have to go through ASM because it's caching
    override fun asmClass(className: String, classfileBuffer: ByteArray): ByteArray {
        return if (asmCacheFlag) {
            classfileBuffer
        } else {
            super.asmClass(className, classfileBuffer)
        }
    }

    /**
     * Read the cached file
     *
     * @param classPathMap OrderedMap<String, ByteArray>
     */
    fun readData(file: FileUtil) {
        val data = PluginData().apply {
            setFileUtil(file, "7z")
        }

        this.classPathMapData.clear()
        data.getMap().forEach {
            this.classPathMapData.put(it.key, it.value.data as ByteArray)
        }
        asmCacheFlag = true
    }

    /**
     * Save the cache file (go through ASM again)
     *
     * @param file FileUtil
     */
    fun saveData(file: FileUtil) {
        val data = PluginData().apply {
            setFileUtil(file,"7z")
        }
        classPathMapData.forEach {
            data.setData(it.key, super.asmClass(it.key,it.value))
        }

        data.save()
    }
}