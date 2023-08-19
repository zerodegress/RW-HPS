/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.game.simulation.core


/**
 * 链接游戏内的方法
 *
 * @author RW-HPS/Dr
 */
interface AbstractGameLinkFunction {
    /**
     * 全局同步
     */
    fun allPlayerSync()

    fun pauseGame(pause: Boolean)

    /**
     * 返回战役室
     *
     * @param time 倒计时
     */
    fun battleRoom(time: Int = 5)

    /**
     * 保存游戏到 save
     */
    fun saveGame()
}