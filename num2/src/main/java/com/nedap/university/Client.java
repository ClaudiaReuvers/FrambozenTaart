package com.nedap.university;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Random;

/**
 * Created by claudia.reuvers on 14/04/2017.
 */
public class Client {

    private InetAddress serverIP;
    private int serverPort;
    private DatagramSocket socket;
    private BufferedReader in;
    private long nextAckExpected;

    Client(InetAddress connectingIP, int connectingPort) throws SocketException {
        this.serverIP = connectingIP;
        this.serverPort = connectingPort;
        this.socket = new DatagramSocket();
        this.in = new BufferedReader(new InputStreamReader(System.in));
    }

    public void init() {
        sendSYN();
        while (true) {//TODO: stop running
            byte buffer[] = new byte[1024];
            DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(receivedPacket);
            } catch (IOException e) {
                e.printStackTrace();//TODO: handle exception at socket.receive
            }
            if (receivedPacket.getData().length > 9) {
                //Do something: e.g. handleIncommingPacket(receivedPacket)
            }
        }
    }

    private void sendSYN() { //TODO: look if still valid
        int seqNr = (new Random()).nextInt(2^32);
        byte[] header = (new ExtraHeader(true, false, false, false, 0, seqNr)).getHeader();
        System.out.print("Send header: ");
        System.out.println((new ExtraHeader(true, false, false, false, 0, seqNr)));
        DatagramPacket sendPacket = new DatagramPacket(header, header.length, this.serverIP, this.serverPort);
        nextAckExpected = seqNr + 1;
        try {
            socket.send(sendPacket);
        } catch (IOException e) {
//            e.printStackTrace();//TODO
        }
    }
}

