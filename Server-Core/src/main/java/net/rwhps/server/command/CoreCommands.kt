/*
 * Copyright 2020-2024 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.command

import net.rwhps.server.command.relay.RelayCommands
import net.rwhps.server.core.Core
import net.rwhps.server.core.Initialization
import net.rwhps.server.core.NetServer
import net.rwhps.server.core.thread.Threads
import net.rwhps.server.data.bean.internal.BeanGithubReleasesApi
import net.rwhps.server.data.global.Data
import net.rwhps.server.data.global.NetStaticData
import net.rwhps.server.dependent.HotLoadClass
import net.rwhps.server.func.StrCons
import net.rwhps.server.game.event.global.ServerStartTypeEvent
import net.rwhps.server.game.manage.HeadlessModuleManage
import net.rwhps.server.io.output.DisableSyncByteArrayOutputStream
import net.rwhps.server.net.HttpRequestOkHttp
import net.rwhps.server.net.NetService
import net.rwhps.server.net.core.IRwHps
import net.rwhps.server.net.handler.tcp.StartHttp
import net.rwhps.server.plugin.GetVersion
import net.rwhps.server.plugin.PluginLoadData
import net.rwhps.server.plugin.center.PluginCenter
import net.rwhps.server.struct.list.Seq
import net.rwhps.server.util.SystemUtils
import net.rwhps.server.util.alone.DiffUpdate
import net.rwhps.server.util.annotations.NeedHelp
import net.rwhps.server.util.file.FileUtils
import net.rwhps.server.util.file.plugin.PluginManage
import net.rwhps.server.util.game.command.CommandHandler
import net.rwhps.server.util.inline.toGson
import net.rwhps.server.util.log.Log
import java.util.*
import java.util.concurrent.atomic.AtomicInteger

/**
 * @author Dr (dr@der.kim)
 */
class CoreCommands(handler: CommandHandler) {
    private fun registerCore(handler: CommandHandler) {
        handler.register("help", "serverCommands.help") { _: Array<String>?, log: StrCons ->
            log("Commands:")
            for (command in handler.commandList) {
                if (command.description.startsWith("#")) {
                    log(
                            "   " + command.text + (if (command.paramText.isEmpty()) "" else " ") + command.paramText + " - " + command.description.substring(
                                    1
                            )
                    )
                } else {
                    if ("HIDE" == command.description) {
                        continue
                    }
                    log(
                            "   " + command.text + (if (command.paramText.isEmpty()) "" else " ") + command.paramText + " - " + Data.i18NBundle.getinput(
                                    command.description
                            )
                    )
                }
            }
        }

        @NeedHelp handler.register("stop", "HIDE") { _: Array<String>?, log: StrCons ->
            if (NetStaticData.netService.size == 0) {
                log("Server does not start")
                return@register
            }
            NetServer.closeServer()
        }

        handler.register("version", "serverCommands.version") { _: Array<String>?, log: StrCons ->
            log(
                    localeUtil.getinput(
                            "status.versionS",
                            SystemUtils.javaHeap / 1024 / 1024,
                            Data.SERVER_CORE_VERSION,
                            NetStaticData.ServerNetType.name
                    )
            )
            if (NetStaticData.ServerNetType.ordinal in IRwHps.NetType.ServerProtocol.ordinal .. IRwHps.NetType.ServerTestProtocol.ordinal) {
                HeadlessModuleManage.hessLoaderMap.values.forEach {
                    log(
                            localeUtil.getinput(
                                    "status.versionS.server",
                                    it.room.maps.mapName,
                                    it.room.playerManage.playerAll.size,
                                    NetStaticData.ServerNetType.name,
                                    it.useClassLoader,
                                    it.room.roomID
                            )
                    )
                }
            } else if (NetStaticData.ServerNetType == IRwHps.NetType.RelayProtocol || NetStaticData.ServerNetType == IRwHps.NetType.RelayMulticastProtocol) {
                val size = AtomicInteger()
                NetStaticData.netService.eachAll { e: NetService -> size.addAndGet(e.getConnectSize()) }
                log(localeUtil.getinput("status.versionS.relay", size.get()))
            }
        }
        handler.register("setlanguage", "[HK/CN/RU/EN]", "serverCommands.setlanguage") { arg: Array<String>, _: StrCons ->
            Initialization.initServerLanguage(Data.core.settings, arg[0])
        }

        handler.register("exit", "serverCommands.exit") { Core.exit() }
    }

    private fun registerInfo(handler: CommandHandler) {
        handler.register("plugins", "serverCommands.plugins") { _: Array<String>?, log: StrCons ->
            PluginManage.run { e: PluginLoadData? ->
                log(localeUtil.getinput("plugin.info", e!!.name, e.description, e.author, e.version))
            }
        }
    }

