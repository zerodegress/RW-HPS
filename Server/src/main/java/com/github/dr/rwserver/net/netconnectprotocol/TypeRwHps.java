package com.github.dr.rwserver.net.netconnectprotocol;

import com.github.dr.rwserver.data.Player;
import com.github.dr.rwserver.data.global.Data;
import com.github.dr.rwserver.io.Packet;
import com.github.dr.rwserver.net.core.AbstractNetConnect;
import com.github.dr.rwserver.net.core.TypeConnect;
import com.github.dr.rwserver.util.PacketType;
import com.github.dr.rwserver.util.Time;
import org.jetbrains.annotations.NotNull;

public class TypeRwHps implements TypeConnect {
    @Override
    public void typeConnect(@NotNull AbstractNetConnect con, @NotNull Packet packet) throws Exception {
        con.setLastReceivedTime();
        if (packet.type == PacketType.PACKET_ADD_GAMECOMMAND) {
            con.receiveCommand(packet);
            con.getPlayer().lastMoveTime = Time.millis();
        } else {
            switch (packet.type) {
                // 连接服务器
                case PacketType.PACKET_PREREGISTER_CONNECTION:
                    con.registerConnection(packet);
                    break;
                // 注册用户
                case PacketType.PACKET_PLAYER_INFO:
                    if (!con.getPlayerInfo(packet)) {
                        con.disconnect();
                    }
                    break;
                case PacketType.PACKET_HEART_BEAT_RESPONSE:
                    Player player = con.getPlayer();
                    player.ping = (int) (System.currentTimeMillis() - player.timeTemp) >> 1;
                    //心跳 懒得处理
                    break;
                // 玩家发送消息
                case PacketType.PACKET_ADD_CHAT:
                    con.receiveChat(packet);
                    break;
                // 玩家主动断开连接
                case PacketType.PACKET_DISCONNECT:
                    con.disconnect();
                    break;
                case PacketType.PACKET_ACCEPT_START_GAME:
                    con.getPlayer().start = true;
                    break;
                case PacketType.PACKET_SERVER_DEBUG:
                    con.debug(packet);
                    break;
                case PacketType.PACKET_SYNC:
                    Data.game.gameSaveCache = packet;
                    break;
                default:
                    break;
            }
        }
    }

    @NotNull
    @Override
    public String getVersion() {
        return "2.0.0";
    }
}
