import java.util.ArrayList;
import java.util.Arrays;

public class Block {
    static int minTargets = 0;
    static final int headerSize = 328;
    static final int targetSize = 328;
    static final int hashSize = 64;//real hashes are gonna be longer than this but uhhhhhhhhh lmao not for a hackathon

    static int minSecurity = 1;

    byte secLevel;
    long time;
    long[] miner;
    Target[] targets;
    byte[][] riderData;
    int logicalSize;
    long[] hash = new long[4];
    boolean voting;
    boolean commenting;

    long[] rawData;

    Block(long[] rawData) {
        this.rawData = rawData;
    }

    void generateRaw() {
        if (rawData != null)
            return;
        rawData = new long[8];
        int count = 0;
        putRaw(time, count++);
        for (int i = 0; i < 4; i++) {
            putRaw(miner[i], count++);
        }
        putRaw(targets.length, count++);
        for (Target t : targets) {
            putRaw(t.time, count++);
            for (int i = 0; i < 4; i++) {
                putRaw(t.hash[i], count++);
            }
        }
        int i = 0;
        int j = 0;
        boolean stopper = true;
        while (i < riderData.length && stopper) {
            byte a = riderData[i][j++];
            if (j >= riderData[i].length) {
                ++i;
                j = 0;
                if (riderData[i][0] == 0) {
                    stopper = false;
                }
            }
            byte b = riderData[i][j++];
            if (j >= riderData[i].length) {
                ++i;
                j = 0;
                if (riderData[i][0] == 0) {
                    stopper = false;
                }
            }
            byte c = riderData[i][j++];
            if (j >= riderData[i].length) {
                ++i;
                j = 0;
                if (riderData[i][0] == 0) {
                    stopper = false;
                }
            }
            byte d = riderData[i][j++];
            if (j >= riderData[i].length) {
                ++i;
                j = 0;
                if (riderData[i][0] == 0) {
                    stopper = false;
                }
            }
            byte e = riderData[i][j++];
            if (j >= riderData[i].length) {
                ++i;
                j = 0;
                if (riderData[i][0] == 0) {
                    stopper = false;
                }
            }
            byte f = riderData[i][j++];
            if (j >= riderData[i].length) {
                ++i;
                j = 0;
                if (riderData[i][0] == 0) {
                    stopper = false;
                }
            }

            byte g = riderData[i][j++];
            if (j >= riderData[i].length) {
                ++i;
                j = 0;
                if (riderData[i][0] == 0) {
                    stopper = false;
                }
            }
            byte h = riderData[i][j++];
            if (j >= riderData[i].length) {
                ++i;
                j = 0;
                if (riderData[i][0] == 0) {
                    stopper = false;
                }
            }
            putRaw(makeLong(a, b, c, d, e, f, g, h), count++);
        }
        for (int k = 0; k < 4; k++) {
            putRaw(hash[k], count++);
        }
        rawData = Arrays.copyOf(rawData, count);
        rawData[rawData.length - 1] = hash[3];
    }

    private long makeLong(byte a, byte b, byte c, byte d, byte e, byte f, byte g, byte h) {
        long val = Byte.toUnsignedLong(a) << 56;
        val += Byte.toUnsignedLong(b) << 48;
        val += Byte.toUnsignedLong(c) << 40;
        val += Byte.toUnsignedLong(d) << 32;
        val += Byte.toUnsignedLong(e) << 24;
        val += Byte.toUnsignedLong(f) << 16;
        val += Byte.toUnsignedLong(g) << 8;
        val += Byte.toUnsignedLong(h);
        return val;
    }

    private void putRaw(long v, int count) {
        if (count >= rawData.length) {
            rawData = Arrays.copyOf(rawData, rawData.length * 2);
        }
        rawData[count] = v;
    }

    byte getSecurityLevel(long[] rawData) {
        //learn more about security
        //please for the love of god use a real cryptographic hash to do this lmao
        //i just dont know enough right now myslef and my teammates that did know
        //enough didnt get invited to boilermake :((((((
        //like seriously this is not security, it just checks that the last x bits of the hash are 0s
        //and doesnt even bother hashing the data using a hash funciton or anything. its actually just a meme right now.
        //it doesnt even use the whole hash. just the last 64 bits
        //its seriously no good dont use this
        byte level = 0;
        long hash = rawData[rawData.length - 1];
        while (hash % 2 != 0) {
            hash >>>= 1;
            ++level;
        }
        return level;
    }

