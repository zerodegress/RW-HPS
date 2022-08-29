/*
 * Copyright 2020-2022 RW-HPS/Dr.
 *
 * 不授权任何人修改 保留一切权力
 * Do not authorize anyone to modify All rights reserved
 */

package cn.rwhps.server.plugin.beta.upstatistics

import cn.rwhps.server.core.thread.CallTimeTask
import cn.rwhps.server.core.thread.Threads
import cn.rwhps.server.data.global.Data
import cn.rwhps.server.data.global.NetStaticData
import cn.rwhps.server.io.GameOutputStream
import cn.rwhps.server.net.core.IRwHps
import cn.rwhps.server.plugin.Plugin
import cn.rwhps.server.plugin.beta.upstatistics.data.BaseDataSend
import cn.rwhps.server.util.PacketType
import cn.rwhps.server.util.encryption.Aes
import cn.rwhps.server.util.inline.toPrettyPrintingJson
import java.io.DataOutputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.concurrent.TimeUnit

/**
 * 在 RW-HPS 中获取数据
 * 为了数据的准确性 我不建议您对源码进行修改
 * 公开统计
 * Version 1.0.0
 *
 *
 * Acquire data in RW-HPS
 * For the accuracy of the data, I do not recommend that you modify the source code
 * Public statistics
 * Version 1.0.0
 *
 * @author RW-HPS/Dr
 */
class GetServerData : Plugin() {
    override fun onEnable() {
        Threads.newTimedTask(CallTimeTask.ServerUpStatistics,0, 5, TimeUnit.SECONDS) {
            if (NetStaticData.ServerNetType != IRwHps.NetType.NullProtocol) {
                val data = when (NetStaticData.ServerNetType) {
                    IRwHps.NetType.ServerProtocol, IRwHps.NetType.ServerProtocolOld, IRwHps.NetType.ServerTestProtocol -> {
                        BaseDataSend(
                            IsServer = true,
                            ServerData = BaseDataSend.Companion.ServerData(
                                IpPlayerCountry = mutableMapOf<String, Int>().also {
                                    Data.game.playerManage.playerGroup.eachAll {  player ->
                                        val ipCountry = player.con!!.ipCountry
                                        if (it.containsKey(ipCountry)) {
                                            it[ipCountry] = it[ipCountry]!! + 1
                                        } else {
                                            it[ipCountry] = 1
                                        }
                                    }
                                }
                            )
                        )
                    }

                    IRwHps.NetType.RelayProtocol, IRwHps.NetType.RelayMulticastProtocol -> {
                        BaseDataSend(
                            IsServer = false,
                            RelayData = BaseDataSend.Companion.RelayData()
                        )
                    }
                    else -> {
                        BaseDataSend(
                            IsServerRun = false,
                            IsServer = true
                        )
                    }
                }

                try {
                    val out = GameOutputStream()
                    out.writeString("RW-HPS Statistics Data")
                    out.writeString(Data.core.serverConnectUuid)
                    out.writeBytesAndLength(Aes.aesEncryptToBytes(data.toPrettyPrintingJson().toByteArray(Data.UTF_8),"RW-HPS Statistics Data"))
                    val packet = out.createPacket(PacketType.SERVER_DEBUG_RECEIVE)
                    Socket().use {
                        it.connect(InetSocketAddress(InetAddress.getByName("relay.der.kim"), 6001), 3000)
                        DataOutputStream(it.getOutputStream()).use { outputStream ->
                            outputStream.writeInt(packet.bytes.size)
                            outputStream.writeInt(packet.type.typeInt)
                            outputStream.write(packet.bytes)
                            outputStream.flush()
                        }
                        it.close()
                    }
                } catch (_: Exception) {
                }
            }
        }
    }
}