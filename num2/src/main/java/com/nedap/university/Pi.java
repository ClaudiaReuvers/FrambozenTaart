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

    Pi(int port) throws SocketException {
        socket = new DatagramSocket(port);
    }

    @Override
    public void run() {
        while(isConnected()) {
            byte[] receiveData = new byte[1024];
            DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
            handleIncommingPacket(receivedPacket);
        }
        System.out.println("No longer connected (at receive)");
    }

    private void handleIncommingPacket(DatagramPacket receivedPacket) {
        ExtraHeader header = ExtraHeader.returnHeader(receivedPacket.getData());
        int length = header.getLength();
        sendResponse(header, receivedPacket.getAddress(), receivedPacket.getPort());
    }

    private void sendResponse(ExtraHeader header, InetAddress IP, int port) {
        if (header.isSyn() & !header.isAck()) {
            ExtraHeader newHeader = new ExtraHeader(true, true, false, false, 0, 0);
            byte[] sendData = newHeader.getHeader();
            DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IP, port);
            try {
                socket.send(sendPacket);
            } catch (IOException e) {
                e.printStackTrace();//TODO
            }
        }
    }

//    public void receivePackets() {
//        byte[] receiveData = new byte[64];
//        DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
//        try {
//            socket.receive(receivedPacket);
//        } catch (IOException e) {
//            e.printStackTrace();//TODO
//        }
//        System.out.println("Received data from: " + receivedPacket.getAddress() + "(IP) at port " + receivedPacket.getPort());
//        ExtraHeader header = ExtraHeader.returnHeader(receivedPacket.getData());
//        System.out.println("Header: SYN(" + header.isSyn() + "), ACK(" + header.isAck() + ")");
//    }

    public boolean isConnected() {
        return socket.isConnected();
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