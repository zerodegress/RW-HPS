/*
 * Copyright 2020-2021 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package com.github.dr.rwserver.net.game

import com.github.dr.rwserver.io.Packet
import com.github.dr.rwserver.net.core.TypeConnect
import com.github.dr.rwserver.net.core.server.AbstractNetConnect
import com.github.dr.rwserver.util.log.Log.error
import io.netty.channel.ChannelHandler.Sharable
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.SimpleChannelInboundHandler
import io.netty.util.AttributeKey

@Sharable
internal class NewServerHandler internal constructor(
    private val startNet: StartNet,
    private var abstractNetConnect: AbstractNetConnect,
    private var typeConnect: TypeConnect
) : SimpleChannelInboundHandler<Any?>() {

    fun update(abstractNetConnect: AbstractNetConnect, typeConnect: TypeConnect) {
        this.abstractNetConnect = abstractNetConnect
        this.typeConnect = typeConnect
    }

    /*
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (msg instanceof Packet) {
                final Packet p = (Packet) msg;
                final Channel channel = ctx.channel();
                AbstractNetConnect con = startNet.OVER_MAP.get(channel.id().asLongText());
                if (con == null) {
                    con = abstractNetConnect.getVersionNet(channel.id().asLongText());
                    startNet.OVER_MAP.put(channel.id().asLongText(), con);
                    con.setConnectionAgreement(new ConnectionAgreement(ctx,channel,startNet));
                }
                final AbstractNetConnect finalCon = con;
                ctx.executor().execute(() -> {
                    try {
                        typeConnect.typeConnect(finalCon, p);
                    } catch (Exception e) {
                        Log.debug(e);
                        finalCon.disconnect();
                    } finally {
                        ReferenceCountUtil.release(msg);
                    }
                });
            }
        } catch (Exception ss) {
            Log.error(ss);
        }
    }
*/
    @Throws(Exception::class)
    override fun channelRead0(ctx: ChannelHandlerContext, msg: Any?) {
        try {
            if (msg is Packet) {
                val attr = ctx.channel().attr(NETTY_CHANNEL_KEY)
                var con = attr.get()
                if (con == null) {
                    con = abstractNetConnect.getVersionNet(ConnectionAgreement(ctx, startNet))
                    attr.setIfAbsent(con)
                }

                ctx.executor().execute {
                    /*
                    if (con.isConnectServer) {
                        con.connectServer!!.send(msg)
                        return@execute
                    }*/
                    //TODO
                    try {
                        typeConnect.typeConnect(con, msg)
                    } catch (e: Exception) {
                        //debug(e)
                        //con.disconnect()
                    }
                }
            }
        } catch (ss: Exception) {
            error(ss)
        }
    }

    @Throws(Exception::class)
    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        ctx.close()
        //error(RuntimeException())
        error(cause)
    }

    companion object {
        @JvmField
        val NETTY_CHANNEL_KEY = AttributeKey.valueOf<AbstractNetConnect>("User-Net")!!
    }
}