package sample;


import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class TransferUtils {

    public static byte[] fileToBytes(File f) throws IOException {
        int bytesRead;
        FileInputStream fis = new FileInputStream(f);
        if (f.length() > Integer.MAX_VALUE) {
            throw new IOException("File size is too big.");
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream((int)f.length());
        byte[] buffer = new byte[8192];
        while ((bytesRead = ((InputStream)fis).read(buffer)) != -1) {
            baos.write(buffer, 0, bytesRead);
        }
        ((InputStream)fis).close();
        return baos.toByteArray();
    }

    public static void bytesToFile(byte[] bytes, File file) {
        FilterOutputStream bos = null;
        try {
            bos = new BufferedOutputStream(new FileOutputStream(file));
            bos.write(bytes);
            ((BufferedOutputStream)bos).flush();
        }
        catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (bos != null) {
                    bos.close();
                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static byte[] intToBytes(int number) {
        return new byte[]{(byte)(number >>> 24 & 255), (byte)(number >>> 16 & 255), (byte)(number >>> 8 & 255), (byte)(number & 255)};
    }

    public static int bytesToInt(byte[] bytes) {
        return (bytes[0] & 255) << 24 | (bytes[1] & 255) << 16 | (bytes[2] & 255) << 8 | bytes[3] & 255;
    }

    public static byte[] createPacket(byte[] ... bytes) {
        int offset = 0;
        int totalLength = 0;
        for (int i = 0; i < bytes.length; ++i) {
            totalLength += bytes[i].length;
        }
        byte[] packet = new byte[totalLength];
        for (int i = 0; i < bytes.length; ++i) {
            if (bytes[i].length <= 0) continue;
            System.arraycopy(bytes[i], 0, packet, offset, bytes[i].length);
            offset += bytes[i].length;
        }
        return packet;
    }
}

