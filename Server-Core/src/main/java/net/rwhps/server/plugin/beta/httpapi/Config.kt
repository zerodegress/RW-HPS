package net.rwhps.server.plugin.beta.httpapi

data class Config(
    val enabled: Boolean = false,
    val path: String = "/plugin/httpApi",
    val token: String = "defaultToken",
)