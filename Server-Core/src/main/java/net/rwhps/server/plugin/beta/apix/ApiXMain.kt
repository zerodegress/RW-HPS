/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.plugin.beta.apix

import net.rwhps.server.data.event.GameOverData
import net.rwhps.server.net.http.AcceptWeb
import net.rwhps.server.net.http.SendWeb
import net.rwhps.server.net.http.WebData
import net.rwhps.server.net.http.WebGet
import net.rwhps.server.plugin.Plugin
import net.rwhps.server.plugin.event.AbstractEvent
import net.rwhps.server.struct.Seq
import net.rwhps.server.util.inline.toGson
import net.rwhps.server.util.inline.toJson
import net.rwhps.server.util.inline.toPrettyPrintingJson

/**
 * @author RW-HPS/Dr
 */
class ApiXMain : Plugin() {
    lateinit var data: ApiXData
    var dataJson = Seq<String>()

    override fun init() {
        data = ApiXData::class.java.toGson(pluginDataFileUtil.toFile("Data.json").readFileStringData())

        if (data.GameOverPositive) {
            WebData.addWebGetInstance("/api/get/event/GameOver", object : WebGet() {
                override fun get(accept: AcceptWeb, send: SendWeb) {
                    send.setData(dataJson.toList().toJson())
                    dataJson.clear()
                    send.send()
                }
            })
        }
    }

    override fun registerEvents(): AbstractEvent {
        return object: AbstractEvent {
            override fun registerGameOverEvent(gameOverData: GameOverData?) {
                if (gameOverData == null) {
                    return
                }

                if (data.GameOverPositive) {
                    ApixDataJson(
                        gameOverData.gameTime,
                        GameOverDataCover(
                            gameOverData.allPlayerList.toList(),
                            gameOverData.winPlayerList.toList(),
                            gameOverData.mapName,
                            hashMapOf<String, Map<String, Int>>().apply {
                                gameOverData.playerData.forEach {
                                    put(it.key, hashMapOf<String, Int>().apply {
                                        it.value.forEach {
                                            put(it.key, it.value)
                                        }
                                    })
                                }
                            },
                            gameOverData.replayName
                        )
                    ).toJson().also {
                        if (data.GameOverPositive) {
                            dataJson.add(it)
                        }
                    }
                }
            }
        }
    }

    override fun onDisable() {
        pluginDataFileUtil.toFile("Data.json").writeFile(data.toPrettyPrintingJson())
    }

    private data class ApixDataJson(
        val gameTime: Int,
        val gameOverData: GameOverDataCover
    )

    private data class GameOverDataCover(
        val allPlayerList: List<String>,
        val winPlayerList: List<String>,
        val mapName: String,
        val playerData: Map<String, Map<String, Int>>,
        val replayName: String
    )
}