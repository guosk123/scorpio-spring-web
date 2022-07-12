package com.scorpio.util;

import com.google.common.hash.Hashing;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public final class TokenUtils {

    private TokenUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String makeSignature(String token, String... param) {

        // 将token与其他参数拼接为一个array
        String[] array = new String[1 + param.length];
        array[0] = token;
        System.arraycopy(param, 0, array, 1, param.length);

        // 进行字典排序
        Arrays.sort(array);

        // 生成字符串
        StringBuilder content = new StringBuilder();
        for (int i = 0; i < array.length; i++) {
            content.append(array[i]);
        }

        // 摘要
        return Hashing.sha512().hashString(content, StandardCharsets.UTF_8).toString();
    }
}
