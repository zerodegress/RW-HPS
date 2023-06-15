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

class JavaScriptPluginFileSystem: FileSystem {

    private val memoryFileSystem = Jimfs.newFileSystem(Configuration.unix())
    private val moduleMap: MutableMap<String, Path> = HashMap()

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

    fun registerModule(name: String, main: Path) {
        moduleMap[name] = main
    }

    fun getPath(first: String, vararg more: String?): Path {
        return memoryFileSystem.getPath(first, *more)
    }

    override fun parsePath(uri: URI?): Path = memoryFileSystem.getPath(uri?.path ?: "/")

    override fun parsePath(path: String?): Path {
        val parsedPath = if(path != null) {
            if(path.startsWith("../") || path.startsWith("./") || path.startsWith("/")) {
                memoryFileSystem.getPath(path)
            } else if(path.startsWith("@")) {
                moduleMap[path.removePrefix("@")] ?: memoryFileSystem.getPath("/")
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