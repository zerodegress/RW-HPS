/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.plugin.beta.gamepanel.http

import com.github.dr.rwserver.net.http.SendWeb
import com.github.dr.rwserver.net.http.WebGet
import com.github.dr.rwserver.util.io.IoRead
import com.github.dr.rwserver.util.log.Log
import io.netty.handler.codec.http.multipart.DefaultHttpDataFactory
import java.net.JarURLConnection
import java.net.URL
import java.util.*
import java.util.jar.JarEntry

class Panel : WebGet() {
    companion object {
        private val factory = DefaultHttpDataFactory(DefaultHttpDataFactory.MINSIZE)

        val resData: MutableMap<String, ByteArray> = HashMap()

        init {
            val res: MutableMap<String, ByteArray> = HashMap()
            val res1: URL = this::class.java.getResource("/pluginDataRes/panel")!!
            val prefix: String = res1.file.split("!/")[1]
            (res1.openConnection() as JarURLConnection).jarFile.use {
                val entries: Enumeration<JarEntry> = it.entries()
                while (entries.hasMoreElements()) {
                    val jarEntry = entries.nextElement()
                    val name = jarEntry.name
                    if (jarEntry.isDirectory || name.endsWith(".class") || !name.startsWith(prefix)) {
                        continue
                    }
                    res[name.replace(prefix, "")] = IoRead.readInputStreamBytes(this::class.java.classLoader.getResource(name)!!.openStream())
                }
                if (res.containsKey("/index.html")) {
                    res["/"] = res["/index.html"]!!
                }
            }

            res.forEach {
                resData["/GamePanel${it.key}"] = it.value
                Log.clog("GamePanel${it.key}")
            }
        }
    }

    override fun get(getUrl: String, data: String, send: SendWeb) {
        send.setData(resData[getUrl]!!)
        send.send()
    }
}