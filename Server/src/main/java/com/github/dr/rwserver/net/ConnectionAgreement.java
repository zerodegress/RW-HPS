package com.github.dr.rwserver.net;

import com.github.dr.rwserver.net.udp.ReliableSocket;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Objects;

import static com.github.dr.rwserver.util.RandomUtil.generateStr;

/**
 * @author Dr
 * @date 2021年2月2日星期二 06:31:11
 */
public class ConnectionAgreement {
    private final ProtocolType<ByteBuf> protocolType;
    private final Object object;
    private final OutputStream socketStream;
    public final String useAgreement;
    public final String ip;
    public final String id;

    /**
     * TCP Send
     * @param channel Netty-Channel
     */
    public ConnectionAgreement(Channel channel) {
        protocolType = channel::writeAndFlush;
        object = channel;
        useAgreement = "TCP";
        ip = convertIp(channel.remoteAddress().toString());
        socketStream = null;
        id = null;
    }

    /**
     * UDP Send
     * @param socket Socket
     * @throws IOException Error
     */
    public ConnectionAgreement(Socket socket) throws IOException {
        socketStream = socket.getOutputStream();
        protocolType = (msg) -> {
            socketStream.write(msg.array());
            socketStream.flush();
        };
        object = socket;
        useAgreement = "UDP";
        ip = convertIp(socket.getRemoteSocketAddress().toString());
        id = generateStr(5);
    }

    public void add(final GroupNet groupNet) {
        if (object instanceof Channel) {
            groupNet.add((Channel) object);
        } else if (object instanceof ReliableSocket) {
            groupNet.add(this);
        }
    }

    public void send(ByteBuf byteBuf) throws IOException {
        protocolType.send(byteBuf);
    }

    public void close(final GroupNet groupNet) throws IOException {
        if (groupNet != null) {
            if (object instanceof Channel) {
                groupNet.remove((Channel) object);
                ((Channel) object).close();
            } else if (object instanceof ReliableSocket) {
                groupNet.remove(this);
                ((ReliableSocket) object).close();
            }
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

    private String convertIp(final String ipString) {
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
