package com.machloop.fpc.manager.system.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
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
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.webapp.WebappConstants;
import com.machloop.alpha.webapp.base.AlarmHelper;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.alpha.webapp.system.bo.AlarmSettingBO;
import com.machloop.alpha.webapp.system.bo.AlarmSettingBO.FireCriteria;
import com.machloop.alpha.webapp.system.service.AlarmSettingService;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.statistics.bo.TimeseriesBO;
import com.machloop.fpc.manager.statistics.service.RrdService;
import com.machloop.fpc.manager.system.bo.MonitorMetricBO;
import com.machloop.fpc.manager.system.dao.MonitorMetricDao;
import com.machloop.fpc.manager.system.data.MetricNetworkTraffic;
import com.machloop.fpc.manager.system.data.MonitorMetricDO;
import com.machloop.fpc.manager.system.service.SystemMetricService;

/**
 * @author liumeng
 *
 * create at 2018年12月14日, fpc-manager
 */
@Service
public class SystemMetricServiceImpl implements SystemMetricService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SystemMetricServiceImpl.class);

  private final Map<String,
      Date> lastAlarmMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  public static final int BLOCK_SIZE = 1000;

  @Autowired
  private MonitorMetricDao monitorMetricDao;

  @Autowired
  private RrdService rrdService;

  @Autowired
  private AlarmSettingService alarmSettingService;

  @Autowired
  private GlobalSettingService globalSettingService;

  // 记录前一次CPU统计，用于计算最新使用率
  private MonitorCpuTimes previousCpuTime;

  // Rrd统计，用于计算rrd区间的平均速率
  private MonitorCpuTimes previousStatCpuTime;

  private MetricNetworkTraffic perviousCpuRrdElement;

  private MetricNetworkTraffic perviousMemRrdElement;

  /**
   * @see com.machloop.fpc.manager.system.service.SystemMetricService#queryDeviceCustomInfo()
   */
  @Override
  public Map<String, Object> queryDeviceCustomInfo() {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    String deviceId = globalSettingService.getValue(WebappConstants.GLOBAL_SETTING_DEVICE_ID);
    if (StringUtils.isBlank(deviceId)) {
      deviceId = IdGenerator.generateUUID().replace("-", "_");
      globalSettingService.setValue(WebappConstants.GLOBAL_SETTING_DEVICE_ID, deviceId);
    }
    result.put(WebappConstants.GLOBAL_SETTING_DEVICE_ID, deviceId);
    result.put(WebappConstants.GLOBAL_SETTING_DEVICE_NAME,
        globalSettingService.getValue(WebappConstants.GLOBAL_SETTING_DEVICE_NAME, false));

    return result;
  }

  /**
   * @see com.machloop.fpc.manager.system.service.SystemMetricService#queryMonitorMetrics()
   */
  @Override
  public List<MonitorMetricBO> queryMonitorMetrics() {

    List<MonitorMetricDO> monitorMetricDOList = monitorMetricDao.queryMonitorMetrics();

    List<MonitorMetricBO> monitorMetricBOList = Lists
        .newArrayListWithCapacity(monitorMetricDOList.size());
    for (MonitorMetricDO monitorMetricDO : monitorMetricDOList) {

      MonitorMetricBO monitorMetricBO = new MonitorMetricBO();
      BeanUtils.copyProperties(monitorMetricDO, monitorMetricBO);

      monitorMetricBOList.add(monitorMetricBO);
    }
    return monitorMetricBOList;
  }

  /**
   * @see com.machloop.fpc.manager.system.service.SystemMetricService#queryCpuMemUsages(
   *                                                        java.util.Date, java.util.Date)
   */
  @Override
  public Map<String, TimeseriesBO> queryCpuMemUsages(String interval, Date startTimeDate,
      Date endTimeDate) {
    TimeseriesBO cpu = rrdService.queryRrdHistogram(FpcConstants.STAT_METRIC_CPU_RRD, interval,
        startTimeDate, endTimeDate);
    TimeseriesBO memory = rrdService.queryRrdHistogram(FpcConstants.STAT_METRIC_MEMORY_RRD,
        interval, startTimeDate, endTimeDate);
    Map<String, TimeseriesBO> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("cpu", cpu);
    map.put("memory", memory);
    return map;
  }

  /**
   * @see com.machloop.fpc.manager.system.service.SystemMetricService#produceMonitorMetric(
   *                                                                        java.util.Date)
   */
  public int produceMonitorMetric(Date metricDatetime) {

    int update = 0;

    if (!SystemUtils.IS_OS_LINUX) {
      return update;
    }

    // 查询告警配置
    Map<String, AlarmSettingBO> alarmSettings = alarmSettingService.queryAlarmSettingsByCache();
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(JsonHelper.serialize(alarmSettings));
    }

    MonitorMetricDO monitorMetric = new MonitorMetricDO();

    // CPU采样
    MonitorCpuTimes cpuTimes = MonitorSystemHelper.fetchCpuTimes();
    // 首次采样不做统计
    if (previousCpuTime != null) {
      monitorMetric.setMetricName(FpcConstants.MONITOR_METRIC_CPU_USED_PCT);
      monitorMetric.setMetricValue(cpuTimes.getCpuUsagePct(previousCpuTime));
      monitorMetric.setMetricTime(metricDatetime);
      update += monitorMetricDao.saveOrUpdateMonitorMetric(monitorMetric);

      alarmMonitoring(alarmSettings.get(WebappConstants.ALARM_SETTING_SOURCE_TYPE_CPU),
          WebappConstants.ALARM_SETTING_METRIC_CPU_USAGE, "CPU使用率",
          Long.parseLong(StringUtils.substringBefore(monitorMetric.getMetricValue(), "%")));
    }
    previousCpuTime = cpuTimes;

    // 内存采样
    MonitorMemory memStat = MonitorSystemHelper.fetchPhysicalMemory();
    monitorMetric = new MonitorMetricDO();
    monitorMetric.setMetricName(FpcConstants.MONITOR_METRIC_MEM_USED_PCT);
    monitorMetric.setMetricValue(memStat.getMemUsagePct());
    monitorMetric.setMetricTime(metricDatetime);
    update += monitorMetricDao.saveOrUpdateMonitorMetric(monitorMetric);

    alarmMonitoring(alarmSettings.get(WebappConstants.ALARM_SETTING_SOURCE_TYPE_MEMORY),
        WebappConstants.ALARM_SETTING_METRIC_MEMORY_USAGE, "内存使用率",
        Long.parseLong(StringUtils.substringBefore(monitorMetric.getMetricValue(), "%")));

    // 系统分区使用率统计
    String systemFilesysUsed = MonitorSystemHelper
        .fetchFilesystemUsagePct(HotPropertiesHelper.getProperty("filesys.mount.root.system"));
    if (StringUtils.isNotBlank(systemFilesysUsed)) {
      monitorMetric = new MonitorMetricDO();
      monitorMetric.setMetricName(FpcConstants.MONITOR_METRIC_FS_SYS_USED_PCT);
      monitorMetric.setMetricValue(systemFilesysUsed);
      monitorMetric.setMetricTime(metricDatetime);
      update += monitorMetricDao.saveOrUpdateMonitorMetric(monitorMetric);

      long systemFilesFree = MonitorSystemHelper
          .fetchFilesystemFreeSpace(HotPropertiesHelper.getProperty("filesys.mount.root.system"));
      alarmMonitoring(alarmSettings.get(WebappConstants.ALARM_SETTING_SOURCE_TYPE_DISK),
          WebappConstants.ALARM_SETTING_METRIC_SYSTEM_PARTITION_FREE, "系统分区剩余空间",
          systemFilesFree / BLOCK_SIZE / BLOCK_SIZE);
    }

    // 索引分区使用率统计
    String indexFilesysUsed = MonitorSystemHelper
        .fetchFilesystemUsagePct(HotPropertiesHelper.getProperty("filesys.mount.root.index"));
    if (StringUtils.isNotBlank(indexFilesysUsed)) {
      monitorMetric = new MonitorMetricDO();
      monitorMetric.setMetricName(FpcConstants.MONITOR_METRIC_FS_IDX_USED_PCT);
      monitorMetric.setMetricValue(indexFilesysUsed);
      monitorMetric.setMetricTime(metricDatetime);
      update += monitorMetricDao.saveOrUpdateMonitorMetric(monitorMetric);

      long indexFilesFree = MonitorSystemHelper
          .fetchFilesystemFreeSpace(HotPropertiesHelper.getProperty("filesys.mount.root.index"));
      alarmMonitoring(alarmSettings.get(WebappConstants.ALARM_SETTING_SOURCE_TYPE_DISK),
          WebappConstants.ALARM_SETTING_METRIC_INDEX_PARTITION_FREE, "索引分区剩余空间",
          indexFilesFree / BLOCK_SIZE / BLOCK_SIZE);
    }

    // 元数据分区使用率统计
    String metadataFsUsed = MonitorSystemHelper
        .fetchFilesystemUsagePct(HotPropertiesHelper.getProperty("filesys.mount.root.metadata"));
    if (StringUtils.isNotBlank(metadataFsUsed)) {
      monitorMetric = new MonitorMetricDO();
      monitorMetric.setMetricName(FpcConstants.MONITOR_METRIC_FS_METADATA_USED_PCT);
      monitorMetric.setMetricValue(metadataFsUsed);
      monitorMetric.setMetricTime(metricDatetime);
      update += monitorMetricDao.saveOrUpdateMonitorMetric(monitorMetric);

      long metadataFilesFree = MonitorSystemHelper
          .fetchFilesystemFreeSpace(HotPropertiesHelper.getProperty("filesys.mount.root.metadata"));
      alarmMonitoring(alarmSettings.get(WebappConstants.ALARM_SETTING_SOURCE_TYPE_DISK),
          WebappConstants.ALARM_SETTING_METRIC_METADATA_PARTITION_FREE, "元数据分区剩余空间",
          metadataFilesFree / BLOCK_SIZE / BLOCK_SIZE);
    }

    return update;
  }

  /**
   * @see com.machloop.fpc.manager.system.service.SystemMetricService#statisticCpuMemUsage(
   *                                                                        java.util.Date)
   */
  @Override
  public int statisticCpuMemUsage(Date metricTime) {

    int update = 0;

    // 整理metricDate为60秒的整倍数
    double multipleOfInterval = ((double) metricTime.getTime() / 1000)
        / (double) Constants.ONE_MINUTE_SECONDS;
    long roundValue = Math.round(multipleOfInterval);
    metricTime = new Date(roundValue * Constants.ONE_MINUTE_SECONDS * 1000);

    // 只支持linux系统，其他系统返回
    if (!SystemUtils.IS_OS_LINUX) {
      return update;
    }

    long currentTime = metricTime.getTime() / 1000;

    // CPU采样
    MonitorCpuTimes statCpuTimes = MonitorSystemHelper.fetchCpuTimes();
    long cpuValue = 0;
    if (previousStatCpuTime != null) {
      cpuValue = statCpuTimes.getCpuUsagePctLong(previousStatCpuTime);
    }
    MetricNetworkTraffic cpuRrdElement = new MetricNetworkTraffic(cpuValue, metricTime, 0);
    if (perviousCpuRrdElement == null) {
      perviousCpuRrdElement = rrdService.buildMetricStatElem(FpcConstants.STAT_METRIC_CPU_RRD);
    }
    update += rrdService.produceRrdData(cpuRrdElement, perviousCpuRrdElement, currentTime,
        FpcConstants.STAT_METRIC_CPU_RRD);
    perviousCpuRrdElement = cpuRrdElement;
    previousStatCpuTime = statCpuTimes;

    // 内存采样
    MonitorMemory statMemStat = MonitorSystemHelper.fetchPhysicalMemory();
    MetricNetworkTraffic memoryRrdElement = new MetricNetworkTraffic(
        statMemStat.getMemUsagePctLong(), metricTime, 0);
    if (perviousMemRrdElement == null) {
      perviousMemRrdElement = rrdService.buildMetricStatElem(FpcConstants.STAT_METRIC_MEMORY_RRD);
    }
    update += rrdService.produceRrdData(memoryRrdElement, perviousMemRrdElement, currentTime,
        FpcConstants.STAT_METRIC_MEMORY_RRD);
    perviousMemRrdElement = memoryRrdElement;

    return update;
  }

  /**
   * 告警监控
   * @param alarmSetting
   * @param metric
   * @param metricText
   * @param currentValue
   */
  private void alarmMonitoring(AlarmSettingBO alarmSetting, String metric, String metricText,
      long currentValue) {
    if (alarmSetting == null || StringUtils.equals(Constants.BOOL_NO, alarmSetting.getState())) {
      return;
    }

    FireCriteria alarmConfig = alarmSetting.getAlarmMetricConfigs().get(metric);
    if (alarmConfig == null) {
      return;
    }
    long limit = alarmConfig.getOperand();
    boolean isAlarm = false;
    String operatorText = "";
    switch (alarmConfig.getOperator()) {
      case ">":
        isAlarm = currentValue > limit;
        operatorText = "大于";
        break;
      case ">=":
        isAlarm = currentValue >= limit;
        operatorText = "大于等于";
        break;
      case "=":
        isAlarm = currentValue == limit;
        operatorText = "等于";
        break;
      case "<":
        isAlarm = currentValue < limit;
        operatorText = "小于";
        break;
      case "<=":
        isAlarm = currentValue <= limit;
        operatorText = "小于等于";
        break;
      default:
        return;
    }

    if (isAlarm) {
      Date lastAlarmTime = lastAlarmMap.get(alarmConfig.getMetric());
      if (lastAlarmTime == null
          || (lastAlarmTime.getTime() + alarmSetting.getRefireSeconds() * 1000) < System
              .currentTimeMillis()) {
        String unit = StringUtils.equals(WebappConstants.ALARM_SETTING_OPERAND_UNIT_PERCENT,
            alarmConfig.getOperandUnit()) ? "%" : alarmConfig.getOperandUnit();
        StringBuilder logContent = new StringBuilder();
        logContent.append("当前").append(metricText).append(operatorText).append("阈值，");
        logContent.append("当前值为：").append(currentValue).append(unit).append("，");
        logContent.append("阈值为：").append(alarmConfig.getOperand()).append(unit);

        AlarmHelper.alarm(alarmSetting.getLevel(), AlarmHelper.CATEGORY_SERVER_RESOURCE,
            alarmConfig.getMetric(), logContent.toString());

        lastAlarmMap.put(alarmConfig.getMetric(), DateUtils.now());
      }
    }
  }

}
