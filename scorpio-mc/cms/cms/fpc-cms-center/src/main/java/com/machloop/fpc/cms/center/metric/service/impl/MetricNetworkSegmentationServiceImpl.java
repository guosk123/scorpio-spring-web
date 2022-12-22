package com.machloop.fpc.cms.center.metric.service.impl;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.TextUtils;
import com.machloop.fpc.cms.center.metric.dao.*;
import com.machloop.fpc.cms.center.metric.service.MetricNetworkSegmentationService;
import com.machloop.fpc.cms.center.metric.vo.MetricQueryVO;
import com.machloop.fpc.cms.center.sensor.dao.SensorNetworkGroupDao;

/**
 * @author ChenXiao
 * create at 2022/12/8
 */
@Service
public class MetricNetworkSegmentationServiceImpl implements MetricNetworkSegmentationService {


  @Autowired
  private MetricL3DeviceDataRecordDao metricL3DeviceDataRecordDao;

  @Autowired
  private MetricIpConversationDataRecordDao metricIpConversationDataRecordDao;

  @Autowired
  private MetricApplicationDataRecordDao metricApplicationDataRecordDao;

  @Autowired
  private MetricPortDataRecordDao metricPortDataRecordDao;

  @Autowired
  private MetricServiceDataRecordDao metricServiceDataRecordDao;

  @Autowired
  private SensorNetworkGroupDao networkGroupDao;

  @Override
  public List<Map<String, Object>> queryMetricNetworkSegmentationL3Devices(MetricQueryVO queryVO,
      String sortProperty, String sortDirection) {

    List<Map<String, Object>> networkResultList = metricL3DeviceDataRecordDao
        .queryMetricNetworkSegmentationL3Devices(queryVO, sortProperty, sortDirection);

    return mergeMetrics(queryVO, networkResultList, sortProperty, sortDirection);
  }


  @Override
  public List<Map<String, Object>> queryMetricNetworkSegmentationIpConversations(
      MetricQueryVO queryVO, String sortProperty, String sortDirection) {

    List<Map<String, Object>> networkResultList = metricIpConversationDataRecordDao
        .queryMetricNetworkSegmentationIpConversations(queryVO, sortProperty, sortDirection);

    return mergeMetrics(queryVO, networkResultList, sortProperty, sortDirection);
  }

  @Override
  public List<Map<String, Object>> queryMetricNetworkSegmentationApplications(MetricQueryVO queryVO,
      String sortProperty, String sortDirection) {

    List<Map<String, Object>> networkResultList = metricApplicationDataRecordDao
        .queryMetricNetworkSegmentationApplications(queryVO, sortProperty, sortDirection);

    return mergeMetrics(queryVO, networkResultList, sortProperty, sortDirection);
  }

  @Override
  public List<Map<String, Object>> queryMetricNetworkSegmentationPorts(MetricQueryVO queryVO,
      String sortProperty, String sortDirection) {

    List<Map<String, Object>> networkResultList = metricPortDataRecordDao
        .queryMetricNetworkSegmentationPorts(queryVO, sortProperty, sortDirection);

    return mergeMetrics(queryVO, networkResultList, sortProperty, sortDirection);
  }

  @Override
  public List<Map<String, Object>> queryMetricNetworkSegmentationServices(MetricQueryVO queryVO,
      String sortProperty, String sortDirection) {

    List<Map<String, Object>> networkResultList = metricServiceDataRecordDao
        .queryMetricNetworkSegmentationServices(queryVO, sortProperty, sortDirection);

    return mergeMetrics(queryVO, networkResultList, sortProperty, sortDirection);
  }

