/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.dependent.redirections.game

import net.rwhps.server.data.global.Data
import net.rwhps.server.dependent.redirections.MainRedirections
import net.rwhps.server.dependent.redirections.slick.SilckClassPathProperties
import net.rwhps.server.struct.OrderedMap
import net.rwhps.server.struct.Seq
import net.rwhps.server.util.ReflectionUtils
import net.rwhps.server.util.alone.annotations.AsmMark
import net.rwhps.server.util.alone.annotations.GameSimulationLayer
import net.rwhps.server.util.classload.GameModularLoadClass
import net.rwhps.server.util.compression.CompressionDecoderUtils
import net.rwhps.server.util.file.FileName
import net.rwhps.server.util.file.FileUtil
import net.rwhps.server.util.inline.accessibleConstructor
import net.rwhps.server.util.inline.findField
import net.rwhps.server.util.inline.readAsClassBytes
import net.rwhps.server.util.inline.toClass
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
        // 清理Hess的数据, 避免启用安全模式
        FileUtil.getFolder(Data.Plugin_GameCore_Data_Path).toFile("preferences.ini").delete()
        // 清除无用缓存
        FileUtil.getFolder(Data.Plugin_Cache_Path).delete()
    }

    override fun register() {
        /* 修改 每个加载器下 [ResourceLoader] 的初始化实现, 来为 [ResourceLoader] 实现自定义内容 */
        redirect("org/newdawn/slick/util/ResourceLoader" , arrayOf("<clinit>","()V")) { obj: Any, _: String?, _: Class<*>?, _: Array<Any?>? ->
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
            return@redirect null
        }

        // 重定向部分文件系统 (mods maps replay)
        val filePath = FileUtil.getPath(Data.Plugin_Data_Path)+"/"
        // 设置 重定向文件PATH类
        redirect("com/corrodinggames/rts/gameFramework/e/c" , arrayOf("f","()Ljava/lang/String;")) { _: Any?, _: String?, _: Class<*>?, _: Array<Any?>? ->
            filePath
        }
        redirect("com/corrodinggames/rts/gameFramework/e/c" , arrayOf("b","()Ljava/lang/String;")) { _: Any?, _: String?, _: Class<*>?, _: Array<Any?>? ->
            filePath
        }

        // 重定向资源文件系统 (Res FileSystem)
        val resAndAssetsPath = FileUtil.getPath(Data.Plugin_GameCore_Data_Path)+"/"
        redirect("com/corrodinggames/rts/gameFramework/e/c" , arrayOf("a","(Ljava/lang/String;Ljava/lang/String;)Ljava/lang/String;")) { _: Any?, _: String?, _: Class<*>?, args: Array<Any> ->
            val listFiles: Seq<File> = FileUtil.getFolder(Data.Plugin_GameCore_Data_Path).toFolder(args[0].toString()).fileList
            for (file in listFiles) {
                val name: String = FileName.getFileNameNoSuffix(file.name)
                if (name == args[1]) {
                    return@redirect "${resAndAssetsPath}${args[0]}/${file.name}"
                }
            }
            return@redirect null
        }

        // 重定向资源文件系统 (Assets FileSystem)
        redirect("android/content/res/AssetManager" , arrayOf("a","(Ljava/lang/String;I)Ljava/io/InputStream;")) { _: Any?, _: String?, _: Class<*>?, args: Array<Any> ->
            return@redirect FileInputStream("${resAndAssetsPath}assets/${args[0]}")
        }

        redirect("com/corrodinggames/rts/gameFramework/e/c" , arrayOf("f","(Ljava/lang/String;)Ljava/lang/String;")) { obj: Any, _: String?, _: Class<*>?, args: Array<Any> ->
            var d: String = (run_FileSystem(obj,"d",String::class.java).invoke(obj,args[0].toString()) as String).replace("\\","/")

            val path = args[0].toString().replace("\\","/")
            // 跳过已经绝对路径的
            if (path.contains(filePath)) {
                return@redirect path
            }

            /* 覆写 MOD 加载器路径 */
            val overrideModLoad = fun(path: String): String {
                val modFolder = "mods/units"
                val mapFolder = "mods/maps"

                val find: (String, String,String)->String? = { pathIn,  path, toPath ->
                    if (pathIn.startsWith(path)) {
                        if (pathIn == path) {
                            FileUtil.getPath(toPath)
                        } else {
                            // 一个小问题, 来自 splicePath , 他会默认在屁股后面加一个 /
                            FileUtil.splicePath(FileUtil.getPath(toPath),pathIn.substring(path.length))
                        }
                    } else {
                        null
                    }
                }

                find(path,modFolder,Data.Plugin_Mods_Path)?.run {
                    return this
                }
                find(path,mapFolder,Data.Plugin_Maps_Path)?.run {
                    // Luke奇怪的方案, 只能手动加个 / 来触发读取, 不然就成了资源路径 (Assets)
                    return "/$this"
                }


                return resAndAssetsPath + path
            }

            val cc = ReflectionUtils.findField(Class.forName("com.corrodinggames.rts.gameFramework.l",true,obj::class.java.classLoader),"aU",Boolean::class.java)!!

            return@redirect if (cc.get(null) as Boolean) {
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
        redirect("com/corrodinggames/rts/gameFramework/e/c" , arrayOf("i","(Ljava/lang/String;)Lcom/corrodinggames/rts/gameFramework/utility/j;")) { obj: Any, _: String?, _: Class<*>?, args: Array<Any> ->
            var str = args[0].toString().replace(resAndAssetsPath,"").replace("\\","/")

            if (str.startsWith("assets/") || str.startsWith("assets\\")) {
                str = str.substring("assets/".length)
            }
            val str2: String = str
            val str3 = resAndAssetsPath+"assets/"+str2
            return@redirect  try {
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