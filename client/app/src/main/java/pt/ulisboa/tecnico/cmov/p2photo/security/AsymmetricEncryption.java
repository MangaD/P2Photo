package pt.ulisboa.tecnico.cmov.p2photo.security;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;

/**
 * Key store tutorial: http://tutorials.jenkov.com/java-cryptography/keystore.html
 */
public class AsymmetricEncryption {

    private Cipher cipher;

    private static final int keysize = 2048;

    public AsymmetricEncryption() throws NoSuchAlgorithmException, NoSuchPaddingException {
        this.cipher = Cipher.getInstance("RSA");
    }

    /**
     * Asymmetric encription
     */
    public static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        /**
         * Gets the type of algorithm to use in the key-pair generation. In this case it
         * is Elliptical Curves (EC), but could be RSA, ElGammal... EC is the key with
         * smaller size, more efficient with equivalent security.
         */
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        // SHA1PRNG is the standard to generate secure randoms
        SecureRandom random = SecureRandom.getInstance("SHA1PRNG");

        // Use 2048 key size
        keyGen.initialize(keysize, random);
        KeyPair keys = keyGen.generateKeyPair();
        //Key pub = kp.getPublic();
        //Key pvt = kp.getPrivate();
        return keys;
    }

    public static byte[] publicKeyToByteArray(PublicKey publicKey) {
        return publicKey.getEncoded();
    }

    public static PublicKey publicKeyFromByteArray(byte[] data)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(data);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePublic(keySpec);
    }

    public static byte[] privateKeyToByteArray(PrivateKey privateKey) {
        return privateKey.getEncoded();
    }

    public static PrivateKey privateKeyFromByteArray(byte[] data)
            throws NoSuchAlgorithmException, InvalidKeySpecException {
        PKCS8EncodedKeySpec ks = new PKCS8EncodedKeySpec(data);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return kf.generatePrivate(ks);
    }

    /**
     * https://stackoverflow.com/questions/31915617/how-to-encrypt-string-with-public-key-and-decrypt-with-private-key
     */
    public byte[] encrypt(PublicKey publicKey, byte[] inputData)
            throws Exception {
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        byte[] encryptedBytes = cipher.doFinal(inputData);
        return encryptedBytes;
    }

    public byte[] decrypt(PrivateKey privateKey, byte[] inputData)
            throws Exception {
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        byte[] decryptedBytes = cipher.doFinal(inputData);
        return decryptedBytes;
    }

    public static byte[] encryptPrivateKey(PrivateKey privateKey, String password)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException {

        // Generate key of 256 bits from key password entered by user
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] passwordHash = digest.digest(password.getBytes());

        Key aesKey = new SecretKeySpec(passwordHash, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        return cipher.doFinal(privateKeyToByteArray(privateKey));

    }

    public static PrivateKey decryptPrivateKey(byte[] encryptedKey, String password)
            throws NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException,
            BadPaddingException, IllegalBlockSizeException, InvalidKeySpecException {

        // Generate key of 256 bits from key password entered by user
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] passwordHash = digest.digest(password.getBytes());

        Key aesKey = new SecretKeySpec(passwordHash, "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        return privateKeyFromByteArray(cipher.doFinal(encryptedKey));
    }

}