  private List<Map<String, Object>> mergeMetrics(MetricQueryVO queryVO,
      List<Map<String, Object>> networkResultList, String sortProperty, String sortDirection) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isNotEmpty(queryVO.getNetworkId())) {
      List<String> singleNetworkList = CsvUtils.convertCSVToList(queryVO.getNetworkId());
      result.addAll(networkResultList.stream()
          .filter(map -> singleNetworkList.contains(MapUtils.getString(map, "networkId")))
          .collect(Collectors.toList()));
      List<String> resultNetworkList = networkResultList.stream()
          .map(map -> MapUtils.getString(map, "networkId")).collect(Collectors.toList());
      List<String> leaveNetworkIds = singleNetworkList.stream()
          .filter(x -> !resultNetworkList.contains(x)).collect(Collectors.toList());
      leaveNetworkIds.forEach(leaveNetworkId -> {
        Map<String, Object> item = new HashMap<>();
        item.put("networkId", leaveNetworkId);
        item.putAll(addKPIFields(queryVO, Lists.newArrayListWithCapacity(0)));
        result.add(item);
      });
    }
    if (StringUtils.isNotEmpty(queryVO.getNetworkGroupId())) {
      List<String> networkGroupIdList = CsvUtils.convertCSVToList(queryVO.getNetworkGroupId());
      networkGroupIdList.forEach(networkGroupId -> {
        result.add(mergeMetricsOfNetworkGroup(queryVO, networkGroupId, networkResultList));
      });
    }
    // 重新排序
    result.sort(new Comparator<Map<String, Object>>() {
      @Override
      public int compare(Map<String, Object> o1, Map<String, Object> o2) {
        Long o1Value = MapUtils.getLongValue(o1, TextUtils.underLineToCamel(sortProperty), 0);
        Long o2Value = MapUtils.getLongValue(o2, TextUtils.underLineToCamel(sortProperty), 0);

        return StringUtils.equalsIgnoreCase(sortDirection, Sort.Direction.ASC.name())
            ? o1Value.compareTo(o2Value)
            : o2Value.compareTo(o1Value);
      }
    });

    return result;
  }

  private Map<String, Object> mergeMetricsOfNetworkGroup(MetricQueryVO queryVO,
      String networkGroupId, List<Map<String, Object>> networkResultList) {
    Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    item.put("networkGroupId", networkGroupId);
    List<String> networkIds = CsvUtils.convertCSVToList(
        networkGroupDao.querySensorNetworkGroup(networkGroupId).getNetworkInSensorIds());
    networkResultList = networkResultList.stream()
        .filter(x -> networkIds.contains(MapUtils.getString(x, "networkId")))
        .collect(Collectors.toList());
    item.putAll(addKPIFields(queryVO, networkResultList));
    return item;
  }

  public Map<String, Object> addKPIFields(MetricQueryVO queryVO,
      List<Map<String, Object>> networkResultList) {
    String columns = queryVO.getColumns();
    Map<String, Object> item = new HashMap<>();
    if (columns.contains("totalBytes")) {
      item.put("totalBytes",
          networkResultList.isEmpty() ? 0
              : networkResultList.stream()
                  .mapToLong(map -> MapUtils.getLongValue(map, "totalBytes")).sum());
    }
    if (columns.contains("totalPackets")) {
      item.put("totalPackets",
          networkResultList.isEmpty() ? 0
              : networkResultList.stream()
                  .mapToLong(map -> MapUtils.getLongValue(map, "totalPackets")).sum());
    }
    if (columns.contains("establishedSessions")) {
      item.put("establishedSessions",
          networkResultList.isEmpty() ? 0
              : networkResultList.stream()
                  .mapToLong(map -> MapUtils.getLongValue(map, "establishedSessions")).sum());
    }
    if (columns.contains("tcpClientNetworkLatency")) {
      item.put("tcpClientNetworkLatency",
          networkResultList.isEmpty() ? 0
              : networkResultList.stream()
                  .mapToLong(map -> MapUtils.getLongValue(map, "tcpClientNetworkLatency")).sum());
    }
    if (columns.contains("tcpClientNetworkLatencyCounts")) {
      item.put("tcpClientNetworkLatencyCounts",
          networkResultList.isEmpty() ? 0
              : networkResultList.stream()
                  .mapToLong(map -> MapUtils.getLongValue(map, "tcpClientNetworkLatencyCounts"))
                  .sum());
    }
    if (columns.contains("tcpServerNetworkLatency")) {
      item.put("tcpServerNetworkLatency",
          networkResultList.isEmpty() ? 0
              : networkResultList.stream()
                  .mapToLong(map -> MapUtils.getLongValue(map, "tcpServerNetworkLatency")).sum());
    }
    if (columns.contains("tcpServerNetworkLatencyCounts")) {
      item.put("tcpServerNetworkLatencyCounts",
          networkResultList.isEmpty() ? 0
              : networkResultList.stream()
                  .mapToLong(map -> MapUtils.getLongValue(map, "tcpServerNetworkLatencyCounts"))
                  .sum());
    }
    if (columns.contains("serverResponseLatency")) {
      item.put("serverResponseLatency",
          networkResultList.isEmpty() ? 0
              : networkResultList.stream()
                  .mapToLong(map -> MapUtils.getLongValue(map, "serverResponseLatency")).sum());
    }
    if (columns.contains("serverResponseLatencyCounts")) {
      item.put("serverResponseLatencyCounts",
          networkResultList.isEmpty() ? 0
              : networkResultList.stream()
                  .mapToLong(map -> MapUtils.getLongValue(map, "serverResponseLatencyCounts"))
                  .sum());
    }
    if (columns.contains("tcpClientPackets")) {
      item.put("tcpClientPackets",
          networkResultList.isEmpty() ? 0
              : networkResultList.stream()
                  .mapToLong(map -> MapUtils.getLongValue(map, "tcpClientPackets")).sum());
    }
    if (columns.contains("tcpClientRetransmissionPackets")) {
      item.put("tcpClientRetransmissionPackets",
          networkResultList.isEmpty() ? 0
              : networkResultList.stream()
                  .mapToLong(map -> MapUtils.getLongValue(map, "tcpClientRetransmissionPackets"))
                  .sum());
    }
    if (columns.contains("tcpServerPackets")) {
      item.put("tcpServerPackets",
          networkResultList.isEmpty() ? 0
              : networkResultList.stream()
                  .mapToLong(map -> MapUtils.getLongValue(map, "tcpServerPackets")).sum());
    }
    if (columns.contains("tcpServerRetransmissionPackets")) {
      item.put("tcpServerRetransmissionPackets",
          networkResultList.isEmpty() ? 0
              : networkResultList.stream()
                  .mapToLong(map -> MapUtils.getLongValue(map, "tcpServerRetransmissionPackets"))
                  .sum());
    }
    if (columns.contains("tcpClientZeroWindowPackets")) {
      item.put("tcpClientZeroWindowPackets",
          networkResultList.isEmpty() ? 0
              : networkResultList.stream()
                  .mapToLong(map -> MapUtils.getLongValue(map, "tcpClientZeroWindowPackets"))
                  .sum());
    }
    if (columns.contains("tcpServerZeroWindowPackets")) {
      item.put("tcpServerZeroWindowPackets",
          networkResultList.isEmpty() ? 0
              : networkResultList.stream()
                  .mapToLong(map -> MapUtils.getLongValue(map, "tcpServerZeroWindowPackets"))
                  .sum());
    }
    if (columns.contains("tcpEstablishedSuccessCounts")) {
      item.put("tcpEstablishedSuccessCounts",
          networkResultList.isEmpty() ? 0
              : networkResultList.stream()
                  .mapToLong(map -> MapUtils.getLongValue(map, "tcpEstablishedSuccessCounts"))
                  .sum());
    }
    if (columns.contains("tcpEstablishedFailCounts")) {
      item.put("tcpEstablishedFailCounts",
          networkResultList.isEmpty() ? 0
              : networkResultList.stream()
                  .mapToLong(map -> MapUtils.getLongValue(map, "tcpEstablishedFailCounts")).sum());
    }
    return item;
  }
}
