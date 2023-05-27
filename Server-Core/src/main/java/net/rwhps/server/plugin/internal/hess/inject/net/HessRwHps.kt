/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.plugin.internal.hess.inject.net

import net.rwhps.server.net.core.ConnectionAgreement
import net.rwhps.server.net.core.IRwHps
import net.rwhps.server.net.core.TypeConnect
import net.rwhps.server.net.netconnectprotocol.RwHps
import net.rwhps.server.plugin.internal.hess.inject.core.GameEngine
import net.rwhps.server.plugin.internal.hess.inject.lib.PlayerConnectX

/**
 * @author RW-HPS/Dr
 */
class HessRwHps(netType: IRwHps.NetType) : RwHps(netType) {
    override val typeConnect: TypeConnect = TypeHessRwHps(GameVersionServer(PlayerConnectX(GameEngine.netEngine, ConnectionAgreement())))
}