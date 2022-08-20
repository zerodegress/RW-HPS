package cn.rwhps.server.plugin.beta.httpapi

import cn.rwhps.server.util.RandomUtil

data class Config(
    val enabled: Boolean = true,
    val path: String = "/plugin/httpApi",
    val token: String = "defaultToken",
    val salt: String = RandomUtil.getRandomString(32)
)