/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.data.global

import net.rwhps.server.core.ServiceLoader
import net.rwhps.server.net.GroupNet
import net.rwhps.server.net.NetService
import net.rwhps.server.net.core.IRwHps
import net.rwhps.server.struct.Seq
import net.rwhps.server.util.alone.BlackList

/**
 * @author RW-HPS/Dr
 */
object NetStaticData {
    @JvmField
    val groupNet = GroupNet()
    /** Single Room Mode No ID required */
    val relay = Relay("RW-HPS Beta Relay",groupNet)
        get() {
            field.closeRoom = false
            return field
        }
    @JvmField
    val blackList = BlackList()

    var ServerNetType: IRwHps.NetType = IRwHps.NetType.NullProtocol
        set(value) {
            field = value
            if (value != IRwHps.NetType.NullProtocol) {
                /* 设置协议后会自动初始化IRwHps */
                RwHps = ServiceLoader.getService(ServiceLoader.ServiceType.IRwHps,"IRwHps", IRwHps.NetType::class.java).newInstance(value) as IRwHps
            }
        }
    lateinit var RwHps: IRwHps

    @JvmField
    var netService = Seq<NetService>(4)

    @JvmStatic
    fun checkServerStartNet(run : (()->Unit)?) : Boolean {
        if (this::RwHps.isInitialized) {
            run?.let { it() }
            return true
        }
        return false
    }
}