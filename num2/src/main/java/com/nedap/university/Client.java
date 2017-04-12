package com.nedap.university;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

/**
 * Created by claudia.reuvers on 10/04/2017.
 */
class Client extends Thread {

    private InetAddress serverIP;
    private int serverPort;
    private DatagramSocket socket;
    private BufferedReader in;

    Client(InetAddress connectingIP, int connectingPort) throws IOException {
        this.serverIP = connectingIP;
        this.serverPort = connectingPort;
        this.socket = new DatagramSocket();
        this.in = new BufferedReader(new InputStreamReader(System.in));
    }

    @Override
    public void run() {
        sendSYN();
        while (true) {//(isConnected()) {//TODO: stop running
            byte[] receiveData = new byte[1024];
            DatagramPacket receivedPacket = new DatagramPacket(receiveData, receiveData.length);
            try {
                socket.receive(receivedPacket);
            } catch (IOException e) {
                e.printStackTrace();//TODO
            }
            handleIncommingPacket(receivedPacket);
        }
//        System.out.println("No longer connected");
    }

    private void handleIncommingPacket(DatagramPacket packet) {
        ExtraHeader header = ExtraHeader.returnHeader(packet.getData());
        int length = header.getLength();
        byte[] data = generateData(packet.getData(), length);
        appendDataToFile(data);
        sendResponse(header, data);
    }

    private void sendResponse(ExtraHeader header, byte[] data) {
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
//        sendPacket(newHeader, new byte[0]);
        byte[] sendData = newHeader.getHeader();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, serverIP, serverPort); //send SYN-ACK back
        try {
            socket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();//TODO
        }
    }

    private void sendPacket(ExtraHeader header, byte[] data) {
        byte[] sendingData = new byte[header.getLength() + data.length];
        DatagramPacket sendPacket = new DatagramPacket(sendingData, sendingData.length, serverIP, serverPort);
        try {
            socket.send(sendPacket);
        } catch (IOException e) {
            e.printStackTrace();//TODO
        }
    }

    private void appendDataToFile(byte[] data) {
        //TODO
    }

    private byte[] generateData(byte[] dataAndHeader, int length) {
        byte[] data = new byte[dataAndHeader.length - length];
        for (int i = length; i < dataAndHeader.length; i++) {
            data[i - length] = dataAndHeader[i];
        }
        return data;
    }

//    public void receivePackets() {
//        DatagramPacket receivedPacket = new DatagramPacket(new byte[64], 64);
//        try {
//            socket.receive(receivedPacket);
//        } catch (IOException e) {
//            e.printStackTrace();//TODO
//        }
//        byte[] data = receivedPacket.getData();
//        String txt = "";
//        for (int i = 0; i < data.length; i++) {
//            txt += data[i] + ".";
//        }
//        System.out.println(txt);
//    }

    private boolean isConnected() {
        return socket.isConnected();
    }

    private void sendSYN() {
        byte[] header = (new ExtraHeader(true, false, false, false, 0, 0)).getHeader();
        DatagramPacket sendPacket = new DatagramPacket(header, header.length, this.serverIP, this.serverPort);
        try {
            socket.send(sendPacket);
        } catch (IOException e) {
//            e.printStackTrace();//TODO
        }
    }

//    {
//        System.out.println("I send my packets to IP " + IP);
//        BufferedReader inFromUser =
//                new BufferedReader(new InputStreamReader(System.in));
//        DatagramSocket clientSocket = new DatagramSocket();
//        byte[] sendData = new byte[1024];
//        byte[] receiveData = new byte[1024];
//        String sentence = inFromUser.readLine();
//        sendData = sentence.getBytes();
//        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, connectingIP, 9876);
//        clientSocket.send(sendPacket);
//        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
//        clientSocket.receive(receivePacket);
//        String modifiedSentence = new String(receivePacket.getData());
//        System.out.println("FROM SERVER:" + modifiedSentence);
//        clientSocket.close();
//    }
}