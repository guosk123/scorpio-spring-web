package com.scorpio.util;

import org.apache.commons.lang.StringUtils;

import java.util.IllegalFormatException;

public class UnitConversionUtils {
    public static String converseCapacity(String capacityStr) {
        String valueStr;
        String unit;
        if (StringUtils.containsIgnoreCase(capacityStr, "PB")) {
            valueStr = StringUtils.remove(capacityStr, "PB");
            unit = "PB";
        } else if (StringUtils.containsIgnoreCase(capacityStr, "TB")) {
            valueStr = StringUtils.remove(capacityStr, "TB");
            unit = "TB";
        } else if (StringUtils.containsIgnoreCase(capacityStr, "GB")) {
            valueStr = StringUtils.remove(capacityStr, "GB");
            unit = "GB";
        } else if (StringUtils.containsIgnoreCase(capacityStr, "MB")) {
            valueStr = StringUtils.remove(capacityStr, "MB");
            unit = "MB";
        } else {
            return "";
        }
        try {
            double valueDouble = Double.parseDouble(StringUtils.trim(valueStr));
            double resultDouble = converseCapacity(unit, valueDouble);
            return String.format("%.3f %s", resultDouble, unit);
        } catch (NullPointerException | NumberFormatException | IllegalFormatException e) {
            return "";
        }
    }

    private static double converseCapacity(String unit, double value) {
        double result = 0;
        switch (unit) {
            case ("PB"):
                result = value * 1024 * 1024 * 1024 * 1024 * 1024 / 1000 / 1000 / 1000 / 1000 / 1000;
                break;
            case ("TB"):
                result = value * 1024 * 1024 * 1024 * 1024 / 1000 / 1000 / 1000 / 1000;
                break;
            case ("GB"):
                result = value * 1024 * 1024 * 1024 / 1000 / 1000 / 1000;
                break;
            case ("MB"):
                result = value * 1024 * 1024 / 1000 / 1000;
                break;
        }
        return result;
    }
}
