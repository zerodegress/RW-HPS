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
import com.github.dr.rwserver.net.game.ConnectionAgreement
import com.github.dr.rwserver.util.PacketType
import com.github.dr.rwserver.util.threads.ServerUploadData

class TypeRelay(abstractNetConnect: AbstractNetConnect) : TypeConnect(abstractNetConnect) {
    val con = abstractNetConnect as GameVersionRelay

    override fun getTypeConnect(connectionAgreement: ConnectionAgreement): TypeConnect {
         return TypeRelay(con.getVersionNet(connectionAgreement))
    }

    @Throws(Exception::class)
    override fun typeConnect(packet: Packet) {
        con.lastReceivedTime()

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
                        con.setCachePacket(packet)
                        con.sendRelayServerInfo()
                        con.sendRelayServerCheck()
                    }
                    152 ->                     // !!!!!!!!
                        if (Data.config.SingleUserRelay) {
                            val port = con.port
                            if (port == 5123) {
                                con.relayDirectInspection()
                            } else {
                                val relayData = ServerUploadData.getRelayDataPort(port)
                                if (relayData != null) {
                                    con.relayDirectInspection(relayData.relay)
                                } else {
                                    con.relayDirectInspection()
                                }
                            }
                        } else {
                            NetStaticData.relay.setAddSize()
                            if (NetStaticData.relay.admin == null) {
                                con.sendRelayServerId()
                            } else {
                                con.addRelayConnect()
                            }
                        }
                    118 -> con.sendRelayServerTypeReply(packet)
                    176 -> {
                    }
                    112 -> {
                        con.relay!!.isStartGame = true
                        con.sendResultPing(packet)
                    }
                    PacketType.PACKET_DISCONNECT -> con.disconnect()
                    PacketType.PACKET_SERVER_DEBUG -> con.debug(packet)
                    else -> con.sendResultPing(packet)
                }
            }
        }
    }

    override val version: String
        get() = "RELAY"
}