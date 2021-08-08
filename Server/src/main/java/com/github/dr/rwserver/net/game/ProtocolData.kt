package com.github.dr.rwserver.net.game

import com.github.dr.rwserver.net.core.server.AbstractNetConnect
import com.github.dr.rwserver.net.core.TypeConnect
import com.github.dr.rwserver.net.core.AbstractNetPacket

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