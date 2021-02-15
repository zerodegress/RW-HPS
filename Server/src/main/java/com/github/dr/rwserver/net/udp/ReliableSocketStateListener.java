package com.github.dr.rwserver.net.udp;

/**
 * The listener interface for receiving socket events.
 * The class that is interested in processing a socket
 * event implements this interface.
 *
 * @author Adrian Granados
 *
 */
public interface ReliableSocketStateListener {
    /**
     * Invoked when the connection is opened.
     */
    public void connectionOpened(ReliableSocket sock);

    /**
     * Invoked when the attempt to establish a connection is refused.
     */
    public void connectionRefused(ReliableSocket sock);

    /**
     * Invoked when the connection is closed.
     */
    public void connectionClosed(ReliableSocket sock);

    /**
     * Invoked when the (established) connection fails.
     */
    public void connectionFailure(ReliableSocket sock);

    /**
     * Invoked when the connection is reset.
     */
    public void connectionReset(ReliableSocket sock);
}
