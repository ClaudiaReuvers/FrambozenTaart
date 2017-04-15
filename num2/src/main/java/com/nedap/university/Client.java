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
 *
 * @author claudia.reuvers
 */
public class Client extends Thread {

//    private InetAddress destinationIP;
//    private int destinationPort;
    private Sender sender;
    private Receiver receiver;
    private BufferedReader in;
    private boolean isConnected = true;
    private volatile boolean packetArrived; //s.t. a change in this parameter results in a reading of the packet
    private long nextAckExpected;

    /**
     * Creates a <code>Client</code> with a <code>Sender</code> and <code>Receiver</code>.
     * Creates a new <code>DatagramSocket</code> from which the <code>Sender</code> and <code>Receiver</code> send and
     * receive their data.
     * @param connectingIP <code>InetAddress</code> to which the data must be send
     * @param connectingPort port to which the data must be send
     * @throws SocketException if it is not possible to open a new <code>Socket</code> for communication
     */
    Client(InetAddress connectingIP, int connectingPort) throws SocketException {
//        this.destinationIP = connectingIP;
//        this.destinationPort = connectingPort;
        DatagramSocket sock = new DatagramSocket();
        System.out.println("Created new socket on port " + sock.getLocalPort() + "(" + sock.getLocalAddress() + ")");
        this.sender = new Sender(sock);
        sender.setDestPort(connectingPort);
        sender.setDestAddress(connectingIP);
        this.receiver = new Receiver(sock, this);
        receiver.start();
        this.in = new BufferedReader(new InputStreamReader(System.in));
    }

//    public void init() {
//        receiver.start();
//    }

    /**
     * Starts the <code>Client</code>, as long as the client is connected it gets a packet from the queue of the
     * <code>Receiver</code> and sends response.
     */
    @Override
    public void run() {
//        sendDNSRequest();
        while(isConnected) {
            if (packetArrived) {
                determineResponse(receiver.getPacketInQueue());
            }
        }
    }

    /**
     * Determines the appropriate response on the packet.
     * @param packetInQueue the <code>DatagramPacket</code> to which a response is determined
     */
    private void determineResponse(DatagramPacket packetInQueue) {
        ExtraHeader receivedHeader = ExtraHeader.returnHeader(packetInQueue.getData());
        if (receivedHeader.isDNSResponse()) {
            sender.setDestAddress(packetInQueue.getAddress());
            sender.setDestPort(packetInQueue.getPort());
            sendSYN();
        } else if (receivedHeader.isSyn() & !receivedHeader.isAck() & !receivedHeader.isFin()) {        //SYN
            respondToSYN(packetInQueue);
        } else if (receivedHeader.isSyn() & receivedHeader.isAck() & !receivedHeader.isFin()) {  //SYN ACK
            respondToSYNACK(packetInQueue);
        } else if (!receivedHeader.isSyn() & receivedHeader.isAck() & !receivedHeader.isFin()) { //    ACK
            respondToACK(packetInQueue);
        } else if (!receivedHeader.isSyn() & !receivedHeader.isAck() & receivedHeader.isFin()) { //FIN
            respondToFIN(packetInQueue);
        } else if (!receivedHeader.isSyn() & receivedHeader.isAck() & receivedHeader.isFin()) {  //FIN ACK
            respondToFINACK(packetInQueue);
        } else {
            System.out.println("Unknown flags"); //TODO
        }
    }

    private void respondToSYN(DatagramPacket packetInQueue) {
        System.out.println("SYN");
        sendSYNACK(packetInQueue);
    }

    private void respondToSYNACK(DatagramPacket packetInQueue) {
        System.out.println("SYN ACK");
        sendACK(packetInQueue);
        //TODO: response to SYN ACK
    }

    private void respondToACK(DatagramPacket packetInQueue) {
        System.out.println("ACK");
        //TODO: response to ACK
    }

    private void respondToFIN(DatagramPacket packetInQueue) {
        System.out.println("FIN");
        //TODO: response to FIN
    }

