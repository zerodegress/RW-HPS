package com.github.dr.rwserver.net;

import com.github.dr.rwserver.data.Player;
import com.github.dr.rwserver.io.Packet;
import com.github.dr.rwserver.util.log.Log;
import com.github.dr.rwserver.util.zip.gzip.GzipEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.io.IOException;
import java.net.SocketAddress;

/**
 * @author Dr
 * @date 2020/9/5 13:31
 */
public interface AbstractNetConnect {
    /*
     * TODO : AntiCheats
     */

    /**
     * 获取版本协议
     * @param sockAds SocketAds
     * @return 协议
     */
    AbstractNetConnect getVersionNet(SocketAddress sockAds, ByteBufAllocator bufAllocator);



    /**
     * Import
     */
    /**
     * 设置玩家
     * @param player Player
     */
    void setPlayer(Player player);
    /**
     * 获取玩家
     * @return Player
     */
    Player getPlayer();
    /**
     * 尝试次数+1
     */
    void setTry();
    /**
     * 获取尝试次数
     * @return 尝试次数
     */
    int getTry();
    /**
     * 设置Protocol
     * @param protocol Protocol
     */
    void setProtocol(Protocol protocol);
    /**
     * 获取连接协议
     * @return 协议
     */
    String getProtocol();
    /**
     * 服务端可支持的版本
     * @return 版本号
     */
    String getVersion();



    /**
     *  # Core
     */
    /**
     * 获取系统命名的消息包
     * SERVER: ...
     * @param byteBuf The message
     * @return ByteBuf
     */
    void sendSystemMessage(String text);
    /**
     * 发送用户名命名的消息
     * @param      byteBuf     The message
     */
    void sendChatMessage(String msg, String sendBy, int team);
    /**
     * 发送服务器消息
     * @throws IOException Error
     */
    void sendServerInfo(boolean utilData) throws IOException;
    /**
     * 自杀
     */
    void sendSurrender();
    /**
     * 踢出玩家
     * @param reason 发送原因
     */
    void sendKick(String reason) throws IOException;
    /**
     * Ping
     */
    void ping();
    /**
     * 提取GameSave包
     * @param packet packet
     * @return 包
     */
    byte[] getGameSaveData(Packet packet) throws IOException;
    /**
     * 接受语言包
     * @param p Packet
     * @throws IOException Error
     */
    void receiveChat(Packet p) throws IOException;
    /**
     * 接受位移包
     * @param p Packet
     * @throws IOException Error
     */
    void receiveCommand(Packet p) throws IOException;
    /**
     * 发送游戏开始包
     * @throws IOException Error
     */
    void sendStartGame() throws IOException;
    /**
     * 发送队伍包
     * @param gzip GzipPacket
     */
    void sendTeamData(GzipEncoder gzip);

    /**
     * 获取玩家的信息并注册
     * @param p Packet包
     * @return 注册状态
     * @throws IOException err
     */
    boolean getPlayerInfo(Packet p) throws IOException;
    /**
     * 注册连接
     * @param p Packet包
     * @throws IOException err
     */
    void registerConnection(Packet p) throws IOException;
    /**
     * 断开连接
     */
    void disconnect();
    /**
     * 诱骗客户端发送Save包
     */
    void getGameSave();

    /**
     * 发送重连包
     * @param packet ByteBuf
     */
    void sendGameSave(ByteBuf packet);



    default void reConnect() {
        try {
            sendKick("不支持重连");
        } catch (IOException e) {
            Log.error("(",e);
        }
    }

    default void debug(Packet packet) {
    }

    default void senddebug(String str) {
    }
}
