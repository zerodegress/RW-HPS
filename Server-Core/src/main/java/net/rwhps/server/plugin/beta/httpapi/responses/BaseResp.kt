package net.rwhps.server.plugin.beta.httpapi.responses

data class BaseResp(
    val code: Int = 200,
    val reason: String? = null,
    val data: Any? = null
)