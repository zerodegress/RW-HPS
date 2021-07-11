package com.github.dr.rwserver.net.netconnectprotocol;


import com.github.dr.rwserver.Main;
import com.github.dr.rwserver.core.Call;
import com.github.dr.rwserver.core.thread.Threads;
import com.github.dr.rwserver.data.Player;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.data.global.NetStaticData;
import com.github.dr.rwserver.game.EventType;
import com.github.dr.rwserver.game.GameCommand;
import com.github.dr.rwserver.io.GameInputStream;
import com.github.dr.rwserver.io.GameOutputStream;
import com.github.dr.rwserver.io.Packet;
import com.github.dr.rwserver.net.core.AbstractNetConnect;
import com.github.dr.rwserver.util.IsUtil;
import com.github.dr.rwserver.util.LocaleUtil;
import com.github.dr.rwserver.util.PacketType;
import com.github.dr.rwserver.util.encryption.Game;
import com.github.dr.rwserver.util.game.CommandHandler;
import com.github.dr.rwserver.util.game.Events;
import com.github.dr.rwserver.util.log.Log;
import com.github.dr.rwserver.util.zip.gzip.GzipEncoder;
import com.ip2location.IPResult;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import static com.github.dr.rwserver.util.ExtractUtil.hexToByteArray;
import static com.github.dr.rwserver.util.IsUtil.isBlank;
import static com.github.dr.rwserver.util.IsUtil.notIsBlank;
import static com.github.dr.rwserver.util.RandomUtil.generateInt;
import static com.github.dr.rwserver.util.encryption.Game.connectKey;

/**
 * @author Dr
 * @date 2020/9/5 17:02:33
 */
public class GameVersionServer extends AbstractGameVersion {

    private String playerConnectKey;

    private final ReentrantLock sync = new ReentrantLock(true);

    public GameVersionServer(final String uuid) {
    }

    @Override
    public AbstractNetConnect getVersionNet(final String uuid) {
        return new GameVersionServer(uuid);
    }

    @Override
    public Player getPlayer() {
        return player;
    }

    @Override
    public String getVersion() {
        return "1.14 RW-HPS";
    }

    @Override
    public void sendSystemMessage(String msg) {
        if (player.noSay) {
            return;
        }
        super.sendSystemMessage(msg);
    }

    @Override
    public void sendServerInfo(boolean utilData) throws IOException {
        GameOutputStream o = new GameOutputStream();
        o.writeString(Data.SERVER_ID);
        o.writeInt(NetStaticData.protocolData.getGameNetVersion());
        /* 地图 */
        o.writeInt(Data.game.maps.mapType.ordinal());
        o.writeString(Data.game.maps.mapPlayer + Data.game.maps.mapName);
        o.writeInt(Data.game.credits);
        o.writeInt(Data.game.mist);
        o.writeBoolean(true);
        o.writeInt(1);
        o.writeByte(7);

        o.writeBoolean(false);
        /* Admin Ui */
        o.writeBoolean(player.isAdmin);

        o.writeInt(Data.game.maxUnit);
        o.writeInt(Data.game.maxUnit);

        o.writeInt(Data.game.initUnit);
        o.writeFloat(Data.game.income);
        /* NO Nukes */
        o.writeBoolean(Data.game.noNukes);

        o.writeBoolean(false);

        o.writeBoolean(utilData);
        if (utilData) {
            o.flushEncodeData(Data.utilData);
        }

        /* 共享控制 */
        o.writeBoolean(Data.game.sharedControl);
        o.writeBoolean(false);
        o.writeBoolean(false);
        // 允许观众
        o.writeBoolean(true);
        o.writeBoolean(false);

        sendPacket(o.createPacket(PacketType.PACKET_SERVER_INFO));
    }

