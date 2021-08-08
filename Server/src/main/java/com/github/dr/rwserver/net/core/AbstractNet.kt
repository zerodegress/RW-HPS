package com.github.dr.rwserver.net.core

import com.github.dr.rwserver.data.global.NetStaticData
import com.github.dr.rwserver.net.game.*
import com.github.dr.rwserver.net.game.AcceptorIdleStateTrigger
import com.github.dr.rwserver.net.game.NewServerHandler
import com.github.dr.rwserver.net.game.PacketDecoder
import com.github.dr.rwserver.net.game.PacketEncoder
import com.github.dr.rwserver.util.log.Log.info
import com.github.dr.rwserver.util.log.exp.ImplementedException
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelInitializer
import io.netty.channel.ChannelPipeline
import io.netty.channel.socket.SocketChannel
import io.netty.handler.timeout.IdleStateHandler
import io.netty.util.concurrent.EventExecutorGroup
import java.util.concurrent.TimeUnit

@Sharable
open class AbstractNet(protected val startNet: StartNet): ChannelInitializer<SocketChannel>() {
    private val idleStateTrigger: AcceptorIdleStateTrigger = AcceptorIdleStateTrigger(startNet)
    private var newServerHandler: NewServerHandler = NewServerHandler(startNet, NetStaticData.protocolData.abstractNetConnect, NetStaticData.protocolData.typeConnect)

    protected fun addTimeOut(channelPipeline: ChannelPipeline) {
        //info("addTimeOut")
        channelPipeline.addLast(IdleStateHandler(0, 5, 0, TimeUnit.SECONDS))
        channelPipeline.addLast(idleStateTrigger)
    }

    protected fun addPacketDecoderAndEncoder(channelPipeline: ChannelPipeline) {
        //info("addPacketDecoderAndEncoder")
        channelPipeline.addLast(PacketDecoder())
        channelPipeline.addLast(PacketEncoder())
    }

    protected fun addNewServerHandler(channelPipeline: ChannelPipeline) {
        //info("addNewServerHandler")
        channelPipeline.addLast(newServerHandler)
    }
    protected fun addNewServerHandler(eventExecutorGroup: EventExecutorGroup, channelPipeline: ChannelPipeline) {
        //info("addNewServerHandler")
        channelPipeline.addLast(eventExecutorGroup,newServerHandler)
    }

    fun updateNet() {
        newServerHandler.update(NetStaticData.protocolData.abstractNetConnect, NetStaticData.protocolData.typeConnect)
    }

    override fun initChannel(socketChannel: SocketChannel) {
        throw ImplementedException("Need to implement")
    }

}