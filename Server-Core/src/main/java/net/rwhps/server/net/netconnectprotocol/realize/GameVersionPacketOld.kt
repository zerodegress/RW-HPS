/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.net.netconnectprotocol.realize

import net.rwhps.server.data.global.Data
import net.rwhps.server.data.player.Player
import net.rwhps.server.io.GameOutputStream
import net.rwhps.server.io.packet.Packet
import net.rwhps.server.util.IsUtil
import net.rwhps.server.util.PacketType
import net.rwhps.server.util.Time
import net.rwhps.server.util.encryption.Game
import net.rwhps.server.util.encryption.Sha
import java.io.IOException
import java.math.BigInteger

/**
 * Provides support for most common packages for the server
 *
 * @author RW-HPS/Dr
 */
class GameVersionPacketOld : GameVersionPacket() {
    @Throws(IOException::class)
    override fun writePlayer(player: Player, stream: GameOutputStream, startGame: Boolean) {
        with (stream) {
            if (startGame) {
                writeByte(player.site)
                writeInt(player.ping)
                // 玩家是否可控
                writeBoolean(player.controlThePlayer)
                writeBoolean(player.sharedControl)
                return
            }
            writeByte(player.site)
            writeInt(Data.game.credits)
            writeInt(player.team)
            writeIsString(player.name)
            writeBoolean(false)

            /* -1 N/A ; -2 -  ; -99 HOST */
            writeInt(if (player.con != null) player.ping else if (Time.concurrentSecond()-player.lastMoveTime > 120) -1 else -2)
            writeLong(System.currentTimeMillis())
            /* MS */
            writeBoolean(false)
            writeInt(0)

            writeInt(player.site)
            writeByte(0)

            /* 共享控制 */
            writeBoolean(Data.game.sharedControl)
            /* 是否掉线 */
            writeBoolean(player.sharedControl)
            /* 是否投降 */
            writeBoolean(false)
            writeBoolean(false)
            writeInt(-9999)

            writeBoolean(false)
            // 延迟后显示 （HOST)
            writeInt(if (player.isAdmin) 1 else 0)
        }
    }

    @Throws(IOException::class)
    override fun getPlayerConnectPacket(): Packet {
        val out = GameOutputStream()
        out.writeString("com.corrodinggames.rwhps.forward")
        out.writeInt(1)
        out.writeInt(151)
        out.writeInt(151)
        return out.createPacket(PacketType.PREREGISTER_INFO_RECEIVE)
    }

    @Throws(IOException::class)
    override fun getPlayerRegisterPacket(name: String, uuid: String, passwd: String?, key: Int): Packet {
        val out = GameOutputStream()
        out.writeString("com.corrodinggames.rts")
        out.writeInt(4)
        out.writeInt(151)
        out.writeInt(151)
        out.writeString(name)

        if (IsUtil.isBlank(passwd)) {
            out.writeBoolean(false)
        } else {
            out.writeBoolean(true)
            out.writeString(BigInteger(1, Sha.sha256Array(passwd!!)).toString(16).uppercase())
        }

        out.writeString("com.corrodinggames.rts.java")
        out.writeString(uuid)
        out.writeInt(1198432602)
        out.writeString(Game.connectKey(key))
        return out.createPacket(PacketType.REGISTER_PLAYER)
    }
}