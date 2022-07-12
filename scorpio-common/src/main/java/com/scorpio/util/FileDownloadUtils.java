package com.scorpio.util;

import com.scorpio.Constants;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.commons.lang.StringUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public final class FileDownloadUtils {

    private FileDownloadUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * @param userAgent
     * @param fileName
     * @return
     */
    public static String fileNameToUtf8String(String userAgent, String fileName) {

        boolean isFireFox = StringUtils.containsIgnoreCase(userAgent, "firefox");
        if (isFireFox) {
            fileName = new String(fileName.getBytes(StandardCharsets.UTF_8), StandardCharsets.ISO_8859_1);
        } else {
            fileName = TextUtils.toUtf8String(fileName);
            if (StringUtils.containsIgnoreCase(userAgent, "MSIE") && fileName.length() > 150) {
                // see http://support.microsoft.com/default.aspx?kbid=816868

                // 根据request的locale 得出可能的编码
                fileName = new String(fileName.getBytes(StandardCharsets.UTF_8),
                        StandardCharsets.ISO_8859_1);
            }
        }
        return fileName;
    }

    /**
     * 下载文件
     *
     * @param request
     * @param response
     * @param file
     * @param fileTrueName
     * @throws IOException
     */
    public static void downloadFile(HttpServletRequest request, HttpServletResponse response,
                                    File file, String fileTrueName) throws IOException {

        int length = (int) file.length();
        if (length > 0) {
            response.setContentType("application/octet-stream");
            response.addHeader("Content-Disposition", "attachment; filename="
                    + FileDownloadUtils.fileNameToUtf8String(request.getHeader("User-Agent"), fileTrueName));
            response.resetBuffer();
            response.setContentLength(length);
            byte[] buf = new byte[Constants.BLOCK_DEFAULT_SIZE];
            int readLength;
            ServletOutputStream servletOS = response.getOutputStream();

            try (InputStream inStream = java.nio.file.Files.newInputStream(file.toPath())) {
                while ((readLength = inStream.read(buf)) != -1) {
                    servletOS.write(buf, 0, readLength);
                }
            }

            servletOS.flush();
            servletOS.close();
            response.flushBuffer();
        }
    }
}
