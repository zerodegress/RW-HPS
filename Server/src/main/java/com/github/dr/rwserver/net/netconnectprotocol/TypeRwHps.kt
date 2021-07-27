package com.github.dr.rwserver.net.netconnectprotocol

import com.github.dr.rwserver.net.core.TypeConnect
import kotlin.Throws
import com.github.dr.rwserver.net.core.AbstractNetConnect
import com.github.dr.rwserver.util.PacketType
import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.io.Packet
import com.github.dr.rwserver.util.Time.millis
import java.lang.Exception

class TypeRwHps : TypeConnect {
    @Throws(Exception::class)
    override fun typeConnect(con: AbstractNetConnect, packet: Packet) {
        con.setLastReceivedTime()
        if (packet.type == PacketType.PACKET_ADD_GAMECOMMAND) {
            con.receiveCommand(packet)
            con.player!!.lastMoveTime = millis()
        } else {
            when (packet.type) {
                PacketType.PACKET_PREREGISTER_CONNECTION -> con.registerConnection(packet)
                PacketType.PACKET_PLAYER_INFO -> if (!con.getPlayerInfo(packet)) {
                    con.disconnect()
                }
                PacketType.PACKET_HEART_BEAT_RESPONSE -> {
                    val player = con.player
                    player!!.ping = (System.currentTimeMillis() - player.timeTemp).toInt() shr 1
                }
                PacketType.PACKET_ADD_CHAT -> con.receiveChat(packet)
                PacketType.PACKET_DISCONNECT -> con.disconnect()
                PacketType.PACKET_ACCEPT_START_GAME -> con.player!!.start = true
                PacketType.PACKET_SERVER_DEBUG -> con.debug(packet)
                PacketType.PACKET_SYNC -> Data.game.gameSaveCache = packet
                else -> {
                }
            }
        }
    }

    override val version: String
        get() = "2.0.0"
}