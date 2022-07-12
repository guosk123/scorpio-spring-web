package com.scorpio.util;

import org.apache.commons.lang3.StringUtils;

import java.time.*;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;

public final class DateUtils {

    private DateUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static Date now() {
        return Date.from(LocalDateTime.now().atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date beforeSecondDate(Date date, int amount) {
        return Date.from(date.toInstant().minus(amount, ChronoUnit.SECONDS)
                .atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date afterSecondDate(Date date, int amount) {
        return Date.from(date.toInstant().plus(amount, ChronoUnit.SECONDS)
                .atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date beforeDayDate(Date date, int amount) {
        return Date.from(
                date.toInstant().minus(amount, ChronoUnit.DAYS).atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date afterDayDate(Date date, int amount) {
        return Date.from(
                date.toInstant().plus(amount, ChronoUnit.DAYS).atZone(ZoneId.systemDefault()).toInstant());
    }

    public static int dayOfWeek(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).getDayOfWeek().getValue();
    }

    public static int dayOfMonth(Date date) {
        return date.toInstant().atZone(ZoneId.systemDefault()).getDayOfMonth();
    }

    /*********************************************************************
     * convert Date to String
     **********************************************************************/

    public static String toStringFormat(Date date, String pattern, Locale locale) {
        if (date == null) {
            return "";
        }
        return date.toInstant().atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern(pattern, locale));
    }

    public static String toStringFormat(Date date, String pattern, ZoneId zoneId) {
        if (date == null) {
            return "";
        }
        return date.toInstant().atZone(zoneId).format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String toStringFormat(Date date, String pattern) {
        if (date == null) {
            return "";
        }
        return date.toInstant().atZone(ZoneId.systemDefault())
                .format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String toStringYYYYMM(Date date) {
        return toStringFormat(date, "yyyy-MM");
    }

    public static String toStringYYYYMMDD(Date date) {
        return toStringFormat(date, "yyyy-MM-dd");
    }

    public static String toStringYYYYMMDDHHMMSS(Date date) {
        return toStringFormat(date, "yyyy-MM-dd HH:mm:ss");
    }

    public static String toStringYYYYMMDDHHMMSS(Date date, ZoneId zoneId) {
        return date.toInstant().atZone(zoneId)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static String toStringYYYYMMDDHHMMSS(OffsetDateTime date, ZoneId zoneId) {
        return date.toInstant().atZone(zoneId)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static String toStringISO8601(Date date) {
        return toStringFormat(date, "yyyy-MM-dd'T'HH:mm:ssXXX");
    }

    public static String toStringISO8601(OffsetDateTime date, ZoneId zoneId) {
        return date.toInstant().atZone(zoneId)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"));
    }

    public static String toStringNanoISO8601(OffsetDateTime date, ZoneId zoneId) {
        return date.toInstant().atZone(zoneId)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSXXX"));
    }

    /*********************************************************************
     * parse String to Date
     **********************************************************************/

    public static Date parseYYYYMMDDDate(String strDate) {
        return parseYYYYMMDDDate(strDate, "yyyy-MM-dd");
    }

    public static Date parseYYYYMMDDDate(String strDate, String pattern) {
        LocalDate date = LocalDate.parse(strDate, DateTimeFormatter.ofPattern(pattern));
        return Date.from(date.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static Date parseYYYYMMDDHHMMSSDate(String strDate) {
        LocalDateTime dateTime = LocalDateTime.parse(strDate,
                DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static Date parseISO8601Date(String strDate) {
        ZonedDateTime dateTime = ZonedDateTime.parse(strDate,
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"));
        return Date.from(dateTime.toInstant());
    }

    public static Date parseNanoISO8601Date(String strDate) {
        ZonedDateTime dateTime = ZonedDateTime.parse(strDate,
                DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSXXX"));
        return Date.from(dateTime.toInstant());
    }

    /*********************************************************************
     * transform String to Another String Format
     **********************************************************************/
    public static String transformDateString(String strDate, String oldPattern, ZoneId oldZoneId,
                                             String newPattern, ZoneId newZoneId) {
        if (StringUtils.isBlank(strDate)) {
            return "";
        }
        LocalDateTime oldLocalDateTime = LocalDateTime.parse(strDate,
                DateTimeFormatter.ofPattern(oldPattern));
        ZonedDateTime oldZonedDateTime = ZonedDateTime.of(oldLocalDateTime, oldZoneId);
        oldZonedDateTime.format(DateTimeFormatter.ofPattern(newPattern));
        ZonedDateTime newZonedDateTime = ZonedDateTime.ofInstant(oldZonedDateTime.toInstant(),
                newZoneId);
        return newZonedDateTime.format(DateTimeFormatter.ofPattern(newPattern));
    }

    public static String transformDateString(String strDate, String oldPattern, String newPattern,
                                             ZoneId newZoneId) {
        if (StringUtils.isBlank(strDate)) {
            return "";
        }
        ZonedDateTime oldZonedDateTime = ZonedDateTime.parse(strDate,
                DateTimeFormatter.ofPattern(oldPattern));
        oldZonedDateTime.format(DateTimeFormatter.ofPattern(newPattern));
        ZonedDateTime newZonedDateTime = ZonedDateTime.ofInstant(oldZonedDateTime.toInstant(),
                newZoneId);
        return newZonedDateTime.format(DateTimeFormatter.ofPattern(newPattern));
    }

    public static String transformDateString(String strDate, String oldPattern, String newPattern) {
        ZoneId zoneId = ZoneId.systemDefault();
        return transformDateString(strDate, oldPattern, zoneId, newPattern, zoneId);
    }

    /**
     * @param strDate yyyy-MM-dd HH:mm:ss.SSSSSSSSS
     * @param zoneId
     * @return
     */
    public static String transformNanoDateStringToISO8601(String strDate, ZoneId zoneId) {
        return transformDateString(strDate, "yyyy-MM-dd HH:mm:ss.SSSSSSSSS", zoneId,
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSXXX", zoneId);
    }

    /**
     * @param strDate   yyyy-MM-dd HH:mm:ss.SSSSSSSSS
     * @param oldZoneId
     * @param newZoneId
     * @return
     */
    public static String transformNanoDateStringToISO8601(String strDate, ZoneId oldZoneId,
                                                          ZoneId newZoneId) {
        return transformDateString(strDate, "yyyy-MM-dd HH:mm:ss.SSSSSSSSS", oldZoneId,
                "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSXXX", newZoneId);
    }

}
