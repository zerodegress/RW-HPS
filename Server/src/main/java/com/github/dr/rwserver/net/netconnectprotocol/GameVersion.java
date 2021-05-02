package com.github.dr.rwserver.net.netconnectprotocol;

import com.github.dr.rwserver.data.Player;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.io.Packet;
import com.github.dr.rwserver.net.AbstractNetConnect;
import com.github.dr.rwserver.net.GroupNet;
import com.github.dr.rwserver.net.Protocol;
import com.github.dr.rwserver.util.log.Log;
import com.github.dr.rwserver.util.zip.gzip.GzipEncoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;

import java.io.IOException;

public abstract class GameVersion implements AbstractNetConnect {
    protected int errorTry = 0;
    protected boolean isPasswd = false;
    protected volatile boolean isTry = false;
    protected Player player = null;

    protected Protocol protocol;
    protected ByteBufAllocator bufAllocator;

    @Override
    public Player getPlayer() {
        return player;
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
    public void setTryBolean(boolean tryBolean) {
        this.isTry = tryBolean;
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
    public void setProtocol(Protocol protocol) {
        this.protocol = protocol;
    }

    @Override
    public String getProtocol() {
        return protocol.useAgreement;
    }

    @Override
    public String getVersion() {
        return "1.14";
    }

    @Override
    public void sendSystemMessage(String msg) {
        try {
            sendPacket(Data.game.connectPacket.getSystemMessageByteBuf(msg));
        } catch (IOException e) {
            Log.error("[Player] Send System Chat Error",e);
        }
    }

    @Override
    public void sendChatMessage(String msg, String sendBy, int team) {
        try {
            sendPacket(Data.game.connectPacket.getChatMessageByteBuf(msg,sendBy,team));
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
    public void sendKick(String reason) throws IOException {

    }

    @Override
    public void ping() {
        try {
            sendPacket(Data.game.connectPacket.getPingByteBuf(player));
        } catch (IOException e) {
            errorTry++;
        }
    }

    @Override
    public byte[] getGameSaveData(Packet packet) throws IOException {
        return null;
    }

    @Override
    public void receiveChat(Packet p) throws IOException {

    }

    @Override
    public void receiveCommand(Packet p) throws IOException {

    }

    @Override
    public void sendStartGame() throws IOException {

    }

    @Override
    public void sendTeamData(GzipEncoder gzip) {

    }

    @Override
    public boolean getPlayerInfo(Packet p) throws IOException {
        return false;
    }

    @Override
    public void registerConnection(Packet p) throws IOException {

    }

    @Override
    public void sendErrorPasswd() throws IOException {

    }

    protected void close(final GroupNet groupNet) {
        try {
            protocol.close(groupNet);
        } catch (Exception e) {
            Log.error("Close Connect",e);
        }
    }

    @Override
    public void getGameSave() {

    }

    @Override
    public void sendGameSave(ByteBuf packet) {
        sendPacket(packet);
    }

    /**
     * 发送包
     * @param bb 数据
     */
    protected void sendPacket(ByteBuf bb) {
        try {
            protocol.send(bb);
        } catch (Exception e) {
            Log.error("[UDP] SendError - 本消息单独出现无妨 连续多次出现请debug",e);
            disconnect();
        }
    }
}
