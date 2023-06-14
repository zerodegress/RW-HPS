/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.plugin

import net.rwhps.server.data.global.Data
import net.rwhps.server.data.json.Json
import net.rwhps.server.dependent.LibraryManager
import net.rwhps.server.struct.Seq
import net.rwhps.server.util.IsUtil
import net.rwhps.server.util.compression.CompressionDecoderUtils
import net.rwhps.server.util.file.FileUtil
import net.rwhps.server.util.log.Log
import net.rwhps.server.util.log.Log.error
import net.rwhps.server.util.log.Log.warn
import org.graalvm.polyglot.io.FileSystem
import java.io.File
import java.io.IOException
import java.lang.reflect.InvocationTargetException
import java.net.URI
import java.net.URLClassLoader
import java.nio.ByteBuffer
import java.nio.channels.ClosedChannelException
import java.nio.channels.SeekableByteChannel
import java.nio.file.*
import java.nio.file.attribute.FileAttribute

object PluginGlobalContext {
    val jsFileSystems = ArrayList<FileSystem>()
}

/**
 * 在这里完成 插件的加载
 * @author RW-HPS/Dr
 */
class PluginsLoad {
    private fun loadPlugin(fileList: Seq<File>): Seq<PluginLoadData> {
        val jsFileSystem = JavaScriptPluginFileSystem(HashMap())
        PluginGlobalContext.jsFileSystems.add(jsFileSystem)

        val data = Seq<PluginLoadData>()
        val dataName = Seq<String>()
        val dataImport = Seq<PluginImportData>()
        for (file in fileList) {
            val zip = CompressionDecoderUtils.zip(file)
            try {
                val imp = zip.getZipNameInputStream("plugin.json")
                if (imp == null) {
                    error("Invalid Plugin file", file.name)
                    continue
                }
                // 读取 插件需要的 依赖
                // 进行注入进服务端
                val imports = zip.getZipNameInputStream("imports.json")
                if (imports != null) {
                    loadImports(Json(FileUtil.readFileString(imports)).getArraySeqData("imports"))
                }

                val json = Json(FileUtil.readFileString(imp))
                if (!GetVersion(Data.SERVER_CORE_VERSION).getIfVersion(json.getString("supportedVersions"))) {
                    warn("Plugin版本不兼容 Plugin名字为: ", json.getString("name"))
                    continue
                }
                if (IsUtil.isBlank(json.getString("import"))) {
                    val mainPlugin = if (json.getString("main").endsWith("js",true)) {
                        zip.getSpecifiedSuffixInThePackage(".mjs", true).forEach {
                            jsFileSystem.addPath(Path.of(it.key), it.value)
                        }
                        val mainJs = zip.getZipNameInputStream(json.getString("main"))
                        if (mainJs == null) {

                            error("Invalid JavaScriptPlugin Main", json.getString("main"))
                            continue
                        }
                        JavaScriptPlugin.loadESMPlugin(json.getString("main"), FileUtil.readFileString(mainJs), jsFileSystem)
                    } else {
                        loadClass(file, json.getString("main"))
                    }

                    data.add(PluginLoadData(
                        json.getString("name"),
                        json.getString("author"),
                        json.getString("description"),
                        json.getString("version"),
                        mainPlugin
                    ))
                    dataName.add(json.getString("name"))
                } else {
                    dataImport.add(PluginImportData(json, file))
                }
            } catch (e: Exception) {
                error("Failed to load", e)
            } finally {
                zip.close()
            }
        }

        // 检查是否加载了依赖插件
        var i = 0
        val count = dataImport.size
        while (i < count) {
            dataImport.eachAll { e: PluginImportData ->
                if (dataName.contains(e.pluginData.getString("import"))) {
                    try {
                        val mainPlugin = loadClass(e.file, e.pluginData.getString("main"))
                        data.add(PluginLoadData(
                            e.pluginData.getString("name"),
                            e.pluginData.getString("author"),
                            e.pluginData.getString("description"),
                            e.pluginData.getString("version"),
                            mainPlugin
                        ))
                        dataName.add(e.pluginData.getString("name"))
                        dataImport.remove(e)
                    } catch (err: Exception) {
                        error("Failed to load", e)
                    }
                }
            }
            i++
        }
        return data
    }


    private fun loadImports(importsJson: Seq<Json>) {
        val lib = LibraryManager()
        importsJson.eachAll {
            lib.implementation(it.getString("group"), it.getString("module"), it.getString("version"))
        }
        lib.loadToClassLoader()
    }

    /**
     * 根据包名加载实例
     * @param file Jar文件
     * @param main 主类的包名
     * @return Plugin实例
     * @throws InstantiationException 如果声明基础构造函数的类表示抽象类
     * @throws InvocationTargetException 如果基础构造函数引发异常
     * @throws ExceptionInInitializerError 如果此方法引发的初始化失败
     */
    @Throws(InstantiationException::class, InvocationTargetException::class, ExceptionInInitializerError::class)
    private fun loadClass(file: File, main: String): Plugin {
        val classLoader = URLClassLoader(arrayOf(file.toURI().toURL()), ClassLoader.getSystemClassLoader())
        Log.info(file.name)
        val classMain = classLoader.loadClass(main)
        //mainPlugin.classLoader = classLoader
        return classMain.getDeclaredConstructor().newInstance() as Plugin
    }

