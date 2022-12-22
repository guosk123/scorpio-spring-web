package com.machloop.fpc.manager.system.service.impl;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.metric.system.data.MonitorCpuTimes;
import com.machloop.alpha.common.metric.system.data.MonitorMemory;
import com.machloop.alpha.common.metric.system.helper.MonitorSystemHelper;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.webapp.system.service.DeviceNtpService;
import com.machloop.fpc.manager.system.dao.MonitorMetricDao;
import com.machloop.fpc.manager.system.dao.MonitorMetricDataDao;
import com.machloop.fpc.manager.system.data.MonitorMetricDO;
import com.machloop.fpc.manager.system.data.MonitorMetricDataDO;
import com.machloop.fpc.manager.system.service.MonitorMetricDataService;

/**
 * @author liyongjun
 *
 * create at 2019年9月16日, fpc-manager
 */
@Service
public class MonitorMetricServiceImpl implements MonitorMetricDataService {

  // 记录前一次CPU统计，用于计算最新使用率
  private MonitorCpuTimes previousCpuTime;

  @Autowired
  private MonitorMetricDataDao monitorMetricDataDao;

  // 查询总内存大小
  @Autowired
  private MonitorMetricDao monitorMetricDao;

  @Autowired
  private DeviceNtpService deviceNtpService;

  /**
   * @see com.machloop.fpc.manager.system.service.MonitorMetricDataService#queryRuntimeEnvironment()
   */
  @Override
  public Map<String, Object> queryRuntimeEnvironment() {
    Map<String, Object> restMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    restMap.put("uptime", MonitorSystemHelper.fetchSystemRuntimeSecond());

    Map<String, String> timeMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    timeMap.put("dateTime", deviceNtpService.queryDeviceNtp().getDateTime());
    timeMap.put("timeZone", deviceNtpService.queryDeviceNtp().getTimeZone());
    restMap.put("systemTime", timeMap);

    restMap.put("systemFsUsedRatio", MonitorSystemHelper
        .fetchFilesystemUsagePctLong(HotPropertiesHelper.getProperty("filesys.mount.root.system")));
    restMap.put("indexFsUsedRatio", MonitorSystemHelper
        .fetchFilesystemUsagePctLong(HotPropertiesHelper.getProperty("filesys.mount.root.index")));
    restMap.put("metadataFsUsedRatio", MonitorSystemHelper.fetchFilesystemUsagePctLong(
        HotPropertiesHelper.getProperty("filesys.mount.root.metadata")));

    return restMap;
  }

  /**
   * @see com.machloop.fpc.manager.system.service.MonitorMetricDataService#statMonitorMetricData(java.util.Date, java.util.Date, int)
   */
  @Override
  public List<Map<String, Object>> statMonitorMetricData(String startTime, String endTime,
      int interval) {
    Date startTimeDate = DateUtils.parseISO8601Date(startTime);
    Date endTimeDate = DateUtils.parseISO8601Date(endTime);

    List<MonitorMetricDataDO> monitorMetricDOList = monitorMetricDataDao
        .queryMonitorMetricData(startTimeDate, endTimeDate, interval);
    List<Map<String, Object>> resultList = Lists
        .newArrayListWithCapacity(monitorMetricDOList.size());
    List<MonitorMetricDO> monitorMetricDOS = monitorMetricDao.queryMonitorMetrics();

    for (MonitorMetricDataDO monitorMetricDO : monitorMetricDOList) {
      Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      resultMap.put("cpuUsedRatio", monitorMetricDO.getCpuUsedRatio());
      resultMap.put("memoryUsedRatio", monitorMetricDO.getMemoryUsedRatio());
      resultMap.put("systemFsUsedRatio", monitorMetricDO.getSystemFsUsedRatio());
      resultMap.put("systemFsFree", monitorMetricDO.getSystemFsFree());
      resultMap.put("indexFsUsedRatio", monitorMetricDO.getIndexFsUsedRatio());
      resultMap.put("indexFsFree", monitorMetricDO.getIndexFsFree());
      resultMap.put("metadataFsUsedRatio", monitorMetricDO.getMetadataFsUsedRatio());
      resultMap.put("metadataFsFree", monitorMetricDO.getMetadataFsFree());
      resultMap.put("metadataHotFsUsedRatio", monitorMetricDO.getMetadataHotFsUsedRatio());
      resultMap.put("metadataHotFsFree", monitorMetricDO.getMetadataHotFsFree());
      resultMap.put("packetFsUsedRatio", monitorMetricDO.getPacketFsUsedRatio());
      resultMap.put("packetFsFree", monitorMetricDO.getPacketFsFree());
      resultMap.put("timestamp", DateUtils.toStringISO8601(monitorMetricDO.getTimestamp()));
      addTotalFsUsedRatio(monitorMetricDOS, resultMap);

      resultList.add(resultMap);
    }

    return resultList;
  }

