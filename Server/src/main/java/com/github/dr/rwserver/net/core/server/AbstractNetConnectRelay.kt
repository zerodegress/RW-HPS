/*
 * Copyright 2020-2021 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.net.core.server

import com.github.dr.rwserver.io.Packet

/**
 * Only provide interface but not implement
 * As the interface of game CoreNet, it provides various version support for GameServer
 * @author Dr
 * @date 2021/7/31/ 14:14
 */interface AbstractNetConnectRelay {
    /**
     * Server类型
     * @param msg RelayID
     */
    fun sendRelayServerType(msg: String)

    /**
     * 类型回复
     */
    fun sendRelayServerTypeReply(packet: Packet)

    fun sendRelayServerInfo()

    /**
     * (WARN) This part temporarily refuses to provide analysis and needs to wait for the right time
     * @author Dr
     */
    fun sendRelayServerCheck()

    /**
     * (WARN) This part temporarily refuses to provide analysis and needs to wait for the right time
     * @author Dr
     */
    fun sendRelayServerId()

    /*

     */

    /**
     * (WARN) This part temporarily refuses to provide analysis and needs to wait for the right time
     * @author Dr
     */
    fun sendRelayPlayerInfo()

    /**
     * (WARN) This part temporarily refuses to provide analysis and needs to wait for the right time
     * @author Dr
     */
    fun getRelayUnitData(packet: Packet)
}