/*
 * Copyright 2020-2024 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.custom

import net.rwhps.server.data.global.Data
import net.rwhps.server.plugin.beta.UpListMain
import net.rwhps.server.plugin.beta.http.RwHpsWebApiMain
import net.rwhps.server.plugin.internal.headless.HessMain
import net.rwhps.server.util.file.plugin.PluginManage

/**
 * 内部的一些插件 加载
 *
 * @property core String
 * @property coreEx String
 * @property amusement String
 *
 * @author Dr (dr@der.kim)
 */
internal class LoadCoreCustomPlugin {
    private val core = "[Core Plugin]"
    private val coreEx = "[Core Plugin Extend]"
    private val amusement = "[Amusement Plugin]"
    private val example = "[Example Plugin]"

    init {
        PluginManage.addPluginClass(
                "Headless Rusted Warfare" ,"HessServer", "Dr", "$core Headless Rusted Warfare", Data.SERVER_CORE_VERSION, HessMain(), mkdir = false, skip = true
        )

        PluginManage.addPluginClass("UpList","UpListEx","Dr","$core UpList","1.0", UpListMain(), mkdir = false, skip = true)
        PluginManage.addPluginClass(RwHpsWebApiMain.pluginInfo, RwHpsWebApiMain(), mkdir = true, skip = true)
    }
}