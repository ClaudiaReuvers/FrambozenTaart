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
    private boolean FINreceived = false;

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
            respondToDNSResponse(packetInQueue);
        } else if (receivedHeader.isSyn() & !receivedHeader.isAck() & !receivedHeader.isFin()) { //SYN
            sender.setDestAddress(packetInQueue.getAddress());
            sender.setDestPort(packetInQueue.getPort());
            sendSYNACK(packetInQueue);
        } else if (receivedHeader.isSyn() & receivedHeader.isAck() & !receivedHeader.isFin()) {  //SYN ACK
            respondToSYNACK(packetInQueue);
        } else if (!receivedHeader.isSyn() & receivedHeader.isAck() & !receivedHeader.isFin()) { //    ACK
            if (receivedHeader.isUploadRequest()) {
                respondToUploadRequest(packetInQueue);
            } else if (receivedHeader.isDownloadRequest()) {
                respondToDownLoadRequest(packetInQueue);
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
            respondToFINACK();
        } else {
            System.out.println("Unknown flags, no response is send.");
        }
    }

    /**
     * Respond to a DNSresponse of the server.
     * The destination address and port are set to the IPaddress of the Pi and the port given by the Pi.
     * @param packetInQueue <code>DatagramPacket</code> to which the DNSresponse is a reply
     */
    private void respondToDNSResponse(DatagramPacket packetInQueue) {
        ExtraHeader receivedHeader = ExtraHeader.returnHeader(packetInQueue.getData());
        int dataLength = receivedHeader.getLengthData();
        System.out.println("DataLength: " + dataLength);
        byte[] data = getDataOfPacket(packetInQueue.getData());
        System.out.println("Data: " + new String(data));
        int port = Integer.parseInt(new String(data).split(" ")[0]);
        sender.setDestAddress(packetInQueue.getAddress());
        sender.setDestPort(port);
        sendSYN();
    }

    /**
     * Respond to a SYN ACK packet.
     * Terminal input is read to determine if a upload or download will take place. If upload is chosen, a upload
     * request is send to the Pi. If download is chosen, a request for all available files on the Pi is send and a list
     * of all available files is displayed to choose from.
     * @param packetInQueue the <code>DatagramPacket</code> to which the response is send
     */
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
    }

    private void respondToFIN(DatagramPacket packetInQueue) {
        FINreceived = true;
        sendFINACK(packetInQueue);
    }

    /**
     * The <code>DatagramSocket</code> of the <code>Client</code> will be closed.
     */
    private void respondToFINACK() {
        shutdown();
    }

    private void respondToDownLoadRequest(DatagramPacket receivedPacket) {
        byte[] data = getDataOfPacket(receivedPacket.getData());
        String filename = new String(data);
        System.out.println("Request for a download of " + filename);
        sendUploadRequest(receivedPacket, filename);
    }

    /**
     * Sends an uploadrequest to the connected IPAddress and port.
     * The data of the packet (a filename and -size) is used to create a <code>TransferFile</code> with the appropriate
     * name and size to which data will be appended. And ACK is send as a confirmation of the retrieved uploadrequest.
     * @param receivedPacket The <code>DatagramPacket</code> to which the uploadrequest is a response.
     */
    private void respondToUploadRequest(DatagramPacket receivedPacket) {
        receiving = true;
        ExtraHeader receivedHeader = ExtraHeader.returnHeader(receivedPacket.getData());
        byte[] data = getDataOfPacket(receivedPacket.getData());
        System.out.println("Received txt: " + new String(data));
        String[] fileInfo = (new String(data).split(" "));
        String filename = fileInfo[1];
        System.out.println("Filename_" + filename + "_");
        int size = Integer.parseInt(fileInfo[0]);
        receivingFile = new TransferFile(filename, size);
        System.out.println("Requested for " + size + ", buffer made of length: " + receivingFile.getBufferSize());
        sendACK(receivedPacket);
    }

    /**
     * Adds the retrieved data to the buffer of the <code>TransferFile</code>.
     * Data is read from the <code>DatagramPacket</code> and added to the buffer of the <code>TransferFile</code>. If
     * the final data is added, the file is saved and this client is ready to perform new down-/uploads.
     * @param receivedPacket The <code>DatagramPacket</code> from which the data will be retreived
     */
    private void receiveData(DatagramPacket receivedPacket) {
        ExtraHeader receivedHeader = ExtraHeader.returnHeader(receivedPacket.getData());
        byte[] data = getDataOfPacket(receivedPacket.getData());
        System.out.println("Add data at location: " + receivingFile.getLocation() + "(" + data.length + ")/" + receivingFile.getBufferSize());
        try {
            receivingFile.appendToBuffer(data, receivedHeader.getLengthData());
        } catch (TransferFile.EndOfFileException e) {
            receivingFile.saveReceivedFile();
            receiving = false;
        }
        sendACK(receivedPacket);
    }

    /**
     * Sends the next part of the file to the receiver.
     * Sends the next datapart of the to transfer file to the receiver. If the file has reach its end, a FIN is send.
     * @param receivedPacket the <code>DatagramPacket</code> to which the response is
     */
    private void sendData(DatagramPacket receivedPacket) {
        ExtraHeader receivedHeader = ExtraHeader.returnHeader(receivedPacket.getData());
        long seqNr = receivedHeader.getAckNr();
        long ackNr = receivedHeader.getSeqNr() + receivedHeader.getLengthData() + 1;
        nextAckExpected = seqNr + receivedHeader.getLengthData() + 1;
        ExtraHeader sendingHeader = new ExtraHeader(false, true, false, false, ackNr, seqNr);
        byte[] data = sendingFile.readFromBuffer(10240 - ExtraHeader.headerLength());
        System.out.println("Send " + sendingFile.getLocation() + "/" + sendingFile.getBufferSize());
        if (data.length < (10240 - ExtraHeader.headerLength())) { //check if the buffer is smaller than expected, so you are at the end of the file
            sending = false;
            System.out.println("Whole file send!");
            sendFIN = false;
        }
        getSender().send(sendingHeader, data);
    }

