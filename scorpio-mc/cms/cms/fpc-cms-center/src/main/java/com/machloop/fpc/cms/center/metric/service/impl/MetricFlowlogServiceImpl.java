package com.machloop.fpc.cms.center.metric.service.impl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Inet4Address;
import java.net.Inet6Address;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.ServletContext;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.ExportUtils;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.common.util.TextUtils;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.appliance.bo.HostGroupBO;
import com.machloop.fpc.cms.center.appliance.service.HostGroupService;
import com.machloop.fpc.cms.center.knowledge.bo.*;
import com.machloop.fpc.cms.center.knowledge.service.GeoService;
import com.machloop.fpc.cms.center.knowledge.service.SaProtocolService;
import com.machloop.fpc.cms.center.knowledge.service.SaService;
import com.machloop.fpc.cms.center.metric.dao.MetricFlowlogDataRecordDao;
import com.machloop.fpc.cms.center.metric.service.MetricFlowlogService;
import com.machloop.fpc.cms.center.metric.vo.MetricFlowLogQueryVO;
import com.machloop.fpc.cms.center.metric.vo.MetricQueryVO;
import com.machloop.fpc.cms.center.sensor.bo.SensorNetworkPermBO;
import com.machloop.fpc.cms.center.sensor.dao.SensorLogicalSubnetDao;
import com.machloop.fpc.cms.center.sensor.dao.SensorNetworkDao;
import com.machloop.fpc.cms.center.sensor.dao.SensorNetworkGroupDao;
import com.machloop.fpc.cms.center.sensor.data.SensorLogicalSubnetDO;
import com.machloop.fpc.cms.center.sensor.data.SensorNetworkDO;
import com.machloop.fpc.cms.center.sensor.data.SensorNetworkGroupDO;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkPermService;
import com.machloop.fpc.cms.common.FpcCmsConstants;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * @author guosk
 *
 * create at 2021年6月1日, fpc-manager
 */
@Service
public class MetricFlowlogServiceImpl implements MetricFlowlogService {

  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(MetricFlowlogServiceImpl.class);

  @Autowired
  private MetricFlowlogDataRecordDao metricFlowlogDataRecordDao;
  @Autowired
  private SensorNetworkDao sensorNetworkDao;
  @Autowired
  private SensorLogicalSubnetDao logicalSubnetDao;
  @Autowired
  private SensorNetworkGroupDao networkGroupDao;
  @Autowired
  private SensorNetworkPermService sensorNetworkPermService;

  @Autowired
  private SaService saService;
  @Autowired
  private GeoService geoService;
  @Autowired
  private HostGroupService hostGroupService;
  @Autowired
  private SaProtocolService saProtocolService;
  @Autowired
  private DictManager dictManager;
  @Autowired
  private ServletContext servletContext;
  private static final Map<String,
      String> fields = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  static {
    fields.put("interface", "接口编号");
    fields.put("networkId", "所属网络");
    fields.put("serviceId", "所属业务");
    fields.put("startTime", "开始时间");
    fields.put("reportTime", "记录时间");
    fields.put("duration", "持续时间(s)");
    fields.put("upstreamBytes", "上行字节数");
    fields.put("downstreamBytes", "下行字节数");
    fields.put("totalBytes", "总字节数");
    fields.put("upstreamPackets", "正向包数");
    fields.put("downstreamPackets", "反向包数");
    fields.put("totalPackets", "总数据包");
    fields.put("upstreamPayloadBytes", "上行负载字节数");
    fields.put("downstreamPayloadBytes", "下行负载字节数");
    fields.put("totalPayloadBytes", "总负载字节数");
    fields.put("upstreamPayloadPackets", "上行负载数据包数");
    fields.put("downstreamPayloadPackets", "下行负载数据包数");
    fields.put("totalPayloadPackets", "总负载包数");
    fields.put("tcpClientNetworkLatency", "客户端网络时延");
    fields.put("tcpServerNetworkLatency", "服务器网络时延");
    fields.put("tcpClientNetworkLatencyAvg", "客户端网络平均时延");
    fields.put("tcpServerNetworkLatencyAvg", "服务器网络平均时延");
    fields.put("serverResponseLatency", "服务器响应时延");
    fields.put("serverResponseLatencyAvg", "服务器响应平均时延");
    fields.put("tcpClientLossBytes", "TCP客户端丢包字节数");
    fields.put("tcpServerLossBytes", "TCP服务端丢包字节数");
    fields.put("tcpClientZeroWindowPackets", "客户端零窗口包数");
    fields.put("tcpServerZeroWindowPackets", "服务器零窗口包数");
    fields.put("tcpSessionState", "tcp会话状态");
    fields.put("tcpEstablishedSuccessCounts", "TCP建立成功数");
    fields.put("tcpEstablishedFailCounts", "TCP建立失败数");
    fields.put("establishedSessions", "新建会话数");
    fields.put("tcpSynPackets", "TCP同步数据包数");
    fields.put("tcpSynAckPackets", "TCP同步确认数据包数");
    fields.put("tcpSynRstPackets", "TCP同步重置数据包");
    fields.put("tcpClientRetransmissionPackets", "TCP客户端重传包数");
    fields.put("tcpClientPackets", "TCP客户端总包数");
    fields.put("tcpClientRetransmissionRate", "客户端重传率");
    fields.put("tcpServerRetransmissionPackets", "TCP服务端重传包数");
    fields.put("tcpServerRetransmissionRate", "服务端重传率");
    fields.put("tcpServerPackets", "TCP服务端总包数");
    fields.put("macAddress", "MAC地址");
    fields.put("ethernetType", "三层协议类型");
    fields.put("ethernetProtocol", "网络层协议");
    fields.put("hostgroupId", "地址组");
    fields.put("hostgroupIdInitiator", "源IP所属地址组");
    fields.put("hostgroupIdResponder", "目的IP所属地址组");
    fields.put("ipLocality", "IP所在位置");
    fields.put("vlanId", "VLANID");
    fields.put("port", "端口号");
    fields.put("ipProtocol", "传输层协议");
    fields.put("l7ProtocolId", "应用层协议");
    fields.put("categoryId", "应用分类");
    fields.put("subcategoryId", "应用子分类");
    fields.put("applicationId", "应用名称");
    fields.put("provinceId", "省份");
    fields.put("countryId", "国家");
    fields.put("ipAddress", "IP地址");
    fields.put("ipAAddress", "IP_A");
    fields.put("ipBAddress", "IP_B");
    fields.put("totalBytes", "总字节数");
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricFlowlogService#queryMetricNetworks(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricNetworks(MetricQueryVO queryVO, String sortProperty,
      String sortDirection) {
    MetricFlowLogQueryVO metricFlowLogQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, metricFlowLogQueryVO);

    return queryMetricNetworks(metricFlowLogQueryVO, sortProperty, sortDirection, false);
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricFlowlogService#queryMetricNetworkHistograms(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String)
   */
  public List<Map<String, Object>> queryMetricNetworkHistograms(MetricQueryVO queryVO,
      String sortProperty, String sortDirection) {
    MetricFlowLogQueryVO metricFlowLogQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, metricFlowLogQueryVO);

    // 查询网络组
    Map<String, String> networkGroups = networkGroupDao.querySensorNetworkGroups().stream().collect(
        Collectors.toMap(SensorNetworkGroupDO::getId, SensorNetworkGroupDO::getNetworkInSensorIds));

