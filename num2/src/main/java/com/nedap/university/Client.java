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
        while(true) {
            sendSYN();
        }
//        receivePacket();
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

    void receivePacket() {
        DatagramPacket receivedPacket = new DatagramPacket(new byte[64], 64);
        try {
            socket.receive(receivedPacket);
        } catch (IOException e) {
            e.printStackTrace();//TODO
        }
        byte[] data = receivedPacket.getData();
        String txt = "";
        for (int i = 0; i < data.length; i++) {
            txt += data[i] + ".";
        }
        System.out.println(txt);
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