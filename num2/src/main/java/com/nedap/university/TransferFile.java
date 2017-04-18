package com.nedap.university;

import javax.imageio.IIOException;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
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

    TransferFile(String filename) throws IOException {
        this.filename = "Files/" + filename;
        createPath();
        this.location = 0;
    }

    private void createPath() throws IOException {
        Path path = Paths.get(this.filename);
        buffer = Files.readAllBytes(path);
    }

    int getBufferSize() {
        return this.buffer.length;
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

    void appendToBuffer(byte[] data) throws EndOfFileException {
        for (int i = 0; i < data.length; i++) {
            buffer[i + location] = data[i];
        }
        location += data.length;
        if (location == buffer.length) {
            saveReceivedFile();
            throw new EndOfFileException("Final data of this file received.");
        }
    }

    void saveReceivedFile() {
        try {
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(buffer));
            File outputfile = new File(filename);
            ImageIO.write(image, "jpg", outputfile);
        } catch (IIOException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();//TODO
        }
    }

    class EndOfFileException extends Throwable {
        EndOfFileException(String msg) {
            super(msg);
        }
    }
}