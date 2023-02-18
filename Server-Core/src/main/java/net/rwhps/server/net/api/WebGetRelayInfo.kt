/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.net.api

import net.rwhps.server.core.thread.CallTimeTask
import net.rwhps.server.core.thread.Threads
import net.rwhps.server.data.global.Data
import net.rwhps.server.data.global.NetStaticData
import net.rwhps.server.data.global.Relay
import net.rwhps.server.net.NetService
import net.rwhps.server.net.http.AcceptWeb
import net.rwhps.server.net.http.SendWeb
import net.rwhps.server.net.http.WebGet
import net.rwhps.server.util.SystemUtil
import net.rwhps.server.util.Time
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

class WebGetRelayInfo : WebGet() {

    override fun get(accept: AcceptWeb, send: SendWeb) {
        send.setData(relayInfo)
        send.send()
    }

    companion object {
        var relayInfo: String = "NOT DATA"

        init {
            Threads.newTimedTask(CallTimeTask.ServerStatusUpdate, 0, 30, TimeUnit.SECONDS) {
                val size = AtomicInteger()
                NetStaticData.netService.eachAll { e: NetService -> size.addAndGet(e.getConnectSize()) }

                relayInfo = """
                            <!--
                              ~ Copyright 2020-2023 RW-HPS Team and contributors.
                              ~
                              ~ 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
                              ~ Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
                              ~
                              ~ https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
                              -->
            
                            <!DOCTYPE html>
                            <html lang="zh-cn">
                            <head>
                                <meta charset="UTF-8">
                                <title>RW-HPS RELAY  Run Status</title>
                            </head>
                            <body>
                                
                                <h1> 欢迎来到 [RW-HPS RELAY] 数据统计！  </h1>
                                <p> RW-HPS RELAY 由 [RW-HPS Team] 开发，致力于为中国玩家提供更好的游戏体验 </p>
                                <p> RCN的现有数据来自自动上报 </p>
                                <h2> 欢迎来到 [RW-HPS RELAY] 数据统计！  </h2>
                                <p> 房间总个数 : ${Relay.roomAllSize}  </p>
                                <p> (未开始)游戏房间总个数 : ${Relay.roomNoStartSize}  </p>
                                <p> 目前在线人数 : ${size.get()}  </p>
                                <p> 服务器版本 : ${Data.SERVER_CORE_VERSION}  </p>
                                <br/>
                                <p> Java堆大小(MB) : ${SystemUtil.javaHeap / (1024 * 1024)}  </p>
                                <p> Java总内存(MB) : ${SystemUtil.javaTotalMemory / (1024 * 1024)}  </p>
                                <p> Java可用内存(MB) : ${SystemUtil.javaFreeMemory / (1024 * 1024)}  </p>
                                <p> Java供应商 : ${SystemUtil.javaVendor}  </p>
                                <p> Java版本 : ${SystemUtil.javaVersion}  </p>
                                <p> 系统 : ${SystemUtil.osName}  </p>
                                <p> 数据最后更新 : ${Time.getMilliFormat(1)}</p>
                                <br/>
                                <br/>
                                
                                <br/>
                                
                                RW-HPS Team :
                                <blockquote>
                                    <p dir="auto">在 GitHub Discussions 提出的问题会收到回复, 也欢迎分享你基于项目的新想法<br>
                                        邮件联系: <a href="mailto:dr@der.kim">dr@der.kim</a><br>
                                        腾讯QQ群: <del>去Github自己看</del>
                                        <br>
                                        Github: <a href="https://github.com/RW-HPS/RW-HPS" rel="nofollow">RW-HPS</a></p>
                                        <del>电报群: <a href="https://t.me/RW_HPS" rel="nofollow">RW-HPS</a></del>
                                        <br>
                                        Discord: <a href="https://discord.gg/VwwxJhVG64" rel="nofollow">RW-HPS</a></p>
                                        捐赠服务器: <a href="https://afdian.net/@derdct" rel="nofollow">RW-HPS</a></p>
                                </blockquote>
                            </body>
                            </html>
                        """.trimIndent()
            }
        }
    }
}