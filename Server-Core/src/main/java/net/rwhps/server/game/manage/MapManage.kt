/*
 * Copyright 2020-2024 RW-HPS Team and contributors.
 *  
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.game.manage

import net.rwhps.server.data.global.Data
import net.rwhps.server.game.GameMaps
import net.rwhps.server.struct.map.OrderedMap
import net.rwhps.server.util.algorithms.Base64
import net.rwhps.server.util.compression.CompressionDecoderUtils
import net.rwhps.server.util.file.FileUtils
import net.rwhps.server.util.log.Log
import java.io.File

object MapManage {

    /** 地图数据  */
    val maps = GameMaps()
    val mapsData = OrderedMap<String, GameMaps.MapData>(8)

    fun readMapAndSave() {
        readList(FileUtils.getFolder(Data.ServerMapsPath)) { file, postpone, name ->
            when (postpone) {
                ".tmx" -> try {
                    mapsData[name] = GameMaps.MapData(GameMaps.MapType.CustomMap, GameMaps.MapFileType.File, name)
                } catch (exception: Exception) {
                    Log.error("read tmx Maps", exception)
                }
                ".zip" -> try {
                    CompressionDecoderUtils.zip(file).use {
                        val zipTmx = it.getTheFileNameOfTheSpecifiedSuffixInTheZip("tmx")
                        zipTmx.eachAll { zipMapName: String ->
                            mapsData[zipMapName] = GameMaps.MapData(
                                    GameMaps.MapType.CustomMap, GameMaps.MapFileType.Zip, zipMapName, name
                            )
                        }
                    }
                } catch (exception: Exception) {
                    Log.error("ZIP READ", exception)
                }
            }
        }
        readList(FileUtils.getFolder(Data.ServerSavePath)) { file, postpone, name ->
            when (postpone) {
                ".rwsave" -> try {
                    mapsData[name] = GameMaps.MapData(GameMaps.MapType.SavedGames, GameMaps.MapFileType.File, name)
                } catch (exception: Exception) {
                    Log.error("read save Maps", exception)
                }
                ".zip" -> try {
                    CompressionDecoderUtils.zip(file).use {
                        val zipSave = it.getTheFileNameOfTheSpecifiedSuffixInTheZip("save")
                        zipSave.eachAll { zipSaveName: String ->
                            mapsData[zipSaveName] = GameMaps.MapData(
                                    GameMaps.MapType.SavedGames, GameMaps.MapFileType.Zip, zipSaveName, name
                            )
                        }
                    }
                } catch (exception: Exception) {
                    Log.error("ZIP READ", exception)
                }
            }
        }
    }

    private fun readList(fileUtils: FileUtils, process: (File, String, String)->Unit) {
        val list = fileUtils.fileListNotNullSizeSort
        list.eachAll { e: File ->
            val original = if (Base64.isBase64(e.name)) Base64.decodeString(e.name) else e.name
            val postpone = original.substring(original.lastIndexOf("."))
            val name = original.substring(0, original.length - postpone.length)
            process(e, postpone, name)
        }
    }
}