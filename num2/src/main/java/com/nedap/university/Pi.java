package com.nedap.university;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.*;

/**
 * Created by claudia.reuvers on 14/04/2017.
 *
 * @author claudia.reuvers
 */
public class Pi extends Thread{

    private DatagramSocket broadcastSocket;
    private InetAddress ownIP;

    /**
     * Creates a new <code>Pi</code> with a specified broadcast-port.
     * @param port port to which the <code>Pi</code> listens for connections
     * @throws SocketException if it is not possible to open a socket on the port
     */
    Pi(int port) throws SocketException, UnknownHostException {
        broadcastSocket = new DatagramSocket(port);
        ownIP = InetAddress.getByName("192.168.40.6");
        System.out.println("Local IP: " + ownIP);
    }

    /**
     * Listens for DNSRequests.
     * If a DNSRequest comes in, a new <code>Client</code> is created with a <code>Sender</code> and
     * <code>Receiver</code> on a new <code>Socket</code>.
     */
    @Override
    public void run() {
        while (true) {//TODO: stop running
            byte buffer[] = new byte[1024];
            DatagramPacket receivedPacket = new DatagramPacket(buffer, buffer.length);
            try {
                broadcastSocket.receive(receivedPacket);
            } catch (IOException e) {
                e.printStackTrace();//TODO: handle exception at socket.receive()
            }
            ExtraHeader receivedPacketHeader = ExtraHeader.returnHeader(receivedPacket.getData());
            System.out.println("Broadcast received: " + receivedPacketHeader);
            if (receivedPacketHeader.isDNSRequest()) {
                try {
                    respondToDNSRequest(receivedPacket);
                } catch (SocketException e) {
                    System.out.println("Unable to create a socket for " + receivedPacket.getAddress() + " on port " + receivedPacket.getPort() + ".");
//                    System.out.println("Unable to send DNS Response to " + receivedPacket.getAddress() + " on port " + receivedPacket.getPort());
                }
            } else {
                System.out.println("Received packet is not recognized.");
            }
        }
    }

    /**
     * Creates a new <code>Client</code> with <code>Sender</code> and <code>Receiver</code> for this DNSRequest
     * @param receivedPacket packet received on the broadcast-port
     * @throws SocketException if it is not possible to create a socket for the <code>Client</code>
     */
    private void respondToDNSRequest(DatagramPacket receivedPacket) throws SocketException {
        ExtraHeader header = new ExtraHeader();
        header.setDNSResponse();
        Client client = new Client(receivedPacket.getAddress(), receivedPacket.getPort(), 15423); //TODO: remove after debugging, otherwise it is not possible to have multiple clients
        DatagramSocket receivingSocket = client.getReceiver().getReceivingSocket();
        System.out.println("Opened socket on " + receivingSocket.getLocalPort());
        String socketString = Integer.toString(receivingSocket.getLocalPort());
        String IPadressString = ownIP.toString();
        String response = socketString + " " + IPadressString;
        byte[] DNSresponse = response.getBytes();
        header.setLength(DNSresponse.length);
        byte[] totalPacket = joinByteArrays(header.getHeader(), DNSresponse);
        DatagramPacket packet = new DatagramPacket(totalPacket, totalPacket.length, receivedPacket.getAddress(), receivedPacket.getPort());
        System.out.println("Send: " + header);
        try {
            client.start();
//            client.getSender().send(header, new byte[0]);
            broadcastSocket.send(packet);

        } catch (IOException e) {
            e.printStackTrace(); //TODO
        }


//        DatagramSocket sock = new DatagramSocket();
//        Sender sender = new Sender(sock);
//        sender.setDestPort(receivedPacket.getPort());
//        sender.setDestAddress(receivedPacket.getAddress());
//        Receiver receiver = new Receiver(sock, sender); //TODO: start sender & receiver
//        try {
//            sender.send(header, new byte[0]);
//        } catch (IOException e) {
//            e.printStackTrace();//TODO
//        }
    }

//    private byte[] joinByteArrays(byte[] array1, byte[] array2) {
//        byte[] data = new byte[array1.length + array2.length];
//        for (int i = 0; i < array1.length; i++) {
//            data[i] = array1[i];
//        }
//        for (int i = 0; i < array2.length; i++) {
//            data[i + array1.length - 1] = array2[i];
//        }
//        return data;
//    }

    byte[] joinByteArrays(byte[] array1, byte[] array2) {
//        byte[] data = new byte[array1.length + array2.length];
//        for (int i = 0; i < array1.length; i++) {
//            data[i] = array1[i];
//        }
//        for (int i = 0; i < array2.length; i++) {
//            data[i + array1.length - 1] = array2[i];
//        }
//        return data;
//        byte[] combinedData = new byte[array1.length + array2.length];
//        System.arraycopy(array1,0,combinedData,0         ,array1.length);
//        System.arraycopy(array2,0,combinedData,array1.length+1,array2.length);
//        return combinedData;


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(array1);
            outputStream.write(array2);
        } catch (IOException e){
            System.out.println("Could not write this!");
        }
        return outputStream.toByteArray( );

    }
}