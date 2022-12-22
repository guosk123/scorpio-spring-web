package com.machloop.fpc.cms.center.metric.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.webapp.system.dao.AlarmDao;
import com.machloop.alpha.webapp.system.data.AlarmDO;
import com.machloop.alpha.webapp.system.vo.AlarmQueryVO;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.central.bo.FpcBO;
import com.machloop.fpc.cms.center.central.dao.FpcDiskIODao;
import com.machloop.fpc.cms.center.central.dao.FpcSystemDao;
import com.machloop.fpc.cms.center.central.service.FpcService;
import com.machloop.fpc.cms.center.metric.dao.MetricSensorNetworkFlowDao;
import com.machloop.fpc.cms.center.metric.service.MetricSensorService;
import com.machloop.fpc.cms.center.metric.vo.MetricSensorQueryVO;
import com.machloop.fpc.cms.center.sensor.dao.SensorNetworkDao;
import com.machloop.fpc.cms.center.sensor.data.SensorNetworkDO;
import com.machloop.fpc.cms.common.FpcCmsConstants;

/**
 * @author "Minjiajun"
 *
 * create at 2022年1月25日, fpc-cms-center
 */
@Service
public class MetricSensorServiceImpl implements MetricSensorService {

  public static final int NUM_100 = 100;

  private static final int SCALE_COUNTS = 4;

  @Autowired
  private FpcService fpcService;

  @Autowired
  private FpcSystemDao fpcSystemDao;

  @Autowired
  private FpcDiskIODao fpcDiskIODao;

  @Autowired
  private AlarmDao alarmDao;

  @Autowired
  private SensorNetworkDao sensorNetworkDao;

