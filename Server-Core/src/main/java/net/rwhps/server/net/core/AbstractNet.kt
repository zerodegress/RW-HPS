/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.net.core

import io.netty.channel.Channel
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelPipeline
import io.netty.channel.socket.SocketChannel
import io.netty.handler.timeout.IdleStateHandler
import io.netty.util.Attribute
import io.netty.util.AttributeKey
import io.netty.util.concurrent.DefaultEventExecutorGroup
import io.netty.util.concurrent.EventExecutorGroup
import net.rwhps.server.net.code.tcp.PacketDecoder
import net.rwhps.server.net.code.tcp.PacketEncoder
import net.rwhps.server.net.handler.tcp.AcceptorIdleStateTrigger
import net.rwhps.server.net.handler.tcp.NewServerHandler
import net.rwhps.server.util.concurrent.threads.ThreadFactoryName
import java.util.concurrent.TimeUnit


/**
 * @author Dr (dr@der.kim)
 */
@Sharable
abstract class AbstractNet: ChannelInitializer<SocketChannel>() {
    private val ioGroup: EventExecutorGroup = DefaultEventExecutorGroup(128, ThreadFactoryName.nameThreadFactory("IO-Group"))

    private lateinit var newServerHandler: INetServerHandler
    private val idleStateTrigger: AcceptorIdleStateTrigger by lazy { AcceptorIdleStateTrigger(this) }

    val nettyChannelData = AttributeKey.valueOf<TypeConnect>("User-Net")!!

    fun init(newServerHandler: INetServerHandler = NewServerHandler(this)) {
        this.newServerHandler = newServerHandler
    }

    fun getTypeConnect(channel: Channel): Attribute<TypeConnect> {
        return channel.attr(nettyChannelData)
    }

    protected fun addTimeOut(channelPipeline: ChannelPipeline) {
        channelPipeline.addLast(IdleStateHandler(0, 5, 0, TimeUnit.SECONDS))
        channelPipeline.addLast(idleStateTrigger)
    }

    protected fun addPacketDecoderAndEncoder(channelPipeline: ChannelPipeline) {
        channelPipeline.addLast(PacketDecoder())
        channelPipeline.addLast(PacketEncoder())
    }

    protected fun addNewServerHandler(channelPipeline: ChannelPipeline) {
        channelPipeline.addLast(newServerHandler)
    }

    protected fun addNewServerHandlerExecutorGroup(channelPipeline: ChannelPipeline) {
        channelPipeline.addLast(ioGroup, newServerHandler)
    }

    protected fun rwinit(channelPipeline: ChannelPipeline) {
        addTimeOut(channelPipeline)
        addPacketDecoderAndEncoder(channelPipeline)
        addNewServerHandler(channelPipeline)
        //addNewServerHandlerExecutorGroup(channelPipeline)
    }

    internal fun getConnectSize(): Int {
        return idleStateTrigger.connectNum.get()
    }

    open fun close() {
        ioGroup.shutdownGracefully()
    }
}