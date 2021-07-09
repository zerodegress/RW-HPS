package com.github.dr.rwserver.net.core;

import com.github.dr.rwserver.data.Player;
import com.github.dr.rwserver.io.Packet;
import com.github.dr.rwserver.net.ConnectionAgreement;
import com.github.dr.rwserver.util.log.Log;
import com.github.dr.rwserver.util.zip.gzip.GzipEncoder;
import okhttp3.internal.cache2.Relay;

import java.io.IOException;

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
     * @param uuid uuid
     * @return 协议
     */
    AbstractNetConnect getVersionNet(final String uuid);

    /**
     * 获取玩家
     * @return Player
     */
    Player getPlayer();

    /**
     * 设置一个缓存数据包
     * @param packet v包
     */
    void setCache(Packet packet);

    /**
     * 获取连接IP
     * @return IP
     */
    String getIp();

    /**
     * 获取使用的本地端口
     * @return Port
     */
    int getPort();

    /**
     * 获取玩家名字
     * @return 玩家名字
     */
    String getName();

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
     * 设置尝试
     * @param tryBoolean 状态
     */
    void setTryBoolean(boolean tryBoolean);

    /**
     * 获取尝试状态
     * @return Boolean
     */
    boolean getTryBoolean();

    /**
     * 获取是否在输入密码
     * @return 值
     */
    boolean getIsPasswd();

    /**
     * 设置最后的接受数据时间
     */
    void setLastReceivedTime();

    /**
     * 获取最后的发言时间
     * @return Time
     */
    long getLastReceivedTime();

    /**
     * 设置ConnectionAgreement
     * @param connectionAgreement ConnectionAgreement
     */
    void setConnectionAgreement(ConnectionAgreement connectionAgreement);

    /**
     * 获取连接协议
     * @return 协议
     */
    String getConnectionAgreement();

    /**
     * 服务端可支持的版本
     * @return 版本号
     */
    String getVersion();


    /**
     * 获取系统命名的消息包
     * SERVER: ...
     * @param msg The message
     */
    void sendSystemMessage(String msg);
    /**
     * 发送用户名命名的消息
     * @param msg String
     * @param sendBy String
     * @param team Int
     */
    void sendChatMessage(String msg, String sendBy, int team);
    /**
     * 发送服务器消息
     * @param utilData 是否发送UnitData
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
     * @throws IOException Error
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
     * @throws IOException Error
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
    boolean getPlayerInfo(final Packet p) throws IOException;
    /**
     * 注册连接
     * @param p Packet包
     * @throws IOException err
     */
    void registerConnection(Packet p) throws IOException;

    /**
     * 密码错误
     * @throws IOException err
     */
    void sendErrorPasswd() throws IOException;
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
    void sendGameSave(Packet packet);

    default void reConnect() {
        try {
            sendKick("不支持重连");
        } catch (IOException e) {
            Log.error("(",e);
        }
    }

    /**
     * 发送包
     * @param packet 数据
     */
    void sendPacket(Packet packet);

    /**
     * Debug 特殊开发 暂不开放
     * @param packet Packet
     */
    default void debug(Packet packet) {
    }

    /**
     * Debug 特殊开发 暂不开放
     * @param str String
     */
    default void sendDebug(String str) {
    }
}

