/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.util.compression

import net.rwhps.server.util.compression.core.CompressionDecoder
import net.rwhps.server.util.compression.seven.SevenZipFileDecoder
import net.rwhps.server.util.compression.zip.ZipFileDecoder
import net.rwhps.server.util.compression.zip.ZipStreamDecoder
import java.io.File
import java.io.InputStream

/**
 * @author RW-HPS/Dr
 */
object CompressionDecoderUtils {
    fun zip(file: File): CompressionDecoder = CompressionDecoder(ZipFileDecoder(file))
    fun zipSeek(inStream: InputStream): CompressionDecoder = CompressionDecoder(ZipFileDecoder(inStream))
    fun zipStream(inStream: InputStream): CompressionDecoder = CompressionDecoder(ZipStreamDecoder(inStream))

    fun sevenZip(file: File): CompressionDecoder = CompressionDecoder(SevenZipFileDecoder(file))
    fun sevenZipSeek(inStream: InputStream): CompressionDecoder = CompressionDecoder(SevenZipFileDecoder(inStream))
    fun sevenZipStream(inStream: InputStream): CompressionDecoder = sevenZipSeek(inStream)
}