    private void respondToFINACK(DatagramPacket packetInQueue) {
        System.out.println("FIN ACK");
        //TODO: response to FIN ACK
    }

//    public void init() {
//        while(!isConnected) {
//            sendDNSRequest();
//        }

//        sendSYN();
//        while (true) {//TODO: stop running
//            byte buffer[] = new byte[1024];
//            DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
//            try {
//                socket.receive(receivedPacket);
//            } catch (IOException e) {
//                e.printStackTrace();//TODO: handle exception at socket.receive
//            }
//            if (receivedPacket.getData().length > 9) {
//                //Do something: e.g. handleIncommingPacket(receivedPacket)
//            }
//        }
//    }

    /**
     * Sends a SYN packet.
     * The sequence number of this packet is set to a random number between 0 and 2^32 - 1.
     */
    private void sendSYN() { //TODO: look if still valid
        int seqNr = (new Random()).nextInt(2^32);
        ExtraHeader header = new ExtraHeader(true, false, false, false, 0, seqNr);
        System.out.print("Send header: " + header);
        nextAckExpected = seqNr + 1;
        try {
            sender.send(header, new byte[0]);
        } catch (IOException e) {
            e.printStackTrace();//TODO
        }
    }

    /**
     * Sends a SYN ACK packet.
     * The sequence number of this packet is set to a random number between 0 and 2^32 - 1. The acknr is updated with the
     * received seqNr + the length of the data + 1. And the nextExpectedAckNr is also updated.
     * @param receivedPacket the packet to which the SYN ACK must respond
     */
    private void sendSYNACK(DatagramPacket receivedPacket) {
        int seqNr = (new Random()).nextInt(2^32);
        ExtraHeader receivedHeader = ExtraHeader.returnHeader(receivedPacket.getData());
        long ackNr = receivedHeader.getSeqNr() + receivedHeader.getLengthData() + 1;
        nextAckExpected = seqNr + receivedHeader.getLengthData() + 1;
        ExtraHeader header = new ExtraHeader(true, true, false, false, ackNr, seqNr);
        try {
            getSender().send(header, new byte[0]);
        } catch (IOException e) {
            e.printStackTrace(); //TODO
        }
    }

    /**
     * Sends an ACK packet.
     * The sequence number of the packet is set to the ackNr of the received packet. The ackNr and nextExpectedAckNr
     * are updated.
     * @param receivedPacket the packet to which the ACK packet must respond
     */
    private void sendACK(DatagramPacket receivedPacket) {
        ExtraHeader receivedHeader = ExtraHeader.returnHeader(receivedPacket.getData());
        long seqNr = receivedHeader.getAckNr();
        long ackNr = receivedHeader.getSeqNr() + receivedHeader.getLengthData() + 1;
        nextAckExpected = seqNr + receivedHeader.getLengthData() + 1;
        ExtraHeader sendingHeader = new ExtraHeader(false, true, false, false, ackNr, seqNr);
        try {
            getSender().send(sendingHeader, new byte[0]);
        } catch (IOException e) {
            e.printStackTrace(); //TODO
        }
    }

    /**
     * Sends a DNSRequest.
     */
    void sendDNSRequest() {
        ExtraHeader header = new ExtraHeader();
        header.setDNSRequest();
        header.setNoRequest();
//        byte[] headerBytes = header.getHeader();
//        DatagramPacket packet = new DatagramPacket(headerBytes, headerBytes.length, destinationIP, destinationPort);
        try {
            sender.send(header, new byte[0]);
        } catch (IOException e) {
            e.printStackTrace();//TODO
        }
    }

    /**
     * Sets the packetArrived-boolean on the specified value.
     * @param arrived if <code>true</code> the client is notified and a response is generated, else nothing happens
     */
    void packetArrived(boolean arrived) {
        this.packetArrived = arrived;
    }

    /**
     * Returns the <code>Sender</code> of this <code>Client</code>.
     * @return <code>Sender</code> of the <code>Client</code>
     */
    Sender getSender() {
        return this.sender;
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

