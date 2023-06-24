package net.rwhps.server.plugin.beta.httpapi.responses

import net.rwhps.server.util.SystemUtils

data class AboutResp(
    val system: String = SystemUtils.osName,
    val arch: String = System.getProperty("os.arch"),
    val jvmName: String = System.getProperty("java.vm.name"),
    val jvmVersion: String = SystemUtils.javaVersion
)