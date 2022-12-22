package com.machloop.fpc.cms.center.central.service.impl;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.helper.GracefulShutdownHelper;
import com.machloop.alpha.common.metric.system.data.MonitorNetwork;
import com.machloop.alpha.common.metric.system.helper.MonitorNetifHelper;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.central.bo.CentralNetifBO;
import com.machloop.fpc.cms.center.central.bo.CentralNetifUsage;
import com.machloop.fpc.cms.center.central.bo.FpcBO;
import com.machloop.fpc.cms.center.central.dao.CentralNetifDao;
import com.machloop.fpc.cms.center.central.data.CentralNetifDO;
import com.machloop.fpc.cms.center.central.service.CentralNetifService;
import com.machloop.fpc.cms.center.central.service.FpcService;
import com.machloop.fpc.cms.center.system.service.LicenseService;
import com.machloop.fpc.cms.common.FpcCmsConstants;

@Service
public class CentralNetifServiceImpl implements CentralNetifService {

  private static final String DEFAULT_SYSTEM_MGMT_NETIF_ID = "1";

  @Autowired
  private CentralNetifDao centralNetifDao;

  @Autowired
  private GlobalSettingService globalSettingService;

  @Autowired
  private FpcService fpcService;

  @Autowired
  private LicenseService licenseService;

  private MonitorNetwork previousNetStat;

