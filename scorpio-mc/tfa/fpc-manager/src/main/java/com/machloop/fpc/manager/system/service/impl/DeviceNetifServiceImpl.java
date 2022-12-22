package com.machloop.fpc.manager.system.service.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.metric.system.data.MonitorNetwork;
import com.machloop.alpha.common.metric.system.helper.MonitorNetifHelper;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.appliance.dao.ForwardPolicyDao;
import com.machloop.fpc.manager.appliance.dao.TransmitTaskDao;
import com.machloop.fpc.manager.appliance.data.ForwardPolicyDO;
import com.machloop.fpc.manager.appliance.data.TransmitTaskDO;
import com.machloop.fpc.manager.metric.dao.MetricNetifDataRecordDao;
import com.machloop.fpc.manager.statistics.bo.TimeseriesBO;
import com.machloop.fpc.manager.statistics.service.RrdService;
import com.machloop.fpc.manager.system.bo.DeviceNetifBO;
import com.machloop.fpc.manager.system.dao.DeviceNetifDao;
import com.machloop.fpc.manager.system.data.DeviceNetifDO;
import com.machloop.fpc.manager.system.data.MetricNetworkRrd;
import com.machloop.fpc.manager.system.data.MetricNetworkTraffic;
import com.machloop.fpc.manager.system.service.DeviceNetifService;
import com.machloop.fpc.npm.appliance.dao.NetworkNetifDao;
import com.machloop.fpc.npm.appliance.data.NetworkNetifDO;
import com.machloop.fpc.npm.appliance.service.NetworkService;

/**
 * @author liumeng
 *
 * create at 2018年12月13日, fpc-manager
 */
@Service
public class DeviceNetifServiceImpl implements DeviceNetifService {

  private static final Logger LOGGER = LoggerFactory.getLogger(DeviceNetifServiceImpl.class);

  @Autowired
  private DeviceNetifDao deviceNetifDao;

  @Autowired
  private DictManager dictManager;

  @Autowired
  private RrdService rrdService;

  @Autowired
  private NetworkService networkService;

  @Autowired
  private MetricNetifDataRecordDao metricNetifDataRecordDao;

  @Autowired
  private ForwardPolicyDao forwardPolicyDao;

  @Autowired
  private TransmitTaskDao transmitTaskDao;

  @Autowired
  private NetworkNetifDao networkNetifDao;

  private MetricNetworkRrd previousNetStat;

  /**
   * @see com.machloop.fpc.manager.system.service.DeviceNetifService#queryDeviceNetifs()
   */
  @Override
  public List<DeviceNetifBO> queryDeviceNetifs() {
    return queryDeviceNetifsByCategories(FpcConstants.DEVICE_NETIF_CATEGORY_MGMT,
        FpcConstants.DEVICE_NETIF_CATEGORY_INGEST, FpcConstants.DEVICE_NETIF_CATEGORY_TRANSMIT,
        FpcConstants.DEVICE_NETIF_CATEGORY_NETFLOW, FpcConstants.DEVICE_NETIF_CATEGORY_DEFAULT);
  }