  @Autowired
  private MetricSensorNetworkFlowDao metricSensorNetworkFlowDao;

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricSensorService#queryIndexData(java.util.List)
   */
  @Override
  public Map<String, Object> queryIndexData(MetricSensorQueryVO queryVO) {

    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    List<String> indexList = CsvUtils.convertCSVToList(queryVO.getMetric());
    if (indexList.contains("online_sensor_count")) {
      result.put("onlineSensorCount", getOnlineFpcs().size());
    }
    if (indexList.contains(CenterConstants.SENSOR_ALARM_COUNT) && getOnlineFpcs().size() > 0) {
      AlarmQueryVO alarmQueryVO = new AlarmQueryVO();
      alarmQueryVO.setTimeBegin(queryVO.getStartTime());
      alarmQueryVO.setTimeEnd(queryVO.getEndTime());
      List<String> onlineSensorSerialNumberList = getOnlineFpcs().stream()
          .map(fpcBO -> fpcBO.getSerialNumber()).collect(Collectors.toList());
      String onlineSensorSerialNumbers = CsvUtils
          .convertCollectionToCSV(onlineSensorSerialNumberList);
      alarmQueryVO.setNodeId(onlineSensorSerialNumbers);
      // 这里获取Common、FPCManager、FPCEngine、SYSMonitor组件上报的告警
      alarmQueryVO.setComponent("000000,001001,001002,001004");
      List<AlarmDO> alarmList = alarmDao.queryAlarmsWithoutPage(alarmQueryVO);
      result.put("sensorAlarmCount", alarmList.size());
      result.put("onlineSensorSerialNumbers", onlineSensorSerialNumbers);
    } else {
      result.put("sensorAlarmCount", 0);
      result.put("onlineSensorSerialNumbers", 0);
    }
    return result;
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricSensorService#querySensorTopTrend(com.machloop.fpc.cms.center.metric.vo.MetricSensorQueryVO)
   */
  @Override
  public List<Map<String, Object>> queryUsageRateTopTrend(MetricSensorQueryVO queryVO) {

    List<String> onlineTfaSerialNumberList = getOnlineFpcs().stream().map(e -> e.getSerialNumber())
        .collect(Collectors.toList());
    if (CollectionUtils.isEmpty(onlineTfaSerialNumberList)) {
      return Lists.newArrayListWithExpectedSize(0);
    }
    List<String> topSerialNumberList = Lists
        .newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    queryVO.setDeviceType(FpcCmsConstants.DEVICE_TYPE_TFA);
    fpcSystemDao.queryFpcSystemByMetric(queryVO, onlineTfaSerialNumberList).forEach(map -> {
      topSerialNumberList.add(MapUtils.getString(map, "monitored_serial_number"));
    });
    if (CollectionUtils.isEmpty(topSerialNumberList)) {
      return Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    }

    List<Map<String, Object>> topTrendMetricList = fpcSystemDao
        .queryFpcSystemHistogramByMetric(queryVO, topSerialNumberList);

    return topTrendMetricList;
  }

  @Override
  public List<Map<String, Object>> queryDiskIOTopTrend(MetricSensorQueryVO queryVO) {

    List<String> onlineTfaSerialNumberList = getOnlineFpcs().stream().map(e -> e.getSerialNumber())
        .collect(Collectors.toList());
    if (CollectionUtils.isEmpty(onlineTfaSerialNumberList)) {
      return Lists.newArrayListWithExpectedSize(0);
    }

    List<String> topSerialNumberList = fpcDiskIODao.queryDiskIO(queryVO, onlineTfaSerialNumberList)
        .stream().map(e -> MapUtils.getString(e, "monitored_serial_number"))
        .collect(Collectors.toList());
    if (CollectionUtils.isEmpty(topSerialNumberList)) {
      return Lists.newArrayListWithExpectedSize(0);
    }

    return fpcDiskIODao.queryDiskIOHistogram(queryVO, topSerialNumberList);
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricSensorService#querySensorFreeSpace(com.machloop.fpc.cms.center.metric.vo.MetricSensorQueryVO)
   */
  @Override
  public List<Map<String, Object>> querySensorFreeSpace(MetricSensorQueryVO queryVO) {
    List<String> onlineTfaSerialNumberList = getOnlineFpcs().stream().map(e -> e.getSerialNumber())
        .collect(Collectors.toList());
    if (CollectionUtils.isEmpty(onlineTfaSerialNumberList)) {
      return Lists.newArrayListWithExpectedSize(0);
    }

    return fpcSystemDao.queryFpcFreeSpaceMetric(queryVO, onlineTfaSerialNumberList);
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricSensorService#querySensorNetworkTopTrend(com.machloop.fpc.cms.center.metric.vo.MetricSensorQueryVO)
   * 适用吞吐量、并发会话、新建会话TOP趋势图
   */
  @Override
  public List<Map<String, Object>> querySensorNetworkTopTrend(MetricSensorQueryVO queryVO) {
    // 获取在线设备
    List<String> onlineTfaSerialNumberList = getOnlineFpcs().stream().map(e -> e.getSerialNumber())
        .collect(Collectors.toList());
    if (CollectionUtils.isEmpty(onlineTfaSerialNumberList)) {
      return Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    }
    // 获取在线设备下的网络
    Map<String,
        List<String>> serialNumNetworkIdDict = sensorNetworkDao
            .querySensorNetworksBySensorIdList(onlineTfaSerialNumberList).stream()
            .collect(Collectors.groupingBy(SensorNetworkDO::getSensorId, HashMap::new,
                Collectors.mapping(SensorNetworkDO::getNetworkInSensorId, Collectors.toList())));

    // 获取所有网络当前指标
    Map<String,
        Long> allNetworkMetrics = metricSensorNetworkFlowDao.queryAllNetworkMetrics(queryVO)
            .stream().collect(Collectors.toMap(item -> MapUtils.getString(item, "network_id"),
                item -> MapUtils.getLong(item, queryVO.getMetric())));

    // 按设备聚合，根据指标降序排序，得到topN设备
    List<String> topSerialNumbers = serialNumNetworkIdDict.entrySet().stream()
        .collect(Collectors.toMap(Entry::getKey, item -> {
          return item.getValue().stream()
              .mapToLong(networkId -> MapUtils.getLong(allNetworkMetrics, networkId, 0L)).sum();
        })).entrySet().stream().filter(e -> e.getValue() != 0L)
        .sorted((p2, p1) -> p1.getValue().compareTo(p2.getValue())).map(e -> e.getKey())
        .limit(queryVO.getTopNumber()).collect(Collectors.toList());

    // 将topSerialNumbers的数量补全为onlineTfaSerialNumberList的数量
    if (topSerialNumbers.size() < onlineTfaSerialNumberList.size()) {
      @SuppressWarnings("unchecked")
      // collect为没有数据的探针序列号
      List<String> collect = (List<String>) CollectionUtils.removeAll(onlineTfaSerialNumberList,
          topSerialNumbers);
      topSerialNumbers.addAll(collect.subList(0,
          queryVO.getTopNumber() > onlineTfaSerialNumberList.size()
              ? onlineTfaSerialNumberList.size() - topSerialNumbers.size()
              : queryVO.getTopNumber() - topSerialNumbers.size()));
    }

    // 统计topN设备的趋势图
    List<Map<String, Object>> result = Lists
        .newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    for (String serialNumber : topSerialNumbers) {
      List<String> networkIdList = serialNumNetworkIdDict.get(serialNumber);
      if (CollectionUtils.isEmpty(networkIdList)) {
        continue;
      }

      List<Map<String, Object>> targetSensorMetric = metricSensorNetworkFlowDao
          .querySensorNetworkHistogramByMetric(queryVO, networkIdList);
      targetSensorMetric.forEach(sensorMetric -> sensorMetric.put("serialNumber", serialNumber));

      result.addAll(targetSensorMetric);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricSensorService#queryEstablishSuccessRateTopTrend(com.machloop.fpc.cms.center.metric.vo.MetricSensorQueryVO)
   * 适用TCP建连成功率最差TOP趋势
   */
  @Override
  public List<Map<String, Object>> queryEstablishSuccessRateTopTrend(MetricSensorQueryVO queryVO) {
    // 获取在线设备
    List<String> onlineTfaSerialNumberList = getOnlineFpcs().stream().map(e -> e.getSerialNumber())
        .collect(Collectors.toList());
    if (CollectionUtils.isEmpty(onlineTfaSerialNumberList)) {
      return Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    }
    // 获取在线设备下的网络
    Map<String,
        List<String>> serialNumNetworkIdDict = sensorNetworkDao
            .querySensorNetworksBySensorIdList(onlineTfaSerialNumberList).stream()
            .collect(Collectors.groupingBy(SensorNetworkDO::getSensorId, HashMap::new,
                Collectors.mapping(SensorNetworkDO::getNetworkInSensorId, Collectors.toList())));

    // 获取所有网络统计指标
    Map<String,
        Map<String, Object>> allNetworkEstabSessionCount = metricSensorNetworkFlowDao
            .queryAllNetworkTcpEstablishedSessionCount(queryVO).stream().collect(
                Collectors.toMap(item -> MapUtils.getString(item, "network_id"), item -> item));

    // 按设备聚合，根据指标升序排序，得到topN设备
    List<String> topSerialNumbers = serialNumNetworkIdDict.entrySet().stream()
        .collect(Collectors.toMap(Entry::getKey, item -> {
          double successCount = 0;
          double totalCount = 0;
          for (String networkId : item.getValue()) {
            Map<String, Object> temp = allNetworkEstabSessionCount.get(networkId);
            if (temp != null) {
              successCount += MapUtils.getDoubleValue(temp, "success");
              totalCount += MapUtils.getDoubleValue(temp, "total");
            }
          }

          return totalCount == 0 ? BigDecimal.ZERO
              : new BigDecimal(String.valueOf(successCount / totalCount)).setScale(SCALE_COUNTS,
                  RoundingMode.HALF_UP);
        })).entrySet().stream().filter(e -> e.getValue() != BigDecimal.ZERO)
        .sorted((p1, p2) -> p1.getValue().compareTo(p2.getValue())).map(e -> e.getKey())
        .limit(queryVO.getTopNumber()).collect(Collectors.toList());

    // 将topSerialNumbers的数量补全为onlineTfaSerialNumberList的数量
    if (topSerialNumbers.size() < onlineTfaSerialNumberList.size()) {
      @SuppressWarnings("unchecked")
      // collect为没有数据的探针序列号
      List<String> collect = (List<String>) CollectionUtils.removeAll(onlineTfaSerialNumberList,
          topSerialNumbers);
      topSerialNumbers.addAll(collect.subList(0,
          queryVO.getTopNumber() > onlineTfaSerialNumberList.size()
              ? onlineTfaSerialNumberList.size() - topSerialNumbers.size()
              : queryVO.getTopNumber() - topSerialNumbers.size()));
    }

    // 统计topN设备的趋势图
    List<Map<String, Object>> result = Lists
        .newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    for (String serialNumber : topSerialNumbers) {
      List<Map<String, Object>> targetSensorMetricList = metricSensorNetworkFlowDao
          .queryEstablishedSuccessRateHistogram(queryVO, serialNumNetworkIdDict.get(serialNumber));

      targetSensorMetricList
          .forEach(sensorMetric -> sensorMetric.put("serialNumber", serialNumber));

      result.addAll(targetSensorMetricList);
    }

    return result;
  }

  private List<FpcBO> getOnlineFpcs() {
    return fpcService.queryAllFpc().stream().filter(
        fpc -> StringUtils.equals(fpc.getConnectStatus(), FpcCmsConstants.CONNECT_STATUS_NORMAL))
        .collect(Collectors.toList());
  }
}
