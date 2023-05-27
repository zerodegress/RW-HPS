/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.util

/**
 * @date  2023/5/25 12:00
 * @author  RW-HPS/Dr
 */
object SystemSetProperty {
    fun setOnlyIpv4() {
        // F U C K IPV6
        System.setProperty("java.net.preferIPv6Stack","false")
        System.setProperty("java.net.preferIPv4Stack","true")
    }

    fun setAwtHeadless() {
        // F U C K Termux
        System.setProperty("java.awt.headless","true")
    }
}