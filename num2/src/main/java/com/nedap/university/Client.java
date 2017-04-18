package com.nedap.university;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Arrays;
import java.util.Random;

/**
 * Created by claudia.reuvers on 14/04/2017.
 *
 * @author claudia.reuvers
 */
public class Client extends Thread {

    private InetAddress broadcastIP;
    private int broadcastPort;
    private Sender sender;
    private Receiver receiver;
    private BufferedReader in;
    private boolean isConnected = true;
    private volatile boolean packetArrived; //s.t. a change in this parameter results in a reading of the packet
    private long nextAckExpected;
    private boolean sending = false;
    private boolean sendFIN;
    private boolean receiving = false;
    private TransferFile sendingFile;
    private TransferFile receivingFile;

    /**
     * Creates a <code>Client</code> with a <code>Sender</code> and <code>Receiver</code>.
     * Creates a new <code>DatagramSocket</code> from which the <code>Sender</code> and <code>Receiver</code> send and
     * receive their data.
     * @param connectingIP <code>InetAddress</code> to which the data must be send
     * @param connectingPort port to which the data must be send
     * @throws SocketException if it is not possible to open a new <code>Socket</code> for communication
     */
    Client(InetAddress connectingIP, int connectingPort) throws SocketException {
        this.broadcastIP = connectingIP;
        this.broadcastPort = connectingPort;
        DatagramSocket sock = new DatagramSocket();
        this.sender = new Sender(sock);
//        sender.setDestPort(connectingPort);
//        sender.setDestAddress(connectingIP);
        this.receiver = new Receiver(sock, this);
        receiver.start();
        this.in = new BufferedReader(new InputStreamReader(System.in));
    }

    Client(InetAddress connectingIP, int connectingPort, int port) throws SocketException {
        this.broadcastIP = connectingIP;
        this.broadcastPort = connectingPort;
        DatagramSocket sock = new DatagramSocket(port);
        this.sender = new Sender(sock);
//        sender.setDestPort(connectingPort);
//        sender.setDestAddress(connectingIP);
        this.receiver = new Receiver(sock, this);
        receiver.start();
        this.in = new BufferedReader(new InputStreamReader(System.in));
    } //TODO: remove if testing with Wireshark is finished

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
            int dataLength = receivedHeader.getLengthData();
            System.out.println("DataLength: " + dataLength);
            byte[] data = getDataOfPacket(packetInQueue.getData(), dataLength);
            System.out.println("Data: " + new String(data));
            int port = Integer.parseInt(new String(data).split(" ")[0]);
            sender.setDestAddress(packetInQueue.getAddress());
            sender.setDestPort(port);
            sendSYN();
        } else if (receivedHeader.isSyn() & !receivedHeader.isAck() & !receivedHeader.isFin()) { //SYN
            sender.setDestAddress(packetInQueue.getAddress());
            sender.setDestPort(packetInQueue.getPort());
            respondToSYN(packetInQueue);
        } else if (receivedHeader.isSyn() & receivedHeader.isAck() & !receivedHeader.isFin()) {  //SYN ACK
            respondToSYNACK(packetInQueue);
        } else if (!receivedHeader.isSyn() & receivedHeader.isAck() & !receivedHeader.isFin()) { //    ACK
            if (receivedHeader.isUploadRequest()) {
                respondToUploadRequest(packetInQueue);
            } else if (receivedHeader.isDownloadRequest()) {
                respondToDownLoadRequest();
            } else if (sending) {
                sendData(packetInQueue);
            } else if (receiving) {
                receiveData(packetInQueue);
            } else if (!sendFIN) {
                sendFIN(packetInQueue);
            }
//            respondToACK(packetInQueue);
        } else if (!receivedHeader.isSyn() & !receivedHeader.isAck() & receivedHeader.isFin()) { //FIN
            respondToFIN(packetInQueue);
        } else if (!receivedHeader.isSyn() & receivedHeader.isAck() & receivedHeader.isFin()) {  //FIN ACK
            respondToFINACK(packetInQueue);
        } else {
            System.out.println("Unknown flags"); //TODO
        }
    }

    private void receiveData(DatagramPacket receivedPacket) {
        ExtraHeader receivedHeader = ExtraHeader.returnHeader(receivedPacket.getData());
        byte[] data = getDataOfPacket(receivedPacket.getData(), receivedHeader.getLengthData());
        System.out.println("Add data at location: " + receivingFile.getLocation() + "(" + data.length + ")/" + receivingFile.getBufferSize());
        try {
            receivingFile.appendToBuffer(data, receivedHeader.getLengthData());
        } catch (TransferFile.EndOfFileException e) {
            e.printStackTrace();
            receiving = false;
        }
        sendACK(receivedPacket);
    }

    private void sendData(DatagramPacket receivedPacket) {
        ExtraHeader receivedHeader = ExtraHeader.returnHeader(receivedPacket.getData());
        long seqNr = receivedHeader.getAckNr();
        long ackNr = receivedHeader.getSeqNr() + receivedHeader.getLengthData() + 1;
        nextAckExpected = seqNr + receivedHeader.getLengthData() + 1;
        ExtraHeader sendingHeader = new ExtraHeader(false, true, false, false, ackNr, seqNr);
        byte[] data = sendingFile.readFromBuffer(1024 - ExtraHeader.headerLength());
        System.out.println("Send " + sendingFile.getLocation() + "/" + sendingFile.getBufferSize());
        if (data.length < (1024 - ExtraHeader.headerLength())) { //check if the buffer is smaller than expected, so you are at the end of the file
            sending = false;
            System.out.println("Whole file send!");
            sendFIN = false;
        }
        try {
            getSender().send(sendingHeader, data);
        } catch (IOException e) {
            e.printStackTrace(); //TODO
        }
    }

    private void sendFIN(DatagramPacket receivedPacket) {
        ExtraHeader receivedHeader = ExtraHeader.returnHeader(receivedPacket.getData());
        long seqNr = receivedHeader.getAckNr();
        long ackNr = receivedHeader.getSeqNr() + receivedHeader.getLengthData() + 1;
        nextAckExpected = seqNr + receivedHeader.getLengthData() + 1;
        ExtraHeader sendingHeader = new ExtraHeader(false, false, true, false, ackNr, seqNr);
        try {
            getSender().send(sendingHeader, new byte[0]);
        } catch (IOException e) {
            e.printStackTrace(); //TODO
        }
    }

    private void respondToDownLoadRequest() {
        //TODO
    }

    private void respondToUploadRequest(DatagramPacket receivedPacket) {
        receiving = true;
        ExtraHeader receivedHeader = ExtraHeader.returnHeader(receivedPacket.getData());
        byte[] data = getDataOfPacket(receivedPacket.getData(), receivedHeader.getLengthData());
        System.out.println("Received txt: " + new String(data));
        String[] fileInfo = (new String(data).split(" "));
        String filename = fileInfo[1];
        int size = Integer.parseInt(fileInfo[0]);
        receivingFile = new TransferFile(filename, size);
        System.out.println("Requested for " + size + ", buffer made of length: " + receivingFile.getBufferSize());
        sendACK(receivedPacket);
    }

    byte[] getDataOfPacket(byte[] dataAndHeader, int dataLength) {
        return Arrays.copyOfRange(dataAndHeader, ExtraHeader.headerLength(), dataAndHeader.length);//ExtraHeader.headerLength() + dataLength);
    }

    private void respondToSYN(DatagramPacket packetInQueue) {
        System.out.println("SYN");
        sendSYNACK(packetInQueue);
    }

    private void respondToSYNACK(DatagramPacket packetInQueue) {
        System.out.println("SYN ACK");
        String response = "";
        while (!response.equals("up") & !response.equals("down") & !response.equals("no")) {
            response = readString("Do you want to up/download a packet to/from the Pi? (up/down/no)");
        }
        if (response.equals("up")) {
            sendUploadRequest(packetInQueue);
        } else if (response.equals("down")) {
            sendDownloadRequest(packetInQueue);
        } else {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();//TODO
            }
            respondToSYNACK(packetInQueue);
        }
