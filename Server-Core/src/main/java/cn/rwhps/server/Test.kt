/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server

import cn.rwhps.server.data.global.Data
import cn.rwhps.server.dependent.LibraryManager
import cn.rwhps.server.util.file.FileUtil
import cn.rwhps.server.util.zip.zip.ZipDecoder

object Test {
    fun startGameCore() {
        // 释放jar
        val gameCoreLibs = FileUtil.getFolder(Data.Plugin_GameCore_Lib_Path,true)
        ZipDecoder(Test::class.java.getResourceAsStream("/libs.zip")!!).getSpecifiedSuffixInThePackageAllFileName("jar").each { k,v ->
            gameCoreLibs.toFile(k).run {
                if (!exists()) {
                    writeFileByte(v,true)
                }
            }
        }

        // 加载jar
        val libMg = LibraryManager()
        gameCoreLibs.fileList.eachAll {
            libMg.customImportLib(it)
        }
        libMg.loadToClassLoader()


        com.corrodinggames.rts.java.Main.main(arrayOf("-disable_vbos","-disable_atlas","-nomusic","-nosound","-nodisplay"))
    }
}