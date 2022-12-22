package com.machloop.fpc.cms.center.central.service.impl;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
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
import com.machloop.alpha.common.helper.GracefulShutdownHelper;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.metric.system.data.MonitorCpuTimes;
import com.machloop.alpha.common.metric.system.data.MonitorMemory;
import com.machloop.alpha.common.metric.system.helper.MonitorSystemHelper;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.webapp.WebappConstants;
import com.machloop.alpha.webapp.base.AlarmHelper;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.alpha.webapp.system.bo.AlarmSettingBO;
import com.machloop.alpha.webapp.system.bo.AlarmSettingBO.FireCriteria;
import com.machloop.alpha.webapp.system.service.AlarmSettingService;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.broker.dao.FpcNetworkDao;
import com.machloop.fpc.cms.center.central.bo.CentralSystemBO;
import com.machloop.fpc.cms.center.central.bo.FpcBO;
import com.machloop.fpc.cms.center.central.bo.FpcStorageSpaceUsage;
import com.machloop.fpc.cms.center.central.dao.CentralSystemDao;
import com.machloop.fpc.cms.center.central.dao.FpcSystemDao;
import com.machloop.fpc.cms.center.central.data.CentralSystemDO;
import com.machloop.fpc.cms.center.central.service.CentralSystemService;
import com.machloop.fpc.cms.center.central.service.FpcService;
import com.machloop.fpc.cms.center.sensor.dao.SensorLogicalSubnetDao;
import com.machloop.fpc.cms.center.sensor.dao.SensorNetworkDao;
import com.machloop.fpc.cms.center.sensor.dao.SensorNetworkGroupDao;
import com.machloop.fpc.cms.center.system.service.LicenseService;
import com.machloop.fpc.cms.common.FpcCmsConstants;

@Service
public class CentralSystemServiceImpl implements CentralSystemService {

  private static final Logger LOGGER = LoggerFactory.getLogger(CentralSystemServiceImpl.class);

  public static final int BLOCK_SIZE = 1000;

  private final Map<String,
      Date> lastAlarmMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  @Autowired
  private CentralSystemDao centralSystemDao;

  @Autowired
  private FpcSystemDao fpcSystemDao;

  @Autowired
  private AlarmSettingService alarmSettingService;

  @Autowired
  private GlobalSettingService globalSettingService;

  @Autowired
  private FpcService fpcService;

  @Autowired
  private FpcNetworkDao fpcNetworkDao;

  @Autowired
  private SensorNetworkDao sensorNetworkDao;

  @Autowired
  private SensorNetworkGroupDao networkGroupDao;

  @Autowired
  private SensorLogicalSubnetDao logicalSubnetDao;

  @Autowired
  private LicenseService licenseService;

  private MonitorCpuTimes previousCpuTime;

