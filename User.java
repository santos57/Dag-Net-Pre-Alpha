import java.util.Arrays;

public class User implements Comparable {
    //we need 256 bits for RSA so just have a bunch of long boys
    private long[] pKey;
    public static int LEN = 256;

    public User(long[] privateKey) {
        pKey = Arrays.copyOf(privateKey, LEN / 64 + (64 - LEN % 64) % 64);
    }

    public User(long pk0, long pk1, long pk2, long pk3) {
        pKey = new long[4];
        pKey[0] = pk0;
        pKey[1] = pk1;
        pKey[2] = pk2;
        pKey[3] = pk3;
    }

    public long[] getpKey() {
        return pKey;
    }

    //obviously this should actually encrypt the data using RSA
    public short[] decrypt(short[] message) {
        return message;
    }

    public short[] encrypt(short[] message) {
        return message;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof User) {
            return ((User) obj).pKey[0] == pKey[0] &&
                    ((User) obj).pKey[1] == pKey[1] &&
                    ((User) obj).pKey[2] == pKey[2] &&
                    ((User) obj).pKey[3] == pKey[3];
        }
        return false;
    }

    @Override
    public int compareTo(Object o) {
        if (o instanceof User) {
            for (int i = 0; i < LEN / 64 + (64 - LEN % 64) % 64; i++) {
                if (((User) o).pKey[i] > pKey[i]) {
                    return -1;
                }
                if (((User) o).pKey[i] < pKey[i]) {
                    return 1;
                }
            }
            return 0;
        }
        return -1;
    }
}
