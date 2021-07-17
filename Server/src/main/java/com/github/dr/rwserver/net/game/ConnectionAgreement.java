package com.github.dr.rwserver.net.game;

import com.github.dr.rwserver.io.Packet;
import com.github.dr.rwserver.net.GroupNet;
import com.github.dr.rwserver.net.udp.ReliableSocket;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import org.jetbrains.annotations.NotNull;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Objects;

import static com.github.dr.rwserver.util.RandomUtil.generateStr;

/**
 * @author Dr
 * @date 2021年2月2日星期二 06:31:11
 */
public class ConnectionAgreement {
    private final ProtocolType<Packet> protocolType;
    private final StartNet startNet;
    private final ChannelHandlerContext channelHandlerContext;
    private final Object object;
    private final DataOutputStream socketStream;
    public final String useAgreement;
    public final String ip;
    public final int localPort;
    public final String id;

    /**
     * TCP Send
     * @param channel Netty-Channel
     */
    public ConnectionAgreement(@NotNull ChannelHandlerContext channelHandlerContext,@NotNull Channel channel,@NotNull StartNet startNet) {
        this.protocolType = channel::writeAndFlush;
        this.startNet = startNet;
        this.channelHandlerContext = channelHandlerContext;
        this.object = channel;
        this.useAgreement = "TCP";
        this.ip = convertIp(channel.remoteAddress().toString());
        this.localPort = ((InetSocketAddress) channel.localAddress()).getPort();
        this.socketStream = null;
        this.id = null;
    }

    /**
     * UDP Send
     * @param socket Socket
     * @throws IOException Error
     */
    public ConnectionAgreement(@NotNull Socket socket,@NotNull StartNet startNet) throws IOException {
        this.socketStream = new DataOutputStream(socket.getOutputStream());
        protocolType = (msg) -> {
            socketStream.writeInt(msg.bytes.length);
            socketStream.writeInt(msg.type);
            socketStream.write(msg.bytes);
            socketStream.flush();
        };
        this.startNet = startNet;
        this.channelHandlerContext = null;
        this.object = socket;
        this.useAgreement = "UDP";
        this.ip = convertIp(socket.getRemoteSocketAddress().toString());
        this.localPort = socket.getLocalPort();
        this.id = generateStr(5);
    }

    public void add(@NotNull final GroupNet groupNet) {
        if (object instanceof Channel) {
            groupNet.add((Channel) object);
        } else if (object instanceof ReliableSocket) {
            groupNet.add(this);
        }
    }

    public void send(@NotNull Packet packet) throws IOException {
        protocolType.send(packet);
    }

    public void close(final GroupNet groupNet) throws IOException {
        if (groupNet != null) {
            if (object instanceof Channel) {
                groupNet.remove((Channel) object);
            } else if (object instanceof ReliableSocket) {
                groupNet.remove(this);
            }
        }
        if (object instanceof Channel) {
            ((Channel) object).close();
            channelHandlerContext.close();
            startNet.OVER_MAP.remove(((Channel) object).id().asLongText());
        } else if (object instanceof ReliableSocket) {
            socketStream.close();
            ((ReliableSocket) object).close();
            startNet.OVER_MAP.remove(((ReliableSocket) object).getRemoteSocketAddress().toString());
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        return this.id.equals(((ConnectionAgreement) o).id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    private String convertIp(@NotNull final String ipString) {
        return ipString.substring(1, ipString.indexOf(':'));
    }

    private interface ProtocolType<T>{
        /**
         * 接管Send逻辑
         * @param t Msg
         * @throws IOException Error
         */
        void send(T t) throws IOException;
    }
}
