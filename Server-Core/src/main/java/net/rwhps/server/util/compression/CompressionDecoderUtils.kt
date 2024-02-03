/*
 * Copyright 2020-2024 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.util.compression

import net.rwhps.server.util.compression.core.AbstractDecoder
import net.rwhps.server.util.compression.seven.SevenZipFileDecoder
import net.rwhps.server.util.compression.zip.ZipFileDecoder
import net.rwhps.server.util.io.IoRead
import java.io.File
import java.io.InputStream

/**
 * 获取解压构造器
 *
 * 不支持 网络流 , 需要本地全部读取成 bytes
 *
 * @author Dr (dr@der.kim)
 */
object CompressionDecoderUtils {

    fun zip(file: File): AbstractDecoder = ZipFileDecoder(file)
    fun zip(bytes: ByteArray): AbstractDecoder = ZipFileDecoder(bytes)
    fun zipAllReadStream(inStream: InputStream): AbstractDecoder = ZipFileDecoder(IoRead.readInputStreamBytes(inStream))

    fun sevenZip(file: File): AbstractDecoder = SevenZipFileDecoder(file)
    fun sevenZip(bytes: ByteArray): AbstractDecoder = SevenZipFileDecoder(bytes)
    fun sevenAllReadStream(inStream: InputStream): AbstractDecoder = sevenZip(IoRead.readInputStreamBytes(inStream))
}