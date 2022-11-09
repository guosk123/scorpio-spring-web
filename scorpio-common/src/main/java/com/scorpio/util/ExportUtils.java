package com.scorpio.util;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import com.scorpio.Constants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.excel.EasyExcel;
import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.write.metadata.WriteSheet;
import com.google.common.collect.Lists;

/**
 * @author guosk
 *
 * create at 2022年8月31日, alpha-common
 */
public final class ExportUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExportUtils.class);

  private ExportUtils() {
    throw new IllegalStateException("Utility class");
  }

  /**
   * 数据导出
   * @param titles 标题
   * @param fetchData 数据迭代器
   * @param file 目标文件
   * @param fileType 文件类型（csv/excel）
   * @param out 输出流（参数不为空时，会将内容先写入到文件，最后通过文件写入到输出流）
   * @throws IOException
   */
  public static void export(List<String> titles, FetchData fetchData, File file, String fileType,
      @Nullable OutputStream out) throws IOException {
    // 判断导出格式是否合法
    if (!StringUtils.equalsAnyIgnoreCase(fileType, Constants.EXPORT_FILE_TYPE_CSV,
        Constants.EXPORT_FILE_TYPE_EXCEL)) {
      LOGGER.warn("Unsupported export file style: {}", fileType);
      throw new UnsupportedOperationException("不支持的导出样式");
    }

    // 导出模式为execl时，初始化写对象
    ExcelWriter excelWriter = null;
    WriteSheet writeSheet = null;
    if (StringUtils.equals(fileType, Constants.EXPORT_FILE_TYPE_EXCEL)) {
      List<List<String>> header = titles.stream().map(item -> Lists.newArrayList(item))
          .collect(Collectors.toList());

      excelWriter = EasyExcel.write(file).head(header).build();
      writeSheet = EasyExcel.writerSheet("dataset").build();
    }

    // 遍历数据，写入到文件内
    int index = 0;
    while (fetchData.hasNext()) {
      try {
        List<List<String>> dataset = fetchData.next();
        if (CollectionUtils.isEmpty(dataset)) {
          break;
        }

        if (StringUtils.equals(fileType, Constants.EXPORT_FILE_TYPE_CSV)) {
          if (index == 0) {
            String title = CsvUtils.spliceRowData(titles.toArray(new String[titles.size()]));
            FileUtils.writeStringToFile(file, title, "UTF-8", false);
          }

          for (List<String> line : dataset) {
            FileUtils.writeStringToFile(file,
                CsvUtils.spliceRowData(line.toArray(new String[line.size()])), "UTF-8", true);
          }
        } else {
          excelWriter.write(dataset, writeSheet);
        }
      } catch (IOException e) {
        LOGGER.warn("export failed, delete tmp file: {}", file.getAbsolutePath());
        FileUtils.deleteQuietly(file);
      }

      index++;
    }

    // 导出模式为execl时，销毁写对象
    if (excelWriter != null) {
      excelWriter.finish();
    }

    // 将文件内容写入到输出流
    if (out != null) {
      FileUtils.copyFile(file, out);
      FileUtils.deleteQuietly(file);
    }
  }

  /**
   * 数据迭代器
   */
  public interface FetchData extends Iterator<List<List<String>>> {
  }

  /**
   * 数据迭代器demo
   */
  public final class FetchDataImpl implements FetchData {

    // 游标，代表下次读取数据从哪个位置开始
    private int offset = 0;
    // 单次读取数据最大量
    private int batchSize = 100;

    /**
     * @see Iterator#hasNext()
     */
    @Override
    public boolean hasNext() {
      return offset % batchSize == 0;
    }

    /**
     * @see Iterator#next()
     */
    @Override
    public List<List<String>> next() {
      // limit 100 offset 0
      int currentSize = 80;
      // 定位到下次的游标
      offset += currentSize;

      return null;
    }
  }

}
