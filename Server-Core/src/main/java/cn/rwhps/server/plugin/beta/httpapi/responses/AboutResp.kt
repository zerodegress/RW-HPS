package cn.rwhps.server.plugin.beta.httpapi.responses

import cn.rwhps.server.data.global.Data

data class AboutResp(
    val system: String = Data.core.osName,
    val arch: String = System.getProperty("os.arch"),
    val jvmName: String = System.getProperty("java.vm.name"),
    val jvmVersion: String = Data.core.javaVersion
)