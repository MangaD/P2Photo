package pt.ulisboa.tecnico.cmov.p2photo.security;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
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
public class SymmetricEncryption {

    private Cipher cipher;

    public SymmetricEncryption() throws NoSuchAlgorithmException, NoSuchPaddingException {
        this.cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
    }

    /**
     * Symmetric encryption
     */
    public static SecretKey generateAESKey() throws NoSuchAlgorithmException {
        return KeyGenerator.getInstance("AES").generateKey();
    }

    public static byte[] secretKeyToByteArray(SecretKey secretKey) {
        return secretKey.getEncoded();
    }

    public static SecretKey secretKeyFromByteArray(byte[] data) {
        return new SecretKeySpec(data, 0, data.length, "AES");
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

    byte[] decryptAES(byte[] encryptedBytes, SecretKey secretKey)
            throws IOException, InvalidKeyException, InvalidAlgorithmParameterException {

        try (ByteArrayInputStream in = new ByteArrayInputStream(encryptedBytes);
             CipherInputStream cipherIn = new CipherInputStream(in, cipher)) {
            byte[] fileIv = new byte[16];
            in.read(fileIv);
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(fileIv));
            byte[] content = new byte[encryptedBytes.length-16];
            cipherIn.read(content);
            return content;
        }
    }
}
