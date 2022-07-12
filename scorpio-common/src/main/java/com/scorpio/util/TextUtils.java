package com.scorpio.util;

import com.google.common.collect.Sets;
import com.scorpio.Constants;
import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang3.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TextUtils {

    private static final Pattern ILLEGAL_CHARACTER_PATTERN = Pattern
            .compile("[*?><;&!//\'\"\\`\\(\\)\\{\\}|]{1}");

    private TextUtils() {
        throw new IllegalStateException("Utility class");
    }


    public static boolean toBoolean(String text) {

        if (text.length() == 1) {
            char ch0 = text.charAt(0);
            if (ch0 == '0') {
                return false;
            }
            if (ch0 == '1') {
                return true;
            }
        }
        return BooleanUtils.toBoolean(text);
    }

    public static String toUtf8String(String text) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c >= 0 && c <= 127) { // 单字节字符在UTF-8的表示为01111111，即0~127（原来的代码这里是255）
                builder.append(c);
            } else {
                byte[] b = Character.toString(c).getBytes(StandardCharsets.UTF_8);

                for (int j = 0; j < b.length; j++) {
                    // x & 0xFF, this operation converts byte to int, dropping the sign.
                    builder.append('%').append(Integer.toHexString(b[j] & 255).toUpperCase(Locale.US));
                }
            }
        }
        return builder.toString();
    }

    public static String byte2Hex(byte[] paramArrayOfByte) {
        StringBuilder builder = new StringBuilder();
        String str = "";
        for (int i = 0; i < paramArrayOfByte.length; ++i) {
            str = Integer.toHexString(paramArrayOfByte[i] & 0xFF);
            if (str.length() == 1) {
                builder.append('0');
            }
            builder.append(str);
        }
        return builder.toString().toUpperCase(Locale.US);
    }

    public static String underLineToCamel(String text) {

        if (text == null) {
            return text;
        }
        int length = text.length();
        StringBuilder result = new StringBuilder(length);
        int resultLength = 0;
        boolean wasPrevUnderLine = false;
        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);
            if (c == '_') {
                wasPrevUnderLine = true;
                continue;
            }
            if (wasPrevUnderLine) {
                c = Character.toUpperCase(c);
                wasPrevUnderLine = false;
            }
            result.append(c);
            resultLength += 1;
        }
        return resultLength > 0 ? result.toString() : text;

    }

    public static String camelToUnderLine(String text) {
        if (text == null) {
            return text;
        }

        String regex = "([A-Z])";
        Matcher matcher = Pattern.compile(regex).matcher(text);
        while (matcher.find()) {
            String target = matcher.group();
            text = text.replaceAll(target, "_" + target.toLowerCase());
        }
        return text;
    }

    public static String matchingIllegalCharacters(String text) {
        Set<String> illegalCharacters = Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE);

        Matcher matcher = ILLEGAL_CHARACTER_PATTERN.matcher(text);
        while (matcher.find()) {
            illegalCharacters.add(matcher.group());
        }

        return StringUtils.join(illegalCharacters, ",");
    }
}
