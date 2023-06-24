/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.plugin

import net.rwhps.server.data.event.GameOverData
import net.rwhps.server.data.global.Data
import net.rwhps.server.data.json.Json
import net.rwhps.server.data.player.AbstractPlayer
import net.rwhps.server.func.ConsMap
import net.rwhps.server.func.ConsSeq
import net.rwhps.server.func.Prov
import net.rwhps.server.game.GameMaps
import net.rwhps.server.game.GameUnitType
import net.rwhps.server.game.simulation.core.AbstractPlayerData
import net.rwhps.server.io.input.SyncByteArrayChannel
import net.rwhps.server.io.output.CompressOutputStream
import net.rwhps.server.io.output.DisableSyncByteArrayOutputStream
import net.rwhps.server.io.packet.Packet
import net.rwhps.server.net.GroupNet
import net.rwhps.server.net.core.ConnectionAgreement
import net.rwhps.server.net.core.DataPermissionStatus
import net.rwhps.server.net.core.IRwHps
import net.rwhps.server.net.core.server.AbstractNetConnectServer
import net.rwhps.server.plugin.event.AbstractEvent
import net.rwhps.server.plugin.event.AbstractGlobalEvent
import net.rwhps.server.struct.IntMap
import net.rwhps.server.struct.ObjectMap
import net.rwhps.server.struct.OrderedMap
import net.rwhps.server.struct.Seq
import net.rwhps.server.util.I18NBundle
import net.rwhps.server.util.PacketType
import net.rwhps.server.util.RandomUtils
import net.rwhps.server.util.compression.core.AbstractDecoder
import net.rwhps.server.util.file.FileUtils
import net.rwhps.server.util.game.CommandHandler
import net.rwhps.server.util.inline.ifNullResult
import net.rwhps.server.util.io.IOUtils
import net.rwhps.server.util.log.Log
import org.graalvm.polyglot.Context
import org.graalvm.polyglot.HostAccess
import org.graalvm.polyglot.Source
import org.graalvm.polyglot.io.FileSystem
import java.io.*
import java.net.URI
import java.nio.channels.SeekableByteChannel
import java.nio.file.*
import java.nio.file.attribute.FileAttribute
import java.util.*
import kotlin.io.path.Path
import kotlin.io.path.pathString

/**
 * 插件全局上下文
 * 主要用于JS插件加载
 * @author RW-HPS/ZeroDegress
 */
class JavaScriptPluginGlobalContext {
    private val moduleMap = ObjectMap<String, Path>()
    private val javaMap = ObjectMap<String, Path>()
    private val scriptFileSystem = OrderedMap<String, ByteArray>()
    private val modules = ObjectMap<Json, String>()

    init {
        injectJavaClass<AbstractDecoder>()
        injectJavaClass<AbstractEvent>()
        injectJavaClass<AbstractGlobalEvent>()
        injectJavaClass<AbstractNetConnectServer>()
        injectJavaClass<AbstractPlayer>()
        injectJavaClass<AbstractPlayerData>()
        injectJavaClass<ConnectionAgreement>()
        injectJavaClass<CompressOutputStream>()
        injectJavaClass<CommandHandler>()
        injectJavaClass<CommandHandler.Command>()
        injectJavaClass<CommandHandler.CommandResponse>()
        injectJavaClass<CommandHandler.CommandRunner<Any>>()
        injectJavaClass<CommandHandler.ResponseType>()
        injectJavaClass<ConsMap<Any, Any>>()
        injectJavaClass<ConsSeq<Any>>()
        injectJavaClass<ConnectionAgreement>()
        injectJavaClass<DisableSyncByteArrayOutputStream>()
        injectJavaClass<DataPermissionStatus.ServerStatus>("ServerStatus")
        injectJavaClass<File>()
        injectJavaClass<FileOutputStream>()
        injectJavaClass<FileUtils>()
        injectJavaClass<GameMaps.MapData>("MapData")
        injectJavaClass<GameOverData>()
        injectJavaClass<GameUnitType.GameActions>("GameActions")
        injectJavaClass<GameUnitType.GameUnits>("GameUnits")
        injectJavaClass<GroupNet>()
        injectJavaClass<I18NBundle>()
        injectJavaClass<IntMap<Any>>()
        injectJavaClass<InputStream>()
        injectJavaClass<InputStreamReader>()
        injectJavaClass<IRwHps.NetType>("NetType")
        injectJavaClass<ObjectMap<Any, Any>>()
        injectJavaClass<OrderedMap<Any, Any>>()
        injectJavaClass<OutputStream>()
        injectJavaClass<Packet>()
        injectJavaClass<PacketType>()
        injectJavaClass<Plugin>()
        injectJavaClass<Properties>()
        injectJavaClass<Prov<Any>>()
        injectJavaClass<Log>()
    }

