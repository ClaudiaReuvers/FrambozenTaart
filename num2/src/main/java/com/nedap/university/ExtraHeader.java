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
    public ExtraHeader(boolean syn, boolean ack, boolean fin, boolean pause, long ackNr, long seqNr) {
        setLength(0);
        setFlags(syn, ack, fin, pause);
        setAckNr(ackNr);
        setSeqNr(seqNr);
        setHeader();
    }

    public void setLength(int length) {
        this.length = (byte)length;
        setHeader();
    }

    public void setFlags(boolean syn, boolean ack, boolean fin, boolean pause) {
        setSyn(syn);
        setAck(ack);
        setFin(fin);
        setPause(pause);
        setHeader();
    }

    public void setUploadRequest() {
        setRequest(true);
        setRequestBit(false);
        setHeader();
    }

    public void setDownloadRequest() {
        setRequest(true);
        setRequestBit(true);
        setHeader();
    }

    public void setGetList() {
        setRequest(false);
        setRequestBit(true);
        setHeader();
    }

    public void setNoRequest() {
        setRequest(false);
        setRequestBit(false);
        setHeader();
    }

    private void setDNS(boolean DNS) {
        if (DNS) {
            flags = setBitToOne(flags, 7);
        } else {
            flags = setBitToZero(flags, 7);
        }
        setHeader();
    }

    private void setDNSflag(boolean DNS) {
        if (DNS) {
            flags = setBitToOne(flags, 6);
        } else {
            flags = setBitToZero(flags, 6);
        }
        setHeader();
    }

    public void setDNSRequest() {
        setDNS(true);
        setDNSflag(true);
        setHeader();
    }

    public void setDNSResponse() {
        setDNS(true);
        setDNSflag(false);
        setHeader();
    }

    public void setNoDNS() {
        setDNS(false);
        setDNSflag(false);
        setHeader();
    }

    private void setRequest(boolean request) {
        if (request) {
            flags = setBitToOne(flags, 5);
        } else {
            flags = setBitToZero(flags, 5);
        }
        setHeader();
    }

    private void setRequestBit(boolean requestBit) {
        if (requestBit) {
            flags = setBitToOne(flags, 4);
        } else {
            flags = setBitToZero(flags, 4);
        }
        setHeader();
    }

    public void setSyn(boolean syn) {
        if (syn) {
            flags = setBitToOne(flags, 3);
        } else {
            flags = setBitToZero(flags, 3);
        }
        setHeader();
    }

    public void setAck(boolean ack) {
        if (ack) {
            flags = setBitToOne(flags, 2);
        } else {
            flags = setBitToZero(flags, 2);
        }
        setHeader();
    }

    public void setFin(boolean fin) {
        if (fin) {
            flags = setBitToOne(flags, 1);
        } else {
            flags = setBitToZero(flags, 1);
        }
        setHeader();
    }

    public void setPause(boolean pause) {
        if (pause) {
            flags = setBitToOne(flags, 0);
        } else {
            flags = setBitToZero(flags, 0);
        }
        setHeader();
    }

    private byte setBitToOne(byte bits, int position) {
        return (byte) (bits | (1 << position));
    }

    private byte setBitToZero(byte bits, int position) {
        return (byte) (bits & ~(1 << position));
    }

    public int getLengthData() {
        return (int) length;
    }

    public boolean isDNSRequest() {
        return isDNS() & isSet(flags, 6);
    }

    public boolean isDNSResponse() {
        return isDNS() & !isSet(flags, 6);
    }

    public boolean isDNS() {
        return isSet(flags, 7);
    }

    public boolean isRequest() {
        return isSet(flags, 5);
    }

    public boolean isDownloadRequest() {
        return isSet(flags, 4) && isSet(flags, 5);
    }

    public boolean isUploadRequest() {
        return isSet(flags, 4) && !isSet(flags, 5);
    }

    public boolean isGetList() {
        return !isSet(flags, 4) && isSet(flags, 5);
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
        setHeader();
    } //TODO: add exception if is set when isAck()==false + add in testExtraHeader

    public long getAckNr() {
        long result = 0x00FF & ackNr[0];
        for (int i = 1; i < ackNr.length; i++) {
            result <<= 8;
            result += 0x00FF & ackNr[i];
        }
        return result;
    }

    public void setSeqNr(long seqNr) {
        this.seqNr[0] = (byte) (seqNr >> 24);
        this.seqNr[1] = (byte) (seqNr >> 16);
        this.seqNr[2] = (byte) (seqNr >> 8);
        this.seqNr[3] = (byte) seqNr;
        setHeader();
    }

    public long getSeqNr() {
        long result = 0x00FF & seqNr[0];
        for (int i = 1; i < seqNr.length; i++) {
            result <<= 8;
            result += 0x00FF & seqNr[i];
        }
        return result;
    }

    private void setHeader() {
        header = new byte[1 + 1 + ackNr.length + seqNr.length];
        header[0] = flags;
        for (int i = 0; i < ackNr.length; i++) {
            header[i+1] = ackNr[i];
        }
        for (int i = 0; i < seqNr.length; i++) {
            header[i+5] = seqNr[i];
        }
        header[9] = length;
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
        setHeader();
        return this.header;
    }

    public static ExtraHeader returnHeader(byte[] data) {
        return new ExtraHeader(data);
    }

    private ExtraHeader(byte[] bytes) {
        this.flags = bytes[0];
        for (int i = 0; i < ackNr.length; i++) {
            this.ackNr[i] = bytes[i + 1];
        }
        for (int i = 0; i < ackNr.length; i++) {
            this.seqNr[i] = bytes[i + 5];
        }
        this.length = bytes[9];
        setHeader();
    }

    public int headerLength() {
        return header.length;
    }

    @Override
    public String toString() {
        String headerString = "";
        if (isDNSRequest()) {
            headerString += "DNS request";
        } else if (isDNSResponse()) {
                headerString += "DNS response";
        }
        if (isSyn()) {
            headerString += "SYN[" + getSeqNr() + "] ";
        }
        if (isAck()) {
            headerString += "ACK[" + getAckNr() + "] ";
        }
        if (isFin()) {
            headerString += "FIN ";
        }
        if (isPause()) {
            headerString += "PAUSE ";
        }
        if (isDownloadRequest()) {
            headerString += "DOWNLOAD";
        } else if (isUploadRequest()) {
            headerString += "UPLOAD";
        } else if (isGetList()) {
            headerString += "GETLIST";
        }
        headerString += "\n Datalength: " + getLengthData();
        return headerString;
    }

}