    @Override
    public void sendSurrender() {
        try (ByteArrayOutputStream buffer = new ByteArrayOutputStream();DataOutputStream stream = new DataOutputStream(buffer)) {
            stream.writeByte(player.site);
            byte[] a = hexToByteArray("000000ffffffffffffffff0000000000000000ffffffffffffffff00022d3100000001000000000000000000000000640000000000");
            for (byte b : a) {
                stream.writeByte(b);
            }
            GameCommand cmd = new GameCommand(player.site, buffer.toByteArray());
            Data.game.gameCommandCache.offer(cmd);
            Call.sendSystemMessage(Data.localeUtil.getinput("player.surrender",player.name));
        } catch (Exception ignored) {}
    }

    @Override
    public void sendKick(String reason) throws IOException {
        GameOutputStream o = new GameOutputStream();
        o.writeString(reason);
        sendPacket(o.createPacket(PacketType.PACKET_KICK));
        disconnect();
    }

    @Override
    public void receiveChat(Packet p) throws IOException {
        try (GameInputStream stream = new GameInputStream(p)) {
            String message = stream.readString();
            CommandHandler.CommandResponse response = null;

            Log.clog("[{0}]: {1}", player.name, message);

            if (player.isAdmin && Threads.getIfScheduledFutureData("AfkCountdown")) {
                Threads.removeScheduledFutureData("AfkCountdown");
                Call.sendMessage(player, Data.localeUtil.getinput("afk.clear", player.name));
            }

            if (message.startsWith(".") || message.startsWith("-") || message.startsWith("_")) {
                int strEnd = Math.min(message.length(), 3);
                if ("qc".equals(message.substring(1, strEnd))) {
                    response = Data.CLIENTCOMMAND.handleMessage("/" + message.substring(5), player);
                } else {
                    response = Data.CLIENTCOMMAND.handleMessage("/" + message.substring(1), player);
                }
            }
            if (response == null || response.type == CommandHandler.ResponseType.noCommand) {
                if (message.length() > Data.game.maxMessageLen) {
                    sendSystemMessage(Data.localeUtil.getinput("message.maxLen"));
                    return;
                }
                message = Data.core.admin.filterMessage(player, message);
                if (message == null) {
                    return;
                }
                Call.sendMessage(player, message);
                Events.fire(new EventType.PlayerChatEvent(player, message));
            } else {
                if (response.type != CommandHandler.ResponseType.valid) {
                    String text;

                    if (response.type == CommandHandler.ResponseType.manyArguments) {
                        text = "Too many arguments. Usage: " + response.command.text + " " + response.command.paramText;
                    } else if (response.type == CommandHandler.ResponseType.fewArguments) {
                        text = "Too few arguments. Usage: " + response.command.text + " " + response.command.paramText;
                    } else {
                        text = "Unknown command. Check .help";
                    }
                    player.sendSystemMessage(text);
                }
            }
        }
    }

