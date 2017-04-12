package com.nedap.university;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Main {

    private static boolean keepAlive = true;
    private static boolean running = false;

    private static InetAddress PiIP;
    private static final int broadcastPortPi = 9876;

    private Main() {}

    public static void main(String[] args) {
//        running = true;
//        System.out.println("Hello, Nedap University!");
//
//        initShutdownHook();
//
//        while (keepAlive) {
//            try {
//                // do useful stuff
//                Thread.sleep(1000);
//            } catch (InterruptedException e) {
//                Thread.currentThread().interrupt();
//            }
//        }
//
//        System.out.println("Stopped");
//        running = false;

        //Get IPaddress of the Pi
        //TODO: get this IPaddress by mDNS
        try {
            PiIP = InetAddress.getByName("192.168.40.6");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        if (args[0].equals("pi")) {
            System.out.println("Hi pi!");
            try {
                Pi pi = new Pi(broadcastPortPi);
                pi.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (args[0].equals("client")) {
            System.out.println("Hi laptop!");
            try {
                Client client = new Client(PiIP, broadcastPortPi);
                client.start();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static String readString(String prompt) {
        System.out.print(prompt);
        String msg = null;
        try {
            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
            msg = in.readLine();
        } catch (IOException e) {
            System.out.println("IOException at readString in client.");
        }
        return (msg == null) ? "" : msg;
    }

    private static void initShutdownHook() {
        final Thread shutdownThread = new Thread() {
            @Override
            public void run() {
                keepAlive = false;
                while (running) {
                    try {
                        Thread.sleep(10);
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                    }
                }
            }
        };
        Runtime.getRuntime().addShutdownHook(shutdownThread);
    }
}

//class UDPClient
//{
//    public static void main(String args[]) throws Exception
//    {
//        BufferedReader inFromUser =
//                new BufferedReader(new InputStreamReader(System.in));
//        DatagramSocket clientSocket = new DatagramSocket();
//        InetAddress IPAddress = InetAddress.getByName("localhost");
//        byte[] sendData = new byte[1024];
//        byte[] receiveData = new byte[1024];
//        String sentence = inFromUser.readLine();
//        sendData = sentence.getBytes();
//        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IPAddress, 9876);
//        clientSocket.send(sendPacket);
//        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
//        clientSocket.receive(receivePacket);
//        String modifiedSentence = new String(receivePacket.getData());
//        System.out.println("FROM SERVER:" + modifiedSentence);
//        clientSocket.close();
//    }
//}