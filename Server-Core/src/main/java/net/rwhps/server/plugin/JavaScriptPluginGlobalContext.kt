/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.plugin

import net.rwhps.server.data.bean.BeanPluginInfo
import net.rwhps.server.data.global.Data
import net.rwhps.server.io.input.SyncByteArrayChannel
import net.rwhps.server.net.HttpRequestOkHttp
import net.rwhps.server.struct.ObjectMap
import net.rwhps.server.struct.OrderedMap
import net.rwhps.server.struct.Seq
import net.rwhps.server.util.RandomUtils
import net.rwhps.server.util.inline.ifNullResult
import net.rwhps.server.util.io.IOUtils
import net.rwhps.server.util.log.Log
import net.rwhps.server.util.plugin.ScriptResUtils
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess
import org.graalvm.polyglot.PolyglotAccess
import org.graalvm.polyglot.Source
import org.graalvm.polyglot.io.FileSystem
import java.io.*
import java.net.URI
import java.nio.channels.SeekableByteChannel
import java.nio.file.*
import java.nio.file.attribute.FileAttribute
import kotlin.io.path.Path
import kotlin.io.path.pathString

/**
 * 插件全局上下文
 * 主要用于JS插件加载
 *
 * @author RW-HPS/ZeroDegress
 * @author RW-HPS/Dr
 */
class JavaScriptPluginGlobalContext {
    private val moduleMap = ObjectMap<String, Path>()
    private val javaMap = ObjectMap<String, Path>()
    private val urlMap = Seq<String>()
    private val scriptFileSystem = OrderedMap<String, ByteArray>()
    private val modules = ObjectMap<BeanPluginInfo, String>()

    /**
     * 注册一个模块，这样在js中可通过"@module"这样的方式引用
     *
     * @param name 模块名称
     * @param main 模块入口文件
     */
    fun registerModule(name: String, main: Path) {
        Log.debug("Registered Module to esm: $name")
        moduleMap[name] = main
    }

    /**
     * 注册一个Java包，这样在js中可通过"java:package"这样的方式引用
     *
     * @param name 包名称
     * @param main 模块入口文件
     */
    fun registerJavaPackage(name: String, main: Path) {
        Log.debug("Registered Java package to esm: $name")
        javaMap[name] = main
    }

    /**
     * 添加一个ESM插件
     *
     * @param pluginInfo ESM插件JSON数据
     * @param pluginData ESM插件模块所在压缩包的Map格式
     */
    fun addESMPlugin(pluginInfo: BeanPluginInfo, pluginData: OrderedMap<String, ByteArray>) {
        val pluginName = pluginInfo.name
        registerModule(pluginName, Path("/$pluginName/${pluginInfo.main}"))

        pluginData.eachAll { k,v ->
           scriptFileSystem["/$pluginName/$k"] = v
        }

        modules[pluginInfo, RandomUtils.getRandomIetterString(10)]
    }

    /**
     * 加载全部ESM插件
     *
     * @return 插件加载数据
     */
    fun loadESMPlugins(): Seq<PluginLoadData> {
        try {
            val cx = Context.newBuilder()
                .allowExperimentalOptions(true)
                .allowPolyglotAccess(PolyglotAccess.ALL)
                .option("engine.WarnInterpreterOnly", "false")
                .option("js.esm-eval-returns-exports", "true")
                .option("js.webassembly", "true")
                .allowHostAccess(HostAccess.newBuilder()
                    .allowAllClassImplementations(true)
                    .allowAllImplementations(true)
                    .allowPublicAccess(true)
                    .allowArrayAccess(true)
                    .allowListAccess(true)
                    .allowIterableAccess(true)
                    .allowIteratorAccess(true)
                    .allowMapAccess(true)
                    .build())
                .allowHostClassLookup { _ -> true }
                .fileSystem(getOnlyReadFileSystem(scriptFileSystem))
                .allowIO(true)
                .build()
            cx.enter()

            ScriptResUtils.setFileSystem(scriptFileSystem)

            var loadScript = ""
            modules.eachAll { pluginInfo,v ->
                loadScript += "export { default as $v } from '/${pluginInfo.name}/${pluginInfo.main}';"
                loadScript += Data.LINE_SEPARATOR
            }

            val defaults = cx.eval(Source.newBuilder("js", loadScript, "\$load.mjs").build())

            return Seq<PluginLoadData>().apply {
                modules.eachAll { pluginInfo,v ->
                    this.add(PluginLoadData(
                        pluginInfo.name,
                        pluginInfo.author,
                        pluginInfo.description,
                        pluginInfo.version,
                        if(defaults.canExecute()) {
                            defaults.getMember(v).execute().`as`(Plugin::class.java)
                        } else {
                            defaults.getMember(v).`as`(Plugin::class.java)
                        }
                    ))
                }
            }
        } catch (e: Exception) {
            Log.error("JavaScript plugin loading failed: ", e)
        }
        return Seq()
    }