    @Override
    public void receiveCommand(Packet p) throws IOException {
        sync.lock();
        try (GameInputStream in = new GameInputStream(new GameInputStream(p).getDecodeBytes())) {
            byte[] bytes;
            //if (Data.game.sharedControl) {
            GameOutputStream o = new GameOutputStream();
            o.writeByte(in.readByte());
            final boolean boolean1 = in.readBoolean();
            o.writeBoolean(boolean1);
            if (boolean1) {
                o.writeInt(in.readInt());
                final int int1 = in.readInt();
                o.writeInt(int1);
                if (int1 == -2) {
                    o.writeString(in.readString());
                }
                o.writeBytes(in.stream.readNBytes(28));
                /*
                o.writeFloat(in.readFloat());
                o.writeFloat(in.readFloat());
                o.writeLong(in.readLong());
                o.writeByte(in.readByte());
                o.writeFloat(in.readFloat());
                o.writeFloat(in.readFloat());
                o.writeBoolean(in.readBoolean());
                o.writeBoolean(in.readBoolean());
                o.writeBoolean(in.readBoolean());
                */
                final boolean boolean2 = in.readBoolean();
                o.writeBoolean(boolean2);
                if (boolean2) {
                    o.writeString(in.readString());
                }
            }
            o.writeBytes(in.stream.readNBytes(10));
            /*
            o.writeBoolean(in.readBoolean());
            o.writeBoolean(in.readBoolean());
            o.writeInt(in.readInt());
            o.writeInt(in.readInt());
             */
            final boolean boolean3 = in.readBoolean();
            o.writeBoolean(boolean3);
            if (boolean3) {
                o.writeBytes(in.stream.readNBytes(8));
                /*
                o.writeFloat(in.readFloat());
                o.writeFloat(in.readFloat());
                 */
            }
            o.writeBoolean(in.readBoolean());
            final int int2 = in.readInt();
            o.writeInt(int2);
            for (int i = 0; i < int2; i++) {
                o.writeBytes(in.stream.readNBytes(8));
                //o.writeLong(in.readLong());
            }
            final boolean boolean4 = in.readBoolean();
            o.writeBoolean(boolean4);
            if (boolean4) {
                o.writeByte(in.readByte());
            }
            final boolean boolean5 = in.readBoolean();
            o.writeBoolean(boolean5);
            if (boolean5) {
                o.writeBytes(in.stream.readNBytes(8));
                /*
                o.writeFloat(in.readFloat());
                o.writeFloat(in.readFloat());
                 */
            }
            o.writeBytes(in.stream.readNBytes(8));
            //o.writeLong(in.readLong());
            o.writeString(in.readString());
            o.writeBoolean(in.readBoolean());
            in.readShort();
            o.writeShort((short) Data.game.sharedControlPlayer);
            o.flushData(in);
            Packet packet = o.createPacket();
        //} else {
        //    bytes = inData.getDecodeBytes();
        //}

        GameCommand cmd = new GameCommand(player.site, packet.bytes);
        Data.game.gameCommandCache.offer(cmd);
        } catch (Exception e) {
            Log.error(e);
        } finally {
            sync.unlock();
        }
    }

    @Override
    public byte[] getGameSaveData(Packet packet) throws IOException {
        try (GameInputStream stream = new GameInputStream(packet)) {
            GameOutputStream o = new GameOutputStream();
            o.writeByte(stream.readByte());
            o.writeInt(stream.readInt());
            o.writeInt(stream.readInt());
            o.writeFloat(stream.readFloat());
            o.writeFloat(stream.readFloat());
            o.writeBoolean(false);
            o.writeBoolean(false);
            stream.readBoolean();
            stream.readBoolean();
            stream.readString();
            return stream.readStreamBytes();
        }
    }

    @Override
    public void sendStartGame() throws IOException {
        sendServerInfo(true);
        sendPacket(NetStaticData.protocolData.abstractNetPacket.getStartGamePacket());
        if (notIsBlank(Data.game.startAd)) {
            sendSystemMessage(Data.game.startAd);
        }
    }

    @Override
    public void sendTeamData(GzipEncoder gzip) {
        try {
            GameOutputStream o = new GameOutputStream();
            /* 玩家位置 */
            o.writeInt(player.site);
            o.writeBoolean(Data.game.isStartGame);

            /* 最大玩家 */
            o.writeInt(Data.game.maxPlayer);
            o.flushEncodeData(gzip);
            /* 迷雾 */
            o.writeInt(Data.game.mist);
            o.writeInt(Data.game.credits);
            o.writeBoolean(true);
            /* AI Difficulty ?*/
            o.writeInt(1);

            o.writeByte(5);

            o.writeInt(Data.game.maxUnit);
            o.writeInt(Data.game.maxUnit);
            /* 初始单位 */
            o.writeInt(Data.game.initUnit);
            /* 倍速 */
            o.writeFloat(Data.game.income);
            /* NO Nukes */
            o.writeBoolean(Data.game.noNukes);
            o.writeBoolean(false);
            o.writeBoolean(false);
            /* 共享控制 */
            o.writeBoolean(Data.game.sharedControl);
            /* 游戏暂停 */
            o.writeBoolean(false);

            sendPacket(o.createPacket(PacketType.PACKET_TEAM_LIST));
        } catch (IOException e) {
            Log.error("Team",e);
        }
    }