  @Override
  public List<Map<String, Object>> queryLatestStatMonitorMetricData() {

    List<MonitorMetricDataDO> monitorMetricDOList = monitorMetricDataDao
        .queryLatestMonitorMetricData();
    List<Map<String, Object>> resultList = Lists
        .newArrayListWithCapacity(monitorMetricDOList.size());
    List<MonitorMetricDO> monitorMetricDOS = monitorMetricDao.queryMonitorMetrics();

    for (MonitorMetricDataDO monitorMetricDO : monitorMetricDOList) {
      Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      resultMap.put("cpuUsedRatio", monitorMetricDO.getCpuUsedRatio());
      resultMap.put("memoryUsedRatio", monitorMetricDO.getMemoryUsedRatio());
      resultMap.put("systemFsUsedRatio", monitorMetricDO.getSystemFsUsedRatio());
      resultMap.put("indexFsUsedRatio", monitorMetricDO.getIndexFsUsedRatio());
      resultMap.put("metadataFsUsedRatio", monitorMetricDO.getMetadataFsUsedRatio());
      resultMap.put("metadataHotFsUsedRatio", monitorMetricDO.getMetadataHotFsUsedRatio());
      resultMap.put("packetFsUsedRatio", monitorMetricDO.getPacketFsUsedRatio());
      resultMap.put("timestamp", DateUtils.toStringISO8601(monitorMetricDO.getTimestamp()));
      addTotalFsUsedRatio(monitorMetricDOS, resultMap);

      resultList.add(resultMap);
    }

    return resultList;
  }

  /**
   * restapi 系统总使用率
   * @param monitorMetricDOS 系统空间相关信息
   * @param resultMap
   */
  private void addTotalFsUsedRatio(List<MonitorMetricDO> monitorMetricDOS,
      Map<String, Object> resultMap) {
    long totalFs = 0;
    Map<String, Long> totalFsMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    Iterator<MonitorMetricDO> iterator = monitorMetricDOS.iterator();
    while (iterator.hasNext()) {
      MonitorMetricDO monitorMetricDO = iterator.next();
      if (StringUtils.equalsAny(monitorMetricDO.getMetricName(), "fs_index_total_byte",
          "fs_metadata_total_byte", "fs_system_total_byte", "fs_metadata_hot_total_byte")) {
        totalFsMap.put(monitorMetricDO.getMetricName(),
            Long.valueOf(monitorMetricDO.getMetricValue()));
        totalFs += Long.valueOf(monitorMetricDO.getMetricValue());
      }
    }

    // 计算已使用大小
    Long fsUsed = totalFsMap.get("fs_index_total_byte")
        * (Integer) resultMap.get("indexFsUsedRatio")
        + totalFsMap.get("fs_metadata_total_byte") * (Integer) resultMap.get("metadataFsUsedRatio")
        + totalFsMap.get("fs_metadata_hot_total_byte")
            * (Integer) resultMap.get("metadataHotFsUsedRatio")
        + totalFsMap.get("fs_system_total_byte") * (Integer) resultMap.get("systemFsUsedRatio");
    // 系统内存总使用率
    int totalFsUsedRatio = 100 - Math.round(100 * (totalFs - fsUsed / 100) / totalFs);

    resultMap.put("totalFsUsedRatio", totalFsUsedRatio);
  }

  /**
   * @see com.machloop.fpc.manager.system.service.MonitorMetricDataService#produceMonitorMetricData()
   */
  @Override
  public MonitorMetricDataDO produceMonitorMetricData() {

    // 整理metricDate为60秒的整倍数
    double multipleOfInterval = ((double) DateUtils.now().getTime() / 1000)
        / Constants.ONE_MINUTE_SECONDS;
    long roundValue = Math.round(multipleOfInterval);
    Date metricTime = new Date(roundValue * Constants.ONE_MINUTE_SECONDS * 1000);

    MonitorMetricDataDO monitorMetricDO = new MonitorMetricDataDO();

    // CPU采样
    MonitorCpuTimes cpuTimes = MonitorSystemHelper.fetchCpuTimes();
    // 首次采样不做统计
    if (previousCpuTime == null) {
      monitorMetricDO.setCpuUsedRatio(0);
    } else {
      monitorMetricDO.setCpuUsedRatio((int) cpuTimes.getCpuUsagePctLong(previousCpuTime));
    }
    previousCpuTime = cpuTimes;

    // 内存采样
    MonitorMemory memStat = MonitorSystemHelper.fetchPhysicalMemory();
    monitorMetricDO.setMemoryUsedRatio((int) memStat.getMemUsagePctLong());

    // 系统区使用率统计
    monitorMetricDO.setSystemFsUsedRatio((int) MonitorSystemHelper
        .fetchFilesystemUsagePctLong(HotPropertiesHelper.getProperty("filesys.mount.root.system")));

    // 索引区使用率统计
    monitorMetricDO.setIndexFsUsedRatio((int) MonitorSystemHelper
        .fetchFilesystemUsagePctLong(HotPropertiesHelper.getProperty("filesys.mount.root.index")));

    // 元数据区使用率统计
    int metadataFsUsedRatio = (int) MonitorSystemHelper.fetchFilesystemUsagePctLong(
        HotPropertiesHelper.getProperty("filesys.mount.root.metadata"));
    monitorMetricDO.setMetadataFsUsedRatio(metadataFsUsedRatio);

    monitorMetricDO.setTimestamp(metricTime);

    // 统计数据存入clickhouse
    monitorMetricDataDao.saveMonitorMetricData(monitorMetricDO);

    return monitorMetricDO;
  }

}
