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
import net.rwhps.server.util.alone.annotations.GameSimulationLayer

interface AbstractGameNet {
    @GameSimulationLayer.GameSimulationLayer_KeyWords("lastNetworkPlayerName")
    fun newConnect(
        ip: String = "127.0.0.1:${Data.config.Port}",
        name: String = Data.headlessName
    )

    /**
     * 使用 Hess 启动一个新的服务器 （通过NIO)
     * @param port Int
     * @param name String
     */
    @GameSimulationLayer.GameSimulationLayer_KeyWords("networking already started")
    fun startHessPort(port: Int = 5123, passwd: String? = null, name: String = Data.headlessName)
}