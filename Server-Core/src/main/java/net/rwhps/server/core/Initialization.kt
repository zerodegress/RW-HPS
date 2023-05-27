/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.core

import net.rwhps.server.Main
import net.rwhps.server.core.ServiceLoader.ServiceType
import net.rwhps.server.core.thread.CallTimeTask
import net.rwhps.server.core.thread.Threads
import net.rwhps.server.data.HessModuleManage
import net.rwhps.server.data.global.Cache
import net.rwhps.server.data.global.Data
import net.rwhps.server.data.global.Data.serverCountry
import net.rwhps.server.data.global.NetStaticData
import net.rwhps.server.data.global.Relay
import net.rwhps.server.data.plugin.PluginData
import net.rwhps.server.io.GameOutputStream
import net.rwhps.server.net.HttpRequestOkHttp
import net.rwhps.server.net.NetService
import net.rwhps.server.net.core.IRwHps
import net.rwhps.server.net.core.server.AbstractNetConnect
import net.rwhps.server.net.core.server.AbstractNetConnectServer
import net.rwhps.server.net.netconnectprotocol.*
import net.rwhps.server.net.netconnectprotocol.realize.*
import net.rwhps.server.util.*
import net.rwhps.server.util.algorithms.Aes
import net.rwhps.server.util.algorithms.Rsa
import net.rwhps.server.util.file.FileUtil
import net.rwhps.server.util.inline.readBytes
import net.rwhps.server.util.inline.toPrettyPrintingJson
import net.rwhps.server.util.log.Log
import java.io.DataOutputStream
import java.io.FilterInputStream
import java.io.IOException
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.Socket
import java.util.*
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger


/**
 * @author RW-HPS/Dr
 */
class Initialization {
    private fun checkEnvironment() {
        if (SystemUtil.osArch.contains("arm",ignoreCase = true)) {
            Log.clog("&r RW-HPS It may not be compatible with your architecture, there will be unexpected situations, Thank you !")
        }
    }

    private fun loadLang() {
        Data.i18NBundleMap.put("CN", I18NBundle(Main::class.java.getResourceAsStream("/bundles/GA_zh_CN.properties")!!))
        Data.i18NBundleMap.put("HK", I18NBundle(Main::class.java.getResourceAsStream("/bundles/GA_zh_HK.properties")!!))
        Data.i18NBundleMap.put("RU", I18NBundle(Main::class.java.getResourceAsStream("/bundles/GA_ru_RU.properties")!!))
        Data.i18NBundleMap.put("EN", I18NBundle(Main::class.java.getResourceAsStream("/bundles/GA_en_US.properties")!!))

        // Default use EN
        Data.i18NBundle = Data.i18NBundleMap["EN"]
    }

