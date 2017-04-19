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
    private Queue<DatagramPacket> receivedFrames;
    private long LastSeqReceived;
    private int RECEIVERWINDOW = 4;

    /**
     * Creates a <code>Receiver</code> connected to a specified <code>DatagramSocket</code> and <code>Client</code>.
     * @param receivingSocket socket from which packets are received
     * @param client client to which this receiver belongs
     */
    Receiver(DatagramSocket receivingSocket, Client client) {
        this.receivingSocket = receivingSocket;
        this.client = client;
        this.queue = new ConcurrentLinkedQueue<>();
        this.receivedFrames = new ConcurrentLinkedQueue<>();
    }

    /**
     * Receive packets and add them to the queue.
     * If a packet arrives and the queue is larger than 0 items, the client is notified.
     */
    @Override
    public void run() {
        while (isConnected) { //TODO: set this boolean to false when no longer connected
            DatagramPacket receivedPacket = receivePackets();
            while (receivedPacket != null) {
                receivedFrames.add(receivedPacket);
                ExtraHeader receivedHeader = ExtraHeader.returnHeader(receivedPacket.getData());
                if (receivedHeader.isSyn()) {
                    System.out.println("Received a SYN, LSeqR set to " + (receivedHeader.getSeqNr() - 1));
                    setLastFrameReceived(receivedHeader.getSeqNr() - 1);
                } else if (receivedHeader.isDNSResponse()) {
                    queue.add(receivedPacket);

                }
                if (queue.contains(receivedPacket)) {
                    System.out.println("Set rcvdPkt to null");
                    receivedPacket = null;
                }
                while (isInReceivingWindow(receivedPacket)) {
                    queue.add(receivedPacket);
                    System.out.println("QueueSize: " + queue.size());
                    updateLastFrameReceived();
                    System.out.println("LSeqR = " + LastSeqReceived);
//                    setClientPacketArrived();
                }
                setClientPacketArrived();
                receivedPacket = receivePackets();
            }
        }
        System.out.println("No longer connected.");
    }

    private void updateLastFrameReceived() {
        boolean updated = false;
        while (!updated) {
            for (DatagramPacket rcvd : receivedFrames) {
                ExtraHeader header = ExtraHeader.returnHeader(rcvd.getData());
                long seqNr = header.getSeqNr();
                if (seqNr == LastSeqReceived + 1) {
                    LastSeqReceived++;
                    receivedFrames.remove(rcvd);
                    queue.add(rcvd);
                    System.out.println("LSeqR updated to " + LastSeqReceived + ". Packet added to queue; " + header);
                    updated = true;
                }
            }
        }
    }

    private boolean isInReceivingWindow(DatagramPacket receivedPacket) {
        if (receivedPacket == null) {
            return false;
        }
        ExtraHeader receivedHeader = ExtraHeader.returnHeader(receivedPacket.getData());
//        if (receivedHeader.isDNSRequest() | receivedHeader.isDNSResponse()) {
//            receivedFrames.add(receivedPacket);
//            System.out.println("Added to queue: " + receivedHeader);
//            setClientPacketArrived();
//            return true;
//        }
        long nr = receivedHeader.getSeqNr();
        return (nr > LastSeqReceived && nr <= (LastSeqReceived + RECEIVERWINDOW));
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
//        ExtraHeader h = null;
        try {
            receivingSocket.receive(receivedPacket);
//            h = ExtraHeader.returnHeader(receivedPacket.getData());
//            System.out.println("Received: " + ExtraHeader.returnHeader(receivedPacket.getData()));
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

    private void setLastFrameReceived(long LFR) {
        this.LastSeqReceived = LFR;
    }
}
