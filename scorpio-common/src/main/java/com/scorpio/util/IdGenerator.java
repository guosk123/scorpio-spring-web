package com.scorpio.util;

import com.scorpio.algorithm.uuid.TimeBasedUuid;

public final class IdGenerator {

    private static final TimeBasedUuid TIME_UUID_GENERATOR = new TimeBasedUuid();

    private IdGenerator() {
        throw new IllegalStateException("Utility class");
    }

    public static String generateUUID() {
        return TIME_UUID_GENERATOR.getBase64UUID();
    }

}
