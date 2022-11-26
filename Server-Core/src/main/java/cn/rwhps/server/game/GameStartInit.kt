package cn.rwhps.server.game

import cn.rwhps.server.data.global.Data
import cn.rwhps.server.dependent.LibraryManager
import cn.rwhps.server.net.HttpRequestOkHttp
import cn.rwhps.server.util.file.FileUtil
import cn.rwhps.server.util.log.Log
import cn.rwhps.server.util.zip.zip.ZipDecoder
import java.io.File

object GameStartInit {
    private val resMd5 = "6d61d95d9fd7ef679d0013efad1466de"
    private val assetsMd5 = "b594e7e8d2a0ad925c8ac0e00edbdbad"

    fun init(): Boolean {
        try {
            // 加载游戏依赖
            val gameCoreLibs = FileUtil.getFolder(Data.Plugin_GameCore_Lib_Path,true)
            ZipDecoder(GameStartInit::class.java.getResourceAsStream("/libs.zip")!!).getSpecifiedSuffixInThePackageAllFileName("jar").each { k, v ->
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


            val resFile = FileUtil.getFolder(Data.Plugin_Cache_Path).toFile("Game-Res.zip")
            val assetsFile = FileUtil.getFolder(Data.Plugin_Cache_Path).toFile("Game-Assets.zip")
            val temp = FileUtil(File(".").canonicalPath,true)

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
                        ZipDecoder(file.file).getZipAllBytes().each { filePath, bytes ->
                            temp.toFile(filePath).writeFileByte(bytes,true)
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
}