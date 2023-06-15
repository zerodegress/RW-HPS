package net.rwhps.server.plugin

import com.google.common.jimfs.Configuration
import com.google.common.jimfs.Jimfs
import org.graalvm.polyglot.io.FileSystem
import java.io.IOException
import java.net.URI
import java.nio.ByteBuffer
import java.nio.channels.ClosedChannelException
import java.nio.channels.SeekableByteChannel
import java.nio.file.*
import java.nio.file.attribute.FileAttribute

/**
 * JS插件文件系统
 * 为JS插件加载提供虚拟环境
 * @author RW-HPS/ZeroDegress
 */
class JavaScriptPluginFileSystem: FileSystem {

    private val memoryFileSystem = Jimfs.newFileSystem(
        Configuration.unix()
            .toBuilder()
            .setWorkingDirectory("/")
            .build())
    private val moduleMap: MutableMap<String, Path> = HashMap()
    private val javaMap: MutableMap<String, Path> = HashMap()

    /**
     * 只读数据流
     * @author RW-HPS/ZeroDegress
     */
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

    /**
     * 注册一个模块，这样在js中可通过"@module"这样的方式引用
     * @param name 模块名称
     * @param main 模块入口文件
     */
    fun registerModule(name: String, main: Path) {
        moduleMap[name] = main
    }

    /**
     * 注册一个Java包，这样在js中可通过"java:package"这样的方式引用
     * @param name 包名称
     * @param main 模块入口文件
     */
    fun registerJavaPackage(name: String, main: Path) {
        //debug的时候用
        //Log.clog("Registered Java package to esm:$name")
        javaMap[name] = main
    }

    /**
     * 获取虚拟路径
     */
    fun getPath(first: String, vararg more: String?): Path {
        return memoryFileSystem.getPath(first, *more)
    }

    override fun parsePath(uri: URI?): Path {
        return parsePath(uri?.toString())
    }

    override fun parsePath(path: String?): Path {
        val parsedPath = if(path != null) {
            if(path.startsWith("../") || path.startsWith("./") || path.startsWith("/")) {
                memoryFileSystem.getPath(path)
            } else if(path.startsWith("@")) {
                moduleMap[path.removePrefix("@")] ?: memoryFileSystem.getPath("/")
            } else if(path.startsWith("java:")) {
                javaMap[path.removePrefix("java:")] ?: memoryFileSystem.getPath("/")
            } else {
                memoryFileSystem.getPath("/").resolve(path)
            }
        } else {
            memoryFileSystem.getPath("/")
        }
        return parsedPath
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
            ReadOnlySeekableByteArrayChannel(Files.readAllBytes(path))
        } else {
            ReadOnlySeekableByteArrayChannel(ByteArray(0))
        }
    }

    override fun newDirectoryStream(dir: Path?, filter: DirectoryStream.Filter<in Path>?): DirectoryStream<Path> {
        throw Exception("'newDirectoryStream(dir: Path?, filter: DirectoryStream.Filter<in Path>?): DirectoryStream<Path>' not implemented")
    }

    override fun toAbsolutePath(path: Path?): Path = path?.toAbsolutePath() ?: memoryFileSystem.getPath("/")

    override fun toRealPath(path: Path?, vararg linkOptions: LinkOption?): Path = path?.toRealPath() ?: memoryFileSystem.getPath("/")

    override fun readAttributes(
        path: Path?,
        attributes: String?,
        vararg options: LinkOption?
    ): MutableMap<String, Any> {
        throw Exception("'delete(path: Path?)' not implemented")
    }
}