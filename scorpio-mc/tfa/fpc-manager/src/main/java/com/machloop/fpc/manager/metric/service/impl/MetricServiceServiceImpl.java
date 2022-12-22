package com.machloop.fpc.manager.metric.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.conn.util.InetAddressUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.common.util.NetworkUtils.IpVersion;
import com.machloop.alpha.common.util.TokenUtils;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.appliance.service.AlertMessageService;
import com.machloop.fpc.manager.metric.bo.MetricDashboardSettingsBO;
import com.machloop.fpc.manager.metric.dao.MetricDashboardSettingsDao;
import com.machloop.fpc.manager.metric.dao.MetricDscpDataRecordDao;
import com.machloop.fpc.manager.metric.dao.MetricServiceDataRecordDao;
import com.machloop.fpc.manager.metric.data.MetricDashboardSettingsDO;
import com.machloop.fpc.manager.metric.data.MetricDscpDataRecordDO;
import com.machloop.fpc.manager.metric.data.MetricServiceDataRecordDO;
import com.machloop.fpc.manager.metric.service.MetricServiceService;
import com.machloop.fpc.manager.metric.vo.MetricQueryVO;
import com.machloop.fpc.npm.appliance.bo.BaselineSettingBO;
import com.machloop.fpc.npm.appliance.bo.BaselineValueBO;
import com.machloop.fpc.npm.appliance.dao.LogicalSubnetDao;
import com.machloop.fpc.npm.appliance.dao.NetworkDao;
import com.machloop.fpc.npm.appliance.dao.ServiceDao;
import com.machloop.fpc.npm.appliance.dao.ServiceFollowDao;
import com.machloop.fpc.npm.appliance.dao.ServiceNetworkDao;
import com.machloop.fpc.npm.appliance.data.LogicalSubnetDO;
import com.machloop.fpc.npm.appliance.data.NetworkDO;
import com.machloop.fpc.npm.appliance.data.ServiceDO;
import com.machloop.fpc.npm.appliance.service.BaselineService;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author guosk
 *
 * create at 2021年4月22日, fpc-manager
 */
