///*
// * Copyright 2020-2023 RW-HPS Team and contributors.
// *
// * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
// * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
// *
// * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
// */
//
//package net.rwhps.server.command.relay
//
//import net.rwhps.server.custom.RCNBind
//import net.rwhps.server.data.global.Data
//import net.rwhps.server.data.global.NetStaticData
//import net.rwhps.server.data.global.Relay
//import net.rwhps.server.data.plugin.PluginManage
//import net.rwhps.server.net.netconnectprotocol.internal.relay.fromRelayJumpsToAnotherServer
//import net.rwhps.server.net.netconnectprotocol.realize.GameVersionRelay
//import net.rwhps.server.util.IsUtil
//import net.rwhps.server.util.RandomUtil
//import net.rwhps.server.util.Time
//import net.rwhps.server.util.game.CommandHandler
//import net.rwhps.server.util.threads.ServerUploadData
//
//internal class RelayClientCommandsBAK(handler: CommandHandler) {
//    private val localeUtil = Data.i18NBundle
//
//    private fun isAdmin(con: GameVersionRelay, sendMsg: Boolean = true): Boolean {
//        if (con.relay?.admin === con) {
//            return true
//        }
//        if (sendMsg) {
//            sendMsg(con,localeUtil.getinput("err.noAdmin"))
//        }
//        return false
//    }
//
//    init {
//        handler.register("help", "clientCommands.help") { _: Array<String>?, con: GameVersionRelay ->
//            val str = StringBuilder(16)
//            for (command in handler.commandList) {
//                if (command.description.startsWith("#")) {
//                    str.append("   ").append(command.text).append(if (command.paramText.isEmpty()) "" else " ")
//                        .append(command.paramText).append(" - ").append(command.description.substring(1))
//                        .append(Data.LINE_SEPARATOR)
//                } else {
//                    if ("HIDE" == command.description) {
//                        continue
//                    }
//                    str.append("   ").append(command.text).append(if (command.paramText.isEmpty()) "" else " ")
//                        .append(command.paramText).append(" - ").append(localeUtil.getinput(command.description))
//                        .append(Data.LINE_SEPARATOR)
//                }
//            }
//            sendMsg(con,str.toString())
//        }
//
//        handler.register("jump","<ip/id>", "#jump Server") { args: Array<String>, con: GameVersionRelay ->
//            if (!isAdmin(con,false)) {
//                con.sendPacket(fromRelayJumpsToAnotherServer(args[0]))
//            } else {
//                sendMsg(con,"You Is ADMIN !")
//            }
//        }
//
////        handler.register("am", "<on/off>", "#混战") { args: Array<String>, con: GameVersionRelay ->
////            con.relay!!.dogfightLock = "on" == args[0]
////            if (con.relay!!.dogfightLock) {
////                con.relay!!.abstractNetConnectIntMap.forEach {
////                    if (it.value.playerRelay != null) {
////                        it.value.sendPackageToHOST(chatUserMessagePacketInternal("-qc -self_team ${it.value.playerRelay!!.site + 1}"))
////                    }
////                }
////            }
////            sendMsg(con, localeUtil.getinput("server.amTeam", if (con.relay!!.dogfightLock) "开启" else "关闭"))
////        }
//
//        handler.register("toup", "#UpList") { _: Array<String>, con: GameVersionRelay ->
//            if (isAdmin(con)) {
//
//                if (!RCNBind.checkBind(con.registerPlayerId!!) && RCNBind.tourist(con.ipLong24)) {
//                    sendMsg(con,"[没有绑定]: 超过游客限制 不允许使用 UP, 请绑定以解除限制")
//                    return@register
//                }
//                if (!RCNBind.checkBind(con.registerPlayerId!!)) {
//                    sendMsg(con,"欢迎您 游客!")
//                }
//
//                if (con.relay!!.relayData.uplistStatus == Relay.RelayData.UpListStatus.UpIng) {
//                    sendMsg(con,"你已经在列表了")
//                } else if (con.relay!!.relayData.uplistStatus == Relay.RelayData.UpListStatus.NoUp) {
//                    val status = con.relay!!.relayData.up()
//
//                    if (status) {
//                        sendMsg(con,Data.i18NBundle.getinput("relay.uplist", con.relay!!.relayData.port.toString(), con.relay!!.id, con.relay!!.internalID))
//                    } else {
//                        sendMsg(con,Data.i18NBundle.getinput("relay.uplistno",Data.configRelayPublish.MainID+con.relay!!.id))
//                    }
//                } else {
//                    sendMsg(con,"你已经上过列表了")
//                }
//            }
//        }
//
//        handler.register("tonp", "#Remove List") { _: Array<String>, con: GameVersionRelay ->
//            if (isAdmin(con)) {
//                if (con.relay!!.relayData.uplistStatus.ordinal < Relay.RelayData.UpListStatus.UpIng.ordinal) {
//                    sendMsg(con,"你不在列表")
//                } else if (con.relay!!.relayData.uplistStatus == Relay.RelayData.UpListStatus.UpIng) {
//                    ServerUploadData.sendPostRM(con.relay!!.id.toInt())
//                    sendMsg(con,"下列表拉!")
//                }
//            }
//        }
//
//        handler.register("allmute","#Remove List") { _: Array<String>, con: GameVersionRelay ->
//            if (isAdmin(con)) {
//                con.relay!!.allmute = !con.relay!!.allmute
//                sendMsg(con,"全局禁言状态是 :  ${if (con.relay!!.allmute) "开启" else "关闭"}")
//            }
//        }
//
//        handler.register("code" ,"#Remove List") { _: Array<String>, con: GameVersionRelay ->
//            if (isAdmin(con)) {
//                if (con.data == null) {
//                    sendMsg(con,"你的没有绑定战队")
//                    return@register
//                }
//                if (con.data!!.newBindCode) {
//                    val bind = RandomUtil.getRandomString(6)
//                    RCNBind.newBindCode.put(bind.uppercase(), arrayOf(con.data!!.index.toString(),con.data!!.qq.toString(),con.data!!.userName))
//                    sendMsg(con,"生成的绑定码: $bind")
//                } else {
//                    sendMsg(con,"你的权限不够")
//                }
//            }
//        }
//
//        handler.register("hello" ,"#Join Hello Msg") { _: Array<String>, con: GameVersionRelay ->
//            if (con.data == null) {
//                sendMsg(con,"你的没有绑定")
//                return@register
//            }
//            con.data!!.sendHelloMsg = !con.data!!.sendHelloMsg
//            sendMsg(con,"你目前进服消息提示是 : ${if (con.data!!.sendHelloMsg) "开启" else "关闭"}")
//        }
//
//        handler.register("name" ,"<Name>","#Set Bind Name") { args: Array<String>, con: GameVersionRelay ->
//            if (con.data == null) {
//                sendMsg(con,"你没有绑定")
//                return@register
//            }
//            con.data!!.userName = args[0]
//            sendMsg(con,"你新名字是 : ${con.data!!.userName}")
//        }
//
//        handler.register("bind" ,"<CODE>","#Set Bind Name") { args: Array<String>, con: GameVersionRelay ->
//            if (con.data == null) {
//                sendMsg(con,"你没有进行单人绑定")
//                return@register
//            }
//            if (args[0].length < 3) {
//                sendMsg(con,"码太短")
//                return@register
//            }
//
//            val code = if (args[0].startsWith("RB",true)) args[0].substring(2) else args[0]
//            if (Data.configRelayPublish.BindCustom.containsKey(code)) {
//                val data: Array<String> = Data.configRelayPublish.BindCustom[code]!!
//                con.data!!.groupQQ = data[1].toLong()
//                con.data!!.groupIndex = data[0].toInt()
//                con.data!!.groupName = data[2]
//                con.data!!.newBindCode = true
//            } else if (RCNBind.newBindCode.containsKey(code)) {
//                val data: Array<String> = RCNBind.newBindCode[code]!!
//                con.data!!.groupQQ = data[1].toLong()
//                con.data!!.groupIndex = data[0].toInt()
//                con.data!!.groupName = data[2]
//            } else {
//                sendMsg(con,"码无效")
//                return@register
//            }
//            con.data!!.group = true
//            sendMsg(con,"你新绑定是 : ${con.data!!.groupName}")
//        }
//        handler.register("rmbind" ,"#Set Bind Name") { _: Array<String>, con: GameVersionRelay ->
//            if (con.data == null) {
//                sendMsg(con,"你没有绑定")
//                return@register
//            }
//            if (!con.data!!.group) {
//                sendMsg(con,"你没有绑定团队")
//                return@register
//            }
//            con.data!!.groupName = ""
//            con.data!!.groupIndex = 0
//            con.data!!.groupQQ = 0
//            con.data!!.group = false
//            con.data!!.newBindCode = false
//            sendMsg(con,"清理绑定 : OK")
//        }
//
//        PluginManage.runRegisterRelayClientCommands(handler)
//    }
//
//    private fun sendMsg(con: GameVersionRelay, msg: String) {
//        con.sendPacket(NetStaticData.RwHps.abstractNetPacket.getChatMessagePacket(msg,"RELAY-CN",5))
//    }
//
//    private fun findPlayer(con: GameVersionRelay, findIn: String): GameVersionRelay? {
//        var conTg: GameVersionRelay? = null
//
//        if (con.relay!!.relayData.uplistStatus == Relay.RelayData.UpListStatus.NoUp) {
//            sendMsg(con,"NoUpList 禁止使用本命令")
//            return conTg
//        }
//
//        var findNameIn: String? = null
//        var findPositionIn: Int? = null
//
//        if (IsUtil.isNumeric(findIn)) {
//            findPositionIn = findIn.toInt()-1
//        } else {
//            findNameIn = findIn
//        }
//
//        findNameIn?.let { findName ->
//            var count = 0
//            con.relay!!.abstractNetConnectIntMap.values.forEach {
//                if (it.playerRelay!!.name.contains(findName,ignoreCase = true)) {
//                    conTg = it
//                    count++
//                }
//            }
//            if (count > 1) {
//                sendMsg(con,"目标不止一个, 请不要输入太短的玩家名")
//                return@let
//            }
//            if (conTg == null) {
//                sendMsg(con,"找不到玩家")
//                return@let
//            }
//        }
//
//        findPositionIn?.let {findPosition ->
//            con.relay!!.abstractNetConnectIntMap.values.forEach {
//                if (it.playerRelay?.site == findPosition) {
//                    conTg = it
//                }
//            }
//            if (conTg == null) {
//                sendMsg(con,"找不到玩家")
//                return@let
//            }
//        }
//
//        return conTg
//    }
//}