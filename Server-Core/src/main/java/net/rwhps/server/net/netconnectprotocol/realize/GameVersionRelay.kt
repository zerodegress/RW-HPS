/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.net.netconnectprotocol.realize

import com.vdurmont.emoji.EmojiManager
import net.rwhps.server.data.global.Cache
import net.rwhps.server.data.global.Data
import net.rwhps.server.data.global.NetStaticData
import net.rwhps.server.data.global.Relay
import net.rwhps.server.game.GameMaps
import net.rwhps.server.io.GameInputStream
import net.rwhps.server.io.GameOutputStream
import net.rwhps.server.io.output.CompressOutputStream
import net.rwhps.server.io.packet.Packet
import net.rwhps.server.net.core.ConnectionAgreement
import net.rwhps.server.net.core.DataPermissionStatus.RelayStatus
import net.rwhps.server.net.core.server.AbstractNetConnect
import net.rwhps.server.net.core.server.AbstractNetConnectData
import net.rwhps.server.net.core.server.AbstractNetConnectRelay
import net.rwhps.server.net.netconnectprotocol.internal.relay.fromRelayJumpsToAnotherServer
import net.rwhps.server.net.netconnectprotocol.internal.relay.relayServerInitInfo
import net.rwhps.server.net.netconnectprotocol.internal.relay.relayServerTypeInternal
import net.rwhps.server.net.netconnectprotocol.internal.relay.relayServerTypeReplyInternal
import net.rwhps.server.net.netconnectprotocol.internal.server.chatUserMessagePacketInternal
import net.rwhps.server.util.GameOtherUtil.getBetaVersion
import net.rwhps.server.util.IsUtil
import net.rwhps.server.util.PacketType
import net.rwhps.server.util.algorithms.NetConnectProofOfWork
import net.rwhps.server.util.alone.annotations.MainProtocolImplementation
import net.rwhps.server.util.game.CommandHandler
import net.rwhps.server.util.log.Log.debug
import net.rwhps.server.util.log.Log.error
import java.io.IOException
import java.util.*

/**
 * Many thanks to them for providing cloud computing for the project
 * This is essential to complete the RW-HPS Relay test
 * @Thanks : [SimpFun Cloud](https://vps.tiexiu.xyz)
 * @Thanks : [Github 1dNDN](https://github.com/1dNDN)
 *
 * This test was done on :
 * Relay-CN (V. 6.1.0)
 * 2022.7.22 10:00
 */

/**
 * Relay protocol implementation
 * Direct forwarding consumes more bandwidth and the same effect as using VPN forwarding
 *
 * @property permissionStatus       Connection authentication status
 * @property netConnectAuthenticate Connection validity verification
 * @property relay                  Relay instance
 * @property site                   Connect the forwarding location within the RELAY
 * @property cachePacket            Cached Package
 * @property relaySelect            Function1<String, Unit>?
 * @property name                   player's name
 * @property registerPlayerId       UUID-Hash code after player registration
 * @property betaGameVersion        Is it a beta version
 * @property clientVersion          clientVersion
 * @property version                Protocol version
 * @constructor
 *
 * @author RW-HPS/Dr
 */
@MainProtocolImplementation
open class GameVersionRelay(connectionAgreement: ConnectionAgreement) : AbstractNetConnect(connectionAgreement), AbstractNetConnectData, AbstractNetConnectRelay {
    override var permissionStatus: RelayStatus = RelayStatus.InitialConnection
        internal set

    /** Client computing proves non-Bot */
    private var netConnectAuthenticate: NetConnectProofOfWork? = null

    override var relay: Relay? = null
        protected set

    var site = -1
        protected set

    protected var cachePacket: Packet? = null
        private set

    protected var relaySelect: ((String) -> Unit)? = null

    override var name = "NOT NAME"
        protected set
    override var registerPlayerId: String? = null
        protected set
    override var betaGameVersion = false
        protected set
    override var clientVersion = 151
        protected set

    // 172
    protected val version2 = 999

    override fun setCachePacket(packet: Packet) {
        cachePacket = packet
    }

    override fun setlastSentPacket(packet: Packet) {
        /* 此协议下不被使用 */
    }

    override val version: String
        get() = "1.14 RELAY"

    override fun sendRelayServerInfo() {
        val cPacket: Packet? = Cache.packetCache["sendRelayServerInfo"]
        if (IsUtil.notIsBlank(cPacket)) {
            sendPacket(cPacket!!)
            return
        }
        try {
            val packetCache = relayServerInitInfo()
            Cache.packetCache.put("sendRelayServerInfo",packetCache)
            sendPacket(packetCache)
        } catch (e: Exception) {
            error(e)
        }
    }