    @Override
    public boolean getPlayerInfo(Packet p) throws IOException {
        try (GameInputStream stream = new GameInputStream(p)) {
            stream.readString();
            stream.readInt();
            stream.readInt();
            stream.readInt();
            String name = stream.readString();
            Log.debug("name", name);
            String passwd = stream.isReadString();
            Log.debug("passwd", passwd);
            stream.readString();
            String uuid = stream.readString();
            Log.debug("uuid", uuid);
            Log.debug("?", stream.readInt());
            String token = stream.readString();
            Log.debug("token", token);
            /*
            if (!token.equals(playerConnectKey)) {
                sendKick("You Open Mod?");
                return false;
            }
             */
            final EventType.PlayerConnectPasswdCheckEvent playerConnectPasswdCheck = new EventType.PlayerConnectPasswdCheckEvent(this, passwd);
            Events.fire(playerConnectPasswdCheck);
            if (playerConnectPasswdCheck.result) {
                return true;
            }
            if (notIsBlank(playerConnectPasswdCheck.name)) {
                name = playerConnectPasswdCheck.name;
            }
            isPasswd = false;

            AtomicBoolean re = new AtomicBoolean(false);
            if (Data.game.isStartGame) {
                Data.playerAll.each(i -> i.uuid.equals(uuid), e -> {
                    re.set(true);
                    this.player = e;
                    player.con = this;
                    Data.playerGroup.add(e);
                });
                if (!re.get()) {
                    if (isBlank(Data.game.startPlayerAd)) {
                        sendKick("游戏已经开局 请等待 # The game has started, please wait");
                    } else {
                        sendKick(Data.game.startPlayerAd);
                    }
                    return false;
                }
            } else {
                if (Data.playerGroup.size() >= Data.game.maxPlayer) {
                    if (isBlank(Data.game.maxPlayerAd)) {
                        sendKick("服务器没有位置 # The server has no free location");
                    } else {
                        sendKick(Data.game.maxPlayerAd);
                    }
                    return false;
                }
                LocaleUtil localeUtil = Data.localeUtilMap.get("CN");
                if (Data.game.ipCheckMultiLanguageSupport) {
                    IPResult rec = Data.ip2Location.IPQuery(connectionAgreement.ip);
                    if (!"OK".equals(rec.getStatus())) {
                        localeUtil = Data.localeUtilMap.get(rec.getCountryShort());
                    }
                }
                player = Player.addPlayer(this, uuid, name, localeUtil);
            }

            connectionAgreement.add(NetStaticData.groupNet);
            Call.sendTeamData();
            sendServerInfo(true);
            Events.fire(new EventType.PlayerJoinEvent(player));

            if (notIsBlank(Data.game.enterAd)) {
                sendSystemMessage(Data.game.enterAd);
            }
            Call.sendSystemMessage(Data.localeUtil.getinput("player.ent", player.name));
            if (re.get()) {
                reConnect();
            }
            return true;
        } finally {
            playerConnectKey = null;
        }
    }

