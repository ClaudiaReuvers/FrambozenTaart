package com.nedap.university;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random;

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
        System.out.print("Rcvd header: ");
        printHeader(header);
        sendResponse(header, receivedPacket.getAddress(), receivedPacket.getPort());
    }

    private void printHeader(ExtraHeader header) {
        System.out.println("Length: " + header.getLength() + ". SYN: " + header.isSyn() + "[" + header.getSeqNr() + "]. ACK: " + header.isAck() + "[" + header.getAckNr() + "].");
    }

    private void sendResponse(ExtraHeader receivedHeader, InetAddress IP, int port) {
        if (receivedHeader.isAck()) {
            long receivedAckNr = receivedHeader.getAckNr();
            if (receivedAckNr != nextAckExpected) {
                //TODO
                System.out.println("The received ackNr is " + receivedAckNr + ", but " + nextAckExpected + " was expected.");
                return;
            }
        }
        long receivedAckNr = receivedHeader.getAckNr();
        long receivedSeqNr = receivedHeader.getSeqNr();
        ExtraHeader newHeader;
        if (receivedHeader.isSyn() & !receivedHeader.isAck() & !receivedHeader.isFin()) { // SYN (no ACK, no FIN)
            // TODO: create a new Socket for communication s.t. the 'main' socket is open for new clients
            System.out.println("I see a SYN packet");
            long sendSeqNr = (new Random()).nextInt(2^32);
            long sendAckNr = receivedHeader.getSeqNr() + 1;
            nextAckExpected = sendSeqNr + 1;
            newHeader = new ExtraHeader(true, true, false, false, sendAckNr, sendSeqNr);
            System.out.print("Send header: ");
            printHeader(newHeader);
        } else if (receivedHeader.isAck() & !receivedHeader.isFin() & !receivedHeader.isSyn()) { //ACK (no SYN, no FIN)
            System.out.println("I see an ACK packet");
            long sendSeqNr = receivedHeader.getAckNr();
            long sendAckNr = receivedHeader.getSeqNr() + 1; //TODO: increase ackNr with datalength if the packet has data
            nextAckExpected = sendSeqNr + 1;
            newHeader = new ExtraHeader(false, true, false, false, sendAckNr, sendSeqNr);
        } else if (receivedHeader.isSyn() & receivedHeader.isAck() & !receivedHeader.isFin()) { // SYN ACK (no FIN)
            System.out.println("I see a SYN ACK packet");
            long sendSeqNr = receivedHeader.getAckNr();
            long sendAckNr = receivedHeader.getSeqNr() + 1;
            nextAckExpected = sendSeqNr + 1;
            newHeader = new ExtraHeader(false, true, false, false, sendAckNr, sendSeqNr);
        } else if (receivedHeader.isFin() & !receivedHeader.isSyn() & !receivedHeader.isAck()) { // FIN (no SYN, no ACK) //TODO: initialise shutdown
            long sendSeqNr = receivedHeader.getAckNr();
            long sendAckNr = receivedHeader.getSeqNr() + 1; //TODO: increase ackNr with datalength if the packet has data
            nextAckExpected = sendSeqNr + 1;
            System.out.println("I see a FIN packet");
            newHeader = new ExtraHeader(false, true, true, false, sendAckNr, sendAckNr);
        } else if (receivedHeader.isFin() & !receivedHeader.isSyn() & receivedHeader.isAck()) { // FIN ACK (no SYN) //TODO: shutdown
            long sendSeqNr = receivedHeader.getAckNr();
            long sendAckNr = receivedHeader.getSeqNr() + 1; //TODO: increase ackNr with datalength if the packet has data
            nextAckExpected = sendSeqNr + 1;
            System.out.println("I see a FIN ACK packet");
            newHeader = new ExtraHeader(false, true, false, false, sendAckNr, sendSeqNr);
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