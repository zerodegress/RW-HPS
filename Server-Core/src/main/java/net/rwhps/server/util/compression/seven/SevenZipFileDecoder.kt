/*
 * Copyright 2020-2024 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.util.compression.seven

import net.rwhps.server.game.GameMaps
import net.rwhps.server.io.input.DisableSyncByteArrayInputStream
import net.rwhps.server.struct.list.Seq
import net.rwhps.server.struct.map.OrderedMap
import net.rwhps.server.util.compression.core.AbstractDecoder
import net.rwhps.server.util.file.FileName
import net.rwhps.server.util.io.IoRead
import net.rwhps.server.util.log.Log
import net.rwhps.server.util.log.exp.FileException
import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry
import org.apache.commons.compress.archivers.sevenz.SevenZFile
import org.apache.commons.compress.utils.SeekableInMemoryByteChannel
import java.io.File
import java.io.IOException
import java.io.InputStream

/**
 * 7z
 * @property sevenZipFile SevenZFile
 * @author Dr (dr@der.kim)
 */
internal open class SevenZipFileDecoder: AbstractDecoder {
    private val sevenZipFile: SevenZFile

    constructor(bytes: ByteArray) {
        sevenZipFile = SevenZFile(SeekableInMemoryByteChannel(bytes))
    }

    constructor(file: File) {
        sevenZipFile = SevenZFile(file)
    }

    override fun getSpecifiedSuffixInThePackage(endWith: String, withSuffix: Boolean): OrderedMap<String, ByteArray> {
        val data = OrderedMap<String, ByteArray>(8)
        var sevenZipEntry: SevenZArchiveEntry?
        try {
            IoRead.MultiplexingReadStream().use { multiplexingReadStream ->
                while (sevenZipFile.nextEntry.also { sevenZipEntry = it } != null) {
                    val nameCache = sevenZipEntry!!.name
                    var name = nameCache.split("/").toTypedArray()[nameCache.split("/").toTypedArray().size - 1]
                    if (name.endsWith(endWith)) {
                        name = if (withSuffix) {
                            name
                        } else {
                            name.substring(0, name.length - name.substring(name.lastIndexOf(".")).length)
                        }
                        data.put(name, multiplexingReadStream.readInputStreamBytes(sevenZipFile.getInputStream(sevenZipEntry)))
                    }
                }
            }
        } catch (e: IOException) {
            Log.error(e)
        }
        return data
    }

    override fun getSpecifiedSuffixInThePackageAllFileNameAndPath(endWithSeq: Seq<String>): OrderedMap<String, ByteArray> {
        val data = OrderedMap<String, ByteArray>(8)
        try {
            var sevenZipEntry: SevenZArchiveEntry?
            IoRead.MultiplexingReadStream().use { multiplexingReadStream ->
                while (sevenZipFile.nextEntry.also { sevenZipEntry = it } != null) {
                    val nameCache = sevenZipEntry!!.name

                    endWithSeq.eachAllFind({ nameCache.endsWith(it) }) { _: String ->
                        //Log.debug(nameCache,name)
                        data.put(nameCache, multiplexingReadStream.readInputStreamBytes(sevenZipFile.getInputStream(sevenZipEntry)))
                    }
                }
            }
        } catch (e: IOException) {
            Log.error(e)
        }
        return data
    }

    override fun getTheFileNameOfTheSpecifiedSuffixInTheZip(endWith: String): Seq<String> {
        val data = Seq<String>(8)
        var sevenZipEntry: SevenZArchiveEntry?
        while (sevenZipFile.nextEntry.also { sevenZipEntry = it } != null) {
            val name = sevenZipEntry!!.name
            if (name.endsWith(endWith)) {
                data.add(name.substring(0, name.length - name.substring(name.lastIndexOf(".")).length))
            }
        }
        return data
    }

    @Throws(Exception::class)
    override fun getTheFileBytesOfTheSpecifiedSuffixInTheZip(mapData: GameMaps.MapData): ByteArray {
        var sevenZipEntry: SevenZArchiveEntry?
        while (sevenZipFile.nextEntry.also { sevenZipEntry = it } != null) {
            val name = sevenZipEntry!!.name
            if (name.endsWith(mapData.mapType.fileType) && FileName.getFileNameNoSuffix(name).contains(mapData.mapFileName)) {
                return IoRead.readInputStreamBytes(sevenZipFile.getInputStream(sevenZipEntry))
            }
        }
        throw FileException("CANNOT_FIND_FILE")
    }

    override fun getZipNameInputStream(nameIn: String): InputStream? {
        var sevenZipEntry: SevenZArchiveEntry?
        try {
            IoRead.MultiplexingReadStream().use { multiplexingReadStream ->
                while (sevenZipFile.nextEntry.also { sevenZipEntry = it } != null) {
                    val nameCache = sevenZipEntry!!.name
                    Log.clog(nameCache)
                    //val name = nameCache.split("/").toTypedArray()[nameCache.split("/").toTypedArray().size - 1]
                    if (nameIn == nameCache) {
                        return DisableSyncByteArrayInputStream(
                                multiplexingReadStream.readInputStreamBytes(sevenZipFile.getInputStream(sevenZipEntry))
                        )
                    }
                }
            }
        } catch (e: IOException) {
            Log.error(e)
        }
        return null
    }

    override fun getZipAllBytes(withPath: Boolean): OrderedMap<String, ByteArray> {
        val data = OrderedMap<String, ByteArray>(8)
        try {
            var sevenZipEntry: SevenZArchiveEntry?
            IoRead.MultiplexingReadStream().use { multiplexingReadStream ->
                while (sevenZipFile.nextEntry.also { sevenZipEntry = it } != null) {
                    if (!sevenZipEntry!!.isDirectory) {
                        val nameCache = sevenZipEntry!!.name
                        val name = if (withPath) {
                            nameCache
                        } else {
                            nameCache.split("/").toTypedArray()[nameCache.split("/").toTypedArray().size - 1]
                        }
                        val bytes = multiplexingReadStream.readInputStreamBytes(sevenZipFile.getInputStream(sevenZipEntry))
                        data.put(name, bytes)
                    }
                }
            }
        } catch (e: IOException) {
            Log.error(e)
        }
        return data
    }

    override fun close() {
        this.sevenZipFile.close()
    }
}