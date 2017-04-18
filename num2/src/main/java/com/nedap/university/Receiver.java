package com.nedap.university;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by claudia.reuvers on 14/04/2017.
 *
 * @author claudia.reuvers
 */
public class Receiver extends Thread {

    private DatagramSocket receivingSocket;
    private Client client;
//    private int destPort;
//    private InetAddress destAddress;
    private boolean isConnected = true;
    private Queue<DatagramPacket> queue;

    /**
     * Creates a <code>Receiver</code> connected to a specified <code>DatagramSocket</code> and <code>Client</code>.
     * @param receivingSocket socket from which packets are received
     * @param client client to which this receiver belongs
     */
    Receiver(DatagramSocket receivingSocket, Client client) {
        this.receivingSocket = receivingSocket;
        this.client = client;
        this.queue = new ConcurrentLinkedQueue<>();
    }

    /**
     * Receive packets and add them to the queue.
     * If a packet arrives and the queue is larger than 0 items, the client is notified.
     */
    @Override
    public void run() {
        while (isConnected) { //TODO: set this boolean to false when no longer connected
            DatagramPacket receivedPacket = receivePackets();
            queue.add(receivedPacket);
            setClientPacketArrived();
        }
        System.out.println("No longer connected.");
    }

    /**
     * Notify the client if there are packets in the queue.
     */
    private void setClientPacketArrived() {
        if (queue.size() > 0) {
            client.packetArrived(true);
        } else {
            client.packetArrived(false);
        }
    }
//    public void setDestPort(int destPort) {
//        this.destPort = destPort;
//    }
//
//    public int getDestPort() {
//        return this.destPort;
//    }
//
//    public void setDestAddress(InetAddress destAddress) {
//        this.destAddress = destAddress;
//    }
//
//    public InetAddress getDestAddress() {
//        return this.destAddress;
//    }

    /**
     * Receive a packet from the socket.
     * @return <code>DatagramPacket</code> retrieved from the socket
     */
    private DatagramPacket receivePackets(){
        byte[] buf = new byte[10240];
        DatagramPacket receivedPacket = new DatagramPacket(buf, buf.length);
        try {
            receivingSocket.receive(receivedPacket);
            ExtraHeader h = ExtraHeader.returnHeader(receivedPacket.getData());
            System.out.println("Received: " + ExtraHeader.returnHeader(receivedPacket.getData()));
        } catch (IOException e) {
            if (!client.getFINreceived()) {
                System.out.println("Could not receive a file from this socket (" + receivingSocket.getInetAddress() + "::" + receivingSocket.getPort() + ")");
            }
        }
        return receivedPacket;
    }

    /**
     * Returns the first packet in the queue
     * @return first <code>DatagramPacket</code> in the queue
     */
    DatagramPacket getPacketInQueue() {
        DatagramPacket packet = queue.remove();
        setClientPacketArrived();
        return packet;
    }

    /**
     * Returns the <code>DatagramSocket</code> on which this <code>Receiver</code> receives its packets.
     * @return the receiving <code>DatagramSocket</code>
     */
    public DatagramSocket getReceivingSocket() {
        return this.receivingSocket;
    }
}
