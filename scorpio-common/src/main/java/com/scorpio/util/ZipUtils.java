package com.scorpio.util;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public final class ZipUtils {

    private ZipUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * 将一批文件进行压缩
     *
     * @param sourceFiles   原文件集合
     * @param targetZipFile 压缩后目标文件
     * @throws IOException
     */
    public static void zipFiles(File[] sourceFiles, File targetZipFile) throws IOException {
        ZipOutputStream outputStream = null;
        try {
            outputStream = new ZipOutputStream(new FileOutputStream(targetZipFile));
            for (File file : sourceFiles) {
                try {
                    addEntry("", file, outputStream);
                } catch (Exception e) {
                    continue;
                }
            }
        } catch (Exception e) {
            throw new IOException(e);
        } finally {
            if (outputStream != null) {
                outputStream.close();
            }
        }
    }

    /**
     * 将文件写入到zip文件中
     *
     * @param source
     * @param outputstream
     * @throws IOException
     */
    private static void addEntry(String base, File source, ZipOutputStream outputstream)
            throws IOException {
        FileInputStream is = null;
        try {
            String entry = base + source.getName();
            if (source.isDirectory()) {
                for (File file : source.listFiles()) {
                    addEntry(entry + File.separator, file, outputstream);
                }
            } else {
                is = FileUtils.openInputStream(source);
                if (is != null) {
                    outputstream.putNextEntry(new ZipEntry(entry));

                    int len = 0;
                    byte[] buffer = new byte[10 * 1024];
                    while ((len = is.read(buffer)) > 0) {
                        outputstream.write(buffer, 0, len);
                        outputstream.flush();
                    }
                    outputstream.closeEntry();
                }
            }

        } finally {
            if (is != null) {
                is.close();
            }
        }
    }

}