    /**
     * Plugin导入的数据
     * @property pluginData Json示例
     * @property file Jar的文件
     * @constructor PluginImportData
     */
    private class PluginImportData(@JvmField val pluginData: Json, @JvmField val file: File)

    companion object {
        /**
         * 返回文件夹内指定后辍的文件并包装为PluginLoadData
         * @param f FileUtil
         * @return Seq<PluginLoadData>
         */
        @JvmStatic
        internal fun resultPluginData(f: FileUtil): Seq<PluginLoadData> {
            val jarFileList = Seq<File>()
            val list = f.fileList
            for (file in list) {
                if (file.name.endsWith("jar") || file.name.endsWith("zip")) {
                    jarFileList.add(file)
                }
            }
            return PluginsLoad().loadPlugin(jarFileList)
        }

        @JvmStatic
        internal fun addPluginClass(name: String,author: String,description: String, version: String, main: Plugin,mkdir: Boolean,skip: Boolean,list: Seq<PluginLoadData>) {
            list.add(PluginLoadData(name, author, description, version, main, mkdir , skip))
        }
    }
}

class JavaScriptPluginFileSystem(
    private val pathMap: MutableMap<Path, ByteArray>
): FileSystem {
    private class ReadOnlySeekableByteArrayChannel(private val data: ByteArray): SeekableByteChannel {
        var position = 0
        var closed = false
        override fun close() {
            this.closed = true
        }

        override fun isOpen(): Boolean = !closed

        override fun write(src: ByteBuffer?): Int {
            throw UnsupportedOperationException()
        }

        override fun position(): Long {
            return position.toLong()
        }

        @Throws(IOException::class)
        override fun position(newPosition: Long): SeekableByteChannel {
            ensureOpen()
            position = Math.max(0, Math.min(newPosition, size())).toInt()
            return this
        }

        override fun size(): Long {
            return data.size.toLong()
        }

        @Throws(IOException::class)
        override fun read(buf: ByteBuffer): Int {
            ensureOpen()
            val remaining = size().toInt() - position
            if (remaining <= 0) {
                return -1
            }
            var readBytes = buf.remaining()
            if (readBytes > remaining) {
                readBytes = remaining
            }
            buf.put(data, position, readBytes)
            position += readBytes
            return readBytes
        }

        override fun truncate(size: Long): SeekableByteChannel {
            throw UnsupportedOperationException()
        }


        @Throws(ClosedChannelException::class)
        private fun ensureOpen() {
            if (!isOpen) {
                throw ClosedChannelException()
            }
        }

    }
    fun addPath(path: Path, arr: ByteArray) {
        pathMap[path] = arr
    }

    override fun parsePath(uri: URI?): Path {
        throw Exception("'parsePath(uri: URI?): Path' not implemented")
    }

    override fun parsePath(path: String?): Path {
        return if(path != null) {
            Path.of(path)
        } else {
            Path.of("")
        }
    }

    override fun checkAccess(path: Path?, modes: MutableSet<out AccessMode>?, vararg linkOptions: LinkOption?) {
    }

    override fun createDirectory(dir: Path?, vararg attrs: FileAttribute<*>?) {
        throw Exception("'createDirectory(dir: Path?, vararg attrs: FileAttribute<*>?)' not implemented")
    }

    override fun delete(path: Path?) {
        throw Exception("'delete(path: Path?)' not implemented")
    }

    override fun newByteChannel(
        path: Path?,
        options: MutableSet<out OpenOption>?,
        vararg attrs: FileAttribute<*>?
    ): SeekableByteChannel {
        return if(path != null) {
            val res = pathMap.entries.find { it.key == path }
            if(res != null) {
                ReadOnlySeekableByteArrayChannel(res.value)
            } else {
                ReadOnlySeekableByteArrayChannel(ByteArray(0))
            }
        } else {
            ReadOnlySeekableByteArrayChannel(ByteArray(0))
        }
    }

    override fun newDirectoryStream(dir: Path?, filter: DirectoryStream.Filter<in Path>?): DirectoryStream<Path> {
        throw Exception("'newDirectoryStream(dir: Path?, filter: DirectoryStream.Filter<in Path>?): DirectoryStream<Path>' not implemented")
    }

    override fun toAbsolutePath(path: Path?): Path {
        return path ?: Path.of("")
    }

    override fun toRealPath(path: Path?, vararg linkOptions: LinkOption?): Path {
        return path ?: Path.of("")
    }

    override fun readAttributes(
        path: Path?,
        attributes: String?,
        vararg options: LinkOption?
    ): MutableMap<String, Any> {
        throw Exception("'delete(path: Path?)' not implemented")
    }
}