package com.github.dr.rwserver.net;

import com.github.dr.rwserver.data.Player;
import com.github.dr.rwserver.game.GameCommand;
import com.github.dr.rwserver.io.Packet;
import com.github.dr.rwserver.struct.Seq;
import com.github.dr.rwserver.util.zip.gzip.GzipEncoder;
import io.netty.channel.Channel;

import java.io.IOException;
import java.net.SocketAddress;

/**
 * @author Dr
 * @Data 2020/9/5 13:31
 */
public interface AbstractNetConnect {

    /**
     * Import
     */

    /**
     * 获取特定版本协议
     * @param sockAds SocketAds
     * @return 协议
     */
    public AbstractNetConnect getVersionNet(SocketAddress sockAds);

    /**
     * 设置玩家
     * @param player Player
     */
    public void setPlayer(Player player);

    /**
     * 获取玩家
     * @return Player
     */
    public Player getPlayer();

    /**
     * 尝试次数+1
     */
    public void setTry();

    /**
     * 获取尝试次数
     * @return 尝试次数
     */
    public int getTry();

    /**
     * 设置Channel
     * @param c Channel
     */
    public void setChannel(Channel c);

    public String getVersion();

    /**
     * Core
     */

    public void sendSystemMessage(String msg);

    public void sendChatMessage(String msg, String sendBy, int team);

    public void sendServerInfo() throws IOException;

    public void upServerInfo();

    public void surrender();

    public void sendKick(String reason);

    public void ping();

    public void receiveChat(Packet p) throws IOException;

    public void receiveCommand(Packet p) throws IOException;

    public void sendGameTickCommand(int tick, GameCommand cmd);

    public void sendGameTickCommands(int tick, Seq<GameCommand> cmd);

    public void sendTick(int tick);

    public void sendTeamData(GzipEncoder gzip);

    public GzipEncoder getTeamData();

    /**
     * 开始游戏
     * @throws IOException err
     */
    public void startGame() throws IOException;

    /**
     * 获取玩家的信息并注册
     * @param p Packet包
     * @return 注册状态
     * @throws IOException err
     */
    public boolean getPlayerInfo(Packet p) throws IOException;

    /**
     * 注册连接
     * @param p Packet包
     * @throws IOException err
     */
    public void registerConnection(Packet p) throws IOException;

    /**
     * 断开连接
     */
    public void disconnect();

    default void reConnect() {
        sendKick("不支持重连");
    }
}