//public class SecondClient extends Thread {
//
//    private boolean isSending = false;
//    private byte[] buffer = new byte[0];
//    private int offset = 0;
//
//    private void handleIncommingPacket(DatagramPacket receivedPacket) {
//        ExtraHeader receivedHeader = ExtraHeader.returnHeader(receivedPacket.getData());
//        System.out.print("Rcvd header: "); printHeader(receivedHeader);
//        if (checkReceivedAckNr(receivedHeader)) {
//            return;
//        }
//        byte[] sendingData;
//        long sendSeqNr = receivedHeader.getAckNr();
//        long increaseNumbering = 1;//receivedPacket.getData().length - receivedHeader.getLength() + 1;
//        long sendAckNr = receivedHeader.getSeqNr() + increaseNumbering;
//        nextAckExpected = sendSeqNr + increaseNumbering;
//        if (receivedHeader.isSyn() & !receivedHeader.isAck() & !receivedHeader.isFin()) {//SYN (no ACK, no FIN)
//            System.out.println("I see a SYN packet"); //TODO: create a new Socket for communication s.t. the 'main' socket is open for new clients
//            sendSeqNr = (new Random()).nextInt(2^32);
//            nextAckExpected = sendSeqNr + increaseNumbering;
////            long sendAckNr = receivedHeader.getSeqNr() + 1;
////            nextAckExpected = sendSeqNr + 1;
//            byte[] header = createSYNACK(sendSeqNr, sendAckNr);
//            sendingData = header;
//        } else if (receivedHeader.isSyn() & receivedHeader.isAck() & !receivedHeader.isFin()) {//SYN ACK (no FIN)
//            System.out.println("I see a SYN ACK packet");
//            ExtraHeader newHeader = determineChoice();
//            String request;
//            if (newHeader.isRequest()) {
//                if (newHeader.isGET()) {
//                    request = requestDownloadFileString(); //TODO
//                } else {
//                    request = requestSendFileString();
//                    isSending = true;
//                }
//                newHeader.setAckNr(sendAckNr);
//                newHeader.setSeqNr(sendSeqNr);
//                newHeader.setLength(request.getBytes().length);
//                byte[] header = newHeader.getHeader();
//                sendingData = joinHeaderAndData(header, request.getBytes());
//            } else {
//                newHeader.setAckNr(sendAckNr);
//                newHeader.setSeqNr(sendSeqNr);
//                sendingData = newHeader.getHeader();
//            }
////            sendSeqNr += request.length;
////            nextAckExpected += request.length;
//
////            long sendSeqNr = receivedHeader.getAckNr();
////            long sendAckNr = receivedHeader.getSeqNr() + 1;
////            nextAckExpected = sendSeqNr + 1;
////            byte[] header = createACK(sendSeqNr, sendAckNr);
////            sendingData = joinHeaderAndData(header, request);
//            System.out.println("Sendingdata is of length: " + sendingData.length);
//        } else if (!receivedHeader.isSyn() & receivedHeader.isAck() & !receivedHeader.isFin()) {//ACK (no SYN, no FIN)
//            System.out.println("I see an ACK packet"); //TODO: set adapt nextACKExpected & sendAckNr for data length
////            long sendSeqNr = receivedHeader.getAckNr();
////            long sendAckNr = receivedHeader.getSeqNr() + 1;
////            nextAckExpected = sendSeqNr + 1;
//            if (isSending) {
//                byte[] data = putPartOfBufferInSendingData(256, 0);
//                byte[] header = createACK(sendSeqNr, sendAckNr, data.length);
//                sendingData = joinHeaderAndData(header, data);
//            } else {
//                sendingData = new byte[0];
//            }
//
////            byte[] header = createACK(sendSeqNr, sendAckNr);
////            sendingData = header;
//        } else if (!receivedHeader.isSyn() & !receivedHeader.isAck() & receivedHeader.isFin()) {//FIN (no SYN, no ACK)
//            System.out.println("I see an FIN packet");
////            long sendSeqNr = receivedHeader.getAckNr();
////            long sendAckNr = receivedHeader.getSeqNr() + 1;
////            nextAckExpected = sendSeqNr + 1;
//            byte[] header = createFINACK(sendSeqNr, sendAckNr);
//            sendingData = header;
//        } else if (!receivedHeader.isSyn() & receivedHeader.isAck() & receivedHeader.isFin()) {//FIN ACK (no SYN)
//            System.out.println("I see an FIN ACK packet");
////            long sendSeqNr = receivedHeader.getAckNr();
////            long sendAckNr = receivedHeader.getSeqNr() + 1;
////            nextAckExpected = sendSeqNr + 1;
//            byte[] header = createACK(sendSeqNr, sendAckNr);
//            sendingData = header;
//        } else {
//            //TODO: invalid flag combination
//            System.out.println("Unrecognized packet");
//            return;
//        }
//        sendToClient(sendingData, receivedPacket.getAddress(), receivedPacket.getPort());
//    }
//
//    private byte[] putPartOfBufferInSendingData(int dataSize, int offset) {
//        byte[] data = new byte[dataSize];
//        for (int i = 0; i < dataSize; i++) {
//            data[i] = buffer[i + offset];
//        }
//        return data;
//    }
//
//    private ExtraHeader determineChoice() {
//        String choice;
//        boolean isValidChoice = false;
//        ExtraHeader header = new ExtraHeader(false, true, false, false, 0, 0);
//        while (!isValidChoice) {
//            choice = readString("Do you want to upload or download a file? (up/down/no)");
//            if (choice.equals("up")) {
//                header.setRequest(true);
//                isValidChoice = true;
//            } else if (choice.equals("down")) {
//                header.setRequest(true);
//                header.setGET(true);
//                isValidChoice = true;
//            } else if (choice.equals("no")) {
//                isValidChoice = true;
//                System.out.println("You have chosen 'no', at this moment we can't do a thing..."); //TODO
//            }
//        }
//        return header;
//    }
//
//    private byte[] requestSendFile() {
//        String file = "";
//        byte[] fileInBytes = new byte[0];
//        boolean validFile = false;
//        while (!validFile) {
//            file = readString("What file do you want to send?");
//            try {
//                fileInBytes = writeFileToByteArray(file);
//                validFile = true;
//            } catch (IOException e) {
//                System.out.println("This is not a valid file.");
//            }
//        }
//        return fileInBytes;
//    }
//
//    private String requestSendFileString() {
//        String file = "";
//        byte[] fileInBytes = new byte[0];
//        boolean validFile = false;
//        while (!validFile) {
//            file = readString("What file do you want to send?");
//            try {
//                buffer = writeFileToByteArray(file);
//                validFile = true;
//            } catch (IOException e) {
//                System.out.println("This is not a valid file.");
//            }
//        }
//        return file;
//    }
//
//    private String requestDownloadFileString() {
//        return readString("What file do you want to download?");
//    }
//
//    private byte[] joinHeaderAndData(byte[] a, byte[] b) {
////        byte[] allData = new byte[header.length + data.length];
////        int count = 0;
////        for (int i = 0; i < header.length; i++) {
////            allData[i] = header[i];
////            count++;
////        }
////        for (int i = 0; i < data.length; i++) {
////            allData[count + i] = data[i];
////        }
////        return allData;
//        ByteBuffer bb = ByteBuffer.allocate(a.length + b.length);
//        bb.put(a);
//        bb.put(b);
//        byte[] result = bb.array();
//        return result;
//
//    }
//
//    private byte[] createACK(long sendSeqNr, long sendAckNr) {
//        return (new ExtraHeader(false, true, false, false, sendAckNr, sendSeqNr)).getHeader();
//    }
//
//    private byte[] createACK(long sendSeqNr, long sendAckNr, int length) {
//        ExtraHeader header = new ExtraHeader(false, true, false, false, sendAckNr, sendSeqNr);
//        header.setLength(length);
//        return header.getHeader();
//    }
//
//    private byte[] createSYNACK(long sendSeqNr, long sendAckNr) {
//        return (new ExtraHeader(true, true, false, false, sendAckNr, sendSeqNr)).getHeader();
//    }
//
//    private byte[] createFIN(long sendSeqNr, long sendAckNr) {
//        return (new ExtraHeader(false, false, true, false, sendAckNr, sendSeqNr)).getHeader();
//    }
//
//    private byte[] createFINACK(long sendSeqNr, long sendAckNr) {
//        return (new ExtraHeader(false, true, true, false, sendAckNr, sendSeqNr)).getHeader();
//    }
//
//    private void sendToClient(byte[] sendData, InetAddress IP, int port) {
//        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IP, port);
//        System.out.print("Send header: "); printHeader(ExtraHeader.returnHeader(sendData));
//        System.out.println("     length: " + sendData.length);
//        try {
//            socket.send(sendPacket);
//        } catch (IOException e) {
//            e.printStackTrace();//TODO: not able to send packet
//        }
//    }
//
//    private boolean checkReceivedAckNr(ExtraHeader receivedHeader) {
//        if (receivedHeader.isAck()) {
//            long receivedAckNr = receivedHeader.getAckNr();
//            if (receivedAckNr != nextAckExpected) {
//                //TODO: what to do if the ack is not as expected
//                System.out.println("The received ackNr is " + receivedAckNr + ", but " + nextAckExpected + " was expected.");
//                return true;
//            }
//        }
//        return false;
//    }
//
//    private void printHeader(ExtraHeader header) {
//        System.out.println("Length: " + header.getLength() + ". SYN: " + header.isSyn() + "[" + header.getSeqNr() + "]. ACK: " + header.isAck() + "[" + header.getAckNr() + "].");
//    }
//
//    private void writeByteArrayToFile(byte[] byteArrayOfFile, String name) {
//        try {
//            BufferedImage image = ImageIO.read(new ByteArrayInputStream(byteArrayOfFile));
//            File outputfile = new File(name);
//            ImageIO.write(image, "jpg", outputfile);
//        } catch (IOException e) {
//            e.printStackTrace(); //TODO
//        }
//    }
//
//    private byte[] writeFileToByteArray(String filename) throws IOException {
//        byte[] fileInBytes;
//        filename = "src/" + filename;
//        Path path = Paths.get(filename);
////        try {
//        fileInBytes = Files.readAllBytes(path);
////        } catch (IOException e) {
////            e.printStackTrace();//TODO
////            fileInBytes = new byte[0];
////        }
//        return fileInBytes;
//    }
//
//    private static String readString(String prompt) {
//        System.out.print(prompt);
//        String msg = null;
//        try {
//            BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
//            msg = in.readLine();
//        } catch (IOException e) {
//            System.out.println("IOException at readString in client.");
//        }
//        return (msg == null) ? "" : msg;
//    }
//}

