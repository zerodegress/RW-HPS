/*
 * Copyright 2020-2021 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.net.core

import com.github.dr.rwserver.io.Packet
import com.github.dr.rwserver.net.core.server.AbstractNetConnect

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