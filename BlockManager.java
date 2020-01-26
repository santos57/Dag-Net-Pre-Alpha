import java.io.*;
import java.util.Arrays;

public class BlockManager {
    static String saveLocation = "D:\\Blocks\\";
    static long[] rawData;

    public static Block read(long t, long[] hash) {
        int longCount = 0;
        rawData = new long[8];
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i != 5; i++) {
            sb.insert(0, "\\");
            for (int j = 0; j < i; j++) {
                sb.insert(0, "00");
            }
            sb.insert(0, t % 100);
            t /= 100;
        }
        sb.insert(0, saveLocation);
        for (int i = 0; i < 4; i++) {
            sb.append(String.format("%016x", hash[i]));
        }
        sb.append(".block");

        try {
            FileInputStream fr = new FileInputStream(sb.toString());
            byte[] data = fr.readAllBytes();
            int j = 1;
            long l = 0;
            for (int i = 0; i < data.length; i++, j++) {
                if (j > 8) {
                    addGrow(l, longCount++);
                    j = 1;
                    l = 0;
                }
                l += (Byte.toUnsignedLong(data[i]) << (64 - (8 * j)));
            }
            addGrow(l, longCount++);
            return new Block(Arrays.copyOf(rawData, longCount));
        } catch (IOException e) {
            return null;
        }

    }

    private static void addGrow(long data, int logicalSize) {
        while (logicalSize >= rawData.length) {
            rawData = Arrays.copyOf(rawData, rawData.length * 2);
        }
        rawData[logicalSize] = data;
    }

    public static boolean save(Block b) {
        long t = b.time;
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i != 5; i++) {
            sb.insert(0, "\\");
            for (int j = 0; j < i; j++) {
                sb.insert(0, "00");
            }
            sb.insert(0, t % 100);
            t /= 100;
        }
        sb.insert(0, saveLocation);
        try {
            new File(sb.toString()).mkdirs();
        } catch (Exception e) {
            return false;
        }
        File file;
        for (int i = 0; i < 4; i++) {
            sb.append(String.format("%016x", b.hash[i]));
        }
        sb.append(".block");
        file = new File(sb.toString());
        try {
            FileOutputStream fw = new FileOutputStream(file);
            if (b.rawData != null) {
                byte[] bbuff = new byte[8];
                for (long l : b.rawData) {
                    for (int i = 1; i <= 8; i++) {
                        bbuff[i - 1] = (((byte) (l >>> (64 - 8 * i))));
                    }
                    fw.write(bbuff);
                }
            }
            fw.close();
        } catch (IOException e) {
            return false;
        }
        return true;
    }
}