  /**
   * @see com.machloop.fpc.manager.system.service.DeviceNetifService#queryDeviceNetifsWithBandwidth()
   */
  @Override
  public List<DeviceNetifBO> queryDeviceNetifsWithBandwidth() {
    List<DeviceNetifBO> deviceNetifs = queryDeviceNetifsByCategories(
        FpcConstants.DEVICE_NETIF_CATEGORY_MGMT, FpcConstants.DEVICE_NETIF_CATEGORY_INGEST,
        FpcConstants.DEVICE_NETIF_CATEGORY_TRANSMIT, FpcConstants.DEVICE_NETIF_CATEGORY_NETFLOW,
        FpcConstants.DEVICE_NETIF_CATEGORY_DEFAULT);

    List<String> networkNetifNameList = networkNetifDao.queryAllNetworkNetifs().stream()
        .map(NetworkNetifDO::getNetifName).collect(Collectors.toList());
    List<ForwardPolicyDO> forwardPolicyDOList = forwardPolicyDao.queryForwardPolicys();
    Set<String> forwardNetIfNameList = Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    forwardPolicyDOList.forEach(forwardPolicyDO -> {
      List<String> netIfNames = JsonHelper.deserialize(forwardPolicyDO.getNetifName(),
          new TypeReference<List<String>>() {
          }, false);
      forwardNetIfNameList.addAll(netIfNames);
    });
    Set<String> transmitTaskNetIfNameList = transmitTaskDao
        .queryTransmitTasksByMode(FpcConstants.TRANSMIT_TASK_MODE_REPLAY).stream()
        .map(TransmitTaskDO::getReplayNetif).collect(Collectors.toSet());

    deviceNetifs.forEach(netif -> {
      if (!StringUtils.equals(netif.getCategory(), FpcConstants.DEVICE_NETIF_CATEGORY_DEFAULT)) {
        Map<String, Object> netifLatestState = metricNetifDataRecordDao
            .queryNetifLatestState(netif.getName());
        if (MapUtils.isNotEmpty(netifLatestState)) {
          netif.setMetricTime(MapUtils.getString(netifLatestState, "timestamp"));

          Long currentBytes = 0L;
          if (StringUtils.equals(netif.getCategory(),
              FpcConstants.DEVICE_NETIF_CATEGORY_TRANSMIT)) {
            currentBytes = MapUtils.getLongValue(netifLatestState, "transmit_bytes", 0L);
          } else {
            currentBytes = MapUtils.getLongValue(netifLatestState, "total_bytes", 0L);
          }

          // 求带宽
          BigDecimal bg = new BigDecimal(
              currentBytes * Constants.BYTE_BITS / (double) Constants.ONE_MINUTE_SECONDS);
          netif.setBandwidth(bg.setScale(2, BigDecimal.ROUND_HALF_UP).doubleValue());
        }
        StringBuilder useMessage = new StringBuilder();
        String netifName = netif.getName();
        if (networkNetifNameList.contains(netifName)) {
          useMessage.append("网络/");
        }
        if (forwardNetIfNameList.contains(netifName)) {
          useMessage.append("实时转发策略/");
        }
        if (transmitTaskNetIfNameList.contains(netifName)) {
          useMessage.append("全流量查询任务/");
        }
        netif.setUseMessage(StringUtils.isBlank(useMessage.toString()) ? ""
            : useMessage.substring(0, useMessage.indexOf("/")));
      }
    });

    return deviceNetifs;
  }

  /**
   * @see com.machloop.fpc.manager.system.service.DeviceNetifService#queryDeviceNetifsByCategories(
   *java.lang.String[])
   */
  @Override
  public List<DeviceNetifBO> queryDeviceNetifsByCategories(String... category) {
    Map<String,
        String> categoryDict = dictManager.getBaseDict().getItemMap("device_netif_category");
    Map<String, String> typeDict = dictManager.getBaseDict().getItemMap("device_netif_type");

    List<String> categoryList = Lists.newArrayList(category);
    List<DeviceNetifDO> netifDOList = deviceNetifDao.queryDeviceNetifs(categoryList);

    List<DeviceNetifBO> netifBOList = Lists.newArrayListWithCapacity(netifDOList.size());
    for (DeviceNetifDO netifDO : netifDOList) {

      DeviceNetifBO netifBO = new DeviceNetifBO();
      BeanUtils.copyProperties(netifDO, netifBO);

      netifBO.setCategoryText(MapUtils.getString(categoryDict, netifBO.getCategory(), ""));
      netifBO.setTypeText(MapUtils.getString(typeDict, netifBO.getType(), ""));
      netifBO.setUpdateTime(DateUtils.toStringISO8601(netifDO.getUpdateTime()));
      netifBOList.add(netifBO);
    }
    return netifBOList;
  }

