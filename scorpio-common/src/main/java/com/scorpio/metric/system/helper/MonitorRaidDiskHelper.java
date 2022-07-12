package com.scorpio.metric.system.helper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Map;

import com.scorpio.Constants;
import com.scorpio.metric.MetricConstants;
import com.scorpio.metric.system.data.MonitorRaid;
import com.scorpio.metric.system.data.MonitorRaidDisk;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.collect.Maps;
import com.google.common.primitives.Ints;

/**
 * @author liumeng
 *
 * create at 2019年3月16日, alpha-common
 */
public final class MonitorRaidDiskHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(MonitorRaidDiskHelper.class);

  private static final String DEVICE_FRONT_PANEL_ID = "0";
  private static final String DEVICE_FRONT_PANEL = "前面板";
  private static final String DEVICE_REAR_PANEL_ID = "1";
  private static final String DEVICE_REAR_PANEL = "后面板";

  private MonitorRaidDiskHelper() {
  }

  /**
   * @param cmdDiskRaid
   * @return
   */
  public static Map<String, MonitorRaid> fetchRaidInfo(String cmdDiskRaid) {

    Map<String, MonitorRaid> deviceRaidMap = Maps
        .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    // 只支持linux系统
    if (!SystemUtils.IS_OS_LINUX) {
      return deviceRaidMap;
    }

    Process process = null;
    try {
      ProcessBuilder builder = new ProcessBuilder("sh", "-c",
          cmdDiskRaid + " -LdPdInfo -aALL -NoLog"
              + " | grep -Ei \"(Virtual Drive|Name|RAID Level|^Size|^State)\"");
      builder.redirectErrorStream(true);
      process = builder.start();
      parseRaidInfo(deviceRaidMap, process);

      process.waitFor();
    } catch (InterruptedException e) {
      LOGGER.info("fetch Raid info has been interrupt.");
      Thread.currentThread().interrupt();
    } catch (IOException e) {
      LOGGER.warn("fetch Raid info failed.", e);
    } finally {
      if (process != null) {
        process.destroy();
      }
    }

    return deviceRaidMap;
  }

  public static Map<String, String> fetchRaidLevelInfo(String cmdDiskRaid) {

    Map<String, String> deviceRaidMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    // 只支持linux系统
    if (!SystemUtils.IS_OS_LINUX) {
      return deviceRaidMap;
    }

    Process process = null;
    try {
      ProcessBuilder builder = new ProcessBuilder(cmdDiskRaid, "-LdPdInfo", "-aALL", "-NoLog");
      builder.redirectErrorStream(true);
      process = builder.start();
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(process.getInputStream(), Charsets.UTF_8))) {
        String raidNo = "";
        String raidLevel = "";
        String line;
        while ((line = reader.readLine()) != null) {
          LOGGER.trace("Detect RAID level from \" {} \".", line);

          if (StringUtils.contains(line, "Virtual Drive:")) {
            raidNo = StringUtils
                .substringAfter(StringUtils.substringBefore(line, "("), "Virtual Drive:").trim();
          } else if (StringUtils.contains(line, "Primary-")) {
            raidLevel = StringUtils
                .substringAfter(StringUtils.substringBefore(line, ","), "Primary-").trim();
            deviceRaidMap.put(raidNo, raidLevel);
          }
        }
      }

      process.waitFor();
    } catch (InterruptedException e) {
      LOGGER.info("fetch RAID level has been interrupt.");
      Thread.currentThread().interrupt();
    } catch (IOException e) {
      LOGGER.warn("fetch RAID level failed.", e);
    } finally {
      if (process != null) {
        process.destroy();
      }
    }
    return deviceRaidMap;
  }

  /**
   * 
   * @return
   */
  public static Map<String, MonitorRaidDisk> fetchDiskInfo(String cmdDiskRaid) {

    Map<String, MonitorRaidDisk> deviceDiskMap = Maps
        .newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    // 只支持linux系统
    if (!SystemUtils.IS_OS_LINUX) {
      return deviceDiskMap;
    }

    Process process = null;
    try {
      ProcessBuilder builder = new ProcessBuilder("sh", "-c",
          cmdDiskRaid + " -PDList -aALL -NoLog"
              + " | grep -Ei \"(Enclosure Device|Slot Number|Raw Size|Firmware state|WWN"
              + "|Drive's position|error|array|failure|Media Type|Raw Size|Foreign State)\"");
      builder.redirectErrorStream(true);
      process = builder.start();
      parseRaidDiskInfo(deviceDiskMap, process);

      process.waitFor();
    } catch (InterruptedException e) {
      LOGGER.info("fetch Disk info has been interrupt.");
      Thread.currentThread().interrupt();
    } catch (IOException e) {
      LOGGER.warn("fetch Disk info failed.", e);
    } finally {
      if (process != null) {
        process.destroy();
      }
    }

    return deviceDiskMap;
  }

  /**
   * 提取磁盘重建进度
   * 
   * @param devicdId
   * @param slotNo
   * @return
   */
  public static String fetchRebuildProgress(String cmdDiskRaid, String deviceId, String slotNo) {

    String progress = "";

    // 只支持linux系统
    if (!SystemUtils.IS_OS_LINUX) {
      return progress;
    }
    Process process = null;
    try {
      ProcessBuilder builder = new ProcessBuilder(cmdDiskRaid, "-pdrbld", "-showprog",
          "-physdrv[" + deviceId + ":" + slotNo + "]", "-aALL", "-NoLog");
      builder.redirectErrorStream(true);
      process = builder.start();
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(process.getInputStream(), Charsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          LOGGER.trace("Detect disk rebuild progress from \" {} \".", line);

          /*
           * 例子：
           * 
           * MegaCli64 -pdrbld -showprog -physdrv[32:7] -aALL
           * 
           * Rebuild Progress on Device at Enclosure 32, Slot 7 Completed 82% in 361 Minutes
           * 
           * 
           * Completed对应的百分比就是重建的进度
           */
          String response = "Rebuild Progress on Device at Enclosure " + deviceId + ", Slot "
              + slotNo + " Completed";
          if (StringUtils.contains(line, response)) {
            String temp = StringUtils.substringAfter(line, response).trim();
            progress = StringUtils.substringBefore(temp, "in").trim();
            break;
          }
        }
      }

      process.waitFor();
    } catch (InterruptedException e) {
      LOGGER.info("fetch rebuild process has been interrupt.");
      Thread.currentThread().interrupt();
    } catch (IOException e) {
      LOGGER.warn("fetch rebuild process failed.", e);
    } finally {
      if (process != null) {
        process.destroy();
      }
    }

    return progress;
  }

  /**
   * 提取磁盘回拷进度
   * @param cmdDiskRaid
   * @param deviceId
   * @param slotNo
   * @return
   */
  public static String fetchCopybackProgress(String cmdDiskRaid, String deviceId, String slotNo) {

    String progress = "";

    // 只支持linux系统
    if (!SystemUtils.IS_OS_LINUX) {
      return progress;
    }

    Process process = null;
    try {
      ProcessBuilder builder = new ProcessBuilder(cmdDiskRaid, "-pdcpybk", "-showprog",
          "-physdrv[" + deviceId + ":" + slotNo + "]", "-aALL", "-NoLog");
      builder.redirectErrorStream(true);
      process = builder.start();
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(process.getInputStream(), Charsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          LOGGER.trace("Detect disk copyback progress from \" {} \".", line);

          /*
           * 例子：
           * 
           * MegaCli64 -PDCpyBk -ShowProg -PhysDrv[0:18] -a0
           * 
           * Copyback Progress on Device at Enclosure 0, Slot 18 Completed 4% in 3 Minutes.
           * 
           * 
           * Completed对应的百分比就是回拷的进度
           */
          String response = "Copyback Progress on Device at Enclosure " + deviceId + ", Slot "
              + slotNo + " Completed";
          if (StringUtils.contains(line, response)) {
            String temp = StringUtils.substringAfter(line, response).trim();
            progress = StringUtils.substringBefore(temp, "in").trim();
            break;
          }
        }
      }

      process.waitFor();
    } catch (InterruptedException e) {
      LOGGER.info("fetch copyback process has been interrupt.");
      Thread.currentThread().interrupt();
    } catch (IOException e) {
      LOGGER.warn("fetch copyback process failed.", e);
    } finally {
      if (process != null) {
        process.destroy();
      }
    }

    return progress;
  }

  /**
   * 热插拔热备盘后需要走此流程
   * @param cmdDiskRaid
   * @param state
   * @param foreignState
   * @param deviceId
   * @param slotNo
   */
  public static void recoveryHotSpare(String cmdDiskRaid, String state, String foreignState,
      String deviceId, String slotNo) {

    if (StringUtils.equalsIgnoreCase(MetricConstants.DEVICE_DISK_STATE_UNCONFIGURED_GOOD, state)
        && StringUtils.equalsIgnoreCase(MetricConstants.DEVICE_DISK_FOREIGN_STATE_NONE,
            foreignState)) {
      // 新盘需要设置为全局热备盘
      MonitorRaidDiskHelper.toHotSpare(cmdDiskRaid, deviceId, slotNo);
    } else if (StringUtils.equalsIgnoreCase(MetricConstants.DEVICE_DISK_STATE_UNCONFIGURED_GOOD,
        state)
        && StringUtils.equalsIgnoreCase(MetricConstants.DEVICE_DISK_FOREIGN_STATE_FOREIGN,
            foreignState)) {
      // 清除外部配置
      MonitorRaidDiskHelper.clearForeignConfiguration(cmdDiskRaid);
      // 有外部配置的盘，清除外部配置后，设置为热备盘
      MonitorRaidDiskHelper.toHotSpare(cmdDiskRaid, deviceId, slotNo);
    } else if (StringUtils.equalsIgnoreCase(MetricConstants.DEVICE_DISK_STATE_UNCONFIGURED_BAD,
        state)) {
      // 清除外部配置
      MonitorRaidDiskHelper.clearForeignConfiguration(cmdDiskRaid);
      // 有外部配置的盘，清除外部配置后，设置为热备盘
      MonitorRaidDiskHelper.toHotSpare(cmdDiskRaid, deviceId, slotNo);
    }
  }

  /**
   * 更换的数据盘为脏盘时走该流程
   * 更换的数据盘为新盘会自动回拷不需要执行此流程
   * @param cmdDiskRaid
   * @param deviceId
   * @param slotNo
   */
  public static void recoveryDataDisk(String cmdDiskRaid, String state, String foreignState,
      String deviceId, String slotNo) {

    if (StringUtils.equalsIgnoreCase(MetricConstants.DEVICE_DISK_STATE_UNCONFIGURED_GOOD, state)
        && StringUtils.equalsIgnoreCase(MetricConstants.DEVICE_DISK_FOREIGN_STATE_FOREIGN,
            foreignState)) {
      // 清除外部配置
      MonitorRaidDiskHelper.clearForeignConfiguration(cmdDiskRaid);

    } else if (StringUtils.equalsIgnoreCase(MetricConstants.DEVICE_DISK_STATE_UNCONFIGURED_BAD,
        state)) {
      // 将该盘状态改为Unconfigured-Good
      MonitorRaidDiskHelper.toUnconfiguredGood(cmdDiskRaid, deviceId, slotNo);

      // 清除外部配置
      MonitorRaidDiskHelper.clearForeignConfiguration(cmdDiskRaid);
    }
  }

  private static void parseRaidInfo(Map<String, MonitorRaid> deviceRaidMap, Process process)
      throws IOException {
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(process.getInputStream(), Charsets.UTF_8))) {

      MonitorRaid deviceRaid = null;
      String raidNo = "";
      String raidLevel = "";
      String line;
      while ((line = reader.readLine()) != null) {
        LOGGER.trace("Detect raid state from \" {} \".", line);

        raidNo = parseRaidNo(raidNo, line);

        if (StringUtils.contains(line, "Primary-")) {
          raidLevel = StringUtils.substringAfter(StringUtils.substringBefore(line, ","), "Primary-")
              .trim();
          if (StringUtils.isNotBlank(raidLevel)) {
            deviceRaid = deviceRaidMap.get(raidNo + "_" + raidLevel);
            if (deviceRaid == null) {
              deviceRaid = new MonitorRaid();
              deviceRaidMap.put(raidNo + "_" + raidLevel, deviceRaid);
            }
            deviceRaid.setRaidNo(raidNo);
            deviceRaid.setRaidLevel(raidLevel);
          } else {
            deviceRaid = null;
          }
          raidNo = "";
        }

        // 解析RAID状态
        parseState(deviceRaid, line);
      }
    }
  }

  /**
   * @param deviceDiskMap
   * @param process
   * @throws IOException
   */
  private static void parseRaidDiskInfo(Map<String, MonitorRaidDisk> deviceDiskMap, Process process)
      throws IOException {
    try (BufferedReader reader = new BufferedReader(
        new InputStreamReader(process.getInputStream(), Charsets.UTF_8))) {

      MonitorRaidDisk deviceDisk = null;
      String deviceId = "";
      String line;
      while ((line = reader.readLine()) != null) {
        LOGGER.trace("Detect disk state from \" {} \".", line);

        // 解析Enclosure Device ID，用于查询重建进度
        deviceId = parseDeviceId(deviceId, line);

        // 解析槽位号，通过槽位号获取或创建deviceDisk
        if (StringUtils.contains(line, "Slot Number:")) {
          String slotNo = StringUtils.substringAfter(line, "Slot Number:").trim();
          if (StringUtils.isNotBlank(slotNo)) {
            deviceDisk = deviceDiskMap.get(deviceId + "_" + slotNo);
            if (deviceDisk == null) {
              deviceDisk = new MonitorRaidDisk();
              deviceDiskMap.put(deviceId + "_" + slotNo, deviceDisk);
            }
            deviceDisk.setSlotNo(slotNo);
            deviceDisk.setDeviceId(deviceId);
            deviceDisk.setPhysicalLocation(toPhysicalLocation(deviceId));
            deviceId = "";
          } else {
            deviceDisk = null;
            deviceId = "";
          }
        }

        // 解析RAID组号
        parseRaidNo(deviceDisk, line);

        // 解析Error Count
        parseErrorCount(deviceDisk, line);

        // 解析硬盘WWN
        parseDiskWwn(deviceDisk, line);

        // 解析磁盘状态
        parseState(deviceDisk, line);

        // 解析磁盘外来状态
        parseForeignState(deviceDisk, line);

        // 解析磁盘介质
        parseMedium(deviceDisk, line);

        // 解析磁盘容量
        parseCapacity(deviceDisk, line);
      }
    }
  }

  /**
   * @param deviceDisk
   * @param line
   */
  private static void parseRaidNo(MonitorRaidDisk deviceDisk, String line) {
    if (deviceDisk != null && StringUtils.contains(line, "Drive's position: DiskGroup:")) {
      String raidNo = StringUtils
          .substringAfter(StringUtils.substringBefore(line, ","), "Drive's position: DiskGroup:")
          .trim();
      deviceDisk.setRaidNo(raidNo);
    }

    // 解析Array编号
    if (deviceDisk != null && StringUtils.contains(line, "Array #:")) {
      String arrayNo = StringUtils.substringAfter(line, "Array #:").trim();
      deviceDisk.setArrayNo(arrayNo);
    }
  }

  /**
   * @param deviceDisk
   * @param line
   */
  private static void parseState(MonitorRaidDisk deviceDisk, String line) {
    if (deviceDisk != null && StringUtils.contains(line, "Firmware state:")) {
      String state = StringUtils
          .substringAfter(StringUtils.substringBefore(line, ","), "Firmware state:").trim();
      if (StringUtils.equalsIgnoreCase(state, "Online")) {
        deviceDisk.setState(MetricConstants.DEVICE_DISK_STATE_ONLINE);
      } else if (StringUtils.equalsIgnoreCase(state, "Hotspare")) {
        deviceDisk.setState(MetricConstants.DEVICE_DISK_STATE_HOTSPARE);

        // 热备状态的磁盘从arrayNo中获取raid组编号
        deviceDisk.setRaidNo(
            StringUtils.isNotBlank(deviceDisk.getArrayNo()) ? deviceDisk.getArrayNo() : "");
      } else if (StringUtils.equalsIgnoreCase(state, "Rebuild")) {
        deviceDisk.setState(MetricConstants.DEVICE_DISK_STATE_REBUILD);
      } else if (StringUtils.equalsIgnoreCase(state, "Unconfigured(good)")) {
        deviceDisk.setState(MetricConstants.DEVICE_DISK_STATE_UNCONFIGURED_GOOD);

        // raid组编号默认为空
        deviceDisk.setRaidNo("");
      } else if (StringUtils.equalsIgnoreCase(state, "Unconfigured(bad)")) {
        deviceDisk.setState(MetricConstants.DEVICE_DISK_STATE_UNCONFIGURED_BAD);

        // raid组编号默认为空
        deviceDisk.setRaidNo("");
      } else if (StringUtils.equalsIgnoreCase(state, "Copyback")) {
        deviceDisk.setState(MetricConstants.DEVICE_DISK_STATE_COPYBACK);

        // raid组编号默认为空
        deviceDisk.setRaidNo("");
      } else {
        deviceDisk.setState(MetricConstants.DEVICE_DISK_STATE_FAILED);

        // raid组编号默认为空
        deviceDisk.setRaidNo("");
      }

    }


  }

  /**
   * @param deviceDisk
   * @param line
   */
  private static void parseForeignState(MonitorRaidDisk deviceDisk, String line) {
    if (deviceDisk != null && StringUtils.contains(line, "Foreign State:")) {
      String foreignState = StringUtils.substringAfter(line, "Foreign State:").trim();
      if (StringUtils.equalsIgnoreCase(foreignState, "None")) {
        deviceDisk.setForeignState(MetricConstants.DEVICE_DISK_FOREIGN_STATE_NONE);
      } else if (StringUtils.equalsIgnoreCase(foreignState, "Foreign")) {
        deviceDisk.setForeignState(MetricConstants.DEVICE_DISK_FOREIGN_STATE_FOREIGN);
      }
    }
  }

  /**
   * @param deviceId
   * @param line
   * @return
   */
  private static String parseDeviceId(String deviceId, String line) {
    if (StringUtils.contains(line, "Enclosure Device ID:")) {
      deviceId = StringUtils.substringAfter(line, "Enclosure Device ID:").trim();
    }
    return deviceId;
  }

  /**
   * @param deviceDisk
   * @param line
   */
  private static void parseErrorCount(MonitorRaidDisk deviceDisk, String line) {
    // 解析Media Error Count
    if (deviceDisk != null && StringUtils.contains(line, "Media Error Count:")) {
      Integer mediaErrorCount = Ints
          .tryParse(StringUtils.substringAfter(line, "Media Error Count:").trim());
      deviceDisk.setMediaErrorCount(mediaErrorCount == null ? 0 : mediaErrorCount.intValue());
    }

    // 解析Other Error Count
    if (deviceDisk != null && StringUtils.contains(line, "Other Error Count:")) {
      Integer otherErrorCount = Ints
          .tryParse(StringUtils.substringAfter(line, "Other Error Count:").trim());
      deviceDisk.setOtherErrorCount(otherErrorCount == null ? 0 : otherErrorCount.intValue());
    }

    // 解析Predictive Failure Count
    if (deviceDisk != null && StringUtils.contains(line, "Predictive Failure Count:")) {
      Integer predictiveFailureCount = Ints
          .tryParse(StringUtils.substringAfter(line, "Predictive Failure Count:").trim());
      deviceDisk.setPredictiveFailureCount(
          predictiveFailureCount == null ? 0 : predictiveFailureCount.intValue());
    }
  }

  private static void parseDiskWwn(MonitorRaidDisk deviceDisk, String line) {
    if (deviceDisk != null && StringUtils.contains(line, "WWN:")) {
      String wwn = StringUtils.substringAfter(line, "WWN:").trim();
      deviceDisk.setWwn(wwn);
    }
  }

  /**
   * @param deviceDisk
   * @param line
   */
  private static void parseMedium(MonitorRaidDisk deviceDisk, String line) {
    if (deviceDisk != null && StringUtils.contains(line, "Media Type:")) {
      String mediaType = StringUtils.substringAfter(line, "Media Type:").trim();
      if (StringUtils.equalsIgnoreCase(mediaType, "Hard Disk Device")) {
        deviceDisk.setMediaType(MetricConstants.DEVICE_DISK_MEDIUM_HDD);
      } else {
        // 我们目前不知道SSD返回的命令格式
        deviceDisk.setMediaType(MetricConstants.DEVICE_DISK_MEDIUM_SSD);
      }
    }
  }

  /**
   * @param deviceDisk
   * @param line
   */
  private static void parseCapacity(MonitorRaidDisk deviceDisk, String line) {
    if (deviceDisk != null && StringUtils.contains(line, "Raw Size:")) {
      String size = StringUtils.substringAfter(StringUtils.substringBefore(line, "["), "Raw Size:")
          .trim();
      deviceDisk.setSize(size);
    }
  }

  private static String parseRaidNo(String raidNo, String line) {
    if (StringUtils.contains(line, "Virtual Drive:")) {
      raidNo = StringUtils.substringAfter(StringUtils.substringBefore(line, "("), "Virtual Drive:")
          .trim();
    }
    return raidNo;
  }

  private static void parseState(MonitorRaid deviceRaid, String line) {
    if (deviceRaid != null && StringUtils.contains(line, "State")) {
      String state = StringUtils.substringAfter(line, ":").trim();
      if (StringUtils.equalsIgnoreCase(state, "Optimal")) {
        deviceRaid.setState(MetricConstants.DEVICE_RAID_STATE_OPTIMAL);
      } else if (StringUtils.equalsIgnoreCase(state, "Partially Degraded")) {
        deviceRaid.setState(MetricConstants.DEVICE_RAID_STATE_PARTIALLY_DEGRADED);
      } else if (StringUtils.equalsIgnoreCase(state, "Degraded")) {
        deviceRaid.setState(MetricConstants.DEVICE_RAID_STATE_DEGRADED);
      } else if (StringUtils.equalsIgnoreCase(state, "Offline")) {
        deviceRaid.setState(MetricConstants.DEVICE_RAID_STATE_OFFLINE);
      } else {
        deviceRaid.setState(MetricConstants.DEVICE_RAID_STATE_FAULT);
      }
    }
  }

  private static String toPhysicalLocation(String deviceId) {
    if (StringUtils.equals(DEVICE_FRONT_PANEL_ID, deviceId)) {
      return DEVICE_FRONT_PANEL;
    } else if (StringUtils.equals(DEVICE_REAR_PANEL_ID, deviceId)) {
      return DEVICE_REAR_PANEL;
    }

    return "";
  }

  /**
   * 新盘更换热备盘需要重新将新盘设置为全局热备盘
   * @param cmdDiskRaid
   * @param deviceId
   * @param slotNo
   */
  private static void toHotSpare(String cmdDiskRaid, String deviceId, String slotNo) {

    // 只支持linux系统
    if (!SystemUtils.IS_OS_LINUX) {
      return;
    }

    Process process = null;
    try {
      ProcessBuilder builder = new ProcessBuilder(cmdDiskRaid, "-pdhsp", "-set",
          "-physdrv[" + deviceId + ":" + slotNo + "]", "-a0", "-NoLog");
      builder.redirectErrorStream(true);
      process = builder.start();
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(process.getInputStream(), Charsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          LOGGER.info(
              "disk state conversion to hot spare, device id is {}, slot number is {},  \" {} \".",
              deviceId, slotNo, line);
        }
      }
      process.waitFor();
    } catch (InterruptedException e) {
      LOGGER.info("conversion to hot spare has been interrupt.");
      Thread.currentThread().interrupt();
    } catch (IOException e) {
      LOGGER.warn("conversion to hot spare failed.", e);
    } finally {
      if (process != null) {
        process.destroy();
      }
    }
  }

  /**
   * 数据盘或者热备盘热插拔后硬盘状态会变为Unconfigured(bad)
   * 使用MegaCli64 -PDMakeGood -PhysDrv[0:17] -a0
   * 会将硬盘状态由Unconfigured(bad)变更为Unconfigured(good)
   * @param cmdDiskRaid
   */
  private static void toUnconfiguredGood(String cmdDiskRaid, String deviceId, String slotNo) {

    // 只支持linux系统
    if (!SystemUtils.IS_OS_LINUX) {
      return;
    }

    Process process = null;
    try {
      ProcessBuilder builder = new ProcessBuilder(cmdDiskRaid, "-PDMakeGood",
          "-physdrv[" + deviceId + ":" + slotNo + "]", "-a0", "-NoLog");
      builder.redirectErrorStream(true);
      process = builder.start();
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(process.getInputStream(), Charsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          LOGGER.info(
              "disk state conversion to unconfigured good , device id is {}, slot number is {},  \" {} \".",
              deviceId, slotNo, line);
        }
      }
      process.waitFor();
    } catch (InterruptedException e) {
      LOGGER.info("unconfigured bad to unconfigured good has been interrupt.");
      Thread.currentThread().interrupt();
    } catch (IOException e) {
      LOGGER.warn("unconfigured bad to unconfigured good failed.", e);
    } finally {
      if (process != null) {
        process.destroy();
      }
    }
  }

  /**
   * 导入外部配置
   * 如果是热插拔，用MegaCli64 -cfgforeign -Import -a0
   * 如果是新盘，用MegaCli64 -pdhsp -set -physdrv[0:19] -a0
   */
  @SuppressWarnings("unused")
  private static void importForeignConfiguration(String cmdDiskRaid) {

    // 只支持linux系统
    if (!SystemUtils.IS_OS_LINUX) {
      return;
    }

    Process process = null;
    try {
      ProcessBuilder builder = new ProcessBuilder(cmdDiskRaid, "-CfgForeign", "-import", "-a0",
          "-NoLog");
      builder.redirectErrorStream(true);
      process = builder.start();
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(process.getInputStream(), Charsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          LOGGER.info("imports the foreign configuration, \" {} \".", line);
        }
      }
      process.waitFor();
    } catch (InterruptedException e) {
      LOGGER.info("import foreign configuration has been interrupt.");
      Thread.currentThread().interrupt();
    } catch (IOException e) {
    } finally {
      if (process != null) {
        process.destroy();
      }
    }
  }

  /**
   * 数据盘Firmware state值为Unconfigured(good), Foreign State的值为Foreign时使用此命令清除外部配置
   * 使用此命令后，如果热备盘重建完成后，数据盘会自动变为回拷状态
   * 清除外部配置
   * @param cmdDiskRaid
   */
  private static void clearForeignConfiguration(String cmdDiskRaid) {

    // 只支持linux系统
    if (!SystemUtils.IS_OS_LINUX) {
      return;
    }

    Process process = null;
    try {
      ProcessBuilder builder = new ProcessBuilder(cmdDiskRaid, "-CfgForeign", "-clear", "-a0",
          "-NoLog");
      builder.redirectErrorStream(true);
      process = builder.start();
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(process.getInputStream(), Charsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          LOGGER.info("clears the foreign configuration, \" {} \".", line);
        }
      }
      process.waitFor();
    } catch (InterruptedException e) {
      LOGGER.info("clear foreign configuration has been interrupt.");
      Thread.currentThread().interrupt();
    } catch (IOException e) {
      LOGGER.warn("clear foreign configuration failed.", e);
    } finally {
      if (process != null) {
        process.destroy();
      }
    }
  }

}