    @Throws(IOException::class)
    override fun relayDirectInspection(relay: Relay?) {
        GameInputStream(cachePacket!!).use { inStream ->
            inStream.readString()
            val packetVersion = inStream.readInt()
            clientVersion = inStream.readInt()
            betaGameVersion = getBetaVersion(clientVersion)

            if (packetVersion >= 1) {
                inStream.skip(4)
            }
            var queryString = ""
            if (packetVersion >= 2) {
                queryString = inStream.readIsString()
            }
            if (packetVersion >= 3) {
                // Player Name
                inStream.readString()
            }
            if (relay == null) {
                if (IsUtil.isBlank(queryString) || "RELAYCN".equals(queryString, ignoreCase = true)) {
                    sendRelayServerType(Data.i18NBundle.getinput("relay.hi", Data.SERVER_CORE_VERSION))
                } else {
                    idCustom(queryString)
                }
            } else {
                this.relay = relay
                addRelayConnect()
            }
        }
    }

    override fun sendVerifyClientValidity() {
        netConnectAuthenticate = Relay.randPow
        val netConnectAuthenticate: NetConnectProofOfWork = netConnectAuthenticate!!
        val authenticateType = netConnectAuthenticate.authenticateType.toInt()
        try {
            val o = GameOutputStream()
            // 返回相同
            o.writeInt(netConnectAuthenticate.resultInt)
            o.writeInt(authenticateType)
            if (authenticateType == 0 || netConnectAuthenticate.authenticateType in 2..4 || authenticateType == 6) {
                o.writeBoolean(true)
                o.writeInt(netConnectAuthenticate.initInt_1)
            } else {
                o.writeBoolean(false)
            }
            if (authenticateType == 1 || netConnectAuthenticate.authenticateType in 2..4) {
                o.writeBoolean(true)
                o.writeInt(netConnectAuthenticate.initInt_2)
            } else {
                o.writeBoolean(false)
            }
            if (netConnectAuthenticate.authenticateType in 5..6) {
                o.writeString(netConnectAuthenticate.outcome)
                o.writeString(netConnectAuthenticate.fixedInitial)
                o.writeInt(netConnectAuthenticate.maximumNumberOfCalculations)
            }

            o.writeBoolean(false)

            sendPacket(o.createPacket(PacketType.RELAY_POW))
        } catch (e: Exception) {
            error(e)
        }
    }

    override fun receiveVerifyClientValidity(packet: Packet): Boolean {
        try {
            GameInputStream(packet).use { inStream ->
                if (netConnectAuthenticate != null) {
                    if (netConnectAuthenticate!!.check(inStream.readInt(),inStream.readInt(),inStream.readString())) {
                        netConnectAuthenticate = null
                        return true
                    }
                }
            }
        } catch (e: Exception) {
            error(e)
        }
        return false
    }

    override fun sendRelayServerType(msg: String, run: ((String) -> Unit)?) {
        try {
            sendPacket(relayServerTypeInternal(msg))

            relaySelect = run

            inputPassword = true
        } catch (e: Exception) {
            error(e)
        }
    }

    @Throws(IOException::class)
    override fun receiveChat(packet: Packet) {
        GameInputStream(packet).use { inStream ->

            val message: String = inStream.readString()

            if (relay == null || relay!!.allmute) {
                return
            }

            if (message.startsWith(".") || message.startsWith("-")) {
                val response = Data.RELAY_COMMAND.handleMessage(message, this)
                if (response == null || response.type == CommandHandler.ResponseType.noCommand) {
                } else if (response.type == CommandHandler.ResponseType.valid) {
                    return
                } else if (response.type != CommandHandler.ResponseType.valid) {
                    val text: String = when (response.type) {
                        CommandHandler.ResponseType.manyArguments -> "Too many arguments. Usage: " + response.command.text + " " + response.command.paramText
                        CommandHandler.ResponseType.fewArguments -> "Too few arguments. Usage: " + response.command.text + " " + response.command.paramText
                        else -> {
                            sendPackageToHOST(packet)
                            return
                        }
                    }
                    sendPacket(NetStaticData.RwHps.abstractNetPacket.getSystemMessagePacket(text))
                }
            }

            sendPackageToHOST(packet)
        }
    }

    override fun sendRelayServerTypeReply(packet: Packet) {
        try {
            inputPassword = false

            val id = relayServerTypeReplyInternal(packet)
            if (relaySelect == null) {
                idCustom(id)
            } else {
                relaySelect!!(id)
            }
        } catch (e: Exception) {
            error(e)
        }
    }

