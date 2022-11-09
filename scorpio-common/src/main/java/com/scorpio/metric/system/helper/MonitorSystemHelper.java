package com.scorpio.metric.system.helper;

import com.google.common.base.Charsets;
import com.scorpio.metric.system.data.MonitorCpuTimes;
import com.scorpio.metric.system.data.MonitorMemory;
import com.scorpio.util.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 
 * @author liumeng
 *
 * create at 2019年3月16日, alpha-common
 */
public final class MonitorSystemHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(MonitorSystemHelper.class);

  private static final Pattern CPU_JIFFIES_PATTERN = Pattern.compile("cpu\\s+(.*)",
      Pattern.MULTILINE);
  private static final Pattern TOTAL_MEMORY_PATTERN = Pattern.compile("MemTotal:\\s+(\\d+) kB",
      Pattern.MULTILINE);
  private static final Pattern FREE_MEMORY_PATTERN = Pattern.compile("MemFree:\\s+(\\d+) kB",
      Pattern.MULTILINE);
  private static final Pattern BUFFERS_MEMORY_PATTERN = Pattern.compile("Buffers:\\s+(\\d+) kB",
      Pattern.MULTILINE);
  private static final Pattern CACHED_MEMORY_PATTERN = Pattern.compile("Cached:\\s+(\\d+) kB",
      Pattern.MULTILINE);
  private static final Pattern SLAB_MEMORY_PATTERN = Pattern.compile("Slab:\\s+(\\d+) kB",
      Pattern.MULTILINE);

  private static final Tuple2<Pattern, String> CPU_MODEL_INTEL = Tuples
          .of(Pattern.compile("(Intel\\(R\\))", Pattern.MULTILINE), "英特尔");
  private static final Tuple2<Pattern,
          String> CPU_MODEL_PHYTIUM = Tuples.of(Pattern.compile("(Phytium)", Pattern.MULTILINE), "飞腾");
  private static final Tuple2<Pattern,
          String> CPU_MODEL_SW = Tuples.of(Pattern.compile("(SW\\d+)", Pattern.MULTILINE), "申威");

  private static final Tuple3<String, String, Pattern> OS_TAG_KYLINSOFT = Tuples.of("KYLINSOFT",
          "银河麒麟", Pattern.compile("(ky\\d+)", Pattern.MULTILINE));
  private static final Tuple3<String, String, Pattern> OS_TAG_CENTOS = Tuples.of("centos", "CentOS",
          Pattern.compile("(el\\d+)", Pattern.MULTILINE));


  private MonitorSystemHelper() {
  }

  /**
   * 
   * @return
   */
  public static MonitorCpuTimes fetchCpuTimes() {

    if (!SystemUtils.IS_OS_LINUX) {
      return new MonitorCpuTimes(0, 0, 0);
    }

    String[] parsedJiffies = FileUtils.runRegexOnFile(CPU_JIFFIES_PATTERN, "/proc/stat")
        .split("\\s+");
    long userJiffies = Long.parseLong(parsedJiffies[0]) + Long.parseLong(parsedJiffies[1]);
    long idleJiffies = Long.parseLong(parsedJiffies[3]);
    long systemJiffies = Long.parseLong(parsedJiffies[2]);
    // this is for Linux >= 2.6
    if (parsedJiffies.length > 4) {
      for (int i = 4; i < parsedJiffies.length; i++) {
        systemJiffies += Long.parseLong(parsedJiffies[i]);
      }
    }

    LOGGER.trace("Fetch cpu metric: {}/{}/{}.", userJiffies, idleJiffies, systemJiffies);
    return new MonitorCpuTimes(toMillis(userJiffies), toMillis(systemJiffies),
        toMillis(idleJiffies));
  }

  /**
   * 
   * @return
   */
  public static MonitorMemory fetchPhysicalMemory() {

    if (!SystemUtils.IS_OS_LINUX) {
      return new MonitorMemory(0, 0, 0, 0, 0);
    }

    String fileContent = "";
    try {
      fileContent = FileUtils.slurp("/proc/meminfo");
    } catch (IOException e) {
      LOGGER.warn("read file '/proc/meminfo' error.", e);
      return new MonitorMemory(0, 0, 0, 0, 0);
    }

    String freeMemory = FileUtils.runRegexOnFileContent(FREE_MEMORY_PATTERN, fileContent);
    long free = Long.parseLong(freeMemory) * 1024;

    String buffersMemory = FileUtils.runRegexOnFileContent(BUFFERS_MEMORY_PATTERN, fileContent);
    long buffers = Long.parseLong(buffersMemory) * 1024;

    String cachedMemory = FileUtils.runRegexOnFileContent(CACHED_MEMORY_PATTERN, fileContent);
    long cached = Long.parseLong(cachedMemory) * 1024;

    String slabMemory = FileUtils.runRegexOnFileContent(SLAB_MEMORY_PATTERN, fileContent);
    long slab = Long.parseLong(slabMemory) * 1024;

    String totalMemory = FileUtils.runRegexOnFileContent(TOTAL_MEMORY_PATTERN, fileContent);
    long total = Long.parseLong(totalMemory) * 1024;

    LOGGER.trace("Fetch memory metric, free: {}, buffers: {}, cached: {}, slab: {}, total: {}.",
        free, buffers, cached, slab, total);
    return new MonitorMemory(free, buffers, cached, slab, total);
  }

  /**
   *
   * @return
   */
  public static String fetchCpuModelInfo() {
    if (!SystemUtils.IS_OS_LINUX) {
      return "";
    }

    // cat /proc/cpuinfo | grep -m 1 "model name" && arch
    StringBuilder cpuInfo = new StringBuilder();

    Process process = null;
    try {
      ProcessBuilder builder = new ProcessBuilder("sh", "-c",
              "cat /proc/cpuinfo | grep -m 1 \"model name\" && arch");
      builder.redirectErrorStream(true);
      process = builder.start();
      try (BufferedReader reader = new BufferedReader(
              new InputStreamReader(process.getInputStream(), Charsets.UTF_8))) {
        String line;
        int index = 0;
        while ((line = reader.readLine()) != null) {
          LOGGER.trace("Detect cpu model info from \" {} \".", line);
          if (index == 0) {
            if (CPU_MODEL_INTEL.getT1().matcher(line).find()) {
              cpuInfo.append(CPU_MODEL_INTEL.getT2());
            } else if (CPU_MODEL_PHYTIUM.getT1().matcher(line).find()) {
              cpuInfo.append(CPU_MODEL_PHYTIUM.getT2());
            } else if (CPU_MODEL_SW.getT1().matcher(line).find()) {
              cpuInfo.append(CPU_MODEL_SW.getT2());
            } else {
              break;
            }
          } else {
            cpuInfo.append(line);
          }

          index++;
        }
      }

      process.waitFor();
    } catch (InterruptedException e) {
      LOGGER.info("fetch cpu model has been interrupt.");
      Thread.currentThread().interrupt();
    } catch (IOException e) {
      LOGGER.warn("fetch cpu model failed.", e);
    } finally {
      if (process != null) {
        process.destroy();
      }
    }

    return cpuInfo.toString();
  }

  /**
   *
   * @return
   */
  public static String fetchOsInfo() {
    if (!SystemUtils.IS_OS_LINUX) {
      return "";
    }

    // cat /proc/version
    StringBuilder osVersion = new StringBuilder();
    try {
      String fileContent = FileUtils.slurp("/proc/version");

      if (StringUtils.containsIgnoreCase(fileContent, OS_TAG_KYLINSOFT.getT1())) {
        Matcher matcher = OS_TAG_KYLINSOFT.getT3().matcher(fileContent);
        matcher.find();
        osVersion.append(OS_TAG_KYLINSOFT.getT2()).append("V")
                .append(StringUtils.remove(matcher.group(1), "ky"));
      } else if (StringUtils.containsIgnoreCase(fileContent, OS_TAG_CENTOS.getT1())) {
        Matcher matcher = OS_TAG_CENTOS.getT3().matcher(fileContent);
        matcher.find();
        osVersion.append(OS_TAG_CENTOS.getT2()).append(StringUtils.remove(matcher.group(1), "el"));
      }
    } catch (IOException e) {
      LOGGER.warn("read file '/proc/version' error.", e);
      return osVersion.toString();
    }

    return osVersion.toString();
  }

  /**
   * 
   * @param fsRoot
   * @return
   */
  public static String fetchFilesystemUsagePct(String fsRoot) {

    long usedLong = fetchFilesystemUsagePctLong(fsRoot);
    if (usedLong >= 0) {
      return usedLong + "%";
    } else {
      return "";
    }
  }

  /**
   * 
   * @param fsRoot
   * @return
   */
  public static long fetchFilesystemUsagePctLong(String fsRoot) {

    long used = 0L;

    if (!SystemUtils.IS_OS_LINUX) {
      return used;
    }

    File filesystem = new File(fsRoot);
    if (filesystem.exists() && filesystem.isDirectory()) {
      double usedPct = ((double) (filesystem.getTotalSpace() - filesystem.getFreeSpace()))
          / ((double) filesystem.getTotalSpace());
      used = Math.round(usedPct * 100);

      LOGGER.trace("Fetch fs:{} metric: {}/{}.", fsRoot, filesystem.getFreeSpace(),
          filesystem.getTotalSpace());
    }

    return used;
  }

  /**
   * 
   * @param fsRoot
   * @return
   */
  public static long fetchFilesystemFreeSpace(String fsRoot) {

    long free = 0L;

    if (!SystemUtils.IS_OS_LINUX) {
      return free;
    }

    File filesystem = new File(fsRoot);
    if (filesystem.exists() && filesystem.isDirectory()) {
      free = filesystem.getFreeSpace();

      LOGGER.trace("Fetch fs:{} metric: {}/{}.", fsRoot, filesystem.getFreeSpace(),
          filesystem.getTotalSpace());
    }

    return free;
  }

  public static String fetchMainboardUuid() {

    String uuid = "";

    if (!SystemUtils.IS_OS_LINUX) {
      return uuid;
    }

    Process process = null;
    try {
      ProcessBuilder builder = new ProcessBuilder("dmidecode", "-t", "1");
      builder.redirectErrorStream(true);
      process = builder.start();
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(process.getInputStream(), Charsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          LOGGER.trace("Detect mainboard UUID info from \" {} \".", line);

          if (StringUtils.contains(line, "UUID:")) {
            uuid = StringUtils.substringAfter(line, "UUID:").trim();
          }
        }
      }

      process.waitFor();
    } catch (InterruptedException e) {
      LOGGER.info("fetch mainboard uuid has been interrupt.");
      Thread.currentThread().interrupt();
    } catch (IOException e) {
      LOGGER.warn("fetch mainboard uuid failed.", e);
    } finally {
      if (process != null) {
        process.destroy();
      }
    }
    return uuid;
  }

  public static String fetchMainboardSerialNumber() {

    String serialNumber = "";

    if (!SystemUtils.IS_OS_LINUX) {
      return serialNumber;
    }

    Process process = null;
    try {
      ProcessBuilder builder = new ProcessBuilder("dmidecode", "-t", "2");
      builder.redirectErrorStream(true);
      process = builder.start();
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(process.getInputStream(), Charsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          LOGGER.trace("Detect mainboard serial number info from \" {} \".", line);

          if (StringUtils.contains(line, "Serial Number:")) {
            serialNumber = StringUtils.substringAfter(line, "Serial Number:").trim();
          }
        }
      }

      process.waitFor();
    } catch (InterruptedException e) {
      LOGGER.info("fetch mainboard serial number has been interrupt.");
      Thread.currentThread().interrupt();
    } catch (IOException e) {
      LOGGER.warn("fetch mainboard serial number failed.", e);
    } finally {
      if (process != null) {
        process.destroy();
      }
    }
    return serialNumber;
  }

  /**
   * 查询系统运行时间，单位为秒
   * @return
   */
  public static long fetchSystemRuntimeSecond() {
    long systemRunningSeconds = 0L;

    if (!SystemUtils.IS_OS_LINUX) {
      return systemRunningSeconds;
    }

    Process process = null;
    try {
      ProcessBuilder builder = new ProcessBuilder("cat", "/proc/uptime");
      builder.redirectErrorStream(true);
      process = builder.start();
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(process.getInputStream(), Charsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          LOGGER.trace("Detect system run time info from \" {} \".", line);
          String runTime = StringUtils.split(line, " ")[0];
          systemRunningSeconds = Double.valueOf(runTime).longValue();
          break;
        }
      }

      process.waitFor();
    } catch (InterruptedException e) {
      LOGGER.info("fetch system run time has been interrupt.");
      Thread.currentThread().interrupt();
    } catch (IOException e) {
      LOGGER.warn("fetch system run time failed.", e);
    } finally {
      if (process != null) {
        process.destroy();
      }
    }

    return systemRunningSeconds;
  }

  private static long toMillis(long jiffies) {
    return jiffies * 10; // /proc/stat中的时间单位，一般地定义为jiffies(一般地等于10ms)
  }

}
