/*
 * Copyright 2020-2023 RW-HPS Team and contributors.
 *
 * 此源代码的使用受 GNU AFFERO GENERAL PUBLIC LICENSE version 3 许可证的约束, 可以在以下链接找到该许可证.
 * Use of this source code is governed by the GNU AGPLv3 license that can be found through the following link.
 *
 * https://github.com/RW-HPS/RW-HPS/blob/master/LICENSE
 */

package net.rwhps.server.util.internal.net.rudp;

import net.rwhps.server.net.core.TypeConnect;
import net.rwhps.server.util.internal.net.rudp.impl.Segment;
import net.rwhps.server.util.log.Log;
import org.jetbrains.annotations.NotNull;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.SocketAddress;
import java.util.ArrayList;

/**
 * ReliableClientSocket
 *
 * @author RW-HPS/Dr
 * @date 2023/7/17 8:41
 */
public class ReliableClientSocket extends ReliableSocket {
    private final ArrayList<Segment> queue = new ArrayList<>();
    private final RUDPHeadProcessor selector;

    public TypeConnect type = null;
    public int needLength = 0;
    public int needType = 0;

    public ReliableClientSocket(RUDPHeadProcessor selector, DatagramSocket sock, SocketAddress endpoint) throws IOException {
        super(sock);
        this.endpoint = endpoint;
        this.selector = selector;
    }

    @Override
    protected void init(DatagramSocket sock, ReliableSocketProfile profile) {
        super.init(sock, profile);
    }

    DataInputStream s = null;

    @Override
    public void handleSegment(@NotNull Segment segment) {
        super.handleSegment(segment);

        if (_in.update() > 0 && _in != null) {
            if (needLength == 0 && needType == 0) {
                if (_in.available() < 8) {
                    return;
                }
                if (s == null) {
                    s = new DataInputStream(_in);
                }
                try {
                    needLength = s.readInt();
                    needType = s.readInt();
                } catch (IOException e) {
                }
            }

            if (_in.available() >= needLength) {
                selector.registerRead(this);
            }
        }
    }


    @Override
    protected Segment receiveSegmentImpl() {
        synchronized (queue) {
            while (queue.isEmpty()) {
                try {
                    queue.wait();
                } catch (InterruptedException xcp) {
                    Log.error("[RUDP]", xcp);
                }
            }

            return queue.remove(0);
        }
    }

    protected void segmentReceived(Segment s) {
        synchronized (queue) {
            queue.add(s);
            queue.notify();
        }
    }

    @Override
    protected void closeSocket() {
        synchronized (queue) {
            queue.clear();
            queue.add(null);
            queue.notify();
        }
    }

    @Override
    protected void log(String msg) {
        Log.debug(getPort() + ": " + msg);
    }
}
