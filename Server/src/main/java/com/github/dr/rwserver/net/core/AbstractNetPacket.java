package com.github.dr.rwserver.net.core;

import com.github.dr.rwserver.data.Player;
import com.github.dr.rwserver.game.GameCommand;
import com.github.dr.rwserver.io.Packet;
import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.util.zip.gzip.GzipEncoder;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * @author Dr
 */
public interface AbstractNetPacket {
    /**
     * 获取系统命名的消息包
     * SERVER: ...
     * @param msg The message
     * @return Packet
     * @throws IOException err
     */
    Packet getSystemMessagePacket(String msg) throws IOException;
    /**
     * 发送用户名命名的消息
     * @param      msg     The message
     * @param      sendBy  The send by
     * @param      team    The team
     * @return Packet
     * @throws IOException err
     */
    Packet getChatMessagePacket(String msg, String sendBy, int team) throws IOException;
    /**
     * Ping
     * @param player Player
     * @return Packet
     * @throws IOException err
     */
    Packet getPingPacket(Player player) throws IOException;
    /**
     * 获取时刻包
     * @param tick Tick
     * @return Packet
     * @throws IOException err
     */
    Packet getTickPacket(int tick) throws IOException;
    /**
     * 获取时刻包
     * @param tick Tick
     * @param cmd 位移
     * @return Packet
     * @throws IOException err
     */
    Packet getGameTickCommandPacket(int tick, GameCommand cmd) throws IOException;
    /**
     * 获取时刻包
     * @param tick Tick
     * @param cmd 多位移
     * @return Packet
     * @throws IOException err
     */
    Packet getGameTickCommandsPacket(int tick, Seq<GameCommand> cmd) throws IOException;
    /**
     * 获取队伍包
     * @return 队伍包
     * @throws IOException err
     */
    GzipEncoder getTeamDataPacket() throws IOException;
    /**
     * 转换GameSave包
     * @param packet packet
     * @return Packet
     * @throws IOException err
     */
    Packet convertGameSaveDataPacket(Packet packet) throws IOException;
    /**
     * 开始游戏
     * @return Packet
     * @throws IOException err
     */
    Packet getStartGamePacket() throws IOException;
    /**
     * 获取包中的地图名
     * @param bytes Packet.bytes
     * @return 地图名
     * @throws IOException err
     */
    String getPacketMapName(byte[] bytes) throws IOException;

    /**
     * 退出
     * @return Packet
     * @throws IOException err
     */
    Packet getExitPacket() throws IOException;

    /**
     * 写入玩家的数据
     * @param player Player
     * @param stream Data流
     */
    void writePlayer(Player player, DataOutputStream stream) throws IOException ;

    /**
     * 获取连接包
     * @return Packet
     */
    Packet getPlayerConnectPacket();

    /**
     * 获取注册包
     * @param name Player Name
     * @param uuid Player UUID
     * @param passwd Server Passwd
     * @param key Server Register Key
     * @return Packet
     */
    Packet getPlayerRegisterPacket(String name,String uuid,String passwd,int key);

}
