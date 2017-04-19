package com.nedap.university;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;

/**
 * Created by claudia.reuvers on 18/04/2017.
 */
public class TransferFile {

    private String filename;
    private byte[] buffer;
    private int location;

    TransferFile(String filename, int length) {
        this.filename = "Files/" + filename;
        this.buffer = new byte[length];
        this.location = 0;
    }

    TransferFile(String filename) {//throws IOException {
        this.filename = "Files/" + filename;
//        createPath();
        this.location = 0;
    }

    void createPath() throws IOException {
        Path path = Paths.get(this.filename);
        buffer = Files.readAllBytes(path);
    }

    int getBufferSize() {
        return this.buffer.length;
    }

    void setBufferSize(int size) {
        this.buffer = new byte[size];
    }

    String getFilename() {
        return this.filename;
    }

    int getLocation() {
        return this.location;
    }

    byte[] readFromBuffer(int length) {//throws EndOfFileException {
        int endPoint = location + length;
        if (endPoint > buffer.length) {
            endPoint = buffer.length;
        }
        byte[] data = Arrays.copyOfRange(buffer, location, endPoint);
        location += length;
        return data;
    }

    void appendToBuffer(byte[] data, int length) throws EndOfFileException {
        System.out.println("Append from " + location + " with length " + length);
        System.arraycopy(data,0, buffer, location, length);
//        for (int i = 0; i < data.length; i++) {
//            buffer[i + location] = data[i];
//        }
        location += length;
        if (location == buffer.length) {
//            saveReceivedFile();
            throw new EndOfFileException("End of the file.\nSaved file as " + filename);
        }
    }

    void saveReceivedFile() {
        FileOutputStream outputFile = null;
        boolean isValidPath = false;
        while (!isValidPath) {
            try {
                System.out.println("Try to write file to " + filename);
                outputFile = new FileOutputStream(filename);
                isValidPath = true;
            } catch (FileNotFoundException e) {
                filename = Main.readString("New filename: ");
            }
        }
        System.out.println("Write file to " + filename);
        try {
            outputFile.write(buffer);
        } catch (IOException e1) {
            System.out.println("Could not write the retrieved data to a the file.");
        }

//            BufferedImage image = ImageIO.read(new ByteArrayInputStream(buffer));
//            File outputfile = new File(filename);
//            ImageIO.write(image, "jpg", outputfile);
    }

    class EndOfFileException extends Throwable {
        EndOfFileException(String msg) {
            super(msg);
        }
    }
}