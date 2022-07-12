package com.scorpio.metric.system.helper;

import com.google.common.collect.Maps;
import com.scorpio.metric.system.data.MonitorNetwork;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MonitorNetifHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(MonitorNetifHelper.class);

  private static final Pattern NET_DEV_PATTERN = Pattern
      .compile("^ *([A-Za-z0-9]*):\\D*(\\d+)\\D+(\\d+)\\D+(\\d+)\\D+(\\d+)\\D+\\d+\\D+\\d+\\D+\\d+"
          + "\\D+\\d+\\D+(\\d+)\\D+(\\d+)\\D+(\\d+)\\D+(\\d+)\\D+\\d+\\D+\\d+\\D+\\d+\\D+\\d+.*");

  private MonitorNetifHelper() {
  }

  public static Map<String, MonitorNetwork> monitorNetifTraffic(Collection<String> netifNameList) {

    Map<String, MonitorNetwork> trafficMap = Maps.newLinkedHashMapWithExpectedSize(netifNameList.size());

    // 只支持linux系统，其他系统返回
    if (!SystemUtils.IS_OS_LINUX) {
      return trafficMap;
    }

    // 解析接口统计文件
    String line = "";
    try (BufferedReader reader = new BufferedReader(new InputStreamReader(
        java.nio.file.Files.newInputStream(Paths.get("/proc/net/dev")), StandardCharsets.UTF_8))) {
      while ((line = reader.readLine()) != null) {
        Matcher m = NET_DEV_PATTERN.matcher(line);
        // 若网卡名列表为空，取出所有网卡数据
        if (m.matches() && (CollectionUtils.isEmpty(netifNameList)
            || netifNameList.contains(m.group(1).toLowerCase(Locale.US)))) {

          long bytesRx = Long.parseLong(m.group(2));
          long packetsRx = Long.parseLong(m.group(3));
          long bytesTx = Long.parseLong(m.group(6));
          long packetsTx = Long.parseLong(m.group(7));

          MonitorNetwork networkTraffic = new MonitorNetwork();
          networkTraffic.setNetifName(m.group(1).toLowerCase(Locale.US));
          networkTraffic.setBytesRx(bytesRx);
          networkTraffic.setBytesTx(bytesTx);
          networkTraffic.setPacketsRx(packetsRx);
          networkTraffic.setPacketsTx(packetsTx);

          trafficMap.put(networkTraffic.getNetifName(), networkTraffic);
        }
      }
    } catch (NumberFormatException | IOException e) {
      LOGGER.warn("Fail to parse netif metric.", e);
    }
    return trafficMap;
  }

  /**
   * @param netifName
   * @return
   */
  public static boolean detectNetifState(String netifName) {
    boolean isUp = false;

    // 只支持linux系统
    if (!SystemUtils.IS_OS_LINUX) {
      return isUp;
    }

    Process process = null;
    try {
      ProcessBuilder builder = new ProcessBuilder("ip", "link", "ls", netifName);
      builder.redirectErrorStream(true);
      process = builder.start();
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          LOGGER.trace("Detect netif state from \" {} \".", line);
          if (StringUtils.contains(line, " UP ")) {
            isUp = true;
            break;
          }
        }
      }

      process.waitFor();
    } catch (InterruptedException e) {
      LOGGER.info("detect netif state has bean interrupt.");
      Thread.currentThread().interrupt();
    } catch (IOException e) {
      LOGGER.warn("detect netif state failed.", e);
    } finally {
      if (process != null) {
        process.destroy();
      }
    }

    return isUp;
  }

}
