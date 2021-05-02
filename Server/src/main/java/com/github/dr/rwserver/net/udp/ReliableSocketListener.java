package com.github.dr.rwserver.net.udp;

/**
 * The listener interface for receiving packet events.
 * The class that is interested in processing a packet
 * event implements this interface.
 *
 * @author Adrian Granados
 *
 */
public interface ReliableSocketListener {
    /**
     * Invoked when a data packet is sent.
     */
    void packetSent();

    /**
     * Invoked when a data packet is retransmitted.
     */
    void packetRetransmitted();

    /**
     * Invoked when a data packet is received in-order.
     */
    void packetReceivedInOrder();

    /**
     * Invoked when a out of sequence data packet is received.
     */
    void packetReceivedOutOfOrder();
}
