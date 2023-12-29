/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.game.simulation.core

import net.rwhps.server.data.global.Data
import net.rwhps.server.util.annotations.mark.GameSimulationLayer

interface AbstractLinkGameNet {
    /**
     * 加入一个新服务器
     *
     * @param ip IP
     * @param name 需要使用的名字
     */
    @GameSimulationLayer.GameSimulationLayer_KeyWords("lastNetworkPlayerName")
    fun newConnect(
        ip: String = "127.0.0.1:${Data.config.port}", name: String = Data.headlessName
    )

    /**
     * 使用 Hess 启动一个新的服务器 （通过NIO)
     * @param port 端口
     * @param passwd 密码
     * @param name 需要使用的名字
     */
    @GameSimulationLayer.GameSimulationLayer_KeyWords("networking already started")
    fun startHessPort(port: Int = 5123, passwd: String? = null, name: String = Data.headlessName)
}