    override fun sendRelayServerId() {
        try {
            inputPassword = false
            if (relay == null) {
                //Log.debug("sendRelayServerId","Relay == null");
                relay = NetStaticData.relay
            }

            if (site != -1) {
                relay!!.removeAbstractNetConnect(site)
                site = -1
            }

            relay!!.admin = this

            val o = GameOutputStream()
            if (clientVersion >= version2) {
                o.writeByte(2)
                o.writeBoolean(true)
                o.writeBoolean(true)
                o.writeBoolean(true)
                o.writeString(relay!!.serverUuid)
                o.writeBoolean(relay!!.isMod) //MOD
                o.writeBoolean(false)
                o.writeBoolean(true)
                o.writeString("{{RW-HPS Relay}}.Room ID : ${Data.configRelay.MainID}" + relay!!.id)
                o.writeBoolean(false)
                o.writeIsString(registerPlayerId)
            } else {
                // packetVersion
                o.writeByte(1)
                // allowThisConnectionForwarding
                o.writeBoolean(true)
                // removeThisConnection
                o.writeBoolean(true)
                // useServerId
                o.writeIsString(relay!!.serverUuid)
                // useMods
                o.writeBoolean(relay!!.isMod) //MOD
                // showPublicly
                o.writeBoolean(false)
                // relayMessageOnServer
                o.writeIsString("{{RW-HPS Relay}}.Room ID : ${Data.configRelay.MainID}" + relay!!.id)
                // useMulticast
                o.writeBoolean(false)
            }

            sendPacket(o.createPacket(PacketType.RELAY_BECOME_SERVER)) //+108+140
            //getRelayT4(Data.localeUtil.getinput("relay.server.admin.connect",relay.getId()));
            sendPacket(NetStaticData.RwHps.abstractNetPacket.getChatMessagePacket(Data.i18NBundle.getinput("relay.server.admin.connect", Data.configRelay.MainID+relay!!.id, Data.configRelay.MainID+relay!!.internalID.toString()), "RELAY_CN-ADMIN", 5))
            sendPacket(NetStaticData.RwHps.abstractNetPacket.getChatMessagePacket(Data.i18NBundle.getinput("relay", Data.configRelay.MainID+relay!!.id), "RELAY_CN-ADMIN", 5))
            //ping();

            //debug(name)
            if (name.equals("SERVER", ignoreCase = true) || name.equals("RELAY", ignoreCase = true)) {
                relay!!.groupNet.disconnect() // Close Room
                disconnect() // Close Connect & Reset Room
            }
        } catch (e: Exception) {
            error(e)
        }
    }

    override fun getRelayT4(msg: String) {
        try {
            sendPacket(chatUserMessagePacketInternal(msg))
        } catch (e: Exception) {
            error(e)
        }
    }

