/*
 * Copyright 2020-2021 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.net.netconnectprotocol

import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.data.global.NetStaticData
import com.github.dr.rwserver.io.Packet
import com.github.dr.rwserver.net.core.TypeConnect
import com.github.dr.rwserver.net.core.server.AbstractNetConnect
import com.github.dr.rwserver.util.PacketType

class TypeRelayOpenSource : TypeConnect {
    @Throws(Exception::class)
    override fun typeConnect(con: AbstractNetConnect, packet: Packet) {
        con.setLastReceivedTime()
        when (packet.type) {
            175 -> {
                con.addRelaySend(packet)
            }
            PacketType.PACKET_HEART_BEAT -> {
                con.addGroup(packet)
                con.getPingData(packet)
            }
            else -> {
                when (packet.type) {
                    160 -> {
                        con.setCache(packet)
                        con.sendRelayServerInfo()
                        con.sendRelayServerCheck()
                    }
                    152 ->
                        if (Data.config.SingleUserRelay) {
                            NetStaticData.relayOpenSource.setAddSize()
                            if (NetStaticData.relayOpenSource.admin == null) {
                                con.sendRelayServerId()
                            } else {
                                con.addRelayConnect()
                            }
                        } else {
                            con.relayDirectInspection()
                        }
                    118 -> con.sendRelayServerTypeReply(packet)
                    112 -> {
                        //con.relay!!.isStartGame = true
                        con.addRelayAccept(packet)
                    }
                    PacketType.PACKET_DISCONNECT -> con.disconnect()
                    PacketType.PACKET_SERVER_DEBUG -> con.debug(packet)
                    else -> con.addRelayAccept(packet)
                }
            }
        }
    }

    override val version: String
        get() = "2.0.0"
}