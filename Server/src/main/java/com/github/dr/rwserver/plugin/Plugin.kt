package com.github.dr.rwserver.plugin

import com.github.dr.rwserver.data.plugin.PluginData
import com.github.dr.rwserver.plugin.event.AbstractEvent
import com.github.dr.rwserver.util.RandomUtil.generateLowerStr
import com.github.dr.rwserver.util.game.CommandHandler

/**
 * @author Dr
 */
open class Plugin {
    @JvmField
    val pluginData = PluginData(generateLowerStr(10))

    /** 最先执行 可以进行Plugin的数据读取  -1  */
    open fun onEnable() {}

    /** 注册要在服务器端使用的任何命令，例如从控制台 -2  */
    open fun registerServerCommands(handler: CommandHandler?) {}

    /** 注册要在客户端使用的任何命令，例如来自游戏内玩家 -3  */
    open fun registerClientCommands(handler: CommandHandler?) {}

    /**
     * 注册事件
     * @return AbstractEvent实例
     */
    open fun registerEvents(): AbstractEvent? {
        return null
    }

    /** 创建所有插件并注册命令后调用 -5  */
    open fun init() {}

    /** Server退出时执行 可以进行Plugin的数据保存 -6  */
    open fun onDisable() {
        pluginData.save()
    }
}