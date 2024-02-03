/*
 * Copyright 2020-2024 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.plugin.beta.statisticsbackend.data

data class BaseData(
    val ServerRunPort: Map<Int, Int>,// 每个Port的使用次数
    val ServerNetType: Map<String, Int>,// 每个协议的使用次数
    val SystemCount: Map<String, Int>,// 系统统计
    val JavaCount: Map<String, Int>,// Java版本统计
    val VersionCount: Map<String?, Int>,// Java版本统计
    val ServerData: ServerData,// Server服务器统计
    val RelayData: RelayData, // Relay服务器数据统计
) {
    companion object {
        data class ServerData(
            val ServerSize: Int,// 服务器个数
            val PlayerSize: Int,// 玩家总数
            val PlayerVersion: Map<Int, Int>,// Server协议下 玩家使用的版本数量 (使用内部版本号)
            val IpCountry: Map<String, Int>,// Server协议下 服务器的(RW-HPS)国家IP数
            val IpPlayerCountry: Map<String, Int>,// Server协议下 玩家国家IP数
        )

        data class RelayData(
            val RelaySize: Int,// Relay服务器总数
            val PlayerSize: Int,// Relay玩家总数
            val RoomAllSize: Int,// Relay 全部房间数
            val RoomNoStartSize: Int,// Relay没有开始游戏房间总数
            val RoomPublicListSize: Int,// Relay房间在列表总数
            val PlayerVersion: Map<Int, Int>,// Relay协议下 玩家使用的版本数量 (使用内部版本号)
            val IpCountry: Map<String, Int>,// Relay协议下 服务器的(RW-HPS)国家IP数
            val IpPlayerCountry: Map<String, Int>,// Relay协议下 玩家国家IP数
        )
    }
}
