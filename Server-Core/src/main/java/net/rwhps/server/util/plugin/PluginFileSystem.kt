package net.rwhps.server.util.plugin

import net.rwhps.server.struct.Seq
import java.net.URI
import java.nio.file.*
import java.nio.file.attribute.UserPrincipalLookupService
import java.nio.file.spi.FileSystemProvider

class PluginFileSystem: FileSystem() {
    private var isOpen = true
    private val rootDir = PluginFileSystemPath(this, "/")

    fun getRoot(): Path = rootDir
    override fun close() {
        isOpen = false
    }

    override fun provider(): FileSystemProvider {
        TODO("Not yet implemented")
    }

    override fun isOpen(): Boolean = isOpen

    override fun isReadOnly(): Boolean = true

    override fun getSeparator(): String = "/"

    override fun getRootDirectories(): MutableIterable<Path> = mutableListOf(rootDir)

    override fun getFileStores(): MutableIterable<FileStore> {
        TODO("Not yet implemented")
    }

    override fun supportedFileAttributeViews(): MutableSet<String> {
        TODO("Not yet implemented")
    }

    override fun getPath(first: String, vararg more: String?): Path {
        val fragments = Seq<String>()
        val abs = if(more.isNotEmpty()) {
            more[0] == "/"
        } else {
            false
        }
        if(abs) {
            fragments.add("/")
        }
        more.forEach { str ->
            str?.split("/")?.forEach { str1 ->
                if(str1.trim().isNotEmpty()) {
                    fragments.add(str1.trim())
                }
            }
        }
        return PluginFileSystemPath(this, *fragments.toArray(String::class.java))
    }

    override fun getPathMatcher(syntaxAndPattern: String?): PathMatcher {
        TODO("Not yet implemented")
    }

    override fun getUserPrincipalLookupService(): UserPrincipalLookupService {
        TODO("Not yet implemented")
    }

    override fun newWatchService(): WatchService {
        TODO("Not yet implemented")
    }

    private class PluginFileSystemPath(
        private val fileSystem: PluginFileSystem,
        vararg val fragments: String
    ): Path {
        override fun compareTo(other: Path): Int {
            return this.toString().compareTo(other.toString())
        }

        override fun register(
            watcher: WatchService,
            events: Array<out WatchEvent.Kind<*>>,
            vararg modifiers: WatchEvent.Modifier?
        ): WatchKey {
            TODO("Not yet implemented")
        }

        override fun getFileSystem(): FileSystem {
            return fileSystem
        }

        override fun isAbsolute(): Boolean = if(this.fragments.isNotEmpty()) {
            this.fragments[0] == "/"
        } else {
            false
        }

        override fun getRoot(): Path = fileSystem.getRoot()

        override fun getFileName(): Path = if(this.fragments.isNotEmpty()) {
            this.fileSystem.getPath(this.fragments.last())
        } else {
            this.fileSystem.getPath("/")
        }

        override fun getParent(): Path = if(this.fragments.isEmpty() || this.fragments.last() == "/") {
            this.fileSystem.getPath("/")
        } else {
            this.fileSystem.getPath(
                this.fragments.slice(0 until this.fragments.size).joinToString("/")
            )
        }

        override fun getNameCount(): Int = this.fragments.size

        override fun getName(index: Int): Path = this.fileSystem.getPath(this.fragments[index])

        override fun subpath(beginIndex: Int, endIndex: Int): Path = this.fileSystem.getPath(
            this.fragments.slice(beginIndex .. endIndex).joinToString("/")
        )

        override fun startsWith(other: Path): Boolean = this.toString().startsWith(other.toString())

        override fun endsWith(other: Path): Boolean = this.toString().endsWith(other.toString())

        override fun normalize(): Path {
            val nFragments = Seq<String>()
            this.fragments.forEach { str ->
                when (str) {
                    "." -> {}
                    ".." -> {
                        if(nFragments.isEmpty() || (nFragments.isNotEmpty() && (nFragments.last() == "/"))) {
                            nFragments.add(str)
                        } else {
                            nFragments.pop()
                        }
                    }
                }
            }
            return this.fileSystem.getPath("", *nFragments.toArray(String::class.java))
        }

        override fun resolve(other: Path): Path = this.fileSystem.getPath(this.toString(), other.toString())

        override fun relativize(other: Path): Path = this.fileSystem.getPath(other.toString().removePrefix(this.toString()))

        override fun toUri(): URI = URI("plugins:///${this.normalize().toString()}")

        override fun toAbsolutePath(): Path = this.fileSystem.getRoot().resolve(this)

        override fun toRealPath(vararg options: LinkOption?): Path = this.toAbsolutePath()

        override fun toString(): String = this.fragments.joinToString("/")
    }
}