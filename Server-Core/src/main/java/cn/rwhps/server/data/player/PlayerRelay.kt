/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */
package cn.rwhps.server.data.player

import cn.rwhps.server.data.totalizer.TimeAndNumber
import cn.rwhps.server.net.netconnectprotocol.realize.GameVersionRelay
import org.apache.commons.text.similarity.JaroWinklerSimilarity

/**
 * RELAY Detailed stats for players inside
 *
 * @property con GameVersionRelay
 * @property uuid String
 * @property name String
 * @property team Int
 * @property site Int
 * @property watch Boolean
 * @property mute Long
 * @constructor
 */
class PlayerRelay(
    val con: GameVersionRelay,
    val uuid: String,
    var name: String
) {
    var nowName = ""

    var team = 0
        set(value) { watch = (value == -3) ; field = value }
    /** List position  */
    var site = 0
    var watch = false
        private set
    /** Mute expiration time */
    var mute = false

    var lastSentMessage: String = ""
    val messageSimilarity = JaroWinklerSimilarity()
    val messageSimilarityCount = TimeAndNumber(60,5)

    override fun toString(): String {
        return  """
                Player :
                Name : $name
                uuid-Hash : $uuid
                
                Site : $site
                Team : $team
                """.trimIndent()
    }
}