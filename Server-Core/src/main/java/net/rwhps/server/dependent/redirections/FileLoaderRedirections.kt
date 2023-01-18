package net.rwhps.server.dependent.redirections

import net.rwhps.asm.agent.AsmCore
import net.rwhps.server.data.global.Data
import net.rwhps.server.dependent.redirections.slick.SilckClassPathProperties
import net.rwhps.server.struct.OrderedMap
import net.rwhps.server.struct.Seq
import net.rwhps.server.util.GameModularLoadClass
import net.rwhps.server.util.ReflectionUtils
import net.rwhps.server.util.alone.annotations.AsmMark
import net.rwhps.server.util.alone.annotations.GameSimulationLayer
import net.rwhps.server.util.compression.CompressionDecoderUtils
import net.rwhps.server.util.file.FileName
import net.rwhps.server.util.file.FileUtil
import net.rwhps.server.util.inline.*
import net.rwhps.server.util.log.Log
import java.io.*
import java.lang.reflect.Constructor
import java.lang.reflect.Method


/**
 * 通过 ASM 来覆写 游戏的文件系统来达到自定义位置
 * 不要骂了 只能这样写了
 *
 * @property font 把字体放在ZIP内来减少大小
 * @author RW-HPS/Dr
 */
@AsmMark.ClassLoaderCompatible
@GameSimulationLayer.GameSimulationLayer_KeyWords("FileLoader: ")
class FileLoaderRedirections : MainRedirections {
    private val font = CompressionDecoderUtils.lz77Stream(FileUtil.getInternalFileStream("/font.7z")).getZipAllBytes()
    private val fileSystemAsm = "FileLoader-ASM: "
    private val fileSystemLocation = FileUtil.getFolder(Data.Plugin_GameCore_Data_Path).file

    init {
        // Mkdie Mods Folder
        FileUtil.getFolder(Data.Plugin_Mods_Path).mkdir()
    }

