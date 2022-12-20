/*
 * Copyright 2020-2022 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

@file:JvmName("StringUtils")
@file:JvmMultifileClass

package net.rwhps.server.util.inline

import net.rwhps.server.util.ExtractUtil.bytesToHex
import net.rwhps.server.util.ExtractUtil.hexToByteArray


fun Byte.toStringHex() : String {
    return bytesToHex(this)
}

fun ByteArray.toStringHex() : String {
    return bytesToHex(this)
}

fun String.hexToByte() : Byte {
    return hexToByteArray(this)[0]
}

fun String.hexToBytes() : ByteArray {
    return hexToByteArray(this)
}