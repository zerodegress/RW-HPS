/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.game.simulation.gameFramework.net

import net.rwhps.server.data.global.Data
import net.rwhps.server.io.packet.Packet
import net.rwhps.server.net.core.ConnectionAgreement
import net.rwhps.server.net.core.TypeConnect
import net.rwhps.server.net.core.server.AbstractNetConnect
import net.rwhps.server.util.PacketType
import net.rwhps.server.util.log.exp.ImplementedException

/**
 * Parse the [net.rwhps.server.net.core.IRwHps.NetType.ServerProtocol] protocol
 * @property con                GameVersionRelay
 * @property conClass           Initialize
 * @property abstractNetConnect AbstractNetConnect
 * @property version            Parser version
 * @author RW-HPS/Dr
 */
open class TypeRwHps(private val con: GameVersionServer) : TypeConnect {

    override val abstractNetConnect: AbstractNetConnect
        get() = con

    override fun getTypeConnect(connectionAgreement: ConnectionAgreement): TypeConnect {
        throw ImplementedException.PlayerImplementedException("[TypeRwHps] Should not be enforced")
    }

    @Throws(Exception::class)
    override fun typeConnect(packet: Packet) {
        try {
            when (packet.type) {
                PacketType.CHAT_RECEIVE -> {
                    con.receiveChat(packet)
                }

                else -> {}
            }
            con.recivePacket(packet)
        } catch (_: Exception) {
        }
    }

    override val version: String
        get() = "${Data.SERVER_CORE_VERSION}: 1.0.0"
}