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
    private long nextAckExpected;

    Pi(int port) throws SocketException {
        socket = new DatagramSocket(port);
    }

    @Override
    public void run() {
        while (true) {//(isConnected()) { //TODO: stop running
            byte[] buffer = new byte[1024];
            DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(receivedPacket);
            } catch (IOException e) {
                e.printStackTrace();//TODO
            }
            if (receivedPacket.getData().length > 9) { //only send a response if the packet has at least a header
                handleIncommingPacket(receivedPacket);
            }
        }
//        System.out.println("No longer connected (at receive)");
    }

    private void handleIncommingPacket(DatagramPacket receivedPacket) {
        ExtraHeader header = ExtraHeader.returnHeader(receivedPacket.getData());
        System.out.println("Length: " + header.getLength() + ". SYN: " + header.isSyn() + ". ACK: " + header.isAck() + "(" + header.getAckNr() + "). SeqNr: " + header.getSeqNr() + ".");
        sendResponse(header, receivedPacket.getAddress(), receivedPacket.getPort());
    }

    private void sendResponse(ExtraHeader header, InetAddress IP, int port) {
        ExtraHeader newHeader;
        if (header.isSyn() & !header.isAck() & !header.isFin()) { // SYN (no ACK, no FIN)
            // TODO: create a new Socket for communication s.t. the 'main' socket is open for new clients
            System.out.println("I see a SYN packet");
            newHeader = new ExtraHeader(true, true, false, false, 0, 0);
        } else if (header.isAck() & !header.isFin() & !header.isSyn()) { //ACK (no SYN, no FIN)
            System.out.println("I see an ACK packet");
            newHeader = new ExtraHeader(false, true, false, false, 0, 0);
        } else if (header.isSyn() & header.isAck() & !header.isFin()) { // SYN ACK (no FIN)
            System.out.println("I see a SYN ACK packet");
            newHeader = new ExtraHeader(false, true, false, false, 0, 0);
        } else if (header.isFin() & !header.isSyn() & !header.isAck()) { // FIN (no SYN, no ACK)
            System.out.println("I see a FIN packet");
            newHeader = new ExtraHeader(false, true, true, false, 0, 0);
        } else if (header.isFin() & !header.isSyn() & header.isAck()) { // FIN ACK (no SYN)
            System.out.println("I see a FIN ACK packet");
            newHeader = new ExtraHeader(false, true, false, false, 0, 0);
        } else {
            //TODO: packet not recognized, invalid flag combination; for now: send ACK
            System.out.println("Unrecognized packet");
            newHeader = new ExtraHeader(false, true, false, false, 0, 0);
        }
        byte[] sendData = newHeader.getHeader();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IP, port); //send SYN-ACK back
        try {
            socket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();//TODO
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