    boolean parseRaw() {
        int indexCounter = 6;
        try {
            //no raw data to parse
            if (rawData == null) {
                return false;
            }
            secLevel = getSecurityLevel(rawData);
            for (int i = 1; i <= 4; i++) {
                hash[4 - i] = rawData[rawData.length - i];
            }
            //we dont fucking care about this block cause it's not done enough work
            if (secLevel < minSecurity) {
                return false;
            }
            //eat the time long
            time = rawData[0];
            //eat the 4 miner longs;
            miner = Arrays.copyOfRange(rawData, 1, 5);
            byte targetCount = (byte) (rawData[5]);
            if (targetCount < minTargets) {
                return false;
            }
            targets = new Target[targetCount];
            for (int i = 0; i < targetCount; i++) {
                targets[i] = new Target();
                targets[i].time = rawData[indexCounter++];
                for (int j = 0; j < 4; j++) {
                    targets[i].hash[j] = rawData[indexCounter++];
                }
            }

        } catch (IndexOutOfBoundsException e) {
            return false;
        }
        //all other data should be rider data now.
        int offset = 0;
        riderData = new byte[1][];
        try {
            while (indexCounter < rawData.length) {
                offset = (offset + 8);
                while (offset > 64) {
                    ++indexCounter;
                    offset -= 64;
                }
                byte riderType = (byte) (rawData[indexCounter] >>> (64 - offset));
                if (riderType == 0) {
                    byte[] hData = new byte[1 + 4 * 8];  //1 to save the rider type. 4 longs to save the hash
                    hData[0] = riderType;
                    offset = 0;
                    ++indexCounter;
                    for (int i = 1; i <= 4 * 8; i++) {
                        offset = (offset + 8);
                        while (offset > 64) {
                            ++indexCounter;
                            offset -= 64;
                        }
                        hData[i] = (byte) (rawData[indexCounter] >>> (64 - offset));
                    }
                    addGrow(hData);
                    break;
                }
                if (riderType == 1) {
                    byte[] hData = new byte[1 + 4 + 8 + 4 * 8 + 4 * 8 + 4 * 8];//rider type, weight, target block time, target block hash, target comment signature, voter signature
                    hData[0] = riderType;
                    for (int i = 1; i <= 4 + 8 + 4 * 8 + 4 * 8 + 4 * 8; i++) {
                        offset = (offset + 8);
                        while (offset > 64) {
                            ++indexCounter;
                            offset -= 64;
                        }
                        hData[i] = (byte) (rawData[indexCounter] >>> (64 - offset));
                    }
                    voting = true;
                    addGrow(hData);
                }
                if (riderType == 2) {
                    int msgLen = 0;
                    byte[] temp = new byte[5];
                    temp[0] = riderType;
                    for (int i = 1; i <= 4; i++) {
                        offset = (offset + 8);
                        while (offset > 64) {
                            ++indexCounter;
                            offset -= 64;
                        }
                        temp[i] = (byte) (rawData[indexCounter] >>> (64 - offset));
                        msgLen += temp[i] << (32 - (8 * i));
                    }
                    byte[] hData = Arrays.copyOf(temp, 1 + 4 + msgLen + 4 * 8);
                    for (int i = 0; i < msgLen + 4 * 8; i++) {
                        offset = (offset + 8);
                        while (offset > 64) {
                            ++indexCounter;
                            offset -= 64;
                        }
                        hData[i + 5] = (byte) (rawData[indexCounter] >>> (64 - offset));
                    }
                    addGrow(hData);
                    commenting = true;
                }
                //Other message types go here
            }
        } catch (IndexOutOfBoundsException e) {
            return false;
        }
        riderData = Arrays.copyOf(riderData, logicalSize);
        return true;
    }

    void addGrow(byte[] data) {
        if (riderData.length == logicalSize) {
            riderData = Arrays.copyOf(riderData, riderData.length * 2);
        }
        riderData[logicalSize++] = data;
    }
}
