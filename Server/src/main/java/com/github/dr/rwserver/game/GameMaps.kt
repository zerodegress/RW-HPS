package com.github.dr.rwserver.game

import com.github.dr.rwserver.data.global.Data
import com.github.dr.rwserver.util.file.FileUtil.Companion.toFolder
import com.github.dr.rwserver.util.log.Log.error
import com.github.dr.rwserver.util.zip.zip.ZipDecoder

/**
 * 游戏的地图数据
 * @author Dr
 */
class GameMaps {
    /** 地图类型  */
    @JvmField
    var mapType = MapType.defaultMap

    /** 地图数据  */
    @JvmField
    var mapData: MapData? = null

    /** 地图名  */
    @JvmField
    var mapName = "Crossing Large (10p)"

    /** 地图人数  */
    @JvmField
    var mapPlayer = "[z;p10]"

    /** 地图的类型  */
    enum class MapType {
        /**
         * defaultMap : 官方地图
         * customMap  : 自定义的地图
         * savedGames : 游戏里保存的进度
         */
        defaultMap, customMap, savedGames
    }

    enum class MapFileType {
        /**
         * file : 以文件的形式提供
         * zip  : 以ZIP提供(文件在ZIP内)
         * web  : 以WebFile的形式提供(需要下载)
         */
        file, zip, web
    }

    class MapData {
        @JvmField
        val mapType: MapType
        @JvmField
        val mapFileType: MapFileType
        @JvmField
        val mapFileName: String
        @JvmField
        val zipFileName: String?
        @JvmField
        var mapSize = 0
        @JvmField
        var bytesMap: ByteArray? = null

        constructor(mapType: MapType, mapFileType: MapFileType, mapFileName: String) {
            this.mapType = mapType
            this.mapFileType = mapFileType
            this.mapFileName = mapFileName
            zipFileName = null
        }

        constructor(mapType: MapType, mapFileType: MapFileType, mapFileName: String, zipFileName: String) {
            this.mapType = mapType
            this.mapFileType = mapFileType
            this.mapFileName = mapFileName
            this.zipFileName = zipFileName
        }

        /**
         * 根据地图类型获取文件后缀
         * @return 后缀
         */
        val type: String
            get() = if ("savedGames" == mapType.name) ".save" else ".tmx"

        /**
         * 懒加载
         * 读取地图到内部bytes
         */
        fun readMap() {
            val fileUtil = toFolder(Data.Plugin_Maps_Path)
            when (mapFileType) {
                MapFileType.file -> try {
                    bytesMap = fileUtil.toPath(mapFileName + type).readFileByte()
                    mapSize = bytesMap!!.size
                } catch (e: Exception) {
                    error("Read Map Bytes Error", e)
                }
                MapFileType.zip -> try {
                    bytesMap =
                        ZipDecoder(fileUtil.toPath(zipFileName!!).file).getTheFileBytesOfTheSpecifiedSuffixInTheZip(this)
                    mapSize = bytesMap!!.size
                } catch (e: Exception) {
                    error("Read Map Bytes Error", e)
                }
                MapFileType.web -> {
                }
                else -> {
                }
            }
        }

        /**
         * 清理服务器使用的地图数据
         */
        fun clean() {
            mapSize = 0
            bytesMap = null
        }
    }
}