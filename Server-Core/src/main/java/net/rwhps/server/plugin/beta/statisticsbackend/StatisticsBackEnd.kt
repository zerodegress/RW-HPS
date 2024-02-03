/*
 * Copyright 2020-2024 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.plugin.beta.statisticsbackend

import io.netty.handler.codec.http.HttpHeaderNames
import net.rwhps.server.core.Initialization
import net.rwhps.server.core.ServiceLoader
import net.rwhps.server.core.thread.CallTimeTask
import net.rwhps.server.core.thread.Threads
import net.rwhps.server.core.thread.Threads.newThreadCore
import net.rwhps.server.data.global.Data
import net.rwhps.server.data.global.NetStaticData
import net.rwhps.server.func.StrCons
import net.rwhps.server.net.NetService
import net.rwhps.server.net.core.IRwHps
import net.rwhps.server.net.core.web.WebGet
import net.rwhps.server.net.handler.tcp.StartHttp
import net.rwhps.server.net.http.AcceptWeb
import net.rwhps.server.net.http.SendWeb
import net.rwhps.server.plugin.Plugin
import net.rwhps.server.plugin.beta.statisticsbackend.data.BaseData
import net.rwhps.server.plugin.beta.statisticsbackend.net.NetConnectBack
import net.rwhps.server.plugin.beta.statisticsbackend.net.TypeBack
import net.rwhps.server.struct.map.OrderedMap
import net.rwhps.server.util.Time
import net.rwhps.server.util.annotations.mark.PrivateMark
import net.rwhps.server.util.game.command.CommandHandler
import net.rwhps.server.util.inline.toPrettyPrintingJson
import net.rwhps.server.util.log.Log
import java.util.*
import java.util.concurrent.TimeUnit

@PrivateMark
class StatisticsBackEnd: Plugin() {
    override fun onEnable() {
        // Register Custom Protocol
        // 包分流器
        ServiceLoader.addService(ServiceLoader.ServiceType.ProtocolType, IRwHps.NetType.DedicatedToTheBackend.name, TypeBack::class.java)
        // 包解析器
        ServiceLoader.addService(ServiceLoader.ServiceType.Protocol, IRwHps.NetType.DedicatedToTheBackend.name, NetConnectBack::class.java)
    }

    override fun registerCoreCommands(handler: CommandHandler) {
        handler.register("starthd", "serverCommands.start") { _: Array<String>?, log: StrCons ->
            if (NetStaticData.netService.size > 0) {
                log("The server is not closed, please close")
                return@register
            }

            Log.set(Data.config.log.uppercase(Locale.getDefault()))

            NetStaticData.ServerNetType = IRwHps.NetType.DedicatedToTheBackend

            handler.handleMessage("startnetservice true")

            newThreadCore {
                Data.webData.addWebGetInstance("/api/get/dataAll", object: WebGet() {
                    override fun get(accept: AcceptWeb, send: SendWeb) {
                        if (accept.getHeaders(HttpHeaderNames.ORIGIN) != null) {
                            send.setHead(HttpHeaderNames.ACCESS_CONTROL_ALLOW_ORIGIN, accept.getHeaders(HttpHeaderNames.ORIGIN)!!)
                            send.setHead(HttpHeaderNames.ACCESS_CONTROL_ALLOW_CREDENTIALS, "true")
                        }
                        send.setHead(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8")
                        send.setData(statisticalDataSend)
                        send.send()
                    }
                })
                NetService(StartHttp::class.java).openPort(6006)
            }

            Threads.newTimedTask(CallTimeTask.ServerUpStatistics, 0, 1, TimeUnit.MINUTES) {
                val time = Time.concurrentSecond() - 600
                statisticalData.eachAll { t, n ->
                    if (n.data.SendTime < time) {
                        statisticalData.remove(t)
                    }
                }

                val ServerRunPort = mutableMapOf<Int, Int>()
                val ServerNetType = mutableMapOf<String, Int>()
                val SystemCount = mutableMapOf<String, Int>()
                val JavaCount = mutableMapOf<String, Int>()
                val VersionCount = mutableMapOf<String?, Int>()

                var ServerServerSize = 0
                var ServerPlayerSize = 0
                val ServerPlayerVersion = mutableMapOf<Int, Int>()
                val ServerIpCountry = mutableMapOf<String, Int>()
                val ServerIpPlayerCountry = mutableMapOf<String, Int>()

                var RelayServerSize = 0
                var RelayPlayerSize = 0
                var RoomAllSize = 0
                var RoomNoStartSize = 0
                var RoomPublicListSize = 0
                val RelayPlayerVersion = mutableMapOf<Int, Int>()
                val RelayIpCountry = mutableMapOf<String, Int>()
                val RelayIpPlayerCountry = mutableMapOf<String, Int>()

                statisticalData.values.forEach {
                    val data = it.data
                    if (data.IsServerRun) {
                        if (ServerRunPort.containsKey(data.ServerRunPort)) {
                            ServerRunPort[data.ServerRunPort] = ServerRunPort[data.ServerRunPort]!! + 1
                        } else {
                            ServerRunPort[data.ServerRunPort] = 1
                        }

                        if (ServerNetType.containsKey(data.ServerNetType)) {
                            ServerNetType[data.ServerNetType] = ServerNetType[data.ServerNetType]!! + 1
                        } else {
                            ServerNetType[data.ServerNetType] = 1
                        }

                        if (SystemCount.containsKey(data.System)) {
                            SystemCount[data.System] = SystemCount[data.System]!! + 1
                        } else {
                            SystemCount[data.System] = 1
                        }

                        if (JavaCount.containsKey(data.JavaVersion)) {
                            JavaCount[data.JavaVersion] = JavaCount[data.JavaVersion]!! + 1
                        } else {
                            JavaCount[data.JavaVersion] = 1
                        }

                        if (VersionCount.containsKey(data.VersionCount)) {
                            VersionCount[data.VersionCount] = VersionCount[data.VersionCount]!! + 1
                        } else {
                            VersionCount[data.VersionCount] = 1
                        }

                        if (data.IsServer) {
                            ServerServerSize++
                            ServerPlayerSize += data.ServerData!!.PlayerSize

                            if (ServerPlayerVersion.containsKey(data.ServerData.PlayerVersion)) {
                                ServerPlayerVersion[data.ServerData.PlayerVersion] = ServerPlayerVersion[data.ServerData.PlayerVersion]!! + 1
                            } else {
                                ServerPlayerVersion[data.ServerData.PlayerVersion] = 1
                            }

                            if (ServerIpCountry.containsKey(it.country)) {
                                ServerIpCountry[it.country] = ServerIpCountry[it.country]!! + 1
                            } else {
                                ServerIpCountry[it.country] = 1
                            }

                            data.ServerData.IpPlayerCountry.forEach { t, u ->
                                if (ServerIpPlayerCountry.containsKey(t)) {
                                    ServerIpPlayerCountry[t] = ServerIpPlayerCountry[t]!! + u
                                } else {
                                    ServerIpPlayerCountry[t] = u
                                }
                            }
                        } else {
                            RelayServerSize++
                            RelayPlayerSize += data.RelayData!!.PlayerSize
                            RoomAllSize += data.RelayData.RoomAllSize
                            RoomNoStartSize += data.RelayData.RoomNoStartSize
                            RoomPublicListSize += data.RelayData.RoomPublicListSize


                            data.RelayData.PlayerVersion.forEach { t, u ->
                                if (RelayPlayerVersion.containsKey(t)) {
                                    RelayPlayerVersion[t] = RelayPlayerVersion[t]!! + u
                                } else {
                                    RelayPlayerVersion[t] = u
                                }
                            }

                            if (RelayIpCountry.containsKey(it.country)) {
                                RelayIpCountry[it.country] = RelayIpCountry[it.country]!! + 1
                            } else {
                                RelayIpCountry[it.country] = 1
                            }

                            data.RelayData.IpPlayerCountry.forEach { t, u ->
                                if (RelayIpPlayerCountry.containsKey(t)) {
                                    RelayIpPlayerCountry[t] = RelayIpPlayerCountry[t]!! + u
                                } else {
                                    RelayIpPlayerCountry[t] = u
                                }
                            }
                        }
                    }
                }

                statisticalDataSend = BaseData(
                        ServerRunPort, ServerNetType, SystemCount, JavaCount, VersionCount, BaseData.Companion.ServerData(
                        ServerServerSize, ServerPlayerSize, ServerPlayerVersion, ServerIpCountry, ServerIpPlayerCountry
                ), BaseData.Companion.RelayData(
                        RelayServerSize,
                        RelayPlayerSize,
                        RoomAllSize,
                        RoomNoStartSize,
                        RoomPublicListSize,
                        RelayPlayerVersion,
                        RelayIpCountry,
                        RelayIpPlayerCountry
                )
                ).toPrettyPrintingJson()
            }
        }
    }

    companion object {
        internal val statisticalData = OrderedMap<String, StatisticsBackEndData>()
        internal var statisticalDataSend = ""

        internal data class StatisticsBackEndData(val data: Initialization.Companion.BaseDataSend, val country: String)
    }
}