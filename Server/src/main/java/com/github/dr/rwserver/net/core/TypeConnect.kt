package com.github.dr.rwserver.net.core

import com.github.dr.rwserver.io.Packet
import kotlin.Throws
import java.lang.Exception

/**
 * 适配多协议支持
 * com.github.dr.rwServer.net.game.NewServerHandler 只提供网络支持 解析数据包的调用需要本方法
 * @author Dr
 */
interface TypeConnect {
    /**
     * 协议处理
     * @param con 传入协议实现
     * @param packet 接受的包
     * @throws Exception Error
     */
    @Throws(Exception::class)
    fun typeConnect(con: AbstractNetConnect, packet: Packet)

    /**
     * 获取TypeConnect处理版本号
     * @return Version
     */
    val version: String
}