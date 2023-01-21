package net.rwhps.server.plugin.beta.httpapi.responses

import net.rwhps.server.util.SystemUtil

data class AboutResp(
    val system: String = SystemUtil.osName,
    val arch: String = System.getProperty("os.arch"),
    val jvmName: String = System.getProperty("java.vm.name"),
    val jvmVersion: String = SystemUtil.javaVersion
)