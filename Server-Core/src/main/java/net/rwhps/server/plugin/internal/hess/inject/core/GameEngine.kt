/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.plugin.internal.hess.inject.core

import com.corrodinggames.librocket.b
import com.corrodinggames.librocket.scripts.Multiplayer
import com.corrodinggames.librocket.scripts.Root
import com.corrodinggames.librocket.scripts.ScriptContext
import com.corrodinggames.rts.gameFramework.j.ad
import com.corrodinggames.rts.gameFramework.l
import net.rwhps.server.core.ServiceLoader
import net.rwhps.server.data.HessModuleManage
import net.rwhps.server.data.global.Data
import net.rwhps.server.data.global.ServerRoom
import net.rwhps.server.game.simulation.core.*
import net.rwhps.server.net.core.IRwHps
import net.rwhps.server.plugin.internal.hess.HessMain
import net.rwhps.server.plugin.internal.hess.inject.command.ClientCommands
import net.rwhps.server.plugin.internal.hess.inject.command.ServerCommands
import net.rwhps.server.plugin.internal.hess.inject.net.HessRwHps
import net.rwhps.server.util.inline.accessibleConstructor
import net.rwhps.server.util.inline.findField

/**
 * RW-HPS 的 Hess 实现内部接口
 * 内部实现通过 [GameEngine] 来减少一些混淆 也可以参见 (RW-AC项目)
 *
 * @author RW-HPS/Dr
 */
internal object GameEngine {
    lateinit var data: AbstractGameModule
        private set

    /** 设置脚本加载器, 并完成初始化 */
    val root = Root().apply {
        // 设置对应的 Multiplayer
        this.multiplayer = Multiplayer::class.java.accessibleConstructor(Root::class.java).newInstance(this)

        // 初始化 ScriptContext 的 libRocket
        ScriptContext::class.java.findField("libRocket")!!.set(this,object: b() {
            override fun EnableScissorRegion(p0: Boolean) {
                // ignore
            }
        })
    }

    val gameEngine: l get() = l.B()
    val mapEngine: com.corrodinggames.rts.game.b.b get() = gameEngine.bL
    val netEngine: ad get() = gameEngine.bX

    val settingsEngine get() = gameEngine.bQ!!

    val gameStatistics get() = gameEngine.bY!!

    /**
     * 通过这里完成 [AbstractGameModule] 通用接口实现
     */
    @JvmStatic
    fun init() {
        val loader = GameEngine.javaClass.classLoader
        HessModuleManage.addGameModule(loader.toString(), object: AbstractGameModule {
            override val useClassLoader: ClassLoader = loader
            override val gameHessData: AbstractGameHessData = GameHessData()
            override val gameNet: AbstractGameNet = GameNet()
            override val gameUnitData: AbstractGameUnitData = GameUnitData()
            override val gameFast: AbstractGameFast = GameFast()
            override val gameData: AbstractGameData = GameData()
            override val gameDataLink: AbstractGameLinkData = GameLinkData()
            override val room: ServerRoom = ServerRoom()
        }.also {
            data = it
        })

        // 设置客户端UUID, 避免Admin/ban等不能持久化
        settingsEngine.networkServerId = Data.core.serverConnectUuid

        ServiceLoader.addService(ServiceLoader.ServiceType.IRwHps, IRwHps.NetType.ServerProtocol.name, HessRwHps::class.java)

        ServerCommands(HessMain.serverServerCommands)
        ClientCommands(HessMain.serverClientCommands)
    }
}