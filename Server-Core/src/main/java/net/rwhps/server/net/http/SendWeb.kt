/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.net.http

import io.netty.channel.Channel
import io.netty.handler.codec.http.DefaultFullHttpResponse
import io.netty.handler.codec.http.HttpHeaderNames
import io.netty.handler.codec.http.HttpRequest
import io.netty.handler.codec.http.HttpResponseStatus
import net.rwhps.server.data.global.Data

/**
 * @author RW-HPS/Dr
 */
class SendWeb(
    private val channel: Channel,
    private val request: HttpRequest
) {
    private var cacheData: ByteArray? = null
    var status: HttpResponseStatus = HttpResponseStatus.OK
    /** 覆盖原header */
    private val replaceHeaders: MutableMap<String, String> = mutableMapOf(
        /* 默认头 使用 RW-HPS 自定义 */
        HttpHeaderNames.SERVER.toString() to "RW-HPS/${Data.SERVER_CORE_VERSION} (WebData)",
        HttpHeaderNames.CONTENT_TYPE.toString() to "charset=utf-8",
        HttpHeaderNames.ACCEPT_CHARSET.toString() to "UTF-8"
    )
    /** 附加的header,用于需要重复的header */
    private val appendHeaders: MutableMap<String, ArrayList<String>> = mutableMapOf()

    fun setData(bytes: ByteArray) {
        cacheData = bytes
    }
    fun setData(string: String) {
        cacheData = string.toByteArray(Data.UTF_8)
    }

    fun addCookie(cKey: String, cValue: String, maxAge: Int) {
        if (!appendHeaders.containsKey(HttpHeaderNames.SET_COOKIE.toString())) {
            appendHeaders[HttpHeaderNames.SET_COOKIE.toString()] = arrayListOf()
        }
        appendHeaders[HttpHeaderNames.SET_COOKIE.toString()]!!.add("$cKey=$cValue; Max-Age=$maxAge; Path=/")
    }

    fun addHead(key: String, value: String) {
        if (!appendHeaders.containsKey(key)) {
            appendHeaders[key] = arrayListOf()
        }
        appendHeaders[key]!!.add(value)
    }

    fun setHead(key: String, value: String) {
        replaceHeaders[key] = value
    }

    fun send404() {
        status = HttpResponseStatus.NOT_FOUND
        cacheData = """
        <p>[404] File not found
            <br>The project is based on 
                <strong>RW-HPS</strong>
                <br>
                    <a href="https://github.com/RW-HPS/RW-HPS">RW-HPS Github</a>
                </p>
        """.trimIndent().toByteArray(Data.UTF_8)
        send()
    }

    fun send() {
        if (cacheData == null) {
            throw NullPointerException()
        }
        val defaultFullHttpResponse = DefaultFullHttpResponse(request.protocolVersion(), status)
        for (header in replaceHeaders) {
            defaultFullHttpResponse.headers().set(header.key, header.value)
        }
        for (header in appendHeaders) {
            for (value in header.value) {
                defaultFullHttpResponse.headers().add(header.key, header.value)
            }
        }

        defaultFullHttpResponse.headers().setInt(HttpHeaderNames.CONTENT_LENGTH, cacheData!!.size)
        defaultFullHttpResponse.content().writeBytes(cacheData)

        channel.writeAndFlush(defaultFullHttpResponse)

        channel.close()
    }

}