/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.net.netconnectprotocol.realize

import net.rwhps.server.io.packet.Packet
import net.rwhps.server.net.core.ConnectionAgreement
import java.io.IOException

/**
 * FFA Test
 *
 * @property version String
 *
 * @author RW-HPS/Dr
 */
class GameVersionFFA(connectionAgreement: ConnectionAgreement?) : GameVersionServer(connectionAgreement!!) {
    override val version: String
        get() = "1.14 RW-HPS-FFA"

    @Throws(IOException::class)
    override fun receiveChat(packet: Packet) {
    }


}