package com.nedap.university;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

/**
 * Created by claudia.reuvers on 14/04/2017.
 */
public class Pi {

    private DatagramSocket broadcastSocket;

    Pi(int port) throws SocketException {
        broadcastSocket = new DatagramSocket(port);
    }

    public void init() {
        while (true) {//TODO: stop running
            byte buffer[] = new byte[1024];
            DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
            try {
                broadcastSocket.receive(receivedPacket);
            } catch (IOException e) {
                e.printStackTrace();//TODO: handle exception at socket.receive()
            }
            if (receivedPacket.getLength() > 9) {
                //Do something e.g. handleIncommingPacket(receivedPacket);
            }
        }
    }
}

//    private long nextAckExpected;
//    private boolean connectionSetUp = false;
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
////            long sendSeqNr = receivedHeader.getAckNr();
////            long sendAckNr = receivedHeader.getSeqNr() + 1;
////            nextAckExpected = sendSeqNr + 1;
//            byte[] header = createACK(sendSeqNr, sendAckNr);
//            sendingData = header;
//        } else if (!receivedHeader.isSyn() & receivedHeader.isAck() & !receivedHeader.isFin()) {//ACK (no SYN, no FIN)
//            System.out.println("I see an ACK packet of length " + receivedPacket.getLength()); //TODO: set adapt nextACKExpected & sendAckNr for data length
//            if (receivedHeader.isRequest()) {
//                String inhoud = getTextInData(receivedPacket);
//                if (receivedHeader.isSEND()) {
//                    System.out.println("Request to send: " + inhoud);
//                    byte[] header = createACK(sendSeqNr, sendAckNr);
//                    sendingData = header;
//                } else {//IS GET
//                    System.out.println("Request to GET: " + inhoud);
//                    byte[] header = createACK(sendSeqNr, sendAckNr);
//                    sendingData = header;
//                }
//            } else {
//                byte[] header = createACK(sendSeqNr, sendAckNr);
//                sendingData = header;
//                if (receivedHeader.isSEND()) {//nothing
//
//                } else {//show list
//
//                }
//
//            }
//
////            long sendSeqNr = receivedHeader.getAckNr();
////            long sendAckNr = receivedHeader.getSeqNr() + 1;
////            nextAckExpected = sendSeqNr + 1;
////            byte[] header = createACK(sendSeqNr, sendAckNr);
////            if (data != null) {
////                sendingData = joinHeaderAndData(header, data);
////            } else {
////                sendingData = header;
////            }
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
//    private byte[] joinHeaderAndData(byte[] header, byte[] data) {
//        byte[] allData = new byte[header.length + data.length];
//        int count = 0;
//        for (int i = 0; i < header.length; i++) {
//            allData[i] = header[i];
//            count++;
//        }
//        for (int i = 0; i < data.length; i++) {
//            allData[count + i] = data[i];
//        }
//        return allData;
//    }
//
//    private String getTextInData(DatagramPacket receivedPacket) {
//        ExtraHeader receivedHeader = ExtraHeader.returnHeader(receivedPacket.getData());
//        int length = receivedHeader.getLength();
//        byte[] data = new byte[length];
//        for (int i = 0; i < data.length; i++) {
//            data[i] = receivedPacket.getData()[i + receivedHeader.headerLength()];
//        }
//        return new String(data);
//    }
//
//    private byte[] createFINACK(long sendSeqNr, long sendAckNr) {
//        return (new ExtraHeader(false, true, true, false, sendAckNr, sendSeqNr)).getHeader();
//    }
//
//    private byte[] createFIN(long sendSeqNr, long sendAckNr) {
//        return (new ExtraHeader(false, false, true, false, sendAckNr, sendSeqNr)).getHeader();
//    }
//
//    private byte[] createACK(long sendSeqNr, long sendAckNr) {
//        return (new ExtraHeader(false, true, false, false, sendAckNr, sendSeqNr)).getHeader();
//    }
//
//    private void sendToClient(byte[] sendData, InetAddress IP, int port) {
//        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, IP, port);
//        System.out.print("Send header: "); printHeader(ExtraHeader.returnHeader(sendData));
//        try {
//            socket.send(sendPacket);
//        } catch (IOException e) {
//            e.printStackTrace();//TODO: not able to send packet
//        }
//    }
//
//    private byte[] createSYNACK(long sendSeqNr, long sendAckNr) {
//        return (new ExtraHeader(true, true, false, false, sendAckNr, sendSeqNr)).getHeader();
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
//    private byte[] writeFileToByteArray(String filename) {
//        byte[] fileInBytes;
//        filename = "src/" + filename;
//        Path path = Paths.get(filename);
//        try {
//            fileInBytes = Files.readAllBytes(path);
//        } catch (IOException e) {
//            e.printStackTrace();//TODO
//            fileInBytes = new byte[0];
//        }
//        return fileInBytes;
//    }
//}

