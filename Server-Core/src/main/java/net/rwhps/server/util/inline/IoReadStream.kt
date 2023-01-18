/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */


@file:JvmName("InlineUtils")
@file:JvmMultifileClass

package net.rwhps.server.util.inline

import net.rwhps.server.util.file.FileUtil
import net.rwhps.server.util.io.IoRead
import java.io.InputStream

/**
 * IoReadStream
 * @author RW-HPS/Dr
 */
fun InputStream.readBytes(): ByteArray {
    return IoRead.readInputStreamBytes(this)
}

fun Class<*>.readBytes(): ByteArray {
    return this.javaClass.toString().readAsClassBytes()
}
fun String.readAsClassBytes(): ByteArray {
    return FileUtil.getInternalFileStream(
        "/"+this
            .replace("class ", "")
            .replace(".", "/")+".class"
    ).readBytes()
}