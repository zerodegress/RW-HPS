package com.github.dr.rwserver.net.netconnectprotocol;

import com.github.dr.rwserver.data.Player;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.data.global.NetStaticData;
import com.github.dr.rwserver.io.GameInputStream;
import com.github.dr.rwserver.io.GameOutputStream;
import com.github.dr.rwserver.io.Packet;
import com.github.dr.rwserver.net.GroupNet;
import com.github.dr.rwserver.net.core.AbstractNetConnect;
import com.github.dr.rwserver.net.game.ConnectionAgreement;
import com.github.dr.rwserver.util.Time;
import com.github.dr.rwserver.util.log.Log;
import com.github.dr.rwserver.util.zip.gzip.GzipEncoder;
import okhttp3.internal.cache2.Relay;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;

/**
 * 作为{@link AbstractNetConnect} 和 协议实现的中间人
 * 目的是为了多协议支持
 * 共用协议放在本处
 * @author Dr
 */
public abstract class AbstractGameVersion implements AbstractNetConnect {
    /** 错误次数 */
    protected int errorTry = 0;
    /** 是否停留在输入界面 */
    protected boolean isPasswd = false;
    /** 最后一次接受到包的时间 */
    protected long lastReceivedTime = Time.concurrentMillis();
    /** 玩家是否死亡 */
    protected volatile boolean isDis = false;
    /** 是否已经重试过 */
    protected volatile boolean isTry = false;
    /** 玩家 */
    protected Player player = null;

    protected ConnectionAgreement connectionAgreement;

    @Override
    public String getIp() {
        return connectionAgreement.ip;
    }

    @Override
    public int getPort() {
        return connectionAgreement.localPort;
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public void setCache(Packet packet) {
    }

    @Override
    public void setTry() {
        errorTry++;
    }

    @Override
    public int getTry() {
        return errorTry;
    }

    @Override
    public void setTryBoolean(boolean tryBoolean) {
        this.isTry = tryBoolean;
    }

    @Override
    public boolean getTryBoolean() {
        return isTry;
    }

    @Override
    public boolean getIsPasswd() {
        return isPasswd;
    }

    @Override
    public void setLastReceivedTime() {
        this.isTry = false;
        this.lastReceivedTime = Time.concurrentMillis();
    }

    @Override
    public long getLastReceivedTime() {
        return lastReceivedTime;
    }

    @Override
    public void setConnectionAgreement(ConnectionAgreement connectionAgreement) {
        this.connectionAgreement = connectionAgreement;
    }

    @Override
    public String getConnectionAgreement() {
        return connectionAgreement.useAgreement;
    }

    @Override
    public String getVersion() {
        return "1.14";
    }

    @Override
    public void sendSystemMessage(@NotNull String msg) {
        try {
            sendPacket(NetStaticData.protocolData.abstractNetPacket.getSystemMessagePacket(msg));
        } catch (IOException e) {
            Log.error("[Player] Send System Chat Error",e);
        }
    }

    @Override
    public void sendChatMessage(@NotNull String msg, String sendBy, int team) {
        try {
            sendPacket(NetStaticData.protocolData.abstractNetPacket.getChatMessagePacket(msg,sendBy,team));
        } catch (IOException e) {
            Log.error("[Player] Send Player Chat Error",e);
        }
    }

    @Override
    public void sendServerInfo(boolean utilData) throws IOException {
    }

    @Override
    public void sendSurrender() {
    }

    @Override
    public void sendKick(@NotNull String reason) throws IOException {
    }

    @Override
    public void ping() {
        try {
            sendPacket(NetStaticData.protocolData.abstractNetPacket.getPingPacket(player));
        } catch (IOException e) {
            errorTry++;
        }
    }

    @Override
    public byte[] getGameSaveData(@NotNull Packet packet) throws IOException {
        return null;
    }

    @Override
    public void receiveChat(@NotNull Packet p) throws IOException {
    }

    @Override
    public void receiveCommand(@NotNull Packet p) throws IOException {
    }

    @Override
    public void sendStartGame() throws IOException {
    }

    @Override
    public void sendTeamData(@NotNull GzipEncoder gzip) {
    }

    @Override
    public boolean getPlayerInfo(@NotNull Packet p) throws IOException {
        return false;
    }

    @Override
    public void registerConnection(@NotNull Packet p) throws IOException {
    }

    @Override
    public void sendErrorPasswd() throws IOException {
    }

    protected void close(final GroupNet groupNet) {
        try {
            connectionAgreement.close(groupNet);
        } catch (Exception e) {
            Log.error("Close Connect",e);
        }
    }

    @Override
    public void getGameSave() {
    }

    @Override
    public void sendGameSave(@NotNull Packet packet) {
        sendPacket(packet);
    }

    @Override
    public void sendPacket(@NotNull Packet packet) {
        try {
            connectionAgreement.send(packet);
        } catch (Exception e) {
            Log.error("[UDP] SendError - 本消息单独出现无妨 连续多次出现请debug",e);
            disconnect();
        }
    }
}
