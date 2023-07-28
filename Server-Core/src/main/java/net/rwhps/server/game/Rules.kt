/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */
package net.rwhps.server.game

import net.rwhps.server.data.bean.BeanCoreConfig
import net.rwhps.server.data.bean.BeanServerConfig
import net.rwhps.server.data.global.Data
import net.rwhps.server.data.global.NetStaticData
import net.rwhps.server.util.IsUtils.notIsBlank
import net.rwhps.server.util.algorithms.digest.DigestUtils.sha256
import java.math.BigInteger

/**
 * @author RW-HPS/Dr
 */
class Rules(private var config: BeanCoreConfig, private var configServer: BeanServerConfig) {
    /** 倍数  */
    var income: Float

    /** 初始钱  */
    var credits = 0

    /** 最大玩家  */
    var maxPlayer: Int
        private set

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
    val passwd: String = if (notIsBlank(Data.configServer.passwd)) BigInteger(1, sha256(Data.configServer.passwd)).toString(16)
        .uppercase() else ""

    /** 游戏暂停  */
    @Volatile
    var gamePaused = false

    init {
        NetStaticData.relay.isMod = config.singleUserRelayMod
        val maxPlayer = configServer.maxPlayer + 1
        this.maxPlayer = maxPlayer
        income = configServer.defIncome
    }

}