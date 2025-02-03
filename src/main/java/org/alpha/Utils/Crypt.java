package org.alpha.Utils;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Base64;

public class Crypt {

    private static SecretKeySpec secretKey;

    public static void setKey(final String myKey) {
        MessageDigest sha = null;
        try {
            byte[] key = myKey.getBytes(StandardCharsets.UTF_8);
            sha = MessageDigest.getInstance("SHA-1");
            key = sha.digest(key);
            key = Arrays.copyOf(key, 16);
            secretKey = new SecretKeySpec(key, "AES");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public static String encrypt(final String strToEncrypt, final String secret) {
        try {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            return Base64.getEncoder()
                    .encodeToString(cipher.doFinal(strToEncrypt.getBytes("UTF-8")));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e.toString());
        }
        return null;
    }

    public static String decrypt(final String strToDecrypt, final String secret) {
        try {
            setKey(secret);
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            return new String(cipher.doFinal(Base64.getDecoder()
                    .decode(strToDecrypt)));
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e.toString());
        }
        return null;
    }

    public static void main(String[] args) {
        /*String encrypt = encrypt("/api/otpserviceV2/restricted/details/all", "c676b160-44a5-483e-87fd-74e604c65577");
        System.out.println("encrypt = " + encrypt);*/

        String decrypt = decrypt("tWtLqf20+euhm53d+FQrN1EFvX+BmGSw2KDUbvvwipvTC3eggy6tgU6wVn29rZUx", "c676b160-44a5-483e-87fd-74e604c65577");
        String decrypt1 = decrypt("Sm5MX+ONFgfjeKhz/GQxkbMJcFj41sNq+XhnoVQL1GWf7ovlQHs8h/5+ip7A4BVt", "c676b160-44a5-483e-87fd-74e604c65577");
        System.out.println("decrypt = " + decrypt + " " +decrypt1);

    }
}
