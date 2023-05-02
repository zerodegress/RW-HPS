/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

function main() {
    importClass("net.rwhps.server.plugin.Plugin")
    importClass("net.rwhps.server.test.qqbot.Start")
    importClass("net.rwhps.server.custom.RCNBind")

    var plugin = new JavaAdapter(Plugin, {
        init: function () {
            Start.bot.getGroup(901913920).sendMessage(RCNBind.INSTANCE.getBindData$Server_Core("7842C79A87996F9138EB4B94BE64335F0E4C2AFB2D0737358AAD83DEC437D457").toString())
        }
    })
    return plugin
}

