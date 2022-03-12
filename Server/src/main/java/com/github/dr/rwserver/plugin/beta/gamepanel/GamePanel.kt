/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.plugin.beta.gamepanel

import com.github.dr.rwserver.net.http.WebData
import com.github.dr.rwserver.plugin.Plugin
import com.github.dr.rwserver.plugin.beta.gamepanel.http.Panel
import com.github.dr.rwserver.plugin.beta.gamepanel.http.PanelWebSocket
import com.github.dr.rwserver.plugin.beta.gamepanel.http.TestWeb

/**
 * 实现一个WEB版的服务器控制台
 * @author HuiAnxiaoxing
 * @author Dr
 */
class GamePanel : Plugin() {
    override fun init() {
        WebData.addWebGetInstance("/GamePanel/Test",TestWeb())
        WebData.addWebGetInstance("/GamePanel/*", Panel())

        WebData.addWebSocketInstance("/WebSocket/GamePanel", PanelWebSocket())
    }
}