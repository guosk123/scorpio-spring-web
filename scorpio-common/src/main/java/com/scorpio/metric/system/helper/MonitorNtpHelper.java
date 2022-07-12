package com.scorpio.metric.system.helper;

import com.google.common.base.Charsets;
import com.scorpio.metric.system.data.MonitorSysTime;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.util.Date;


/**
 * @author mazhiyuan
 *
 * create at 2020年6月12日, alpha-common
 */
public final class MonitorNtpHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(MonitorNtpHelper.class);

  private static final String ntpConfPath = "";

  private MonitorNtpHelper() {
  }

  /**
   * 查询系统时间
   * 
   * @return
   */
  public static MonitorSysTime queryDatetime() {

    MonitorSysTime deviceNtp = new MonitorSysTime();

    Process process = null;
    try {
      ProcessBuilder builder = new ProcessBuilder("timedatectl");
      builder.redirectErrorStream(true);
      process = builder.start();
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(process.getInputStream(), Charsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          LOGGER.trace("Detect NTP info from \" {} \".", line);

          if (StringUtils.contains(line, "Local time:")) {
            String datetime = StringUtils.substringAfter(line, "Local time:").trim();

            String localTime = StringUtils.substring(datetime,
                StringUtils.indexOf(datetime, " ") + 1, StringUtils.lastIndexOf(datetime, " "));

            deviceNtp.setDateTime(StringUtils.trim(localTime));
          } else if (StringUtils.contains(line, "Time zone:")) {
            String timezone = StringUtils.substringBetween(line, "Time zone:", "(").trim();
            deviceNtp.setTimeZone(timezone);
          }
        }
      }

      process.waitFor();
    } catch (InterruptedException e) {
      LOGGER.info("fetch ntp configuration has been interrupt.");
      Thread.currentThread().interrupt();
    } catch (IOException e) {
      LOGGER.warn("fetch ntp configuration failed.", e);
    } finally {
      if (process != null) {
        process.destroy();
      }
    }

    return deviceNtp;
  }

  /**
   * 
   * @return
   */
  public static String queryNtpConf() {

    String ntpServer = "";

    try (BufferedReader file = new BufferedReader(new InputStreamReader(
        java.nio.file.Files.newInputStream(Paths.get(ntpConfPath)), Charsets.UTF_8))) {
      // 读原配置文件的数据
      String line;
      while ((line = file.readLine()) != null) {

        line = line.trim();

        if (StringUtils.startsWithIgnoreCase(line, "server ")
            && StringUtils.endsWithIgnoreCase(line, "iburst prefer")) {
          // 只读第一个server配置 既退出
          ntpServer = StringUtils.substringBetween(line, "server ", " iburst prefer");
          break;
        }
      }
    } catch (IOException e) {
      LOGGER.warn("Fail to parse ntp configuration.", e);
    }

    LOGGER.debug("ntp server is {}.", ntpServer);
    return ntpServer;
  }

  /**
   * 
   * @param ntpServer
   */
  public static void saveOrUpdateNtpConf(String ntpLogPath, String ntpServer) {

    // 关闭ntp
    changeNtpEnable(false);

    // 运行ntpdate, ntpdate -u <ntpserver-ip/域名>
    Process process = null;
    try {
      process = processNtpDateCommand(ntpLogPath, "ntpdate", "-u", ntpServer);
    } catch (InterruptedException e) {
      LOGGER.info("execute ntpdate command has been interrupt.");
      Thread.currentThread().interrupt();
    } catch (IOException e) {
      LOGGER.warn("execute ntpdate command failed.", e);
    } finally {
      if (process != null) {
        process.destroy();
      }
    }

    StringBuilder buffer = new StringBuilder();
    // 修改server
    try (BufferedReader fileIn = new BufferedReader(new InputStreamReader(
        java.nio.file.Files.newInputStream(Paths.get(ntpConfPath)), Charsets.UTF_8))) {
      // 读原配置文件的数据
      String line;
      boolean isAddServer = true;
      while ((line = fileIn.readLine()) != null) {

        line = line.trim();

        if (StringUtils.startsWithIgnoreCase(line, "server ")
            && StringUtils.endsWithIgnoreCase(line, "iburst prefer")) {

          isAddServer = false;
          buffer.append("server ").append(ntpServer).append(" iburst prefer");
        } else {
          buffer.append(line);
        }
        buffer.append('\n');
      }

      if (isAddServer) {
        buffer.append("server ").append(ntpServer).append(" iburst prefer");
        buffer.append('\n');
      }

    } catch (IOException e) {
      LOGGER.warn("Fail to read ntp configuration.", e);
    }

    try (FileOutputStream fileOut = FileUtils.openOutputStream(new File(ntpConfPath))) {
      // 替换源文件写入
      fileOut.write(buffer.toString().getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      LOGGER.warn("Fail to write ntp configuration.", e);
    }

    changeNtpEnable(true);
  }

  /**
   * 
   * @param timeZone
   * @param dateTime
   */
  public static void saveDatetime(String timeZone, String dateTime) {

    try {
      // 校验时间是否合法, 去掉不合法时间字符
      SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      Date validDate = sdf.parse(dateTime);
      dateTime = sdf.format(validDate);

      // 校验时区是否合法
      ZoneId.of(timeZone);
    } catch (ParseException e) {
      LOGGER.warn("timeZone or dateTime style failed.", e);
    }

    // 关闭ntp
    changeNtpEnable(false);

    Process process1 = null;
    Process process2 = null;
    try {

      // 设置时区
      process1 = processTimeDateCtlCommand("timedatectl", "set-timezone", timeZone);

      // 设置时间
      process2 = processTimeDateCtlCommand("timedatectl", "set-time", dateTime);

      // 将远程ntp服务器从ntp.conf文件去除
      deleteNtpConf();

    } catch (InterruptedException e) {
      LOGGER.info("save datetime configuration has been interrupt.");
      Thread.currentThread().interrupt();
    } catch (IOException e) {
      LOGGER.warn("save datetime configuration failed.", e);
    } finally {
      if (process1 != null) {
        process1.destroy();
      }
      if (process2 != null) {
        process2.destroy();
      }

      changeNtpEnable(true);
    }
  }

  /**
   * 
   * @param enable
   */
  private static void changeNtpEnable(boolean enable) {

    Process process = null;
    try {
      // 开/关NTP服务
      process = processTimeDateCtlCommand("timedatectl", "set-ntp", String.valueOf(enable));

    } catch (InterruptedException e) {
      LOGGER.info("save ntp enable configuration has been interrupt.");
      Thread.currentThread().interrupt();
    } catch (IOException e) {
      LOGGER.warn("save ntp enable configuration failed.", e);
    } finally {
      if (process != null) {
        process.destroy();
      }
    }
  }

  private static Process processTimeDateCtlCommand(String... commands)
      throws IOException, InterruptedException {
    ProcessBuilder builder = new ProcessBuilder(commands);
    builder.redirectErrorStream(true);
    Process process = builder.start();
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(process.getInputStream(), Charsets.UTF_8))) {
      StringBuilder buffer = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        buffer.append(line).append('\n');
      }

      if (buffer.length() > 0) {
        LOGGER.warn("command execution failed", buffer.toString());
      }
    }

    process.waitFor();
    return process;
  }

  private static Process processNtpDateCommand(String ntpLogPath, String... commands)
      throws IOException, InterruptedException {
    ProcessBuilder builder = new ProcessBuilder(commands);
    builder.redirectErrorStream(true);
    Process process = builder.start();
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(process.getInputStream(), Charsets.UTF_8))) {
      StringBuilder buffer = new StringBuilder();
      String line;
      while ((line = reader.readLine()) != null) {
        buffer.append(line).append('\n');
      }
      LOGGER.trace("ntpdate command execution result is {}.", buffer.toString());

      // 将执行ntpdate的命令的结果记录到ntp log中
      File ntpLogFile = new File(ntpLogPath);
      try (FileOutputStream fileOut = FileUtils.openOutputStream(ntpLogFile, true)) {
        fileOut.write(buffer.toString().getBytes(StandardCharsets.UTF_8));
      } catch (IOException e) {
        LOGGER.warn("Fail to write ntp log.", e);
      }

      if (!StringUtils.contains(buffer.toString(), "offset")) {
        LOGGER.warn("command execution failed", buffer.toString());
      }
    }

    process.waitFor();
    return process;
  }

  private static void deleteNtpConf() {
    // 删除server

    StringBuilder buffer = new StringBuilder();
    try (BufferedReader fileIn = new BufferedReader(new InputStreamReader(
        java.nio.file.Files.newInputStream(Paths.get(ntpConfPath)), Charsets.UTF_8))) {
      // 读原配置文件的数据
      String line;
      while ((line = fileIn.readLine()) != null) {

        line = line.trim();

        if (StringUtils.startsWithIgnoreCase(line, "server ")
            && StringUtils.endsWithIgnoreCase(line, "iburst prefer")) {
          // 去除远程ntp服务器地址
        } else {
          buffer.append(line);
        }
        buffer.append('\n');
      }

    } catch (IOException e) {
      LOGGER.warn("Fail to read ntp configuration.", e);
    }

    try (FileOutputStream fileOut = FileUtils.openOutputStream(new File(ntpConfPath))) {
      // 替换源文件写入
      fileOut.write(buffer.toString().getBytes(StandardCharsets.UTF_8));
    } catch (IOException e) {
      LOGGER.warn("Fail to write ntp configuration.", e);
    }
  }

}
