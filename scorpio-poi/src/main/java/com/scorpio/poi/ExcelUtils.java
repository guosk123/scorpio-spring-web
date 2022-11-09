package com.scorpio.poi;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.List;

import org.apache.poi.hssf.util.HSSFColor.HSSFColorPredefined;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import com.google.common.collect.Lists;

import reactor.util.function.Tuple3;
import reactor.util.function.Tuple4;
import reactor.util.function.Tuples;

public final class ExcelUtils {

  private static final short DEFAULT_COLUMN_WIDTH = 15;
  private static final short DEFAULT_HEADER_FONT_HEIGHT = 12;

  private static final short DEFAULT_HEADER_FILL_COLOR = HSSFColorPredefined.GREY_40_PERCENT
      .getIndex();
  private static final short DEFAULT_HEADER_FONT_COLOR = HSSFColorPredefined.BLACK.getIndex();
  private static final String DEFAULT_HEADER_FONT_NAME = "微软雅黑";
  private static final short DEFAULT_DATA_FONT_HEIGHT = 10;
  private static final short DEFAULT_DATA_FILL_COLOR = HSSFColorPredefined.WHITE.getIndex();
  private static final short DEFAULT_DATA_FONT_COLOR = HSSFColorPredefined.BLACK.getIndex();
  private static final String DEFAULT_DATA_FONT_NAME = "微软雅黑";

  public static final ExcelStyle DEFAULT_STYLE = ExcelStyle.getDefault();

  private ExcelUtils() {
    throw new IllegalStateException("Utility class");
  }

