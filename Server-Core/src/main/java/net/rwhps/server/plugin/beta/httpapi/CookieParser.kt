package net.rwhps.server.plugin.beta.httpapi

object CookieParser {
    fun String.toCookie(): MutableMap<String, String> {
        val cookies: MutableMap<String, String> = mutableMapOf()
        val split = this.split(";")
        for (cookie in split) {
            val split2 = cookie.split("=")
            cookies[split2[0]] = split2[1]
        }
        return cookies
    }
}