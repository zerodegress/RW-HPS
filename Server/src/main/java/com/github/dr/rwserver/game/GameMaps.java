package com.github.dr.rwserver.game;

/**
 * @author Dr
 */
public class GameMaps {

    public MapType mapType = MapType.defaultMap;
    public int mapSize = 0;
    public byte[] bytesMap = null;
    public String mapName = "Crossing Large (10p)";
    public String mapPlayer = "[z;p10]";

    public static enum MapType {
        /**
         * ?
         */
        defaultMap, customMap, savedGames
    }

    public static class MapData {
        public final MapType mapType;
        public final int mapSize;
        public final byte[] bytesMap;

        public MapData(final MapType mapType,final byte[] bytesMap) {
            this.mapType = mapType;
            this.mapSize = bytesMap.length;
            this.bytesMap = bytesMap;
        }

        public MapData(final MapType mapType,final int mapSize,final byte[] bytesMap) {
            this.mapType = mapType;
            this.mapSize = mapSize;
            this.bytesMap = bytesMap;
        }
    }
}
