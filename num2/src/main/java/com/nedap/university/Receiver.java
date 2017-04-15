package com.nedap.university;

import javax.xml.crypto.Data;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by claudia.reuvers on 14/04/2017.
 */
public class Receiver extends Thread {

    private DatagramSocket receivingSocket;
    private Client client;
//    private int destPort;
//    private InetAddress destAddress;
    private boolean isConnected = true;
    private Queue<DatagramPacket> queue;

    Receiver(DatagramSocket receivingSocket, Client client) {
        this.receivingSocket = receivingSocket;
        this.client = client;
        this.queue = new ConcurrentLinkedQueue<>();
    }

    @Override
    public void run() {
        while (isConnected) {
            DatagramPacket receivedPacket = receivePackets();
            queue.add(receivedPacket);
            setClientPacketArrived();
        }
        System.out.println("No longer connected.");
    }

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

    public DatagramPacket receivePackets(){
        byte[] buf = new byte[1024];
        DatagramPacket receivedPacket = new DatagramPacket(buf, buf.length);
        try {
            receivingSocket.receive(receivedPacket);
        } catch (IOException e) {
            e.printStackTrace();//TODO
        }
        return receivedPacket;
    }

    public DatagramPacket getPacketInQueue() {
        DatagramPacket packet = queue.remove();
        setClientPacketArrived();
        return packet;
    }
}
