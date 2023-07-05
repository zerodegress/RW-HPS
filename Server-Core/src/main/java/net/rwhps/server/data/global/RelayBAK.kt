///*
// * Copyright 2020-2023 RW-HPS Team and contributors.
// *
// * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
// * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
// *
// * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
// */
//
//package net.rwhps.server.data.global
//
//import net.rwhps.server.data.global.Data.LINE_SEPARATOR
//import net.rwhps.server.math.Rand
//import net.rwhps.server.net.GroupNet
//import net.rwhps.server.net.core.DataPermissionStatus
//import net.rwhps.server.util.algorithms.NetConnectProofOfWork
//import net.rwhps.server.net.netconnectprotocol.realize.GameVersionRelay
//import net.rwhps.server.struct.IntMap
//import net.rwhps.server.struct.IntSeq
//import net.rwhps.server.struct.Seq
//import net.rwhps.server.util.IsUtil.isNumeric
//import net.rwhps.server.util.IsUtil.notIsBlank
//import net.rwhps.server.util.RandomUtil.getRandomString
//import net.rwhps.server.util.StringFilteringUtil.cutting
//import net.rwhps.server.util.Time
//import net.rwhps.server.util.Time.concurrentSecond
//import net.rwhps.server.util.Time.utcMillis
//import net.rwhps.server.util.algorithms.digest.DigestUtil.md5Hex
//import net.rwhps.server.util.algorithms.digest.DigestUtil.sha256
//import net.rwhps.server.util.log.Log
//import net.rwhps.server.util.log.Log.debug
//import net.rwhps.server.util.pool.ProxyPool.ProxyData
//import net.rwhps.server.util.threads.ServerUploadData
//import java.io.IOException
//import java.math.BigInteger
//import java.util.*
//import java.util.concurrent.atomic.AtomicInteger
//import java.util.concurrent.locks.ReentrantLock
//import java.util.function.Consumer
//import kotlin.concurrent.withLock
//
//class RelayBAK {
//    /**  */
//    @JvmField
//    val groupNet: GroupNet
//    val abstractNetConnectIntMap = IntMap<GameVersionRelay>(10,true)
//
//
//    @Volatile
//    var admin: GameVersionRelay? = null
//        set(value) {
//            field = value
//            value?.permissionStatus = DataPermissionStatus.RelayStatus.HostPermission
//        }
//
//    @Volatile
//    var closeRoom = false
//
//    internal val relayData: RelayData
//
//    val roomCreateTime = Time.concurrentSecond()
//
//   // val serverUuid = UUID.randomUUID().toString()
//    val serverUuid = Data.SERVER_RELAY_UUID
//    val internalID : Int
//    val id: String
//    var isMod = false
//    var minSize = 1
//        private set
//    var isStartGame: Boolean = false
//        set(value) {
//            if (field && value) {
//                return
//            }
//            field = value
//            if (value && relayData.uplistStatus == RelayData.UpListStatus.UpIng) {
//                ServerUploadData.sendPostRM(internalID)
//            }
//            startGameTime = Time.concurrentSecond()+300
//        }
//    var startGameTime = 0
//        private set
//
//    var allmute = false
//    var dogfightLock: Boolean = false
//
//    private val site = AtomicInteger(0)
//    private val size = AtomicInteger()
//
//    private constructor(internalID : Int, id: String, playerName: String, isMod: Boolean, betaGameVersion: Boolean, version: Int, maxPlayer: Int) {
//        serverRelayData.put(internalID, this)
//        this.internalID = internalID
//        this.id = id
//        groupNet = GroupNet()
//        this.isMod = isMod
//        relayData = RelayData(internalID, this, playerName, betaGameVersion, version, maxPlayer)
//    }
//
//    /**
//     * 仅供内部使用
//     * 作为单房间实例化
//     * @param id String
//     * @param groupNet GroupNet
//     * @constructor
//     */
//    internal constructor(id: String, groupNet: GroupNet) {
//        this.internalID = 0
//        this.id = id
//        this.groupNet = groupNet
//        relayData = RelayData(0, this,"", false, 0,0)
//    }
//
//    val allIP: String
//        get() {
//            val str = StringBuilder(10)
//            str.append(LINE_SEPARATOR)
//                .append(admin!!.name)
//                .append(" / ")
//                .append("IP: ").append(admin!!.ip)
//                .append(" / ")
//                .append("Protocol: ").append(admin!!.useConnectionAgreement)
//                .append(" / ")
//                .append("Admin: true")
//            abstractNetConnectIntMap.values.forEach(Consumer { e: GameVersionRelay ->
//                str.append(LINE_SEPARATOR)
//                    .append(e.name)
//                    .append(" / ")
//                    .append("IP: ").append(e.ip)
//                    .append(" / ")
//                    .append("Protocol: ").append(e.useConnectionAgreement)
//                    .append(" / ")
//                    .append("Admin: false")
//            })
//            return str.toString()
//        }
//
//    fun re() {
//        removeRoom() {
//            if (relayData.uplistStatus == RelayData.UpListStatus.UpIng) {
//                ServerUploadData.sendPostRM(internalID)
//            }
//        }
//    }
//
//    fun removeRoom(run: ()->Unit = {}) {
//        try {
//            groupNet.disconnect()
//            admin?.disconnect()
//            abstractNetConnectIntMap.values.forEach { it.disconnect() }
//            closeRoom = true
//            abstractNetConnectIntMap.clear()
//            site.set(0)
//            admin = null
//            isStartGame = false
//        } catch (e: Exception) {
//        } finally {
//            serverRelayData.remove(internalID)
//            run()
//        }
//    }
//
//    fun getAbstractNetConnect(site: Int): GameVersionRelay? {
//        return abstractNetConnectIntMap[site]
//    }
//
//    fun setAbstractNetConnect(abstractNetConnect: GameVersionRelay): GameVersionRelay? {
//        return abstractNetConnectIntMap.put(site.get(), abstractNetConnect)
//    }
//
//    fun sendMsg(msg: String) {
//        try {
//            admin!!.sendPacket(NetStaticData.RwHps.abstractNetPacket.getSystemMessagePacket(msg))
//            groupNet.broadcastAndUDP(NetStaticData.RwHps.abstractNetPacket.getSystemMessagePacket(msg))
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }
//
//    fun sendMsg(msg: String, msgName: String, team: Int) {
//        try {
//            admin!!.sendPacket(NetStaticData.RwHps.abstractNetPacket.getChatMessagePacket(msg,msgName,team))
//            groupNet.broadcastAndUDP(NetStaticData.RwHps.abstractNetPacket.getChatMessagePacket(msg,msgName,team))
//        } catch (e: IOException) {
//            e.printStackTrace()
//        }
//    }
//
//    fun removeAbstractNetConnect(site: Int) {
//        abstractNetConnectIntMap.remove(site)
//    }
//
//    fun getActiveConnectionSize(): Int {
//        return abstractNetConnectIntMap.size
//    }
//
//    fun getPosition(): Int {
//        return site.get()
//    }
//
//    fun setAddPosition() {
//        site.incrementAndGet()
//    }
//
//    fun setRemovePosition() {
//        site.decrementAndGet()
//    }
//
//    fun getSize(): Int {
//        return size.get()
//    }
//
//    fun setAddSize() {
//        size.incrementAndGet()
//    }
//
//    fun setRemoveSize() {
//        size.decrementAndGet()
//    }
//
//    fun setRelayDataMapName(name: String) {
//        relayData.mapName = name
//    }
//
//    fun updateMinSize() {
//        try {
//            minSize = abstractNetConnectIntMap.toArrayKey().toIntArray().min()
//        } catch (e: Exception) {
//            Log.error("[RELAY updateMinSize]",e)
//        }
//    }
//
//    override fun equals(other: Any?): Boolean {
//        if (this === other) {
//            return true
//        }
//        if (other == null || javaClass != other.javaClass) {
//            return false
//        }
//        val relay = other as RelayBAK
//        return internalID == coverRelayID(relay.id)
//    }
//
//    override fun hashCode(): Int {
//        return Objects.hash(id)
//    }
//
//    internal class RelayData(
//        val id: Int,
//        val relay: RelayBAK,
//        playerName: String,
//        betaGameVersion: Boolean,
//        private val version: Int,
//        maxPlayer: Int,
//    ) {
//        private val name: String
//
//        var mapName = "RelayCN-MapName"
//            set(value) {
//                field = value.replace("&","")
//            }
//        @JvmField
//        var port = 0
//        var proxyData: ProxyData? = null
//        private val PlayerMaxSzie = maxPlayer
//        @Volatile
//        var makeTime = 0L
//            private set
//        @Volatile
//        var uplistStatus = UpListStatus.NoUp
//
//        var result = false
//        private val betaGameVersion: Boolean
//        private val serverToken = getRandomString(40)
//        private val userId = "u_" + UUID.randomUUID()
//
//        //5200-5500
//        init {
//            name = playerName.replace("&","").ifBlank { "NOT Player" }
//            this.betaGameVersion = betaGameVersion
//
//        }
//
//        fun up(): Boolean {
//            if (uplistStatus == UpListStatus.UpIng) {
//                return false
//            }
//            makeTime = concurrentSecond().toLong()
//
//            proxyData = NetStaticData.httpSynchronize.proxyData
//
//            return if (notIsBlank(proxyData)) {
//                while (true) {
//                    // 端口段
//                    val randInt = rand.random(5250, 5500)
//                    if (!portUsed.contains(randInt)) {
//                        port = randInt
//                        break
//                    }
//                }
//                debug("RELAY DATA", port)
//                ServerUploadData.registerRelayData(id, this)
//                portUsed.add(port)
//                uplistStatus = UpListStatus.UpIng
//                true
//            } else {
//                false
//            }
//        }
//
//        /*
//        val add: FormBody.Builder
//            get() {
//                val time = utcMillis
//                return FormBody.Builder()
//                    .add("action","add")
//                    .add("user_id",userId)
//                    .add("game_name","RW-HPS")
//                    .add("_1","$time")
//                    .add("tx2",reup("_" + userId + 5))
//                    .add("tx3",reup("_" + userId + (5 + time)))
//                    .add("game_version=",
//                        if (betaGameVersion) "160" else "151")
//                    .add("game_version_string",
//                        if (betaGameVersion) if (relay.isMod) "1.15.P-RN-MOD" else "1.15.P-RN" else if (relay.isMod) "1.14-RN-MOD" else "1.14-RN")
//                    .add("game_version_beta","$betaGameVersion")
//                    .add("private_token",serverToken)
//                    .add("private_token_2",md5Hex(md5Hex(serverToken)))
//                    .add("confirm",md5Hex("a" + md5Hex(serverToken)))
//                    .add("password_required","false")
//                    .add("created_by",Name)
//                    .add("private_ip","10.0.0.1")
//                    .add("port_number","$port")
//                    .add("game_map",MapName)
//                    .add("game_mode","skirmishMap")
//                    .add("game_status","battleroom")
//                    .add("player_count","${relay.getSize()}")
//                    .add("max_player_count","$PlayerMaxSzie")
//            }
//        val up: FormBody.Builder
//            get() {
//                var stat = "battleroom"
//                if (relay.isStartGame) {
//                    stat = "ingame"
//                }
//
//                return FormBody.Builder()
//                    .add("action","update")
//                    .add("id",userId)
//                    .add("game_name","RW-HPS")
//                    .add("private_token",serverToken)
//                    .add("password_required","false")
//                    .add("created_by",Name)
//                    .add("private_ip","127.0.0.1")
//                    .add("port_number","$port")
//                    .add("game_map",MapName)
//                    .add("game_mode","skirmishMap")
//                    .add("game_status",stat)
//                    .add("player_count","${relay.getSize()}")
//                    .add("max_player_count","$PlayerMaxSzie")
//            }
//        val portCheck: FormBody.Builder
//            get() {
//                return FormBody.Builder()
//                    .add("action","self_info")
//                    .add("port","$port")
//                    .add("id",userId)
//                    .add("tx3",reup("-" + userId + "54"))
//            }
//        val rmList: FormBody.Builder
//            get() {
//                portUsed.removeValue(port)
//                return FormBody.Builder()
//                    .add("action","remove")
//                    .add("id",userId)
//                    .add("private_token",serverToken)
//            }
//        */
//
//        val add: String
//            get() {
//                val sb = StringBuilder()
//                val time = utcMillis
//                sb.append("action=add")
//                    .append("&user_id=").append(userId)
//                    .append("&game_name=RW-HPS")
//                    .append("&_1=").append(time)
//                    .append("&tx2=").append(reup("_" + userId + 5))
//                    .append("&tx3=").append(reup("_" + userId + (5 + time)))
//                    .append("&game_version=").append(version)
//                    .append("&game_version_string=")
//                    .append(
//                        if(version == 176) {
//                            if (relay.isMod) "1.15-RN-MOD" else "1.15-RN"
//                        } else (if (betaGameVersion) {
//                            if (relay.isMod) "1.15.P*-RN-MOD" else "1.15.P*-RN"
//                        } else if (relay.isMod) "1.14-RN-MOD" else "1.14-RN"))
//                    .append("&game_version_beta=").append(betaGameVersion)
//                    .append("&private_token=").append(serverToken)
//                    .append("&private_token_2=").append(md5Hex(md5Hex(serverToken)))
//                    .append("&confirm=").append(md5Hex("a" + md5Hex(serverToken)))
//                    .append("&password_required=").append(false)
//                    .append("&created_by=").append(cutting(name,15))
//                    .append("&private_ip=10.0.0.1")
//                    .append("&port_number=").append(port)
//                    .append("&game_map=").append(mapName)
//                    .append("&game_mode=skirmishMap")
//                    .append("&game_status=battleroom")
//                    .append("&player_count=").append(relay.getSize())
//                    .append("&max_player_count=").append(PlayerMaxSzie)
//                return sb.toString()
//            }
//        val up: String
//            get() {
//                var stat = "battleroom"
//                if (relay.isStartGame) {
//                    stat = "ingame"
//                }
//                val sb = StringBuilder()
//                sb.append("action=update")
//                    .append("&id=").append(userId)
//                    .append("&game_name=RW-HPS")
//                    .append("&private_token=").append(serverToken)
//                    .append("&password_required=").append(false)
//                    .append("&created_by=").append(cutting(name,15))
//                    .append("&private_ip=127.0.0.1")
//                    .append("&port_number=").append(port)
//                    .append("&game_map=").append(mapName)
//                    .append("&game_mode=skirmishMap")
//                    .append("&game_status=").append(stat)
//                    .append("&player_count=").append(relay.getSize())
//                    .append("&max_player_count=").append(PlayerMaxSzie)
//                return sb.toString()
//            }
//        val portCheck: String
//            get() = "action=self_info&port=" + port + "&id=" + userId + "&tx3=" + reup("-" + userId + "54")
//
//        val rmList: String
//            get() {
//                portUsed.removeValue(port)
//                val sb = StringBuilder()
//                sb.append("action=remove")
//                    .append("&id=").append(userId)
//                    .append("&private_token=").append(serverToken)
//                return sb.toString()
//            }
//
//        override fun equals(other: Any?): Boolean {
//            if (this === other) {
//                return true
//            }
//            if (other == null || javaClass != other.javaClass) {
//                return false
//            }
//            val relay = other as RelayData
//            return id == relay.id
//        }
//
//        override fun hashCode(): Int {
//            return Objects.hash(id)
//        }
//
//        companion object {
//            val portUsed = IntSeq()
//            private val rand = Rand()
//
//            private fun reup(str: String): String {
//                return cutting(BigInteger(1, sha256(str)).toString(16).uppercase(), 4)
//            }
//        }
//
//        enum class UpListStatus {
//            NoUp,UpEnd,UpIng;
//        }
//    }
//
//    companion object {
//        val serverRelayIpData = Seq<String>(true)
//
//        private val serverRelayData = IntMap<RelayBAK>(128,true)
//        private val rand = Rand()
//
//        private val getRelayData = ReentrantLock(true)
//
//        @JvmStatic
//        val relayAllIP: String
//            get() {
//                val str = StringBuilder(10)
//                serverRelayData.values.forEach(Consumer { e: RelayBAK ->
//                    str.append(LINE_SEPARATOR)
//                        .append(e.id)
//                        .append(e.allIP)
//                })
//                return str.toString()
//            }
//
//        val allSize: Int
//            get() {
//                val size = AtomicInteger()
//                serverRelayData.values.forEach(Consumer { e: RelayBAK -> size.getAndAdd(e.getSize()) })
//                return size.get()
//            }
//        val roomAllSize: Int
//            get() {
//                return serverRelayData.size
//            }
//        val roomNoStartSize: Int
//            get() {
//                val size = AtomicInteger()
//                serverRelayData.values.forEach(Consumer { e: RelayBAK -> if (!e.isStartGame) size.incrementAndGet() })
//                return size.get()
//            }
//        val roomPublicSize: Int
//            get() {
//                return RelayData.portUsed.size
//            }
//
//        @get:Synchronized
//        internal val randPow: NetConnectProofOfWork
//            get() { return NetConnectProofOfWork() }
//
//        @JvmStatic
//        fun getRelay(id: String): RelayBAK? = getRelayData.withLock { serverRelayData[coverRelayID(id)] }
//
//        @JvmStatic
//        fun getCheckRelay(id: String): Boolean = getRelayData.withLock { serverRelayData.containsKey(coverRelayID(id)) }
//
//        @JvmStatic
//        fun sendAllMsg(msg: String) {
//            serverRelayData.values.forEach(Consumer { e: RelayBAK -> e.sendMsg(msg) })
//        }
//
//        @JvmStatic
//        fun getAllRelayIpCountry(): Map<String,Int> {
//            return mutableMapOf<String, Int>().also {
//                mutableMapOf<String, AtomicInteger>().also { temp ->
//                    Relay.serverRelayData.values.forEach { relay ->
//                        temp.computeIfAbsent(relay.admin!!.ipCountry) { AtomicInteger(0) }.incrementAndGet()
//                        relay.abstractNetConnectIntMap.values.forEach { connect ->
//                            temp.computeIfAbsent(connect.ipCountry) { AtomicInteger(0) }.incrementAndGet()
//                        }
//                    }
//                }.forEach { (country, count) ->
//                    it[country] = count.get()
//                }
//            }
//        }
//
//        @JvmStatic
//        fun getAllRelayVersion(): Map<Int,Int> {
//            return mutableMapOf<Int, Int>().also {
//                mutableMapOf<Int, AtomicInteger>().also { temp ->
//                    Relay.serverRelayData.values.forEach { relay ->
//                        temp.computeIfAbsent(relay.admin!!.clientVersion) { AtomicInteger(0) }.incrementAndGet()
//                        relay.abstractNetConnectIntMap.values.forEach { connect ->
//                            temp.computeIfAbsent(connect.clientVersion) { AtomicInteger(0) }.incrementAndGet()
//                        }
//                    }
//                }.forEach { (version, count) ->
//                    it[version] = count.get()
//                }
//            }
//        }
//
//        internal fun cleanRoom() {
//            val time = concurrentSecond() - 12 * 60 * 60
//            serverRelayData.forEach {
//                if (it.value.roomCreateTime < time) {
//                    it.value.removeRoom()
//                }
//            }
//        }
//
//        internal fun coverRelayID(id: String): Int {
//            return if (isNumeric(id) && id.length < 10) {
//                id.toInt()
//            } else {
//                id.hashCode().let {
//                    return if (it > 0) {
//                        -it
//                    } else {
//                        it
//                    }
//                }
//            }
//        }
//
//        /**
//         * This method should not be open to the public for internal use only
//         * @param id                Custom ID left blank self generated
//         * @param playerName        Player name data
//         * @param isMod             Whether mods room
//         * @param betaGameVersion   Is it a beta version
//         * @param maxPlayer         Maximum number of people
//         * @return Relay
//         */
//        @Synchronized
//        internal fun getRelay(id: String = "", playerName: String, isMod: Boolean, betaGameVersion: Boolean, version: Int, maxPlayer: Int): RelayBAK {
//            var idRelay = id
//            val relay: RelayBAK =  if (id.isBlank()) {
//                                    while (true) {
//                                        val intId = rand.random(1000, 100000)
//                                        if (!getCheckRelay(intId.toString())) {
//                                            idRelay = intId.toString()
//                                            debug(intId)
//                                            break
//                                        }
//                                    }
//                                    RelayBAK(coverRelayID(idRelay),idRelay,playerName,isMod,betaGameVersion,version,maxPlayer)
//                                } else {
//                                    RelayBAK(coverRelayID(idRelay),idRelay,playerName,isMod,betaGameVersion,version,maxPlayer)
//                                }
//            return relay
//        }
//    }
//}