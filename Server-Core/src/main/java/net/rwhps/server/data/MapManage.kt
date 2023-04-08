/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.data

import net.rwhps.server.data.global.Data
import net.rwhps.server.game.GameMaps
import net.rwhps.server.struct.OrderedMap
import net.rwhps.server.util.algorithms.Base64
import net.rwhps.server.util.compression.CompressionDecoderUtils
import net.rwhps.server.util.file.FileUtil
import net.rwhps.server.util.log.Log
import java.io.File

object MapManage {

    /** 地图数据  */
    val maps = GameMaps()
    val mapsData = OrderedMap<String, GameMaps.MapData>(8)

    fun checkMaps() {
        val list = FileUtil.getFolder(Data.Plugin_Maps_Path).fileListNotNullSizeSort
        list.eachAll { e: File ->
            val original = if (Base64.isBase64(e.name)) Base64.decodeString(e.name) else e.name
            val postpone = original.substring(original.lastIndexOf("."))
            val name = original.substring(0, original.length - postpone.length)
            when (postpone) {
                ".tmx" ->   try {
                    mapsData.put(name, GameMaps.MapData(GameMaps.MapType.customMap, GameMaps.MapFileType.file, name))
                } catch (exception: Exception) {
                    Log.error("read tmx Maps", exception)
                }
                ".save" ->  try {
                    mapsData.put(name, GameMaps.MapData(GameMaps.MapType.savedGames, GameMaps.MapFileType.file, name))
                } catch (exception: Exception) {
                    Log.error("read save Maps", exception)
                }
                ".zip" ->   try {
                    CompressionDecoderUtils.zip(e).use {
                        val zipTmx = it.getTheFileNameOfTheSpecifiedSuffixInTheZip("tmx")
                        zipTmx.eachAll { zipMapName: String -> mapsData.put(zipMapName,
                            GameMaps.MapData(GameMaps.MapType.customMap, GameMaps.MapFileType.zip, zipMapName, original)
                        ) }
                        val zipSave = it.getTheFileNameOfTheSpecifiedSuffixInTheZip("save")
                        zipSave.eachAll { zipSaveName: String -> mapsData.put(zipSaveName,
                            GameMaps.MapData(
                                GameMaps.MapType.savedGames,
                                GameMaps.MapFileType.zip,
                                zipSaveName,
                                original
                            )
                        ) }
                    }
                } catch (exception: Exception) {
                    Log.error("ZIP READ", exception)
                }
                else -> {}
            }
        }
    }
}