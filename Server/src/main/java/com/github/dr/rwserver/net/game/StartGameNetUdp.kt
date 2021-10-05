/*
 * Copyright 2020-2021 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */
package com.github.dr.rwserver.net.game

import com.github.dr.rwserver.data.global.NetStaticData
import com.github.dr.rwserver.io.Packet
import com.github.dr.rwserver.net.core.TypeConnect
import com.github.dr.rwserver.net.core.server.AbstractNetConnect
import com.github.dr.rwserver.util.log.Log.error
import com.github.dr.rwserver.util.threads.ThreadFactoryName.nameThreadFactory
import net.udp.ReliableServerSocket.ReliableClientSocket
import java.io.DataInputStream
import java.io.IOException
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit

internal class StartGameNetUdp(
    private val startNet: StartNet,
    private var abstractNetConnect: AbstractNetConnect,
    private var typeConnect: TypeConnect
) {
    private val group = ThreadPoolExecutor(4, Int.MAX_VALUE, 1L, TimeUnit.MINUTES, LinkedBlockingQueue(), nameThreadFactory("UDP-"))
    private val timeoutDetection: TimeoutDetection = TimeoutDetection(5, startNet)
    internal fun update() {
        abstractNetConnect = NetStaticData.protocolData.abstractNetConnect
        typeConnect = NetStaticData.protocolData.typeConnect
    }

    @Throws(IOException::class)
    protected fun close() {
        group.shutdownNow()
    }

    @Throws(Exception::class)
    internal fun run(socket: ReliableClientSocket) {
        val sockAds = socket.remoteSocketAddress
        var con = startNet.OVER_MAP[sockAds.toString()]
        if (con == null) {
            con = abstractNetConnect.getVersionNet(ConnectionAgreement(socket, startNet))
            startNet.OVER_MAP.put(sockAds.toString(), con)
        }
        val conFinal = con
        group.execute {
            while (!socket.isClosed) {
                try {
                    val `in` = DataInputStream(socket.inputStream)
                    val size = `in`.readInt()
                    val type = `in`.readInt()
                    val bytes = ByteArray(size)
                    var bytesRead = 0
                    while (bytesRead < size) {
                        val readIn = `in`.read(bytes, bytesRead, size - bytesRead)
                        if (readIn == -1) {
                            break
                        }
                        bytesRead += readIn
                    }
                    typeConnect.typeConnect(conFinal, Packet(type, bytes))
                } catch (e: Exception) {
                    error("UDP READ", e)
                    conFinal.disconnect()
                    startNet.OVER_MAP.remove(sockAds.toString())
                    return@execute
                }
            }
            conFinal.disconnect()
            startNet.OVER_MAP.remove(sockAds.toString())
        }
    }

}