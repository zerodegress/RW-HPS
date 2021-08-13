/*
 * Copyright 2020-2021 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.net.netconnectprotocol

import com.github.dr.rwserver.data.Player
import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.game.GameCommand
import com.github.dr.rwserver.game.GameMaps
import com.github.dr.rwserver.io.GameInputStream
import com.github.dr.rwserver.io.GameOutputStream
import com.github.dr.rwserver.io.Packet
import com.github.dr.rwserver.net.core.AbstractNetPacket
import com.github.dr.rwserver.struct.Seq
import com.github.dr.rwserver.util.IsUtil
import com.github.dr.rwserver.util.PacketType
import com.github.dr.rwserver.util.encryption.Game
import com.github.dr.rwserver.util.encryption.Sha
import com.github.dr.rwserver.util.log.Log.error
import com.github.dr.rwserver.util.zip.gzip.GzipEncoder
import java.io.DataOutputStream
import java.io.IOException
import java.math.BigInteger

/**
 * @author Dr
 */
class GameVersionPacket : AbstractNetPacket {
    @Throws(IOException::class)
    override fun getSystemMessagePacket(msg: String): Packet {
        return getChatMessagePacket(msg, "SERVER", 5)
    }

    @Throws(IOException::class)
    override fun getChatMessagePacket(msg: String, sendBy: String, team: Int): Packet {
        val o = GameOutputStream()
        o.writeString(msg)
        o.writeByte(3)
        o.writeBoolean(true)
        o.writeString(sendBy)
        o.writeInt(team)
        o.writeInt(team)
        return o.createPacket(PacketType.PACKET_SEND_CHAT)
    }

    @Throws(IOException::class)
    override fun getPingPacket(player: Player): Packet {
        player.timeTemp = System.currentTimeMillis()
        val o = GameOutputStream()
        o.writeLong(1000L)
        o.writeByte(0)
        return o.createPacket(PacketType.PACKET_HEART_BEAT)
    }

    @Throws(IOException::class)
    override fun getTickPacket(tick: Int): Packet {
        val o = GameOutputStream()
        o.writeInt(tick)
        o.writeInt(0)
        return o.createPacket(PacketType.PACKET_TICK)
    }

    @Throws(IOException::class)
    override fun getGameTickCommandPacket(tick: Int, cmd: GameCommand): Packet {
        val o = GameOutputStream()
        o.writeInt(tick)
        o.writeInt(1)
        val enc = GzipEncoder.getGzipStream("c", false)
        enc.stream.write(cmd.arr)
        o.flushEncodeData(enc)
        return o.createPacket(10)
    }

    @Throws(IOException::class)
    override fun getGameTickCommandsPacket(tick: Int, cmd: Seq<GameCommand>): Packet {
        val o = GameOutputStream()
        o.writeInt(tick)
        o.writeInt(cmd.size())
        for (c in cmd) {
            val enc = GzipEncoder.getGzipStream("c", false)
            enc.stream.write(c.arr)
            o.flushEncodeData(enc)
        }
        return o.createPacket(10)
    }

    @Throws(IOException::class)
    override fun getTeamDataPacket(): GzipEncoder {
        val enc = GzipEncoder.getGzipStream("teams", true)
        for (i in 0 until Data.game.maxPlayer) {
            try {
                val player = Data.game.playerData[i]
                if (player == null) {
                    enc.stream.writeBoolean(false)
                } else {
                    enc.stream.writeBoolean(true)
                    enc.stream.writeInt(0)
                    writePlayer(player, enc.stream)
                }
            } catch (e: Exception) {
                error("[ALL/Player] Get Server Team Info", e)
            }
        }
        return enc
    }

    @Throws(IOException::class)
    override fun convertGameSaveDataPacket(packet: Packet): Packet {
        GameInputStream(packet).use { stream ->
            val o = GameOutputStream()
            o.writeByte(stream.readByte())
            o.writeInt(stream.readInt())
            o.writeInt(stream.readInt())
            o.writeFloat(stream.readFloat())
            o.writeFloat(stream.readFloat())
            o.writeBoolean(false)
            o.writeBoolean(false)
            stream.readBoolean()
            stream.readBoolean()
            stream.readString()
            val bytes = stream.readStreamBytes()
            o.writeString("gameSave")
            o.flushMapData(bytes.size, bytes)
            return o.createPacket(35)
        }
    }

    // 0->本地 1->自定义 2->保存的游戏
    @Throws(IOException::class)
    override fun getStartGamePacket(): Packet {
        val o = GameOutputStream()
        o.writeByte(0)
        // 0->本地 1->自定义 2->保存的游戏
        o.writeInt(Data.game.maps.mapType.ordinal)
        if (Data.game.maps.mapType == GameMaps.MapType.defaultMap) {
            o.writeString("maps/skirmish/" + Data.game.maps.mapPlayer + Data.game.maps.mapName + ".tmx")
        } else {
            o.flushMapData(Data.game.maps.mapData!!.mapSize, Data.game.maps.mapData!!.bytesMap!!)
            o.writeString("SAVE:" + Data.game.maps.mapName + ".tmx")
        }
        o.writeBoolean(false)
        return o.createPacket(PacketType.PACKET_START_GAME)
    }

    @Throws(IOException::class)
    override fun getPacketMapName(bytes: ByteArray): String {
        GameInputStream(bytes).use { stream ->
            stream.readString()
            stream.readInt()
            stream.readInt()
            return stream.readString()
        }
    }

    @Throws(IOException::class)
    override fun getExitPacket(): Packet {
        val o = GameOutputStream()
        o.writeString("exited")
        return o.createPackets(111)
    }

    @Throws(IOException::class)
    override fun writePlayer(player: Player, stream: DataOutputStream) {
        if (Data.game.isStartGame) {
            stream.writeByte(player.site)
            stream.writeInt(player.ping)
            stream.writeBoolean(Data.game.sharedControl)
            stream.writeBoolean(player.sharedControl)
            return
        }
        stream.writeByte(player.site)
        stream.writeInt(Data.game.credits)
        stream.writeInt(player.team)
        stream.writeBoolean(true)
        stream.writeUTF(player.name)
        stream.writeBoolean(false)

        /* -1 N/A  -2 -   -99 HOST */
        stream.writeInt(player.ping)
        stream.writeLong(System.currentTimeMillis())
        /* MS */
        stream.writeBoolean(false)
        stream.writeInt(0)
        stream.writeInt(player.site)
        stream.writeByte(0)
        /* 共享控制 */
        stream.writeBoolean(Data.game.sharedControl)
        /* 是否掉线 */
        stream.writeBoolean(player.sharedControl)
        /* 是否投降 */
        stream.writeBoolean(false)
        stream.writeBoolean(false)
        stream.writeInt(-9999)
        stream.writeBoolean(false)
        // 延迟后显示 （HOST)
        stream.writeInt(if (player.isAdmin) 1 else 0)
    }

    @Throws(IOException::class)
    override fun getPlayerConnectPacket(): Packet {
        val out = GameOutputStream()
        out.writeString("com.corrodinggames.rwhps.forward")
        out.writeInt(1)
        out.writeInt(151)
        out.writeInt(151)
        return out.createPacket(PacketType.PACKET_PREREGISTER_CONNECTION)
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
            out.writeString(BigInteger(1, Sha().sha256Array(passwd!!)).toString(16).uppercase())
        }

        out.writeString("com.corrodinggames.rts.java")
        out.writeString(uuid)
        out.writeInt(1198432602)
        out.writeString(Game.connectKey(key))
        return out.createPacket(PacketType.PACKET_PLAYER_INFO)
    }
}