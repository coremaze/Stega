package stega.Model;

import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.TreeMap;
import stega.Controller.Controller;
import java.util.zip.CRC32;
import java.io.ByteArrayOutputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;
import java.util.zip.Deflater;
import java.util.zip.Inflater;
import javax.imageio.ImageIO;

public class Model {

    ArrayList<FileInfo> fileInfos;
    Controller controller;

    public Model(Controller controller) {
        this.controller = controller;
        this.fileInfos = new ArrayList<>();
    }

    public boolean addFile(String filePath, String fileName) {
        for (FileInfo fi : this.fileInfos) {
            if (fi.getFileName().equals(fileName)) {
                return true;
            }
        }
        FileInfo fileInfo = new FileInfo(filePath, fileName);
        fileInfos.add(fileInfo);
        return false;
    }

    public ArrayList<String> getFileNames() {
        ArrayList<String> fileNames = new ArrayList<>();
        for (FileInfo fi : this.fileInfos) {
            fileNames.add(fi.getFileName());
        }
        return fileNames;
    }

    public void removeFile(String fileName) {
        for (int i = 0; i < this.fileInfos.size(); i++) {
            if (this.fileInfos.get(i).getFileName().equals(fileName)) {
                this.fileInfos.remove(i);
                return;
            }
        }
    }

    public TreeMap<String, byte[]> getFilesFromImage(String imageFilePath) {
        //imageFilePath is received, but not used yet since we aren't generating encoded files yet
        TreeMap<String, byte[]> map = new TreeMap<>();
        try {
            //map.put("testfile1.txt", new byte[]{'a', 's', 'd', 'f'});
            //map.put("testfile2.txt", new byte[]{'w', 'e', 'e', 'e'});
            byte[] decodedBuffer = decodeBufferFromImage(imageFilePath);
            return decodeFiles(decodedBuffer);

        } catch (IOException ex) {
            controller.warn(ex.getMessage());
        }
        return map;
    }

