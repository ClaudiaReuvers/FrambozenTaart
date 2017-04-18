package com.nedap.university;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Created by claudia.reuvers on 14/04/2017.
 *
 * @author claudia.reuvers
 */
class Sender {

//    private int sourcePort;
    private int destPort;
    private DatagramSocket socket;
    private InetAddress destAddress;
//    private boolean isConnected;

    /**
     * Creates a Sender with a socket connected.
     * @param socket socket from which data is sended
     */
    Sender(DatagramSocket socket) {
        this.socket = socket;
    }

    /**
     * Sends the data with the given header.
     * Creates one data packet starting with the header and followed by the header. The datalength in the header is set to the length of <code>data</code>.
     * @param header header of the packet
     * @param data data of the packet
     * @throws IOException if the <code>Sender</code> is not able to send the packet to the destinaation
     */
    void send(ExtraHeader header, byte[] data) throws IOException {
        header.setLength(data.length);
        byte[] sendData = joinByteArrays(header.getHeader(), data);
        System.out.println("Send: " + header);
        DatagramPacket packet = new DatagramPacket(sendData, sendData.length, destAddress, destPort);
        socket.send(packet);
    }

    /**
     * Sets the port to which the packets must be send.
     * @param destPort port to which the packets must be send
     */
    void setDestPort(int destPort) {
        this.destPort = destPort;
    }

    /**
     * Sets the InetAddress to which the packets must be send.
     * @param destAddress InetAddress to which the packets must be send
     */
    void setDestAddress(InetAddress destAddress) {
        this.destAddress = destAddress;
    }

    public void send(ExtraHeader header, byte[] data, InetAddress broadcastIP, int broadcastPort) throws IOException {
        header.setLength(data.length);
        byte[] sendData = header.getHeader();//joinByteArrays(header.getHeader(), data);
        DatagramPacket packet = new DatagramPacket(sendData, sendData.length, broadcastIP, broadcastPort);
        System.out.println("Send: " + header);
        socket.send(packet);
    }

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
