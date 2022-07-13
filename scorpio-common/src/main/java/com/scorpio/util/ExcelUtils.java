package com.scorpio.util;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.google.common.collect.Lists;
import com.scorpio.Constants;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

public class ExcelUtils {

    public static void main(String[] args) throws IOException {
        // 创建文件
        File tempDir = new File("/home/xxx");
        File tempFile = Paths.get(tempDir.getAbsolutePath(), IdGenerator.generateUUID()).toFile();
        tempFile.createNewFile();

        List<List<String>> heads = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        List<List<String>> dataset = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

        // write
        write(tempFile, "test1", heads, dataset);

        // keepWriting
        ExcelWriter excelWriter = EasyExcel.write(tempFile).head(heads).build();
        WriteSheet writeSheet = EasyExcel.writerSheet("test2").build();
        for (int i = 0; i < 3; i++) {
            keepWriting(excelWriter, writeSheet, dataset);
        }
        if (excelWriter != null) {
            excelWriter.close();
        }
    }

    public static void write(File file, String sheetName, List<List<String>> heads, List<List<String>> dataset) {
        EasyExcel.write(file).head(heads).sheet(sheetName).doWrite(dataset);
    }

    public static void keepWriting(ExcelWriter excelWriter, WriteSheet writeSheet, List<List<String>> dataset) {
        excelWriter.write(dataset, writeSheet);
    }
}
