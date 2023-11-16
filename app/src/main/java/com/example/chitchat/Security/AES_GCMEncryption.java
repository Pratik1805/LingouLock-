package com.example.chitchat.Security;

import java.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AES_GCMEncryption {
    private static final String KEY_STRING = "R#2aH7eK*9g@T$p1";
    public static String encrypt(String plainText)  {

        try{
            byte[] keyBytes = KEY_STRING.getBytes("UTF-8");
            SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");

            byte[] iv = new byte[12];


            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey, new GCMParameterSpec(128, iv));

            byte[] cipherText = cipher.doFinal(plainText.getBytes("UTF-8"));

            // Combine IV and ciphertext into a single string (you may use a delimiter)
            String ivAndCipherTextBase64 = Base64.getEncoder().encodeToString(iv) + ":" +
                    Base64.getEncoder().encodeToString(cipherText);

            return ivAndCipherTextBase64;
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String ivAndCipherTextBase64) {
        try{
            byte[] keyBytes = KEY_STRING.getBytes("UTF-8");
            SecretKey secretKey = new SecretKeySpec(keyBytes, "AES");

            String[] parts = ivAndCipherTextBase64.split(":");
            byte[] iv = Base64.getDecoder().decode(parts[0]);
            byte[] cipherText = Base64.getDecoder().decode(parts[1]);

            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.DECRYPT_MODE, secretKey, new GCMParameterSpec(128, iv));

            byte[] plainTextBytes = cipher.doFinal(cipherText);

            return new String(plainTextBytes, "UTF-8");
        }
        catch(Exception e){
            e.printStackTrace();
        }
        return null;
    }
}