  /**
   * @see com.machloop.fpc.manager.system.service.DeviceNetifService#queryNetifUsage(
   *java.lang.String, java.util.Date, java.util.Date)
   */
  @Override
  public TimeseriesBO queryNetifUsage(String rrdName, String interval, Date startTime,
      Date endTime) {
    TimeseriesBO timeseries = rrdService.queryRrdHistogram(rrdName, interval, startTime, endTime);

    // byte转换成bit
    if (StringUtils.contains(rrdName, FpcConstants.STAT_NETIF_RRD_RX_BYTEPS)
        || StringUtils.contains(rrdName, FpcConstants.STAT_NETIF_RRD_TX_BYTEPS)) {
      double[] dataPoints = timeseries.getDataPoint();
      if (dataPoints != null) {
        for (int i = 0; i < dataPoints.length; i++) {
          dataPoints[i] = dataPoints[i] * Constants.BYTE_BITS;
        }
      }
    }

    return timeseries;
  }

  /**
   * @see com.machloop.fpc.manager.system.service.DeviceNetifService#queryNetifTotalUsage(java.lang.String, java.util.Date, java.util.Date, java.util.List, java.lang.String)
   */
  @Override
  public TimeseriesBO queryNetifTotalUsage(String interval, Date startTime, Date endTime,
      List<DeviceNetifBO> deviceNetifBOList, String type) {
    TimeseriesBO totalTimeseries = new TimeseriesBO();
    double[] totalDataPoint = null;

    for (DeviceNetifBO deviceNetifBO : deviceNetifBOList) {
      TimeseriesBO bpsTimeseries = queryNetifUsage(deviceNetifBO.getName() + type, interval,
          startTime, endTime);
      totalTimeseries.setStartTime(bpsTimeseries.getStartTime());
      totalTimeseries.setEndTime(bpsTimeseries.getEndTime());

      if (bpsTimeseries.getDataPoint() != null) {
        if (totalDataPoint == null) {
          totalDataPoint = bpsTimeseries.getDataPoint().clone();
          continue;
        }

        for (int j = 0; j < totalDataPoint.length; j++) {
          if (bpsTimeseries.getDataPoint().length > j) {
            totalDataPoint[j] += bpsTimeseries.getDataPoint()[j];
          }
        }
      }
    }

    totalTimeseries.setDataPoint(totalDataPoint);
    return totalTimeseries;
  }

  /**
   * @see com.machloop.fpc.manager.system.service.DeviceNetifService#queryIngestRxNetifTotalUsage(java.lang.String, java.util.Date, java.util.Date)
   */
  @Override
  public Map<String, TimeseriesBO> queryIngestRxNetifTotalUsage(String interval, Date startTime,
      Date endTime) {
    Map<String, TimeseriesBO> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    List<DeviceNetifBO> deviceNetifBOList = queryDeviceNetifsByCategories(
        FpcConstants.DEVICE_NETIF_CATEGORY_INGEST);
    // 统计总量
    TimeseriesBO bpsTimeseries = queryNetifTotalUsage(interval, startTime, endTime,
        deviceNetifBOList, FpcConstants.STAT_NETIF_RRD_RX_BYTEPS);
    TimeseriesBO ppsTimeseries = queryNetifTotalUsage(interval, startTime, endTime,
        deviceNetifBOList, FpcConstants.STAT_NETIF_RRD_RX_PPS);
    result.put("rxBps", bpsTimeseries);
    result.put("rxPps", ppsTimeseries);
    return result;
  }

