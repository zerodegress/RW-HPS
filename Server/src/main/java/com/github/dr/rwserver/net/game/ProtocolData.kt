/*
 * Copyright 2020-2021 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.net.game

import com.github.dr.rwserver.net.core.AbstractNetPacket
import com.github.dr.rwserver.net.core.TypeConnect
import com.github.dr.rwserver.net.core.server.AbstractNetConnect

class ProtocolData {
    /** 可以支持什么版本 */
    var gameNetVersion = 0
        private set
    
    internal lateinit var abstractNetConnect: AbstractNetConnect
    internal lateinit var typeConnect: TypeConnect
    internal lateinit var abstractNetPacket: AbstractNetPacket

    @JvmField
    var AbstractNetConnectVersion: String = ""
    @JvmField
    var AbstractNetPacketVersion: String = ""
    @JvmField
    var TypeConnectVersion: String = ""

    fun setNetConnectProtocol(protocolData: AbstractNetConnect, gameNetVersion: Int) {
        abstractNetConnect = protocolData
        AbstractNetConnectVersion = protocolData.version
        this.gameNetVersion = gameNetVersion
    }

    fun setNetConnectPacket(packet: AbstractNetPacket, version: String) {
        abstractNetPacket = packet
        AbstractNetPacketVersion = version
    }

    fun setTypeConnect(typeConnect: TypeConnect) {
        this.typeConnect = typeConnect
        TypeConnectVersion = typeConnect.version
    }

    protected fun update(abstractNetConnect: AbstractNetConnect, typeConnect: TypeConnect) {
        this.abstractNetConnect = abstractNetConnect
        this.typeConnect = typeConnect
    }
}