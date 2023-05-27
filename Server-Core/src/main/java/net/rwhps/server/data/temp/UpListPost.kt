/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.data.temp

import net.rwhps.server.util.StringFilteringUtil
import net.rwhps.server.util.Time
import net.rwhps.server.util.algorithms.digest.DigestUtil
import okhttp3.FormBody
import java.math.BigInteger

/**
 * @author RW-HPS/Dr
 */
internal data class UpListPost(
    val time: Long = Time.concurrentMillis(),
    var user_id: String,
    var game_name: String = "RW-HPS",
    var _1: String,
    var tx2: String,
    var tx3: String,
    var game_version: String,
    var game_version_string: String,
    var game_version_beta: String,
    var private_token: String,
    var private_token_2: String,
    var confirm: String,
    var password_required: String,
    var created_by: String,
    var private_ip: String = "10.0.0.1",
    var port_number: Int = 5123,
    var game_map: String,
    var game_mode: String = "skirmishMap",
    var game_status: String = "battleroom",
    var player_count: Int = 0,
    var max_player_count: Int = 10,
) {
    val add: FormBody.Builder
        get() {
            return FormBody.Builder()
                .add("action","add")
                .add("user_id",user_id)
                .add("game_name",game_name)
                .add("_1","$time")
                .add("tx2",tx2)
                .add("tx3",tx3)
                .add("game_version=",game_version)
                .add("game_version_string",game_version_string)
                .add("game_version_beta",game_version_beta)
                .add("private_token",private_token)
                .add("private_token_2",private_token_2)
                .add("confirm",confirm)
                .add("password_required",password_required)
                .add("created_by",created_by)
                .add("private_ip",private_ip)
                .add("port_number","$port_number")
                .add("game_map",game_map)
                .add("game_mode",game_mode)
                .add("game_status",game_status)
                .add("player_count","$player_count")
                .add("max_player_count","$max_player_count")
        }
    val up: FormBody.Builder
        get() {
            return FormBody.Builder()
                .add("action","update")
                .add("id",user_id)
                .add("game_name","RW-HPS")
                .add("private_token",private_token)
                .add("password_required",password_required)
                .add("created_by",created_by)
                .add("private_ip",private_ip)
                .add("port_number","$port_number")
                .add("game_map",game_map)
                .add("game_mode",game_mode)
                .add("game_status",game_status)
                .add("player_count","$player_count")
                .add("max_player_count","$max_player_count")
        }
    val portCheck: FormBody.Builder
        get() {
            return FormBody.Builder()
                .add("action","self_info")
                .add("port","$port_number")
                .add("id",user_id)
                .add("tx3",reup("-" + user_id + "54"))
        }
    val rmList: FormBody.Builder
        get() {
            return FormBody.Builder()
                .add("action","remove")
                .add("id",user_id)
                .add("private_token",private_token)
        }

    private fun reup(str: String): String {
        return StringFilteringUtil.cutting(BigInteger(1, DigestUtil.sha256(str)).toString(16).uppercase(), 4)
    }
}
