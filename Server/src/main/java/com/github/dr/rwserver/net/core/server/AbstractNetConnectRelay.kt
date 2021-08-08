package com.github.dr.rwserver.net.core.server

import com.github.dr.rwserver.io.Packet

/**
 * 只提供接口 不实现
 * 作为游戏CoreNet的分布实现 为GameServer提供多样的版本支持
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
}