    fun register() {
        AsmCore.addPartialMethod("org/newdawn/slick/util/ResourceLoader" , arrayOf("<clinit>","()V")) { obj: Any, _: String?, _: Class<*>?, _: Array<Any?>? ->
            val classIn = obj as Class<*>
            val classLoader = classIn.classLoader

            val resourceLoader = SilckClassPathProperties.ResourceLoader.toClass(classLoader)!!
            val list = ArrayList<Any>()

            val load = if (classLoader is GameModularLoadClass) {
                classLoader.loadClassBytes(SilckClassPathProperties.DrFileSystemLocation,SilckClassPathProperties.DrFileSystemLocation.readAsClassBytes())!!
            } else {
                SilckClassPathProperties.DrFileSystemLocation.toClass(classLoader)!!
            }

            list.add(load.accessibleConstructor(OrderedMap::class.java).newInstance(font))
            list.add(SilckClassPathProperties.ClasspathLocation.toClass(classLoader)!!.accessibleConstructor().newInstance())
            list.add(SilckClassPathProperties.FileSystemLocation.toClass(classLoader)!!.accessibleConstructor(File::class.java).newInstance(fileSystemLocation))
            resourceLoader.findField("locations")!!.set(null,list)
            return@addPartialMethod null
        }

        // 重定向部分文件系统 (mods maps replay)
        val filePath = FileUtil.getPath(Data.Plugin_Data_Path)+"/"
        // 设置 重定向文件PATH类
        AsmCore.addPartialMethod("com/corrodinggames/rts/gameFramework/e/c" , arrayOf("f","()Ljava/lang/String;")) { _: Any?, _: String?, _: Class<*>?, _: Array<Any?>? ->
            filePath
        }
        AsmCore.addPartialMethod("com/corrodinggames/rts/gameFramework/e/c" , arrayOf("b","()Ljava/lang/String;")) { _: Any?, _: String?, _: Class<*>?, _: Array<Any?>? ->
            filePath
        }

        // 重定向资源文件系统 (Res FileSystem)
        val resAndAssetsPath = FileUtil.getPath(Data.Plugin_GameCore_Data_Path)+"/"
        AsmCore.addPartialMethod("com/corrodinggames/rts/gameFramework/e/c" , arrayOf("a","(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;")) { _: Any?, _: String?, _: Class<*>?, args: Array<Any> ->
            val listFiles: Seq<File> = FileUtil.getFolder(Data.Plugin_GameCore_Data_Path).toFolder(args[0].toString()).fileList
            for (file in listFiles) {
                val name: String = FileName.getFileNameNoSuffix(file.name)
                if (name == args[1]) {
                    return@addPartialMethod "${resAndAssetsPath}${args[0]}/${file.name}"
                }
            }
            return@addPartialMethod null
        }

        // 重定向资源文件系统 (Assets FileSystem)
        AsmCore.addPartialMethod("android/content/res/AssetManager" , arrayOf("a","(Ljava/lang/String;I)Ljava/io/InputStream;")) { _: Any?, _: String?, _: Class<*>?, args: Array<Any> ->
            return@addPartialMethod FileInputStream("${resAndAssetsPath}assets/${args[0]}")
        }

        AsmCore.addPartialMethod("com/corrodinggames/rts/gameFramework/e/c" , arrayOf("f","(Ljava/lang/String;)Ljava/lang/String;")) { obj: Any, _: String?, _: Class<*>?, args: Array<Any> ->
            var d: String = (run_FileSystem(obj,"d",String::class.java).invoke(obj,args[0].toString()) as String).replace("\\","/")

            val path = args[0].toString().replace("\\","/")
            // 跳过已经绝对路径的
            if (path.contains(filePath)) {
                return@addPartialMethod path
            }

            /* 覆写 MOD 加载器路径 */
            val overrideModLoad = fun(path: String): String {
                val modFolder = "mods/units"
                if (path.startsWith(modFolder)) {
                    return if (path == modFolder) {
                        FileUtil.getPath(Data.Plugin_Mods_Path)
                    } else {
                        // 一个小问题, 来自 splicePath , 他会默认在屁股后面加一个 /
                        FileUtil.splicePath(FileUtil.getPath(Data.Plugin_Mods_Path),path.substring(modFolder.length))
                    }

                }
                return resAndAssetsPath + path
            }

            val cc = ReflectionUtils.findField(Class.forName("com.corrodinggames.rts.gameFramework.l",true,obj::class.java.classLoader),"aU",Boolean::class.java)!!

            return@addPartialMethod if (cc.get(null) as Boolean) {
                if (d.startsWith("/SD/rusted_warfare_maps")) {
                    d = "/SD/mods/maps" + d.substring("/SD/rusted_warfare_maps".length)
                    //l.e(fileSystemAsm + "convertAbstractPath: Changing to:" + d)
                }
                if (d.startsWith("/SD/rustedWarfare/maps")) {
                    d = "/SD/mods/maps" + d.substring("/SD/rustedWarfare/maps".length)
                    //l.e(fileSystemAsm + "convertAbstractPath2: Changing to:" + d)
                }

                if (d.startsWith("/SD/") || d.startsWith("\\SD\\")) {
                    var substring = d.substring("/SD/".length)
                    if (substring.startsWith("rustedWarfare/")) {
                        substring = substring.substring("rustedWarfare/".length)
                    }
                    overrideModLoad(substring)
                } else if ((run_FileSystem(obj,"c",String::class.java).invoke(obj,d) as Boolean)) {
                    d
                } else {
                    resAndAssetsPath + "assets/$d"
                }
            } else if (d.startsWith("/SD/")) {
                var substring2 = d.substring("/SD/".length)
                if (substring2.startsWith("rustedWarfare/")) {
                    substring2 = substring2.substring("rustedWarfare/".length)
                }
                overrideModLoad(substring2)
            } else {
                d
            }
        }

        // 重定向 流系统
        AsmCore.addPartialMethod("com/corrodinggames/rts/gameFramework/e/c" , arrayOf("i","(Ljava/lang/String;)Lcom/corrodinggames/rts/gameFramework/utility/j;")) { obj: Any, _: String?, _: Class<*>?, args: Array<Any> ->
            var str = args[0].toString().replace(resAndAssetsPath,"").replace("\\","/")

            if (str.startsWith("assets/") || str.startsWith("assets\\")) {
                str = str.substring("assets/".length)
            }
            val str2: String = str
            val str3 = resAndAssetsPath+"assets/"+str2
            return@addPartialMethod  try {
                try {
                    findNewClass_AssetInputStream(obj,InputStream::class.java,String::class.java,String::class.java).newInstance(
                        // AssetManager
                        FileInputStream("${resAndAssetsPath}assets/${str2}"),
                        str3, str2)
                } catch (e: FileNotFoundException) {
                    null
                }
            } catch (e2: IOException) {
                Log.error(fileSystemAsm + "Could not find asset:" + str3)
                null
            }
        }
    }


    private fun run_FileSystem(obj: Any, method: String, vararg paramTypes: Class<*>): Method {
        return ReflectionUtils.findMethod(Class.forName("com.corrodinggames.rts.gameFramework.e.c",true,obj::class.java.classLoader),method,*paramTypes)!!
    }

    private fun findClass_AssetManager(obj: Any): Any {
        val handler = ReflectionUtils.findMethod(Class.forName("com.corrodinggames.rts.appFramework.c",true,obj::class.java.classLoader),"a")
        val context = handler!!.invoke(null)
        val context0 = ReflectionUtils.findMethod(Class.forName("android.content.Context",true,obj::class.java.classLoader),"d")
        return context0!!.invoke(context)
    }

    private fun run_AssetManager(obj: Any, method: String, vararg paramTypes: Class<*>): Method {
        return ReflectionUtils.findMethod(Class.forName("android.content.res.AssetManager",true,obj::class.java.classLoader),method,*paramTypes)!!
    }

    // J
    private fun findNewClass_AssetInputStream(obj: Any, vararg paramTypes: Class<*>): Constructor<*> {
        return ReflectionUtils.accessibleConstructor(Class.forName("com.corrodinggames.rts.gameFramework.utility.j",true,obj::class.java.classLoader), *paramTypes)
    }
}