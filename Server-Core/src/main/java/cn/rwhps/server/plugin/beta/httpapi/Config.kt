package cn.rwhps.server.plugin.beta.httpapi

data class Config(
    val enabled: Boolean = true,
    val listen: String = "127.0.0.1",
    val port: Int = 8080,
    val token: String = "defaultToken"
)