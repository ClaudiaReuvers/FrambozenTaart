package com.nedap.university;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Random;

/**
 * Created by claudia.reuvers on 10/04/2017.
 */
class Client extends Thread {

    private InetAddress serverIP;
    private int serverPort;
    private DatagramSocket socket;
    private BufferedReader in;
    private long nextAckExpected;

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
        System.out.print("Rcvd header: ");
        printHeader(header);
        int length = header.getLength();
        byte[] data = generateData(packet.getData(), length);
        appendDataToFile(data);
        sendResponse(header, data);
    }

    private void sendResponse(ExtraHeader receivedHeader, byte[] data) {
        ExtraHeader newHeader;
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
//        ExtraHeader newHeader;
        if (receivedHeader.isSyn() & !receivedHeader.isAck() & !receivedHeader.isFin()) { // SYN (no ACK, no FIN)
            // TODO: create a new Socket for communication s.t. the 'main' socket is open for new clients
            System.out.println("I see a SYN packet");
            long sendSeqNr = (new Random()).nextInt(2^32);
            nextAckExpected = sendSeqNr + 1;
            newHeader = new ExtraHeader(true, true, false, false, 0, sendSeqNr);
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
        int seqNr = (new Random()).nextInt(2^32);
        byte[] header = (new ExtraHeader(true, false, false, false, 0, seqNr)).getHeader();
        System.out.print("Send header: ");
        printHeader(new ExtraHeader(true, false, false, false, 0, seqNr));
        DatagramPacket sendPacket = new DatagramPacket(header, header.length, this.serverIP, this.serverPort);
        nextAckExpected = seqNr + 1;
        try {
            socket.send(sendPacket);
        } catch (IOException e) {
//            e.printStackTrace();//TODO
        }
    }

    private void printHeader(ExtraHeader header) {
        System.out.println("Length: " + header.getLength() + ". SYN: " + header.isSyn() + "[" + header.getSeqNr() + "]. ACK: " + header.isAck() + "[" + header.getAckNr() + "].");
    }

    private void writeByteArrayToFile(byte[] byteArrayOfFile, String name) {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(byteArrayOfFile));
            File outputfile = new File(name);
            ImageIO.write(image, "jpg", outputfile);
        } catch (IOException e) {
            e.printStackTrace(); //TODO
        }
    }

    private byte[] writeFileToByteArray(String filename) {
        byte[] fileInBytes;
        filename = "Files/" + filename;
        Path path = Paths.get(filename);
        try {
            fileInBytes = Files.readAllBytes(path);
        } catch (IOException e) {
            e.printStackTrace();//TODO
            fileInBytes = new byte[0];
        }
        return fileInBytes;
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