    private fun initMaps() {
        with (Data.MapsMap) {
            put("Beachlanding(2p)[byhxyy]", "Beach landing (2p) [by hxyy]@[p2]")
            put("BigIsland(2p)", "Big Island (2p)@[p2]")
            put("DireStraight(2p)[byuber]", "Dire_Straight (2p) [by uber]@[p2]")
            put("FireBridge(2p)[byuber]", "Fire Bridge (2p) [by uber]@[p2]")
            put("Hills(2p)[ByTstis&KPSS]", "Hills_(2p)_[By Tstis & KPSS]@[p2]")
            put("IceIsland(2p)", "Ice Island (2p)@[p2]")
            put("Lake(2p)", "Lake (2p)@[p2]")
            put("SmallIsland(2p)", "Small_Island (2p)@[p2]")
            put("Twocoldsides(2p)", "Two_cold_sides (2p)@[p2]")
            put("Hercules(2vs1p)[byuber]", "Hercules_(2vs1p) [by_uber]@[p3]")
            put("KingoftheMiddle(3p)", "King of the Middle (3p)@[p3]")
            put("Depthcharges(4p)[byhxyy]", "Depth charges (4p) [by hxyy]@[p4]")
            put("Desert(4p)", "Desert (4p)@[p4]")
            put("IceLake(4p)[byhxyy]", "Ice Lake (4p) [by hxyy]@[p4]")
            put("Islandfreeze(4p)[byhxyy]", "Island freeze (4p) [by hxyy]@[p4]")
            put("Islands(4p)", "Islands (4p)@[p4]")
            put("LavaMaze(4p)", "Lava Maze (4p)@[p4]")
            put("LavaVortex(4p)", "Lava Vortex (4p)@[p4]")
            put("MagmaIsland(4p)", "Magma Island (4p)@[p4]")
            put("Manipulation(4p)[ByTstis]", "Manipulation_(4p)_[By Tstis]@[p4]")
            put("Nuclearwar(4p)[byhxyy]", "Nuclear war (4p) [by hxyy]@[p4]")
            put("Crossing(6p)", "Crossing (6p)@[p6]")
            put("ShoretoShore(6p)", "Shore to Shore (6p)@[p6]")
            put("ValleyPass(6p)", "Valley Pass (6p)@[p6]")
            put("BridgesOverLava(8p)", "Bridges Over Lava (8p)@[p8]")
            put("Coastline(8p)[byhxyy]", "Coastline (8p) [by hxyy]@[p8]")
            put("HugeSubdivide(8p)", "Huge Subdivide (8p)@[p8]")
            put("Interlocked(8p)", "Interlocked (8p)@[p8]")
            put("InterlockedLarge(8p)", "Interlocked Large (8p)@[p8]")
            put("IsleRing(8p)", "Isle Ring (8p)@[p8]")
            put("LargeIceOutcrop(8p)", "Large Ice Outcrop (8p)@[p8]")
            put("LavaBiogrid(8p)", "Lava Bio-grid(8p)@[p8]")
            put("LavaDivide(8p)", "Lava Divide(8p)@[p8]")
            put("ManyIslands(8p)", "Many Islands (8p)@[p8]")
            put("RandomIslands(8p)", "Random Islands (8p)@[p8]")
            put("Tornadoeye(8p)[byhxyy]", "Tornado eye (8p) [by hxyy]@[p8]")
            put("TwoSides(8p)", "Two Sides (8p)@[p8]")
            put("Volcano(8p)", "Volcano (8p)@[p8]")
            put("VolcanoCrater(8p)", "Volcano Crater(8p)@[p8]")
            put("TwoSidesRemake(10p)", "Two Sides Remake (10p)@[z;p10]")
            put("ValleyArena(10p)[byuber]", "Valley Arena (10p) [by_uber]@[z;p10]")
            put("ManyIslandsLarge(10p)", "Many Islands Large (10p)@[z;p10]")
            put("CrossingLarge(10p)", "Crossing Large (10p)@[z;p10]")
            put("Kingdoms(10p)[byVulkan]", "Kingdoms (10p) [by Vulkan]@[z;p10]")
            put("LargeLavaDivide(10p)", "Large Lava Divide (10p)@[z;p10]")
            put("EnclosedIsland(10p)", "Enclosed Island (10p)@[z;p10]")
            put("TwoLargeIslands(10p)", "Two_Large_Islands_(10p)@[z;p10]")
            put("Wetlands(10p)", "Wetlands (10p)@[z;p10]")
        }
    }

    private fun loadIpBin() {
        if (!Data.config.IpCheckMultiLanguageSupport) {
            return
        }
        /*
		try {
			Data.ip2Location = new IP2Location();
			Data.ip2Location.Open(FileUtil.getFolder(Data.Plugin_Data_Path).toFile("IP.bin").getPath(), true);
		} catch (IOException e) {
			Log.error("IP-LOAD ERR",e);
		}*/
    }

