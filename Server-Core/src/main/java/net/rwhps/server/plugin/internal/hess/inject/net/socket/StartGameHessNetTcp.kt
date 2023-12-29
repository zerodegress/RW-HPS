/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.plugin.internal.hess.inject.net.socket

import com.corrodinggames.rts.gameFramework.j.ad
import io.netty.channel.ChannelHandler
import io.netty.channel.socket.SocketChannel
import net.rwhps.server.net.core.AbstractNet

/**
 * @author Dr (dr@der.kim)
 */
@ChannelHandler.Sharable
class StartGameHessNetTcp(private val netEngine: ad): AbstractNet() {
    init {
        init(NewServerHessHandler(netEngine, this))
    }

    @Throws(Exception::class)
    override fun initChannel(socketChannel: SocketChannel) {
        rwinit(socketChannel.pipeline())
    }
}