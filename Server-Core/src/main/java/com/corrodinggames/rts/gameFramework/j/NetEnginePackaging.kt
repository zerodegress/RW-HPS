/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.corrodinggames.rts.gameFramework.j

import net.rwhps.server.io.packet.Packet
import net.rwhps.server.plugin.internal.hess.inject.lib.PlayerConnectX
import com.corrodinggames.rts.gameFramework.j.au as PacketHess

/**
 * 将 Game-Lib 的 Packet 与 RW-HPS 的 Packet 互相转换
 * 并且将包输入进 NetEngine
 *
 * @property netEngine NetEngine
 * @property playerConnectX PlayerConnectX
 * @constructor
 *
 * @author RW-HPS/Dr
 */
class NetEnginePackaging(
    private val netEngine: ad,
    private val playerConnectX: PlayerConnectX
) {
    fun addPacket(packet: Packet) {
        val packetHess = transformHessPacket(packet)

        if (packetHess.b > 100) {
            netEngine.c(packetHess)
        } else {
            netEngine.aN.add(packetHess)
        }
    }

    fun transformHessPacket(packet: Packet): PacketHess {
        return PacketHess(packet.type.typeInt).apply {
            c = packet.bytes
            a = playerConnectX
        }
    }

    fun transformPacket(packetHess: PacketHess): Packet {
        return Packet(packetHess.b,packetHess.c)
    }

    companion object {
        fun transformHessPacketNullSource(packet: Packet): PacketHess {
            return PacketHess(packet.type.typeInt).apply {
                c = packet.bytes
            }
        }
    }
}