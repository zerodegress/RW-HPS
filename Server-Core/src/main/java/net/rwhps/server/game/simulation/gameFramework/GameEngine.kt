/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.game.simulation.gameFramework

import com.corrodinggames.librocket.b
import com.corrodinggames.librocket.scripts.Multiplayer
import com.corrodinggames.librocket.scripts.Root
import com.corrodinggames.librocket.scripts.ScriptContext
import com.corrodinggames.rts.gameFramework.j.ad
import com.corrodinggames.rts.gameFramework.l
import net.rwhps.server.data.HessModuleManage
import net.rwhps.server.data.global.ServerRoom
import net.rwhps.server.game.simulation.core.*
import net.rwhps.server.game.simulation.gameFramework.command.ClientCommands
import net.rwhps.server.util.inline.accessibleConstructor
import net.rwhps.server.util.inline.findField

/**
 * RW-HPS 的 Hess 实现内部接口
 * 内部实现通过 [GameEngine] 来减少一些混淆 也可以参见 (RW-AC项目)
 *
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

    val gameEngine: l = l.B()
    val netEngine: ad = gameEngine.bX

    val settingsEngine = gameEngine.bQ!!

    val gameStatistics = gameEngine.bY!!

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
            override val room: ServerRoom = ServerRoom()
        }.also {
            data = it
        })

        ClientCommands(data.gameData.clientCommand)
    }
}