package com.scorpio.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class HMACSHA256Utils {

    /**
     * sha256_HMAC加密
     *
     * @param message 消息
     * @param secret  秘钥
     * @return 加密后字符串
     */
    public static String sha256_HMAC(String message, String secret) {
        String hash = "";
        try {
            Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
            SecretKeySpec secret_key = new SecretKeySpec(secret.getBytes(), "HmacSHA256");
            sha256_HMAC.init(secret_key);
            byte[] bytes = sha256_HMAC.doFinal(message.getBytes());
            hash = byteArrayToHexString(bytes);
        } catch (Exception e) {
            throw new UnsupportedOperationException(e);
        }

        return hash;
    }

    /**
     * 将加密后的字节数组转换成字符串
     *
     * @param bytes 字节数组
     * @return 字符串
     */
    public static String byteArrayToHexString(byte[] bytes) {
        StringBuilder hs = new StringBuilder();
        String stmp;
        for (int n = 0; bytes != null && n < bytes.length; n++) {
            stmp = Integer.toHexString(bytes[n] & 0XFF);
            if (stmp.length() == 1) hs.append('0');
            hs.append(stmp);
        }
        return hs.toString().toLowerCase();
    }

}
