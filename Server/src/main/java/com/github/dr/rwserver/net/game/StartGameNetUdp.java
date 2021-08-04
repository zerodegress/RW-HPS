package com.github.dr.rwserver.net.game;

import com.github.dr.rwserver.data.global.NetStaticData;
import com.github.dr.rwserver.io.Packet;
import com.github.dr.rwserver.net.core.AbstractNetConnect;
import com.github.dr.rwserver.net.core.TypeConnect;
import com.github.dr.rwserver.net.udp.ReliableServerSocket;
import com.github.dr.rwserver.util.log.Log;
import com.github.dr.rwserver.util.threads.ThreadFactoryName;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.SocketAddress;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

class StartGameNetUdp {
    private final ThreadPoolExecutor group = new ThreadPoolExecutor(4, Integer.MAX_VALUE, 1L, TimeUnit.MINUTES, new LinkedBlockingQueue<>(), ThreadFactoryName.nameThreadFactory("UDP-"));
    private final TimeoutDetection timeoutDetection;
    private final StartNet startNet;
    private AbstractNetConnect abstractNetConnect;
    private TypeConnect typeConnect;

    private NewServerHandler newServerHandler = null;

    protected StartGameNetUdp(StartNet startNet, AbstractNetConnect abstractNetConnect, TypeConnect typeConnect) {
        this.startNet = startNet;
        this.abstractNetConnect = abstractNetConnect;
        this.typeConnect = typeConnect;
        this.timeoutDetection = new TimeoutDetection(5,startNet);
    }

    protected void update() {
        this.abstractNetConnect = NetStaticData.protocolData.abstractNetConnect;
        this.typeConnect = NetStaticData.protocolData.typeConnect;
    }

    protected void close() throws IOException {
        group.shutdownNow();
    }

    protected void run(final ReliableServerSocket.ReliableClientSocket socket) throws Exception {
        final SocketAddress sockAds = socket.getRemoteSocketAddress();
        AbstractNetConnect con = startNet.OVER_MAP.get(sockAds.toString());

        if (con == null) {
            con = abstractNetConnect.getVersionNet(new ConnectionAgreement(socket,startNet));
            startNet.OVER_MAP.put(sockAds.toString(), con);
        }

        final AbstractNetConnect conFinal = con;
        group.execute(() -> {
            while (!socket.isClosed()) {
                try {
                    DataInputStream in = new DataInputStream(socket.getInputStream());
                    int size = in.readInt();
                    int type = in.readInt();
                    byte[] bytes = new byte[size];
                    int bytesRead = 0;
                    while (bytesRead < size) {
                        int readIn = in.read(bytes, bytesRead, size - bytesRead);
                        if (readIn == -1) {
                            break;
                        }
                        bytesRead += readIn;
                    }
                    typeConnect.typeConnect(conFinal, new Packet(type,bytes));
                } catch (Exception e) {
                    Log.error("UDP READ", e);
                    conFinal.disconnect();
                    startNet.OVER_MAP.remove(sockAds.toString());
                    return;
                }
            }
            Log.clog("socket 关闭 导致断开:");
            try{
                Log.clog(conFinal.getPlayer().name);
            }catch (Exception e){
                e.printStackTrace();
            }
            conFinal.disconnect();
            startNet.OVER_MAP.remove(sockAds.toString());
        });
    }

}
