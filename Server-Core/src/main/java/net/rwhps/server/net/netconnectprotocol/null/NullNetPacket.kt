/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.net.netconnectprotocol.`null`

import net.rwhps.server.data.player.Player
import net.rwhps.server.io.GameOutputStream
import net.rwhps.server.io.output.CompressOutputStream
import net.rwhps.server.io.packet.GameCommandPacket
import net.rwhps.server.io.packet.Packet
import net.rwhps.server.net.core.AbstractNetPacket
import net.rwhps.server.struct.Seq
import net.rwhps.server.util.log.exp.ImplementedException

/**
 * Not implemented
 * @author RW-HPS/Dr
 */
class NullNetPacket : AbstractNetPacket {
    override fun getSystemMessagePacket(msg: String): Packet {
        throw ImplementedException("Not yet implemented")
    }

    override fun getChatMessagePacket(msg: String, sendBy: String, team: Int): Packet {
        throw ImplementedException("Not yet implemented")
    }

    override fun getPingPacket(player: Player): Packet {
        throw ImplementedException("Not yet implemented")
    }

    override fun getTickPacket(tick: Int): Packet {
        throw ImplementedException("Not yet implemented")
    }

    override fun getGameTickCommandPacket(tick: Int, cmd: GameCommandPacket): Packet {
        throw ImplementedException("Not yet implemented")
    }

    override fun getGameTickCommandsPacket(tick: Int, cmd: Seq<GameCommandPacket>): Packet {
        throw ImplementedException("Not yet implemented")
    }

    override fun getTeamDataPacket(startGame: Boolean): CompressOutputStream {
        throw ImplementedException("Not yet implemented")
    }

    override fun convertGameSaveDataPacket(packet: Packet): Packet {
        throw ImplementedException("Not yet implemented")
    }

    override fun getStartGamePacket(): Packet {
        throw ImplementedException("Not yet implemented")
    }

    override fun getPacketMapName(bytes: ByteArray): String {
        throw ImplementedException("Not yet implemented")
    }

    override fun getDeceiveGameSave(): Packet {
        throw ImplementedException("Not yet implemented")
    }

    override fun gameSummonPacket(index: Int, unit: String, x: Float, y: Float, size: Int): GameCommandPacket {
        throw ImplementedException("Not yet implemented")
    }

    override fun getExitPacket(): Packet {
        throw ImplementedException("Not yet implemented")
    }

    override fun writePlayer(player: Player, stream: GameOutputStream, startGame: Boolean) {
        throw ImplementedException("Not yet implemented")
    }

    override fun getPlayerConnectPacket(): Packet {
        throw ImplementedException("Not yet implemented")
    }

    override fun getPlayerRegisterPacket(name: String, uuid: String, passwd: String?, key: Int): Packet {
        throw ImplementedException("Not yet implemented")
    }
}