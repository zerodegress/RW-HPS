/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.util.game

import net.rwhps.server.data.global.Data
import net.rwhps.server.net.HttpRequestOkHttp
import net.rwhps.server.plugin.internal.hess.service.data.HessClassPathProperties
import net.rwhps.server.util.classload.GameModularLoadClass
import net.rwhps.server.util.classload.GameModularReusableLoadClass
import net.rwhps.server.util.compression.CompressionDecoderUtils
import net.rwhps.server.util.file.FileUtil
import net.rwhps.server.util.log.Log
import java.lang.reflect.Method

/**
 * 游戏 资源文件 初始化
 *
 * @author RW-HPS/Dr
 */
object GameStartInit {
    private enum class ResMD5(val md5: String) {
        Res("6d61d95d9fd7ef679d0013efad1466de"),
        Assets("b594e7e8d2a0ad925c8ac0e00edbdbad"),
        GameModularReusableClass("81b24654be7578fdb56fecf5254a78cb"),
    }

    fun init(load: GameModularReusableLoadClass): Boolean {
        try {
            val resFile = FileUtil.getFolder(Data.Plugin_GameCore_Data_Path).toFile("Game-Res.zip")
            val assetsFile = FileUtil.getFolder(Data.Plugin_GameCore_Data_Path).toFile("Game-Assets.zip")
            val gameModularReusableClassFile = FileUtil.getFolder(Data.Plugin_GameCore_Data_Path).toFile("GameModularReusableClass.bin")
            val temp = FileUtil.getFolder(Data.Plugin_GameCore_Data_Path,true)

            /* 鉴别两个文件的MD5, 不相同则重下 */
            if (resFile.md5 != ResMD5.Res.md5) {
                resFile.file.delete()
            }
            if (assetsFile.md5 != ResMD5.Assets.md5) {
                assetsFile.file.delete()
            }
            if (gameModularReusableClassFile.md5 != ResMD5.GameModularReusableClass.md5) {
                gameModularReusableClassFile.file.delete()
            }

            val resTask: (FileUtil,String,Boolean)->Unit = { file, resName, unzip ->
                if (!file.exists()) {
                    if (unzip) {
                        temp.toFolder(resName).file.delete()
                    }

                    HttpRequestOkHttp.downUrl(Data.urlData.readString("Get.Res")+file.name, file.file, true).also {
                        Log.clog("$resName : {0}",it)
                    }
                }
                if (unzip && !temp.toFolder(resName).toFile("Check").exists()) {
                    CompressionDecoderUtils.zip(file.file).use {
                        it.getZipAllBytes().each { filePath, bytes ->
                            temp.toFile(filePath).writeFileByte(bytes)
                        }
                    }
                    temp.toFolder(resName).toFile("Check").mkdir()
                }
            }

            resTask(resFile, "res", true)
            resTask(assetsFile, "assets", true)

            if (GameStartInit::class.java.getResourceAsStream("/libs.zip") == null && gameModularReusableClassFile.notExists()) {
                resTask(gameModularReusableClassFile, "gameModularReusableClassFile", false)
                load.readData(gameModularReusableClassFile)
            } else if (gameModularReusableClassFile.exists()) {
                load.readData(gameModularReusableClassFile)
            } else {
                // 加载游戏依赖
                CompressionDecoderUtils.zipStream(GameStartInit::class.java.getResourceAsStream("/libs.zip")!!).use {
                    it.getSpecifiedSuffixInThePackage("jar", true).each { _, v ->
                        load.addSourceJar(v)
                    }
                }
                load.saveData(FileUtil.getFolder(Data.Plugin_Data_Path).toFile("GameModularReusableClass.bin"))
            }
        } catch (e: Exception) {
            Log.fatal(e)
            return false
        }
        return true
    }

    fun start(load: GameModularLoadClass) {
        // Here, several intermediate signal transmission modules are directly injected into this loader
        // Because this loader only has Game-lib.jar
        // 注入 接口
        CompressionDecoderUtils.zipStream(FileUtil.getMyCoreJarStream()).use {
            it.getZipAllBytes().each { k, v ->
                if (
                    // 注入接口
                    k.startsWith(HessClassPathProperties.path.replace(".", "/")) ||
                    // 覆写游戏
                    k.startsWith(HessClassPathProperties.GameHessPath.replace(".","/"))
                ) {
                    val name = k.replace(".class", "")
                    load.addClassBytes(name.replace("/", "."), v)
                }
            }
        }

        val testAClass: Class<*> = load.findClass("com.corrodinggames.rts.java.Main")!!
        val mainMethod: Method = testAClass.getDeclaredMethod("main", Array<String>::class.java)
        mainMethod.invoke(null, arrayOf("-disable_vbos","-disable_atlas","-nomusic","-nosound","-nodisplay"))
    }
}