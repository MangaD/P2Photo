package pt.ulisboa.tecnico.cmov.p2photo.security;

import android.util.Base64;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Symmetric encryption tutorial: https://www.baeldung.com/java-cipher-input-output-stream
 * Assymetric encryption tutorial: https://www.novixys.com/blog/how-to-generate-rsa-keys-java/
 * Why use EC: https://blog.cloudflare.com/ecdsa-the-digital-signature-algorithm-of-a-better-internet/
 */
public class SimmetricEncryption {

    private Cipher cipher;

    public SimmetricEncryption() throws NoSuchAlgorithmException, NoSuchPaddingException {
        this.cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    }

    /**
     * Symmetric encryption
     */
    private SecretKey generateAESKey() throws NoSuchAlgorithmException {
        return KeyGenerator.getInstance("AES").generateKey();
    }

    public static byte[] secretKeyToByteArray(SecretKey secretKey) {
        return secretKey.getEncoded();
    }

    public static SecretKey secretKeyFromByteArray(byte[] data) {
        return new SecretKeySpec(data, 0, data.length, "AES");
    }

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

    public byte[] encryptAES(String content, SecretKey secretKey)
            throws InvalidKeyException, IOException {
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] iv = cipher.getIV();

        try (ByteArrayOutputStream out = new ByteArrayOutputStream();
             CipherOutputStream cipherOut = new CipherOutputStream(out, cipher)) {
            out.write(iv);
            cipherOut.write(content.getBytes());
            return out.toByteArray();
        }
    }

    String decryptAES(byte[] encryptedBytes, SecretKey secretKey)
            throws IOException, InvalidKeyException, InvalidAlgorithmParameterException {

        String content;

        try (ByteArrayInputStream in = new ByteArrayInputStream(encryptedBytes)) {
            byte[] fileIv = new byte[16];
            in.read(fileIv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(fileIv));

            try (
                    CipherInputStream cipherIn = new CipherInputStream(in, cipher);
                    InputStreamReader inputReader = new InputStreamReader(cipherIn);
                    BufferedReader reader = new BufferedReader(inputReader)
            ) {

                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                content = sb.toString();
            }

        }
        return content;
    }
}