    @Override
    public void registerConnection(Packet p) throws IOException {
        // 生成随机Key;
        int keyLen = 6;
        int key = generateInt(keyLen);
        playerConnectKey = connectKey(key);
        try (GameInputStream stream = new GameInputStream(p)) {
            // Game Pkg Name
            stream.readString();
            // 返回都是1 有啥用
            stream.readInt();
            stream.readInt();
            stream.readInt();
            GameOutputStream o = new GameOutputStream();
            o.writeString(Data.SERVER_ID);
            o.writeInt(1);
            o.writeInt(NetStaticData.protocolData.getGameNetVersion());
            o.writeInt(NetStaticData.protocolData.getGameNetVersion());
            o.writeString("com.corrodinggames.rts.server");
            o.writeString(Data.core.serverConnectUuid);
            o.writeInt(key);
            sendPacket(o.createPacket(PacketType.PACKET_REGISTER_CONNECTION));
        }
    }

    /**
     *
     */
    @Override
    public void sendErrorPasswd() {
        try {
            GameOutputStream o = new GameOutputStream();
            o.writeInt(0);
            sendPacket(o.createPacket(PacketType.PACKET_PASSWD_ERROR));
            isPasswd = true;
        } catch (Exception e) {
            Log.error("[Player] sendErrorPasswd",e);
        }
    }

    @Override
    public void disconnect() {
        if (super.isDis) {
            return;
        }
        super.isDis = true;
        if (IsUtil.notIsBlank(player)) {
            Data.playerGroup.remove(player);
            if (!Data.game.isStartGame) {
                Data.playerAll.remove(player);
                player.clear();
                Data.game.playerData[player.site] = null;
            }
            Events.fire(new EventType.PlayerLeaveEvent(player));
        }

        try {
            connectionAgreement.close(NetStaticData.groupNet);
        } catch (Exception e) {
            Log.error("Close Connect",e);
        }
    }

    @Override
    public void getGameSave() {
        try {
            GameOutputStream o = new GameOutputStream();
            o.writeByte(0);
            o.writeInt(0);
            o.writeInt(0);
            o.writeFloat(0);
            o.writeFloat(0);
            o.writeBoolean(true);
            o.writeBoolean(false);
            GzipEncoder gzipEncoder = GzipEncoder.getGzipStream("gameSave",false);
            DataOutputStream io = gzipEncoder.stream;
            io.writeUTF("This is RW-HPS!");
            o.flushEncodeData(gzipEncoder);
            sendPacket(o.createPacket(35));
        } catch (Exception e) {
            Log.error(e);
        }


    }

    @Override
    public void sendGameSave(Packet packet) {
        sendPacket(packet);
    }

    @Override
    public void reConnect() {
        try {
            if (!Data.game.reConnect) {
                sendKick("不支持重连 # Does not support reconnection");
                return;
            }
            super.isDis = false;
            sendPacket(NetStaticData.protocolData.abstractNetPacket.getStartGamePacket());
            Data.game.reConnectBreak = true;
            Call.sendSystemMessage("玩家短线重连中 请耐心等待 不要退出 期间会短暂卡住！！ 需要30s-60s");
            final ExecutorService executorService= Executors.newFixedThreadPool(1);
            Future<String> future = executorService.submit(() -> {
                Data.playerGroup.each(e -> (!e.uuid.equals(this.player.uuid)) && (!e.con.getTryBoolean()),p -> {
                    p.con.getGameSave();
                    while (Data.game.gameSaveCache == null || Data.game.gameSaveCache.type == 0) {
                        if (Thread.interrupted()) {
                            return;
                        }
                    }
                    try {
                        NetStaticData.groupNet.broadcast(NetStaticData.protocolData.abstractNetPacket.convertGameSaveDataPacket(Data.game.gameSaveCache));
                    } catch (IOException e) {
                        Log.error(e);
                    }
                });
                return null;
            });
            try {
                future.get(30, TimeUnit.SECONDS);
            } catch (Exception e) {
                future.cancel(true);
                Log.error(e);
                connectionAgreement.close(NetStaticData.groupNet);
            } finally {
                executorService.shutdown();
                Data.game.gameSaveCache = null;
                Data.game.reConnectBreak = false;
            }
        } catch (Exception e) {
            Log.error("[Player] Send GameSave ReConnect Error",e);
        }
    }
}