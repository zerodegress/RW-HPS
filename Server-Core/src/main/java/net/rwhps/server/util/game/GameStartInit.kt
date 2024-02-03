/*
 * Copyright 2020-2024 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.util.game

import net.rwhps.server.data.global.Data
import net.rwhps.server.net.HttpRequestOkHttp
import net.rwhps.server.plugin.internal.headless.service.data.HessClassPathProperties
import net.rwhps.server.util.classload.GameModularLoadClass
import net.rwhps.server.util.classload.GameModularReusableLoadClass
import net.rwhps.server.util.compression.CompressionDecoderUtils
import net.rwhps.server.util.file.FileUtils
import net.rwhps.server.util.log.Log
import java.lang.reflect.Method
import kotlin.concurrent.thread

/**
 * 游戏 资源文件 初始化
 *
 * @author Dr (dr@der.kim)
 */
object GameStartInit {
    private val gameCorePath = FileUtils.getFolder(Data.Plugin_GameCore_Data_Path, true)

    private enum class ResMD5(val md5: String, val fileUtils: FileUtils) {
        Res("408aa02d8566a771c5ad97caf9f1f701", gameCorePath.toFile("Game-Res.7z")),
        Fonts("e27f86783a04bb6c7bc7b4388f8c8539", gameCorePath.toFile("Game-Fonts.7z")),
        Assets("768984542af2f3bbe1269aca2c8749ff", gameCorePath.toFile("Game-Assets.7z")),
        GameModularReusableClass("1ff43b0cdc2d756bc956ac014f3b438e", gameCorePath.toFile("GameModularReusableClass.bin"))
    }

    fun init(load: GameModularReusableLoadClass): Boolean {
        try {
            val temp = FileUtils.getFolder(Data.Plugin_GameCore_Data_Path, true)

            /* 鉴别两个文件的MD5, 不相同则删除 */
            ResMD5.values().forEach {
                if (it.fileUtils.exists() && it.md5 != it.fileUtils.md5) {
                    Log.debug("File MD5 DoesNotMatch", it.name)
                    it.fileUtils.delete()
                }
            }

            val resTask: (FileUtils, String, Boolean) -> Unit = { file, resName, unzip ->
                if (!file.exists()) {
                    if (unzip) {
                        temp.toFolder(resName).file.delete()
                    }

                    HttpRequestOkHttp.downUrl(Data.urlData.readString("Get.Core.ResDown") + file.name, file.file, true).also {
                        Log.clog("$resName : {0}", it)
                    }
                    file.setReadOnly()
                }
                if (unzip && !temp.toFolder(resName).toFile("Check").exists()) {
                    CompressionDecoderUtils.sevenZip(file.file).use {
                        it.getZipAllBytes().eachAll { filePath, bytes ->
                            temp.toFile(filePath).writeFileByte(bytes)
                        }
                    }
                    temp.toFolder(resName).toFile("Check").also {
                        it.mkdir()
                        it.setReadOnly()
                    }
                }
            }

            resTask(ResMD5.Res.fileUtils, "res", true)
            resTask(ResMD5.Assets.fileUtils, "assets", true)
            resTask(ResMD5.Fonts.fileUtils, "fonts", false)

            if (GameStartInit::class.java.getResourceAsStream("/libs.zip") == null) {
                if (ResMD5.GameModularReusableClass.fileUtils.notExists()) {
                    resTask(ResMD5.GameModularReusableClass.fileUtils, "gameModularReusableClassFile", false)
                }
                load.readData(ResMD5.GameModularReusableClass.fileUtils)
            } else {
                // 加载游戏依赖
                CompressionDecoderUtils.zipAllReadStream(GameStartInit::class.java.getResourceAsStream("/libs.zip")!!).use {
                    it.getSpecifiedSuffixInThePackage("jar", true).eachAll { _, v ->
                        load.addSourceJar(v)
                    }
                }
                //TODO Save GameModularReusableClass
                //load.saveData(FileUtils.getFolder(Data.ServerDataPath).toFile("GameModularReusableClass.bin"))
            }
        } catch (e: Exception) {
            Log.fatal(e)
            return false
        }
        return true
    }

    fun start(load: GameModularLoadClass) {
        thread(name = "Start Hess Game", contextClassLoader = load, priority = Thread.MIN_PRIORITY) {
            // Here, several intermediate signal transmission modules are directly injected into this loader
            // Because this loader only has Game-lib.jar
            // 注入 接口
            CompressionDecoderUtils.zipAllReadStream(FileUtils.getMyCoreJarStream()).use {
                it.getZipAllBytes().eachAll { k, v ->
                    if (
                    // 注入接口
                        k.startsWith(HessClassPathProperties.path.replace(".", "/")) ||
                        // 覆写游戏
                        k.startsWith(HessClassPathProperties.GameHessPath.replace(".", "/"))) {
                        val name = k.replace(".class", "")
                        load.addClassBytes(name.replace("/", "."), v)
                    }
                }
            }

            val testAClass: Class<*> = load.findClass("com.corrodinggames.rts.java.Main")!!
            val mainMethod: Method = testAClass.getDeclaredMethod("main", Array<String>::class.java)
            // 禁用软件加速/关声音/关音乐/不渲染
            mainMethod.invoke(null, arrayOf("-disable_vbos", "-disable_atlas", "-nomusic", "-nosound", "-nodisplay", "-noresources"))
        }
    }
}