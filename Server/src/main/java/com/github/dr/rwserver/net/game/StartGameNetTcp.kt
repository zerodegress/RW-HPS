package com.github.dr.rwserver.net.game

import com.github.dr.rwserver.data.global.NetStaticData
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelInitializer
import io.netty.channel.socket.SocketChannel
import io.netty.handler.timeout.IdleStateHandler
import java.util.concurrent.TimeUnit

@Sharable
internal class StartGameNetTcp(startNet: StartNet) : ChannelInitializer<SocketChannel>() {
    private val idleStateTrigger: AcceptorIdleStateTrigger = AcceptorIdleStateTrigger(startNet)
    private var newServerHandler: NewServerHandler = NewServerHandler(
        startNet,
        NetStaticData.protocolData.abstractNetConnect,
        NetStaticData.protocolData.typeConnect
    )

    fun updateNet() {
        newServerHandler.update(NetStaticData.protocolData.abstractNetConnect, NetStaticData.protocolData.typeConnect)
    }

    @Throws(Exception::class)
    override fun initChannel(socketChannel: SocketChannel) {
        val pipeline = socketChannel.pipeline()
        pipeline.addLast(IdleStateHandler(0, 5, 0, TimeUnit.SECONDS))
        pipeline.addLast(idleStateTrigger)
        pipeline.addLast(PacketDecoder())
        pipeline.addLast(PacketEncoder())
        pipeline.addLast(newServerHandler)
    }


}