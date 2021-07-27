package com.github.dr.rwserver.util

import com.github.dr.rwserver.io.Packet

/**
 * @author Miku
 * @author Dr
 */
object PacketType {
    const val PACKET_SERVER_DEBUG = 2000

    //Server Commands
    const val PACKET_REGISTER_CONNECTION = 161
    const val PACKET_TEAM_LIST = 115
    const val PACKET_HEART_BEAT = 108
    const val PACKET_SEND_CHAT = 141
    const val PACKET_SERVER_INFO = 106
    const val PACKET_KICK = 150
    const val PACKET_SYNCCHECKSUM_STATUS = 31
    const val PACKET_A = 30

    //Client Commands
    const val PACKET_PREREGISTER_CONNECTION = 160
    const val PACKET_HEART_BEAT_RESPONSE = 109
    const val PACKET_ADD_CHAT = 140
    const val PACKET_PLAYER_INFO = 110
    const val PACKET_DISCONNECT = 111
    const val PACKET_ACCEPT_START_GAME = 112
    const val PACKET_ACCEPT_BUTTON_GAME = 20

    //Game Commands
    const val PACKET_ADD_GAMECOMMAND = 20
    const val PACKET_TICK = 10
    const val PACKET_SYNC = 35
    const val PACKET_START_GAME = 120
    const val PACKET_PASSWD_ERROR = 113
    val PACKET_RESULT_EART_BEAT = Packet(PACKET_HEART_BEAT, ByteArray(0))
}