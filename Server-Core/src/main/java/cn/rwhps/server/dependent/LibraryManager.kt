/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.dependent

import cn.rwhps.server.data.global.Data
import cn.rwhps.server.io.input.DisableSyncByteArrayInputStream
import cn.rwhps.server.net.HttpRequestOkHttp
import cn.rwhps.server.struct.Seq
import cn.rwhps.server.util.file.FileUtil
import cn.rwhps.server.util.log.Log
import cn.rwhps.server.util.log.exp.LibraryManagerError
import org.apache.maven.model.io.xpp3.MavenXpp3Reader
import org.apache.maven.project.MavenProject
import java.io.File


class LibraryManager : AgentAttachData() {
    private val loadEnd = Seq<File>()
    private val load = Seq<File>()

    private val URL: String? = null
    private val dependenciesDown = Seq<ImportData>()
    private val dependenciesFile = Seq<File>()
    private val tempGroup = Seq<ImportGroupData>()

    private val source = Seq<UrlData>()

    fun addSource(source: UrlData) {
        this.source.add(source)
    }

    /**
     * 导入本地的依赖
     * @param file FileUtil
     */
    fun customImportLib(file: FileUtil) {
        dependenciesFile.add(file.file)
    }

    fun importLib(text: String, down: Boolean = true) {
        val array = text.split(":")
        importLib(array[0],array[1],array[2],down)
    }

    /**
     * 云端下载
     * @param downUrl String
     * @param name String
     * @param version String
     */
    fun importLib(group: String, module: String, version: String, down: Boolean = true) {
        val groupArray = group.split("\\.".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val constructSource = StringBuilder("/")
        for (s in groupArray) {
            constructSource.append(s).append("/")
        }
        constructSource.append(module).append("/")
            .append(version).append("/")
            .append(module).append("-")
            .append(version)
        val savePath = "$module-$version.jar"
        val groupLibData = ImportGroupData(group,module,version)
        if (tempGroup.contains(groupLibData)) {
            Log.clog(module)
            return
        }
        tempGroup.add(groupLibData)
        getDepend(ImportData(constructSource.toString(), savePath),down)
    }

    fun load() {
        dependenciesDown.each {
            val a = it.fileName
            HttpRequestOkHttp.downUrl(it.getDownUrl(),FileUtil.getFolder(Data.Plugin_Lib_Path).toFile(it.fileName).file).also {
                if (it) {
                    Log.clog("Yes $a")
                } else {
                    Log.clog("Error $a")
                }
            }
        }
    }

    @Throws(LibraryManagerError.DependencyNotFoundException::class)
    private fun getDepend(importData: ImportData, down: Boolean) {
        source.each {
            val result = HttpRequestOkHttp.doGet(importData.getDownPom(it))
            if (result.isNotBlank() && !result.trim().equals("404 Not Found",true)) {
                importData.mainSource = it
                try {
                    val mavenreader = MavenXpp3Reader()
                    val model = mavenreader.read(DisableSyncByteArrayInputStream(result.toByteArray()))
                    val project = MavenProject(model)
                    for (lib in project.dependencies) {
                        when (lib.scope) {
                            "test","provided" -> {}
                            "compile","runtime" -> importLib(lib.groupId,lib.artifactId,lib.version, down)
                            else -> {}
                        }
                    }
                } catch (ex: Exception) {
                }
                if (down) {
                    dependenciesDown.add(importData)
                }
                return@each
            }
        }
    }

    private class ImportGroupData(val group: String, val module: String, val version: String) {
        override fun equals(other: Any?): Boolean {
            if (other is ImportGroupData) {
                return other.group == group && other.module == module
            }
            return false
        }
    }

    private class ImportData(private val constructSource: String, val fileName: String) {
        var mainSource: UrlData = UrlData.Maven

        fun getDownUrl(): String {
            return if (constructSource.startsWith("/")) {
                "${mainSource.url}$constructSource.jar"
            } else {
                "${mainSource.url}/$constructSource.jar"
            }
        }

        fun getDownPom(mainSource: UrlData): String {
            return if (constructSource.startsWith("/")) {
                "${mainSource.url}$constructSource.pom"
            } else {
                "${mainSource.url}/$constructSource.pom"
            }
        }
    }

    public enum class UrlData(val url: String) {
        Maven("https://repo1.maven.org/maven2"),
        MavenAli("https://maven.aliyun.com/repository/central"),
        JitPack("https://jitpack.io"),
    }
}