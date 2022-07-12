package com.scorpio.metric.system.helper;

import com.google.common.base.Charsets;
import com.scorpio.metric.system.data.MonitorProcess;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public final class MonitorProcessHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(MonitorProcessHelper.class);

  public static MonitorProcess fetchProcessInfo(String processName) {

    MonitorProcess processUsage = new MonitorProcess(processName);

    if (!SystemUtils.IS_OS_LINUX) {
      return processUsage;
    }

    Process process = null;
    try {
      // 执行管道必须调用shell，在该shell中运行命令。
      ProcessBuilder builder = new ProcessBuilder("sh", "-c", "ps -aux | grep " + processName);
      builder.redirectErrorStream(true);
      process = builder.start();
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(process.getInputStream(), Charsets.UTF_8))) {
        String line;
        // 字符串解析获得进程信息
        while ((line = reader.readLine()) != null) {
          LOGGER.trace("Detect process info from {}.", line);
          if (StringUtils.contains(line, "/" + processName)) {
            String[] splits = StringUtils.split(line, " ");
            if (splits.length > 3) {
              processUsage.setCpuMetric(splits[2].trim());
              processUsage.setMemoryMetric(splits[3].trim());
            }
          }
        }
      }

      process.waitFor();
    } catch (InterruptedException e) {
      LOGGER.info("fetch process info has been interrupt.");
      Thread.currentThread().interrupt();
    } catch (IOException e) {
      LOGGER.warn("fetch process info failed.", e);
    } finally {
      if (process != null) {
        process.destroy();
      }
    }
    return processUsage;
  }


}
