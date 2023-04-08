/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *  
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.core

import net.rwhps.server.core.thread.Threads.addSavePool
import net.rwhps.server.core.thread.Threads.runSavePool
import net.rwhps.server.data.global.Data
import net.rwhps.server.data.plugin.PluginData
import net.rwhps.server.data.plugin.PluginManage.runOnDisable
import net.rwhps.server.net.Administration
import net.rwhps.server.util.RandomUtil.getRandomString
import net.rwhps.server.util.algorithms.digest.DigestUtil
import net.rwhps.server.util.file.FileUtil
import net.rwhps.server.util.log.Log
import java.math.BigInteger
import java.util.*


/**
 * @author RW-HPS/Dr
 */
class Application {
    /** 服务器 Setting 主数据 */
    val settings: PluginData = PluginData()

    /** 服务器唯一UUID  */
    lateinit var serverConnectUuid: String
    /** Hess HEX */
    lateinit var serverHessUuid: String

    @JvmField
    var serverToken: String = getRandomString(40)
    lateinit var admin: Administration
    @JvmField
    var upServerList = false

    /**
     * 读取 Setting 主数据
     */
    fun load() {
        settings.setFileUtil(FileUtil.getFolder(Data.Plugin_Data_Path).toFile("Settings.bin"))
        admin = Administration(settings)

        Initialization.startInit(settings)

        serverConnectUuid = settings.getData("serverConnectUuid") { UUID.randomUUID().toString() }
        serverHessUuid = settings.getData("serverHessUuid") { BigInteger(1, DigestUtil.sha256(serverConnectUuid+UUID.randomUUID().toString())).toString(16).uppercase() }

        addSavePool {
            settings.setData("serverConnectUuid", serverConnectUuid)
            settings.setData("serverHessUuid", serverHessUuid)
        }
    }

    /**
     * 服务器退出时保存数据
     */
    fun save() {
        // 先执行自己的保存
        runSavePool()
        // 保存自己
        settings.save()
        // 保存Plugin
        runOnDisable()

        Log.saveLog()
    }
}