  public static void main(String[] args) {
    List<String> headers = Lists.newArrayList("ID", "名称", "年龄", "身高");
    List<List<String>> dataset = Lists.newArrayList(Lists.newArrayList("1", "`张三,aa`", "18", "188"),
        Lists.newArrayList("2", "李四", "19", "199"));

    File file = Paths.get("G:", "a.xlsx").toFile();
    try {
      OutputStream out = new FileOutputStream(file);
      Tuple3<String, List<String>,
          List<List<String>>> tuple1 = Tuples.of("呜哈哈11", headers, dataset);
      Tuple3<String, List<String>,
          List<List<String>>> tuple2 = Tuples.of("xxx222", headers, dataset);
      exportExcelWithMutilSheetAndDefaultStyle(Lists.newArrayList(tuple1, tuple2), out);
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  /**
   * 导出单个sheet的xlsx文件
   * @param headers 表头
   * @param dataset 数据集(外层为每行数据，内层为每列数据)
   * @param out 输出流
   * @throws IOException
   */
  public static void exportExcel(List<String> headers, List<List<String>> dataset, OutputStream out)
      throws IOException {
    XSSFWorkbook workbook = new XSSFWorkbook();
    fillData(workbook, "sheet1", DEFAULT_STYLE, headers, dataset);
    workbook.write(out);
  }

  /**
   * 导出单个sheet的xlsx文件
   * @param headers 表头
   * @param dataset 数据集(外层为每行数据，内层为每列数据)
   * @param excelStyle 自定义样式
   * @param out 输出流
   * @throws IOException
   */
  public static void exportExcel(List<String> headers, List<List<String>> dataset,
      ExcelStyle excelStyle, OutputStream out) throws IOException {
    XSSFWorkbook workbook = new XSSFWorkbook();
    fillData(workbook, "sheet1", excelStyle, headers, dataset);
    workbook.write(out);
  }

  /**
   * 导出包含多个sheet的xlsx文件（使用默认样式）
   * @param sheetList 数据集合
   * <p>T1：sheet名称，T2：header集合，T3：数据内容集合(外层为每行数据，内层为每列数据)</p>
   * @param out 输出流
   * @throws IOException
   */
  public static void exportExcelWithMutilSheetAndDefaultStyle(
      List<Tuple3<String, List<String>, List<List<String>>>> sheetList, OutputStream out)
      throws IOException {
    XSSFWorkbook workbook = new XSSFWorkbook();
    sheetList.forEach(sheet -> {
      fillData(workbook, sheet.getT1(), DEFAULT_STYLE, sheet.getT2(), sheet.getT3());
    });

    workbook.write(out);
  }

  /**
   * 导出包含多个sheet的xlsx文件（使用自定义样式）
   * @param sheetList 数据集合</n>
   * <p>T1：sheet名称，T2：自定义样式，T3：header集合，T4：数据内容集合(外层为每行数据，内层为每列数据)</p>
   * @param out 输出流
   * @throws IOException
   */
  public static void exportExcelWithMutilSheet(
      List<Tuple4<String, ExcelStyle, List<String>, List<List<String>>>> sheetList,
      OutputStream out) throws IOException {
    XSSFWorkbook workbook = new XSSFWorkbook();
    sheetList.forEach(sheet -> {
      fillData(workbook, sheet.getT1(), sheet.getT2(), sheet.getT3(), sheet.getT4());
    });

    workbook.write(out);
  }


  /**
   * 生成表格并填充数据
   * @param workbook 工作薄
   * @param sheetName 表格名称
   * @param excelStyle 样式
   * @param headers 表头
   * @param dataset 数据集</n>
   * <p>数据集外层集合为每行的数据，内层集合为每列的数据</p>
   */
  private static void fillData(XSSFWorkbook workbook, String sheetName, ExcelStyle excelStyle,
      List<String> headers, List<List<String>> dataset) {
    // 生成表格
    XSSFSheet sheet = workbook.createSheet(sheetName);
    // 设置表格默认列宽度为15个字节
    sheet.setDefaultColumnWidth(excelStyle.getDefaultColumnWidth());

    // 生成header样式
    XSSFCellStyle headerStyle = workbook.createCellStyle();
    headerStyle.setFillForegroundColor(excelStyle.getHeaderFillColor());
    headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    headerStyle.setBorderBottom(BorderStyle.THIN);
    headerStyle.setBorderLeft(BorderStyle.THIN);
    headerStyle.setBorderRight(BorderStyle.THIN);
    headerStyle.setBorderTop(BorderStyle.THIN);
    headerStyle.setAlignment(HorizontalAlignment.CENTER);
    // 生成header字体
    XSSFFont headerFont = workbook.createFont();
    headerFont.setColor(excelStyle.getHeaderFontColor());
    headerFont.setFontHeightInPoints(excelStyle.getHeaderFontHeight());
    headerFont.setFontName(excelStyle.getHeaderFontName());
    headerFont.setBold(true);
    // 把字体应用到样式
    headerStyle.setFont(headerFont);

    // 生成标题行
    XSSFRow headerRow = sheet.createRow(0);
    for (int i = 0; i < headers.size(); i++) {
      XSSFCell cell = headerRow.createCell(i);
      cell.setCellStyle(headerStyle);
      XSSFRichTextString text = new XSSFRichTextString(headers.get(i));
      cell.setCellValue(text);
    }

    // 生成数据内容样式
    XSSFCellStyle dataStyle = workbook.createCellStyle();
    dataStyle.setFillForegroundColor(excelStyle.getDataFillColor());
    dataStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
    dataStyle.setBorderBottom(BorderStyle.THIN);
    dataStyle.setBorderLeft(BorderStyle.THIN);
    dataStyle.setBorderRight(BorderStyle.THIN);
    dataStyle.setBorderTop(BorderStyle.THIN);
    dataStyle.setAlignment(HorizontalAlignment.CENTER);
    dataStyle.setVerticalAlignment(VerticalAlignment.CENTER);
    // 生成数据字体
    XSSFFont dataFont = workbook.createFont();
    dataFont.setColor(excelStyle.getDataFontColor());
    dataFont.setFontHeightInPoints(excelStyle.getDataFontHeight());
    dataFont.setFontName(excelStyle.getDataFontName());
    dataFont.setBold(false);
    // 把字体应用到当前的样式
    dataStyle.setFont(dataFont);

    // 生成数据行
    for (int i = 0; i < dataset.size(); i++) {
      XSSFRow dataRow = sheet.createRow(i + 1);
      for (int j = 0; j < dataset.get(i).size(); j++) {
        XSSFCell cell = dataRow.createCell(j);
        cell.setCellStyle(dataStyle);
        cell.setCellValue(new String(dataset.get(i).get(j).getBytes(StandardCharsets.UTF_8)));
      }
    }
  }

  @SuppressWarnings("unused")
  private void exportExcelWithPhoto(byte[] imgByte, OutputStream out) {
    // 声明一个工作薄
    XSSFWorkbook workbook = new XSSFWorkbook();
    // 生成一个表格
    XSSFSheet sheet = workbook.createSheet("sheet");
    // 设置表格默认列宽度为15个字节
    sheet.setDefaultColumnWidth((short) 15);

    // 声明一个画图的顶级管理器
    XSSFDrawing draw = sheet.createDrawingPatriarch();
    // 定义注释的大小和位置,详见文档
    XSSFComment comment = draw
        .createCellComment(new XSSFClientAnchor(0, 0, 0, 0, (short) 4, 2, (short) 6, 5));
    // 设置注释内容
    comment.setString(new XSSFRichTextString("添加注释！"));
    // 设置注释作者，当鼠标移动到单元格上是可以在状态栏中看到该内容.
    comment.setAuthor("test");
    // 产生表格标题行
    XSSFRow row = sheet.createRow(0);
    // 设置行高为60px;
    row.setHeightInPoints(60);
    // 设置图片所在列宽度为80px,注意这里单位的一个换算
    sheet.setColumnWidth(0, (short) (35.7 * 80));
    XSSFClientAnchor anchor = new XSSFClientAnchor(0, 0, 1023, 255, (short) 6, 0, (short) 6, 0);
    anchor.setAnchorType(ClientAnchor.AnchorType.MOVE_DONT_RESIZE);
    draw.createPicture(anchor, workbook.addPicture(imgByte, XSSFWorkbook.PICTURE_TYPE_JPEG));

    try {
      workbook.write(out);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        workbook.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public static class ExcelStyle {

    private short defaultColumnWidth;
    private short headerFontHeight;
    private String headerFontName;
    private short headerFillColor;
    private short headerFontColor;
    private short dataFontHeight;
    private String dataFontName;
    private short dataFillColor;
    private short dataFontColor;

    public ExcelStyle() {
    }

    public ExcelStyle(short defaultColumnWidth, short headerFontHeight, String headerFontName,
        short headerFillColor, short headerFontColor, short dataFontHeight, String dataFontName,
        short dataFillColor, short dataFontColor) {
      super();
      this.defaultColumnWidth = defaultColumnWidth;
      this.headerFontHeight = headerFontHeight;
      this.headerFontName = headerFontName;
      this.headerFillColor = headerFillColor;
      this.headerFontColor = headerFontColor;
      this.dataFontHeight = dataFontHeight;
      this.dataFontName = dataFontName;
      this.dataFillColor = dataFillColor;
      this.dataFontColor = dataFontColor;
    }

    @Override
    public String toString() {
      return "ExcelStyle [defaultColumnWidth=" + defaultColumnWidth + ", headerFontHeight="
          + headerFontHeight + ", headerFontName=" + headerFontName + ", headerFillColor="
          + headerFillColor + ", headerFontColor=" + headerFontColor + ", dataFontHeight="
          + dataFontHeight + ", dataFontName=" + dataFontName + ", dataFillColor=" + dataFillColor
          + ", dataFontColor=" + dataFontColor + "]";
    }

    public static ExcelStyle getDefault() {
      return new ExcelStyle(DEFAULT_COLUMN_WIDTH, DEFAULT_HEADER_FONT_HEIGHT,
          DEFAULT_HEADER_FONT_NAME, DEFAULT_HEADER_FILL_COLOR, DEFAULT_HEADER_FONT_COLOR,
          DEFAULT_DATA_FONT_HEIGHT, DEFAULT_DATA_FONT_NAME, DEFAULT_DATA_FILL_COLOR,
          DEFAULT_DATA_FONT_COLOR);
    }

    public short getDefaultColumnWidth() {
      return defaultColumnWidth;
    }

    public void setDefaultColumnWidth(short defaultColumnWidth) {
      this.defaultColumnWidth = defaultColumnWidth;
    }

    public short getHeaderFontHeight() {
      return headerFontHeight;
    }

    public void setHeaderFontHeight(short headerFontHeight) {
      this.headerFontHeight = headerFontHeight;
    }

    public String getHeaderFontName() {
      return headerFontName;
    }

    public void setHeaderFontName(String headerFontName) {
      this.headerFontName = headerFontName;
    }

    public short getHeaderFillColor() {
      return headerFillColor;
    }

    public void setHeaderFillColor(short headerFillColor) {
      this.headerFillColor = headerFillColor;
    }

    public short getHeaderFontColor() {
      return headerFontColor;
    }

    public void setHeaderFontColor(short headerFontColor) {
      this.headerFontColor = headerFontColor;
    }

    public short getDataFontHeight() {
      return dataFontHeight;
    }

    public void setDataFontHeight(short dataFontHeight) {
      this.dataFontHeight = dataFontHeight;
    }

    public String getDataFontName() {
      return dataFontName;
    }

    public void setDataFontName(String dataFontName) {
      this.dataFontName = dataFontName;
    }

    public short getDataFillColor() {
      return dataFillColor;
    }

    public void setDataFillColor(short dataFillColor) {
      this.dataFillColor = dataFillColor;
    }

    public short getDataFontColor() {
      return dataFontColor;
    }

    public void setDataFontColor(short dataFontColor) {
      this.dataFontColor = dataFontColor;
    }
  }

}
