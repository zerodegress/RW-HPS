/*
 * Copyright 2020-2024 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.util.compression.zip

import net.rwhps.server.func.Control.ControlFind
import net.rwhps.server.game.GameMaps
import net.rwhps.server.struct.list.Seq
import net.rwhps.server.struct.map.OrderedMap
import net.rwhps.server.util.compression.core.AbstractDecoder
import net.rwhps.server.util.file.FileName
import net.rwhps.server.util.io.IoRead
import net.rwhps.server.util.log.Log
import net.rwhps.server.util.log.exp.FileException
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry
import org.apache.commons.compress.archivers.zip.ZipFile
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel
import java.io.File
import java.io.IOException
import java.io.InputStream

/**
 * Zip
 * @property zipFile ZipFile
 * @author Dr (dr@der.kim)
 */
internal class ZipFileDecoder: AbstractDecoder {
    private val zipFile: ZipFile
    private var physicalOrder = false

    constructor(bytes: ByteArray) {
        zipFile = ZipFile(SeekableInMemoryByteChannel(bytes))
    }

    constructor(file: File) {
        zipFile = ZipFile(file)
    }

    override fun setPhysicalOrder(value: Boolean) {
        physicalOrder = value
    }

    override fun getSpecifiedSuffixInThePackage(endWith: String, withSuffix: Boolean): OrderedMap<String, ByteArray> {
        val result = OrderedMap<String, ByteArray>(8)

        zipUtils { zipEntry, multiplexingReadStream ->
            var name = FileName.getFileName(zipEntry.name)
            if (name.endsWith(endWith)) {
                if (!withSuffix) {
                    name = FileName.getFileNameNoSuffix(name)
                }
                result[name] = multiplexingReadStream.readInputStreamBytes(zipFile.getInputStream(zipEntry))
            }
            return@zipUtils ControlFind.CONTINUE
        }

        return result
    }

    override fun getSpecifiedSuffixInThePackageAllFileNameAndPath(endWithSeq: Seq<String>): OrderedMap<String, ByteArray> {
        val result = OrderedMap<String, ByteArray>(8)

        zipUtils { zipEntry, multiplexingReadStream ->
            val nameCache = zipEntry.name
            endWithSeq.eachAllFind({ nameCache.endsWith(it) }) { _: String ->
                result[nameCache] = multiplexingReadStream.readInputStreamBytes(zipFile.getInputStream(zipEntry))
            }
            return@zipUtils ControlFind.CONTINUE
        }

        return result
    }

    override fun getTheFileNameOfTheSpecifiedSuffixInTheZip(endWith: String): Seq<String> {
        val result = Seq<String>(8)

        zipUtils { zipEntry, _ ->
            val name = zipEntry.name
            if (name.endsWith(endWith)) {
                result.add(name.substring(0, name.length - name.substring(name.lastIndexOf(".")).length))
            }
            return@zipUtils ControlFind.CONTINUE
        }

        return result
    }

    @Throws(FileException::class)
    override fun getTheFileBytesOfTheSpecifiedSuffixInTheZip(mapData: GameMaps.MapData): ByteArray {
        var result: ByteArray? = null

        zipUtils { zipEntry, _ ->
            val name = zipEntry.name
            if (name.endsWith(mapData.mapType.fileType) && FileName.getFileNameNoSuffix(name).contains(mapData.mapFileName)) {
                result = IoRead.readInputStreamBytes(zipFile.getInputStream(zipEntry))
                return@zipUtils ControlFind.BREAK
            }
            return@zipUtils ControlFind.CONTINUE
        }

        return result ?: throw FileException("CANNOT_FIND_FILE")
    }

    override fun getZipNameInputStream(nameIn: String): InputStream? {
        var result: InputStream? = null

        zipUtils { zipEntry, _ ->
            if (zipEntry.name == nameIn) {
                result = zipFile.getInputStream(zipEntry)
                return@zipUtils ControlFind.BREAK
            }
            return@zipUtils ControlFind.CONTINUE
        }

        return result
    }

    override fun getZipAllBytes(withPath: Boolean): OrderedMap<String, ByteArray> {
        val result = OrderedMap<String, ByteArray>(8)

        zipUtils { zipEntry, multiplexingReadStream ->
            val nameCache = zipEntry.name
            val name = if (withPath) {
                nameCache
            } else {
                FileName.getFileName(zipEntry.name)
            }

            val bytes = multiplexingReadStream.readInputStreamBytes(zipFile.getInputStream(zipEntry))
            result[name] = bytes

            return@zipUtils ControlFind.CONTINUE
        }

        return result
    }

    override fun close() {
        this.zipFile.close()
    }

    private fun zipUtils(run: (ZipArchiveEntry, IoRead.MultiplexingReadStream) -> ControlFind) {
        try {
            var zipEntry: ZipArchiveEntry
            IoRead.MultiplexingReadStream().use { multiplexingReadStream ->
                val entries = if (physicalOrder) {
                    zipFile.entriesInPhysicalOrder
                } else {
                    zipFile.entries
                }
                while (entries.hasMoreElements()) {
                    zipEntry = entries.nextElement() as ZipArchiveEntry
                    if (!zipEntry.isDirectory) {
                        if (run(zipEntry, multiplexingReadStream) == ControlFind.BREAK) {
                            return
                        }
                    }
                }
            }
        } catch (e: IOException) {
            Log.error(e)
        }
    }
}