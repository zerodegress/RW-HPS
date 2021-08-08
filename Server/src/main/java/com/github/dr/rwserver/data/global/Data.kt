package com.github.dr.rwserver.data.global

import com.github.dr.rwserver.command.ex.Vote
import com.github.dr.rwserver.core.Application
import com.github.dr.rwserver.data.Player
import com.github.dr.rwserver.game.Rules
import com.github.dr.rwserver.struct.ObjectMap
import com.github.dr.rwserver.struct.Seq
import com.github.dr.rwserver.util.LocaleUtil
import com.github.dr.rwserver.util.file.LoadConfig
import com.github.dr.rwserver.util.game.CommandHandler
import com.github.dr.rwserver.util.zip.gzip.GzipEncoder.Companion.getGzipStream
import io.netty.channel.Channel
import java.nio.charset.Charset
import java.nio.charset.StandardCharsets

/**
 * @author Dr
 */
object Data {
    const val Plugin_Data_Path = "/data"
    const val Plugin_Save_Path = "/data/save"
    const val Plugin_Cache_Path = "/data/cache"
    const val Plugin_Lib_Path = "/data/lib"
    const val Plugin_Log_Path = "/data/log"
    const val Plugin_Maps_Path = "/data/maps"
    const val Plugin_Plugins_Path = "/data/plugins"
    val UTF_8: Charset = StandardCharsets.UTF_8
    /*
	 * 插件默认变量
	 */
    /** 自定义包名  */
    const val SERVER_ID = "com.github.dr.rwserver"
    const val SERVER_CORE_VERSION = "1.4.0-M1"
    /** 单位数据缓存  */
	@JvmField
	val utilData = getGzipStream("customUnits", false)

    /**  */
    const val SERVER_MAX_TRY = 3
    @JvmField
	val LINE_SEPARATOR: String = System.getProperty("line.separator")

    /** 服务端 客户端命令  */
	@JvmField
	val SERVERCOMMAND = CommandHandler("")
    @JvmField
	val LOGCOMMAND = CommandHandler("!")
    @JvmField
	val CLIENTCOMMAND = CommandHandler("/")

    /**  */
	@JvmField
	val MapsMap: Map<String, String> = HashMap()

    /** 在线玩家  */
	@JvmField
	val playerGroup = Seq<Player>(16)

    /** ALL  */
	@JvmField
	val playerAll = Seq<Player>(16)
    @JvmField
	val core = Application()
    @JvmField
	val localeUtilMap = ObjectMap<String, LocaleUtil>(8)

    lateinit var config: LoadConfig
    //lateinit var ip2Location: IP2Location

    /**
     * 可控变量
     */
    lateinit var localeUtil: LocaleUtil
    lateinit var game: Rules
    lateinit var Vote: Vote
    @JvmField
	var serverChannelB: Channel? = null
}