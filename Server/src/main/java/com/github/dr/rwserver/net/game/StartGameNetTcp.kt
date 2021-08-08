package com.github.dr.rwserver.net.game

import com.github.dr.rwserver.net.core.AbstractNet
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.socket.SocketChannel

@Sharable
internal class StartGameNetTcp(startNet: StartNet): AbstractNet(startNet) {
    @Throws(Exception::class)
    override fun initChannel(socketChannel: SocketChannel) {
        val pipeline = socketChannel.pipeline()
        addTimeOut(pipeline)
        addPacketDecoderAndEncoder(pipeline)
        addNewServerHandler(startNet.ioGroup,pipeline)
    }



}