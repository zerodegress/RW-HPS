package net.rwhps.server.plugin.beta.httpapi.responses

data class PluginsResp(
    val name: String,
    val desc: String,
    val author: String,
    val version: String
)