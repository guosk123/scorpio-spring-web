package com.scorpio.util;

import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Base64;


public final class Base64Utils {

    private Base64Utils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * @param content
     * @return
     */
    public static String decode(String content) {

        if (StringUtils.isBlank(content)) {
            return "";
        }

        return new String(Base64.getDecoder().decode(content), StandardCharsets.UTF_8);
    }

    /**
     * @param content
     * @return
     */
    public static String encode(String content) {

        if (StringUtils.isBlank(content)) {
            return "";
        }

        return new String(Base64.getEncoder().encode(content.getBytes(StandardCharsets.UTF_8)),
                StandardCharsets.UTF_8);
    }
}