    override fun getPingData(packet: Packet) {
        try {
            GameInputStream(packet).use { inStream ->
                val out = GameOutputStream()
                out.transferToFixedLength(inStream,8)
                out.writeByte(1)
                out.writeByte(60)
                sendPacket(out.createPacket(PacketType.HEART_BEAT_RESPONSE))
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun addGroup(packet: Packet) {
        relay!!.groupNet.broadcastAndUDP(packet)
    }

    override fun addGroupPing(packet: Packet) {
        try {
            relay!!.groupNet.broadcastAndUDP(packet)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun addRelayConnect() {
        try {
            permissionStatus = RelayStatus.PlayerPermission

            inputPassword = false
            if (relay == null) {
                relay = NetStaticData.relay
            }

            site = relay!!.setAddPosition()
            relay!!.setAbstractNetConnect(this)

            val o = GameOutputStream()
            if (clientVersion >= version2) {
                o.writeByte(1)
                o.writeInt(site)
                // ?
                o.writeString(registerPlayerId!!)
                //o.writeBoolean(false)
                // User UUID
                o.writeIsString(null)
                o.writeIsString(ip)
                relay!!.admin!!.sendPacket(o.createPacket(PacketType.FORWARD_CLIENT_ADD))
            } else {
                o.writeByte(0)
                o.writeInt(site)
                o.writeString(registerPlayerId!!)
                o.writeIsString(null)
                relay!!.admin!!.sendPacket(o.createPacket(PacketType.FORWARD_CLIENT_ADD))
            }

            sendPackageToHOST(cachePacket!!)
            connectionAgreement.add(relay!!.groupNet)
            sendPacket(NetStaticData.RwHps.abstractNetPacket.getChatMessagePacket(Data.i18NBundle.getinput("relay", Data.configRelay.MainID+relay!!.id), "RELAY_CN-ADMIN", 5))

            this.relay!!.setAddSize()
        } catch (e: Exception) {
            permissionStatus = RelayStatus.CertifiedEnd
            error("[Relay] addRelayConnect", e)
        } finally {
            //cachePacket = null;
        }
    }

    override fun relayRegisterConnection(packet: Packet) {
        if (registerPlayerId.isNullOrBlank()) {
            try {
                GameInputStream(packet).use { stream ->
                    stream.readString()
                    stream.skip(12)
                    name = stream.readString()
                    stream.readIsString()
                    stream.readString()
                    if (IsUtil.isBlank(registerPlayerId)) {
                        registerPlayerId = stream.readString()
                    }
                }
                return
            } catch (e: Exception) {
            }
        }

        sendPackageToHOST(packet)
    }

    override fun addReRelayConnect() {
        try {
            val o = GameOutputStream()
            if (clientVersion >= version2) {
                o.writeByte(1)
                o.writeInt(site)
                // ?
                o.writeString(registerPlayerId!!)
                // User UUID
                o.writeIsString(registerPlayerId!!)
                o.writeIsString(ip)
                relay!!.admin!!.sendPacket(o.createPacket(PacketType.FORWARD_CLIENT_ADD))
                //Log.clog("172")
            } else {
                o.writeByte(0)
                o.writeInt(site)
                o.writeString(registerPlayerId!!)
                o.writeIsString(registerPlayerId!!)
                relay!!.admin!!.sendPacket(o.createPacket(PacketType.FORWARD_CLIENT_ADD))
            }
            relay!!.admin!!.sendPacket(o.createPacket(PacketType.FORWARD_CLIENT_ADD))
            val o1 = GameOutputStream()
            o1.writeInt(site)
            o1.writeInt(cachePacket!!.bytes.size + 8)
            o1.writeInt(cachePacket!!.bytes.size)
            o1.writeInt(160)
            o1.writeBytes(cachePacket!!.bytes)
            relay!!.admin!!.sendPacket(o1.createPacket(PacketType.PACKET_FORWARD_CLIENT_FROM))
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            //cachePacket = null;
        }
    }

    override fun addRelaySend(packet: Packet) {
        try {
            GameInputStream(packet).use { inStream ->
                val target = inStream.readInt()
                val type = inStream.readInt()

                if (type == PacketType.DISCONNECT.typeInt) {
                    return
                }

                val bytes = inStream.readStreamBytes()

                val abstractNetConnect = relay!!.getAbstractNetConnect(target)

                if (PacketType.KICK.typeInt == type) {
                    val gameOutputStream = GameOutputStream()
                    gameOutputStream.writeString(GameInputStream(bytes).readString().replace("\\d".toRegex(), ""))
                    abstractNetConnect?.sendPacket(gameOutputStream.createPacket(type))
                    relayPlayerDisconnect()
                    return
                }

                abstractNetConnect?.sendPacket(Packet(type, bytes))

                when (type) {
                    PacketType.RETURN_TO_BATTLEROOM.typeInt -> {
                        relay!!.isStartGame = false
                    }
                }
            }
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (_: NullPointerException) {
        }
    }

    override fun sendPackageToHOST(packet: Packet) {
        try {
            val o = GameOutputStream()
            o.writeInt(site)
            o.writeInt(packet.bytes.size + 8)
            o.writeInt(packet.bytes.size)
            o.writeInt(packet.type.typeInt)
            o.writeBytes(packet.bytes)
            relay!!.admin?.sendPacket(o.createPacket(PacketType.PACKET_FORWARD_CLIENT_FROM))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun relayPlayerDisconnect() {
        try {
            val out = GameOutputStream()
            out.writeByte(0)
            out.writeInt(site)
            sendPackageToHOST(out.createPacket(PacketType.FORWARD_CLIENT_REMOVE))
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    override fun multicastAnalysis(packet: Packet) {
        // Protocol not supported
    }

    internal fun kick(msg: String) {
        val o = GameOutputStream()
        o.writeString(msg)
        sendPacket(o.createPacket(PacketType.KICK))
        disconnect()
    }

    override fun disconnect() {
        if (super.isDis) {
            return
        }
        super.isDis = true
        if (relay != null && (permissionStatus == RelayStatus.PlayerPermission || permissionStatus == RelayStatus.HostPermission)) {
            relay!!.setRemoveSize()

            var errorClose = false

            try {
                if (this !== relay!!.admin) {
                    relay!!.removeAbstractNetConnect(site)

                    try {
                        if (relay!!.admin != null) {
                            sendPackageToHOST(NetStaticData.RwHps.abstractNetPacket.getExitPacket())
                        }
                    } catch (e: Exception) {
                        error("[Relay disconnect] Send Exited", e)
                    }
                } else {
                    Relay.serverRelayIpData.remove(ip)
                    // 房间开始游戏 或者 在列表
                    if (relay!!.isStartGame) {
                        if (relay!!.getSize() > 0) {
                            // Move Room Admin
                            adminMoveNew()
                        }
                    } else {
                        // Close Room
                        relay!!.groupNet.disconnect()
                    }
                }
            } catch (e: Exception) {
                debug("[Relay Close Error]",e)
                errorClose =true
            }
            if ((relay!!.getSize() <= 0 && !relay!!.closeRoom) || errorClose) {
                debug("[Relay] Gameover")
                relay!!.re()
            }
            super.close(relay!!.groupNet)
        } else {
            super.close(null)
        }
    }

    private fun idCustom(inId: String) {
        // 过滤 制表符 空格 换行符
        var id = inId.replace("\\s".toRegex(), "")

        if (id.isEmpty()) {
            sendRelayServerType(Data.i18NBundle.getinput("relay.server.error.id", "什么都没输入就点击确认"))
            return
        }
        if (EmojiManager.containsEmoji(id)) {
            sendRelayServerType(Data.i18NBundle.getinput("relay.server.error.id", "不能使用Emoji"))
            return
        }

//        if (Relay.roomAllSize > 300) {
//            sendRelayServerType("RELAY-CN 房间数量达到上限, 请稍后重试")
//            return
//        }


        if (id.startsWith("RA")) {
            if (Data.configRelay.MainServer) {
                sendPacket(fromRelayJumpsToAnotherServer("${id[1]}.relay.der.kim/$id"))
                return
            } else {
                id = id.substring(2)
            }
        } else if(id.startsWith("RB",true) && Data.configRelay.UpList) {
            id = id.substring(2)
            sendRelayServerType(Data.i18NBundle.getinput("relay.server.error.id", "[RCN-前瞻] 本服务器不支持绑定"))
            return
        } else if ("R".equals(id[0].toString(), ignoreCase = true)) {
            id = id.substring(1)
        }

        if (id.isEmpty()) {
            sendRelayServerType(Data.i18NBundle.getinput("relay.server.no", "R"))
            return
        }

        if ("C".equals(id[0].toString(), ignoreCase = true)) {
            id = id.substring(1)
            if (id.isEmpty()) {
                sendRelayServerType(Data.i18NBundle.getinput("relay.id.re"))
                return
            }
            if ("M".equals(id[0].toString(), ignoreCase = true)) {
                id = id.substring(1)
                if (checkLength(id)) {
                    if (Relay.getCheckRelay(id) || id.startsWith("A",true) || id.startsWith("B",true)) {
                        sendRelayServerType(Data.i18NBundle.getinput("relay.id.re"))
                    } else {
                        newRelayId(id, true)
                    }
                }
            } else {
                if (checkLength(id)) {
                    if (Relay.getCheckRelay(id) || id.startsWith("A",true) || id.startsWith("B",true)) {
                        sendRelayServerType(Data.i18NBundle.getinput("relay.id.re"))
                    } else {
                        newRelayId(id, false)
                    }
                }
            }
            return
        }

        var uplist = false
        var mods = false
        var newRoom = true

        if (id.startsWith("newup", ignoreCase = true)) {
            uplist = true
            id = id.substring(5)
        } else if (id.startsWith("newsup", ignoreCase = true)) {
            uplist = true
            id = id.substring(6)
        } else if (id.startsWith("modup", ignoreCase = true)) {
            uplist = true
            mods = true
            id = id.substring(5)
        } else if (id.startsWith("modsup", ignoreCase = true)) {
            uplist = true
            mods = true
            id = id.substring(6)
        } else if (id.startsWith("new", ignoreCase = true)) {
            uplist = false
            id = id.substring(3)
        } else if (id.startsWith("news", ignoreCase = true)) {
            uplist = false
            id = id.substring(4)
        } else if (id.startsWith("mod", ignoreCase = true)) {
            uplist = false
            mods = true
            id = id.substring(3)
        } else if (id.startsWith("mods", ignoreCase = true)) {
            uplist = false
            mods = true
            id = id.substring(4)
        } else {
            newRoom = false
        }

        val custom = CustomRelayData()

        try {
            if (id.startsWith("P", ignoreCase = true)) {
                id = id.substring(1)
                val arry = if (id.contains("，")) id.split("，") else id.split(",")
                custom.MaxPlayerSize = arry[0].toInt()
                if  (custom.MaxPlayerSize !in 0..100) {
                    sendRelayServerType(Data.i18NBundle.getinput("relay.id.maxPlayer.re"))
                    return
                }
                if (arry.size > 1) {
                    if (arry[1].contains("I", ignoreCase = true)) {
                        val ay = arry[1].split("I", ignoreCase = true)
                        if (ay.size > 1) {
                            custom.MaxUnitSizt = ay[0].toInt()
                            custom.Income = ay[1].toFloat()
                        } else {
                            custom.Income = ay[0].toFloat()
                        }
                    } else {
                        custom.MaxUnitSizt = arry[1].toInt()
                    }
                    if  (custom.MaxUnitSizt !in 0..Int.MAX_VALUE) {
                        sendRelayServerType(Data.i18NBundle.getinput("relay.id.maxUnit.re"))
                        return
                    }
                }
            }
            if (id.startsWith("I", ignoreCase = true)) {
                id = id.substring(1)
                val arry = if (id.contains("，")) id.split("，") else id.split(",")
                custom.Income = arry[0].toFloat()
                if  (custom.Income !in 0F..Float.MAX_VALUE) {
                    sendRelayServerType(Data.i18NBundle.getinput("relay.id.income.re"))
                    return
                }
            }
        } catch(e: NumberFormatException) {
            sendRelayServerType(Data.i18NBundle.getinput("relay.server.error", e.message))
            return
        }

        if (newRoom) {
            newRelayId(mods,custom)

            if (custom.MaxPlayerSize != -1 || custom.MaxUnitSizt != 200) {
                sendPacket(NetStaticData.RwHps.abstractNetPacket.getChatMessagePacket("自定义人数: ${custom.MaxPlayerSize} 自定义单位: ${custom.MaxUnitSizt}", "RELAY_CN-Custom", 5))
            }
        } else {
            try {
                if (id.contains(".")) {
                    sendRelayServerType(Data.i18NBundle.getinput("relay.server.error", "不能包含 [ . ]"))
                    return
                }
                relay = Relay.getRelay(id)
                if (relay != null) {
                    addRelayConnect()
                } else {
                    sendRelayServerType(Data.i18NBundle.getinput("relay.server.no", id))
                }
            } catch (e: Exception) {
                debug(e)
                sendRelayServerType(Data.i18NBundle.getinput("relay.server.error", e.message))
            }
        }
    }

    private fun checkLength(str: String): Boolean {
        if (str.length > 7 || str.length < 3) {
            sendRelayServerType(Data.i18NBundle.getinput("relay.id.re"))
            return false
        }
        if (str.contains(".")) {
            sendRelayServerType(Data.i18NBundle.getinput("relay.server.error", "不能包含 [ . ]"))
            return false
        }
        return true
    }

    private fun adminMoveNew() {
        // 更新最小玩家
        relay!!.updateMinSize()
        relay!!.getAbstractNetConnect(relay!!.minSize)?.let {
            it.sendRelayServerId()
            relay!!.abstractNetConnectIntMap.values.forEach { obj: GameVersionRelay -> obj.addReRelayConnect() }
        }
    }

    private fun newRelayId(mod: Boolean, customRelayData: CustomRelayData) {
        newRelayId(null, mod, customRelayData)
    }

    private fun newRelayId(id: String?, mod: Boolean, customRelayData: CustomRelayData = CustomRelayData()) {
        val maxPlayer = if (customRelayData.MaxPlayerSize == -1) 10 else customRelayData.MaxPlayerSize

        relay = if (IsUtil.isBlank(id)) {
            Relay.getRelay( playerName = name, isMod = mod, betaGameVersion = betaGameVersion, version = clientVersion, maxPlayer = maxPlayer)
        } else {
            Relay.getRelay( id!!, name, mod, betaGameVersion, clientVersion, maxPlayer)
        }

        relay!!.isMod = mod

        if (customRelayData.MaxPlayerSize != -1 || customRelayData.Income != 1F) {
            customModePlayerSize(customRelayData)
        }

        sendRelayServerId()
        relay!!.setAddSize()
    }

    private fun customModePlayerSize(customRelayData: CustomRelayData) {
        val registerServer = GameOutputStream()
        registerServer.writeString(Data.SERVER_ID)
        registerServer.writeInt(1)
        registerServer.writeInt(clientVersion)
        registerServer.writeInt(clientVersion)
        registerServer.writeString("com.corrodinggames.rts.server")
        registerServer.writeString(relay!!.serverUuid)
        registerServer.writeInt("Dr @ 2022".hashCode())
        sendPacket(registerServer.createPacket(PacketType.PREREGISTER_INFO))

        val o2 = GameOutputStream()
        o2.writeString(Data.SERVER_ID_RELAY)
        o2.writeInt(clientVersion)
        o2.writeInt(GameMaps.MapType.customMap.ordinal)
        o2.writeString("RW-HPS RELAY Custom Mode")
        o2.writeInt(Data.game.credits)
        o2.writeInt(Data.game.mist)
        o2.writeBoolean(true)
        o2.writeInt(1)
        o2.writeByte(0)
        o2.writeBoolean(false)
        o2.writeBoolean(false)
        sendPacket(o2.createPacket(PacketType.SERVER_INFO))

        val o = GameOutputStream()
        o.writeInt(0)
        o.writeBoolean(false)
        /* RELAY Custom MaxPlayer */
        o.writeInt(customRelayData.MaxPlayerSize)
        o.flushEncodeData(CompressOutputStream.getGzipOutputStream("teams", true).also { for (i in 0 until customRelayData.MaxPlayerSize) it.writeBoolean(false)})
        o.writeInt(Data.game.mist)
        o.writeInt(Data.game.credits)
        o.writeBoolean(true)
        o.writeInt(1)
        o.writeByte(5)
        // RELAY Custom MaxUnit
        o.writeInt(customRelayData.MaxUnitSizt)
        o.writeInt(customRelayData.MaxUnitSizt)
        o.writeInt(Data.game.initUnit)
        // RELAY Custom income
        o.writeFloat(customRelayData.Income)
        o.writeBoolean(!Data.game.noNukes)
        o.writeBoolean(false)
        o.writeBoolean(false)
        o.writeBoolean(Data.game.sharedControl)
        o.writeBoolean(Data.game.gamePaused)
        sendPacket(o.createPacket(PacketType.TEAM_LIST))
    }

    private data class CustomRelayData(
        var MaxPlayerSize: Int = -1,
        var MaxUnitSizt: Int = 200,
        var Income: Float = 1f,
    )

}
/**
2023-05-01 17:38:25.569: network:PACKET_SEND_PREREGISTER_INFO: Register connection has already been sent (resending)
2023-05-01 17:38:25.569: sendRegisterConnection...
getNewTextureHolder: append:101
getNewTextureHolder: append:102
getNewTextureHolder: append:103
getNewTextureHolder: append:104
getNewTextureHolder: append:105
getNewTextureHolder: append:106
getNewTextureHolder: append:107
getNewTextureHolder: append:108
getNewTextureHolder: append:109
getNewTextureHolder: append:110
getNewTextureHolder: append:111
getNewTextureHolder: append:112
getNewTextureHolder: append:113
getNewTextureHolder: append:114
2023-05-01 17:38:26.101: FileLoader: Path changed to maps2 path:/SD/rustedWarfare/maps\南京保卫战30p2_map.png
2023-05-01 17:38:26.102: FileLoader: convertAbstractPath2: Changing to:/SD/mods/maps\南京保卫战30p2_map.png
Method:ReleaseTexture
getNewTextureHolder: set:95
2023-05-01 17:38:26.118: convertTexturePath for: drawable:error_missingmap.png
2023-05-01 17:38:26.243: FileLoader: Path changed to maps2 path:/SD/rustedWarfare/maps\南京保卫战30p2_map.png
2023-05-01 17:38:26.244: FileLoader: convertAbstractPath2: Changing to:/SD/mods/maps\南京保卫战30p2_map.png
2023-05-01 17:38:26.654: Next music track, timer:3600.2056
2023-05-01 17:38:26.656: Queued:music/starting/battletanks1B.ogg
2023-05-01 17:38:28.224: FileLoader: Path changed to maps2 path:/SD/rustedWarfare/maps\南京保卫战30p2_map.png
2023-05-01 17:38:28.225: FileLoader: convertAbstractPath2: Changing to:/SD/mods/maps\南京保卫战30p2_map.png
2023-05-01 17:38:28.258: Now playing:music/starting/battletanks1B.ogg
2023-05-01 17:38:29.108: FileLoader: Path changed to maps2 path:/SD/rustedWarfare/maps\南京保卫战30p2_map.png
2023-05-01 17:38:29.108: FileLoader: convertAbstractPath2: Changing to:/SD/mods/maps\南京保卫战30p2_map.png
2023-05-01 17:38:30.113: FileLoader: Path changed to maps2 path:/SD/rustedWarfare/maps\南京保卫战30p2_map.png
2023-05-01 17:38:30.113: FileLoader: convertAbstractPath2: Changing to:/SD/mods/maps\南京保卫战30p2_map.png
2023-05-01 17:38:30.232: FileLoader: Path changed to maps2 path:/SD/rustedWarfare/maps\南京保卫战30p2_map.png
2023-05-01 17:38:30.233: FileLoader: convertAbstractPath2: Changing to:/SD/mods/maps\南京保卫战30p2_map.png
2023-05-01 17:38:30.270: FileLoader: Path changed to maps2 path:/SD/rustedWarfare/maps\南京保卫战30p2_map.png
2023-05-01 17:38:30.270: FileLoader: convertAbstractPath2: Changing to:/SD/mods/maps\南京保卫战30p2_map.png
2023-05-01 17:38:30.427: FileLoader: Path changed to maps2 path:/SD/rustedWarfare/maps\南京保卫战30p2_map.png
2023-05-01 17:38:30.427: FileLoader: convertAbstractPath2: Changing to:/SD/mods/maps\南京保卫战30p2_map.png
2023-05-01 17:38:30.699: FileLoader: Path changed to maps2 path:/SD/rustedWarfare/maps\南京保卫战30p2_map.png
2023-05-01 17:38:30.699: FileLoader: convertAbstractPath2: Changing to:/SD/mods/maps\南京保卫战30p2_map.png
2023-05-01 17:38:30.814: FileLoader: Path changed to maps2 path:/SD/rustedWarfare/maps\南京保卫战30p2_map.png
2023-05-01 17:38:30.814: FileLoader: convertAbstractPath2: Changing to:/SD/mods/maps\南京保卫战30p2_map.png
2023-05-01 17:38:30.958: FileLoader: Path changed to maps2 path:/SD/rustedWarfare/maps\南京保卫战30p2_map.png
2023-05-01 17:38:30.958: FileLoader: convertAbstractPath2: Changing to:/SD/mods/maps\南京保卫战30p2_map.png
2023-05-01 17:38:31.116: FileLoader: Path changed to maps2 path:/SD/rustedWarfare/maps\南京保卫战30p2_map.png
2023-05-01 17:38:31.117: FileLoader: convertAbstractPath2: Changing to:/SD/mods/maps\南京保卫战30p2_map.png
2023-05-01 17:38:31.256: FileLoader: Path changed to maps2 path:/SD/rustedWarfare/maps\南京保卫战30p2_map.png
2023-05-01 17:38:31.256: FileLoader: convertAbstractPath2: Changing to:/SD/mods/maps\南京保卫战30p2_map.png
2023-05-01 17:38:32.240: FileLoader: Path changed to maps2 path:/SD/rustedWarfare/maps\南京保卫战30p2_map.png
2023-05-01 17:38:32.240: FileLoader: convertAbstractPath2: Changing to:/SD/mods/maps\南京保卫战30p2_map.png
2023-05-01 17:38:32.289: FileLoader: Path changed to maps2 path:/SD/rustedWarfare/maps\南京保卫战30p2_map.png
2023-05-01 17:38:32.290: FileLoader: convertAbstractPath2: Changing to:/SD/mods/maps\南京保卫战30p2_map.png
2023-05-01 17:38:32.298: FileLoader: Path changed to maps2 path:/SD/rustedWarfare/maps\南京保卫战30p2_map.png
2023-05-01 17:38:32.299: FileLoader: convertAbstractPath2: Changing to:/SD/mods/maps\南京保卫战30p2_map.png
2023-05-01 17:38:32.632: Receiving network data: 10872/186491
2023-05-01 17:38:32.649: Receiving network data: 21752/186491
2023-05-01 17:38:32.655: Receiving network data: 27192/186491
2023-05-01 17:38:32.659: Receiving network data: 35352/186491
2023-05-01 17:38:32.664: Receiving network data: 43512/186491
2023-05-01 17:38:32.667: Receiving network data: 48952/186491
2023-05-01 17:38:32.672: Receiving network data: 59832/186491
2023-05-01 17:38:32.676: Receiving network data: 59832/186491
2023-05-01 17:38:32.679: Receiving network data: 59832/186491
2023-05-01 17:38:32.682: Receiving network data: 59832/186491
2023-05-01 17:38:32.686: Receiving network data: 59832/186491
2023-05-01 17:38:32.689: Receiving network data: 59832/186491
2023-05-01 17:38:32.692: Receiving network data: 59832/186491
2023-05-01 17:38:32.695: Receiving network data: 59832/186491
2023-05-01 17:38:32.701: Receiving network data: 59832/186491
2023-05-01 17:38:32.704: Receiving network data: 59832/186491
2023-05-01 17:38:32.707: Receiving network data: 59832/186491
2023-05-01 17:38:32.710: Receiving network data: 59832/186491
2023-05-01 17:38:32.713: Receiving network data: 59832/186491
java.net.SocketException: Connection reset
at java.base/sun.nio.ch.NioSocketImpl.implRead(NioSocketImpl.java:324)
at java.base/sun.nio.ch.NioSocketImpl.read(NioSocketImpl.java:351)
2023-05-01 17:38:32.719: Receiving network data: 59832/186491
at java.base/sun.nio.ch.NioSocketImpl$1.read(NioSocketImpl.java:802)
at java.base/java.net.Socket$SocketInputStream.read(Socket.java:919)
at java.base/java.io.DataInputStream.read(DataInputStream.java:149)
at com.corrodinggames.rts.gameFramework.j.d.a(SourceFile:783)
at com.corrodinggames.rts.gameFramework.j.d.run(SourceFile:683)
at java.base/java.lang.Thread.run(Thread.java:830)
java.net.SocketException: Connection reset by peer
2023-05-01 17:38:32.722: id:1: network:ReceiveWorker: Connection reset
at java.base/sun.nio.ch.NioSocketImpl.implWrite(NioSocketImpl.java:421)
2023-05-01 17:38:32.723: Receiving network data: 59832/186491
at java.base/sun.nio.ch.NioSocketImpl.write(NioSocketImpl.java:441)
2023-05-01 17:38:32.723: id:1: handleRemoteDisconnect
at java.base/sun.nio.ch.NioSocketImpl$2.write(NioSocketImpl.java:825)
2023-05-01 17:38:32.725: reportProblem:The server disconnected  (Time out)
at java.base/java.net.Socket$SocketOutputStream.write(Socket.java:989)
at java.base/java.io.BufferedOutputStream.flushBuffer(BufferedOutputStream.java:81)
at java.base/java.io.BufferedOutputStream.flush(BufferedOutputStream.java:142)
at java.base/java.io.DataOutputStream.flush(DataOutputStream.java:123)
at com.corrodinggames.rts.gameFramework.j.e.run(SourceFile:994)
at java.base/java.lang.Thread.run(Thread.java:830)
2023-05-01 17:38:32.730: network:SendWorker:Connection reset by peer
2023-05-01 17:38:32.739: id:1: handleRemoteDisconnect: connection is already disconnecting
 */