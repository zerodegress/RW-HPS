/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package cn.rwhps.server.plugin.beta.upstatistics.data

import cn.rwhps.server.data.global.Data
import cn.rwhps.server.data.global.NetStaticData
import cn.rwhps.server.data.global.Relay
import cn.rwhps.server.io.GameInputStream
import cn.rwhps.server.io.GameOutputStream
import cn.rwhps.server.net.StartNet
import cn.rwhps.server.net.netconnectprotocol.realize.GameVersionServer
import cn.rwhps.server.struct.SerializerTypeAll
import cn.rwhps.server.util.Time
import cn.rwhps.server.util.inline.toGson
import cn.rwhps.server.util.inline.toJson
import java.io.IOException
import java.util.concurrent.atomic.AtomicInteger

data class BaseDataSend(
    val SendTime: Int                             = Time.concurrentSecond(),
    val ServerRunPort: Int                        = Data.config.Port,
    val ServerNetType: String                     = NetStaticData.ServerNetType.name,
    val System: String                            = Data.core.osName,
    val JavaVersion: String                       = Data.core.javaVersion,
    val IsServerRun: Boolean                      = true,
    val IsServer: Boolean,
    val ServerData: ServerData? = null,
    val RelayData: RelayData? = null,
    ) {
    companion object {
        data class ServerData(
            val PlayerSize: Int                     = AtomicInteger().also { NetStaticData.startNet.each { e: StartNet -> it.addAndGet(e.getConnectSize()) } }.get(),
            val MaxPlayer: Int                      = Data.config.MaxPlayer,
            val PlayerVersion: Int                  = (NetStaticData.RwHps.typeConnect.abstractNetConnect as GameVersionServer).supportedVersionInt,
            val IpPlayerCountry: Map<String,Int>,
        )

        data class RelayData(
            val PlayerSize: Int                     = AtomicInteger().also { NetStaticData.startNet.each { e: StartNet -> it.addAndGet(e.getConnectSize()) } }.get(),
            val RoomAllSize: Int                    = Relay.roomAllSize,
            val RoomNoStartSize: Int                = Relay.roomNoStartSize,
            val RoomPublicListSize: Int             = Relay.roomPublicSize,
            val PlayerVersion: Map<Int,Int>         = Relay.getAllRelayVersion(),
            val IpPlayerCountry: Map<String,Int>    = Relay.getAllRelayIpCountry()
        )

        internal val serializer = object : SerializerTypeAll.TypeSerializer<BaseDataSend> {
            @Throws(IOException::class)
            override fun write(stream: GameOutputStream, objectData: BaseDataSend) {
                stream.writeString(objectData.toJson())
            }

            @Throws(IOException::class)
            override fun read(stream: GameInputStream): BaseDataSend {
                return BaseDataSend::class.java.toGson(stream.readString())
            }
        }
    }
}