    private fun registerCorex(handler: CommandHandler) {
        handler.register("plugin", "<TEXT...>", "serverCommands.plugin") { arg: Array<String>, log: StrCons ->
            PluginCenter.pluginCenter.command(arg[0], log)
        }

        handler.register("tryupdate", "serverCommands.tryUpdate") { _: Array<String>, log: StrCons ->
            val jsonAll = Array<BeanGithubReleasesApi>::class.java.toGson(
                    HttpRequestOkHttp.doGet(Data.urlData.readString("Get.Core.Update.AllVersion"))
            )
            val nowVersion = GetVersion(Data.SERVER_CORE_VERSION)

            val updateFunction = fun(update: List<BeanGithubReleasesApi>, beta: Boolean) {
                val sleepUpdate = Seq<BeanGithubReleasesApi.AssetsDTO>()
                update.reversed().forEach {
                    it.assets!!.forEach {
                        if ((!beta && it.name.endsWith(".last.patch")) || (beta && it.name.endsWith(".beta.patch"))) {
                            val out = DisableSyncByteArrayOutputStream()
                            if (!HttpRequestOkHttp.downUrl(it.browserDownloadUrl, out, progressFlag = true)) {
                                Log.error(localeUtil.getinput("err.network.noGithub"))
                                return
                            }
                            it.bytes = out.toByteArray()
                            sleepUpdate.add(it)
                        }
                    }
                }
                var old = FileUtils.getMyCoreJarStream().readAllBytes()

                sleepUpdate.forEach {
                    val new = DisableSyncByteArrayOutputStream()
                    DiffUpdate.patch(old, it.bytes!!, new)
                    Log.clog("Update to ${it.name}")
                    old = new.toByteArray()
                }

                Core.exit() {
                    Log.clog("Use ctrl+c")
                    FileUtils(FileUtils.getMyFilePath()).writeFileByte(old)
                }
            }

            if (Data.config.followBetaVersion) {
                if (nowVersion.getIfVersion("< ${jsonAll[0].tagName}")) {
                    updateFunction(Seq<BeanGithubReleasesApi>().apply {
                        jsonAll.forEach {
                            if (nowVersion.getIfVersion("< ${it.tagName}")) {
                                add(it)
                            }
                        }
                    }, true)
                    return@register
                }
            } else {
                val lastVersion = jsonAll.find { !it.prerelease }!!
                if (nowVersion.getIfVersion("< ${lastVersion.tagName}")) {
                    if (nowVersion.formalEdition) {
                        updateFunction(Seq<BeanGithubReleasesApi>().apply {
                            jsonAll.forEach {
                                if (!it.prerelease && nowVersion.getIfVersion("< ${it.tagName}")) {
                                    add(it)
                                }
                            }
                        }, false)
                    } else {
                        updateFunction(Seq<BeanGithubReleasesApi>().apply {
                            jsonAll.forEach {
                                if (GetVersion(it.tagName).getIfVersion("<= ${lastVersion.tagName}") && nowVersion.getIfVersion(
                                            "< ${it.tagName}"
                                    )) {
                                    add(it)
                                }
                            }
                        }, true)
                    }
                    return@register
                }
            }
            log(localeUtil.getinput("serverCommands.tryUpdate.last"))
        }
    }

    private fun registerStartServer(handler: CommandHandler) {
        handler.register("startrelay", "serverCommands.start") { _: Array<String>?, log: StrCons ->
            if (NetStaticData.netService.size > 0) {
                log("The server is not closed, please close")
                return@register
            }

            /* Register Relay Protocol Command */
            RelayCommands(handler)

            Log.set(Data.config.log.uppercase(Locale.getDefault()))

            NetStaticData.ServerNetType = IRwHps.NetType.RelayProtocol

            handler.handleMessage("startnetservice true 5201 5500") //5200 6500
        }
        handler.register("startrelaytest", "serverCommands.start") { _: Array<String>?, log: StrCons ->
            if (NetStaticData.netService.size > 0) {
                log("The server is not closed, please close")
                return@register
            }

            /* Register Relay Protocol Command */
            RelayCommands(handler)

            Log.set(Data.config.log.uppercase(Locale.getDefault()))

            NetStaticData.ServerNetType = IRwHps.NetType.RelayMulticastProtocol

            handler.handleMessage("startnetservice true 7000 8000")
        }

        handler.register("startnetservice", "<isPort> [sPort] [ePort]", "HIDE") { arg: Array<String>?, _: StrCons? ->
            if (arg != null) {
                if (arg[0].toBoolean()) {
                    val netServiceTcp = NetService()

                    if (NetStaticData.ServerNetType == IRwHps.NetType.RelayProtocol || NetStaticData.ServerNetType == IRwHps.NetType.RelayMulticastProtocol) {
                        netServiceTcp.workThreadCount = 3000
                    } else {
                        // 用户 CPU 并没有那么强悍
                        //netServiceTcp.workThreadCount = Data.configServer.maxPlayer
                    }

                    Threads.newThreadCoreNet {
                        if (arg.size > 2) {
                            netServiceTcp.openPort(Data.config.port, arg[1].toInt(), arg[2].toInt())
                        } else {
                            netServiceTcp.openPort(Data.config.port)
                        }
                    }
                }

                Threads.newThreadCoreNet {
                    if (Data.config.webPort != 0) {
                        val netServiceTcp1 = NetService(StartHttp::class.java)
                        netServiceTcp1.openPort(Data.config.webPort)
                    }
                }
                PluginManage.runGlobalEventManage(ServerStartTypeEvent(NetStaticData.ServerNetType))
            } else {
                Log.clog("[Start Service] No parameter")
            }
        }
    }

    companion object {
        private val localeUtil = Data.i18NBundle
    }

    init {
        registerCore(handler)
        registerCorex(handler)
        registerInfo(handler)
        registerStartServer(handler)


        // Test (孵化器）
        handler.register("log", "[a...]", "serverCommands.exit") { _: Array<String>, _: StrCons ->
            HotLoadClass().load(FileUtils.getFile("a.class").readFileByte())
        }
    }
}