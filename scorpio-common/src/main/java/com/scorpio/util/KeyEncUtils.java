package com.scorpio.util;

import org.apache.commons.lang3.StringUtils;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public final class KeyEncUtils {

    // 初始向量（偏移）
    public static final String VIPARA = "aabbccddeeffgghh";

    private KeyEncUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String encrypt(String key, String content) {
        return encryptOrDecrypt(key, Cipher.ENCRYPT_MODE, content);
    }

    public static String decrypt(String key, String content) {
        return encryptOrDecrypt(key, Cipher.DECRYPT_MODE, content);
    }

    public static String encryptOrDecrypt(String key, int mode, String content) {
        if (StringUtils.isBlank(content)) {
            return "";
        }

        try {
            SecretKeySpec sks = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "AES");
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            if (mode == Cipher.ENCRYPT_MODE) {
                IvParameterSpec zeroIv = new IvParameterSpec(VIPARA.getBytes(StandardCharsets.UTF_8));
                cipher.init(Cipher.ENCRYPT_MODE, sks, zeroIv);
                byte[] result = cipher.doFinal(content.getBytes(StandardCharsets.UTF_8));
                byte[] base64Data = Base64.getEncoder().encode(result);
                return new String(base64Data, StandardCharsets.UTF_8);
            } else if (mode == Cipher.DECRYPT_MODE) {
                byte[] encryptedBase64Bytes = content.getBytes(StandardCharsets.UTF_8);
                byte[] byteMi = Base64.getDecoder().decode(encryptedBase64Bytes);
                IvParameterSpec zeroIv = new IvParameterSpec(VIPARA.getBytes(StandardCharsets.UTF_8));
                cipher.init(Cipher.DECRYPT_MODE, sks, zeroIv);
                byte[] result = cipher.doFinal(byteMi);
                return new String(result, StandardCharsets.UTF_8);
            } else {
                throw new UnsupportedOperationException("parameter mode is invalid.");
            }
        } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException
                 | IllegalBlockSizeException | BadPaddingException |
                 InvalidAlgorithmParameterException e) {
            throw new UnsupportedOperationException(e);
        }
    }
}
