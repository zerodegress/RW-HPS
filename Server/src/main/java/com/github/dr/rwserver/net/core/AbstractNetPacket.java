package com.github.dr.rwserver.net.core;

import com.github.dr.rwserver.data.Player;
import com.github.dr.rwserver.game.GameCommand;
import com.github.dr.rwserver.io.Packet;
import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.util.zip.gzip.GzipEncoder;

import java.io.IOException;

/**
 * @author Dr
 */
public interface AbstractNetPacket {
    /**
     * 获取系统命名的消息包
     * SERVER: ...
     * @param msg The message
     * @return ByteBuf
     * @throws IOException err
     */
    Packet getSystemMessageByteBuf(String msg) throws IOException;
    /**
     * 发送用户名命名的消息
     * @param      msg     The message
     * @param      sendBy  The send by
     * @param      team    The team
     * @return ByteBuf
     * @throws IOException err
     */
    Packet getChatMessageByteBuf(String msg, String sendBy, int team) throws IOException;
    /**
     * Ping
     * @param player Player
     * @return ByteBuf
     * @throws IOException err
     */
    Packet getPingByteBuf(Player player) throws IOException;
    /**
     * 获取时刻包
     * @param tick Tick
     * @return ByteBuf
     * @throws IOException err
     */
    Packet getTickByteBuf(int tick) throws IOException;
    /**
     * 获取时刻包
     * @param tick Tick
     * @param cmd 位移
     * @return ByteBuf
     * @throws IOException err
     */
    Packet getGameTickCommandByteBuf(int tick, GameCommand cmd) throws IOException;
    /**
     * 获取时刻包
     * @param tick Tick
     * @param cmd 多位移
     * @return ByteBuf
     * @throws IOException err
     */
    Packet getGameTickCommandsByteBuf(int tick, Seq<GameCommand> cmd) throws IOException;
    /**
     * 获取队伍包
     * @return 队伍包
     * @throws IOException err
     */
    GzipEncoder getTeamDataByteBuf() throws IOException;
    /**
     * 转换GameSave包
     * @param packet packet
     * @return ByteBuf
     * @throws IOException err
     */
    Packet convertGameSaveDataByteBuf(Packet packet) throws IOException;
    /**
     * 开始游戏
     * @return ByteBuf
     * @throws IOException err
     */
    Packet getStartGameByteBuf() throws IOException;
    /**
     * 获取包中的地图名
     * @param bytes Packet.bytes
     * @return 地图名
     * @throws IOException err
     */
    String getPacketMapName(byte[] bytes) throws IOException;

    /**
     * 退出
     * @return ByteBuf
     * @throws IOException err
     */
    Packet getExitByteBuf() throws IOException;

}