    List<String> networkIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<String> networkGroupIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isAllBlank(queryVO.getNetworkId(), queryVO.getNetworkGroupId())) {
      List<Map<String, Object>> metricNetworks = queryMetricNetworks(metricFlowLogQueryVO,
          sortProperty, sortDirection, true);
      metricNetworks.forEach(item -> {
        String itemNetworkId = MapUtils.getString(item, "networkId");
        String itemNetworkGroupId = MapUtils.getString(item, "networkGroupId");
        if (StringUtils.isNotBlank(itemNetworkId)) {
          networkIds.add(itemNetworkId);
        } else if (StringUtils.isNotBlank(itemNetworkGroupId)) {
          networkGroupIds.add(itemNetworkGroupId);
        }
      });
    } else {
      networkIds.addAll(CsvUtils.convertCSVToList(queryVO.getNetworkId()));
      networkGroupIds.addAll(CsvUtils.convertCSVToList(queryVO.getNetworkGroupId()));
    }

    if (networkIds.isEmpty() && networkGroupIds.isEmpty()) {
      return Lists.newArrayListWithCapacity(0);
    }

    // 查询网络、网络组所包含的所有网络时序统计
    List<String> allNetworkIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    allNetworkIds.addAll(networkIds);
    networkGroupIds
        .forEach(item -> allNetworkIds.addAll(CsvUtils.convertCSVToList(networkGroups.get(item))));
    List<Map<String, Object>> networkHistograms = metricFlowlogDataRecordDao
        .queryMetricNetworkHistograms(metricFlowLogQueryVO, sortProperty, allNetworkIds);

    // 根据选择的网络组，将网络聚合成网络组
    if (CollectionUtils.isNotEmpty(networkGroupIds)) {
      String field = TextUtils.underLineToCamel(sortProperty);
      // 合并网络组
      networkGroupIds.stream()
          .filter(itemNetworkGroupId -> networkGroups.containsKey(itemNetworkGroupId))
          .forEach(itemNetworkGroupId -> {
            List<String> networkIdsInGroup = CsvUtils
                .convertCSVToList(networkGroups.get(itemNetworkGroupId));

            Map<String, Map<String, Object>> oneNetworkGroupMap = Maps
                .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
            networkHistograms.stream()
                .filter(item -> networkIdsInGroup.contains(MapUtils.getString(item, "networkId")))
                .forEach(item -> {
                  String timestamp = MapUtils.getString(item, "timestamp");
                  Map<String, Object> timestampMap = oneNetworkGroupMap.getOrDefault(timestamp,
                      Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE));
                  timestampMap.put("networkGroupId", itemNetworkGroupId);
                  timestampMap.put("timestamp", item.get("timestamp"));
                  timestampMap.put(field, MapUtils.getLongValue(item, field, 0)
                      + MapUtils.getLongValue(timestampMap, field, 0));

                  oneNetworkGroupMap.put(timestamp, timestampMap);
                });

            networkHistograms.addAll(oneNetworkGroupMap.values());
          });

      // 移除网络组内的网络
      allNetworkIds.removeAll(networkIds);
      Iterator<Map<String, Object>> iterator = networkHistograms.iterator();
      while (iterator.hasNext()) {
        Map<String, Object> next = iterator.next();
        String tempNetworkId = MapUtils.getString(next, "networkId");
        if (allNetworkIds.contains(tempNetworkId)) {
          iterator.remove();
        }
      }
    }

    // 重新排序
    networkHistograms.sort(new Comparator<Map<String, Object>>() {
      @Override
      public int compare(Map<String, Object> o1, Map<String, Object> o2) {
        OffsetDateTime o1Value = (OffsetDateTime) o1.get("timestamp");
        OffsetDateTime o2Value = (OffsetDateTime) o2.get("timestamp");

        return o1Value.compareTo(o2Value);
      }
    });

    return networkHistograms;
  }

  private List<Map<String, Object>> queryMetricNetworks(MetricFlowLogQueryVO queryVO,
      String sortProperty, String sortDirection, boolean onlyAggSortProperty) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 获取当前用户的网络（组）权限
    SensorNetworkPermBO currentUserNetworkPerms = sensorNetworkPermService
        .queryCurrentUserNetworkPerms();

    // 查询网络组
    Map<String, String> networkGroups = networkGroupDao.querySensorNetworkGroups().stream().collect(
        Collectors.toMap(SensorNetworkGroupDO::getId, SensorNetworkGroupDO::getNetworkInSensorIds));

    // 确认本次需要查询的网络（组）
    List<String> networkIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<String> networkGroupIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    boolean all = false;
    if (StringUtils.isNotBlank(queryVO.getNetworkId())
        || StringUtils.isNotBlank(queryVO.getNetworkGroupId())) {
      networkIds.addAll(CsvUtils.convertCSVToList(queryVO.getNetworkId()));
      networkGroupIds.addAll(CsvUtils.convertCSVToList(queryVO.getNetworkGroupId()));
    } else {
      all = true;
      // 查询系统内所有的网络（组）
      networkIds.addAll(sensorNetworkDao.querySensorNetworks().stream()
          .map(SensorNetworkDO::getNetworkInSensorId).collect(Collectors.toList()));
      networkIds.addAll(logicalSubnetDao.querySensorLogicalSubnets().stream()
          .map(SensorLogicalSubnetDO::getId).collect(Collectors.toList()));
      networkGroupIds.addAll(networkGroups.keySet());

      if (!currentUserNetworkPerms.getServiceUser()) {
        // 过滤当前用户可用网络
        List<String> networkPerms = CsvUtils
            .convertCSVToList(currentUserNetworkPerms.getNetworkIds());
        networkIds = networkIds.stream().filter(networkId -> networkPerms.contains(networkId))
            .collect(Collectors.toList());

        // 过滤当前用户可用网络组
        List<String> networkGroupPerms = CsvUtils
            .convertCSVToList(currentUserNetworkPerms.getNetworkGroupIds());
        networkGroupIds = networkGroupIds.stream()
            .filter(networkGroupId -> networkGroupPerms.contains(networkGroupId))
            .collect(Collectors.toList());
      }
    }

    if (networkIds.isEmpty() && networkGroupIds.isEmpty()) {
      return Lists.newArrayListWithCapacity(0);
    }

    // 获取要查询的具体网络
    List<String> allNetworkIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    allNetworkIds.addAll(networkIds);
    final List<String> fNetworkGroupIds = networkGroupIds;
    networkGroups.entrySet().stream().filter(item -> fNetworkGroupIds.contains(item.getKey()))
        .forEach(item -> allNetworkIds.addAll(CsvUtils.convertCSVToList(item.getValue())));
    queryVO.setNetworkIds(allNetworkIds);
    // 获取网络统计
    result.addAll(metricFlowlogDataRecordDao.queryMetricNetworks(queryVO, sortProperty,
        sortDirection, onlyAggSortProperty));

    // 根据选择的网络组，将网络聚合成网络组
    if (CollectionUtils.isNotEmpty(networkGroupIds)) {
      // 合并网络组
      List<Map<String, Object>> networkGroupMetrics = networkGroupIds.stream()
          .filter(networkGroupId -> networkGroups.containsKey(networkGroupId))
          .map(networkGroupId -> {
            List<String> networkIdsInGroup = CsvUtils
                .convertCSVToList(networkGroups.get(networkGroupId));
            List<Map<String, Object>> temp = result.stream()
                .filter(item -> networkIdsInGroup.contains(MapUtils.getString(item, "networkId")))
                .collect(Collectors.toList());

            return mergeNetworkGroupMetrics(networkGroupId, temp, onlyAggSortProperty,
                sortProperty);
          }).collect(Collectors.toList());

      if (!all) {
        // 移除网络组内的网络
        allNetworkIds.removeAll(networkIds);
        Iterator<Map<String, Object>> iterator = result.iterator();
        while (iterator.hasNext()) {
          Map<String, Object> next = iterator.next();
          String networkId = MapUtils.getString(next, "networkId");
          if (allNetworkIds.contains(networkId)) {
            iterator.remove();
          }
        }
      }

      result.addAll(networkGroupMetrics);
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

  private Map<String, Object> mergeNetworkGroupMetrics(String networkGroupId,
      List<Map<String, Object>> dataRecord, boolean onlyAggSortProperty, String sortProperty) {
    Map<String, Object> mergeResult = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    mergeResult.put("networkGroupId", networkGroupId);

    if (onlyAggSortProperty) {
      String field = TextUtils.underLineToCamel(sortProperty);
      mergeResult.put(field,
          dataRecord.stream().mapToLong(item -> MapUtils.getLongValue(item, field, 0)).sum());
      return mergeResult;
    }

    mergeResult.put("totalBytes",
        dataRecord.stream().mapToLong(item -> MapUtils.getLongValue(item, "totalBytes", 0)).sum());
    mergeResult.put("totalPackets", dataRecord.stream()
        .mapToLong(item -> MapUtils.getLongValue(item, "totalPackets", 0)).sum());
    mergeResult.put("downstreamBytes", dataRecord.stream()
        .mapToLong(item -> MapUtils.getLongValue(item, "downstreamBytes", 0)).sum());
    mergeResult.put("downstreamPackets", dataRecord.stream()
        .mapToLong(item -> MapUtils.getLongValue(item, "downstreamPackets", 0)).sum());
    mergeResult.put("upstreamBytes", dataRecord.stream()
        .mapToLong(item -> MapUtils.getLongValue(item, "upstreamBytes", 0)).sum());
    mergeResult.put("upstreamPackets", dataRecord.stream()
        .mapToLong(item -> MapUtils.getLongValue(item, "upstreamPackets", 0)).sum());
    mergeResult.put("establishedSessions", dataRecord.stream()
        .mapToLong(item -> MapUtils.getLongValue(item, "establishedSessions", 0)).sum());
    mergeResult.put("tcpClientNetworkLatencyAvg",
        dataRecord.stream()
            .mapToDouble(item -> MapUtils.getDoubleValue(item, "tcpClientNetworkLatencyAvg", 0))
            .average());
    mergeResult.put("tcpServerNetworkLatencyAvg",
        dataRecord.stream()
            .mapToDouble(item -> MapUtils.getDoubleValue(item, "tcpServerNetworkLatencyAvg", 0))
            .average());
    mergeResult.put("serverResponseLatencyAvg",
        dataRecord.stream()
            .mapToDouble(item -> MapUtils.getDoubleValue(item, "serverResponseLatencyAvg", 0))
            .average());
    mergeResult.put("tcpClientRetransmissionPackets", dataRecord.stream()
        .mapToLong(item -> MapUtils.getLongValue(item, "tcpClientRetransmissionPackets", 0)).sum());
    mergeResult.put("tcpClientPackets", dataRecord.stream()
        .mapToLong(item -> MapUtils.getLongValue(item, "tcpClientPackets", 0)).sum());
    mergeResult.put("tcpClientRetransmissionRate",
        dataRecord.stream()
            .mapToDouble(item -> MapUtils.getDoubleValue(item, "tcpClientRetransmissionRate", 0))
            .average());
    mergeResult.put("tcpServerRetransmissionPackets", dataRecord.stream()
        .mapToLong(item -> MapUtils.getLongValue(item, "tcpServerRetransmissionPackets", 0)).sum());
    mergeResult.put("tcpServerPackets", dataRecord.stream()
        .mapToLong(item -> MapUtils.getLongValue(item, "tcpServerPackets", 0)).sum());
    mergeResult.put("tcpServerRetransmissionRate",
        dataRecord.stream()
            .mapToDouble(item -> MapUtils.getDoubleValue(item, "tcpServerRetransmissionRate", 0))
            .average());
    mergeResult.put("tcpClientZeroWindowPackets", dataRecord.stream()
        .mapToLong(item -> MapUtils.getLongValue(item, "tcpClientZeroWindowPackets", 0)).sum());
    mergeResult.put("tcpServerZeroWindowPackets", dataRecord.stream()
        .mapToLong(item -> MapUtils.getLongValue(item, "tcpServerZeroWindowPackets", 0)).sum());
    mergeResult.put("tcpEstablishedSuccessCounts", dataRecord.stream()
        .mapToLong(item -> MapUtils.getLongValue(item, "tcpEstablishedSuccessCounts", 0)).sum());
    mergeResult.put("tcpEstablishedFailCounts", dataRecord.stream()
        .mapToLong(item -> MapUtils.getLongValue(item, "tcpEstablishedFailCounts", 0)).sum());

    return mergeResult;
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricFlowlogService#queryMetricLocations(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricLocations(MetricQueryVO queryVO, String sortProperty,
      String sortDirection) {
    MetricFlowLogQueryVO metricFlowLogQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, metricFlowLogQueryVO);

    List<Map<String, Object>> metricLocations = metricFlowlogDataRecordDao
        .queryMetricLocations(metricFlowLogQueryVO, sortProperty, sortDirection, false);
    // 获取地区字典
    Tuple3<Map<String, String>, Map<String, String>,
        Map<String, String>> locationDict = queryGeoIpDict();

    if (CollectionUtils.isNotEmpty(metricLocations)) {
      Map<String, String> locations = geoService.queryAllLocationIdNameMapping();
      metricLocations = metricLocations.stream()
          .filter(item -> locations.containsKey(MapUtils.getString(item, "countryId"))).map(map -> {
            String countryId = MapUtils.getString(map, "countryId", "");
            String provinceId = MapUtils.getString(map, "provinceId", "");
            String cityId = MapUtils.getString(map, "cityId", "");
            String countryText = "";
            String provinceText = "";
            String cityText = "";
            if (StringUtils.isNotEmpty(countryId)) {
              countryText = MapUtils.getString(locationDict.getT1(), countryId, "");
            }
            map.put("countryText", countryText);
            if (StringUtils.isNotEmpty(provinceId)) {
              provinceText = MapUtils.getString(locationDict.getT2(), provinceId, "");
            }
            map.put("provinceText", provinceText);
            if (StringUtils.isNotEmpty(cityId)) {
              cityText = MapUtils.getString(locationDict.getT3(), cityId, "");
            }
            map.put("cityText", cityText);
            return map;
          }).collect(Collectors.toList());
    }

    return metricLocations;
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricFlowlogService#queryMetricLocationHistograms(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricLocationHistograms(MetricQueryVO queryVO,
      String sortProperty, String sortDirection, String countryId, String provinceId,
      String cityId) {
    MetricFlowLogQueryVO metricFlowLogQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, metricFlowLogQueryVO);

    List<Map<String, Object>> combinationConditions = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isAllBlank(countryId, provinceId, cityId)) {
      List<Map<String, Object>> metricList = metricFlowlogDataRecordDao
          .queryMetricLocations(metricFlowLogQueryVO, sortProperty, sortDirection, true);
      Map<String, String> locations = geoService.queryAllLocationIdNameMapping();
      metricList = metricList.stream()
          .filter(item -> locations.containsKey(MapUtils.getString(item, "countryId")))
          .collect(Collectors.toList());
      if (metricList.size() > queryVO.getCount()) {
        metricList = metricList.subList(0, queryVO.getCount());
      }

      metricList.forEach(metric -> {
        Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
        map.put("country_id", metric.get("countryId"));
        map.put("province_id", metric.get("provinceId"));
        map.put("city_id", metric.get("cityId"));
        combinationConditions.add(map);
      });
    } else {
      Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      map.put("country_id", StringUtils.defaultIfBlank(countryId, null));
      map.put("province_id", StringUtils.defaultIfBlank(provinceId, null));
      map.put("city_id", StringUtils.defaultIfBlank(cityId, null));
      combinationConditions.add(map);
    }

    if (combinationConditions.isEmpty()) {
      return Lists.newArrayListWithCapacity(0);
    }
    // 获取地区字典
    Tuple3<Map<String, String>, Map<String, String>,
        Map<String, String>> locationDict = queryGeoIpDict();

    return metricFlowlogDataRecordDao
        .queryMetricLocationHistograms(metricFlowLogQueryVO, sortProperty, combinationConditions)
        .stream().map(map -> {
          String tempCountryId = MapUtils.getString(map, "countryId", "");
          String tempProvinceId = MapUtils.getString(map, "provinceId", "");
          String tempCityId = MapUtils.getString(map, "cityId", "");
          String countryText = "";
          String provinceText = "";
          String cityText = "";
          if (StringUtils.isNotEmpty(tempCountryId)) {
            countryText = MapUtils.getString(locationDict.getT1(), tempCountryId, "");
          }
          map.put("countryText", countryText);
          if (StringUtils.isNotEmpty(tempProvinceId)) {
            provinceText = MapUtils.getString(locationDict.getT2(), tempProvinceId, "");
          }
          map.put("provinceText", provinceText);
          if (StringUtils.isNotEmpty(tempCityId)) {
            cityText = MapUtils.getString(locationDict.getT3(), tempCityId, "");
          }
          map.put("cityText", cityText);
          return map;
        }).collect(Collectors.toList());
  }

  @Override
  public void exportLocations(MetricQueryVO queryVO, String sortProperty, String sortDirection,
      String fileType, OutputStream out) throws IOException {

    MetricFlowLogQueryVO metricFlowLogQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, metricFlowLogQueryVO);

    Map<String, String> columnNameMap = fields.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    // 标题 存储为名称
    List<String> titles = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.equals(metricFlowLogQueryVO.getColumns(), "*")) {
      titles.addAll(fields.values());
    } else {
      List<String> columnList = CsvUtils.convertCSVToList(metricFlowLogQueryVO.getColumns());
      titles.addAll(columnList.stream().filter(item -> fields.containsKey(item))
          .map(item -> fields.get(item)).collect(Collectors.toList()));
    }

    // 创建临时文件
    File tempDir = (File) servletContext.getAttribute(ServletContext.TEMPDIR);
    File tempFile = Paths.get(tempDir.getAbsolutePath(), metricFlowLogQueryVO.getQueryId())
        .toFile();
    FileUtils.touch(tempFile);

    // 单次查询数量
    int batchSize = Integer.parseInt(HotPropertiesHelper.getProperty("export.flow.log.batch.size"));

    // 创建数据迭代器
    ExportUtils.FetchData fetchData = new ExportUtils.FetchData() {

      private int offset = 0;

      @Override
      public boolean hasNext() {
        return offset % batchSize == 0;
      }

      @Override
      public List<List<String>> next() {
        // 获取数据
        List<Map<String, Object>> metricLocations = metricFlowlogDataRecordDao
            .queryMetricLocations(metricFlowLogQueryVO, sortProperty, sortDirection, false);
        List<List<String>> dataSet = metricLocations.stream()
            .map(metricLocation -> metricLocationMapToStr(metricLocation, titles, columnNameMap))
            .collect(Collectors.toList());

        offset += dataSet.size();
        return dataSet;

      }
    };

    ExportUtils.export(titles, fetchData, tempFile, fileType, out);
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricFlowlogService#queryMetricApplications(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String, int)
   */
  @Override
  public List<Map<String, Object>> queryMetricApplications(MetricQueryVO queryVO,
      String sortProperty, String sortDirection, int type) {
    MetricFlowLogQueryVO metricFlowLogQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, metricFlowLogQueryVO);

    List<String> validIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    Tuple3<List<SaCategoryBO>, List<SaSubCategoryBO>,
        List<SaApplicationBO>> knowledgeRules = saService.queryKnowledgeRules();
    Tuple2<String, String> termField = null;
    switch (type) {
      case FpcCmsConstants.METRIC_TYPE_APPLICATION_CATEGORY:
        termField = Tuples.of("application_category_id", "categoryId");
        validIds.addAll(knowledgeRules.getT1().stream().map(SaCategoryBO::getCategoryId)
            .collect(Collectors.toList()));
        validIds.addAll(saService.queryCustomCategorys().stream()
            .map(SaCustomCategoryBO::getCategoryId).collect(Collectors.toList()));
        break;
      case FpcCmsConstants.METRIC_TYPE_APPLICATION_SUBCATEGORY:
        termField = Tuples.of("application_subcategory_id", "subcategoryId");
        validIds.addAll(knowledgeRules.getT2().stream().map(SaSubCategoryBO::getSubCategoryId)
            .collect(Collectors.toList()));
        validIds.addAll(saService.queryCustomSubCategorys().stream()
            .map(SaCustomSubCategoryBO::getSubCategoryId).collect(Collectors.toList()));
        break;
      case FpcCmsConstants.METRIC_TYPE_APPLICATION_APP:
        termField = Tuples.of("application_id", "applicationId");
        validIds.addAll(knowledgeRules.getT3().stream().map(SaApplicationBO::getApplicationId)
            .collect(Collectors.toList()));
        validIds.addAll(saService.queryCustomApps().stream()
            .map(SaCustomApplicationBO::getApplicationId).collect(Collectors.toList()));
        break;
      default:
        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "不支持的应用统计粒度");
    }

    List<Map<String, Object>> metricApplications = metricFlowlogDataRecordDao
        .queryMetricApplications(metricFlowLogQueryVO, termField, sortProperty, sortDirection,
            false);
    // sa应用 大类 小类 名称字典
    Map<String, Tuple3<String, String, String>> saAppDict = querySaApplicationDict();
    Map<String, Tuple3<String, String, String>> categoryDict = queryCategoryDict();
    Map<String, Tuple3<String, String, String>> subCateGoryDict = querysubCategoryDict();

    String key = termField.getT2();
    return metricApplications.stream()
        .filter(item -> validIds.contains(MapUtils.getString(item, key))).map(map -> {
          switch (type) {
            case FpcCmsConstants.METRIC_TYPE_APPLICATION_CATEGORY:
              String categoryId = MapUtils.getString(map, "categoryId", "");
              String categoryText = "";
              if (categoryDict.get(categoryId) != null) {
                categoryText = categoryDict.get(categoryId).getT2();
              }
              map.put("categoryText", categoryText);
              break;
            case FpcCmsConstants.METRIC_TYPE_APPLICATION_SUBCATEGORY:
              String subcategoryId = MapUtils.getString(map, "subcategoryId", "");
              String subcategoryText = "";
              if (subCateGoryDict.get(subcategoryId) != null) {
                subcategoryText = subCateGoryDict.get(subcategoryId).getT3();
              }
              map.put("subcategoryText", subcategoryText);
              break;
            case FpcCmsConstants.METRIC_TYPE_APPLICATION_APP:
              String applicationId = MapUtils.getString(map, "applicationId", "");
              String applicationText = "";
              if (saAppDict.get(applicationId) != null) {
                applicationText = saAppDict.get(applicationId).getT1();
              }
              map.put("applicationText", applicationText);
              break;
          }

          return map;
        }).collect(Collectors.toList());
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricFlowlogService#queryMetricApplicationHistograms(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, int, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricApplicationHistograms(MetricQueryVO queryVO, int type,
      String sortProperty, String sortDirection, String id) {
    MetricFlowLogQueryVO metricFlowLogQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, metricFlowLogQueryVO);

    List<String> validIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    Tuple3<List<SaCategoryBO>, List<SaSubCategoryBO>,
        List<SaApplicationBO>> knowledgeRules = saService.queryKnowledgeRules();
    Tuple2<String, String> termField = null;
    switch (type) {
      case FpcCmsConstants.METRIC_TYPE_APPLICATION_CATEGORY:
        termField = Tuples.of("application_category_id", "categoryId");
        validIds.addAll(knowledgeRules.getT1().stream().map(SaCategoryBO::getCategoryId)
            .collect(Collectors.toList()));
        validIds.addAll(saService.queryCustomCategorys().stream()
            .map(SaCustomCategoryBO::getCategoryId).collect(Collectors.toList()));
        break;
      case FpcCmsConstants.METRIC_TYPE_APPLICATION_SUBCATEGORY:
        termField = Tuples.of("application_subcategory_id", "subcategoryId");
        validIds.addAll(knowledgeRules.getT2().stream().map(SaSubCategoryBO::getSubCategoryId)
            .collect(Collectors.toList()));
        validIds.addAll(saService.queryCustomSubCategorys().stream()
            .map(SaCustomSubCategoryBO::getSubCategoryId).collect(Collectors.toList()));
        break;
      case FpcCmsConstants.METRIC_TYPE_APPLICATION_APP:
        termField = Tuples.of("application_id", "applicationId");
        validIds.addAll(knowledgeRules.getT3().stream().map(SaApplicationBO::getApplicationId)
            .collect(Collectors.toList()));
        validIds.addAll(saService.queryCustomApps().stream()
            .map(SaCustomApplicationBO::getApplicationId).collect(Collectors.toList()));
        break;
      default:
        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "不支持的应用统计粒度");
    }

    List<String> ids = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isBlank(id)) {
      List<Map<String, Object>> metricList = metricFlowlogDataRecordDao.queryMetricApplications(
          metricFlowLogQueryVO, termField, sortProperty, sortDirection, true);
      String key = termField.getT2();
      metricList = metricList.stream()
          .filter(item -> validIds.contains(MapUtils.getString(item, key)))
          .collect(Collectors.toList());
      if (metricList.size() > queryVO.getCount()) {
        metricList = metricList.subList(0, queryVO.getCount());
      }

      final String fterm = termField.getT2();
      ids = metricList.stream().map(metric -> MapUtils.getString(metric, fterm))
          .collect(Collectors.toList());
    } else {
      ids.add(id);
    }

    if (ids.isEmpty()) {
      return Lists.newArrayListWithCapacity(0);
    }
    // sa应用 大类 小类 名称字典
    Map<String, Tuple3<String, String, String>> saAppDict = querySaApplicationDict();
    Map<String, Tuple3<String, String, String>> categoryDict = queryCategoryDict();
    Map<String, Tuple3<String, String, String>> subCateGoryDict = querysubCategoryDict();

    return metricFlowlogDataRecordDao
        .queryMetricApplicationHistograms(metricFlowLogQueryVO, termField, sortProperty, ids)
        .stream().map(map -> {
          switch (type) {
            case FpcCmsConstants.METRIC_TYPE_APPLICATION_CATEGORY:
              String categoryId = MapUtils.getString(map, "categoryId", "");
              String categoryText = "";
              if (categoryDict.get(categoryId) != null) {
                categoryText = categoryDict.get(categoryId).getT2();
              }
              map.put("categoryText", categoryText);
              break;
            case FpcCmsConstants.METRIC_TYPE_APPLICATION_SUBCATEGORY:
              String subcategoryId = MapUtils.getString(map, "subcategoryId", "");
              String subcategoryText = "";
              if (subCateGoryDict.get(subcategoryId) != null) {
                subcategoryText = subCateGoryDict.get(subcategoryId).getT3();
              }
              map.put("subcategoryText", subcategoryText);
              break;
            case FpcCmsConstants.METRIC_TYPE_APPLICATION_APP:
              String applicationId = MapUtils.getString(map, "applicationId", "");
              String applicationText = "";
              if (saAppDict.get(applicationId) != null) {
                applicationText = saAppDict.get(applicationId).getT1();
              }
              map.put("applicationText", applicationText);
              break;
          }

          return map;
        }).collect(Collectors.toList());
  }

  @Override
  public void exportApplications(MetricQueryVO queryVO, String sortProperty, String sortDirection,
      int type, String fileType, OutputStream out) throws IOException {
    MetricFlowLogQueryVO metricFlowLogQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, metricFlowLogQueryVO);

    Map<String, String> columnNameMap = fields.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    // 标题 存储为名称
    List<String> titles = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.equals(metricFlowLogQueryVO.getColumns(), "*")) {
      titles.addAll(fields.values());
    } else {
      List<String> columnList = CsvUtils.convertCSVToList(metricFlowLogQueryVO.getColumns());
      titles.addAll(columnList.stream().filter(item -> fields.containsKey(item))
          .map(item -> fields.get(item)).collect(Collectors.toList()));
    }

    // 创建临时文件
    File tempDir = (File) servletContext.getAttribute(ServletContext.TEMPDIR);
    File tempFile = Paths.get(tempDir.getAbsolutePath(), metricFlowLogQueryVO.getQueryId())
        .toFile();
    FileUtils.touch(tempFile);

    int batchSize = Integer.parseInt(HotPropertiesHelper.getProperty("export.flow.log.batch.size"));

    ExportUtils.FetchData fetchData = new ExportUtils.FetchData() {

      private int offset = 0;

      @Override
      public boolean hasNext() {
        return offset % batchSize == 0;
      }

      @Override
      public List<List<String>> next() {

        List<String> validIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        Tuple3<List<SaCategoryBO>, List<SaSubCategoryBO>,
            List<SaApplicationBO>> knowledgeRules = saService.queryKnowledgeRules();
        Tuple2<String, String> termField = null;
        switch (type) {
          case FpcCmsConstants.METRIC_TYPE_APPLICATION_CATEGORY:
            termField = Tuples.of("application_category_id", "categoryId");
            validIds.addAll(knowledgeRules.getT1().stream().map(SaCategoryBO::getCategoryId)
                .collect(Collectors.toList()));
            validIds.addAll(saService.queryCustomCategorys().stream()
                .map(SaCustomCategoryBO::getCategoryId).collect(Collectors.toList()));
            break;
          case FpcCmsConstants.METRIC_TYPE_APPLICATION_SUBCATEGORY:
            termField = Tuples.of("application_subcategory_id", "subcategoryId");
            validIds.addAll(knowledgeRules.getT2().stream().map(SaSubCategoryBO::getSubCategoryId)
                .collect(Collectors.toList()));
            validIds.addAll(saService.queryCustomSubCategorys().stream()
                .map(SaCustomSubCategoryBO::getSubCategoryId).collect(Collectors.toList()));
            break;
          case FpcCmsConstants.METRIC_TYPE_APPLICATION_APP:
            termField = Tuples.of("application_id", "applicationId");
            validIds.addAll(knowledgeRules.getT3().stream().map(SaApplicationBO::getApplicationId)
                .collect(Collectors.toList()));
            validIds.addAll(saService.queryCustomApps().stream()
                .map(SaCustomApplicationBO::getCategoryId).collect(Collectors.toList()));
            break;
          default:
            throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "不支持的应用统计粒度");
        }
        // 写入内容
        List<Map<String, Object>> metricLocations = metricFlowlogDataRecordDao
            .queryMetricApplications(metricFlowLogQueryVO, termField, sortProperty, sortDirection,
                false);

        String key = termField.getT2();
        List<Map<String, Object>> result = metricLocations.stream()
            .filter(item -> validIds.contains(MapUtils.getString(item, key)))
            .collect(Collectors.toList());

        if (result.size() == 0) {
          offset = -1;
          result = Lists.newArrayListWithCapacity(0);
        }
        List<List<String>> dataSet = result.stream()
            .map(item -> metricLocationMapToStr(item, titles, columnNameMap))
            .collect(Collectors.toList());

        offset += dataSet.size();

        return dataSet;
      }
    };

    ExportUtils.export(titles, fetchData, tempFile, fileType, out);
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricFlowlogService#queryMetricL7Protocols(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricL7Protocols(MetricQueryVO queryVO,
      String sortProperty, String sortDirection) {
    MetricFlowLogQueryVO metricFlowLogQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, metricFlowLogQueryVO);
    Map<String,
        String> protocolDict = saProtocolService.queryProtocols().stream()
            .collect(Collectors.toMap(protocolMap -> MapUtils.getString(protocolMap, "protocolId"),
                protocolMap -> MapUtils.getString(protocolMap, "nameText")));

    return metricFlowlogDataRecordDao
        .queryMetricL7Protocols(metricFlowLogQueryVO, sortProperty, sortDirection, false).stream()
        .map(map -> {
          String tempL7ProtocolId = MapUtils.getString(map, "l7ProtocolId", "");
          String l7ProtocolText = "";
          if (StringUtils.isNotEmpty(MapUtils.getString(protocolDict, tempL7ProtocolId, ""))) {
            l7ProtocolText = MapUtils.getString(protocolDict, tempL7ProtocolId, "");
          }
          map.put("l7ProtocolText", l7ProtocolText);

          return map;
        }).collect(Collectors.toList());
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricFlowlogService#queryMetricL7ProtocolHistograms(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricL7ProtocolHistograms(MetricQueryVO queryVO,
      String sortProperty, String sortDirection, String l7ProtocolId) {
    MetricFlowLogQueryVO metricFlowLogQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, metricFlowLogQueryVO);

    List<String> l7ProtocolIds = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isBlank(l7ProtocolId)) {
      List<Map<String, Object>> metricList = metricFlowlogDataRecordDao
          .queryMetricL7Protocols(metricFlowLogQueryVO, sortProperty, sortDirection, true);
      if (metricList.size() > queryVO.getCount()) {
        metricList = metricList.subList(0, queryVO.getCount());
      }
      l7ProtocolIds = metricList.stream().map(metric -> MapUtils.getString(metric, "l7ProtocolId"))
          .collect(Collectors.toList());
    } else {
      l7ProtocolIds.add(l7ProtocolId);
    }

    if (l7ProtocolIds.isEmpty()) {
      return Lists.newArrayListWithCapacity(0);
    }
    Map<String,
        String> protocolDict = saProtocolService.queryProtocols().stream()
            .collect(Collectors.toMap(protocolMap -> MapUtils.getString(protocolMap, "protocolId"),
                protocolMap -> MapUtils.getString(protocolMap, "nameText")));

    return metricFlowlogDataRecordDao
        .queryMetricL7ProtocolHistograms(metricFlowLogQueryVO, sortProperty, l7ProtocolIds).stream()
        .map(map -> {
          String tempL7ProtocolId = MapUtils.getString(map, "l7ProtocolId", "");
          String l7ProtocolText = "";
          if (StringUtils.isNotEmpty(MapUtils.getString(protocolDict, tempL7ProtocolId, ""))) {
            l7ProtocolText = MapUtils.getString(protocolDict, tempL7ProtocolId, "");
          }
          map.put("l7ProtocolText", l7ProtocolText);

          return map;
        }).collect(Collectors.toList());
  }

  @Override
  public void exportL7Protocols(MetricQueryVO queryVO, String sortProperty, String sortDirection,
      String l7ProtocolId, String fileType, OutputStream out) throws IOException {
    MetricFlowLogQueryVO metricFlowLogQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, metricFlowLogQueryVO);

    Map<String, String> columnNameMap = fields.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    // 标题 存储为名称
    List<String> titles = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.equals(metricFlowLogQueryVO.getColumns(), "*")) {
      titles.addAll(fields.values());
    } else {
      List<String> columnList = CsvUtils.convertCSVToList(metricFlowLogQueryVO.getColumns());
      titles.addAll(columnList.stream().filter(item -> fields.containsKey(item))
          .map(item -> fields.get(item)).collect(Collectors.toList()));
    }

    // 创建临时文件
    File tempDir = (File) servletContext.getAttribute(ServletContext.TEMPDIR);
    File tempFile = Paths.get(tempDir.getAbsolutePath(), metricFlowLogQueryVO.getQueryId())
        .toFile();
    FileUtils.touch(tempFile);

    int batchSize = Integer.parseInt(HotPropertiesHelper.getProperty("export.flow.log.batch.size"));


    ExportUtils.FetchData fetchData = new ExportUtils.FetchData() {

      private int offset = 0;

      @Override
      public boolean hasNext() {
        return offset % batchSize == 0;
      }

      @Override
      public List<List<String>> next() {
        List<Map<String, Object>> result = metricFlowlogDataRecordDao
            .queryMetricL7Protocols(metricFlowLogQueryVO, sortProperty, sortDirection, false);

        if (result.size() == 0) {
          offset = -1;
          result = Lists.newArrayListWithCapacity(0);
        }
        List<List<String>> dataSet = result.stream()
            .map(item -> metricLocationMapToStr(item, titles, columnNameMap))
            .collect(Collectors.toList());

        offset += dataSet.size();
        return dataSet;
      }
    };

    ExportUtils.export(titles, fetchData, tempFile, fileType, out);
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricFlowlogService#queryMetricPorts(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricPorts(MetricQueryVO queryVO, String sortProperty,
      String sortDirection) {
    MetricFlowLogQueryVO metricFlowLogQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, metricFlowLogQueryVO);

    List<Map<String, Object>> metricPorts = metricFlowlogDataRecordDao
        .queryMetricPorts(metricFlowLogQueryVO, sortProperty, sortDirection, false);
    metricPorts.forEach(metricPort -> {
      metricPort.put("ipProtocol", MapUtils.getString(metricPort, "ipProtocol", "ALL"));
    });

    return metricPorts;
  }

  @Override
  public void exportPorts(MetricQueryVO queryVO, String sortProperty, String sortDirection,
      String fileType, OutputStream out) throws IOException {
    MetricFlowLogQueryVO metricFlowLogQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, metricFlowLogQueryVO);

    Map<String, String> columnNameMap = fields.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    // 标题 存储为名称
    List<String> titles = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.equals(metricFlowLogQueryVO.getColumns(), "*")) {
      titles.addAll(fields.values());
    } else {
      List<String> columnList = CsvUtils.convertCSVToList(metricFlowLogQueryVO.getColumns());
      titles.addAll(columnList.stream().filter(item -> fields.containsKey(item))
          .map(item -> fields.get(item)).collect(Collectors.toList()));
    }

    // 创建临时文件
    File tempDir = (File) servletContext.getAttribute(ServletContext.TEMPDIR);
    File tempFile = Paths.get(tempDir.getAbsolutePath(), metricFlowLogQueryVO.getQueryId())
        .toFile();
    FileUtils.touch(tempFile);

    int batchSize = Integer.parseInt(HotPropertiesHelper.getProperty("export.flow.log.batch.size"));

    ExportUtils.FetchData fetchData = new ExportUtils.FetchData() {

      private int offset = 0;

      @Override
      public boolean hasNext() {
        return offset % batchSize == 0;
      }

      @Override
      public List<List<String>> next() {
        List<Map<String, Object>> result = metricFlowlogDataRecordDao
            .queryMetricPorts(metricFlowLogQueryVO, sortProperty, sortDirection, false);
        result.forEach(metricPort -> {
          metricPort.put("ipProtocol", MapUtils.getString(metricPort, "ipProtocol", "ALL"));
        });

        if (result.size() == 0) {
          offset = -1;
          result = Lists.newArrayListWithCapacity(0);
        }

        List<List<String>> dataSet = result.stream()
            .map(item -> metricLocationMapToStr(item, titles, columnNameMap))
            .collect(Collectors.toList());

        offset += dataSet.size();
        return dataSet;
      }
    };

    ExportUtils.export(titles, fetchData, tempFile, fileType, out);
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricFlowlogService#queryMetricPortHistograms(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricPortHistograms(MetricQueryVO queryVO,
      String sortProperty, String sortDirection, String port) {
    MetricFlowLogQueryVO metricFlowLogQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, metricFlowLogQueryVO);

    List<String> ports = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isBlank(port)) {
      List<Map<String, Object>> metricList = metricFlowlogDataRecordDao
          .queryMetricPorts(metricFlowLogQueryVO, sortProperty, sortDirection, true);
      if (metricList.size() > queryVO.getCount()) {
        metricList = metricList.subList(0, queryVO.getCount());
      }
      ports = metricList.stream().map(metric -> MapUtils.getString(metric, "port"))
          .collect(Collectors.toList());
    } else {
      ports.add(port);
    }

    if (ports.isEmpty()) {
      return Lists.newArrayListWithCapacity(0);
    }

    return metricFlowlogDataRecordDao.queryMetricPortHistograms(metricFlowLogQueryVO, sortProperty,
        ports);
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricFlowlogService#queryMetricHostGroups(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricHostGroups(MetricQueryVO queryVO, String sortProperty,
      String sortDirection) {
    MetricFlowLogQueryVO metricFlowLogQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, metricFlowLogQueryVO);

    List<Map<String, Object>> metricHostGroups = metricFlowlogDataRecordDao
        .queryMetricHostGroups(metricFlowLogQueryVO, sortProperty, sortDirection, false);
    Map<String, String> hostGroupDict = hostGroupService.queryHostGroups().stream()
        .collect(Collectors.toMap(HostGroupBO::getId, HostGroupBO::getName));

    if (CollectionUtils.isNotEmpty(metricHostGroups)) {
      List<String> hostGroupIds = hostGroupService.queryHostGroups().stream()
          .map(HostGroupBO::getId).collect(Collectors.toList());
      metricHostGroups = metricHostGroups.stream()
          .filter(item -> hostGroupIds.contains(MapUtils.getString(item, "hostgroupId")))
          .map(map -> {
            String tempHostgroupId = MapUtils.getString(map, "hostgroupId", "");
            String hostgroupText = "";
            if (StringUtils.isNotEmpty(MapUtils.getString(hostGroupDict, tempHostgroupId, ""))) {
              hostgroupText = MapUtils.getString(hostGroupDict, tempHostgroupId, "");
            }
            map.put("hostgroupText", hostgroupText);

            return map;
          }).collect(Collectors.toList());
    }

    return metricHostGroups;
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricFlowlogService#queryMetricHostGroupHistograms(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricHostGroupHistograms(MetricQueryVO queryVO,
      String sortProperty, String sortDirection, String hostgroupId) {
    MetricFlowLogQueryVO metricFlowLogQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, metricFlowLogQueryVO);

    List<String> hostgroupIds = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isBlank(hostgroupId)) {
      List<Map<String, Object>> metricList = metricFlowlogDataRecordDao
          .queryMetricHostGroups(metricFlowLogQueryVO, sortProperty, sortDirection, true);
      List<String> hostGroupIds = hostGroupService.queryHostGroups().stream()
          .map(HostGroupBO::getId).collect(Collectors.toList());
      metricList = metricList.stream()
          .filter(item -> hostGroupIds.contains(MapUtils.getString(item, "hostgroupId")))
          .collect(Collectors.toList());
      if (metricList.size() > queryVO.getCount()) {
        metricList = metricList.subList(0, queryVO.getCount());
      }

      hostgroupIds = metricList.stream().map(metric -> (String) metric.get("hostgroupId"))
          .collect(Collectors.toList());
    } else {
      hostgroupIds.add(hostgroupId);
    }

    if (hostgroupIds.isEmpty()) {
      return Lists.newArrayListWithCapacity(0);
    }
    Map<String, String> hostGroupDict = hostGroupService.queryHostGroups().stream()
        .collect(Collectors.toMap(HostGroupBO::getId, HostGroupBO::getName));

    return metricFlowlogDataRecordDao
        .queryMetricHostGroupHistograms(metricFlowLogQueryVO, sortProperty, hostgroupIds).stream()
        .map(map -> {
          String tempHostgroupId = MapUtils.getString(map, "hostgroupId", "");
          String hostgroupText = "";
          if (StringUtils.isNotEmpty(MapUtils.getString(hostGroupDict, tempHostgroupId, ""))) {
            hostgroupText = MapUtils.getString(hostGroupDict, tempHostgroupId, "");
          }
          map.put("hostgroupText", hostgroupText);

          return map;
        }).collect(Collectors.toList());
  }

  @Override
  public void exportHostGroups(MetricQueryVO queryVO, String sortProperty, String sortDirection,
      String fileType, OutputStream out) throws IOException {
    MetricFlowLogQueryVO metricFlowLogQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, metricFlowLogQueryVO);

    Map<String, String> columnNameMap = fields.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    // 标题 存储为名称
    List<String> titles = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.equals(metricFlowLogQueryVO.getColumns(), "*")) {
      titles.addAll(fields.values());
    } else {
      List<String> columnList = CsvUtils.convertCSVToList(metricFlowLogQueryVO.getColumns());
      titles.addAll(columnList.stream().filter(item -> fields.containsKey(item))
          .map(item -> fields.get(item)).collect(Collectors.toList()));
    }

    // 创建临时文件
    File tempDir = (File) servletContext.getAttribute(ServletContext.TEMPDIR);
    File tempFile = Paths.get(tempDir.getAbsolutePath(), metricFlowLogQueryVO.getQueryId())
        .toFile();
    FileUtils.touch(tempFile);

    int batchSize = Integer.parseInt(HotPropertiesHelper.getProperty("export.flow.log.batch.size"));

    ExportUtils.FetchData fetchData = new ExportUtils.FetchData() {

      private int offset = 0;

      @Override
      public boolean hasNext() {
        return offset % batchSize == 0;
      }

      @Override
      public List<List<String>> next() {

        List<Map<String, Object>> result = metricFlowlogDataRecordDao
            .queryMetricHostGroups(metricFlowLogQueryVO, sortProperty, sortDirection, false);
        if (CollectionUtils.isNotEmpty(result)) {
          List<String> hostGroupIds = hostGroupService.queryHostGroups().stream()
              .map(HostGroupBO::getId).collect(Collectors.toList());
          result = result.stream()
              .filter(item -> hostGroupIds.contains(MapUtils.getString(item, "hostgroupId")))
              .collect(Collectors.toList());
        }

        if (result.size() == 0) {
          offset = -1;
          result = Lists.newArrayListWithCapacity(0);
        }

        List<List<String>> dataSet = result.stream()
            .map(item -> metricLocationMapToStr(item, titles, columnNameMap))
            .collect(Collectors.toList());

        offset += dataSet.size();

        return dataSet;
      }
    };

    ExportUtils.export(titles, fetchData, tempFile, fileType, out);
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricFlowlogService#queryMetricL2Devices(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricL2Devices(MetricQueryVO queryVO, String sortProperty,
      String sortDirection) {
    MetricFlowLogQueryVO metricFlowLogQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, metricFlowLogQueryVO);

    List<Map<String, Object>> metricL2Devices = metricFlowlogDataRecordDao
        .queryMetricL2Devices(metricFlowLogQueryVO, sortProperty, sortDirection, false);
    Map<String,
        String> ethernetTypeDict = dictManager.getBaseDict().getItemMap("flow_log_ethernet_type");
    metricL2Devices.forEach(metricL2Device -> {
      metricL2Device.put("ethernetType", MapUtils.getString(ethernetTypeDict,
          MapUtils.getString(metricL2Device, "ethernetType", "")));
    });

    return metricL2Devices;
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricFlowlogService#queryMetricL2DeviceHistograms(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricL2DeviceHistograms(MetricQueryVO queryVO,
      String sortProperty, String sortDirection, String macAddress) {
    MetricFlowLogQueryVO metricFlowLogQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, metricFlowLogQueryVO);

    List<String> macAddressList = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isBlank(macAddress)) {
      List<Map<String, Object>> metricList = metricFlowlogDataRecordDao
          .queryMetricL2Devices(metricFlowLogQueryVO, sortProperty, sortDirection, true);
      if (metricList.size() > queryVO.getCount()) {
        metricList = metricList.subList(0, queryVO.getCount());
      }
      macAddressList = metricList.stream().map(metric -> (String) metric.get("macAddress"))
          .collect(Collectors.toList());
    } else {
      macAddressList.add(macAddress);
    }

    if (macAddressList.isEmpty()) {
      return Lists.newArrayListWithCapacity(0);
    }

    return metricFlowlogDataRecordDao.queryMetricL2DeviceHistograms(metricFlowLogQueryVO,
        sortProperty, macAddressList);
  }

  @Override
  public void exportL2Devices(MetricQueryVO queryVO, String sortProperty, String sortDirection,
      String fileType, OutputStream out) throws IOException {
    MetricFlowLogQueryVO metricFlowLogQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, metricFlowLogQueryVO);

    Map<String, String> columnNameMap = fields.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    // 标题 存储为名称
    List<String> titles = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.equals(metricFlowLogQueryVO.getColumns(), "*")) {
      titles.addAll(fields.values());
    } else {
      List<String> columnList = CsvUtils.convertCSVToList(metricFlowLogQueryVO.getColumns());
      titles.addAll(columnList.stream().filter(item -> fields.containsKey(item))
          .map(item -> fields.get(item)).collect(Collectors.toList()));
    }

    // 创建临时文件
    File tempDir = (File) servletContext.getAttribute(ServletContext.TEMPDIR);
    File tempFile = Paths.get(tempDir.getAbsolutePath(), metricFlowLogQueryVO.getQueryId())
        .toFile();
    FileUtils.touch(tempFile);

    int batchSize = Integer.parseInt(HotPropertiesHelper.getProperty("export.flow.log.batch.size"));

    ExportUtils.FetchData fetchData = new ExportUtils.FetchData() {

      int offset = 0;

      @Override
      public boolean hasNext() {
        return offset % batchSize == 0;
      }

      @Override
      public List<List<String>> next() {
        List<Map<String, Object>> result = metricFlowlogDataRecordDao
            .queryMetricL2Devices(metricFlowLogQueryVO, sortProperty, sortDirection, false);
        result.forEach(metricL2Device -> {
          metricL2Device.put("ethernetType",
              MapUtils.getString(metricL2Device, "ethernetType", "ALL"));
        });

        if (result.size() == 0) {
          offset = -1;
          result = Lists.newArrayListWithCapacity(0);
        }

        List<List<String>> dataSet = result.stream()
            .map(item -> metricLocationMapToStr(item, titles, columnNameMap))
            .collect(Collectors.toList());

        offset += dataSet.size();

        return dataSet;
      }
    };

    ExportUtils.export(titles, fetchData, tempFile, fileType, out);
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricFlowlogService#queryMetricL3Devices(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricL3Devices(MetricQueryVO queryVO, String sortProperty,
      String sortDirection) {
    MetricFlowLogQueryVO metricFlowLogQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, metricFlowLogQueryVO);

    List<Map<String, Object>> metricL3Devices = metricFlowlogDataRecordDao
        .queryMetricL3Devices(metricFlowLogQueryVO, sortProperty, sortDirection, false);
    metricL3Devices.forEach(metricL3Device -> {
      Inet4Address ipv4Address = (Inet4Address) metricL3Device.get("ipv4Address");
      Inet6Address ipv6Address = (Inet6Address) metricL3Device.get("ipv6Address");

      metricL3Device.put("ipAddress",
          ipv4Address != null ? ipv4Address.getHostAddress() : ipv6Address.getHostAddress());
      metricL3Device.remove("ipv4Address");
      metricL3Device.remove("ipv6Address");
      metricL3Device.put("macAddress", MapUtils.getString(metricL3Device, "macAddress", "ALL"));
    });

    return metricL3Devices;
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricFlowlogService#queryMetricL3DeviceHistograms(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricL3DeviceHistograms(MetricQueryVO queryVO,
      String sortProperty, String sortDirection, String ipAddress, String ipLocality) {
    MetricFlowLogQueryVO metricFlowLogQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, metricFlowLogQueryVO);

    List<Map<String, Object>> combinationConditions = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isAllBlank(ipAddress, ipLocality)) {
      List<Map<String, Object>> metricList = metricFlowlogDataRecordDao
          .queryMetricL3Devices(metricFlowLogQueryVO, sortProperty, sortDirection, true);
      if (metricList.size() > queryVO.getCount()) {
        metricList = metricList.subList(0, queryVO.getCount());
      }
      metricList.forEach(metric -> {
        Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
        Inet4Address ipv4Address = (Inet4Address) metric.get("ipv4Address");
        Inet6Address ipv6Address = (Inet6Address) metric.get("ipv6Address");
        map.put("ip_address",
            ipv4Address != null ? ipv4Address.getHostAddress() : ipv6Address.getHostAddress());
        map.put("ip_locality", metric.get("ipLocality"));
        combinationConditions.add(map);
      });
    } else {
      Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      if (StringUtils.isNotBlank(ipAddress) && !NetworkUtils.isInetAddress(ipAddress)) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "过滤IP不合法");
      }
      map.put("ip_address", ipAddress);
      map.put("ip_locality", ipLocality);

      combinationConditions.add(map);
    }

    if (combinationConditions.isEmpty()) {
      return Lists.newArrayListWithCapacity(0);
    }

    List<Map<String, Object>> metricL3DeviceHistograms = metricFlowlogDataRecordDao
        .queryMetricL3DeviceHistograms(metricFlowLogQueryVO, sortProperty, combinationConditions);

    metricL3DeviceHistograms.forEach(metricL3DeviceHistogram -> {
      Inet4Address ipv4Address = (Inet4Address) metricL3DeviceHistogram.get("ipv4Address");
      Inet6Address ipv6Address = (Inet6Address) metricL3DeviceHistogram.get("ipv6Address");

      metricL3DeviceHistogram.put("ipAddress",
          ipv4Address != null ? ipv4Address.getHostAddress() : ipv6Address.getHostAddress());
      metricL3DeviceHistogram.remove("ipv4Address");
      metricL3DeviceHistogram.remove("ipv6Address");
    });

    return metricL3DeviceHistograms;
  }

  @Override
  public void exportL3Devices(MetricQueryVO queryVO, String sortProperty, String sortDirection,
      String fileType, OutputStream out) throws IOException {
    MetricFlowLogQueryVO metricFlowLogQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, metricFlowLogQueryVO);

    Map<String, String> columnNameMap = fields.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    // 标题 存储为名称
    List<String> titles = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.equals(metricFlowLogQueryVO.getColumns(), "*")) {
      titles.addAll(fields.values());
    } else {
      List<String> columnList = CsvUtils.convertCSVToList(metricFlowLogQueryVO.getColumns());
      titles.addAll(columnList.stream().filter(item -> fields.containsKey(item))
          .map(item -> fields.get(item)).collect(Collectors.toList()));
    }

    // 创建临时文件
    File tempDir = (File) servletContext.getAttribute(ServletContext.TEMPDIR);
    File tempFile = Paths.get(tempDir.getAbsolutePath(), metricFlowLogQueryVO.getQueryId())
        .toFile();
    FileUtils.touch(tempFile);

    int batchSize = Integer.parseInt(HotPropertiesHelper.getProperty("export.flow.log.batch.size"));

    ExportUtils.FetchData fetchData = new ExportUtils.FetchData() {

      private int offset = 0;

      @Override
      public boolean hasNext() {
        return offset % batchSize == 0;
      }

      @Override
      public List<List<String>> next() {
        List<Map<String, Object>> result = metricFlowlogDataRecordDao
            .queryMetricL3Devices(metricFlowLogQueryVO, sortProperty, sortDirection, false);
        result.forEach(metricL3Device -> {
          Inet4Address ipv4Address = (Inet4Address) metricL3Device.get("ipv4Address");
          Inet6Address ipv6Address = (Inet6Address) metricL3Device.get("ipv6Address");

          metricL3Device.put("ipAddress",
              ipv4Address != null ? ipv4Address.getHostAddress() : ipv6Address.getHostAddress());
          metricL3Device.remove("ipv4Address");
          metricL3Device.remove("ipv6Address");
          metricL3Device.put("macAddress", MapUtils.getString(metricL3Device, "macAddress", "ALL"));
        });

        if (result.size() == 0) {
          offset = -1;
          result = Lists.newArrayListWithCapacity(0);
        }

        List<List<String>> dataSet = result.stream()
            .map(item -> metricLocationMapToStr(item, titles, columnNameMap))
            .collect(Collectors.toList());

        offset += dataSet.size();

        return dataSet;
      }
    };

    ExportUtils.export(titles, fetchData, tempFile, fileType, out);
  }


  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricFlowlogService#queryMetricIpConversations(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricIpConversations(MetricQueryVO queryVO,
      String sortProperty, String sortDirection) {
    MetricFlowLogQueryVO metricFlowLogQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, metricFlowLogQueryVO);

    List<Map<String, Object>> metricIpConversations = metricFlowlogDataRecordDao
        .queryMetricIpConversations(metricFlowLogQueryVO, sortProperty, sortDirection, false);

    metricIpConversations.forEach(metricIpConversation -> {
      String ipAAddress = MapUtils.getString(metricIpConversation, "ipAAddress");
      String ipBAddress = MapUtils.getString(metricIpConversation, "ipBAddress");
      if (StringUtils.startsWithIgnoreCase(ipAAddress, CenterConstants.IPV4_TO_IPV6_PREFIX)
          && StringUtils.startsWithIgnoreCase(ipBAddress, CenterConstants.IPV4_TO_IPV6_PREFIX)) {
        metricIpConversation.put("ipAAddress",
            StringUtils.substringAfterLast(ipAAddress, CenterConstants.IPV4_TO_IPV6_PREFIX));
        metricIpConversation.put("ipBAddress",
            StringUtils.substringAfterLast(ipBAddress, CenterConstants.IPV4_TO_IPV6_PREFIX));
      }
    });

    return metricIpConversations;
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricFlowlogService#queryMetricIpConversationHistograms(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricIpConversationHistograms(MetricQueryVO queryVO,
      String sortProperty, String sortDirection, String ipAAddress, String ipBAddress) {
    MetricFlowLogQueryVO metricFlowLogQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, metricFlowLogQueryVO);

    List<Map<String, Object>> combinationConditions = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isAllBlank(ipAAddress, ipBAddress)) {
      List<Map<String, Object>> metricList = metricFlowlogDataRecordDao
          .queryMetricIpConversations(metricFlowLogQueryVO, sortProperty, sortDirection, true);
      if (metricList.size() > queryVO.getCount()) {
        metricList = metricList.subList(0, queryVO.getCount());
      }

      metricList.forEach(metric -> {
        Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
        map.put("ipAAddress", MapUtils.getString(metric, "ipAAddress"));
        map.put("ipBAddress", MapUtils.getString(metric, "ipBAddress"));

        combinationConditions.add(map);
      });
    } else {
      Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      map.put("ipAAddress", ipAAddress);
      map.put("ipBAddress", ipBAddress);

      combinationConditions.add(map);
    }

    if (combinationConditions.isEmpty()) {
      return Lists.newArrayListWithCapacity(0);
    }

    List<Map<String, Object>> ipConversationHistograms = metricFlowlogDataRecordDao
        .queryMetricIpConversationHistograms(metricFlowLogQueryVO, sortProperty,
            combinationConditions);
    ipConversationHistograms.forEach(ipConversationHistogram -> {
      String ipA = MapUtils.getString(ipConversationHistogram, "ipAAddress");
      String ipB = MapUtils.getString(ipConversationHistogram, "ipBAddress");
      if (StringUtils.startsWithIgnoreCase(ipA, CenterConstants.IPV4_TO_IPV6_PREFIX)
          && StringUtils.startsWithIgnoreCase(ipB, CenterConstants.IPV4_TO_IPV6_PREFIX)) {
        ipConversationHistogram.put("ipAAddress",
            StringUtils.substringAfterLast(ipA, CenterConstants.IPV4_TO_IPV6_PREFIX));
        ipConversationHistogram.put("ipBAddress",
            StringUtils.substringAfterLast(ipB, CenterConstants.IPV4_TO_IPV6_PREFIX));
      }
    });

    return ipConversationHistograms;
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricFlowlogService#graphMetricIpConversations(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.Integer, java.lang.Integer, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> graphMetricIpConversations(MetricQueryVO queryVO,
      Integer minEstablishedSessions, Integer minTotalBytes, String sortProperty,
      String sortDirection) {

    MetricFlowLogQueryVO metricFlowLogQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, metricFlowLogQueryVO);

    List<Map<String, Object>> metricIpConversations = metricFlowlogDataRecordDao
        .graphMetricIpConversations(metricFlowLogQueryVO, minEstablishedSessions, minTotalBytes,
            sortProperty, sortDirection);

    metricIpConversations.forEach(ipConversation -> {
      String ipAAddress = MapUtils.getString(ipConversation, "ipAAddress");
      String ipBAddress = MapUtils.getString(ipConversation, "ipBAddress");
      if (StringUtils.startsWithIgnoreCase(ipAAddress, CenterConstants.IPV4_TO_IPV6_PREFIX)
          && StringUtils.startsWithIgnoreCase(ipBAddress, CenterConstants.IPV4_TO_IPV6_PREFIX)) {
        ipConversation.put("ipAAddress",
            StringUtils.substringAfterLast(ipAAddress, CenterConstants.IPV4_TO_IPV6_PREFIX));
        ipConversation.put("ipBAddress",
            StringUtils.substringAfterLast(ipBAddress, CenterConstants.IPV4_TO_IPV6_PREFIX));
      }
    });

    return metricIpConversations;
  }

  @Override
  public void exportIpConversations(MetricQueryVO queryVO, String sortProperty,
      String sortDirection, String fileType, OutputStream out) throws IOException {

    MetricFlowLogQueryVO metricFlowLogQueryVO = new MetricFlowLogQueryVO();
    BeanUtils.copyProperties(queryVO, metricFlowLogQueryVO);

    Map<String, String> columnNameMap = fields.entrySet().stream()
        .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));
    // 标题 存储为名称
    List<String> titles = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.equals(metricFlowLogQueryVO.getColumns(), "*")) {
      titles.addAll(fields.values());
    } else {
      List<String> columnList = CsvUtils.convertCSVToList(metricFlowLogQueryVO.getColumns());
      titles.addAll(columnList.stream().filter(item -> fields.containsKey(item))
          .map(item -> fields.get(item)).collect(Collectors.toList()));
    }

    // 创建临时文件
    File tempDir = (File) servletContext.getAttribute(ServletContext.TEMPDIR);
    File tempFile = Paths.get(tempDir.getAbsolutePath(), metricFlowLogQueryVO.getQueryId())
        .toFile();
    FileUtils.touch(tempFile);

    int batchSize = Integer.parseInt(HotPropertiesHelper.getProperty("export.flow.log.batch.size"));

    ExportUtils.FetchData fetchData = new ExportUtils.FetchData() {

      private int offset = 0;

      @Override
      public boolean hasNext() {
        return offset % batchSize == 0;
      }

      @Override
      public List<List<String>> next() {
        List<Map<String, Object>> result = metricFlowlogDataRecordDao
            .queryMetricIpConversations(metricFlowLogQueryVO, sortProperty, sortDirection, false);
        result.forEach(metricIpConversation -> {
          String ipAAddress = MapUtils.getString(metricIpConversation, "ipAAddress");
          String ipBAddress = MapUtils.getString(metricIpConversation, "ipBAddress");
          if (StringUtils.startsWithIgnoreCase(ipAAddress, CenterConstants.IPV4_TO_IPV6_PREFIX)
              && StringUtils.startsWithIgnoreCase(ipBAddress,
                  CenterConstants.IPV4_TO_IPV6_PREFIX)) {
            metricIpConversation.put("ipAAddress",
                StringUtils.substringAfterLast(ipAAddress, CenterConstants.IPV4_TO_IPV6_PREFIX));
            metricIpConversation.put("ipBAddress",
                StringUtils.substringAfterLast(ipBAddress, CenterConstants.IPV4_TO_IPV6_PREFIX));
          }
        });

        List<List<String>> dataSet = result.stream()
            .map(item -> metricLocationMapToStr(item, titles, columnNameMap))
            .collect(Collectors.toList());

        offset += dataSet.size();

        return dataSet;
      }
    };

    ExportUtils.export(titles, fetchData, tempFile, fileType, out);
  }

  private Map<String, Tuple3<String, String, String>> querySaApplicationDict() {
    Tuple3<List<SaCategoryBO>, List<SaSubCategoryBO>,
        List<SaApplicationBO>> knowledgeRules = saService.queryKnowledgeRules();

    // 大类
    List<SaCustomCategoryBO> customCategorys = saService.queryCustomCategorys();
    Map<String, String> categoryDict = Maps
        .newHashMapWithExpectedSize(knowledgeRules.getT1().size() + customCategorys.size());
    knowledgeRules.getT1()
        .forEach(category -> categoryDict.put(category.getCategoryId(), category.getNameText()));
    customCategorys.forEach(
        category -> categoryDict.put(category.getCategoryId(), category.getName() + "（自定义）"));

    // 小类
    List<SaCustomSubCategoryBO> customSubCategorys = saService.queryCustomSubCategorys();
    Map<String, String> subCategoryDict = Maps
        .newHashMapWithExpectedSize(knowledgeRules.getT2().size() + customSubCategorys.size());
    knowledgeRules.getT2().forEach(subCategory -> subCategoryDict
        .put(subCategory.getSubCategoryId(), subCategory.getNameText()));
    customSubCategorys.forEach(subCategory -> subCategoryDict.put(subCategory.getSubCategoryId(),
        subCategory.getName() + "（自定义）"));

    // 应用
    List<SaCustomApplicationBO> customRules = saService.queryCustomApps();
    Map<String, Tuple3<String, String, String>> applicationDict = Maps
        .newHashMapWithExpectedSize(customRules.size() + knowledgeRules.getT3().size());
    knowledgeRules.getT3().forEach(app -> {
      String categoryName = categoryDict.get(app.getCategoryId());
      String subCategoryName = subCategoryDict.get(app.getSubCategoryId());
      applicationDict.put(app.getApplicationId(),
          Tuples.of(app.getNameText(), StringUtils.isNotBlank(categoryName) ? categoryName : "",
              StringUtils.isNotBlank(subCategoryName) ? subCategoryName : ""));
    });
    customRules.forEach(app -> {
      String categoryName = categoryDict.get(app.getCategoryId());
      String subCategoryName = subCategoryDict.get(app.getSubCategoryId());
      applicationDict.put(app.getApplicationId(),
          Tuples.of(app.getName() + "（自定义）",
              StringUtils.isNotBlank(categoryName) ? categoryName : "",
              StringUtils.isNotBlank(subCategoryName) ? subCategoryName : ""));
    });
    return applicationDict;
  }

  private Map<String, Tuple3<String, String, String>> queryCategoryDict() {
    Tuple3<List<SaCategoryBO>, List<SaSubCategoryBO>,
        List<SaApplicationBO>> knowledgeRules = saService.queryKnowledgeRules();

    // 大类
    List<SaCustomCategoryBO> customCategorys = saService.queryCustomCategorys();
    Map<String, String> categoryDict = Maps
        .newHashMapWithExpectedSize(knowledgeRules.getT1().size() + customCategorys.size());
    knowledgeRules.getT1()
        .forEach(category -> categoryDict.put(category.getCategoryId(), category.getNameText()));
    customCategorys.forEach(
        category -> categoryDict.put(category.getCategoryId(), category.getName() + "（自定义）"));

    // 小类
    List<SaCustomSubCategoryBO> customSubCategorys = saService.queryCustomSubCategorys();
    Map<String, String> subCategoryDict = Maps
        .newHashMapWithExpectedSize(knowledgeRules.getT2().size() + customSubCategorys.size());
    knowledgeRules.getT2().forEach(subCategory -> subCategoryDict
        .put(subCategory.getSubCategoryId(), subCategory.getNameText()));
    customSubCategorys.forEach(subCategory -> subCategoryDict.put(subCategory.getSubCategoryId(),
        subCategory.getName() + "（自定义）"));

    // 应用
    List<SaCustomApplicationBO> customRules = saService.queryCustomApps();
    Map<String, Tuple3<String, String, String>> applicationDict = Maps
        .newHashMapWithExpectedSize(customRules.size() + knowledgeRules.getT3().size());
    knowledgeRules.getT3().forEach(app -> {
      String categoryName = categoryDict.get(app.getCategoryId());
      String subCategoryName = subCategoryDict.get(app.getSubCategoryId());
      applicationDict.put(app.getCategoryId(),
          Tuples.of(app.getNameText(), StringUtils.isNotBlank(categoryName) ? categoryName : "",
              StringUtils.isNotBlank(subCategoryName) ? subCategoryName : ""));
    });
    customRules.forEach(app -> {
      String categoryName = categoryDict.get(app.getCategoryId());
      String subCategoryName = subCategoryDict.get(app.getSubCategoryId());
      applicationDict.put(app.getApplicationId(),
          Tuples.of(app.getName() + "（自定义）",
              StringUtils.isNotBlank(categoryName) ? categoryName : "",
              StringUtils.isNotBlank(subCategoryName) ? subCategoryName : ""));
    });
    return applicationDict;
  }

  private Map<String, Tuple3<String, String, String>> querysubCategoryDict() {
    Tuple3<List<SaCategoryBO>, List<SaSubCategoryBO>,
        List<SaApplicationBO>> knowledgeRules = saService.queryKnowledgeRules();

    // 大类
    List<SaCustomCategoryBO> customCategorys = saService.queryCustomCategorys();
    Map<String, String> categoryDict = Maps
        .newHashMapWithExpectedSize(knowledgeRules.getT1().size() + customCategorys.size());
    knowledgeRules.getT1()
        .forEach(category -> categoryDict.put(category.getCategoryId(), category.getNameText()));
    customCategorys.forEach(
        category -> categoryDict.put(category.getCategoryId(), category.getName() + "（自定义）"));

    // 小类
    List<SaCustomSubCategoryBO> customSubCategorys = saService.queryCustomSubCategorys();
    Map<String, String> subCategoryDict = Maps
        .newHashMapWithExpectedSize(knowledgeRules.getT2().size() + customSubCategorys.size());
    knowledgeRules.getT2().forEach(subCategory -> subCategoryDict
        .put(subCategory.getSubCategoryId(), subCategory.getNameText()));
    customSubCategorys.forEach(subCategory -> subCategoryDict.put(subCategory.getSubCategoryId(),
        subCategory.getName() + "（自定义）"));

    // 应用
    List<SaCustomApplicationBO> customRules = saService.queryCustomApps();
    Map<String, Tuple3<String, String, String>> applicationDict = Maps
        .newHashMapWithExpectedSize(customRules.size() + knowledgeRules.getT3().size());
    knowledgeRules.getT3().forEach(app -> {
      String categoryName = categoryDict.get(app.getCategoryId());
      String subCategoryName = subCategoryDict.get(app.getSubCategoryId());
      applicationDict.put(app.getSubCategoryId(),
          Tuples.of(app.getNameText(), StringUtils.isNotBlank(categoryName) ? categoryName : "",
              StringUtils.isNotBlank(subCategoryName) ? subCategoryName : ""));
    });
    customRules.forEach(app -> {
      String categoryName = categoryDict.get(app.getCategoryId());
      String subCategoryName = subCategoryDict.get(app.getSubCategoryId());
      applicationDict.put(app.getApplicationId(),
          Tuples.of(app.getName() + "（自定义）",
              StringUtils.isNotBlank(categoryName) ? categoryName : "",
              StringUtils.isNotBlank(subCategoryName) ? subCategoryName : ""));
    });
    return applicationDict;
  }

  private Tuple3<Map<String, String>, Map<String, String>, Map<String, String>> queryGeoIpDict() {
    Tuple3<List<GeoCountryBO>, List<GeoProvinceBO>,
        List<GeoCityBO>> geolocations = geoService.queryGeolocations();

    Map<String, String> countryDict = geolocations.getT1().stream()
        .collect(Collectors.toMap(GeoCountryBO::getCountryId, GeoCountryBO::getNameText));
    countryDict.putAll(geoService.queryCustomCountrys().stream()
        .collect(Collectors.toMap(GeoCustomCountryBO::getCountryId, GeoCustomCountryBO::getName)));

    Map<String, String> provinceDict = geolocations.getT2().stream()
        .collect(Collectors.toMap(GeoProvinceBO::getProvinceId, GeoProvinceBO::getNameText));

    Map<String, String> cityDict = geolocations.getT3().stream()
        .collect(Collectors.toMap(GeoCityBO::getCityId, GeoCityBO::getNameText));

    return Tuples.of(countryDict, provinceDict, cityDict);
  }

  private List<String> metricLocationMapToStr(Map<String, Object> metricResult, List<String> titles,
      Map<String, String> columnNameMap) {

    // sa应用 大类 小类 名称字典
    Map<String, Tuple3<String, String, String>> saAppDict = querySaApplicationDict();
    Map<String, Tuple3<String, String, String>> categoryDict = queryCategoryDict();
    Map<String, Tuple3<String, String, String>> subCateGoryDict = querysubCategoryDict();
    // 地址组名称
    Map<String, String> hostGroupDict = hostGroupService.queryHostGroups().stream()
        .collect(Collectors.toMap(HostGroupBO::getId, HostGroupBO::getName));
    Map<String,
        String> protocolDict = saProtocolService.queryProtocols().stream()
            .collect(Collectors.toMap(protocolMap -> MapUtils.getString(protocolMap, "protocolId"),
                protocolMap -> MapUtils.getString(protocolMap, "nameText")));
    // 获取地区字典
    Tuple3<Map<String, String>, Map<String, String>,
        Map<String, String>> locationDict = queryGeoIpDict();
    // eth类型字典
    Map<String,
        String> ethernetTypeDict = dictManager.getBaseDict().getItemMap("flow_log_ethernet_type");
    // ip内外网位置字典
    Map<String, String> ipLocalityDict = dictManager.getBaseDict()
        .getItemMap("flow_log_ip_address_locality");

    Tuple3<String, String, String> app = null;
    if (saAppDict.get(MapUtils.getString(metricResult, "applicationId", "")) != null) {
      app = saAppDict.get(MapUtils.getString(metricResult, "applicationId", ""));
    } else if (categoryDict.get(MapUtils.getString(metricResult, "categoryId", "")) != null) {
      app = categoryDict.get(MapUtils.getString(metricResult, "categoryId", ""));
    } else {
      app = subCateGoryDict.get(MapUtils.getString(metricResult, "subcategoryId", ""));
    }

    String appName = app != null ? app.getT1() : "";
    String appCategoryName = app != null ? app.getT2() : "";
    String appSubCategoryName = app != null ? app.getT3() : "";

    List<String> values = titles.stream().map(title -> {
      String field = columnNameMap.get(title);

      String value = "";
      switch (field) {
        case "totalBytes":
          value = getPrintSize(MapUtils.getString(metricResult, field, ""));
          break;
        case "tcpClientNetworkLatency":
          value = getDelay(MapUtils.getString(metricResult, field, ""));
          break;
        case "tcpClientNetworkLatencyAvg":
          value = getDelay(MapUtils.getString(metricResult, field, ""));
          break;
        case "tcpServerNetworkLatency":
          value = getDelay(MapUtils.getString(metricResult, field, ""));
          break;
        case "tcpServerNetworkLatencyAvg":
          value = getDelay(MapUtils.getString(metricResult, field, ""));
          break;
        case "serverResponseLatency":
          value = getDelay(MapUtils.getString(metricResult, field, ""));
          break;
        case "serverResponseLatencyAvg":
          value = getDelay(MapUtils.getString(metricResult, field, ""));
          break;
        case "countryId":
          value = MapUtils.getString(locationDict.getT1(),
              MapUtils.getString(metricResult, field, ""), "");
          break;
        case "provinceId":
          value = MapUtils.getString(locationDict.getT2(),
              MapUtils.getString(metricResult, field, ""), "");
          break;
        case "cityId":
          value = MapUtils.getString(locationDict.getT3(),
              MapUtils.getString(metricResult, field, ""), "");
          break;
        case "downstreamBytes":
          value = getPrintSize(MapUtils.getString(metricResult, field, ""));
          break;
        case "upstreamBytes":
          value = getPrintSize(MapUtils.getString(metricResult, field, ""));
          break;
        case "totalPayloadBytes":
          value = getPrintSize(MapUtils.getString(metricResult, field, ""));
          break;
        case "downstreamPayloadBytes":
          value = getPrintSize(MapUtils.getString(metricResult, field, ""));
          break;
        case "upstreamPayloadBytes":
          value = getPrintSize(MapUtils.getString(metricResult, field, ""));
          break;
        case "applicationId":
          value = appName;
          break;
        case "categoryId":
          value = appCategoryName;
          break;
        case "subcategoryId":
          value = appSubCategoryName;
          break;
        case "l7ProtocolId":
          value = MapUtils.getString(protocolDict, MapUtils.getString(metricResult, field, ""), "");
          break;
        case "hostgroupId":
          value = MapUtils.getString(hostGroupDict, MapUtils.getString(metricResult, field, ""),
              "");
          break;
        case "enthernetType":
          value = MapUtils.getString(ethernetTypeDict, MapUtils.getString(metricResult, field, ""),
              "");
          break;
        case "ipLocality":
          value = MapUtils.getString(ipLocalityDict, MapUtils.getString(metricResult, field, ""),
              "");
          break;
        default:
          value = MapUtils.getString(metricResult, field, "");
          break;
      }

      return value;
    }).collect(Collectors.toList());

    return values;
  }

  private String getDelay(String delay) {
    double doubleDelay = Double.parseDouble(delay);
    return String.format("%.0f", doubleDelay) + "ms";
  }

  private String getPrintSize(String bytes) {
    long size = Long.parseLong(bytes);
    // 如果字节数少于1000，则直接以B为单位，否则先除于1000，后3位因太少无意义
    if (size < 1000) {
      return String.valueOf(size) + "B";
    }
    // 如果原字节数除于1000之后，少于1000，则可以直接以KB作为单位
    // 因为还没有到达要使用另一个单位的时候
    // 接下去以此类推
    if (size / 1000 < 1000) {
      String KB = null;
      if (size % 1000 > 100) {
        KB = String.valueOf((size / 1000)) + "." + String.valueOf((size % 1000));
      } else if (size % 1000 > 10) {
        KB = String.valueOf((size / 1000)) + ".0" + String.valueOf((size % 1000));
      } else if (size % 1000 > 1) {
        KB = String.valueOf((size / 1000)) + ".00" + String.valueOf((size % 1000));
      } else {
        KB = String.valueOf((size / 1000)) + ".000" + String.valueOf((size % 1000));
      }
      return String.format("%.3f", Double.parseDouble(KB)) + "KB";
    } else {
      size = size / 100;
    }

    if (size / 10000 < 1000) {
      String MB = null;
      if (size % 10000 > 1000) {
        MB = String.valueOf((size / 10000)) + "." + String.valueOf((size % 10000));
      } else if (size % 10000 > 100) {
        MB = String.valueOf((size / 10000)) + ".0" + String.valueOf((size % 10000));
      } else if (size % 10000 > 10) {
        MB = String.valueOf((size / 10000)) + ".00" + String.valueOf((size % 10000));
      } else if (size % 10000 > 1) {
        MB = String.valueOf((size / 10000)) + ".000" + String.valueOf((size % 10000));
      } else {
        MB = String.valueOf(size / 10000);
      }
      return String.format("%.3f", Double.parseDouble(MB)) + "MB";
    } else {
      size = size / 1000;
      String GB = null;
      if (size % 10000 > 1000) {
        GB = String.valueOf((size / 10000)) + "." + String.valueOf((size % 10000));
      } else if (size % 10000 > 100) {
        GB = String.valueOf((size / 10000)) + ".0" + String.valueOf((size % 10000));
      } else if (size % 10000 > 10) {
        GB = String.valueOf((size / 10000)) + ".00" + String.valueOf((size % 10000));
      } else if (size % 10000 > 1) {
        GB = String.valueOf((size / 10000)) + ".000" + String.valueOf((size % 10000));
      } else {
        GB = String.valueOf(size / 10000);
      }
      return String.format("%.3f", Double.parseDouble(GB)) + "GB";
    }
  }
}