@Service
public class MetricServiceServiceImpl implements MetricServiceService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetricServiceServiceImpl.class);

  private static final String FLOW_SORT_PROPERTY = "total_bytes";
  private static final String FLOW_SORT_DIRECTION = "desc";

  private static final int SCALE_COUNTS = 4;

  @Autowired
  private MetricServiceDataRecordDao metricServiceDao;
  @Autowired
  private MetricDscpDataRecordDao dscpDao;
  @Autowired
  private ServiceNetworkDao serviceNetworkDao;
  @Autowired
  private ServiceDao serviceDao;
  @Autowired
  private ServiceFollowDao serviceFollowDao;

  @Autowired
  private NetworkDao networkDao;
  @Autowired
  private LogicalSubnetDao logicalSubnetDao;

  @Autowired
  private BaselineService baselineService;
  @Autowired
  private AlertMessageService alertMessageService;

  @Autowired
  private MetricDashboardSettingsDao metricDashboardSettingsDao;

  @Autowired
  private RestTemplate restTemplate;

  @Value("${fpc.engine.rest.server.protocol}")
  private String fileServerProtocol;
  @Value("${fpc.engine.rest.server.host}")
  private String fileServerHost;
  @Value("${fpc.engine.rest.server.port}")
  private String fileServerPort;

  /**
   * @see com.machloop.fpc.manager.metric.service.MetricServiceService#queryMetricServices(com.machloop.fpc.manager.metric.vo.MetricQueryVO, com.machloop.alpha.common.base.page.Pageable, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Page<Map<String, Object>> queryMetricServices(MetricQueryVO queryVO, Pageable page,
      String sortProperty, String sortDirection, String name, String isFollow,
      String currentUserId) {
    List<Tuple2<String, String>> serviceNetworks = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(queryVO.getServiceId())
        && StringUtils.isNotBlank(queryVO.getNetworkId())) {
      serviceNetworks.add(Tuples.of(queryVO.getServiceId(), queryVO.getNetworkId()));
    } else {
      // 查询有效业务及所在网络
      List<Tuple2<String, String>> vaildServiceNetwork = serviceNetworkDao
          .queryServiceNetworks().stream().map(serviceNetwork -> Tuples
              .of(serviceNetwork.getServiceId(), serviceNetwork.getNetworkId()))
          .collect(Collectors.toList());

      // 过滤名称
      if (StringUtils.isNotBlank(name)) {
        List<String> services = serviceDao.queryServices(name).stream().map(ServiceDO::getId)
            .collect(Collectors.toList());
        vaildServiceNetwork = vaildServiceNetwork.stream()
            .filter(serviceNetwork -> services.contains(serviceNetwork.getT1()))
            .collect(Collectors.toList());
      }

      // 过滤是否关注
      if (StringUtils.equals(isFollow, Constants.BOOL_YES)) {
        List<Tuple2<String, String>> serviceFollows = serviceFollowDao
            .queryUserFollowService(currentUserId).stream().map(serviceFollow -> Tuples
                .of(serviceFollow.getServiceId(), serviceFollow.getNetworkId()))
            .collect(Collectors.toList());
        vaildServiceNetwork = vaildServiceNetwork.stream()
            .filter(serviceNetwork -> serviceFollows.contains(serviceNetwork))
            .collect(Collectors.toList());
      }

      serviceNetworks.addAll(vaildServiceNetwork);
    }

    if (serviceNetworks.isEmpty()) {
      return new PageImpl<>(Lists.newArrayListWithCapacity(0), page, 0);
    }

    // 获取业务统计信息
    List<String> metrics = Lists.newArrayList("ALL");
    Page<MetricServiceDataRecordDO> metricServices = metricServiceDao.queryMetricServices(queryVO,
        page, sortProperty, sortDirection, metrics, serviceNetworks);
    List<MetricServiceDataRecordDO> contents = Lists.newArrayList(metricServices.getContent());

    int totalNumber = serviceNetworks.size();
    int vaildTotalNumber = (int) metricServices.getTotalElements();

    if (page.getPageSize() - contents.size() > 0) {
      // 查询结果不够当前页数量时，填充未获取到统计信息的业务
      serviceNetworks.removeAll(
          contents.stream().map(metric -> Tuples.of(metric.getServiceId(), metric.getNetworkId()))
              .collect(Collectors.toList()));

      // 排序,保证分页补充的时候不会混乱
      serviceNetworks.sort(new Comparator<Tuple2<String, String>>() {
        @Override
        public int compare(Tuple2<String, String> o1, Tuple2<String, String> o2) {

          long o1Value = o1.getT1().hashCode() + o1.getT2().hashCode();
          long o2Value = o2.getT1().hashCode() + o2.getT2().hashCode();

          return (int) (o1Value - o2Value);
        }
      });

      // 计算需要补充的业务
      int start = page.getOffset() > vaildTotalNumber ? page.getOffset() - vaildTotalNumber : 0;
      int currentPageEnd = page.getOffset() + page.getPageSize();
      int end = (currentPageEnd > totalNumber ? totalNumber : currentPageEnd) - vaildTotalNumber;
      if (serviceNetworks.size() >= end) {
        serviceNetworks = serviceNetworks.subList(start, end);
      }

      serviceNetworks.forEach(serviceNetwork -> {
        MetricServiceDataRecordDO metricService = new MetricServiceDataRecordDO();
        metricService.setServiceId(serviceNetwork.getT1());
        metricService.setNetworkId(serviceNetwork.getT2());

        contents.add(metricService);
      });
    }

    // 获取业务名称
    Map<String, String> serviceIdNameMap = serviceDao.queryServices(name).stream()
        .collect(Collectors.toMap(ServiceDO::getId, ServiceDO::getName));

    // 获取网络名称
    Map<String,
        String> networkIdNameMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    networkIdNameMap.putAll(logicalSubnetDao.queryLogicalSubnets().stream()
        .collect(Collectors.toMap(LogicalSubnetDO::getId, LogicalSubnetDO::getName)));
    networkIdNameMap.putAll(networkDao.queryNetworks().stream()
        .collect(Collectors.toMap(NetworkDO::getId, NetworkDO::getName)));

    return new PageImpl<>(contents.stream().map(tmp -> {
      Map<String, Object> metricServiceMap = metricService2Map(tmp, metrics);
      metricServiceMap.put("serviceText", serviceIdNameMap.get(tmp.getServiceId()));
      metricServiceMap.put("networkText", networkIdNameMap.get(tmp.getNetworkId()));

      return metricServiceMap;
    }).collect(Collectors.toList()), page, totalNumber);
  }

  /**
   * @see com.machloop.fpc.manager.metric.service.MetricServiceService#queryMetricServiceHistograms(com.machloop.fpc.manager.metric.vo.MetricQueryVO, boolean)
   */
  @Override
  public List<Map<String, Object>> queryMetricServiceHistograms(MetricQueryVO queryVO,
      boolean extendedBound) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    List<String> metrics = Lists.newArrayList("ALL");
    List<MetricServiceDataRecordDO> histogram = metricServiceDao
        .queryMetricServiceHistograms(queryVO, extendedBound, metrics);

    for (MetricServiceDataRecordDO tmp : histogram) {
      Map<String, Object> metricServiceMap = metricService2Map(tmp, metrics);
      metricServiceMap.put("timestamp", tmp.getTimestamp());
      result.add(metricServiceMap);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.manager.metric.service.MetricServiceService#queryServiceDashboard(com.machloop.fpc.manager.metric.vo.MetricQueryVO, javax.servlet.http.HttpServletRequest)
   */
  @Override
  public Map<String, Object> queryServiceDashboard(MetricQueryVO queryVO,
      HttpServletRequest request) {
    if (StringUtils.equals(queryVO.getRealTime(), Constants.BOOL_YES)) {
      String path = String.format(ManagerConstants.REST_ENGINE_STATISTICS_SERVICE_DASHBOARD,
          queryVO.getServiceId());
      return queryRealTimeStatistics(path, queryVO.getNetworkId(), request);
    }

    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    // 业务指标汇总
    List<String> summaryMetrics = Lists.newArrayList(ManagerConstants.METRIC_NPM_FLOW,
        ManagerConstants.METRIC_NPM_PERFORMANCE);
    Page<MetricServiceDataRecordDO> metricServices = metricServiceDao.queryMetricServices(queryVO,
        null, FLOW_SORT_PROPERTY, FLOW_SORT_DIRECTION, summaryMetrics, null);
    MetricServiceDataRecordDO summary = metricServices.getTotalElements() == 0
        ? new MetricServiceDataRecordDO()
        : metricServices.getContent().get(0);
    result.putAll(metricService2Map(summary, summaryMetrics));

    // 业务指标趋势图
    List<String> histogramsMetrics = Lists.newArrayList(ManagerConstants.METRIC_NPM_FRAGMENT);
    List<Map<String, Object>> histograms = metricServiceDao
        .queryMetricServiceHistograms(queryVO, true, histogramsMetrics).stream().map(histogram -> {
          Map<String, Object> metricServiceMap = metricService2Map(histogram, histogramsMetrics);
          metricServiceMap.put("timestamp", histogram.getTimestamp());

          return metricServiceMap;
        }).collect(Collectors.toList());
    result.put("histogram", histograms);

    // DSCP
    Map<String, Object> metricDscps = queryMetricDscps(queryVO, FLOW_SORT_PROPERTY,
        FLOW_SORT_DIRECTION, queryVO.getCount());
    result.put("dscp", metricDscps);

    return result;
  }

  /**
   * 负载量
   * @see com.machloop.fpc.manager.metric.service.MetricServiceService#queryPayloadStatistics(com.machloop.fpc.manager.metric.vo.MetricQueryVO, javax.servlet.http.HttpServletRequest)
   */
  @Override
  public List<Map<String, Object>> queryPayloadStatistics(MetricQueryVO queryVO,
      HttpServletRequest request) {
    if (StringUtils.equals(queryVO.getRealTime(), Constants.BOOL_YES)) {
      String path = String.format(ManagerConstants.REST_ENGINE_STATISTICS_SERVICE_PAYLOAD,
          queryVO.getServiceId());
      Map<String, Object> realTimeStatistics = queryRealTimeStatistics(path, queryVO.getNetworkId(),
          request);

      return JsonHelper.deserialize(
          JsonHelper.serialize(realTimeStatistics.get("histogram"), false),
          new TypeReference<List<Map<String, Object>>>() {
          }, false);
    }

    List<String> aggsFields = Lists.newArrayList("total_bytes", "total_packets", "byteps_peak",
        "upstream_bytes", "downstream_bytes", "upstream_packets", "downstream_packets",
        "concurrent_sessions", "established_sessions", "unique_ip_counts");

    Map<Long,
        Map<String, Object>> tempMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    // 当前查询时间统计
    List<Map<String, Object>> currentMetricHistograms = metricServiceDao
        .queryMetricServiceHistogramsWithAggsFields(queryVO, true, aggsFields);
    currentMetricHistograms.forEach(temp -> {
      Long totalBytes = MapUtils.getLong(temp, "totalBytes");
      Long bytepsPeak = MapUtils.getLong(temp, "bytepsPeak");
      // 补点数据只包含timestamp
      if (totalBytes != null) {
        temp.put("bandwidth", totalBytes * Constants.BYTE_BITS / queryVO.getInterval());
        temp.put("bytepsPeak", bytepsPeak);
        temp.put("lastWeekSamePeriodBandwidth", 0);
        temp.put("baselineBandwidth", 0);
        temp.put("lastWeekSamePeriodTotalBytes", 0);
        temp.put("baselineTotalBytes", 0);
        temp.put("lastWeekSamePeriodTotalPackets", 0);
        temp.put("baselineTotalPackets", 0);
      }
      OffsetDateTime offsetTime = (OffsetDateTime) temp.get("timestamp");
      tempMap.put(Date.from(offsetTime.toInstant()).getTime(), temp);
    });

    // 上周同期
    MetricQueryVO lastWeekSamePeriodQueryVO = new MetricQueryVO();
    lastWeekSamePeriodQueryVO.setInterval(queryVO.getInterval());
    lastWeekSamePeriodQueryVO.setServiceId(queryVO.getServiceId());
    lastWeekSamePeriodQueryVO.setNetworkId(queryVO.getNetworkId());
    Tuple2<Date, Date> lastWeekSamePeriod = getLastWeekSameTime(queryVO.getStartTimeDate(),
        queryVO.getEndTimeDate());
    lastWeekSamePeriodQueryVO.setStartTimeDate(lastWeekSamePeriod.getT1());
    lastWeekSamePeriodQueryVO.setEndTimeDate(lastWeekSamePeriod.getT2());
    List<Map<String, Object>> lastWeekSamePeriodMetricHistograms = metricServiceDao
        .queryMetricServiceHistogramsWithAggsFields(lastWeekSamePeriodQueryVO, true,
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
    List<BaselineSettingBO> baselineSettings = baselineService.querySubdivisionBaselineSettings(
        FpcConstants.SOURCE_TYPE_SERVICE, queryVO.getNetworkId(), queryVO.getServiceId(), null);
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
          case FpcConstants.BASELINE_CATEGORY_BANDWIDTH:
            bandwidthBaseline = baselineSetting;
            break;
          case FpcConstants.BASELINE_CATEGORY_FLOW:
            flowBaseline = baselineSetting;
            break;
          case FpcConstants.BASELINE_CATEGORY_PACKET:
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
          FpcConstants.BASELINE_SETTING_SOURCE_NPM, bandwidthBaseline.getId(),
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
          FpcConstants.BASELINE_SETTING_SOURCE_NPM, flowBaseline.getId(),
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
          FpcConstants.BASELINE_SETTING_SOURCE_NPM, packetBaseline.getId(),
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
   * 性能
   * @see com.machloop.fpc.manager.metric.service.MetricServiceService#queryPerformanceStatistics(com.machloop.fpc.manager.metric.vo.MetricQueryVO, javax.servlet.http.HttpServletRequest)
   */
  @Override
  public List<Map<String, Object>> queryPerformanceStatistics(MetricQueryVO queryVO,
      HttpServletRequest request) {
    if (StringUtils.equals(queryVO.getRealTime(), Constants.BOOL_YES)) {
      String path = String.format(ManagerConstants.REST_ENGINE_STATISTICS_SERVICE_PERFORMANCE,
          queryVO.getServiceId());
      Map<String, Object> realTimeStatistics = queryRealTimeStatistics(path, queryVO.getNetworkId(),
          request);

      return JsonHelper.deserialize(
          JsonHelper.serialize(realTimeStatistics.get("histogram"), false),
          new TypeReference<List<Map<String, Object>>>() {
          }, false);
    }

    List<String> metrics = Lists.newArrayList(ManagerConstants.METRIC_NPM_SESSION,
        ManagerConstants.METRIC_NPM_PERFORMANCE);

    Map<Long,
        Map<String, Object>> tempMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    // 当前统计信息
    List<MetricServiceDataRecordDO> currentMetricHistograms = metricServiceDao
        .queryMetricServiceHistograms(queryVO, true, metrics);
    currentMetricHistograms.forEach(current -> {
      Map<String, Object> metricServiceMap = metricService2Map(current, metrics);
      metricServiceMap.put("timestamp", current.getTimestamp());
      metricServiceMap.put("lastWeekSamePeriodServerResponseLatencyAvg", 0);
      metricServiceMap.put("lastWeekSamePeriodServerResponseLatencyAvgInsideService", 0);
      metricServiceMap.put("lastWeekSamePeriodServerResponseLatencyAvgOutsideService", 0);
      metricServiceMap.put("baselineServerResponseLatencyAvg", 0);

      tempMap.put(current.getTimestamp().getTime(), metricServiceMap);
    });

    // 上周同期
    MetricQueryVO lastWeekSamePeriodQueryVO = new MetricQueryVO();
    lastWeekSamePeriodQueryVO.setInterval(queryVO.getInterval());
    lastWeekSamePeriodQueryVO.setServiceId(queryVO.getServiceId());
    lastWeekSamePeriodQueryVO.setNetworkId(queryVO.getNetworkId());
    Tuple2<Date, Date> lastWeekSamePeriod = getLastWeekSameTime(queryVO.getStartTimeDate(),
        queryVO.getEndTimeDate());
    lastWeekSamePeriodQueryVO.setStartTimeDate(lastWeekSamePeriod.getT1());
    lastWeekSamePeriodQueryVO.setEndTimeDate(lastWeekSamePeriod.getT2());
    List<Map<String, Object>> lastWeekSamePeriodMetricHistograms = metricServiceDao
        .queryMetricServiceHistogramsWithAggsFields(lastWeekSamePeriodQueryVO, true,
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

    // 过滤符合时间间隔的基线定义
    List<BaselineSettingBO> baselineSettings = baselineService.querySubdivisionBaselineSettings(
        FpcConstants.SOURCE_TYPE_SERVICE, queryVO.getNetworkId(), queryVO.getServiceId(),
        FpcConstants.BASELINE_CATEGORY_RESPONSELATENCY);
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
      List<BaselineValueBO> packetBaselines = baselineService.queryBaselineValue(
          FpcConstants.BASELINE_SETTING_SOURCE_NPM, responseLatencyBaseline.getId(),
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
   * @see com.machloop.fpc.manager.metric.service.MetricServiceService#queryTcpStatistics(com.machloop.fpc.manager.metric.vo.MetricQueryVO, javax.servlet.http.HttpServletRequest)
   */
  @Override
  public List<Map<String, Object>> queryTcpStatistics(MetricQueryVO queryVO,
      HttpServletRequest request) {
    if (StringUtils.equals(queryVO.getRealTime(), Constants.BOOL_YES)) {
      String path = String.format(ManagerConstants.REST_ENGINE_STATISTICS_SERVICE_TCP,
          queryVO.getServiceId());
      Map<String, Object> realTimeStatistics = queryRealTimeStatistics(path, queryVO.getNetworkId(),
          request);

      return JsonHelper.deserialize(
          JsonHelper.serialize(realTimeStatistics.get("histogram"), false),
          new TypeReference<List<Map<String, Object>>>() {
          }, false);
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

    return metricServiceDao.queryMetricServiceHistogramsWithAggsFields(queryVO, true, aggsFields);
  }

  @Override
  public Map<String, Object> queryDashboardSettings(String operatorId) {
    Map<String, Object> result = metricDashboardSettingsDao.queryDashboardSettings(operatorId);
    if (result.isEmpty()) {
      Map<String,
          Object> parametersMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      List<Object> valueList = new ArrayList<>();
      valueList.add(0);
      valueList.add("0");
      parametersMap.put("totalBytes", valueList);
      parametersMap.put("concurrentSessions", valueList);
      parametersMap.put("establishedSessions", valueList);
      parametersMap.put("tcpClientNetworkLatencyAvg", valueList);
      result.put("parameters", JsonHelper.serialize(parametersMap));
      result.put("percentParameter", "0");
      result.put("timeWindowParameter", "0");
    }
    return result;
  }

  @Override
  public MetricDashboardSettingsBO updateDashboardSettings(
      MetricDashboardSettingsBO metricDashboardSettingsBO, String operatorId) {

    MetricDashboardSettingsDO metricDashboardSettingsDO = new MetricDashboardSettingsDO();
    BeanUtils.copyProperties(metricDashboardSettingsBO, metricDashboardSettingsDO);
    metricDashboardSettingsDO.setOperatorId(operatorId);

    MetricDashboardSettingsDO existSettings = metricDashboardSettingsDao
        .queryDashboardSettingsByOperatorId(operatorId);

    if (StringUtils.isBlank(existSettings.getId())) {
      metricDashboardSettingsDao.saveDashboardSettings(metricDashboardSettingsDO);
    } else {
      metricDashboardSettingsDO.setId(existSettings.getId());
      metricDashboardSettingsDao.updateDashboardSettings(metricDashboardSettingsDO);
    }

    BeanUtils.copyProperties(metricDashboardSettingsDO, metricDashboardSettingsBO);
    return metricDashboardSettingsBO;
  }

  /**
   * 获取实时统计数据
   * @param path
   * @param request
   * @return
   */
  private synchronized Map<String, Object> queryRealTimeStatistics(String path, String networkId,
      HttpServletRequest request) {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    String requestUrl = "";
    try {
      // 使用UUID作为凭证，并取token进行签名
      String credential = IdGenerator.generateUUID();
      String token = HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_TOKEN);
      String date = DateUtils.toStringISO8601(DateUtils.now());
      String serverIp = fileServerHost;
      String[] ipList = StringUtils.split(fileServerHost, ",");
      if (ipList.length > 1) {
        if (InetAddressUtils.isIPv4Address(request.getRemoteAddr())) {
          serverIp = InetAddressUtils.isIPv4Address(ipList[0]) ? ipList[0] : ipList[1];
        } else {
          serverIp = InetAddressUtils.isIPv6Address(ipList[0]) ? ipList[0] : ipList[1];
        }
      }

      // 拼接地址
      StringBuilder url = new StringBuilder();
      url.append(fileServerProtocol);
      url.append("://");
      if (NetworkUtils.isInetAddress(serverIp, IpVersion.V6)) {
        url.append("[").append(serverIp).append("]");
      } else {
        url.append(serverIp);
      }
      url.append(":");
      url.append(fileServerPort);
      url.append(path);
      url.append("?networkId=");
      url.append(networkId);
      url.append("&X-Machloop-Date=");
      url.append(URLEncoder.encode(date, StandardCharsets.UTF_8.name()));
      url.append("&X-Machloop-Credential=");
      url.append(credential);
      url.append("&X-Machloop-Signature=");
      url.append(TokenUtils.makeSignature(token, credential, "GET", date, path));
      requestUrl = url.toString();
      LOGGER.debug("invoke rest api:{}", url);

      String resultStr = restTemplate.getForObject(url.toString(), String.class);
      if (StringUtils.isBlank(resultStr)) {
        throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "系统异常，未获取到统计数据");
      }

      result = JsonHelper.deserialize(resultStr, new TypeReference<Map<String, Object>>() {
      }, false);
    } catch (Exception e) {
      LOGGER.warn("failed to query realTime statistics [" + requestUrl + "].", e);
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "实时刷新请求异常");
    }

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

  @SuppressWarnings("unused")
  private long countAlertMessage(Date startTime, Date endTime, String serviceId, String networkId) {
    return alertMessageService.countAlertMessages(startTime, endTime, networkId, serviceId);
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

  private Map<String, Object> metricService2Map(MetricServiceDataRecordDO dataRecord,
      List<String> metrics) {
    Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    item.put("networkId", dataRecord.getNetworkId());
    item.put("serviceId", dataRecord.getServiceId());

    if (metrics.contains(ManagerConstants.METRIC_NPM_FLOW) || metrics.contains("ALL")) {
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

    if (metrics.contains(ManagerConstants.METRIC_NPM_FRAGMENT) || metrics.contains("ALL")) {
      item.put("fragmentTotalBytes", dataRecord.getFragmentTotalBytes());
      item.put("fragmentTotalPackets", dataRecord.getFragmentTotalPackets());
    }

    if (metrics.contains(ManagerConstants.METRIC_NPM_TCP) || metrics.contains("ALL")) {
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

    if (metrics.contains(ManagerConstants.METRIC_NPM_SESSION) || metrics.contains("ALL")) {
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

    if (metrics.contains(ManagerConstants.METRIC_NPM_PERFORMANCE) || metrics.contains("ALL")) {
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

    if (metrics.contains(ManagerConstants.METRIC_NPM_UNIQUE_IP_COUNTS) || metrics.contains("ALL")) {
      item.put("uniqueIpCounts", dataRecord.getUniqueIpCounts());
    }

    return item;
  }

}
