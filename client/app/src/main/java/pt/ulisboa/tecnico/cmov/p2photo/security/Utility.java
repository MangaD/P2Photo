package pt.ulisboa.tecnico.cmov.p2photo.security;

import android.util.Base64;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Utility {
    /**
     * For sending the keys as a string
     *
     * https://stackoverflow.com/questions/2418485/how-do-i-convert-a-byte-array-to-base64-in-java
     */
    public static String bytesToBase64(byte[] data) {
        return Base64.encodeToString(data, Base64.DEFAULT);
    }

    public static byte[] base64ToBytes(String base64) {
        return Base64.decode(base64, Base64.DEFAULT);
    }

    public static String passwordToSHA512Base64(String password) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        byte[] passwordHash = digest.digest(password.getBytes());
        return Utility.bytesToBase64(passwordHash);
    }
}
