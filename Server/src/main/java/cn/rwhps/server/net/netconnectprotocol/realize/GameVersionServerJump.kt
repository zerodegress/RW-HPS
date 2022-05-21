/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.net.netconnectprotocol.realize

import cn.rwhps.server.net.core.ConnectionAgreement
import cn.rwhps.server.net.netconnectprotocol.internal.relay.fromRelayJumpsToAnotherServer
import cn.rwhps.server.net.netconnectprotocol.internal.relay.relayServerInitInfo
import cn.rwhps.server.util.log.Log

/**
 * 在Server协议进行扩展 加入更多可用参数
 * @author RW-HPS/Dr
 */
class GameVersionServerJump(connectionAgreement: ConnectionAgreement) : GameVersionServer(connectionAgreement) {
    fun sendRelayServerInfo() {
        try {
            sendPacket(relayServerInitInfo())
        } catch (e: Exception) {
            Log.error(e)
        }
    }

    fun jumpNewServer(ip: String) {
        try {
            sendPacket(fromRelayJumpsToAnotherServer(ip))
        } catch (e: Exception) {
            Log.error(e)
        }
    }
}