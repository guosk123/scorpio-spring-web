package com.scorpio.util;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.scorpio.Constants;
import org.apache.commons.lang.StringUtils;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class CsvUtils {

    private static final Pattern SPLIT_PATTERN = Pattern.compile("`((?:[^`]|((?<=\\\\)(?:`)))+)`|``",
            Pattern.MULTILINE);

    private CsvUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 将CSV（Comma-seprate Value）转换为List
     *
     * @param valueCSV
     * @return
     */
    public static List<String> convertCSVToList(String valueCSV) {

        if (StringUtils.isBlank(valueCSV)) {
            return Lists.newArrayListWithCapacity(0);
        }

        String[] valueStrAry = valueCSV.split(",");
        List<String> valueList = Lists.newArrayListWithCapacity(valueStrAry.length);
        for (String valueStr : valueStrAry) {
            if (StringUtils.isBlank(valueCSV)) {
                continue;
            }

            valueList.add(valueStr);
        }

        return valueList;
    }

    /**
     * 将CSV（Comma-seprate Value）转换为Set
     *
     * @param valueCSV
     * @return
     */
    public static Set<String> convertCSVToSet(String valueCSV) {

        if (StringUtils.isBlank(valueCSV)) {
            return Sets.newHashSetWithExpectedSize(0);
        }

        String[] valueStrAry = valueCSV.split(",");
        Set<String> valueList = Sets.newHashSetWithExpectedSize(valueStrAry.length);
        for (String valueStr : valueStrAry) {
            if (StringUtils.isBlank(valueCSV)) {
                continue;
            }

            valueList.add(valueStr);
        }

        return valueList;
    }

    public static String convertCollectionToCSV(Iterable<String> valueCollection) {

        StringBuilder valueCSV = new StringBuilder();

        for (String value : valueCollection) {
            valueCSV.append(value).append(',');
        }
        if (valueCSV.length() > 0) {
            valueCSV.deleteCharAt(valueCSV.length() - 1);
        }
        return valueCSV.toString();
    }

    /**
     * 导出CSV时，字段内容由`包裹，如果字段内容有` 将`转义为\`，有\r\n转义为\\r\\n
     *
     * @param fields
     * @return
     */
    public static String spliceRowData(String... fields) {
        StringBuilder valueStr = new StringBuilder();

        for (String field : fields) {
            if (field == null) {
                field = "";
            }

            valueStr.append("`")
                    .append(StringUtils.replace(StringUtils.replace(field, "`", "\\`"), "\r\n", "\\r\\n"))
                    .append("`,");
        }

        if (valueStr.length() > 0) {
            valueStr.setCharAt(valueStr.length() - 1, '\n');
        }

        return valueStr.toString();
    }

    /**
     * 导入CSV时，行转义
     *
     * @param line
     * @return
     */
    public static List<String> splitRowData(String line) {
        List<String> fieldList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

        if (StringUtils.isBlank(line)) {
            return fieldList;
        }

        Matcher matcher = SPLIT_PATTERN.matcher(line);
        while (matcher.find()) {
            String fieldContext = StringUtils
                    .substringBeforeLast(StringUtils.substringAfter(matcher.group(), "`"), "`");
            fieldList.add(
                    StringUtils.replace(StringUtils.replace(fieldContext, "\\`", "`"), "\\r\\n", "\r\n"));
        }

        return fieldList;
    }
}
