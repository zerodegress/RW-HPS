/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.plugin.beta.noattack

import net.rwhps.server.data.totalizer.TimeAndNumber
import net.rwhps.server.net.core.ConnectionAgreement
import net.rwhps.server.plugin.Plugin
import net.rwhps.server.plugin.event.AbstractGlobalEvent
import net.rwhps.server.struct.Seq
import net.rwhps.server.util.Time
import java.util.concurrent.ConcurrentHashMap

/**
 * Limit player connections
 *
 * Not valid for DDOS
 *
 * @author RW-HPS/Dr
 */
internal class ConnectionLimit: Plugin() {
    private var cacheTime: Long = Time.concurrentMillis() + 60 * 60 * 1000L // 1H


    override fun registerGlobalEvents(): AbstractGlobalEvent = object : AbstractGlobalEvent {
        private val netLimit: ConcurrentHashMap<String, LimitData> = ConcurrentHashMap()
        private val netLimitList: Seq<String> = Seq()

        override fun registerNewConnectEvent(connectionAgreement: ConnectionAgreement): Boolean {
            /*
             * TimeAndNumber ( TimeOut , ConutMax )
             *
             * TimeAndNumber(10,5) is Triggered when five new connections are reached within ten seconds
             *
             */
            val limit = netLimit.computeIfAbsent(connectionAgreement.ipLong24){ LimitData(0, TimeAndNumber(5,8)) }
            limit.LastUpdateTime = Time.concurrentSecond() + 60 * 60



            var flag = false

            if (limit.limit.checkStatus()) {
                if (netLimitList.contains(connectionAgreement.ipLong24)) {
                    flag = true
                } else {
                    limit.limit.count++
                }
            } else {
                netLimitList.add(connectionAgreement.ipLong24)
                flag = true
            }

            /* Clear Cache */
            if (cacheTime < Time.concurrentMillis()) {
                cacheTime = Time.concurrentMillis() + 60 * 60 * 1000L // 1H
                val s = Time.concurrentSecond()
                netLimit.forEach {
                    if (it.value.LastUpdateTime < s) {
                        netLimitList.remove(it.key)
                        netLimit.remove(it.key)
                    }
                }
            }

            return flag
        }
    }

    private data class LimitData(
        var LastUpdateTime: Int,
        val limit: TimeAndNumber
    )
}