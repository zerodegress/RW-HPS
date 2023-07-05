/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

import org.gradle.api.Project
import org.gradle.kotlin.dsl.maven
import org.gradle.kotlin.dsl.repositories
import java.io.File
import java.io.IOException

fun Project.setRepositories() {
    repositories {
        maven(url = "https://maven.aliyun.com/repository/public")
        maven(url = "https://mirrors.cloud.tencent.com/nexus/repository/maven-public")
        maven(url = "https://repo.huaweicloud.com/repository/maven")
        maven(url = "https://jitpack.io")
        maven(url = "https://plugins.gradle.org/m2")
        mavenCentral()
        gradlePluginPortal()
    }
}

fun Project.makeDependTree() {
    val project = this

    val implementationFile = file("${Data.mavenPath}/${this.project.name}/implementation.txt")
    val compileOnlyFile = file("${Data.mavenPath}/${this.project.name}/compileOnly.txt")

    implementationFile.mk()
    compileOnlyFile.mk()

    var implementation = ""
    var compileOnly = ""
    val onlyList = ArrayList<String>()

    project.configurations.findByName("compileOnly")!!.allDependencies.forEach {
        val result = configurations.detachedConfiguration(it).resolvedConfiguration
        result.firstLevelModuleDependencies.forEach { moduleDependency ->
            moduleDependency.allModuleArtifacts.forEach { dependency ->
                onlyList.add(dependency.moduleVersion.id.group + dependency.moduleVersion.id.name + dependency.classifier)
            }
        }
    }

    project.configurations.findByName("compileClasspath")!!.resolvedConfiguration.resolvedArtifacts.forEach { artifact ->
        if (onlyList.contains(artifact.moduleVersion.id.group + artifact.moduleVersion.id.name + artifact.classifier)) {
            compileOnly += "${artifact.type}:${artifact.moduleVersion.id.group}:${artifact.moduleVersion.id.name}:${artifact.moduleVersion.id.version}:${artifact.classifier}${Data.lineSeparator}"
        } else {
            implementation += "${artifact.type}:${artifact.moduleVersion.id.group}:${artifact.moduleVersion.id.name}:${artifact.moduleVersion.id.version}:${artifact.classifier}${Data.lineSeparator}"
        }
    }
    implementationFile.writeText(implementation)
    compileOnlyFile.writeText(compileOnly)
}

fun File.mk() {
    this.parentFile?.mkdirs()

    if (this.isDirectory) {
        return
    }

    if (!this.exists()) {
        try {
            this.createNewFile()
        } catch (e: IOException) {
            error("Mk file")
        }
    }
}