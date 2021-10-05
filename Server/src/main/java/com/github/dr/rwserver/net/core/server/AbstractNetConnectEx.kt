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
import com.github.dr.rwserver.net.game.ConnectServer
import com.github.dr.rwserver.net.game.ConnectionAgreement

/**
 * Only provide interface but not implement
 * As the interface of game CoreNet, it provides various version support for GameServer
 * @author Dr
 * @date 2020/9/5 13:31
 */
interface AbstractNetConnectEx {
    /*
     * TODO : AntiCheats
     */
    /**
     * Get the version agreement
     * @param connectionAgreement ConnectionAgreement
     * @return Protocol
     */
    fun getVersionNet(connectionAgreement: ConnectionAgreement): AbstractNetConnect

    /**
     * Get whether the link is forwarded
     */
    var isConnectServer: Boolean
    var connectServer: ConnectServer?

    /**
     * Set up a cache packet
     * @param packet
     */
    fun setCache(packet: Packet)

    /**
     * Set up a cache packet
     * @param packet
     */
    fun setCacheA(packet: Packet)

    /**
     * Get connection IP
     * @return IP
     */
    val ip: String

    /**
     * Get the local port used
     * @return Port
     */
    val port: Int

    /**
     * Get player name
     * @return Player name
     */
    val name: String

    /**
     * Number of attempts +1
     */
    fun setTry()

    /**
     * Get attempts
     * @return Number of attempts
     */
    val `try`: Int

    /**
     * Set up try
     * //@param status
     * Get try status
     * @return Boolean
     */
    var tryBoolean: Boolean

    /**
     * Get whether you are entering a password
     * @return Boolean
     */
    val inputPassword: Boolean

    /**
     * Set the last time to receive data
     */
    fun setLastReceivedTime()

    /**
     * Get the last time to speak
     * @return Time
     */
    val lastReceivedTime: Long

    /**
     * Get connection agreement
     * @return Protocol
     */
    fun getConnectionAgreement(): String

    /**
     * Protocol version
     * @return version number
     */
    val version: String

    /**
     * Disconnect
     */
    fun disconnect()

    /**
     * Send package
     * @param packet Data
     */
    fun sendPacket(packet: Packet)

    /**
     * Debug Special development not open temporarily
     * @param packet Packet
     */
    fun debug(packet: Packet) {}

    /**
     * Debug Special development not open temporarily
     * @param str String
     */
    fun sendDebug(str: String) {}
}