    private fun initRelay() {
        try {
            Cache.packetCache.put("sendSurrenderPacket",GameOutputStream().also {
                it.writeString(".surrender")
                it.writeByte(0)
            }.createPacket(PacketType.CHAT_RECEIVE))
        } catch (e: Exception) {
            Log.error(e)
        }
    }

    private fun initRsa() {
        try {
            val rsa = Rsa().buildKeyPair()
            Rsa.getPublicKey(rsa.public)
            Rsa.getPrivateKey(rsa.private)
        } catch (e: Exception) {
            Log.error(e)
        }
    }

    private fun initGetServerData() {
        Threads.newTimedTask(CallTimeTask.ServerUpStatistics,0, 1, TimeUnit.MINUTES) {
            if (NetStaticData.ServerNetType != IRwHps.NetType.NullProtocol) {
                try {
                    val data = when (NetStaticData.ServerNetType) {
                        IRwHps.NetType.ServerProtocol, IRwHps.NetType.ServerProtocolOld, IRwHps.NetType.ServerTestProtocol -> {
                            BaseDataSend(
                                IsServer = true,
                                ServerData = BaseDataSend.Companion.ServerData(
                                    IpPlayerCountry = mutableMapOf<String, Int>().also {
                                        HessModuleManage.hps.room.playerManage.playerGroup.eachAll {  player ->
                                            val ipCountry = (player.con!! as AbstractNetConnect).ipCountry
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
                                IsServer = false
                            )
                        }
                    }


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
                } catch (e: Exception) {
                }
            }
        }
    }

    companion object {
        @Volatile
        private var isClose = true

        internal fun startInit(pluginData: PluginData) {
            initServerLanguage(pluginData)
            eula(pluginData)
        }

        /**
         * The country is determined according to the server's export ip when it is first started
         * Choose the language environment according to the country
         */
        internal fun initServerLanguage(pluginData: PluginData, country: String = "") {
            serverCountry =
                if (country.isBlank()) {
                    pluginData.getData("serverCountry") {
                        val countryUrl = HttpRequestOkHttp.doGet(Data.urlData.readString("Get.ServerLanguage.Bak"))

                        when {
                            countryUrl.contains("香港") -> "HK"
                            countryUrl.contains("中国") -> "CN"
                            countryUrl.contains("俄罗斯") -> "RU"
                            else -> "EN"
                        }
                    }
                } else {
                    when {
                        country.contains("HK") || country.contains("CN") || country.contains("RU") -> country
                        else -> "EN"
                    }.also {
                        pluginData.setData("serverCountry",it)
                    }
                }

            Data.i18NBundle = Data.i18NBundleMap[serverCountry]
            Log.clog(Data.i18NBundle.getinput("server.language"))
        }

        private fun eula(pluginData: PluginData) {
            // Eula
            if (pluginData.getData("eulaVersion","") != Data.SERVER_EULA_VERSION) {
                val eulaBytes = if (serverCountry == "CN") {
                    FileUtil.getInternalFileStream("/eula/China.md").readBytes()
                } else {
                    FileUtil.getInternalFileStream("/eula/English.md").readBytes()
                }
                Log.clog(ExtractUtil.str(eulaBytes,Data.UTF_8))

                Log.clog("Agree to enter : Yes , Otherwise please enter : No")
                Data.privateOut.print("Please Enter (Yes/No) > ")

                Scanner(object : FilterInputStream(System.`in`) {
                    @Throws(IOException::class)
                    override fun close() {
                        //do nothing
                    }
                }).use {
                    while (true) {
                        val text = it.nextLine()
                        if (text.equals("Yes",ignoreCase = true)) {
                            pluginData.setData("eulaVersion",Data.SERVER_EULA_VERSION)
                            Log.clog("Thanks !")
                            return
                        } else if (text.equals("No",ignoreCase = true)) {
                            Log.clog("Thanks !")
                            Core.exit()
                        } else {
                            Log.clog("Re Enter")
                            Data.privateOut.print("Please Enter (Yes/No) > ")
                        }
                    }
                }
            }
        }

        internal fun loadService() {
            ServiceLoader.addService(ServiceType.ProtocolType, IRwHps.NetType.ServerTestProtocol.name,      TypeRwHpsJump::class.java)
            ServiceLoader.addService(ServiceType.ProtocolType, IRwHps.NetType.RelayProtocol.name,           TypeRelay::class.java)
            ServiceLoader.addService(ServiceType.ProtocolType, IRwHps.NetType.RelayMulticastProtocol.name,  TypeRelayRebroadcast::class.java)

            ServiceLoader.addService(ServiceType.Protocol,     IRwHps.NetType.ServerProtocolOld.name,       GameVersionServerOld::class.java)
            ServiceLoader.addService(ServiceType.Protocol,     IRwHps.NetType.ServerTestProtocol.name,      GameVersionServerJump::class.java)
            ServiceLoader.addService(ServiceType.Protocol,     IRwHps.NetType.RelayProtocol.name,           GameVersionRelay::class.java)
            ServiceLoader.addService(ServiceType.Protocol,     IRwHps.NetType.RelayMulticastProtocol.name,  GameVersionRelayRebroadcast::class.java)

            ServiceLoader.addService(ServiceType.ProtocolPacket,IRwHps.NetType.ServerProtocol.name,         GameVersionPacket::class.java)
            ServiceLoader.addService(ServiceType.ProtocolPacket,IRwHps.NetType.ServerProtocolOld.name,      GameVersionPacketOld::class.java)

            ServiceLoader.addService(ServiceType.IRwHps,"IRwHps", RwHps::class.java)

        }

        internal data class BaseDataSend(
            val SendTime: Int                             = Time.concurrentSecond(),
            val ServerRunPort: Int                        = Data.config.Port,
            val ServerNetType: String                     = NetStaticData.ServerNetType.name,
            val System: String                            = SystemUtil.osName,
            val JavaVersion: String                       = SystemUtil.javaVersion,
            val VersionCount: String                      = Data.SERVER_CORE_VERSION,
            val IsServerRun: Boolean                      = true,
            val IsServer: Boolean,
            val ServerData: ServerData? = null,
            val RelayData: RelayData? = null,
        ) {
            companion object {
                data class ServerData(
                    val PlayerSize: Int                     = AtomicInteger().also { NetStaticData.netService.eachAll { e: NetService -> it.addAndGet(e.getConnectSize()) } }.get(),
                    val MaxPlayer: Int                      = Data.configServer.MaxPlayer,
                    val PlayerVersion: Int                  = (NetStaticData.RwHps.typeConnect.abstractNetConnect as AbstractNetConnectServer).supportedVersionInt,
                    val IpPlayerCountry: Map<String,Int>,
                )

                data class RelayData(
                    val PlayerSize: Int = AtomicInteger().also {
                        NetStaticData.netService.eachAll { e: NetService ->
                            it.addAndGet(
                                e.getConnectSize()
                            )
                        }
                    }.get(),
                    val RoomAllSize: Int = Relay.roomAllSize,
                    val RoomNoStartSize: Int = Relay.roomNoStartSize,
                    val RoomPublicListSize: Int = 0,
                    val PlayerVersion: Map<Int, Int> = Relay.getAllRelayVersion(),
                    val IpPlayerCountry: Map<String, Int> = Relay.getAllRelayIpCountry(),
                )
            }
        }
    }

    init {
        checkEnvironment()
        //
        loadLang()

        initMaps()
        // 初始化 投降
        initRelay()
        //initRsa()
        initGetServerData()

        Runtime.getRuntime().addShutdownHook(object : Thread("Exit Handler") {
            override fun run() {
                if (!isClose) {
                    return
                }
                isClose = true

                Data.core.save()
                println("Exit Save Ok")

                System.setOut(Data.privateOut)
            }
        })
    }
}