package cn.rwhps.server.dependent.redirections

import cn.rwhps.asm.agent.AsmAgent
import cn.rwhps.asm.api.Redirection
import cn.rwhps.asm.redirections.AsmRedirections
import cn.rwhps.server.util.alone.annotations.GameSimulationLayer
import cn.rwhps.server.util.file.FileUtil
import cn.rwhps.server.util.zip.zip.ZipDecoder
import org.newdawn.slick.util.ResourceLoader

@GameSimulationLayer.GameSimulationLayer_KeyWords("FileLoader: ")
class FileLoaderRedirections : MainRedirections {
    val a = ZipFileSystemLocation(ZipDecoder(FileLoaderRedirections::class.java.getResourceAsStream("/font.zip")!!))

    init {
        ResourceLoader.addResourceLocation(a)
    }

    fun register() {
        /*
        AsmAgent.addPartialMethod("com/corrodinggames/rts/gameFramework/e/c" , arrayOf("a","(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;"))
        redirect("Lcom/corrodinggames/rts/gameFramework/e/c;a(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;") { obj: Any?, desc: String?, type: Class<*>?, args: Array<Any> ->
            //Log.clog("${args[0]}/${args[1]}")
            a.a.keys().forEach {
                //Log.clog(it)
                if (it.startsWith("${args[0]}/${args[1]}")) {
                    Log.clog(it)
                    return@redirect it
                }
            }

            // OLD
            val listFiles: Array<File>? = File(args[0].toString()).listFiles()
            if (listFiles == null) {
                //l.e(((this.f478a + "findFileExtension('" + str).toString() + "','" + str2).toString() + "'): path is not a folder")
                return@redirect null
            }
            for (file in listFiles) {
                var name: String = file.name
                if (name.contains(".")) {
                    name = name.substring(0, name.lastIndexOf(46.toChar()))
                }
                if (name == args[1]) {
                    return@redirect "${args[0]}/${file.name}}"
                }
            }

            //l.e((this.f478a + "Could not find file with path: " + str).toString() + " file:" + str2)
            return@redirect null
        }

        AsmAgent.addPartialMethod("com/corrodinggames/rts/java/e" , arrayOf("b","(IZ)Lcom/corrodinggames/rts/java/s;"))
        redirect("Lcom/corrodinggames/rts/java/e;b(IZ)Lcom/corrodinggames/rts/java/s;") { obj: Any?, desc: String?, type: Class<*>?, args: Array<Any> ->
            val filePath: String = f.f(args[0].toString().toInt()) ?: throw RuntimeException()
            Log.clog(filePath)

            try {
                Log.clog("YES")
                val a2: ImageData = e.a(a.getResourceAsStream(filePath))
                return@redirect e.a(a2, filePath)
            } catch (e2: IOException) {
                throw RuntimeException(e2)
            } catch (e3: OutOfMemoryError) {
            }
            return@redirect null
        }
        */


        // 设置 重定向文件PATH类
        AsmAgent.addPartialMethod("com/corrodinggames/rts/gameFramework/e/c" , arrayOf("f","()Ljava/lang/String;"))
        AsmAgent.addPartialMethod("com/corrodinggames/rts/gameFramework/e/c" , arrayOf("b","()Ljava/lang/String;"))

        // 重定向部分文件系统
        val filePath = FileUtil.defaultFilePath+"data/"
        AsmRedirections.customRedirection["Lcom/corrodinggames/rts/gameFramework/e/c;f()Ljava/lang/String;"] =
            Redirection { _: Any?, _: String?, _: Class<*>?, _: Array<Any?>? -> filePath }
        AsmRedirections.customRedirection["Lcom/corrodinggames/rts/gameFramework/e/c;b()Ljava/lang/String;"] =
            Redirection { _: Any?, _: String?, _: Class<*>?, _: Array<Any?>? -> filePath }
    }
}