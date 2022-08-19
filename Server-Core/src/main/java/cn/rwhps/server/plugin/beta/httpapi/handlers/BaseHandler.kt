package cn.rwhps.server.plugin.beta.httpapi.handlers

import cn.rwhps.server.plugin.beta.httpapi.ConfigHelper.config
import cn.rwhps.server.plugin.beta.httpapi.responses.BaseResp
import com.google.gson.Gson
import com.sun.net.httpserver.HttpExchange
import com.sun.net.httpserver.HttpHandler
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStream

open class BaseHandler : HttpHandler {
    lateinit var os: OutputStream
    var param: MutableMap<String, String> = mutableMapOf()
    val gson = Gson()

    override fun handle(exchange: HttpExchange) {
        val split = getRequestParam(exchange).split("&")
        os = exchange.responseBody
        for (str in split) {
            val split2 = str.split("=")
            param[split2[0]] = split2[1]
        }
        if (param["token"] != config.token) {
            exchange.sendResponseHeaders(403, 0)
            os.write(gson.toJson(BaseResp(code = 403, reason = "invalid token")).toByteArray())
            os.flush()
            os.close()
            return
        }
        exchange.sendResponseHeaders(200, 0)
    }

    fun close() {
        os.flush()
        os.close()
    }

    private fun getRequestParam(exchange: HttpExchange): String {
        val paramStr: String
        if (exchange.requestMethod == "GET") {
            //GET请求读queryString
            paramStr = exchange.requestURI.query
        } else {
            //非GET请求读请求体
            val bufferedReader = BufferedReader(InputStreamReader(exchange.requestBody, "utf-8"))
            val requestBodyContent = StringBuilder()
            var line: String
            while (bufferedReader.readLine().also { line = it } != null) {
                requestBodyContent.append(line)
            }
            paramStr = requestBodyContent.toString()
        }
        return paramStr
    }
}