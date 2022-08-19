package cn.rwhps.server.plugin.beta.httpapi.responses

data class AboutResp(
    val system: String = System.getProperty("os.name"),
    val arch: String = System.getProperty("os.arch"),
    val jvmName: String = System.getProperty("java.vm.name"),
    val jvmVersion: String = System.getProperty("java.version")
)