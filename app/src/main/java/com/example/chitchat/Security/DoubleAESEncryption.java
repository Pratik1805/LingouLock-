package com.example.chitchat.Security;

import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class DoubleAESEncryption {

    private static SecretKeySpec secretKey;
    private static byte[] key;
    public static final String secret_Key = "ssshhhhhhhhhhh!!!!";
    public static void setKey(final String myKey) {
        MessageDigest sha = null;
        try {
            key = myKey.getBytes("UTF-8");
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");
        } catch (NoSuchAlgorithmException | UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public static String doubleEncrypt(final String strToEncrypt, final String secret) {
        try {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);

            // First encryption
            byte[] firstEncryptedBytes = cipher.doFinal(strToEncrypt.getBytes(StandardCharsets.UTF_8));

            // Second encryption
            byte[] secondEncryptedBytes = cipher.doFinal(firstEncryptedBytes);

            // Encode the final encrypted bytes as a Base64 string
            return Base64.getEncoder().encodeToString(secondEncryptedBytes);
        } catch (Exception e) {
            System.out.println("Error while double encrypting: " + e.toString());
        }
        return null;
    }

    public static String doubleDecrypt(final String strToDecrypt, final String secret) {
        try {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);

            // First decryption
            byte[] firstDecryptedBytes = cipher.doFinal(Base64.getDecoder().decode(strToDecrypt));

            // Second decryption
            byte[] secondDecryptedBytes = cipher.doFinal(firstDecryptedBytes);

            // Convert the final decrypted bytes to a string
            return new String(secondDecryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.out.println("Error while double decrypting: " + e.toString());
        }
        return null;
    }

//    public static void main(String[] args) {
//        final String secretKey = "ssshhhhhhhhhhh!!!!";
//
//        String originalString = "RADHE RADHE";
//        String doubleEncryptedString = doubleEncrypt(originalString, secretKey);
//
//        System.out.println("Original String: " + originalString);
//        System.out.println("Double Encrypted String: " + doubleEncryptedString);
//
//        String doubleDecryptedString = doubleDecrypt(doubleEncryptedString, secretKey);
//
//        System.out.println("Double Decrypted String: " + doubleDecryptedString);
//    }
}
