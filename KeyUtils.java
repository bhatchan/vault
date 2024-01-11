// Utility Class to Convert Bytes to Public Keys (and vice versa)

import java.io.*;
import java.security.*;

public class KeyUtils {
    public static byte[] convertPublicKeyToBytes(PublicKey publicKey) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(publicKey);
        return bos.toByteArray();
    }

    public static PublicKey convertBytesToPublicKey(byte[] keyBytes) throws Exception {
        ByteArrayInputStream bis = new ByteArrayInputStream(keyBytes);
        ObjectInputStream ois = new ObjectInputStream(bis);
        return (PublicKey) ois.readObject();
    }
}
