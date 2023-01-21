package net.rwhps.server.game

import net.rwhps.server.data.global.Data
import net.rwhps.server.game.simulation.HessClassPathProperties
import net.rwhps.server.net.HttpRequestOkHttp
import net.rwhps.server.util.GameModularLoadClass
import net.rwhps.server.util.compression.CompressionDecoderUtils
import net.rwhps.server.util.file.FileUtil
import net.rwhps.server.util.log.Log
import java.io.File
import java.lang.reflect.Method

/**
 * 游戏 资源文件 初始化
 *
 * @author RW-HPS/Dr
 */
object GameStartInit {
    /** Res的MD5 */
    private const val resMd5 = "6d61d95d9fd7ef679d0013efad1466de"
    /** Assets的MD5 */
    private const val assetsMd5 = "b594e7e8d2a0ad925c8ac0e00edbdbad"

    fun init(load: GameModularLoadClass): Boolean {
        try {
            // 加载游戏依赖
            CompressionDecoderUtils.zipStream(GameStartInit::class.java.getResourceAsStream("/libs.zip")!!).use {
                it.getSpecifiedSuffixInThePackage("jar",true).each { _, v ->
                    load.addSourceJar(v)
                }
            }


            val resFile = FileUtil.getFolder(Data.Plugin_GameCore_Data_Path).toFile("Game-Res.zip")
            val assetsFile = FileUtil.getFolder(Data.Plugin_GameCore_Data_Path).toFile("Game-Assets.zip")
            val temp = FileUtil.getFolder(Data.Plugin_GameCore_Data_Path,true)

            /* 鉴别两个文件的MD5, 不相同则重下 */
            if (resFile.md5 != resMd5) {
                resFile.file.delete()
            }
            if (assetsFile.md5 != assetsMd5) {
                assetsFile.file.delete()
            }

            val resTask: (FileUtil,Boolean)->Unit = { file, res ->
                (if (res) "res" else "assets").let { resName ->
                    if (!file.exists()) {
                        temp.toFolder(resName).file.delete()

                        HttpRequestOkHttp.downUrl(Data.urlData.readString("Get.Res")+file.name,file.file).also {
                            Log.clog("$resName : {0}",it)
                        }
                    }
                    if (!temp.toFolder(resName).toFile("Check").exists()) {
                        CompressionDecoderUtils.zip(file.file).use {
                            it.getZipAllBytes().each { filePath, bytes ->
                                temp.toFile(filePath).writeFileByte(bytes)
                            }
                        }
                        temp.toFolder(resName).toFile("Check").mkdir()
                    }
                }
            }

            resTask(resFile,true)
            resTask(assetsFile,false)
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
        FileUtil(File(FileUtil.getJarPath())).zipDecoder.use {
            it.getZipAllBytes().each { k, v ->
                if (k.startsWith(HessClassPathProperties.CorePath.replace(".", "/"))) {
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