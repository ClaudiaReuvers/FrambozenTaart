package com.nedap.university;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Created by claudia.reuvers on 14/04/2017.
 *
 * @author claudia.reuvers
 */
class Sender {

//    private int sourcePort;
    private int destPort;
    private DatagramSocket socket;
    private InetAddress destAddress;
//    private boolean isConnected;

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
    void send(ExtraHeader header, byte[] data) throws IOException {
        header.setLength(data.length);
        System.out.println(header);
        byte[] sendData = header.getHeader();//TODO
        DatagramPacket packet = new DatagramPacket(sendData, sendData.length, destAddress, destPort);
        socket.send(packet);
    }

//    public void setSourcePort(int sourcePort) {
//        this.sourcePort = sourcePort;
//    }

    /**
     * Sets the port to which the packets must be send.
     * @param destPort port to which the packets must be send
     */
    void setDestPort(int destPort) {
        this.destPort = destPort;
    }

//    public int getSourcePort() {
//        return this.sourcePort;
//    }

//    public int getDestPort() {
//        return this.destPort;
//    }

    /**
     * Sets the InetAddress to which the packets must be send.
     * @param destAddress InetAddress to which the packets must be send
     */
    void setDestAddress(InetAddress destAddress) {
        this.destAddress = destAddress;
    }

//    public InetAddress getDestAddress() {
//        return this.destAddress;
//    }


}
