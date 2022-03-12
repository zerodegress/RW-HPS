/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.plugin.beta.gamepanel.http

import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.net.http.SendWeb
import com.github.dr.rwserver.net.http.WebGet

class TestWeb : WebGet() {
    override fun get(getUrl: String, data: String, send: SendWeb) {
        send.setData(
            """
            <html lang="zh">
            <head>
            <meta charset="utf-8">
            
            你好 这是 [RW-HPS] GamePanel 的 测试路径  ${Data.LINE_SEPARATOR}
            Hi this is the test path for [RW-HPS] GamePanel  
            """.trimIndent()
        )
        send.send()
    }
}