    public boolean saveFile(String path, String fileName, byte[] contents) {
        File file = new File(path, fileName);

        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(contents);
        } catch (IOException ex) {
            controller.warn(ex.getMessage());
            return true;
        }
        return false;
    }

    private byte[] encodeInt(int integer) {
        long value = (long) integer;
        value &= 0xFFFFFFFF;
        return new byte[]{
            (byte) (value >> 0),
            (byte) (value >> 8),
            (byte) (value >> 16),
            (byte) (value >> 24)};
    }

    private int decodeInt(byte[] bytearray, int offset) {
        int result = 0;
        // Seriously, Java? Why would I ever want a byte to be signed?
        // I shouldn't have to & all these with 0xFF.
        result |= ((int) bytearray[offset + 0] & 0xFF) << 0;
        result |= ((int) bytearray[offset + 1] & 0xFF) << 8;
        result |= ((int) bytearray[offset + 2] & 0xFF) << 16;
        result |= ((int) bytearray[offset + 3] & 0xFF) << 24;
        return result;
    }

    private short decodeShort(byte[] bytearray, int offset) {
        short result = 0;
        result |= ((short) bytearray[offset + 0] & 0xFF) << 0;
        result |= ((short) bytearray[offset + 1] & 0xFF) << 8;
        return result;
    }

    private byte[] encodeShort(short sh) {
        return new byte[]{
            (byte) (sh >> 0),
            (byte) (sh >> 8)};
    }

    public byte[] encodeFiles() throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        for (FileInfo fi : fileInfos) {
            /* 
            file entry:
            2 bytes file name length
            file name
            4 bytes file size
            file contents
             */
            if (fi == null || output == null) {
                throw new IllegalArgumentException("Invalid file.");
            }
            output.write(encodeShort((short) fi.getFileName().length()));
            output.write(fi.getFileName().getBytes());
            Path path = Paths.get(fi.getPath(), fi.getFileName());
            byte[] fileContents = Files.readAllBytes(path);
            output.write(encodeInt(fileContents.length));
            output.write(fileContents);
        }
        byte[] outputBytes = compress(output.toByteArray());
        CRC32 crc32 = new CRC32();
        crc32.update(outputBytes);
        /* result:
        4 bytes length
        4 bytes CRC
        file entries
         */
        byte[] result = new byte[4 + 4 + outputBytes.length];
        System.arraycopy(encodeInt(outputBytes.length), 0, result, 0, 4);
        System.arraycopy(encodeInt((int) crc32.getValue()), 0, result, 4, 4);
        System.arraycopy(outputBytes, 0, result, 8, outputBytes.length);
        return result;
    }

    public TreeMap<String, byte[]> decodeFiles(byte[] data) throws IllegalArgumentException {
        if (data.length < 8) {
            throw new IllegalArgumentException("Invalid file. (Invalid data length)");
        }
        int length = decodeInt(data, 0);
        if (length > data.length - 8 || length < 6) {
            throw new IllegalArgumentException("Invalid file. (Invalid length field)");
        }
        int sourceCrc = decodeInt(data, 4);

        TreeMap<String, byte[]> files = new TreeMap<>();

        byte[] data2 = new byte[length];
        System.arraycopy(data, 8, data2, 0, length);
        data = decompress(data2);

        CRC32 crc32 = new CRC32();
        crc32.update(data2);
        int actualCrc = (int) crc32.getValue();

        if (actualCrc != sourceCrc) {
            System.out.printf("CRC: %X Expected CRC: %X\n", actualCrc, sourceCrc);
            throw new IllegalArgumentException("Invalid file. (Incorrect CRC)");
        }

        while (data.length > 6) {
            short fileNameLength = decodeShort(data, 0);
            byte[] fileNameBytes = new byte[fileNameLength];
            System.arraycopy(data, 2, fileNameBytes, 0, fileNameLength);
            String fileName = new String(fileNameBytes);

            int contentsLength = decodeInt(data, 2 + fileNameLength);
            byte[] contents = new byte[contentsLength];
            System.arraycopy(data, 2 + fileNameLength + 4, contents, 0, contentsLength);

            files.put(fileName, contents);

            int remainingLength = data.length - 2 - fileNameLength - 4 - contents.length;
            byte[] tmpdata = new byte[remainingLength];
            System.arraycopy(data, 2 + fileNameLength + 4 + contents.length, tmpdata, 0, remainingLength);
            data = tmpdata;
        }
        return files;
    }

    public void encodeBufferIntoImage(String sourceImage, String destImage, byte[] buffer) throws IOException, IllegalArgumentException {
        File f = new File(sourceImage);
        BufferedImage img0 = ImageIO.read(f);
        if (img0 == null) {
            throw new IOException("Could not read image.");
        }
        BufferedImage img = new BufferedImage(img0.getWidth(), img0.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.drawImage(img0, 0, 0, img0.getWidth(), img0.getHeight(), null);
        g.dispose();

        int width = img.getWidth();
        int height = img.getHeight();

        if ((width * height) < buffer.length) {
            throw new IllegalArgumentException("Too much data to encode in an image this small!");
        }

        int i = 0;
        boolean done = false;
        for (int y = 0; y < height && !done; y++) {
            for (int x = 0; x < width && !done; x++) {
                int color = img.getRGB(x, y);

                int alpha = ((color >> 24) & 0xFF);
                int red = ((color >> 16) & 0xFF);
                int green = ((color >> 8) & 0xFF);
                int blue = ((color >> 0) & 0xFF);

                int bufByte = buffer[i];

                red = ((red & 0b11111000) | (bufByte & 0b00000111));
                green = ((green & 0b11111000) | ((bufByte >> 3) & 0b00000111));
                blue = ((blue & 0b11111100) | ((bufByte >> 6) & 0b00000011));

                int newColor = 0;

                newColor |= (red << 16);
                newColor |= (green << 8);
                newColor |= (blue << 0);
                newColor |= (alpha << 24);

                img.setRGB(x, y, newColor);
                i++;
                if (i == buffer.length) {
                    done = true;
                }
            }
        }
        f = new File(destImage);
        ImageIO.write(img, "png", f);
    }

    public byte[] decodeBufferFromImage(String sourceImage) throws IOException {
        File f = new File(sourceImage);
        BufferedImage img0 = ImageIO.read(f);
        if (img0 == null) {
            throw new IOException("Could not read image.");
        }
        BufferedImage img = new BufferedImage(img0.getWidth(), img0.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.drawImage(img0, 0, 0, img0.getWidth(), img0.getHeight(), null);
        g.dispose();

        int width = img.getWidth();
        int height = img.getHeight();

        byte[] result = new byte[width * height];

        int i = 0;
        boolean done = false;
        for (int y = 0; y < height && !done; y++) {
            for (int x = 0; x < width && !done; x++) {
                int color = img.getRGB(x, y);

                int red = ((color >> 16) & 0xFF);
                int green = ((color >> 8) & 0xFF);
                int blue = ((color >> 0) & 0xFF);

                int bufByte = 0;
                bufByte |= (red & 0b00000111);
                bufByte |= ((green & 0b00000111) << 3);
                bufByte |= ((blue & 0b00000011) << 6);

                result[i] = (byte) bufByte;

                i++;
            }
        }
        return result;
    }

    private byte[] compress(byte[] input) {
        Deflater deflater = new Deflater();
        deflater.setInput(input);
        deflater.finish();
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        while (!deflater.finished()) {
            int n = deflater.deflate(buffer);
            ostream.write(buffer, 0, n);
        }
        return ostream.toByteArray();
    }

    private byte[] decompress(byte[] input) {
        Inflater inflater = new Inflater();
        inflater.setInput(input);
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream ostream = new ByteArrayOutputStream();
        while (!inflater.finished()) {
            int n = 0;
            try {
                n = inflater.inflate(buffer);
            } catch (DataFormatException ex) {
                Logger.getLogger(Model.class.getName()).log(Level.SEVERE, null, ex);
            }
            ostream.write(buffer, 0, n);
        }
        return ostream.toByteArray();
    }
}
