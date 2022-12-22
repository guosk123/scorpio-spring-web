package com.machloop.fpc.cms.center.metric.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.TextUtils;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.appliance.bo.BaselineSettingBO;
import com.machloop.fpc.cms.center.appliance.bo.BaselineValueBO;
import com.machloop.fpc.cms.center.appliance.service.BaselineService;
import com.machloop.fpc.cms.center.metric.dao.MetricDscpDataRecordDao;
import com.machloop.fpc.cms.center.metric.dao.MetricNetworkDataRecordDao;
import com.machloop.fpc.cms.center.metric.data.MetricDscpDataRecordDO;
import com.machloop.fpc.cms.center.metric.data.MetricNetworkDataRecordDO;
import com.machloop.fpc.cms.center.metric.service.MetricInSecondService;
import com.machloop.fpc.cms.center.metric.service.MetricNetworkService;
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
import reactor.util.function.Tuples;

/**
 * @author guosk
 *
 * create at 2021年4月22日, fpc-manager
 */
@Service
public class MetricNetworkServiceImpl implements MetricNetworkService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetricNetworkServiceImpl.class);

  private static final String FLOW_SORT_PROPERTY = "total_bytes";
  private static final String FLOW_SORT_DIRECTION = "desc";

  private static final int SCALE_COUNTS = 4;

  @Autowired
  private SensorNetworkDao sensorNetworkDao;
  @Autowired
  private SensorLogicalSubnetDao logicalSubnetDao;
  @Autowired
  private SensorNetworkGroupDao networkGroupDao;
  @Autowired
  private MetricNetworkDataRecordDao metricNetworkDao;
  @Autowired
  private MetricDscpDataRecordDao dscpDao;

  @Autowired
  private BaselineService baselineService;
  @Autowired
  private GlobalSettingService globalSetting;
  @Autowired
  private MetricInSecondService metricInSecondService;
  @Autowired
  private SensorNetworkPermService sensorNetworkPermService;

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricNetworkService#queryMetricNetworks(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryMetricNetworks(MetricQueryVO queryVO, String sortProperty,
      String sortDirection) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 获取当前用户的网络（组）权限
    SensorNetworkPermBO currentUserNetworkPerms = sensorNetworkPermService
        .queryCurrentUserNetworkPerms();

    // 获取网络和子网的关系
    Map<String,
        String> networkHierarchy = logicalSubnetDao.querySensorLogicalSubnets().stream()
            .collect(Collectors.toMap(SensorLogicalSubnetDO::getId,
                SensorLogicalSubnetDO::getNetworkInSensorIds));

    List<String> networkIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(queryVO.getNetworkId())) {
      networkIds.add(queryVO.getNetworkId());
    } else {
      // 查询系统内所有的网络
      networkIds.addAll(sensorNetworkDao.querySensorNetworks().stream()
          .map(SensorNetworkDO::getNetworkInSensorId).collect(Collectors.toList()));
      networkIds.addAll(networkHierarchy.keySet());

      if (!currentUserNetworkPerms.getServiceUser()) {
        List<String> networkPerms = CsvUtils
            .convertCSVToList(currentUserNetworkPerms.getNetworkIds());
        networkIds = networkIds.stream().filter(networkId -> networkPerms.contains(networkId))
            .collect(Collectors.toList());
      }
    }

    if (networkIds.isEmpty()) {
      return Lists.newArrayListWithCapacity(0);
    }

    List<String> networkGroupIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    // cms网络组id汇总
    networkGroupIds.addAll(networkGroupDao.querySensorNetworkGroups().stream()
        .map(SensorNetworkGroupDO::getId).collect(Collectors.toList()));
    if (!currentUserNetworkPerms.getServiceUser()) {
      List<String> networkGroupPerms = CsvUtils
          .convertCSVToList(currentUserNetworkPerms.getNetworkGroupIds());
      networkGroupIds = networkGroupIds.stream()
          .filter(networkGroupId -> networkGroupPerms.contains(networkGroupId))
          .collect(Collectors.toList());
    }

    // 获取所有网络的metric信息
    List<String> metrics = Lists.newArrayList("ALL");
    queryVO.setNetworkIds(networkIds);
    queryVO.setCount(0);
    List<MetricNetworkDataRecordDO> allMetricNetworks = metricNetworkDao
        .queryMetricNetworks(queryVO, metrics, sortProperty, sortDirection);

    // 获取网络组统计信息
    List<Map<String, Object>> networkGroupMetrics = Lists
        .newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    for (String networkGroupId : networkGroupIds) {
      List<String> networkIdsInGroup = CsvUtils.convertCSVToList(
          networkGroupDao.querySensorNetworkGroup(networkGroupId).getNetworkInSensorIds());
      List<MetricNetworkDataRecordDO> rawGroupMetric = Lists
          .newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
      for (String networkId : networkIdsInGroup) {
        for (MetricNetworkDataRecordDO temp : allMetricNetworks) {
          if (StringUtils.equals(temp.getNetworkId(), networkId)) {
            rawGroupMetric.add(temp);
          }
        }
      }
      Map<String,
          Object> mergedNetworkMetrics = mergeMetrics(networkGroupId, rawGroupMetric, metrics);
      networkGroupMetrics.add(mergedNetworkMetrics);
    }

    // 填充未获取到统计信息的网络
    networkIds.removeAll(allMetricNetworks.stream().map(MetricNetworkDataRecordDO::getNetworkId)
        .collect(Collectors.toList()));
    networkIds.forEach(networkId -> {
      MetricNetworkDataRecordDO metricNetworkDataRecordDO = new MetricNetworkDataRecordDO();
      metricNetworkDataRecordDO.setNetworkId(networkId);
      allMetricNetworks.add(metricNetworkDataRecordDO);
    });

    for (MetricNetworkDataRecordDO tmp : allMetricNetworks) {
      Map<String, Object> metricNetworkMap = metricNetwork2Map(tmp, metrics);
      metricNetworkMap.put("parentId", networkHierarchy.get(tmp.getNetworkId()));
      result.add(metricNetworkMap);
    }

    // 填充未获取到统计信息的网络组
    networkGroupIds.removeAll(networkGroupMetrics.stream().map(e -> e.get("networkGroupId"))
        .collect(Collectors.toList()));
    networkGroupIds.forEach(networkGroupId -> {
      MetricNetworkDataRecordDO metricNetworkDataRecordDO = new MetricNetworkDataRecordDO();
      metricNetworkDataRecordDO.setNetworkGroupId(networkGroupId);
      networkGroupMetrics.add(metricNetwork2Map(metricNetworkDataRecordDO, metrics));
    });

    result.addAll(networkGroupMetrics);

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

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricNetworkService#queryNetworkDashboard(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, javax.servlet.http.HttpServletRequest)
   */
  @Override
  public Map<String, Object> queryNetworkDashboard(MetricQueryVO queryVO,
      HttpServletRequest request) {
    if (StringUtils.equals(queryVO.getRealTime(), Constants.BOOL_YES)) {
      String path = CenterConstants.REST_ENGINE_STATISTICS_NETWORK_DASHBOARD;
      metricInSecondService.asyncCollection(queryVO, MetricInSecondService.METRIC_NETWORK, path,
          request);
      return metricInSecondService.queryNetworkDashboard(queryVO);
    }

    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    // 网络指标汇总
    List<String> summaryMetrics = Lists.newArrayList(CenterConstants.METRIC_NPM_FLOW,
        CenterConstants.METRIC_NPM_FRAME_LENGTH, CenterConstants.METRIC_NPM_IP_PROTOCOL,
        CenterConstants.METRIC_NPM_ETHERNET_TYPE, CenterConstants.METRIC_NPM_PACKET_TYPE,
        CenterConstants.METRIC_NPM_PERFORMANCE);
    MetricNetworkDataRecordDO metricNetwork = metricNetworkDao.queryMetricNetwork(queryVO,
        summaryMetrics);

    result.putAll(metricNetwork2Map(metricNetwork, summaryMetrics));
    result.put("networkId", queryVO.getNetworkId());
    result.put("networkGroupId", queryVO.getNetworkGroupId());

    // 网络指标趋势图
    List<String> histogramsMetrics = Lists.newArrayList(CenterConstants.METRIC_NPM_FRAME_LENGTH,
        CenterConstants.METRIC_NPM_IP_PROTOCOL, CenterConstants.METRIC_NPM_ETHERNET_TYPE,
        CenterConstants.METRIC_NPM_PACKET_TYPE, CenterConstants.METRIC_NPM_FRAGMENT);
    List<Map<String, Object>> histograms = metricNetworkDao
        .queryMetricNetworkHistograms(queryVO, true, histogramsMetrics).stream().map(histogram -> {
          Map<String, Object> metricNetworkMap = metricNetwork2Map(histogram, histogramsMetrics);
          metricNetworkMap.put("timestamp", histogram.getTimestamp());

          return metricNetworkMap;
        }).collect(Collectors.toList());
    result.put("histogram", histograms);

    // DSCP
    Map<String, Object> metricDscps = queryMetricDscps(queryVO, FLOW_SORT_PROPERTY,
        FLOW_SORT_DIRECTION, queryVO.getCount());
    result.put("dscp", metricDscps);

    return result;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metric.service.MetricNetworkService#queryNetworksTotalPayload(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO)
   */
  @Override
  public Map<String, Object> queryNetworksTotalPayload(MetricQueryVO queryVO) {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    List<String> networkIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(queryVO.getNetworkId())) {
      networkIds.add(queryVO.getNetworkId());
    } else {
      // 查询系统内所有主网络
      networkIds.addAll(sensorNetworkDao.querySensorNetworks().stream()
          .map(SensorNetworkDO::getNetworkInSensorId).collect(Collectors.toList()));
    }

    if (networkIds.isEmpty()) {
      return result;
    }

    long totalBytes = 0;
    long upstreamBytes = 0;
    long downstreamBytes = 0;
    for (Tuple2<Tuple2<Date, Date>, Integer> currentQueryTime : splitQueryDate(
        queryVO.getStartTimeDate(), queryVO.getEndTimeDate())) {
      queryVO.setStartTimeDate(currentQueryTime.getT1().getT1());
      queryVO.setEndTimeDate(currentQueryTime.getT1().getT2());
      queryVO.setInterval(currentQueryTime.getT2());
      List<MetricNetworkDataRecordDO> metricNetworks = metricNetworkDao.queryMetricNetworks(queryVO,
          Lists.newArrayList(CenterConstants.METRIC_NPM_FLOW), FLOW_SORT_PROPERTY,
          FLOW_SORT_DIRECTION);
      for (MetricNetworkDataRecordDO metricNetwork : metricNetworks) {
        totalBytes += metricNetwork.getTotalBytes();
        upstreamBytes += metricNetwork.getUpstreamBytes();
        downstreamBytes += metricNetwork.getDownstreamBytes();
      }
    }

    result.put("totalBytes", totalBytes);
    result.put("upstreamBytes", upstreamBytes);
    result.put("downstreamBytes", downstreamBytes);

    return result;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metric.service.MetricNetworkService#queryPayloadStatistics(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, javax.servlet.http.HttpServletRequest)
   */
  @Override
  public List<Map<String, Object>> queryPayloadStatistics(MetricQueryVO queryVO,
      HttpServletRequest request) {
    if (StringUtils.equals(queryVO.getRealTime(), Constants.BOOL_YES)) {
      String path = CenterConstants.REST_ENGINE_STATISTICS_NETWORK_PAYLOAD;
      metricInSecondService.asyncCollection(queryVO, MetricInSecondService.METRIC_PAYLOAD, path,
          request);
      return metricInSecondService.queryPayloadStatistics(queryVO);
    }

    List<String> aggsFields = Lists.newArrayList("total_bytes", "total_packets", "byteps_peak",
        "upstream_bytes", "downstream_bytes", "upstream_packets", "downstream_packets",
        "filter_discard_bytes", "overload_discard_bytes", "deduplication_bytes",
        "concurrent_sessions", "established_sessions", "unique_ip_counts");

    // 过滤符合时间间隔的基线定义
    List<BaselineSettingBO> baselineSettings = Lists
        .newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);

    if (StringUtils.isNotBlank(queryVO.getNetworkId())) {
      baselineSettings = baselineService.querySubdivisionBaselineSettings(
          FpcCmsConstants.SOURCE_TYPE_NETWORK, queryVO.getNetworkId(), null, null, null);
    }

    if (StringUtils.isNotBlank(queryVO.getNetworkGroupId())) {
      baselineSettings = baselineService.querySubdivisionBaselineSettings(
          FpcCmsConstants.SOURCE_TYPE_NETWORK_GROUP, null, queryVO.getNetworkGroupId(), null, null);
    }

    Map<Long,
        Map<String, Object>> tempMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    // 当前查询时间统计
    List<Map<String, Object>> currentMetricHistograms = metricNetworkDao
        .queryMetricNetworkHistogramsWithAggsFields(queryVO, true, aggsFields);
    currentMetricHistograms.forEach(temp -> {
      temp.put("networkId", queryVO.getNetworkId());
      temp.put("networkGroupId", queryVO.getNetworkGroupId());

      Long totalBytes = MapUtils.getLong(temp, "totalBytes");
      Long bytepsPeak = MapUtils.getLong(temp, "bytepsPeak");
      Long upstreamBytes = MapUtils.getLong(temp, "upstreamBytes");
      Long downstreamBytes = MapUtils.getLong(temp, "downstreamBytes");
      Long filterDiscardBytes = MapUtils.getLong(temp, "filterDiscardBytes");
      Long overloadDiscardBytes = MapUtils.getLong(temp, "overloadDiscardBytes");
      Long deduplicationBytes = MapUtils.getLong(temp, "deduplicationBytes");
      // 补点数据只包含timestamp
      if (totalBytes != null) {
        temp.put("bandwidth", totalBytes * Constants.BYTE_BITS / queryVO.getInterval());
        temp.put("bytepsPeak", bytepsPeak);
        temp.put("upstreamBandwidth", upstreamBytes * Constants.BYTE_BITS / queryVO.getInterval());
        temp.put("downstreamBandwidth",
            downstreamBytes * Constants.BYTE_BITS / queryVO.getInterval());
        temp.put("filterDiscardBandwidth",
            filterDiscardBytes * Constants.BYTE_BITS / queryVO.getInterval());
        temp.put("overloadDiscardBandwidth",
            overloadDiscardBytes * Constants.BYTE_BITS / queryVO.getInterval());
        temp.put("deduplicationBandwidth",
            deduplicationBytes * Constants.BYTE_BITS / queryVO.getInterval());
        temp.put("lastWeekSamePeriodBandwidth", 0);
        temp.put("baselineBandwidth", 0);
        temp.put("lastWeekSamePeriodTotalBytes", 0);
        temp.put("baselineTotalBytes", 0);
        temp.put("lastWeekSamePeriodTotalPackets", 0);
        temp.put("baselineTotalPackets", 0);
      }
      OffsetDateTime timestamp = (OffsetDateTime) temp.get("timestamp");
      tempMap.put(Date.from(timestamp.toInstant()).getTime(), temp);
    });

    // 上周同期
    MetricQueryVO lastWeekSamePeriodQueryVO = new MetricQueryVO();
    lastWeekSamePeriodQueryVO.setInterval(queryVO.getInterval());
    lastWeekSamePeriodQueryVO.setNetworkId(queryVO.getNetworkId());
    lastWeekSamePeriodQueryVO.setNetworkIds(queryVO.getNetworkIds());
    Tuple2<Date, Date> lastWeekSamePeriod = getLastWeekSameTime(queryVO.getStartTimeDate(),
        queryVO.getEndTimeDate());
    lastWeekSamePeriodQueryVO.setStartTimeDate(lastWeekSamePeriod.getT1());
    lastWeekSamePeriodQueryVO.setEndTimeDate(lastWeekSamePeriod.getT2());
    List<Map<String, Object>> lastWeekSamePeriodMetricHistograms = metricNetworkDao
        .queryMetricNetworkHistogramsWithAggsFields(lastWeekSamePeriodQueryVO, true,
            Lists.newArrayList("total_bytes", "total_packets"));
    lastWeekSamePeriodMetricHistograms.forEach(histogram -> {
      OffsetDateTime offsetTime = (OffsetDateTime) histogram.get("timestamp");
      long timestamp = DateUtils
          .afterDayDate(Date.from(offsetTime.toInstant()), Constants.ONE_WEEK_DAYS).getTime();
      long totalBytes = MapUtils.getLong(histogram, "totalBytes", 0L);
      long totalPackets = MapUtils.getLong(histogram, "totalPackets", 0L);
      if (tempMap.containsKey(timestamp)) {
        Map<String, Object> temp = tempMap.get(timestamp);
        temp.put("lastWeekSamePeriodBandwidth",
            totalBytes * Constants.BYTE_BITS / lastWeekSamePeriodQueryVO.getInterval());
        temp.put("lastWeekSamePeriodTotalBytes", totalBytes);
        temp.put("lastWeekSamePeriodTotalPackets", totalPackets);
      }
    });

    // 过滤符合时间间隔的基线定义
    if (CollectionUtils.isEmpty(baselineSettings)) {
      List<Map<String, Object>> result = Lists.newArrayList(tempMap.values());
      sortResultByTimestamp(result);
      return result;
    }
    List<String> windowingModels = baselineService
        .queryWindowingModelByInterval(queryVO.getInterval());
    BaselineSettingBO bandwidthBaseline = null;
    BaselineSettingBO flowBaseline = null;
    BaselineSettingBO packetBaseline = null;
    for (BaselineSettingBO baselineSetting : baselineSettings) {
      if (windowingModels.contains(baselineSetting.getWindowingModel())) {
        String category = baselineSetting.getCategory();
        switch (category) {
          case FpcCmsConstants.BASELINE_CATEGORY_BANDWIDTH:
            bandwidthBaseline = baselineSetting;
            break;
          case FpcCmsConstants.BASELINE_CATEGORY_FLOW:
            flowBaseline = baselineSetting;
            break;
          case FpcCmsConstants.BASELINE_CATEGORY_PACKET:
            packetBaseline = baselineSetting;
            break;
        }
      }
    }
    LOGGER.debug(
        "bandwidthBaseline settings: {}, flowBaseline settings: {}, packetBaseline settings: {}",
        bandwidthBaseline, flowBaseline, packetBaseline);

    // 带宽基线
    if (bandwidthBaseline != null) {
      List<BaselineValueBO> bandwidthBaselines = baselineService.queryBaselineValue(
          FpcCmsConstants.BASELINE_SETTING_SOURCE_NPM, bandwidthBaseline.getId(),
          queryVO.getStartTimeDate(), queryVO.getEndTimeDate());
      bandwidthBaselines.forEach(baselineValue -> {
        long calculateTime = baselineValue.getCalculateTime().getTime();
        if (tempMap.containsKey(calculateTime)) {
          Map<String, Object> temp = tempMap.get(calculateTime);
          temp.put("baselineBandwidth", baselineValue.getValue() * Constants.BYTE_BITS);
        }
      });
    }

    // 流量基线
    if (flowBaseline != null) {
      List<BaselineValueBO> flowBaselines = baselineService.queryBaselineValue(
          FpcCmsConstants.BASELINE_SETTING_SOURCE_NPM, flowBaseline.getId(),
          queryVO.getStartTimeDate(), queryVO.getEndTimeDate());
      flowBaselines.forEach(baselineValue -> {
        long calculateTime = baselineValue.getCalculateTime().getTime();
        if (tempMap.containsKey(calculateTime)) {
          Map<String, Object> temp = tempMap.get(calculateTime);
          temp.put("baselineTotalBytes", baselineValue.getValue());
        }
      });
    }

    // 数据包基线
    if (packetBaseline != null) {
      List<BaselineValueBO> packetBaselines = baselineService.queryBaselineValue(
          FpcCmsConstants.BASELINE_SETTING_SOURCE_NPM, packetBaseline.getId(),
          queryVO.getStartTimeDate(), queryVO.getEndTimeDate());
      packetBaselines.forEach(baselineValue -> {
        long calculateTime = baselineValue.getCalculateTime().getTime();
        if (tempMap.containsKey(calculateTime)) {
          Map<String, Object> temp = tempMap.get(calculateTime);
          temp.put("baselineTotalPackets", baselineValue.getValue());
        }
      });
    }

    List<Map<String, Object>> result = Lists.newArrayList(tempMap.values());
    sortResultByTimestamp(result);
    return result;
  }

  /**
   * 
   * @see com.machloop.fpc.cms.center.metric.service.MetricNetworkService#queryPerformanceStatistics(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, javax.servlet.http.HttpServletRequest)
   */
  @Override
  public List<Map<String, Object>> queryPerformanceStatistics(MetricQueryVO queryVO,
      HttpServletRequest request) {
    if (StringUtils.equals(queryVO.getRealTime(), Constants.BOOL_YES)) {
      String path = CenterConstants.REST_ENGINE_STATISTICS_NETWORK_PERFORMANCE;
      metricInSecondService.asyncCollection(queryVO, MetricInSecondService.METRIC_PERFORMANCE, path,
          request);
      return metricInSecondService.queryPerformanceStatistics(queryVO);
    }

    // 过滤符合时间间隔的基线定义
    List<BaselineSettingBO> baselineSettings = Lists
        .newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(queryVO.getNetworkId())) {
      baselineSettings = baselineService.querySubdivisionBaselineSettings(
          FpcCmsConstants.SOURCE_TYPE_NETWORK, queryVO.getNetworkId(), null, null,
          FpcCmsConstants.BASELINE_CATEGORY_RESPONSELATENCY);
    }
    if (StringUtils.isNotBlank(queryVO.getNetworkGroupId())) {
      baselineSettings = baselineService.querySubdivisionBaselineSettings(
          FpcCmsConstants.SOURCE_TYPE_NETWORK_GROUP, null, queryVO.getNetworkGroupId(), null,
          FpcCmsConstants.BASELINE_CATEGORY_RESPONSELATENCY);
    }

    List<String> metrics = Lists.newArrayList(CenterConstants.METRIC_NPM_SESSION,
        CenterConstants.METRIC_NPM_PERFORMANCE);

    Map<Long,
        Map<String, Object>> tempMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    // 当前统计信息
    List<MetricNetworkDataRecordDO> currentMetricHistograms = metricNetworkDao
        .queryMetricNetworkHistograms(queryVO, true, metrics);
    currentMetricHistograms.forEach(current -> {
      Map<String, Object> metricNetworkMap = metricNetwork2Map(current, metrics);
      metricNetworkMap.put("networkId", queryVO.getNetworkId());
      metricNetworkMap.put("networkGroupId", queryVO.getNetworkGroupId());
      metricNetworkMap.put("timestamp", current.getTimestamp());
      metricNetworkMap.put("lastWeekSamePeriodServerResponseLatencyAvg", 0);
      metricNetworkMap.put("lastWeekSamePeriodServerResponseLatencyAvgInsideService", 0);
      metricNetworkMap.put("lastWeekSamePeriodServerResponseLatencyAvgOutsideService", 0);
      metricNetworkMap.put("baselineServerResponseLatencyAvg", 0);

      tempMap.put(current.getTimestamp().getTime(), metricNetworkMap);
    });

    // 上周同期
    MetricQueryVO lastWeekSamePeriodQueryVO = new MetricQueryVO();
    lastWeekSamePeriodQueryVO.setInterval(queryVO.getInterval());
    lastWeekSamePeriodQueryVO.setNetworkId(queryVO.getNetworkId());
    lastWeekSamePeriodQueryVO.setNetworkIds(queryVO.getNetworkIds());
    Tuple2<Date, Date> lastWeekSamePeriod = getLastWeekSameTime(queryVO.getStartTimeDate(),
        queryVO.getEndTimeDate());
    lastWeekSamePeriodQueryVO.setStartTimeDate(lastWeekSamePeriod.getT1());
    lastWeekSamePeriodQueryVO.setEndTimeDate(lastWeekSamePeriod.getT2());
    List<Map<String, Object>> lastWeekSamePeriodMetricHistograms = metricNetworkDao
        .queryMetricNetworkHistogramsWithAggsFields(lastWeekSamePeriodQueryVO, true,
            Lists.newArrayList("server_response_latency_avg",
                "server_response_latency_avg_inside_service",
                "server_response_latency_avg_outside_service"));
    lastWeekSamePeriodMetricHistograms.forEach(histogram -> {
      OffsetDateTime offsetTime = (OffsetDateTime) histogram.get("timestamp");
      long timestamp = DateUtils
          .afterDayDate(Date.from(offsetTime.toInstant()), Constants.ONE_WEEK_DAYS).getTime();
      long serverResponseLatencyAvg = MapUtils.getLong(histogram, "serverResponseLatencyAvg", 0L);
      long serverResponseLatencyAvgInsideService = MapUtils.getLong(histogram,
          "serverResponseLatencyAvgInsideService", 0L);
      long serverResponseLatencyAvgOutsideService = MapUtils.getLong(histogram,
          "serverResponseLatencyAvgOutsideService", 0L);
      if (tempMap.containsKey(timestamp)) {
        Map<String, Object> temp = tempMap.get(timestamp);
        temp.put("lastWeekSamePeriodServerResponseLatencyAvg", serverResponseLatencyAvg);
        temp.put("lastWeekSamePeriodServerResponseLatencyAvgInsideService",
            serverResponseLatencyAvgInsideService);
        temp.put("lastWeekSamePeriodServerResponseLatencyAvgOutsideService",
            serverResponseLatencyAvgOutsideService);
      }
    });

    if (CollectionUtils.isEmpty(baselineSettings)) {
      List<Map<String, Object>> result = Lists.newArrayList(tempMap.values());
      sortResultByTimestamp(result);
      return result;
    }
    List<String> windowingModels = baselineService
        .queryWindowingModelByInterval(queryVO.getInterval());
    BaselineSettingBO responseLatencyBaseline = null;
    for (BaselineSettingBO baselineSetting : baselineSettings) {
      if (windowingModels.contains(baselineSetting.getWindowingModel())) {
        responseLatencyBaseline = baselineSetting;
        break;
      }
    }
    LOGGER.debug("responseLatencyBaseline settings: {}", responseLatencyBaseline);

    if (responseLatencyBaseline != null) {
      // 服务器响应基线
      List<BaselineValueBO> packetBaselines = baselineService.queryBaselineValue(
          FpcCmsConstants.BASELINE_SETTING_SOURCE_NPM, responseLatencyBaseline.getId(),
          queryVO.getStartTimeDate(), queryVO.getEndTimeDate());
      packetBaselines.forEach(baselineValue -> {
        long calculateTime = baselineValue.getCalculateTime().getTime();
        if (tempMap.containsKey(calculateTime)) {
          Map<String, Object> temp = tempMap.get(calculateTime);
          temp.put("baselineServerResponseLatencyAvg", baselineValue.getValue());
        }
      });
    }

    List<Map<String, Object>> result = Lists.newArrayList(tempMap.values());
    sortResultByTimestamp(result);
    return result;
  }

  /**
   * TCP
   * @see com.machloop.fpc.cms.center.metric.service.MetricNetworkService#queryTcpStatistics(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, javax.servlet.http.HttpServletRequest)
   */
  @Override
  public List<Map<String, Object>> queryTcpStatistics(MetricQueryVO queryVO,
      HttpServletRequest request) {
    if (StringUtils.equals(queryVO.getRealTime(), Constants.BOOL_YES)) {
      String path = CenterConstants.REST_ENGINE_STATISTICS_NETWORK_TCP;
      metricInSecondService.asyncCollection(queryVO, MetricInSecondService.METRIC_TCP, path,
          request);
      return metricInSecondService.queryTcpStatistics(queryVO);
    }

    List<String> aggsFields = Lists.newArrayList("tcp_established_success_counts",
        "tcp_established_fail_counts", "tcp_client_syn_packets", "tcp_server_syn_packets",
        "tcp_client_retransmission_rate", "tcp_server_retransmission_rate",
        "tcp_client_zero_window_packets", "tcp_server_zero_window_packets",
        "tcp_established_success_counts_inside_service",
        "tcp_established_fail_counts_inside_service", "tcp_client_syn_packets_inside_service",
        "tcp_server_syn_packets_inside_service", "tcp_client_zero_window_packets_inside_service",
        "tcp_server_zero_window_packets_inside_service",
        "tcp_client_retransmission_rate_inside_service",
        "tcp_server_retransmission_rate_inside_service",
        "tcp_established_success_counts_outside_service",
        "tcp_established_fail_counts_outside_service", "tcp_client_syn_packets_outside_service",
        "tcp_server_syn_packets_outside_service", "tcp_client_zero_window_packets_outside_service",
        "tcp_server_zero_window_packets_outside_service",
        "tcp_client_retransmission_rate_outside_service",
        "tcp_server_retransmission_rate_outside_service");

    List<Map<String, Object>> result = metricNetworkDao
        .queryMetricNetworkHistogramsWithAggsFields(queryVO, true, aggsFields);
    result.forEach(item -> {
      item.put("networkId", queryVO.getNetworkId());
      item.put("networkGroupId", queryVO.getNetworkGroupId());
    });

    return result;
  }

  /**
   * DSCP占比及趋势图
   * @param queryVO
   * @param sortProperty
   * @param sortDirection
   * @param count
   * @return
   */
  private Map<String, Object> queryMetricDscps(MetricQueryVO queryVO, String sortProperty,
      String sortDirection, int count) {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    List<MetricDscpDataRecordDO> metricDscps = dscpDao.queryMetricDscps(queryVO, sortProperty,
        sortDirection);
    if (metricDscps.size() > count) {
      metricDscps = metricDscps.subList(0, count);
    }
    result.put("volumn", metricDscps);

    List<String> dscpTypes = metricDscps.stream().map(metricDscp -> metricDscp.getType())
        .collect(Collectors.toList());

    List<Map<String, Object>> metricDscpHistograms = dscpDao.queryMetricDscpHistograms(queryVO,
        sortProperty, dscpTypes);
    result.put("histogram", metricDscpHistograms);

    return result;
  }

  private static Tuple2<Date, Date> getLastWeekSameTime(Date startTime, Date endTime) {
    return Tuples.of(DateUtils.beforeDayDate(startTime, Constants.ONE_WEEK_DAYS),
        DateUtils.beforeDayDate(endTime, Constants.ONE_WEEK_DAYS));
  }

  private void sortResultByTimestamp(List<Map<String, Object>> result) {
    result.sort(new Comparator<Map<String, Object>>() {

      @Override
      public int compare(Map<String, Object> o1, Map<String, Object> o2) {
        if (o1.get("timestamp") instanceof Date) {
          Date o1Time = (Date) o1.get("timestamp");
          Date o2Time = (Date) o2.get("timestamp");
          return o1Time.compareTo(o2Time);
        } else if (o1.get("timestamp") instanceof OffsetDateTime) {
          OffsetDateTime o1Time = (OffsetDateTime) o1.get("timestamp");
          OffsetDateTime o2Time = (OffsetDateTime) o2.get("timestamp");
          return o1Time.compareTo(o2Time);
        }
        return 0;
      }

    });
  }

  private Map<String, Object> mergeMetrics(String networkGroupId,
      List<MetricNetworkDataRecordDO> dataRecord, List<String> metrics) {
    Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    item.put("networkGroupId", networkGroupId);

    if (metrics.contains(CenterConstants.METRIC_NPM_FLOW) || metrics.contains("ALL")) {
      item.put("bytepsPeak",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getBytepsPeak).max());
      item.put("packetpsPeak",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getPacketpsPeak).max());
      item.put("totalBytes",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getTotalBytes).sum());
      item.put("totalPackets",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getTotalPackets).sum());
      item.put("downstreamBytes",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getDownstreamBytes).sum());
      item.put("downstreamPackets",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getDownstreamPackets).sum());
      item.put("upstreamBytes",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getUpstreamBytes).sum());
      item.put("upstreamPackets",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getUpstreamPackets).sum());
      item.put("filterDiscardBytes",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getFilterDiscardBytes).sum());
      item.put("filterDiscardPackets",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getFilterDiscardPackets).sum());
      item.put("overloadDiscardBytes",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getOverloadDiscardBytes).sum());
      item.put("overloadDiscardPackets", dataRecord.stream()
          .mapToLong(MetricNetworkDataRecordDO::getOverloadDiscardPackets).sum());
      item.put("deduplicationBytes",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getDeduplicationBytes).sum());
      item.put("deduplicationPackets",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getDeduplicationPackets).sum());
    }

    if (metrics.contains(CenterConstants.METRIC_NPM_IP_PROTOCOL) || metrics.contains("ALL")) {
      item.put("tcpTotalPackets",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getTcpTotalPackets).sum());
      item.put("udpTotalPackets",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getUdpTotalPackets).sum());
      item.put("icmpTotalPackets",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getIcmpTotalPackets).sum());
      item.put("icmp6TotalPackets",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getIcmp6TotalPackets).sum());
      item.put("otherTotalPackets",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getOtherTotalPackets).sum());
    }

    if (metrics.contains(CenterConstants.METRIC_NPM_FRAGMENT) || metrics.contains("ALL")) {
      item.put("fragmentTotalBytes",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getFragmentTotalBytes).sum());
      item.put("fragmentTotalPackets",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getFragmentTotalPackets).sum());
    }

    if (metrics.contains(CenterConstants.METRIC_NPM_TCP) || metrics.contains("ALL")) {
      item.put("tcpSynPackets",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getTcpSynAckPackets).sum());
      item.put("tcpClientSynPackets",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getTcpClientSynPackets).sum());
      item.put("tcpServerSynPackets",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getTcpServerSynPackets).sum());
      item.put("tcpSynAckPackets",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getTcpSynAckPackets).sum());
      item.put("tcpSynRstPackets",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getTcpSynRstPackets).sum());
      item.put("tcpEstablishedFailCounts", dataRecord.stream()
          .mapToLong(MetricNetworkDataRecordDO::getTcpEstablishedFailCounts).sum());
      item.put("tcpEstablishedSuccessCounts", dataRecord.stream()
          .mapToLong(MetricNetworkDataRecordDO::getTcpEstablishedSuccessCounts).sum());
      item.put("tcpEstablishedTimeAvg", dataRecord.stream()
          .mapToLong(MetricNetworkDataRecordDO::getTcpEstablishedTimeAvg).average());
      item.put("tcpZeroWindowPackets",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getTcpZeroWindowPackets).sum());
    }

    if (metrics.contains(CenterConstants.METRIC_NPM_FRAME_LENGTH) || metrics.contains("ALL")) {
      item.put("tinyPackets",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getTinyPackets).sum());
      item.put("smallPackets",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getSmallPackets).sum());
      item.put("mediumPackets",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getMediumPackets).sum());
      item.put("bigPackets",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getBigPackets).sum());
      item.put("largePackets",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getLargePackets).sum());
      item.put("hugePackets",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getHugePackets).sum());
      item.put("jumboPackets",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getJumboPackets).sum());
      item.put("packetLengthAvg",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getPacketLengthAvg).sum());
    }

    if (metrics.contains(CenterConstants.METRIC_NPM_SESSION) || metrics.contains("ALL")) {
      item.put("activeSessions",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getActiveSessions).sum());
      item.put("concurrentSessions",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getConcurrentSessions).sum());
      item.put("concurrentTcpSessions",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getConcurrentTcpSessions).sum());
      item.put("concurrentUdpSessions",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getConcurrentUdpSessions).sum());
      item.put("concurrentArpSessions",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getConcurrentArpSessions).sum());
      item.put("concurrentIcmpSessions", dataRecord.stream()
          .mapToLong(MetricNetworkDataRecordDO::getConcurrentIcmpSessions).sum());
      item.put("establishedSessions",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getEstablishedSessions).sum());
      item.put("destroyedSessions",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getDestroyedSessions).sum());
      item.put("establishedTcpSessions", dataRecord.stream()
          .mapToLong(MetricNetworkDataRecordDO::getEstablishedTcpSessions).sum());
      item.put("establishedUdpSessions", dataRecord.stream()
          .mapToLong(MetricNetworkDataRecordDO::getEstablishedUdpSessions).sum());
      item.put("establishedIcmpSessions", dataRecord.stream()
          .mapToLong(MetricNetworkDataRecordDO::getEstablishedIcmpSessions).sum());
      item.put("establishedOtherSessions", dataRecord.stream()
          .mapToLong(MetricNetworkDataRecordDO::getEstablishedOtherSessions).sum());
      item.put("establishedUpstreamSessions", dataRecord.stream()
          .mapToLong(MetricNetworkDataRecordDO::getEstablishedUpstreamSessions).sum());
      item.put("establishedDownstreamSessions", dataRecord.stream()
          .mapToLong(MetricNetworkDataRecordDO::getEstablishedDownstreamSessions).sum());
    }

    if (metrics.contains(CenterConstants.METRIC_NPM_ETHERNET_TYPE) || metrics.contains("ALL")) {
      item.put("ipv4Frames",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getIpv4Frames).sum());
      item.put("ipv6Frames",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getIpv6Frames).sum());
      item.put("arpFrames",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getArpFrames).sum());
      item.put("ieee8021xFrames",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getIeee8021xFrames).sum());
      item.put("ipxFrames",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getIpxFrames).sum());
      item.put("lacpFrames",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getLacpFrames).sum());
      item.put("mplsFrames",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getMplsFrames).sum());
      item.put("stpFrames",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getStpFrames).sum());
      item.put("otherFrames",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getOtherFrames).sum());
    }

    if (metrics.contains(CenterConstants.METRIC_NPM_PACKET_TYPE) || metrics.contains("ALL")) {
      item.put("unicastBytes",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getUnicastBytes).sum());
      item.put("broadcastBytes",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getBroadcastBytes).sum());
      item.put("multicastBytes",
          dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getMulticastBytes).sum());
    }

    if (metrics.contains(CenterConstants.METRIC_NPM_PERFORMANCE) || metrics.contains("ALL")) {
      item.put("tcpClientNetworkLatency", dataRecord.stream()
          .mapToLong(MetricNetworkDataRecordDO::getTcpClientNetworkLatency).sum());
      item.put("tcpClientNetworkLatencyCounts", dataRecord.stream()
          .mapToLong(MetricNetworkDataRecordDO::getTcpClientNetworkLatencyCounts).sum());
      item.put("tcpClientNetworkLatencyAvg", dataRecord.stream()
          .mapToDouble(MetricNetworkDataRecordDO::getTcpClientNetworkLatencyAvg).average());
      item.put("tcpServerNetworkLatency", dataRecord.stream()
          .mapToLong(MetricNetworkDataRecordDO::getTcpServerNetworkLatency).sum());
      item.put("tcpServerNetworkLatencyCounts", dataRecord.stream()
          .mapToLong(MetricNetworkDataRecordDO::getTcpServerNetworkLatencyCounts).sum());
      item.put("tcpServerNetworkLatencyAvg", dataRecord.stream()
          .mapToDouble(MetricNetworkDataRecordDO::getTcpServerNetworkLatencyAvg).average());
      item.put("serverResponseLatency", dataRecord.stream()
          .mapToDouble(MetricNetworkDataRecordDO::getServerResponseLatency).sum());
      item.put("serverResponseLatencyCounts", dataRecord.stream()
          .mapToDouble(MetricNetworkDataRecordDO::getServerResponseLatencyCounts).sum());
      item.put("serverResponseLatencyAvg", dataRecord.stream()
          .mapToDouble(MetricNetworkDataRecordDO::getServerResponseLatencyAvg).average());
      item.put("serverResponseFastCounts", dataRecord.stream()
          .mapToDouble(MetricNetworkDataRecordDO::getServerResponseFastCounts).sum());
      item.put("serverResponseNormalCounts", dataRecord.stream()
          .mapToDouble(MetricNetworkDataRecordDO::getServerResponseNormalCounts).sum());
      item.put("serverResponseTimeoutCounts", dataRecord.stream()
          .mapToDouble(MetricNetworkDataRecordDO::getServerResponseTimeoutCounts).sum());
      item.put("serverResponseLatencyPeak", dataRecord.stream()
          .mapToDouble(MetricNetworkDataRecordDO::getServerResponseLatencyPeak).max());
      item.put("tcpClientRetransmissionPackets", dataRecord.stream()
          .mapToDouble(MetricNetworkDataRecordDO::getTcpClientRetransmissionPackets).sum());
      item.put("tcpClientPackets",
          dataRecord.stream().mapToDouble(MetricNetworkDataRecordDO::getTcpClientPackets).sum());
      item.put("tcpClientRetransmissionRate", dataRecord.stream()
          .mapToDouble(MetricNetworkDataRecordDO::getTcpClientRetransmissionRate).average());
      item.put("tcpServerRetransmissionPackets", dataRecord.stream()
          .mapToDouble(MetricNetworkDataRecordDO::getTcpServerRetransmissionPackets).sum());
      item.put("tcpServerPackets",
          dataRecord.stream().mapToDouble(MetricNetworkDataRecordDO::getTcpServerPackets).sum());
      item.put("tcpServerRetransmissionRate", dataRecord.stream()
          .mapToDouble(MetricNetworkDataRecordDO::getTcpServerRetransmissionRate).average());
      double tcpRetransmissionRate = 0;
      long tcpPackets = dataRecord.stream()
          .mapToLong(MetricNetworkDataRecordDO::getTcpClientPackets).sum()
          + dataRecord.stream().mapToLong(MetricNetworkDataRecordDO::getTcpClientSynPackets).sum();
      if (tcpPackets > 0) {
        tcpRetransmissionRate = (dataRecord.stream()
            .mapToDouble(MetricNetworkDataRecordDO::getTcpClientRetransmissionPackets).sum()
            + dataRecord.stream()
                .mapToDouble(MetricNetworkDataRecordDO::getTcpServerRetransmissionPackets).sum())
            / (double) tcpPackets;
      }
      item.put("tcpRetransmissionRate", new BigDecimal(String.valueOf(tcpRetransmissionRate))
          .setScale(SCALE_COUNTS, RoundingMode.HALF_UP));
      item.put("tcpClientZeroWindowPackets", dataRecord.stream()
          .mapToDouble(MetricNetworkDataRecordDO::getTcpClientZeroWindowPackets).sum());
      item.put("tcpServerZeroWindowPackets", dataRecord.stream()
          .mapToDouble(MetricNetworkDataRecordDO::getTcpServerZeroWindowPackets).sum());
    }
    if (metrics.contains(CenterConstants.METRIC_NPM_UNIQUE_IP_COUNTS) || metrics.contains("ALL")) {
      item.put("uniqueIpCounts",
          dataRecord.stream().mapToDouble(MetricNetworkDataRecordDO::getUniqueIpCounts).sum());
    }
    return item;
  }

  private Map<String, Object> metricNetwork2Map(MetricNetworkDataRecordDO dataRecord,
      List<String> metrics) {
    Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    item.put("networkId", dataRecord.getNetworkId());

    if (metrics.contains(CenterConstants.METRIC_NPM_FLOW) || metrics.contains("ALL")) {
      item.put("bytepsPeak", dataRecord.getBytepsPeak());
      item.put("packetpsPeak", dataRecord.getPacketpsPeak());
      item.put("totalBytes", dataRecord.getTotalBytes());
      item.put("totalPackets", dataRecord.getTotalPackets());
      item.put("downstreamBytes", dataRecord.getDownstreamBytes());
      item.put("downstreamPackets", dataRecord.getDownstreamPackets());
      item.put("upstreamBytes", dataRecord.getUpstreamBytes());
      item.put("upstreamPackets", dataRecord.getUpstreamPackets());
      item.put("filterDiscardBytes", dataRecord.getFilterDiscardBytes());
      item.put("filterDiscardPackets", dataRecord.getFilterDiscardPackets());
      item.put("overloadDiscardBytes", dataRecord.getOverloadDiscardBytes());
      item.put("overloadDiscardPackets", dataRecord.getOverloadDiscardPackets());
      item.put("deduplicationBytes", dataRecord.getDeduplicationBytes());
      item.put("deduplicationPackets", dataRecord.getDeduplicationPackets());
    }

    if (metrics.contains(CenterConstants.METRIC_NPM_IP_PROTOCOL) || metrics.contains("ALL")) {
      item.put("tcpTotalPackets", dataRecord.getTcpTotalPackets());
      item.put("udpTotalPackets", dataRecord.getUdpTotalPackets());
      item.put("icmpTotalPackets", dataRecord.getIcmpTotalPackets());
      item.put("icmp6TotalPackets", dataRecord.getIcmp6TotalPackets());
      item.put("otherTotalPackets", dataRecord.getOtherTotalPackets());
    }

    if (metrics.contains(CenterConstants.METRIC_NPM_FRAGMENT) || metrics.contains("ALL")) {
      item.put("fragmentTotalBytes", dataRecord.getFragmentTotalBytes());
      item.put("fragmentTotalPackets", dataRecord.getFragmentTotalPackets());
    }

    if (metrics.contains(CenterConstants.METRIC_NPM_TCP) || metrics.contains("ALL")) {
      item.put("tcpSynPackets", dataRecord.getTcpSynPackets());
      item.put("tcpClientSynPackets", dataRecord.getTcpClientSynPackets());
      item.put("tcpServerSynPackets", dataRecord.getTcpServerSynPackets());
      item.put("tcpSynAckPackets", dataRecord.getTcpSynAckPackets());
      item.put("tcpSynRstPackets", dataRecord.getTcpSynRstPackets());
      item.put("tcpEstablishedFailCounts", dataRecord.getTcpEstablishedFailCounts());
      item.put("tcpEstablishedSuccessCounts", dataRecord.getTcpEstablishedSuccessCounts());
      item.put("tcpEstablishedTimeAvg", dataRecord.getTcpEstablishedTimeAvg());
      item.put("tcpZeroWindowPackets", dataRecord.getTcpZeroWindowPackets());

      item.put("tcpEstablishedSuccessCountsInsideService",
          dataRecord.getTcpEstablishedSuccessCountsInsideService());
      item.put("tcpEstablishedFailCountsInsideService",
          dataRecord.getTcpEstablishedFailCountsInsideService());
      item.put("tcpClientSynPacketsInsideService",
          dataRecord.getTcpClientSynPacketsInsideService());
      item.put("tcpServerSynPacketsInsideService",
          dataRecord.getTcpServerSynPacketsInsideService());
      item.put("tcpClientRetransmissionPacketsInsideService",
          dataRecord.getTcpClientRetransmissionPacketsInsideService());
      item.put("tcpServerRetransmissionPacketsInsideService",
          dataRecord.getTcpServerRetransmissionPacketsInsideService());
      item.put("tcpClientPacketsInsideService", dataRecord.getTcpClientPacketsInsideService());
      item.put("tcpServerPacketsInsideService", dataRecord.getTcpServerPacketsInsideService());
      item.put("tcpClientZeroWindowPacketsInsideService",
          dataRecord.getTcpClientZeroWindowPacketsInsideService());
      item.put("tcpServerZeroWindowPacketsInsideService",
          dataRecord.getTcpServerZeroWindowPacketsInsideService());

      item.put("tcpEstablishedSuccessCountsOutsideService",
          dataRecord.getTcpEstablishedSuccessCountsOutsideService());
      item.put("tcpEstablishedFailCountsOutsideService",
          dataRecord.getTcpEstablishedFailCountsOutsideService());
      item.put("tcpClientSynPacketsOutsideService",
          dataRecord.getTcpClientSynPacketsOutsideService());
      item.put("tcpServerSynPacketsOutsideService",
          dataRecord.getTcpServerSynPacketsOutsideService());
      item.put("tcpClientRetransmissionPacketsOutsideService",
          dataRecord.getTcpClientRetransmissionPacketsOutsideService());
      item.put("tcpClientPacketsOutsideService", dataRecord.getTcpClientPacketsOutsideService());
      item.put("tcpServerRetransmissionPacketsOutsideService",
          dataRecord.getTcpServerRetransmissionPacketsOutsideService());
      item.put("tcpServerPacketsOutsideService", dataRecord.getTcpServerPacketsOutsideService());
      item.put("tcpClientZeroWindowPacketsOutsideService",
          dataRecord.getTcpClientZeroWindowPacketsOutsideService());
      item.put("tcpServerZeroWindowPacketsOutsideService",
          dataRecord.getTcpServerZeroWindowPacketsOutsideService());


      item.put("tcpClientRetransmissionRateInsideService",
          dataRecord.getTcpClientRetransmissionRateInsideService());
      item.put("tcpServerRetransmissionRateInsideService",
          dataRecord.getTcpServerRetransmissionRateInsideService());
      item.put("tcpClientRetransmissionRateOutsideService",
          dataRecord.getTcpClientRetransmissionRateOutsideService());
      item.put("tcpServerRetransmissionRateOutsideService",
          dataRecord.getTcpServerRetransmissionRateOutsideService());
    }

    if (metrics.contains(CenterConstants.METRIC_NPM_FRAME_LENGTH) || metrics.contains("ALL")) {
      item.put("tinyPackets", dataRecord.getTinyPackets());
      item.put("smallPackets", dataRecord.getSmallPackets());
      item.put("mediumPackets", dataRecord.getMediumPackets());
      item.put("bigPackets", dataRecord.getBigPackets());
      item.put("largePackets", dataRecord.getLargePackets());
      item.put("hugePackets", dataRecord.getHugePackets());
      item.put("jumboPackets", dataRecord.getJumboPackets());
      item.put("packetLengthAvg", dataRecord.getPacketLengthAvg());
    }

    if (metrics.contains(CenterConstants.METRIC_NPM_SESSION) || metrics.contains("ALL")) {
      item.put("activeSessions", dataRecord.getActiveSessions());
      item.put("concurrentSessions", dataRecord.getConcurrentSessions());
      item.put("concurrentTcpSessions", dataRecord.getConcurrentTcpSessions());
      item.put("concurrentUdpSessions", dataRecord.getConcurrentUdpSessions());
      item.put("concurrentArpSessions", dataRecord.getConcurrentArpSessions());
      item.put("concurrentIcmpSessions", dataRecord.getConcurrentIcmpSessions());
      item.put("establishedSessions", dataRecord.getEstablishedSessions());
      item.put("destroyedSessions", dataRecord.getDestroyedSessions());
      item.put("establishedTcpSessions", dataRecord.getEstablishedTcpSessions());
      item.put("establishedUdpSessions", dataRecord.getEstablishedUdpSessions());
      item.put("establishedIcmpSessions", dataRecord.getEstablishedIcmpSessions());
      item.put("establishedOtherSessions", dataRecord.getEstablishedOtherSessions());
      item.put("establishedUpstreamSessions", dataRecord.getEstablishedUpstreamSessions());
      item.put("establishedDownstreamSessions", dataRecord.getEstablishedDownstreamSessions());
    }

    if (metrics.contains(CenterConstants.METRIC_NPM_ETHERNET_TYPE) || metrics.contains("ALL")) {
      item.put("ipv4Frames", dataRecord.getIpv4Frames());
      item.put("ipv6Frames", dataRecord.getIpv6Frames());
      item.put("arpFrames", dataRecord.getArpFrames());
      item.put("ieee8021xFrames", dataRecord.getIeee8021xFrames());
      item.put("ipxFrames", dataRecord.getIpxFrames());
      item.put("lacpFrames", dataRecord.getLacpFrames());
      item.put("mplsFrames", dataRecord.getMplsFrames());
      item.put("stpFrames", dataRecord.getStpFrames());
      item.put("otherFrames", dataRecord.getOtherFrames());
    }

    if (metrics.contains(CenterConstants.METRIC_NPM_PACKET_TYPE) || metrics.contains("ALL")) {
      item.put("unicastBytes", dataRecord.getUnicastBytes());
      item.put("broadcastBytes", dataRecord.getBroadcastBytes());
      item.put("multicastBytes", dataRecord.getMulticastBytes());
    }

    if (metrics.contains(CenterConstants.METRIC_NPM_PERFORMANCE) || metrics.contains("ALL")) {
      item.put("tcpClientNetworkLatency", dataRecord.getTcpClientNetworkLatency());
      item.put("tcpClientNetworkLatencyCounts", dataRecord.getTcpClientNetworkLatencyCounts());
      item.put("tcpClientNetworkLatencyAvg", dataRecord.getTcpClientNetworkLatencyAvg());
      item.put("tcpServerNetworkLatency", dataRecord.getTcpServerNetworkLatency());
      item.put("tcpServerNetworkLatencyCounts", dataRecord.getTcpServerNetworkLatencyCounts());
      item.put("tcpServerNetworkLatencyAvg", dataRecord.getTcpServerNetworkLatencyAvg());
      item.put("serverResponseLatency", dataRecord.getServerResponseLatency());
      item.put("serverResponseLatencyCounts", dataRecord.getServerResponseLatencyCounts());
      item.put("serverResponseLatencyAvg", dataRecord.getServerResponseLatencyAvg());
      item.put("serverResponseFastCounts", dataRecord.getServerResponseFastCounts());
      item.put("serverResponseNormalCounts", dataRecord.getServerResponseNormalCounts());
      item.put("serverResponseTimeoutCounts", dataRecord.getServerResponseTimeoutCounts());
      item.put("serverResponseLatencyPeak", dataRecord.getServerResponseLatencyPeak());
      item.put("tcpClientRetransmissionPackets", dataRecord.getTcpClientRetransmissionPackets());
      item.put("tcpClientPackets", dataRecord.getTcpClientPackets());
      item.put("tcpClientRetransmissionRate", dataRecord.getTcpClientRetransmissionRate());
      item.put("tcpServerRetransmissionPackets", dataRecord.getTcpServerRetransmissionPackets());
      item.put("tcpServerPackets", dataRecord.getTcpServerPackets());
      item.put("tcpServerRetransmissionRate", dataRecord.getTcpServerRetransmissionRate());
      double tcpRetransmissionRate = 0;
      long tcpPackets = dataRecord.getTcpClientPackets() + dataRecord.getTcpServerPackets();
      if (tcpPackets > 0) {
        tcpRetransmissionRate = (dataRecord.getTcpClientRetransmissionPackets()
            + dataRecord.getTcpServerRetransmissionPackets()) / (double) tcpPackets;
      }
      item.put("tcpRetransmissionRate", new BigDecimal(String.valueOf(tcpRetransmissionRate))
          .setScale(SCALE_COUNTS, RoundingMode.HALF_UP));
      item.put("tcpClientZeroWindowPackets", dataRecord.getTcpClientZeroWindowPackets());
      item.put("tcpServerZeroWindowPackets", dataRecord.getTcpServerZeroWindowPackets());

      // Performance和TCP新增内外网服务字段
      item.put("serverResponseLatencyInsideService",
          dataRecord.getServerResponseLatencyInsideService());
      item.put("serverResponseLatencyCountsInsideService",
          dataRecord.getServerResponseLatencyCountsInsideService());
      item.put("serverResponseLatencyPeakInsideService",
          dataRecord.getServerResponseLatencyPeakInsideService());
      item.put("serverResponseFastCountsInsideService",
          dataRecord.getServerResponseFastCountsInsideService());
      item.put("serverResponseNormalCountsInsideService",
          dataRecord.getServerResponseNormalCountsInsideService());
      item.put("serverResponseTimeoutCountsInsideService",
          dataRecord.getServerResponseTimeoutCountsInsideService());
      item.put("tcpClientNetworkLatencyInsideService",
          dataRecord.getTcpClientNetworkLatencyInsideService());
      item.put("tcpClientNetworkLatencyCountsInsideService",
          dataRecord.getTcpClientNetworkLatencyCountsInsideService());
      item.put("tcpServerNetworkLatencyInsideService",
          dataRecord.getTcpServerNetworkLatencyInsideService());
      item.put("tcpServerNetworkLatencyCountsInsideService",
          dataRecord.getTcpServerNetworkLatencyCountsInsideService());
      item.put("tcpClientRetransmissionPacketsInsideService",
          dataRecord.getTcpClientRetransmissionPacketsInsideService());
      item.put("tcpServerRetransmissionPacketsInsideService",
          dataRecord.getTcpServerRetransmissionPacketsInsideService());

      item.put("serverResponseLatencyOutsideService",
          dataRecord.getServerResponseLatencyOutsideService());
      item.put("serverResponseLatencyCountsOutsideService",
          dataRecord.getServerResponseLatencyCountsOutsideService());
      item.put("serverResponseLatencyPeakOutsideService",
          dataRecord.getServerResponseLatencyPeakOutsideService());
      item.put("serverResponseFastCountsOutsideService",
          dataRecord.getServerResponseFastCountsOutsideService());
      item.put("serverResponseNormalCountsOutsideService",
          dataRecord.getServerResponseNormalCountsOutsideService());
      item.put("serverResponseTimeoutCountsOutsideService",
          dataRecord.getServerResponseTimeoutCountsOutsideService());
      item.put("tcpClientNetworkLatencyOutsideService",
          dataRecord.getTcpClientNetworkLatencyOutsideService());
      item.put("tcpClientNetworkLatencyCountsOutsideService",
          dataRecord.getTcpClientNetworkLatencyCountsOutsideService());
      item.put("tcpServerNetworkLatencyOutsideService",
          dataRecord.getTcpServerNetworkLatencyOutsideService());
      item.put("tcpServerNetworkLatencyCountsOutsideService",
          dataRecord.getTcpServerNetworkLatencyCountsOutsideService());
      item.put("tcpClientRetransmissionPacketsOutsideService",
          dataRecord.getTcpClientRetransmissionPacketsOutsideService());
      item.put("tcpServerRetransmissionPacketsOutsideService",
          dataRecord.getTcpServerRetransmissionPacketsOutsideService());

      item.put("tcpClientNetworkLatencyAvgInsideService",
          dataRecord.getTcpClientNetworkLatencyAvgInsideService());
      item.put("tcpServerNetworkLatencyAvgInsideService",
          dataRecord.getTcpServerNetworkLatencyAvgInsideService());
      item.put("serverResponseLatencyAvgInsideService",
          dataRecord.getServerResponseLatencyAvgInsideService());
      item.put("tcpClientNetworkLatencyAvgOutsideService",
          dataRecord.getTcpClientNetworkLatencyAvgOutsideService());
      item.put("tcpServerNetworkLatencyAvgOutsideService",
          dataRecord.getTcpServerNetworkLatencyAvgOutsideService());
      item.put("serverResponseLatencyAvgOutsideService",
          dataRecord.getServerResponseLatencyAvgOutsideService());
    }

    if (metrics.contains(CenterConstants.METRIC_NPM_UNIQUE_IP_COUNTS) || metrics.contains("ALL")) {
      item.put("uniqueIpCounts", dataRecord.getUniqueIpCounts());
    }

    return item;
  }

  /**
   * 当页面不提供查询时间间隔时使用
   * 将查询时间切分为不同精度的多个查询时间段
   * @param startTimeDate
   * @param endTimeDate
   * @return ((时间段开始时间, 时间段结束时间), 查询精度)
   */
  private List<Tuple2<Tuple2<Date, Date>, Integer>> splitQueryDate(final Date startTimeDate,
      final Date endTimeDate) {

    // 将查询时间切分为不同精度的多个查询时间段
    // |----|----|----|----|----|
    // | 1m | 5m | 1h | 5m | 1m |

    List<Tuple2<Tuple2<Date, Date>, Integer>> dateRanges = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    if (org.apache.commons.lang3.time.DateUtils.isSameInstant(startTimeDate, endTimeDate)) {
      dateRanges
          .add(Tuples.of(Tuples.of(startTimeDate, endTimeDate), Constants.ONE_MINUTE_SECONDS));
      return dateRanges;
    }

    Date hourFrom = null, hourTo = null;
    Date total5minFrom = null, total5minTo = null;

    Date head5minFrom = null, head5minTo = null;
    Date tail5minFrom = null, tail5minTo = null;

    Date head1minFrom = null, head1minTo = null;
    Date tail1minFrom = null, tail1minTo = null;

    long totalMills = endTimeDate.getTime() - startTimeDate.getTime();

    if (totalMills >= Constants.ONE_HOUR_SECONDS * 1000) {
      // 只保留小时精度
      Date startTimeHourTruncate = org.apache.commons.lang3.time.DateUtils.truncate(startTimeDate,
          Calendar.HOUR);
      Date endTimeHourTruncate = org.apache.commons.lang3.time.DateUtils.truncate(endTimeDate,
          Calendar.HOUR);

      // 开始结束时间在同一小时，则hourFrom,hourTo为null
      if (!org.apache.commons.lang3.time.DateUtils.isSameInstant(startTimeHourTruncate,
          endTimeHourTruncate)) {
        // 小时精度的开始时间为，>=原始查询开始时间的最近的小时时间；小时精度的结束时间为，<=原始查询结束时间的最近的小时时间
        hourFrom = org.apache.commons.lang3.time.DateUtils.isSameInstant(startTimeHourTruncate,
            startTimeDate) ? startTimeDate
                : com.machloop.alpha.common.util.DateUtils.afterSecondDate(startTimeHourTruncate,
                    (int) TimeUnit.HOURS.toSeconds(1));
        hourTo = endTimeHourTruncate;
      }
    }

    if (totalMills >= Constants.FIVE_MINUTE_SECONDS * 1000) {
      // 只保留分钟精度
      Date startTimeMinuteTruncate = org.apache.commons.lang3.time.DateUtils.truncate(startTimeDate,
          Calendar.MINUTE);
      Date endTimeMinuteTruncate = org.apache.commons.lang3.time.DateUtils.truncate(endTimeDate,
          Calendar.MINUTE);

      Date startTime5MinuteTruncate = org.apache.commons.lang3.time.DateUtils
          .setMinutes(startTimeMinuteTruncate, org.apache.commons.lang3.time.DateUtils
              .toCalendar(startTimeMinuteTruncate).get(Calendar.MINUTE) / 5 * 5);
      Date endTime5MinuteTruncate = org.apache.commons.lang3.time.DateUtils
          .setMinutes(endTimeMinuteTruncate, org.apache.commons.lang3.time.DateUtils
              .toCalendar(endTimeMinuteTruncate).get(Calendar.MINUTE) / 5 * 5);

      // 开始结束时间在同一5分钟，则total5minFrom,total5minTo为null
      if (!org.apache.commons.lang3.time.DateUtils.isSameInstant(startTime5MinuteTruncate,
          endTime5MinuteTruncate)) {
        // 分钟精度的开始时间为，>=原始查询开始时间的最近的整5分钟时间；分钟精度的结束时间为，<=原始查询结束时间的最近的整5分钟时间
        total5minFrom = org.apache.commons.lang3.time.DateUtils.toCalendar(startTimeMinuteTruncate)
            .get(Calendar.MINUTE) % 5 == 0
            && org.apache.commons.lang3.time.DateUtils.isSameInstant(startTimeMinuteTruncate,
                startTimeDate)
                    ? startTimeDate
                    : org.apache.commons.lang3.time.DateUtils.addMinutes(startTimeMinuteTruncate,
                        (org.apache.commons.lang3.time.DateUtils.toCalendar(startTimeMinuteTruncate)
                            .get(Calendar.MINUTE) / 5 + 1) * 5
                            - org.apache.commons.lang3.time.DateUtils
                                .toCalendar(startTimeMinuteTruncate).get(Calendar.MINUTE));
        total5minTo = org.apache.commons.lang3.time.DateUtils.toCalendar(endTimeMinuteTruncate)
            .get(Calendar.MINUTE) % 5 == 0
            && org.apache.commons.lang3.time.DateUtils.isSameInstant(endTimeMinuteTruncate,
                endTimeDate)
                    ? endTimeDate
                    : org.apache.commons.lang3.time.DateUtils.setMinutes(endTimeMinuteTruncate,
                        org.apache.commons.lang3.time.DateUtils.toCalendar(endTimeMinuteTruncate)
                            .get(Calendar.MINUTE) / 5 * 5);
      }
    }

    if (total5minFrom != null && total5minTo != null) {
      head1minFrom = startTimeDate;
      head1minTo = total5minFrom;
      tail1minFrom = total5minTo;
      tail1minTo = endTimeDate;
    } else {
      // 总时间间隔不足5分钟
      head1minFrom = startTimeDate;
      head1minTo = endTimeDate;
    }

    if (hourFrom != null && hourTo != null) {
      head5minFrom = total5minFrom;
      head5minTo = hourFrom;
      tail5minFrom = hourTo;
      tail5minTo = total5minTo;
    } else {
      // 总时间间隔不足1小时
      head5minFrom = total5minFrom;
      head5minTo = total5minTo;
    }

    // 考虑rollup是否聚合到该时间点, 该时间点未完成rollup
    String lastes1hour = globalSetting
        .getValue(CenterConstants.GLOBAL_SETTING_DR_ROLLUP_LATEST_1HOUR);
    String lastes5min = globalSetting
        .getValue(CenterConstants.GLOBAL_SETTING_DR_ROLLUP_LATEST_5MIN);

    Date lastes1hDate = StringUtils.isNotBlank(lastes1hour)
        ? com.machloop.alpha.common.util.DateUtils.parseISO8601Date(lastes1hour)
        : null;
    Date lastes5minDate = StringUtils.isNotBlank(lastes5min)
        ? com.machloop.alpha.common.util.DateUtils.parseISO8601Date(lastes5min)
        : null;

    /*
     * 当拆分后各个间隔的开始和结束时间一致，则此时间段无需查询 当拆分后时间段内有未rollup的时间，则退化为查询1min的索引
     */
    if (hourFrom != null && hourTo != null
        && !org.apache.commons.lang3.time.DateUtils.isSameInstant(hourFrom, hourTo)) {
      if (lastes1hDate == null || lastes1hDate.before(hourTo)) {
        dateRanges.add(Tuples.of(Tuples.of(hourFrom, hourTo), Constants.ONE_MINUTE_SECONDS));
      } else {
        dateRanges.add(Tuples.of(Tuples.of(hourFrom, hourTo), Constants.ONE_HOUR_SECONDS));
      }
    }

    if (head5minFrom != null && head5minTo != null
        && !org.apache.commons.lang3.time.DateUtils.isSameInstant(head5minFrom, head5minTo)) {
      if (lastes5minDate == null || lastes5minDate.before(head5minTo)) {
        dateRanges
            .add(Tuples.of(Tuples.of(head5minFrom, head5minTo), Constants.ONE_MINUTE_SECONDS));
      } else {
        dateRanges
            .add(Tuples.of(Tuples.of(head5minFrom, head5minTo), Constants.FIVE_MINUTE_SECONDS));
      }

    }
    if (tail5minFrom != null && tail5minTo != null
        && !org.apache.commons.lang3.time.DateUtils.isSameInstant(tail5minFrom, tail5minTo)) {
      if (lastes5minDate == null || lastes5minDate.before(tail5minTo)) {
        dateRanges
            .add(Tuples.of(Tuples.of(tail5minFrom, tail5minTo), Constants.ONE_MINUTE_SECONDS));
      } else {
        dateRanges
            .add(Tuples.of(Tuples.of(tail5minFrom, tail5minTo), Constants.FIVE_MINUTE_SECONDS));
      }
    }

    if (head1minFrom != null && head1minTo != null
        && !org.apache.commons.lang3.time.DateUtils.isSameInstant(head1minFrom, head1minTo)) {
      dateRanges.add(Tuples.of(Tuples.of(head1minFrom, head1minTo), Constants.ONE_MINUTE_SECONDS));
    }
    if (tail1minFrom != null && tail1minTo != null
        && !org.apache.commons.lang3.time.DateUtils.isSameInstant(tail1minFrom, tail1minTo)) {
      dateRanges.add(Tuples.of(Tuples.of(tail1minFrom, tail1minTo), Constants.ONE_MINUTE_SECONDS));
    }


    long resultTotalMills = dateRanges.stream()
        .mapToLong(range -> range.getT1().getT2().getTime() - range.getT1().getT1().getTime())
        .sum();
    if (totalMills != resultTotalMills) {
      LOGGER.warn("split query date error.date ranges: {}", dateRanges);
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug(
          "split query date, rawStartTime: [{}], rawEndTime: [{}], "
              + "head1minFrom: [{}], head1minTo: [{}], head5minFrom: [{}], head5minTo: [{}], "
              + "hourFrom: [{}], hourTo: [{}], tail5minFrom: [{}], tail5minTo: [{}], "
              + "tail1minFrom: [{}], tail1minTo: [{}]",
          com.machloop.alpha.common.util.DateUtils.toStringYYYYMMDDHHMMSS(startTimeDate),
          com.machloop.alpha.common.util.DateUtils.toStringYYYYMMDDHHMMSS(endTimeDate),
          com.machloop.alpha.common.util.DateUtils.toStringYYYYMMDDHHMMSS(head1minFrom),
          com.machloop.alpha.common.util.DateUtils.toStringYYYYMMDDHHMMSS(head1minTo),
          com.machloop.alpha.common.util.DateUtils.toStringYYYYMMDDHHMMSS(head5minFrom),
          com.machloop.alpha.common.util.DateUtils.toStringYYYYMMDDHHMMSS(head5minTo),
          com.machloop.alpha.common.util.DateUtils.toStringYYYYMMDDHHMMSS(hourFrom),
          com.machloop.alpha.common.util.DateUtils.toStringYYYYMMDDHHMMSS(hourTo),
          com.machloop.alpha.common.util.DateUtils.toStringYYYYMMDDHHMMSS(tail5minFrom),
          com.machloop.alpha.common.util.DateUtils.toStringYYYYMMDDHHMMSS(tail5minTo),
          com.machloop.alpha.common.util.DateUtils.toStringYYYYMMDDHHMMSS(tail1minFrom),
          com.machloop.alpha.common.util.DateUtils.toStringYYYYMMDDHHMMSS(tail1minTo));
    }
    return dateRanges;
  }

}