  /**
   * @see com.machloop.fpc.cms.center.central.service.CentralSystemService#queryCentralSystems(java.lang.String, java.lang.String, int, java.util.Date, java.util.Date)
   */
  @Override
  public List<CentralSystemBO> queryCentralSystems(String deviceType, String monitoredSerialNumber,
      int interval, Date startTime, Date endTime) {
    List<CentralSystemDO> centralSystemDOList = centralSystemDao.queryCentralSystems(deviceType,
        monitoredSerialNumber, interval, startTime, endTime);

    List<CentralSystemBO> centralSystemBOList = Lists
        .newArrayListWithExpectedSize(centralSystemDOList.size());
    for (CentralSystemDO centralSystemDO : centralSystemDOList) {
      CentralSystemBO centralSystemBO = new CentralSystemBO();
      BeanUtils.copyProperties(centralSystemDO, centralSystemBO);
      centralSystemBO.setMetricTime(DateUtils.toStringISO8601(centralSystemDO.getMetricTime()));
      centralSystemBOList.add(centralSystemBO);
    }

    return centralSystemBOList;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.CentralSystemService#queryCentralSystem(java.lang.String, java.lang.String)
   */
  @Override
  public CentralSystemBO queryCentralSystem(String deviceType, String monitoredSerialNumber) {
    CentralSystemDO centralSystemDO = centralSystemDao.queryCentralSystem(deviceType,
        monitoredSerialNumber);

    CentralSystemBO centralSystemBO = new CentralSystemBO();
    BeanUtils.copyProperties(centralSystemDO, centralSystemBO);
    centralSystemBO.setMetricTime(DateUtils.toStringISO8601(centralSystemDO.getMetricTime()));

    if (StringUtils.equals(deviceType, FpcCmsConstants.DEVICE_TYPE_TFA)) {
      CentralSystemDO fpcSystemDO = fpcSystemDao.queryLatestFpcSystemMetrics(monitoredSerialNumber);
      centralSystemBO.setMemoryMetric(fpcSystemDO.getMemoryMetric());
      centralSystemBO.setCpuMetric(fpcSystemDO.getCpuMetric());
      centralSystemBO.setSystemFsMetric(fpcSystemDO.getSystemFsMetric());
      centralSystemBO.setIndexFsMetric(fpcSystemDO.getIndexFsMetric());
      centralSystemBO.setMetadataFsMetric(fpcSystemDO.getMetadataFsMetric());
      centralSystemBO.setMetadataHotFsMetric(fpcSystemDO.getMetadataHotFsMetric());
      centralSystemBO.setPacketFsMetric(fpcSystemDO.getPacketFsMetric());
    }

    return centralSystemBO;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.CentralSystemService#queryStorageSpaceUsagesByRanking(int)
   */
  @Override
  public Map<String, Object> queryStorageSpaceUsagesByRanking(int number) {
    Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    List<FpcStorageSpaceUsage> list = centralSystemDao.queryStorageSpaceUsagesByRanking();

    // 获取设备信息集合
    List<String> monitoredSerialNumbers = Lists.newArrayListWithExpectedSize(list.size());
    for (FpcStorageSpaceUsage spaceUsage : list) {
      monitoredSerialNumbers.add(spaceUsage.getDeviceSerialNumber());
    }
    List<FpcBO> fpcList = fpcService.queryFpcBySerialNumbers(monitoredSerialNumbers, true);
    Map<String, String> fpcMap = Maps.newHashMapWithExpectedSize(fpcList.size());
    for (FpcBO fpcBO : fpcList) {
      fpcMap.put(fpcBO.getSerialNumber(), fpcBO.getName());
    }

    // 计算总容量和使用量
    long dataFsTotalByte = 0;
    long dataFsUsedByte = 0;
    Iterator<FpcStorageSpaceUsage> iterator = list.iterator();
    while (iterator.hasNext()) {
      FpcStorageSpaceUsage entity = iterator.next();
      String fpcName = fpcMap.get(entity.getDeviceSerialNumber());

      if (StringUtils.isBlank(fpcName)) {
        iterator.remove();
        continue;
      }
      entity.setDeviceName(fpcName);
      dataFsTotalByte += entity.getFsDataTotalByte();
      dataFsUsedByte += dataFsTotalByte * (entity.getFsDataUsedPct() / 100.0);
    }
    resultMap.put("dataFsTotalByte", dataFsTotalByte);
    resultMap.put("dataFsUsedByte", dataFsUsedByte);

    // 返回前number个元素
    if (list.size() <= number) {
      resultMap.put("ranking", list);
    } else {
      resultMap.put("ranking", list.subList(0, number));
    }

    return resultMap;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.CentralSystemService#queryFpcMonitorMetric(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Map<String, Object> queryMaxDataOldestTime(String networkId, String networkGroupId) {

    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    CentralSystemDO centralSystemDO = new CentralSystemDO();
    List<String> networkIdList = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);

    List<String> existSensorNetworkIdList = sensorNetworkDao.querySensorNetworks().stream()
        .map(e -> e.getNetworkInSensorId()).collect(Collectors.toList());

    if (StringUtils.isNotBlank(networkId)) {
      if (existSensorNetworkIdList.contains(networkId)) {
        networkIdList = Lists.newArrayList(networkId);
      } else {
        networkIdList = CsvUtils.convertCSVToList(
            logicalSubnetDao.querySensorLogicalSubnet(networkId).getNetworkInSensorIds());
      }
      result.put("networkId", networkId);
    }
    if (StringUtils.isNotBlank(networkGroupId)) {
      networkIdList = CsvUtils.convertCSVToList(
          networkGroupDao.querySensorNetworkGroup(networkGroupId).getNetworkInSensorIds());
      result.put("networkGroupId", networkGroupId);
    }

    if (CollectionUtils.isEmpty(networkIdList)) {
      return Maps.newHashMapWithExpectedSize(0);
    }
    List<String> fpcSerialNumbers = fpcNetworkDao.queryFpcNetworkByFpcNetworkIds(networkIdList)
        .stream().map(e -> e.getFpcSerialNumber()).collect(Collectors.toList());
    centralSystemDO = centralSystemDao.queryMaxDataOldestTime(FpcCmsConstants.DEVICE_TYPE_TFA,
        fpcSerialNumbers);
    result.put("metricTime", centralSystemDO.getMetricTime());
    result.put("fpcSerialNumber", centralSystemDO.getMonitoredSerialNumber());
    result.put("dataOldestTime", centralSystemDO.getDataOldestTime());

    return result;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.CentralSystemService#statisticCentralSystem(java.util.Date)
   */
  @Override
  public int statisticCentralSystem(Date metricTime) {
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

    // 查询告警配置
    Map<String, AlarmSettingBO> alarmSettings = alarmSettingService.queryAlarmSettingsByCache();
    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(JsonHelper.serialize(alarmSettings));
    }

    CentralSystemDO centralSystemDO = new CentralSystemDO();
    centralSystemDO.setDeviceType(FpcCmsConstants.DEVICE_TYPE_CMS);
    centralSystemDO.setMonitoredSerialNumber(licenseService.queryDeviceSerialNumber());
    centralSystemDO.setMetricTime(metricTime);

    // CPU采样
    MonitorCpuTimes currentCpuState = MonitorSystemHelper.fetchCpuTimes();
    if (previousCpuTime != null) {
      centralSystemDO.setCpuMetric((int) currentCpuState.getCpuUsagePctLong(previousCpuTime));

      alarmMonitoring(alarmSettings.get(WebappConstants.ALARM_SETTING_SOURCE_TYPE_CPU),
          WebappConstants.ALARM_SETTING_METRIC_CPU_USAGE, "CPU使用率", centralSystemDO.getCpuMetric());
    }
    previousCpuTime = currentCpuState;

    // 内存采样
    MonitorMemory memStat = MonitorSystemHelper.fetchPhysicalMemory();
    centralSystemDO.setMemoryMetric((int) memStat.getMemUsagePctLong());

    alarmMonitoring(alarmSettings.get(WebappConstants.ALARM_SETTING_SOURCE_TYPE_MEMORY),
        WebappConstants.ALARM_SETTING_METRIC_MEMORY_USAGE, "内存使用率",
        centralSystemDO.getMemoryMetric());

    // 系统使用分区统计
    long systemFilesysUsed = MonitorSystemHelper
        .fetchFilesystemUsagePctLong(HotPropertiesHelper.getProperty("filesys.mount.root.system"));
    centralSystemDO.setSystemFsMetric((int) systemFilesysUsed);

    long systemFilesFree = MonitorSystemHelper
        .fetchFilesystemFreeSpace(HotPropertiesHelper.getProperty("filesys.mount.root.system"));
    alarmMonitoring(alarmSettings.get(WebappConstants.ALARM_SETTING_SOURCE_TYPE_DISK),
        WebappConstants.ALARM_SETTING_METRIC_SYSTEM_PARTITION_FREE, "系统分区剩余空间",
        systemFilesFree / BLOCK_SIZE / BLOCK_SIZE);

    // 更新实时表
    centralSystemDao.saveOrUpdateCentralSystem(centralSystemDO);

    // 插入历史表
    centralSystemDO = centralSystemDao.saveCentralSystem(centralSystemDO);

    return StringUtils.isBlank(centralSystemDO.getId()) ? update : ++update;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.CentralSystemService#collectCentralSystem(java.lang.String, java.util.List)
   */
  @Override
  public void collectCentralSystem(String deviceType, List<CentralSystemDO> centralSystems) {
    // 更新实时表
    centralSystems.sort(new Comparator<CentralSystemDO>() {

      @Override
      public int compare(CentralSystemDO o1, CentralSystemDO o2) {
        return o2.getMetricTime().compareTo(o1.getMetricTime());
      }
    });
    centralSystemDao.saveOrUpdateCentralSystem(centralSystems.get(0));

    // 插入历史表
    if (!StringUtils.equals(deviceType, FpcCmsConstants.DEVICE_TYPE_TFA)) {
      centralSystemDao.saveCentralSystems(centralSystems);
    }
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.CentralSystemService#rollupCentralSystem()
   */
  @Override
  public void rollupCentralSystem() {
    long intervalSecond = Constants.FIVE_MINUTE_SECONDS;
    String latestRollupSetting = CenterConstants.GLOBAL_SETTING_SYSTEM_ROLLUP_LATEST_5MIN;

    LocalDateTime currentIntervalDateTime = computeCurrentIntervalDateTime(LocalDateTime.now(),
        intervalSecond);
    Date thisTime = Date.from(currentIntervalDateTime.atZone(ZoneId.systemDefault()).toInstant());

    String latestTimeStr = globalSettingService.getValue(latestRollupSetting);
    if (StringUtils.isNotBlank(latestTimeStr) && thisTime.getTime()
        - DateUtils.parseISO8601Date(latestTimeStr).getTime() >= intervalSecond * 1000) {
      Date latestTime = DateUtils.parseISO8601Date(latestTimeStr);
      while (!GracefulShutdownHelper.isShutdownNow() && latestTime.before(thisTime)) {

        Date startTime = new Date(latestTime.getTime());
        Date endTime = new Date(startTime.getTime() + intervalSecond * 1000);

        // 汇总所有数据
        centralSystemDao.rollupCentralSystem(startTime, endTime, "", "");

        // 记录统计时间
        globalSettingService.setValue(latestRollupSetting, DateUtils.toStringISO8601(endTime));
        latestTime = endTime;
      }
    } else if (StringUtils.isBlank(latestTimeStr)) {

      // 第一次执行，只统计当前开始时间
      Date startTime = Date.from(currentIntervalDateTime.minusSeconds(intervalSecond)
          .atZone(ZoneId.systemDefault()).toInstant());

      // 汇总所有数据
      centralSystemDao.rollupCentralSystem(startTime, thisTime, "", "");

      // 记录统计时间
      globalSettingService.setValue(latestRollupSetting, DateUtils.toStringISO8601(thisTime));
    }
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.CentralSystemService#clearHisCentralSystem(java.util.Date, int)
   */
  @Override
  public int clearHisCentralSystem(Date clearTime, int interval) {
    return centralSystemDao.deleteCentralSystem(clearTime, interval);
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.CentralSystemService#deleteCentralSystem(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteCentralSystem(String deviceType, String monitoredSerialNumber) {
    return centralSystemDao.deleteCentralSystem(deviceType, monitoredSerialNumber);
  }

  /**
   * @param localDateTime
   * @param intervalSecond
   * @return
   */
  private LocalDateTime computeCurrentIntervalDateTime(LocalDateTime localDateTime,
      long intervalSecond) {
    // 根据间隔时间
    LocalTime localTime = LocalTime.ofSecondOfDay(
        localDateTime.toLocalTime().toSecondOfDay() / intervalSecond * intervalSecond);
    return LocalDateTime.of(localDateTime.toLocalDate(), localTime);
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
        logContent.append("当前").append(metricText).append(operatorText).append("阈值,");
        logContent.append("当前值为：").append(currentValue).append(unit).append(",");
        logContent.append("阈值为：").append(alarmConfig.getOperand()).append(unit);

        AlarmHelper.alarm(alarmSetting.getLevel(), AlarmHelper.CATEGORY_SERVER_RESOURCE,
            alarmConfig.getMetric(), logContent.toString());

        lastAlarmMap.put(alarmConfig.getMetric(), DateUtils.now());
      }
    }
  }

}
