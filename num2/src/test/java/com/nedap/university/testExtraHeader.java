package com.nedap.university;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by claudia.reuvers on 11/04/2017.
 */
public class testExtraHeader {

    private ExtraHeader header;
    private ExtraHeader inputHeader;

    @Before
    public void setUp() {
        header = new ExtraHeader();
        inputHeader = new ExtraHeader(true, true, true, true, 2, 2);
    }

    @Test
    public void testSetUp() {
        assertEquals(10, header.getLength());
        assertFalse(header.isSyn());
        assertFalse(header.isAck());
        assertFalse(header.isFin());
        assertFalse(header.isPause());
        assertEquals(0, header.getAckNr());
        assertEquals(0, header.getSeqNr());
        assertEquals(10, header.getHeader().length);
        assertEquals(header.getLength(), header.getHeader().length);
    }

    @Test
    public void testSetUpHeader2() {
        assertEquals(10, inputHeader.getLength());
        assertTrue(inputHeader.isSyn());
        assertTrue(inputHeader.isAck());
        assertTrue(inputHeader.isFin());
        assertTrue(inputHeader.isPause());
        assertEquals(2, inputHeader.getAckNr());
        assertEquals(2, inputHeader.getSeqNr());
        assertEquals(10, inputHeader.getHeader().length);
        assertEquals(inputHeader.getLength(), inputHeader.getHeader().length);
    }

    @Test
    public void testSetAndGetPerFlags() {
        header.setSyn(true);
        assertTrue(header.isSyn());
        assertFalse(header.isAck());
        assertFalse(header.isFin());
        assertFalse(header.isPause());
        header.setSyn(false);
        header.setAck(true);
        assertFalse(header.isSyn());
        assertTrue(header.isAck());
        assertFalse(header.isFin());
        assertFalse(header.isPause());
        header.setAck(false);
        header.setFin(true);
        assertFalse(header.isSyn());
        assertFalse(header.isAck());
        assertTrue(header.isFin());
        assertFalse(header.isPause());
        header.setFin(false);
        header.setPause(true);
        assertFalse(header.isSyn());
        assertFalse(header.isAck());
        assertFalse(header.isFin());
        assertTrue(header.isPause());
        header.setPause(false);
        assertFalse(header.isSyn());
        assertFalse(header.isAck());
        assertFalse(header.isFin());
        assertFalse(header.isPause());
    }

    @Test
    public void testSetAndGetFlags() {
        header.setFlags(true, true, true, true);
        assertTrue(header.isSyn());
        assertTrue(header.isAck());
        assertTrue(header.isFin());
        assertTrue(header.isPause());
        header.setFlags(true, false, false, true);
        assertTrue(header.isSyn());
        assertFalse(header.isAck());
        assertFalse(header.isFin());
        assertTrue(header.isPause());
    }

    @Test
    public void testSetAndGetAckNr() {
        header.setAckNr(0);
        assertEquals(0, header.getAckNr());
        header.setAckNr(34566);
        assertEquals(34566, header.getAckNr());
        header.setAckNr((long) Math.pow(2, 32) - 1);
        assertEquals((long) Math.pow(2, 32) - 1, header.getAckNr());
        header.setAckNr((long) Math.pow(2,32));
        assertEquals(0, header.getAckNr());
        header.setAckNr((long) Math.pow(2, 32) + 1);
        assertEquals(1, header.getAckNr());
    }

    @Test
    public void testSetAndGetSeqNr() {
        header.setSeqNr(0);
        assertEquals(0, header.getSeqNr());
        header.setSeqNr(34566);
        assertEquals(34566, header.getSeqNr());
        header.setSeqNr((long) Math.pow(2, 32) - 1);
        assertEquals((long) Math.pow(2, 32) - 1, header.getSeqNr());
        header.setSeqNr((long) Math.pow(2,32));
        assertEquals(0, header.getSeqNr());
        header.setSeqNr((long) Math.pow(2, 32) + 1);
        assertEquals(1, header.getSeqNr());
    }


    @Test
    public void testReturnHeader() {
        ExtraHeader copyHeader = ExtraHeader.returnHeader(inputHeader.getHeader());
        assertEquals(inputHeader.getLength(), copyHeader.getLength());
        assertEquals(inputHeader.isSyn(), copyHeader.isSyn());
        assertEquals(inputHeader.isAck(), copyHeader.isAck());
        assertEquals(inputHeader.isFin(), copyHeader.isFin());
        assertEquals(inputHeader.isPause(), copyHeader.isPause());
        assertEquals(inputHeader.getAckNr(), copyHeader.getAckNr());
        assertEquals(inputHeader.getSeqNr(), copyHeader.getSeqNr());
    }
}
