package com.github.dr.rwserver.game;

import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.util.file.FileUtil;
import com.github.dr.rwserver.util.log.Log;
import com.github.dr.rwserver.util.zip.zip.ZipDecoder;

import static com.github.dr.rwserver.game.GameMaps.MapFileType.file;

/**
 * @author Dr
 */
public class GameMaps {

    public MapType mapType = MapType.defaultMap;
    public MapData mapData;
    public String mapName = "Crossing Large (10p)";
    public String mapPlayer = "[z;p10]";

    public static enum MapType {
        /**
         * ?
         */
        defaultMap, customMap, savedGames
    }

    public static enum MapFileType {
        /**
         * ?
         */
        file, zip, web
    }

    public static class MapData {
        public final MapType mapType;
        public final MapFileType mapFileType;
        public final String mapFileName;
        public final String zipFileName;

        public int mapSize = 0;
        public byte[] bytesMap = null;

        public MapData(final MapType mapType,final MapFileType mapFileType,final String mapFileName) {
            this.mapType = mapType;
            this.mapFileType = mapFileType;
            this.mapFileName = mapFileName;
            this.zipFileName = null;
        }

        public MapData(final MapType mapType,final MapFileType mapFileType,final String mapFileName,final String zipFileName) {
            this.mapType = mapType;
            this.mapFileType = mapFileType;
            this.mapFileName = mapFileName;
            this.zipFileName = zipFileName;
        }

        public MapData(final MapType mapType,final byte[] bytesMap) {
            this.mapType = mapType;
            this.mapFileType = file;
            this.mapFileName = null;
            this.zipFileName = null;
            this.mapSize = bytesMap.length;
            this.bytesMap = bytesMap;
        }

        public MapData(final MapType mapType,final int mapSize,final byte[] bytesMap) {
            this.mapType = mapType;
            this.mapFileType = file;
            this.mapFileName = null;
            this.zipFileName = null;
            this.mapSize = mapSize;
            this.bytesMap = bytesMap;
        }

        public String getType() {
            return mapType.name().equals("savedGames") ? ".save" : ".tmx";
        }

        public void readMap() {
            FileUtil fileUtil = FileUtil.File(Data.Plugin_Maps_Path);
            switch (mapFileType) {
                case file:
                    try {
                        this.bytesMap = fileUtil.toPath(mapFileName+getType()).readFileByte();
                        this.mapSize = bytesMap.length;
                    } catch (Exception e) {
                        Log.error("Read Map Bytes Error",e);
                    }
                    break;
                case zip:
                    try {
                        this.bytesMap = new ZipDecoder(fileUtil.toPath(zipFileName).getFile()).GetTheFileBytesOfTheSpecifiedSuffixInTheZip(this);
                        this.mapSize = bytesMap.length;
                    } catch (Exception e) {
                        Log.error("Read Map Bytes Error",e);
                    }
                    break;
                case web:
                    break;
                default:
                    break;
            }
        }
    }
}
