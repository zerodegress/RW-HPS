package com.github.dr.rwserver.game;

/**
 * @author Dr
 */
public class GameMaps {

    public MapType MapType = MapType.defaultMap;
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
}
