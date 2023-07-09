package net.rwhps.server.plugin

import net.rwhps.server.util.log.Log
import net.rwhps.server.util.plugin.PluginFileSystem
import org.junit.jupiter.api.Test

class PluginFileSystemTest {
    private val fakeFileSystem = PluginFileSystem()
    init {
        Log.set("DEBUG")
        Log.setCopyPrint(true)
    }

    fun path(first: String, vararg more: String?) = fakeFileSystem.getPath(first, *more)

    @Test
    fun testToString() {
        assert(fakeFileSystem.getRoot().toString() == "/") {
            "根目录解析为：${fakeFileSystem.getRoot().toString()}"
        }
        assert(fakeFileSystem.getPath("/a/b/c").toString() == "/a/b/c") {
            "目录'/a/b/c'解析为：${fakeFileSystem.getPath("/a/b/c").toString()}"
        }
        assert(fakeFileSystem.getPath("/a/b/c/").toString() == "/a/b/c") {
            "目录'/a/b/c/'解析为：${fakeFileSystem.getPath("/a/b/c/").toString()}"
        }
        assert(fakeFileSystem.getPath("a/b").toString() == "a/b") {
            "目录'a/b'解析为：${fakeFileSystem.getPath("a/b").toString()}"
        }
        assert(fakeFileSystem.getPath("").toString() == "") {
            "目录''解析为：${fakeFileSystem.getPath("").toString()}"
        }
    }

    @Test
    fun testResolve() {
        assert(path("a").resolve(path("b")).toString() == "a/b") {
            "目录'a'下'b 解析为：${path("a").resolve(path("b"))}"
        }
    }
}