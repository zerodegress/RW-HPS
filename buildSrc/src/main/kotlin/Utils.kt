/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package buildSrc

import org.gradle.api.Project

val lineSeparator = System.getProperty("line.separator")!!
val mavenPath = "/src/main/resources/maven"

fun Project.makeDependTree() {
    val project = this

    val implementationFile = file("$mavenPath/${this.project.name}/implementation.txt").apply { mkdirs();delete() }
    val compileOnlyFile = file("$mavenPath/${this.project.name}/compileOnly.txt").apply { mkdirs();delete() }

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
            compileOnly += "${artifact.type}:${artifact.moduleVersion.id.group}:${artifact.moduleVersion.id.name}:${artifact.moduleVersion.id.version}:${artifact.classifier}$lineSeparator"
        } else {
            implementation += "${artifact.type}:${artifact.moduleVersion.id.group}:${artifact.moduleVersion.id.name}:${artifact.moduleVersion.id.version}:${artifact.classifier}$lineSeparator"
        }
    }
    implementationFile.writeText(implementation)
    compileOnlyFile.writeText(compileOnly)
}