    /**
     * 注入指定的Java包
     * @param packname 被注入的Java包名
     */
    private fun injectJavaPackage(packname: String) {
        val classes = getPackageClasses(packname)
        val packagePathAll = "/\$java/$packname/index.js"
        if(!javaMap.containsKey(packname)) {
            registerJavaPackage(packname, Path(packagePathAll))
            val script = classes.joinToString(Data.LINE_SEPARATOR) { cls ->
                if(!cls.name.contains("$")) {
                    "export const ${cls.name.split(".").last()} = Java.type('${cls.name}');"
                } else {
                    "export const ${cls.name.split(".").last().split("$").last()} = Java.type('${cls.name}');"
                }
            }
            scriptFileSystem[packagePathAll] = script.toByteArray()
        }
    }

    private fun getPackageClasses(packname: String): Seq<Class<*>> {
        val types = Seq<Class<*>>()
        val loader = Thread.currentThread().contextClassLoader
        val resources = loader.getResources(packname.replace(".", "/"))
        while(resources.hasMoreElements()) {
            val resource = resources.nextElement()
            val file = File(resource.file)

            if (file.isDirectory) {
                val files = file.list()
                if (files != null) {
                    for (fileName in files) {
                        if(!fileName.endsWith(".class")) {
                            continue
                        }
                        val className: String =
                            ("$packname.").toString() + fileName.substring(0, fileName.lastIndexOf('.'))
                        val type = Class.forName(className)
                        types.add(type)
                    }
                }
            }
        }
        return types
    }

    /**
     * RamFileSystem
     *
     * @param fileSystem OrderedMap<String, ByteArray>
     * @return FileSystem
     */
    private fun getOnlyReadFileSystem(fileSystem: OrderedMap<String, ByteArray>): FileSystem {
        return object: FileSystem { override fun parsePath(uri: URI?): Path {
                return parsePath(uri.toString())
            }

            override fun parsePath(path: String?): Path {
                if (path == null) {
                    return ScriptResUtils.defPath
                }
                /*
                 * 我们在这里使用 contains(".."), 来识别以下路径
                 *  /a/../a.mjs 将被解析成 /a.mjs
                 */
                val parsedPath = when {
                    //完整路径
                    path.contains("..") || path.startsWith("./") || path.startsWith("/") -> Path(path)
                    //插件入口快捷方式
                    path.startsWith("@") -> moduleMap[path.removePrefix("@")] ?: ScriptResUtils.defPath
                    //java注入类型快捷访问方式
                    path.startsWith("java:") -> javaMap[path.removePrefix("java:")] ?: run {
                        injectJavaPackage(path.removePrefix("java:"))
                        javaMap[path.removePrefix("java:")] ?: ScriptResUtils.defPath.resolve(path)
                    }
                    // URL
                    path.startsWith("http://") || path.startsWith("https://") -> {
                        val js = HttpRequestOkHttp.doGet(path)
                        if (!urlMap.contains(path)) {
                            urlMap.add(path)
                            scriptFileSystem[path] = js.toByteArray()
                        }
                        Path(path)
                    }
                    // 与直接文件路径没有区别
                    path.startsWith("file://") -> ScriptResUtils.defPath.resolve(path.removePrefix("file://"))
                    // 直接引用插件模块
                    !path.contains("/") -> run {
                        moduleMap[path] ?: ScriptResUtils.defPath.resolve(path)
                    }
                    // Other
                    else -> ScriptResUtils.defPath.resolve(path)
                }
                return parsedPath
            }

            /**
             * checkAccess Ignored, Because the RAM File System does not need it
             * @param path Path
             * @param modes MutableSet<out AccessMode>
             * @param linkOptions Array<out LinkOption?>
             */
            override fun checkAccess(path: Path?, modes: MutableSet<out AccessMode>?, vararg linkOptions: LinkOption?) {
                // Ignored, because the RAM File System does not need it
            }
            /**
             * createDirectory Ignored, Because the RAM File System does not need it
             * @param dir Path
             * @param attrs Array<out FileAttribute<*>?>
             */
            override fun createDirectory(dir: Path?, vararg attrs: FileAttribute<*>?) {
                // Ignored, because the RAM File System does not need it
            }
            /**
             * delete Ignored, Because the RAM File System does not need it
             * @param path Path
             * @return Path
             */
            override fun delete(path: Path?) {
                // Ignored, because the RAM File System does not need it
            }

            override fun newByteChannel(path: Path, options: MutableSet<out OpenOption>?, vararg attrs: FileAttribute<*>?): SeekableByteChannel {
                val bytes = fileSystem[path.pathString.replace("\\","/")]
                return SyncByteArrayChannel(bytes.ifNullResult(IOUtils.EMPTY_BYTE_ARRAY) { it },true)
            }

            override fun newDirectoryStream(dir: Path?, filter: DirectoryStream.Filter<in Path>?): DirectoryStream<Path> {
                Log.debug("G newDirectoryStream")
                TODO("Not yet implemented")
            }

            /**
             * toAbsolutePath Ignored, Because the RAM File System does not need it
             * @param path Path
             * @return Path
             */
            override fun toAbsolutePath(path: Path): Path {
                return path
            }
            /**
             * toRealPath Ignored, Because the RAM File System does not need it
             * @param path Path
             * @return Path
             */override fun toRealPath(path: Path, vararg linkOptions: LinkOption?): Path {
                return path
            }

            override fun readAttributes(path: Path?, attributes: String?, vararg options: LinkOption?): MutableMap<String, Any> {
                TODO()
            }
        }
    }
}