    /**
     * 注册一个模块，这样在js中可通过"@module"这样的方式引用
     * @param name 模块名称
     * @param main 模块入口文件
     */
    fun registerModule(name: String, main: Path) {
        Log.debug("Registered Module to esm: $name")
        moduleMap[name] = main
    }

    /**
     * 注册一个Java包，这样在js中可通过"java:package"这样的方式引用
     * @param name 包名称
     * @param main 模块入口文件
     */
    fun registerJavaPackage(name: String, main: Path) {
        Log.debug("Registered Java package to esm: $name")
        javaMap[name] = main
    }

    /**
     * 添加一个ESM插件
     * @param json ESM插件JSON数据
     * @param pluginData ESM插件模块所在压缩包的Map格式
     */
    fun addESMPlugin(json: Json, pluginData: OrderedMap<String, ByteArray>) {
        val pluginName = json.getString("name")
        registerModule(pluginName, Path("/$pluginName/${json.getString("main")}"))

        pluginData.eachAll { k,v ->
           scriptFileSystem["/$pluginName$k"] = v
        }

        modules[json, RandomUtils.getRandomIetterString(10)]
    }

    /**
     * 加载全部ESM插件
     * @return 插件加载数据
     */
    fun loadESMPlugins(): Seq<PluginLoadData> {
        try {
            val cx = Context.newBuilder()
                .allowExperimentalOptions(true)
                .option("engine.WarnInterpreterOnly", "false")
                .option("js.esm-eval-returns-exports", "true")
                .allowHostAccess(HostAccess.newBuilder()
                    .allowAllClassImplementations(true)
                    .allowAllImplementations(true)
                    .allowPublicAccess(true)
                    .build())
                .allowHostClassLookup { _ -> true }
                .fileSystem(getOnlyReadFileSystem(scriptFileSystem))
                .allowIO(true)
                .build()
            cx.enter()

            var loadScript = ""
            modules.eachAll { k,v ->
                loadScript += "export { default as $v } from '/${k.getString("name")}/${k.getString("main")}';"
                loadScript += Data.LINE_SEPARATOR
            }

            val defaults = cx.eval(Source.newBuilder("js", loadScript, "\$load.mjs").build())

            return Seq<PluginLoadData>().apply {
                modules.eachAll { k,v ->
                    this.add(PluginLoadData(
                        k.getString("name"),
                        k.getString("author"),
                        k.getString("description"),
                        k.getString("version"),
                        if(defaults.canExecute()) {
                            defaults.getMember(v).execute().`as`(Plugin::class.java)
                        } else {
                            defaults.getMember(v).`as`(Plugin::class.java)
                        }
                    ))
                }
            }
        } catch (e: Exception) {
            error("JavaScript plugin loading failed: $e")
        }
    }

    /**
     * 将指定Java类型注入
     */
    private inline fun <reified T> injectJavaClass(rename: String? = null) {
        val packageName = T::class.java.`package`.name
        val packagePathAll = "/\$java/$packageName/index.mjs"

        if(!scriptFileSystem.containsKey("\$java/$packageName")) {
            registerJavaPackage(packageName, Path(packagePathAll))
        }

        scriptFileSystem[packagePathAll] = """
            export const ${rename ?: T::class.java.name.split(".").last()} = Java.type('${T::class.java.name}');
        """.trimIndent().toByteArray()
    }

    /**
     * RamFileSystem
     *
     * @param fileSystem OrderedMap<String, ByteArray>
     * @return FileSystem
     */
    private fun getOnlyReadFileSystem(fileSystem: OrderedMap<String, ByteArray>): FileSystem {
        return object: FileSystem {
            val defPath = Path("/")
            override fun parsePath(uri: URI?): Path {
                return parsePath(uri.toString())
            }

            override fun parsePath(path: String?): Path {
                if (path == null) {
                    return defPath
                }
                /*
                 * 我们在这里使用 contains(".."), 来识别以下路径
                 *  /a/../a.mjs 将被解析成 /a.mjs
                 */
                val parsedPath = if(path.contains("..") || path.startsWith("./") || path.startsWith("/")) {
                    //完整路径
                    Path(path)
                } else if(path.startsWith("@")) {
                    //插件入口快捷方式
                    moduleMap[path.removePrefix("@")] ?: defPath
                } else if(path.startsWith("java:")) {
                    //java注入类型快捷访问方式
                    javaMap[path.removePrefix("java:")] ?: defPath
                } else {
                    defPath.resolve(path)
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
                Log.debug("J readAttributes")
                TODO("Not yet implemented")
            }
        }
    }
}