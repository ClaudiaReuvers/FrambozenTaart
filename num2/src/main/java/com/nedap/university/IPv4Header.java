//package com.nedap.university;
//
//import java.net.InetAddress;
//
///**
// * Created by claudia.reuvers on 10/04/2017.
// */
//public class IPv4Header {
//
//    private byte firstByte;
//    private byte TOS;
//    private byte[] totalLength;     //16
//    private byte[] identificationNr;
//    private byte[] flagsAndOffset;
//    private byte TTL;
//    private byte protocol;
//    private byte[] checksum;
//    private byte[] srcAddress;
//    private byte[] dstAddress;
//
//    public IPv4Header() {
//        firstByte = (byte) (makeVersion(4) | makeHeaderLength(5));
//        TOS = makeTOS(0); //TOS
//        totalLength = makeTotalLength(20);//TODO: + length of the data
//        identificationNr = makeIdent(9);//TODO
//        flagsAndOffset = makeFlagsAndOffset(true, false, 0);//TODO
//        TTL = makeTTL(64); //TODO: now default
//        protocol = makeProtocol(17);
//        checksum = makeChecksum();
//        srcAddress = makeSourceAddress();
//        dstAddress = makeDestinationAddress(InetAddress.getLocalHost());
//    }
//
//    //First byte
//    private byte makeVersion(int versionNumber) {
//        byte version = (byte) versionNumber;
//        return (byte) (version << 4);
//    }
//
//    private byte makeHeaderLength(int headerLength) {
//        return (byte)headerLength;
//    }
//
//    public int getVersion() {
//        byte version = (byte) (firstByte >> 4);
//        return (int) version;
//    }
//
//    public int getHeaderLength() {
//        byte headerLength = (byte) (firstByte | (byte)getVersion());
//        return (int)headerLength;
//    }
//
//    //Second byte
//    private byte makeTOS(int tos) {
//        return (byte) tos;
//    }
//
//    private int getTOS() {
//        return (int) TOS;
//    }
//
//    //Third & fourth byte
//    private byte[] makeTotalLength(int totalLength) {
//        byte[] length = new byte[2];
//        length[0] = (byte) (totalLength >> 8);
//        length[1] = (byte) (totalLength);
//        return length;
//    }
//
//    public byte[] getTotalLength() {
//        return totalLength;
//    }
//
//    public int getTotalLengthInt() {
//        return (totalLength[0] << 8) + totalLength[1];
//    }
//
//    //Fifth & sixth byte
//    private byte[] makeIdent(int identificationNumber) {
//        byte[] ident = new byte[2];
//        ident[0] = (byte) (identificationNumber >> 8);
//        ident[1] = (byte) (identificationNumber);
//        return ident;
//    }
//
//    public byte[] getIdentificationNr() {
//        return identificationNr;
//    }
//
//    //Seventh & eigth byte
//    private byte[] makeFlagsAndOffset(boolean mayFragment, boolean moreFragments, int offset) {
//        byte[] flagsOffset = new byte[2];
//        flagsOffset[0] = (byte) (offset >> 8);
//        flagsOffset[1] = (byte) (offset);
//        setMayFragment(mayFragment, flagsOffset);
//        setMoreFragments(moreFragments, flagsOffset);
//        return flagsOffset;
//    }
//
//    private void setMoreFragments(boolean moreFragments, byte[] flagsOffset) {
//        if (moreFragments) {
//            flagsOffset[0] = (byte) (flagsOffset[0] & (byte)2);
//        }
//    }
//
//    private void setMayFragment(boolean mayFragment, byte[] flagsOffset) {
//        if (mayFragment) {
//            flagsOffset[0] = (byte) (flagsOffset[0] & (byte)4);
//        }
//    }
//
//    public byte[] getFlagsAndOffset() {
//        return flagsAndOffset;
//    }
//
//    public boolean mayFragment() {
//        return isSet(flagsAndOffset, 2);
//    }
//
//    public boolean moreFragments() {
//        return isSet(flagsAndOffset, 3);
//    }
//
//    //Nineth bit
//    private byte makeTTL(int ttl) {
//        return (byte) ttl;
//    }
//
//    private int getTTL() {
//        return (int) TTL;
//    }
//
//    //Tenth bit
//    private byte makeProtocol(int protocol) {
//        return (byte) protocol;
//    }
//
//    private int getProtocol() {
//        return (int) protocol;
//    }
//
//    //Eleventh and twelfth bit
//    private byte[] makeChecksum() {
//        byte[] checksum = new byte[2];
//        //TODO
//        return checksum;
//    }
//
//    private byte[] getChecksum() {
//        return checksum;
//    }
//
//    //13th - 16th bit
//    private byte[] makeSourceAddress() {
//        byte[] sourceAddress = new byte[4];
//        //TODO
//        return sourceAddress;
//    }
//
//    private byte[] getSourceAddress() {
//        return srcAddress;
//    }
//
//    //17th - 20th bit
//    private byte[] makeDestinationAddress(InetAddress address) {
//        byte[] destinationAddress = new byte[4];
//        //TODO
//        return destinationAddress;
//    }
//
//    private byte[] getDestinationAddress() {
//        return dstAddress;
//    }
//
//
//    private boolean isSet(byte[] byteArray, int bit) {
//        int index = bit / 8;
//        int bitPosition = bit * 8;
//        return (byteArray[index] >> bitPosition & 1) == 1;
//    }
//
//    public byte[] getByteArray() {
//        byte[] header = new byte[20];
//        header[0] = firstByte;
//        header[1] = TOS;
//        header[2] = totalLength[0];
//        header[3] = totalLength[1];
//        header[4] = identificationNr[0];
//        header[5] = identificationNr[1];
//        header[6] = flagsAndOffset[0];
//        header[7] = flagsAndOffset[1];
//        header[8] = TTL;
//        header[9] = protocol;
//        header[10] = checksum[0];
//        header[11] = checksum[1];
//        header[12] = srcAddress[0];
//        header[13] = srcAddress[1];
//        header[14] = srcAddress[2];
//        header[15] = srcAddress[3];
//        header[16] = dstAddress[0];
//        header[17] = dstAddress[1];
//        header[18] = dstAddress[2];
//        header[19] = dstAddress[3];
//        return header;
//    }
//
//
//    public static void main(String[] args) {
//        IPv4Header header = new IPv4Header();
//        System.out.println("Version: " + header.getVersion());
//        System.out.println("Hlength: " + header.getHeaderLength() + " " + header.getTotalLengthInt());
//
//        byte[] totalLength = header.getTotalLength();
//        System.out.println("Byte: " + Integer.toBinaryString((int)totalLength[0]) + "_" + Integer.toBinaryString((int)totalLength[1]) + " as int " + Byte.toString(totalLength[0]) + "_" + Byte.toString(totalLength[1]));
//        byte[] identnr = header.getIdentificationNr();
//        System.out.println("Byte: " + Integer.toBinaryString((int)identnr[0]) + "_" + Integer.toBinaryString((int)identnr[1]) + " as int " + Byte.toString(identnr[0]) + "_" + Byte.toString(identnr[1]));
//    }
//
//
//
////    private int version;
////    private int trafficClass;
////    private int flowLabel;
////    private int payloadLength;
////    private int nextHeader;
////    private int hopLimit;
////    private String sourceAddress;
////    private String destinationAddress;
////
////    public IPv4Header(int asdf) {
////        version = 0x6;
////        trafficClass = 0x00;
////        flowLabel = 0x00000;
////        payloadLength = 0x0014;
////        nextHeader = 0xfd;
////        hopLimit = 0xff;
////        sourceAddress = "2001067c2564a130d577e7f770d51a9d";
////        destinationAddress = "2001067c2564a170020423fffede4b2c";
////    }
//
////    public IPv4Header(List<Integer> packetList) {
////        version = getHeaderValue(VERSION, packetList);
////        trafficClass = getHeaderValue(TRAFFIC_CLASS, packetList);
////        flowLabel = getHeaderValue(FLOW_LABEL, packetList);
////        payloadLength = getHeaderValue(PAYLOAD_LENGTH, packetList);
////        nextHeader = getHeaderValue(NEXT_HEADER, packetList);
////        hopLimit = getHeaderValue(HOP_LIMIT, packetList);
////        sourceAddress = getPacketValueString(SOURCE_ADDRESS, packetList);
////        destinationAddress = getPacketValueString(DESTINATION_ADDRESS, packetList);
////    }
//
//}
