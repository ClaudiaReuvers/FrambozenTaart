package com.nedap.university;

import com.nedap.university.Utils.Utils;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.concurrent.ConcurrentHashMap;

import static com.nedap.university.Utils.Utils.joinByteArrays;

/**
 * Created by claudia.reuvers on 14/04/2017.
 *
 * @author claudia.reuvers
 */
class Sender implements TimeOutEventHandler {

//    private int sourcePort;
    private int destPort;
    private DatagramSocket socket;
    private InetAddress destAddress;
//    private boolean isConnected;
    static final int TIMEOUT = 2000;
    private ConcurrentHashMap<Long, DatagramPacket> unAcknowledgedPackets = new ConcurrentHashMap<>();
    private long nextAckExpected;

    /**
     * Creates a Sender with a socket connected.
     * @param socket socket from which data is sended
     */
    Sender(DatagramSocket socket) {
        this.socket = socket;
    }

    /**
     * Sends the data with the given header.
     * Creates one data packet starting with the header and followed by the header. The datalength in the header is set to the length of <code>data</code>.
     * @param header header of the packet
     * @param data data of the packet
     * @throws IOException if the <code>Sender</code> is not able to send the packet to the destinaation
     */
    void send(ExtraHeader header, byte[] data){
        header.setLength(data.length);
        nextAckExpected = header.getSeqNr() + data.length + 1;
        byte[] sendData = joinByteArrays(header.getHeader(), data);
        System.out.println("Send: " + header);
        DatagramPacket packet = new DatagramPacket(sendData, sendData.length, destAddress, destPort);
        try {
            socket.send(packet);
            addUnackedPacket(header.getSeqNr() + data.length + 1, packet);
            Utils.Timeout.SetTimeout(TIMEOUT, this, packet);
        } catch (IOException e) {
            System.out.println("Unable to send packet: " + header);
        }
    }

    private void addUnackedPacket(long ackNr, DatagramPacket packet) {
        unAcknowledgedPackets.put(ackNr, packet);
    }

    /**
     * Sets the port to which the packets must be send.
     * @param destPort port to which the packets must be send
     */
    void setDestPort(int destPort) {
        this.destPort = destPort;
    }

    /**
     * Sets the InetAddress to which the packets must be send.
     * @param destAddress InetAddress to which the packets must be send
     */
    void setDestAddress(InetAddress destAddress) {
        this.destAddress = destAddress;
    }

    void send(ExtraHeader header, byte[] data, InetAddress broadcastIP, int broadcastPort) {
        header.setLength(data.length);
        byte[] sendData = header.getHeader();//joinByteArrays(header.getHeader(), data);
        DatagramPacket packet = new DatagramPacket(sendData, sendData.length, broadcastIP, broadcastPort);
        System.out.println("Send: " + header);
        try {
            socket.send(packet);
            addUnackedPacket(header.getSeqNr() + data.length + 1, packet);
            Utils.Timeout.SetTimeout(TIMEOUT, this, packet);
        } catch (IOException e) {
            System.out.println("Unable to send packet: " + header);
        }
    }

    @Override
    public void TimeoutElapsed(DatagramPacket tag) {
        if (tag instanceof DatagramPacket) {
            DatagramPacket packet = (DatagramPacket) tag;
//            DatagramPacket packet = unAcknowledgedPackets.get(ackNr);
            System.out.println("Resend: " + ExtraHeader.returnHeader(packet.getData()));
            try {
                socket.send(packet);
                Utils.Timeout.SetTimeout(TIMEOUT, this, packet);
            } catch (IOException e) {
                System.out.println("Unable to resend packet: " + ExtraHeader.returnHeader(packet.getData()));
            }
        }
    }

    void acknowledgePacket(long ackNr) {
        unAcknowledgedPackets.remove(ackNr);
    }

    public long getNextAckExpected() {
        return nextAckExpected;
    }
}