  /**
   * @see com.machloop.fpc.cms.center.central.service.CentralNetifService#queryCentralNetifProfiles(java.lang.String, java.lang.String, java.lang.String[])
   */
  @Override
  public List<CentralNetifBO> queryCentralNetifProfiles(String deviceType,
      String monitoredSerialNumber, String... categorys) {
    List<CentralNetifDO> list = centralNetifDao.queryCentralNetifProfiles(deviceType,
        monitoredSerialNumber, Lists.newArrayList(categorys));

    List<CentralNetifBO> netifMetricBOList = Lists.newArrayListWithCapacity(list.size());
    for (CentralNetifDO centralNetifDO : list) {
      CentralNetifBO centralNetifBO = new CentralNetifBO();
      BeanUtils.copyProperties(centralNetifDO, centralNetifBO);
      netifMetricBOList.add(centralNetifBO);
    }

    return netifMetricBOList;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.CentralNetifService#queryCentralNetifs(java.lang.String, java.lang.String, java.lang.String, java.util.List, int, java.util.Date, java.util.Date)
   */
  @Override
  public List<CentralNetifBO> queryCentralNetifs(String deviceType, String monitoredSerialNumber,
      String netifName, List<String> categoryList, int interval, Date startTime, Date endTime) {
    List<CentralNetifDO> list = centralNetifDao.queryCentralNetifs(deviceType,
        monitoredSerialNumber, netifName, categoryList, interval, startTime, endTime);

    List<CentralNetifBO> result = Lists.newArrayListWithCapacity(list.size());
    for (CentralNetifDO centralNetifDO : list) {
      CentralNetifBO centralNetifBO = new CentralNetifBO();
      BeanUtils.copyProperties(centralNetifDO, centralNetifBO);
      centralNetifBO.setMetricTime(DateUtils.toStringISO8601(centralNetifDO.getMetricTime()));

      result.add(centralNetifBO);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.CentralNetifService#queryTotalReceivingNetifs(int, java.util.Date, java.util.Date)
   */
  @Override
  public List<CentralNetifBO> queryTotalReceivingNetifs(int interval, Date startTime,
      Date endTime) {
    List<CentralNetifBO> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    List<String> fpcSerialNumbers = fpcService.queryAllFpc().stream().map(FpcBO::getSerialNumber)
        .collect(Collectors.toList());
    if (CollectionUtils.isEmpty(fpcSerialNumbers)) {
      return result;
    }

    List<CentralNetifDO> list = centralNetifDao.queryTotalReceivingNetifs(
        FpcCmsConstants.DEVICE_TYPE_TFA, fpcSerialNumbers, interval, startTime, endTime);
    for (CentralNetifDO centralNetifDO : list) {
      CentralNetifBO centralNetifBO = new CentralNetifBO();
      BeanUtils.copyProperties(centralNetifDO, centralNetifBO);
      centralNetifBO.setMetricTime(DateUtils.toStringISO8601(centralNetifDO.getMetricTime()));

      result.add(centralNetifBO);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.CentralNetifService#queryNetifUsagesByRanking(int)
   */
  @Override
  public List<CentralNetifUsage> queryNetifUsagesByRanking(int number) {

    List<CentralNetifUsage> list = centralNetifDao.queryNetifUsagesByRanking();

    // 获取设备信息集合
    List<String> serialNumbers = Lists.newArrayListWithExpectedSize(list.size());
    for (CentralNetifUsage centralNetifUsage : list) {
      serialNumbers.add(centralNetifUsage.getDeviceSerialNumber());
    }
    List<FpcBO> fpcBOList = fpcService.queryFpcBySerialNumbers(serialNumbers, true);
    Map<String, String> deviceMap = Maps.newHashMapWithExpectedSize(fpcBOList.size());
    for (FpcBO fpcBO : fpcBOList) {
      deviceMap.put(fpcBO.getSerialNumber(), fpcBO.getName());
    }

    // 计算使用率
    Iterator<CentralNetifUsage> iterator = list.iterator();
    while (iterator.hasNext()) {
      CentralNetifUsage centralNetifUsage = iterator.next();

      // 如果未查到设备信息则将该条数据从列表移出
      String deviceName = deviceMap.get(centralNetifUsage.getDeviceSerialNumber());
      if (StringUtils.isBlank(deviceName)) {
        iterator.remove();
        continue;
      }
      centralNetifUsage.setDeviceName(deviceName);
      centralNetifUsage.setTotalBandwidth(centralNetifUsage.getTotalBandwidth() * 1000 * 1000);
      // 计算使用率
      long usage = Math.round(
          ((double) centralNetifUsage.getUsagedBandwidth() / centralNetifUsage.getTotalBandwidth())
              * 100);
      centralNetifUsage.setUsage(usage);
    }

    // 排序
    list.sort(new Comparator<CentralNetifUsage>() {

      @Override
      public int compare(CentralNetifUsage o1, CentralNetifUsage o2) {
        double o1UsageRate = (double) o1.getUsagedBandwidth() / o1.getTotalBandwidth();
        double o2UsageRate = (double) o2.getUsagedBandwidth() / o2.getTotalBandwidth();

        return -Double.compare(o1UsageRate, o2UsageRate);
      }

    });

    // 返回前number个元素
    if (list.size() <= number) {
      return list;
    } else {
      return list.subList(0, number);
    }
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.CentralNetifService#statisticCentralNetifs(java.util.Date)
   */
  @Transactional
  @Override
  public int statisticCentralNetifs(Date metricTime) {
    int update = 0;

    // 整理metricDate为60秒的整倍数
    double multipleOfInterval = ((double) metricTime.getTime() / 1000)
        / (double) Constants.ONE_MINUTE_SECONDS;
    long roundValue = Math.round(multipleOfInterval);
    metricTime = new Date(roundValue * Constants.ONE_MINUTE_SECONDS * 1000);

    // 获取本地设备序列号
    String deviceSerialNumber = licenseService.queryDeviceSerialNumber();

    // 查询管理接口名称
    List<CentralNetifDO> netifDOList = centralNetifDao.queryCentralNetifProfiles(
        FpcCmsConstants.DEVICE_TYPE_CMS, deviceSerialNumber,
        Lists.newArrayList(FpcCmsConstants.DEVICE_NETIF_CATEGORY_MGMT));
    if (CollectionUtils.isEmpty(netifDOList)) {
      // 填充管理口的监控设备序列号
      centralNetifDao.updateNetifBelongDeviceSerialNumber(DEFAULT_SYSTEM_MGMT_NETIF_ID,
          deviceSerialNumber);
      netifDOList = centralNetifDao.queryCentralNetifProfiles(FpcCmsConstants.DEVICE_TYPE_CMS,
          deviceSerialNumber, Lists.newArrayList(FpcCmsConstants.DEVICE_NETIF_CATEGORY_MGMT));
    }

    String mgtIfname = "";
    if (CollectionUtils.isNotEmpty(netifDOList)) {
      mgtIfname = netifDOList.get(0).getNetifName();
    }

    if (StringUtils.isBlank(mgtIfname)) {
      return update;
    }

    // 采集网卡数据
    Map<String, MonitorNetwork> trafficMap = MonitorNetifHelper
        .monitorNetifTraffic(Lists.newArrayList(mgtIfname));

    MonitorNetwork monitorNetwork = trafficMap.get(mgtIfname);

    if (previousNetStat != null) {
      CentralNetifDO centralNetifDO = new CentralNetifDO();

      centralNetifDO.setDeviceType(FpcCmsConstants.DEVICE_TYPE_CMS);
      centralNetifDO.setMonitoredSerialNumber(deviceSerialNumber);
      centralNetifDO.setNetifName(mgtIfname);
      centralNetifDO.setState(Constants.STATE_UP);
      centralNetifDO.setCategory(FpcCmsConstants.DEVICE_NETIF_CATEGORY_MGMT);
      centralNetifDO.setSpecification(0);
      centralNetifDO.setMetricTime(metricTime);
      centralNetifDO.compute(previousNetStat, monitorNetwork, Constants.ONE_MINUTE_SECONDS);
      // 更新实时表
      centralNetifDao.saveOrUpdateCentralNetifs(Lists.newArrayList(centralNetifDO));

      // 插入历史表
      centralNetifDao.saveCentralNetifs(Lists.newArrayList(centralNetifDO));
      update++;
    }
    // 将当前接口流量记录到内存中
    previousNetStat = monitorNetwork;

    return update;
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.CentralNetifService#collectCentralNetifs(java.util.List)
   */
  @Override
  public void collectCentralNetifs(List<CentralNetifDO> centralNetifList) {

    // 更新实时表
    centralNetifDao.saveOrUpdateCentralNetifs(centralNetifList);

    // 保存设备接口流量数据
    centralNetifDao.saveCentralNetifs(centralNetifList);
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.CentralNetifService#rollupCentralNetifs()
   */
  @Override
  public void rollupCentralNetifs() {
    long intervalSecond = Constants.FIVE_MINUTE_SECONDS;
    String latestRollupSetting = CenterConstants.GLOBAL_SETTING_NETIF_ROLLUP_LATEST_5MIN;

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
        centralNetifDao.rollupCentralNetifs(startTime, endTime, "", "");

        // 记录统计时间
        globalSettingService.setValue(latestRollupSetting, DateUtils.toStringISO8601(endTime));
        latestTime = endTime;
      }
    } else if (StringUtils.isBlank(latestTimeStr)) {

      // 第一次执行，只统计当前开始时间
      Date startTime = Date.from(currentIntervalDateTime.minusSeconds(intervalSecond)
          .atZone(ZoneId.systemDefault()).toInstant());

      // 汇总所有数据
      centralNetifDao.rollupCentralNetifs(startTime, thisTime, "", "");

      // 记录统计时间
      globalSettingService.setValue(latestRollupSetting, DateUtils.toStringISO8601(thisTime));
    }
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.CentralNetifService#clearHisCentralNetifs(java.util.Date, int)
   */
  @Override
  public int clearHisCentralNetifs(Date clearTime, int interval) {
    return centralNetifDao.deleteCentralNetifs(clearTime, interval);
  }

  /**
   * @see com.machloop.fpc.cms.center.central.service.CentralNetifService#deleteCentralNetifs(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteCentralNetifs(String deviceType, String monitoredSerialNumber) {
    return centralNetifDao.deleteCentralNetifs(deviceType, monitoredSerialNumber);
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

}
