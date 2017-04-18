package com.nedap.university;

import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by claudia.reuvers on 18/04/2017.
 */
public class testTransferFile {

    private String filename;
    private String filename2;
    private TransferFile receiving;
    private TransferFile sending;

    @Before
    public void setUp() {
        filename = "Test/TryFile.jpg";
        filename2 = "Test/TryFile2.jpg";
        try {
            sending = new TransferFile(filename);
        } catch (IOException e) {
            e.printStackTrace();
        }
        receiving = new TransferFile(filename2, sending.getBufferSize());
    }

    @Test
    public void testSetUp() {
        assertEquals("Files/" + filename, sending.getFilename());
        assertEquals("Files/" + filename2, receiving.getFilename());
        assertEquals(sending.getBufferSize(), receiving.getBufferSize());
    }

    @Test
    public void testReadFromAndAppendToBuffer() {
        int testSize = 1024;
        int nrPackets = sending.getBufferSize() / 1024;
        int counter = 0;
        while (counter < nrPackets) {
            byte[] readData = sending.readFromBuffer(testSize);
            assertEquals(testSize, readData.length);
            assertEquals((counter + 1) * testSize, sending.getLocation());
            try {
                receiving.appendToBuffer(readData);
            } catch (TransferFile.EndOfFileException e) {
                e.printStackTrace();
            }
            assertEquals((counter + 1) * testSize, receiving.getLocation());
            counter++;
        }
        byte[] readData2 = sending.readFromBuffer(testSize);
        assertEquals(sending.getBufferSize() % 1024, readData2.length);
    }

    @Test
    public void testException() {
        int testSize = 1024;
        int nrPackets = sending.getBufferSize() / 1024;
        int counter = 0;
        while (counter < nrPackets) {
            byte[] readData = sending.readFromBuffer(testSize);
            try {
                receiving.appendToBuffer(readData);
            } catch (TransferFile.EndOfFileException e) {
                e.printStackTrace();
            }
            assertEquals((counter + 1) * testSize, receiving.getLocation());
            counter++;
        }
        byte[] readData2 = sending.readFromBuffer(testSize);
        boolean testThrown = false;
        try {
            receiving.appendToBuffer(readData2);
        } catch (TransferFile.EndOfFileException e) {
            testThrown = true;
        }
        assertTrue(testThrown);
        assertEquals(receiving.getBufferSize(), receiving.getLocation());
        TransferFile testReceiving = null;
        try {
            testReceiving = new TransferFile(filename2);
        } catch (IOException e) {
            e.printStackTrace();
        }
//        assertEquals(sending.getBufferSize(), testReceiving.getBufferSize()); //TODO: look how it can be that the size differs
    }
}
