package com.scorpio.rest.common;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Date;

import org.springframework.stereotype.Component;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;

@Component
public class SignatureUtil {

    private static final Integer APP_CONFIG_LENGTH = 3;

    private SignatureUtil() {
    }

    /**
     * 管理接口签名
     *
     * @param appkey
     * @param appToken
     * @param timestamp
     * @return
     */
    public static String generateManageApiSig(String appkey, String appToken, String timestamp) {
        String[] strarray = new String[APP_CONFIG_LENGTH];
        strarray[0] = appkey;
        strarray[1] = appToken;
        strarray[2] = timestamp;
        Arrays.sort(strarray);

        StringBuilder sb = new StringBuilder();
        for (String s : strarray) {
            sb.append(s);
        }

        String signature = Hashing.sha512().hashString(sb, StandardCharsets.UTF_8).toString();

        return signature;
    }

    /**
     * 业务接口签名
     *
     * @param iso8601Time
     * @param method
     * @param url
     * @return
     */
    public static String generateSignature(String iso8601Time, String method, String url) {
        StringBuilder strarray = new StringBuilder();

        strarray.append(iso8601Time).append("\n");
        strarray.append(PropertiesUtil.getProperty("machloop.iosp.clientId")).append("\n");
        strarray.append(method).append("\n");
        strarray.append(url);

        HashFunction hmacSha1 = Hashing
                .hmacSha1(PropertiesUtil.getProperty("machloop.iosp.client.appkey").getBytes());
        String signature = hmacSha1.hashString(strarray.toString(), StandardCharsets.UTF_8).toString();

        return signature;
    }

}
