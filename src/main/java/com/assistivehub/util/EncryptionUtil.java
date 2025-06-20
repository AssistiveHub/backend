package com.assistivehub.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;

@Component
public class EncryptionUtil {

    private static final String ALGORITHM = "AES";
    private static final String TRANSFORMATION = "AES/ECB/PKCS5Padding";
    private static final int AES_KEY_LENGTH = 32; // AES-256 requires 32 bytes

    @Value("${encryption.secret.key:MySecretEncryptionKey2024!@#}")
    private String secretKey;

    /**
     * 키를 AES-256에 맞는 32바이트로 조정합니다.
     */
    private byte[] getAdjustedKey(String key) {
        try {
            // SHA-256 해시를 사용하여 32바이트 키 생성
            MessageDigest sha = MessageDigest.getInstance("SHA-256");
            byte[] keyBytes = sha.digest(key.getBytes(StandardCharsets.UTF_8));
            return Arrays.copyOf(keyBytes, AES_KEY_LENGTH);
        } catch (Exception e) {
            throw new RuntimeException("키 조정 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 텍스트를 암호화합니다.
     */
    public String encrypt(String plainText) {
        try {
            byte[] adjustedKey = getAdjustedKey(secretKey);
            SecretKeySpec secretKeySpec = new SecretKeySpec(adjustedKey, ALGORITHM);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);

            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);

        } catch (Exception e) {
            throw new RuntimeException("암호화 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 암호화된 텍스트를 복호화합니다.
     */
    public String decrypt(String encryptedText) {
        try {
            byte[] adjustedKey = getAdjustedKey(secretKey);
            SecretKeySpec secretKeySpec = new SecretKeySpec(adjustedKey, ALGORITHM);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);

            byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(decryptedBytes, StandardCharsets.UTF_8);

        } catch (Exception e) {
            throw new RuntimeException("복호화 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }

    /**
     * 안전한 랜덤 암호화 키를 생성합니다 (32바이트)
     */
    public static String generateSecretKey() {
        try {
            KeyGenerator keyGenerator = KeyGenerator.getInstance(ALGORITHM);
            keyGenerator.init(256);
            SecretKey secretKey = keyGenerator.generateKey();
            return Base64.getEncoder().encodeToString(secretKey.getEncoded());
        } catch (Exception e) {
            throw new RuntimeException("키 생성 중 오류가 발생했습니다: " + e.getMessage(), e);
        }
    }
}