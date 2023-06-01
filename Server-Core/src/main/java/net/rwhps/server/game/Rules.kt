/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */
package net.rwhps.server.game

import net.rwhps.server.core.thread.CallTimeTask
import net.rwhps.server.core.thread.Threads
import net.rwhps.server.core.thread.Threads.newTimedTask
import net.rwhps.server.data.base.BaseCoreConfig
import net.rwhps.server.data.base.BaseServerConfig
import net.rwhps.server.data.event.GameOverData
import net.rwhps.server.data.global.Data
import net.rwhps.server.data.global.NetStaticData
import net.rwhps.server.data.player.PlayerManage
import net.rwhps.server.game.GameMaps.MapData
import net.rwhps.server.io.packet.GameCommandPacket
import net.rwhps.server.struct.OrderedMap
import net.rwhps.server.struct.Seq
import net.rwhps.server.util.IsUtil.notIsBlank
import net.rwhps.server.util.Time
import net.rwhps.server.util.algorithms.Base64.decodeString
import net.rwhps.server.util.algorithms.Base64.isBase64
import net.rwhps.server.util.algorithms.digest.DigestUtil.sha256
import net.rwhps.server.util.compression.CompressionDecoderUtils
import net.rwhps.server.util.file.FileUtil.Companion.getFolder
import net.rwhps.server.util.log.Log.error
import java.io.File
import java.math.BigInteger
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author RW-HPS/Dr
 */
class Rules(private var config: BaseCoreConfig, private var configServer: BaseServerConfig) {
    /** End Time */
    var endTime = 0
        private set
    var startTime = 0
        private set
    /** 是否已启动游戏  */
    @Volatile
    var isStartGame = false
        set(value) {
            field = value
            isGameover = value
            startTime = Time.concurrentSecond()
            endTime = Time.concurrentSecond()+Data.configServer.MaxGameIngTime
        }

    /** 倍数  */
    var income: Float

    /** 初始钱  */
    var credits = 0

    /** 最大玩家  */
    var maxPlayer: Int
        private set

    /** 地图数据  */
    val maps = GameMaps()

    /** nukes  */
    var noNukes = false

    /** 初始单位  */
    var initUnit = 1

    /** 迷雾  */
    var mist = 2

    /** 共享控制  */
    var sharedControl = false

    /** 密码  */
    @JvmField
    val passwd: String = if (notIsBlank(Data.configServer.Passwd)) BigInteger(1, sha256(Data.configServer.Passwd)).toString(16).uppercase() else ""

    /** 按键包缓存  */
    val gameCommandCache = Seq<GameCommandPacket>(16,true)



    /** 重连暂停  */
    @Volatile
    var gameReConnectPaused = false
    /** 游戏暂停  */
    @Volatile
    var gamePaused = false

    /** PlayerManage  */
    var playerManage: PlayerManage
        private set

    /** AFK  */
    var isAfk: Boolean = true
    /** Mpa Lock  */
    var mapLock: Boolean = false
    var battleRoyalLock: Boolean = false
        set(value) {
            playerManage.amTeam = battleRoyalLock
            field = value
        }

    /* */
    var lockTeam = false
    val mapsData = OrderedMap<String, MapData>(8)

    val tickGame = AtomicInteger(6)
    var isGameover = false

    var replayName: String = ""
        set(value) {
            field = value.trim()
        }
    var gameOverData: GameOverData? = null

    init {
//        try {
//            checkMaps()
//            clog(Data.i18NBundle.getinput("server.load.maps"))
//        } catch (exp: Exception) {
//            debug("Read Error", exp)
//        }
        NetStaticData.relay.isMod = config.SingleUserRelayMod
        autoLoadOrUpdate(config)
        val maxPlayer = configServer.MaxPlayer+1
        this.maxPlayer = maxPlayer
        playerManage = PlayerManage(maxPlayer)
        income = configServer.DefIncome

//        if (maxPlayer > 100) {
//            Log.skipping("[WARN !]","The number of players is too large and the game cannot be played normally")
//        }
    }

    fun init() {
        isAfk = Data.core.settings.getData("Rules.IsAfk") { isAfk }
        mapLock = Data.core.settings.getData("Rules.MapLock") { mapLock }
        battleRoyalLock = Data.core.settings.getData("Rules.DogfightLock") { battleRoyalLock }


        Threads.addSavePool {
            Data.core.settings.setData("Rules.IsAfk",isAfk)
            Data.core.settings.setData("Rules.MapLock",mapLock)
            Data.core.settings.setData("Rules.DogfightLock",battleRoyalLock)
        }
    }

    fun re() {
        this.config = Data.config

        gameCommandCache.clear()
        playerManage.cleanPlayerAllData()
        // 重置Tick
        tickGame.set(10)

        income = configServer.DefIncome
        val maxPlayer = configServer.MaxPlayer+1
        this.maxPlayer = maxPlayer
        playerManage = PlayerManage(maxPlayer)

        initUnit = 1
        mist = 2
        sharedControl = false
        lockTeam = false
        playerManage.amTeam = battleRoyalLock

        gameReConnectPaused = false
        gamePaused = false

        gameOverData = null
    }

    fun checkMaps() {
        val list = getFolder(Data.Plugin_Maps_Path).fileListNotNullSizeSort
        list.eachAll { e: File ->
            val original = if (isBase64(e.name)) decodeString(e.name) else e.name
            val postpone = original.substring(original.lastIndexOf("."))
            val name = original.substring(0, original.length - postpone.length)
            when (postpone) {
                ".tmx" ->   try {
                                mapsData.put(name, MapData(GameMaps.MapType.customMap, GameMaps.MapFileType.file, name))
                            } catch (exception: Exception) {
                                error("read tmx Maps", exception)
                            }
                ".save" ->  try {
                                mapsData.put(name, MapData(GameMaps.MapType.savedGames, GameMaps.MapFileType.file, name))
                            } catch (exception: Exception) {
                                error("read save Maps", exception)
                            }
                ".zip" ->   try {
                                CompressionDecoderUtils.zip(e).use {
                                    val zipTmx = it.getTheFileNameOfTheSpecifiedSuffixInTheZip("tmx")
                                    zipTmx.eachAll { zipMapName: String -> mapsData.put(zipMapName, MapData(GameMaps.MapType.customMap, GameMaps.MapFileType.zip, zipMapName, original)) }
                                    val zipSave = it.getTheFileNameOfTheSpecifiedSuffixInTheZip("save")
                                    zipSave.eachAll { zipSaveName: String -> mapsData.put(zipSaveName, MapData(GameMaps.MapType.savedGames, GameMaps.MapFileType.zip, zipSaveName, original)) }
                                }
                            } catch (exception: Exception) {
                                error("ZIP READ", exception)
                            }
                else -> {}
            }
        }
    }

    private fun autoLoadOrUpdate(config: BaseCoreConfig) {
        if (config.AutoReLoadMap) {
            newTimedTask(CallTimeTask.AutoUpdateMapsTask, 0, 1, TimeUnit.MINUTES){
                if (notIsBlank(Data.game) && !Data.game.isStartGame) {
                    Data.game.mapsData.clear()
                    Data.game.checkMaps()
                }
            }
        }
    }
}