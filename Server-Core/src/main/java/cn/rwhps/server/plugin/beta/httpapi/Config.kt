package cn.rwhps.server.plugin.beta.httpapi

data class Config(
    val enabled: Boolean = true,
    val path: String = "/plugin/httpApi",
    val token: String = "defaultToken"
)