/*
 * Copyright 2020-2024 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.util

import net.rwhps.server.io.GameOutputStream
import net.rwhps.server.io.packet.Packet
import net.rwhps.server.struct.map.IntMap
import net.rwhps.server.util.inline.ifNullResult
import net.rwhps.server.util.log.exp.VariableException

/**
 * The tag corresponding to the protocol number of the server
 *
 * From Game-Ilb.jar and Rukkit
 *
 * @property typeInt Int
 * @property typeIntBytes 缓存的 bytes , 避免多次 Int to Bytes (邪门优化)
 *
 *
 * @author RukkitDev/Miku
 * @author Dr (dr@der.kim)
 */
enum class PacketType(val typeInt: Int, val typeIntBytes: ByteArray = GameOutputStream.intToBytes(typeInt)) {
    /**
     * CUSTOM PACKET
     *//* DEBUG */
    SERVER_DEBUG_RECEIVE(2000),
    SERVER_DEBUG(2001),

    /* Ex */
    GET_SERVER_INFO_RECEIVE(3000),
    GET_SERVER_INFO(3001),
    UPDATA_CLASS_RECEIVE(3010),
    STATUS_RESULT(3999),

    /**
     * Game Core Packet
     *//* Preregister */
    PREREGISTER_INFO_RECEIVE(160),
    PREREGISTER_INFO(161),
    PASSWD_ERROR(113),
    REGISTER_PLAYER(110),

    /* Server Info */
    SERVER_INFO(106),
    TEAM_LIST(115),

    /* Heart */
    HEART_BEAT(108),
    HEART_BEAT_RESPONSE(109),

    /* Chat */
    CHAT_RECEIVE(140),
    CHAT(141),

    /* Net Status */
    PACKET_DOWNLOAD_PENDING(4),
    KICK(150),
    DISCONNECT(111),

    /* StartGame */
    START_GAME(120),
    ACCEPT_START_GAME(112),
    RETURN_TO_BATTLEROOM(122),

    /* GameStart Commands */
    TICK(10),
    GAMECOMMAND_RECEIVE(20),
    SYNCCHECKSUM_STATUS(31),
    SYNC_CHECK(30),
    SYNC(35),

    /* Relay */
    RELAY_117(117),
    RELAY_118_117_RETURN(118),
    RELAY_POW(151),
    RELAY_POW_RECEIVE(152),

    RELAY_VERSION_INFO(163),
    RELAY_BECOME_SERVER(170),
    FORWARD_CLIENT_ADD(172),
    FORWARD_CLIENT_REMOVE(173),
    PACKET_FORWARD_CLIENT_FROM(174),
    PACKET_FORWARD_CLIENT_TO(175),
    PACKET_FORWARD_CLIENT_TO_REPEATED(176),
    PACKET_RECONNECT_TO(178),


    EMPTYP_ACKAGE(0),
    NOT_RESOLVED(-1);

    companion object {
        private val typeMap: IntMap<PacketType> = IntMap(values().size)

        val nullPacket = Packet(EMPTYP_ACKAGE, byteArrayOf())

        init {
            values().forEach {
                if (typeMap.containsKey(it.typeInt)) {
                    throw VariableException.RepeatAddException("[PacketType]")
                }
                typeMap[it.typeInt] = it
            }
            PacketType.CHAT.ordinal
        }

        fun from(type: Int): PacketType = typeMap[type].ifNullResult(NOT_RESOLVED) { it }
    }
}