//        sendACK(packetInQueue);
        //TODO: response to SYN ACK
    }

    private void sendDownloadRequest(DatagramPacket packetInQueue) {
        //TODO
    }

    private void sendUploadRequest(DatagramPacket receivedPacket) {
        ExtraHeader receivedHeader = ExtraHeader.returnHeader(receivedPacket.getData());
        long seqNr = receivedHeader.getAckNr();
        long ackNr = receivedHeader.getSeqNr() + receivedHeader.getLengthData() + 1;
        nextAckExpected = seqNr + receivedHeader.getLengthData() + 1;
        ExtraHeader sendingHeader = new ExtraHeader(false, true, false, false, ackNr, seqNr);
        sendingHeader.setUploadRequest();
        String filename = "";
        byte[] data;
        while (filename.equals("")) {
            filename = readString("Which file do you want to upload?");
            try {
                sendingFile = new TransferFile(filename);
                String sendData = sendingFile.getBufferSize() + " " + filename;
                data = sendData.getBytes();
                String[] data2 = (new String(data)).split(" ");
                int size = Integer.parseInt(data2[0]);
                System.out.println("Request to upload:" + data2[1] + "_" + size);
                sendingHeader.setLength(data.length);
                getSender().send(sendingHeader, data);
                sending = true;
            } catch (IOException e) {
                System.out.println("Could not find the file. The file must be in the Files/ folder.");
                filename = "";
            }
        }
//        sendingHeader.setLength(data.length);
//        System.out.println("Data length: " + data.length);
//        try {
//            getSender().send(sendingHeader, data);
//            sending = true;
//        } catch (IOException e) {
//            e.printStackTrace(); //TODO
//        }
    }

    private String readString(String prompt) {
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

    private void respondToACK(DatagramPacket packetInQueue) {
        System.out.println("ACK");
        sendACK(packetInQueue);
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
            System.out.println("Acknr: " + ackNr + ", secnr: " + seqNr);
            System.out.println(receiver.getReceivingSocket().getInetAddress() + ", port: " + receiver.getReceivingSocket().getPort());
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
            sender.send(header, new byte[0], broadcastIP, broadcastPort);
//            sender.send(header, new byte[0]);
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

    /**
     * Returns the <code>Receiver</code> of this <code>Client</code>.
     * @return <code>Receiver</code> of the <code>Client</code>
     */
    Receiver getReceiver() {
        return this.receiver;
    }
}