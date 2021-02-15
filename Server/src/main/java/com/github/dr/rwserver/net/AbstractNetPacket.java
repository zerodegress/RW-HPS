package com.github.dr.rwserver.net;

import com.github.dr.rwserver.data.Player;
import com.github.dr.rwserver.game.GameCommand;
import com.github.dr.rwserver.io.Packet;
import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.util.zip.gzip.GzipEncoder;
import io.netty.buffer.ByteBuf;

import java.io.IOException;

public interface AbstractNetPacket {
    /**
     * 获取系统命名的消息包
     * SERVER: ...
     * @param msg The message
     * @return ByteBuf
     */
    ByteBuf getSystemMessageByteBuf(String msg) throws IOException;
    /**
     * 发送用户名命名的消息
     * @param      msg     The message
     * @param      sendBy  The send by
     * @param      team    The team
     */
    ByteBuf getChatMessageByteBuf(String msg, String sendBy, int team) throws IOException;
    /**
     * Ping
     */
    ByteBuf getPingByteBuf(Player player) throws IOException;
    /**
     * 获取时刻包
     * @param tick Tick
     */
    ByteBuf getTickByteBuf(int tick) throws IOException;
    /**
     * 获取时刻包
     * @param tick Tick
     * @param cmd 位移
     */
    ByteBuf getGameTickCommandByteBuf(int tick, GameCommand cmd) throws IOException;
    /**
     * 获取时刻包
     * @param tick Tick
     * @param cmd 多位移
     */
    ByteBuf getGameTickCommandsByteBuf(int tick, Seq<GameCommand> cmd) throws IOException;
    /**
     * 获取队伍包
     * @return 队伍包
     */
    GzipEncoder getTeamDataByteBuf() throws IOException;
    /**
     * 转换GameSave包
     * @param packet packet
     * @return 包
     */
    ByteBuf convertGameSaveDataByteBuf(Packet packet) throws IOException;
    /**
     * 开始游戏
     * @throws IOException err
     */
    ByteBuf getStartGameByteBuf() throws IOException;
}
