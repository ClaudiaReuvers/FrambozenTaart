package com.nedap.university;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by claudia.reuvers on 10/04/2017.
 */
class Pi extends Thread {

    private DatagramSocket socket;

    public Pi(int port) throws SocketException {
        socket = new DatagramSocket(port);
    }

    @Override
    public void run() {
        receivePackets();
    }

    private void receivePackets() {
        byte[] receiveData = new byte[64];
        DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
        try {
            socket.receive(receivedPacket);
        } catch (IOException e) {
            e.printStackTrace();//TODO
        }
        System.out.println("Received data from: " + receivedPacket.getAddress() + "(IP) at port " + receivedPacket.getPort());
        ExtraHeader header = ExtraHeader.returnHeader(receivedPacket.getData());
        System.out.println("Header: SYN(" + header.isSyn() + "), ACK(" + header.isAck() + ")");
    }

//    public Pi() throws Exception
//    {
//        DatagramSocket serverSocket = new DatagramSocket(9876);
//        System.out.println("I'm listening on port 9876");
//        byte[] receiveData = new byte[1024];
//        byte[] sendData = new byte[1024];
//        while(true)
//        {
//            DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
//            serverSocket.receive(receivePacket);
//            System.out.println("I receive my packets from IP " + receivePacket.getAddress());
//            String sentence = new String( receivePacket.getData());
//            System.out.println("RECEIVED: " + sentence);
//            InetAddress IPAddress = receivePacket.getAddress();
//            int port = receivePacket.getPort();
//            String capitalizedSentence = sentence.toUpperCase();
//            sendData = capitalizedSentence.getBytes();
//            DatagramPacket sendPacket =
//                    new DatagramPacket(sendData, sendData.length, IPAddress, port);
//            serverSocket.send(sendPacket);
//        }
//    }
}