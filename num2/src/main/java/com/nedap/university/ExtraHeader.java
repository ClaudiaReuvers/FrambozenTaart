package com.nedap.university;

/**
 * Created by claudia.reuvers on 11/04/2017.
 */
public class ExtraHeader {


    private byte flags;
    private byte[] ackNr = new byte[4];
    private byte[] seqNr = new byte[4];
    private byte length;
    private byte[] header;

    /**
     * Creates a <code>ExtraHeader</code> with default values.
     * @ensures this.getLength() == 10
     * @ensures this.isSyn() == false
     * @ensures this.isAck() == false
     * @ensures this.isFin() == false
     * @ensures this.isPause() == false
     * @ensures this.getAckNr() == 0
     * @ensures this.getSeqNr() == 0
     */
    public ExtraHeader() {
        this(false, false, false, false, 0, 0);
//        firstByte = (byte) (makeLength(4) | setFlags(false, false, false, false));
//        setAckNr(0);
//        setSeqNr(0);
    }

    /**
     * Creates a <code>ExtraHeader</code> with specified values.
     * Acknowledgement number may only be set if isAck() == true
     * @param syn synchronize flag, if syn == true, this bit is set to 1
     * @param ack acknowledge flag, if ack == true, this bit is set to 1
     * @param fin fin flag, if fin == true, this bit is set to 1
     * @param pause pause flag, if pause == true, this bit is set to 1
     * @param ackNr sets the acknowledgementnr to ackNr
     * @param seqNr sets the sequencenr to seqNr
     */
    public ExtraHeader(boolean syn, boolean ack, boolean fin, boolean pause, int ackNr, int seqNr) {
        setLength(10);
        setFlags(syn, ack, fin, pause);
//        setLength(length);
//        setFlags(syn, ack, fin, pause);
        setAckNr(ackNr);
        setSeqNr(seqNr);
        setHeader();
    }

    private void setLength(int length) {
        this.length = (byte)length;
    }

    public void setFlags(boolean syn, boolean ack, boolean fin, boolean pause) {
        setSyn(syn);
        setAck(ack);
        setFin(fin);
        setPause(pause);
    }

    public void setSyn(boolean syn) {
        if (syn) {
            flags = setBitToOne(flags, 3);
//            flags = (byte) (flags | (1 << 3));
//            flags  = (byte) (flags & (byte) 8);
        } else {
            flags = setBitToZero(flags, 3);
        }
    }

    public void setAck(boolean ack) {
        if (ack) {
            flags = setBitToOne(flags, 2);
//            flags = (byte) (flags | (1 << 2));
//            flags  = (byte) (flags & (byte) 4);
        } else {
            flags = setBitToZero(flags, 2);
        }
    }

    public void setFin(boolean fin) {
        if (fin) {
            flags = setBitToOne(flags, 1);
//            flags = (byte) (flags | (1 << 1));
//            flags  = (byte) (flags & (byte) 2);
        } else {
            flags = setBitToZero(flags, 1);
        }
    }

    public void setPause(boolean pause) {
        if (pause) {
            flags = setBitToOne(flags, 0);
//            flags = (byte) (flags | (1 << 0));
//            flags  = (byte) (flags & (byte) 1);
        } else {
            flags = setBitToZero(flags, 0);
        }
    }

    private byte setBitToOne(byte bits, int position) {
        return (byte) (bits | (1 << position));
    }

    private byte setBitToZero(byte bits, int position) {
        return (byte) (bits & ~(1 << position));
    }

    public int getLength() {
//        byte length = (byte) (lengthFlags >> 4);
        return (int) length;
    }

    public boolean isSyn() {
        return isSet(flags, 3);
    }

    public boolean isAck() {
        return isSet(flags, 2);
    }

    public boolean isFin() {
        return isSet(flags, 1);
    }

    public boolean isPause() {
        return isSet(flags, 0);
    }

    private boolean isSet(byte testByte, int bitPosition) {
        return (testByte >> bitPosition & 1) == 1;
    }

    public void setAckNr(long ackNr) {
        this.ackNr[0] = (byte) (ackNr >> 24);
        this.ackNr[1] = (byte) (ackNr >> 16);
        this.ackNr[2] = (byte) (ackNr >> 8);
        this.ackNr[3] = (byte) ackNr;
    } //TODO: add exception if is set when isAck()==false + add in testExtraHeader

    public long getAckNr() {
        long result = 0x00FF & ackNr[0];
        for (int i = 1; i < ackNr.length; i++) {
            result <<= 8;
            result += 0x00FF & ackNr[i];
        }
//        long result = 0x00FF & ackNr[0];
//        result <<= 8;
//        result += 0x00FF & ackNr[1];
//        result <<= 8;
//        result += 0x00FF & ackNr[2];
//        result <<= 8;
//        result += 0x00FF & ackNr[3];
        return result;
    }

    public void setSeqNr(long seqNr) {
        this.seqNr[0] = (byte) (seqNr >> 24);
        this.seqNr[1] = (byte) (seqNr >> 16);
        this.seqNr[2] = (byte) (seqNr >> 8);
        this.seqNr[3] = (byte) seqNr;
    }

    public long getSeqNr() {
        long result = 0x00FF & seqNr[0];
        for (int i = 1; i < seqNr.length; i++) {
            result <<= 8;
            result += 0x00FF & seqNr[i];
        }
        return result;
//        return (seqNr[0] << 24) + (seqNr[1] << 16) + (seqNr[2] << 8) + seqNr[3];
    }

    private void setHeader() {
        header = new byte[1 + 1 + ackNr.length + seqNr.length];
        header[0] = length;
        header[1] = flags;
        for (int i = 0; i < ackNr.length; i++) {
            header[i+2] = ackNr[i];
        }
        for (int i = 0; i < seqNr.length; i++) {
            header[i+6] = seqNr[i];
        }
    }

    public byte getLengthByte() {
        return this.length;
    }

    public byte getFlagsByte() {
        return this.flags;
    }

    public byte[] getAckBytes() {
        return this.ackNr;
    }

    public byte[] getSeqBytes() {
        return this.seqNr;
    }

    public byte[] getHeader() {
        return header;
    }

//    public static void main(String[] args) {
//        ExtraHeader header = new ExtraHeader();
//        System.out.println("Flags field: " + Integer.toBinaryString(header.getFlagsByte()));
//        System.out.println("Syn (false): " + header.isSyn());
//        header.setSyn(true);
//        System.out.println("Flags field: " + Integer.toBinaryString(header.getFlagsByte()));
//        System.out.println("Syn (true): " + header.isSyn());
//        System.out.println("Ack (false): " + header.isAck());
//        header.setSyn(false);
//        header.setAck(true);
//        System.out.println("Flags field: " + Integer.toBinaryString(header.getFlagsByte()));
//        System.out.println("Ack (true): " + header.isAck());
//        header.setAck(false);
//        header.setFin(true);
//        System.out.println("Flags field: " + Integer.toBinaryString(header.getFlagsByte()));
//        System.out.println("Fin (true): " + header.isFin());
//    }

}