  /**
   * @see com.machloop.fpc.manager.system.service.DeviceNetifService#batchUpdateDeviceNetifs(java.util.List, java.lang.String)
   */
  @Override
  public List<DeviceNetifBO> batchUpdateDeviceNetifs(List<DeviceNetifBO> netifBOList,
      String operatorId) {
    // 判断接口是否已在网络中配置，如果已配置需要先在网络中解绑接口才能修改接口类型
    List<Map<String, Object>> configuredNetifs = networkService.queryNetworkNetif();
    List<String> configuredNetifId = configuredNetifs.stream()
        .map(networkNetif -> (String) networkNetif.get("id")).collect(Collectors.toList());
    List<
        String> updateNetifId = netifBOList
            .stream().filter(netif -> StringUtils
                .equals(FpcConstants.DEVICE_NETIF_CATEGORY_TRANSMIT, netif.getCategory()))
            .map(netif -> netif.getId()).collect(Collectors.toList());
    if (CollectionUtils.containsAny(configuredNetifId, updateNetifId)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "已在网络中配置的接口不能修改用途");
    }
    // 判断接口是否被实时转发使用
    List<DeviceNetifDO> deviceNetifDOList = deviceNetifDao
        .queryDeviceNetifs(Lists.newArrayList("2"));
    List<ForwardPolicyDO> forwardPolicyDOList = forwardPolicyDao.queryForwardPolicys();
    Set<String> forwardNetIfNameList = Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    forwardPolicyDOList.forEach(forwardPolicyDO -> {
      List<String> netIfNames = JsonHelper.deserialize(forwardPolicyDO.getNetifName(),
          new TypeReference<List<String>>() {
          }, false);
      forwardNetIfNameList.addAll(netIfNames);
    });
    List<String> forwardNetifIdList = forwardNetIfNameList.stream()
        .map(netIfName -> deviceNetifDOList.stream()
            .filter(deviceNetifDO -> StringUtils.equals(netIfName, deviceNetifDO.getName()))
            .collect(Collectors.toList()).get(0).getId())
        .collect(Collectors.toList());
    List<DeviceNetifBO> forwardUpdateNetif = netifBOList.stream()
        .filter(netifBO -> forwardNetifIdList.contains(netifBO.getId()) && !StringUtils
            .equals(FpcConstants.DEVICE_NETIF_CATEGORY_TRANSMIT, netifBO.getCategory()))
        .collect(Collectors.toList());
    if (CollectionUtils.isNotEmpty(forwardUpdateNetif)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
          "已在实时转发中配置的接口不能修改接口用途");
    }
    // 判断判断接口是否被全流量查询任务使用
    List<TransmitTaskDO> transmitTaskDOList = transmitTaskDao
        .queryTransmitTasksByMode(FpcConstants.TRANSMIT_TASK_MODE_REPLAY);
    Set<String> transmitTaskNetIfNameList = transmitTaskDOList.stream()
        .map(TransmitTaskDO::getReplayNetif).collect(Collectors.toSet());
    List<String> transmitTaskNetIfIdList = transmitTaskNetIfNameList.stream()
        .map(netIfName -> deviceNetifDOList.stream()
            .filter(deviceNetifDO -> StringUtils.equals(netIfName, deviceNetifDO.getName()))
            .collect(Collectors.toList()).get(0).getId())
        .collect(Collectors.toList());
    List<DeviceNetifBO> transmitTaskUpdateNetif = netifBOList.stream()
        .filter(netifBO -> transmitTaskNetIfIdList.contains(netifBO.getId()) && !StringUtils
            .equals(FpcConstants.DEVICE_NETIF_CATEGORY_TRANSMIT, netifBO.getCategory()))
        .collect(Collectors.toList());
    if (CollectionUtils.isNotEmpty(transmitTaskUpdateNetif)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
          "已在全流量查询任务中配置的接口不能修改接口用途");
    }


    for (DeviceNetifBO netif : netifBOList) {
      if (StringUtils.equals(netif.getCategory(), FpcConstants.DEVICE_NETIF_CATEGORY_MGMT)) {
        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "管理口不支持编辑");
      }
    }

    List<DeviceNetifDO> netifDOList = Lists.newArrayListWithCapacity(netifBOList.size());
    for (DeviceNetifBO netifBO : netifBOList) {

      DeviceNetifDO netifDO = new DeviceNetifDO();
      BeanUtils.copyProperties(netifBO, netifDO);

      netifDO.setOperatorId(operatorId);
      netifDOList.add(netifDO);
    }

    deviceNetifDao.updateDeviceNetifs(netifDOList);

    return queryDeviceNetifs();
  }

  /**
   * @see com.machloop.fpc.manager.system.service.DeviceNetifService#monitorNetifState()
   */
  @Override
  public int monitorNetifState() {
    int update = 0;

    List<String> categoryList = Lists.newArrayList(FpcConstants.DEVICE_NETIF_CATEGORY_MGMT);
    List<DeviceNetifDO> netifDOList = deviceNetifDao.queryDeviceNetifs(categoryList);
    for (DeviceNetifDO netifDO : netifDOList) {
      String netifName = netifDO.getName();

      LOGGER.debug("Detect netif state for {}.", netifName);

      boolean isUp = MonitorNetifHelper.detectNetifState(netifName);
      update += deviceNetifDao.updateDeviceNetifState(netifDO.getId(),
          isUp ? Constants.STATE_UP : Constants.STATE_DOWN);
    }

    return update;
  }

  /**
   * @see com.machloop.fpc.manager.system.service.DeviceNetifService#statisticNetifUsage(java.util.Date)
   */
  public int statisticNetifUsage(Date metricTime) {

    int update = 0;

    // 整理metricDate为60秒的整倍数
    double multipleOfInterval = ((double) metricTime.getTime() / 1000)
        / (double) Constants.ONE_MINUTE_SECONDS;
    long roundValue = Math.round(multipleOfInterval);
    metricTime = new Date(roundValue * Constants.ONE_MINUTE_SECONDS * 1000);

    // 查询管理接口名称
    List<String> categoryList = Lists.newArrayList(FpcConstants.DEVICE_NETIF_CATEGORY_MGMT);
    List<DeviceNetifDO> netifDOList = deviceNetifDao.queryDeviceNetifs(categoryList);

    String mgtIfname = "";
    if (CollectionUtils.isNotEmpty(netifDOList)) {
      mgtIfname = netifDOList.get(0).getName();
    }

    if (StringUtils.isBlank(mgtIfname)) {
      return update;
    }

    MetricNetworkRrd metricNetStat = null;

    // 采集网卡统计信息
    Map<String, MonitorNetwork> trafficMap = MonitorNetifHelper
        .monitorNetifTraffic(Lists.newArrayList(mgtIfname));
    for (Map.Entry<String, MonitorNetwork> entry : trafficMap.entrySet()) {
      metricNetStat = new MetricNetworkRrd(metricTime, entry.getValue().getBytesRx(),
          entry.getValue().getPacketsRx(), entry.getValue().getBytesTx(),
          entry.getValue().getPacketsTx());
    }

    // 采集网卡数据异常，返回0
    if (metricNetStat == null) {
      return 0;
    }

    String rrdNameRxByteps = mgtIfname + FpcConstants.STAT_NETIF_RRD_RX_BYTEPS;
    String rrdNameRxPps = mgtIfname + FpcConstants.STAT_NETIF_RRD_RX_PPS;
    String rrdNameTxByteps = mgtIfname + FpcConstants.STAT_NETIF_RRD_TX_BYTEPS;
    String rrdNameTxPps = mgtIfname + FpcConstants.STAT_NETIF_RRD_TX_PPS;

    if (previousNetStat == null) {
      // 内存为空，从数据库中读上次统计值
      previousNetStat = new MetricNetworkRrd(rrdService.buildMetricStatElem(rrdNameRxByteps),
          rrdService.buildMetricStatElem(rrdNameRxPps),
          rrdService.buildMetricStatElem(rrdNameTxByteps),
          rrdService.buildMetricStatElem(rrdNameTxPps));
    }

    String[] rrdNames = {rrdNameRxByteps, rrdNameRxPps, rrdNameTxByteps, rrdNameTxPps};
    long currentTime = metricTime.getTime() / 1000;
    for (String rrdName : rrdNames) {
      // 获取速率
      MetricNetworkTraffic element = metricNetStat.getElement(rrdName);
      MetricNetworkTraffic newElement = new MetricNetworkTraffic(
          metricNetStat.getValue(previousNetStat, rrdName, Constants.ONE_MINUTE_SECONDS),
          element.getLastTime(), element.getLastPosition());
      update += rrdService.produceRrdData(newElement, previousNetStat.getElement(rrdName),
          currentTime, rrdName);
      // 将结果位置时间赋值给previousNetStat，用于下次计算
      element.setLastPosition(newElement.getLastPosition());
      element.setLastTime(newElement.getLastTime());
    }
    previousNetStat = metricNetStat;
    return update;
  }
}
