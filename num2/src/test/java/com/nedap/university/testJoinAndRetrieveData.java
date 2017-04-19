package com.nedap.university;

import org.junit.Before;
import org.junit.Test;

import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import static com.nedap.university.Utils.Utils.getDataOfPacket;
import static com.nedap.university.Utils.Utils.joinByteArrays;
import static org.junit.Assert.assertEquals;

/**
 * Created by claudia.reuvers on 18/04/2017.
 */
public class testJoinAndRetrieveData {

    private Client client;

    @Before
    public void setUp() {
        try {
            InetAddress address = InetAddress.getByName("198.168.40.6");
            client = new Client(address, 12345);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void test() {
        ExtraHeader header = new ExtraHeader();
        byte[] data = new byte[9];
        data[0] = (byte) 4;
        data[8] = (byte) 5;
        header.setLength(data.length);
        byte[] joinedData = joinByteArrays(header.getHeader(), data);
        assertEquals(ExtraHeader.headerLength() + 9, joinedData.length);
        byte[] sameData = getDataOfPacket(joinedData);
        assertEquals(9, sameData.length);
        assertEquals(data[0], sameData[0]);
        assertEquals(data[8], sameData[8]);
    }
}