//    private void respondToACK(DatagramPacket packetInQueue) {
//        System.out.println("ACK");
//        sendACK(packetInQueue);
//        //TODO: response to ACK
//    }

    /**
     * Sends a DNSRequest.
     */
    void sendDNSRequest() {
        ExtraHeader header = new ExtraHeader();
        header.setDNSRequest();
        header.setNoRequest();
        sender.send(header, new byte[0], broadcastIP, broadcastPort);
    }

    /**
     * Sends a SYN packet.
     * The sequence number of this packet is set to a random number between 0 and 2^32 - 1.
     */
    private void sendSYN() { //TODO: look if still valid
        int seqNr = (new Random()).nextInt(2^32);
        ExtraHeader header = new ExtraHeader(true, false, false, false, 0, seqNr);
        nextAckExpected = seqNr + 1;
        sender.send(header, new byte[0]);
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
        System.out.println("Acknr: " + ackNr + ", secnr: " + seqNr);
        System.out.println(receiver.getReceivingSocket().getInetAddress() + ", port: " + receiver.getReceivingSocket().getPort());
        getSender().send(header, new byte[0]);
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
        getSender().send(sendingHeader, new byte[0]);
    }

    private void sendDownloadRequest(DatagramPacket receivedPacket) {
        ExtraHeader receivedHeader = ExtraHeader.returnHeader(receivedPacket.getData());
        long seqNr = receivedHeader.getAckNr();
        long ackNr = receivedHeader.getSeqNr() + receivedHeader.getLengthData() + 1;
        nextAckExpected = seqNr + receivedHeader.getLengthData() + 1;
        ExtraHeader sendingHeader = new ExtraHeader(false, true, false, false, ackNr, seqNr);
        sendingHeader.setDownloadRequest();
        String filename = "TryFile.jpg";
        byte[] data = filename.getBytes();
//        boolean validFilename = false;
//        while (!validFilename)
//        try {
            receivingFile = new TransferFile(filename);
            System.out.println("Request to download: " + filename);
            sender.send(sendingHeader, data);
            receiving = true;
//            validFilename = true;
//        } catch (IOException e) {
////            e.printStackTrace();//TODO
//            filename = readString("Chose another filename");
//        }
    }

    /**
     * Sends a upload request to the source of the received packet.
     * Sends a upload request to the source of the received packet with as data the filesize and -name. If you want to
     * send an file that is not in your folder, a new file has to be chosen until a valid file is chosen.
     * @param receivedPacket the <code>DatagramPacket</code> to which the uploadrequest is a reply
     */
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
                sendingFile.createPath();
                String sendData = sendingFile.getBufferSize() + " " + filename;
                data = sendData.getBytes();
                String[] data2 = (new String(data)).split(" ");
                int size = Integer.parseInt(data2[0]);
                System.out.println("Request to upload:" + data2[1] + " of size " + size);
                sendingHeader.setLength(data.length);
                getSender().send(sendingHeader, data);
                sending = true;
            } catch (IOException e) {
                System.out.println("Could not find the file. The file must be in the Files/ folder.");
                filename = "";
            }
        }
    }

    private void sendUploadRequest(DatagramPacket receivedPacket, String filename) {
//        filename = "/home/pi/Files/TryFile2.jpg";
//        filename = "Files/TryFile.jpg";
        ExtraHeader receivedHeader = ExtraHeader.returnHeader(receivedPacket.getData());
        long seqNr = receivedHeader.getAckNr();
        long ackNr = receivedHeader.getSeqNr() + receivedHeader.getLengthData() + 1;
        nextAckExpected = seqNr + receivedHeader.getLengthData() + 1;
        ExtraHeader sendingHeader = new ExtraHeader(false, true, false, false, ackNr, seqNr);
        sendingHeader.setUploadRequest();
        byte[] data;
        boolean validFilename = false;
        while (!validFilename) {
//        while (filename.equals("")) {
//            filename = readString("Which file do you want to upload?");
            try {
                sendingFile = new TransferFile(filename);
                sendingFile.createPath();
                String sendData = sendingFile.getBufferSize() + " " + filename;
                data = sendData.getBytes();
                String[] data2 = (new String(data)).split(" ");
                int size = Integer.parseInt(data2[0]);
                System.out.println("Request to upload:" + data2[1] + "_" + size);
                sendingHeader.setLength(data.length);
                getSender().send(sendingHeader, data);
                sending = true;
                validFilename = true;
            } catch (IOException e) {
                System.out.println("Could not find the file. The file must be in the Files/ folder.");
                filename = readString("Try another filename");
            }
        }
//        }
    }

    /**
     * Sends a FIN packet
     * @param receivedPacket the <code>DatagramPacket</code> to which the FIN is a reply
     */
    private void sendFIN(DatagramPacket receivedPacket) {
        ExtraHeader receivedHeader = ExtraHeader.returnHeader(receivedPacket.getData());
        long seqNr = receivedHeader.getAckNr();
        long ackNr = receivedHeader.getSeqNr() + receivedHeader.getLengthData() + 1;
        nextAckExpected = seqNr + receivedHeader.getLengthData() + 1;
        ExtraHeader sendingHeader = new ExtraHeader(false, false, true, false, ackNr, seqNr);
        getSender().send(sendingHeader, new byte[0]);
    }

    /**
     * Sends a FIN ACK.
     * Sends a FIN ACK and afterwards closes the sending and receiving socket after 2 seconds.
     * @param receivedPacket the <code>DatagramPacket</code> to which the FIN ACK is a reply
     */
    private void sendFINACK(DatagramPacket receivedPacket) {
        ExtraHeader receivedHeader = ExtraHeader.returnHeader(receivedPacket.getData());
        long seqNr = receivedHeader.getAckNr();
        long ackNr = receivedHeader.getSeqNr() + receivedHeader.getLengthData() + 1;
        nextAckExpected = seqNr + receivedHeader.getLengthData() + 1;
        ExtraHeader sendingHeader = new ExtraHeader(false, true, true, false, ackNr, seqNr);
        getSender().send(sendingHeader, new byte[0]);
        shutdown();
    }

    /**
     * Sets the packetArrived-boolean on the specified value.
     * @param arrived if <code>true</code> the client is notified and a response is generated, else nothing happens
     */
    void packetArrived(boolean arrived) {
        this.packetArrived = arrived;
    }

    /**
     * Returns the data of the complete packet (header and data).
     * @param dataAndHeader a byte-array of the header and data
     * @return the data of the complete packet
     */
    byte[] getDataOfPacket(byte[] dataAndHeader) {
        ExtraHeader header = ExtraHeader.returnHeader(dataAndHeader);
        int from = ExtraHeader.headerLength();
        int to = from + header.getLengthData();
        return Arrays.copyOfRange(dataAndHeader, from, to);//dataAndHeader.length);
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

    public boolean getFINreceived() {
        return this.FINreceived;
    }

    /**
     * Reads terminal input.
     * @param prompt the shown message to which the terminal input is a response
     * @return terminal input
     */
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

    /**
     * Closes the socket after 2 seconds.
     */
    private void shutdown() {
        try {
            Thread.sleep(2000);//TODO: chose other time-out time
        } catch (InterruptedException e) {
        }
        System.out.println("Shutting down");
        receiver.getReceivingSocket().close();
        System.exit(0);
    }
}