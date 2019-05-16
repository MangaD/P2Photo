package pt.ulisboa.tecnico.cmov.p2photo.security;

import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;

public class AsymmetricEncryption {

    private Cipher cipher;

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
        keyGen.initialize(2048, random);
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
}
