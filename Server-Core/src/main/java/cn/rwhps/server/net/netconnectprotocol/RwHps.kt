/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.net.netconnectprotocol

import cn.rwhps.server.core.ServiceLoader
import cn.rwhps.server.core.ServiceLoader.ServiceType
import cn.rwhps.server.data.player.Player
import cn.rwhps.server.io.GameOutputStream
import cn.rwhps.server.io.output.CompressOutputStream
import cn.rwhps.server.io.packet.GameCommandPacket
import cn.rwhps.server.io.packet.Packet
import cn.rwhps.server.net.core.AbstractNetPacket
import cn.rwhps.server.net.core.ConnectionAgreement
import cn.rwhps.server.net.core.IRwHps
import cn.rwhps.server.net.core.TypeConnect
import cn.rwhps.server.net.core.server.AbstractNetConnect
import cn.rwhps.server.struct.Seq
import cn.rwhps.server.util.log.Log
import cn.rwhps.server.util.log.exp.ImplementedException

/**
 * 核心协议实现
 * @property netType NetType                        : 使用的Net协议类型
 * @property typeConnect TypeConnect                : 连接解析器
 * @property abstractNetPacket AbstractNetPacket    : NetPacket
 * @author RW-HPS/Dr
 */
class RwHps(private val netType: IRwHps.NetType) : IRwHps {
    override val typeConnect: TypeConnect =
        try {
            val protocolClass = ServiceLoader.getServiceClass(ServiceType.Protocol,netType.name)
            ServiceLoader.getService(ServiceType.ProtocolType,netType.name,Class::class.java).newInstance(protocolClass) as TypeConnect
        } catch (e: Exception) {
            Log.fatal(e)
            object : TypeConnect {
                override fun getTypeConnect(connectionAgreement: ConnectionAgreement): TypeConnect { TODO("Not yet implemented") }
                override fun typeConnect(packet: Packet) { TODO("Not yet implemented") }
                override val abstractNetConnect: AbstractNetConnect get() = TODO("Not yet implemented")
                override val version: String get() = TODO("Not yet implemented")
            }
        }

    override val abstractNetPacket: AbstractNetPacket =
        try {
            try {
                ServiceLoader.getService(ServiceType.ProtocolPacket, netType.name)
            } catch (e: ImplementedException) {
                ServiceLoader.getService(ServiceType.ProtocolPacket, IRwHps.NetType.ServerProtocol.name)
            }.newInstance() as AbstractNetPacket
        } catch (e: Exception) {
            Log.fatal(e)
            object : AbstractNetPacket {
                override fun getSystemMessagePacket(msg: String): Packet { TODO("Not yet implemented") }
                override fun getChatMessagePacket(msg: String, sendBy: String, team: Int): Packet { TODO("Not yet implemented") }
                override fun getPingPacket(player: Player): Packet { TODO("Not yet implemented") }
                override fun getTickPacket(tick: Int): Packet { TODO("Not yet implemented") }
                override fun getGameTickCommandPacket(tick: Int, cmd: GameCommandPacket): Packet { TODO("Not yet implemented") }
                override fun getGameTickCommandsPacket(tick: Int, cmd: Seq<GameCommandPacket>): Packet { TODO("Not yet implemented") }
                override fun getTeamDataPacket(startGame: Boolean): CompressOutputStream { TODO("Not yet implemented") }
                override fun convertGameSaveDataPacket(packet: Packet): Packet { TODO("Not yet implemented") }
                override fun getStartGamePacket(): Packet { TODO("Not yet implemented") }
                override fun getPacketMapName(bytes: ByteArray): String { TODO("Not yet implemented") }
                override fun getDeceiveGameSave(): Packet { TODO("Not yet implemented") }
                override fun gameSummonPacket(index: Int, unit: String, x: Float, y: Float, size: Int, ): GameCommandPacket { TODO("Not yet implemented") }
                override fun getExitPacket(): Packet { TODO("Not yet implemented") }
                override fun writePlayer(player: Player, stream: GameOutputStream, startGame: Boolean) { TODO("Not yet implemented") }
                override fun getPlayerConnectPacket(): Packet { TODO("Not yet implemented") }
                override fun getPlayerRegisterPacket(name: String, uuid: String, passwd: String?, key: Int): Packet { TODO("Not yet implemented") }
            }
        }

}