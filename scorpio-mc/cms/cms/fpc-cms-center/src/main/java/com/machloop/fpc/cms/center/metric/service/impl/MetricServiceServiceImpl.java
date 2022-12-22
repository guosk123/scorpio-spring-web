package com.machloop.fpc.cms.center.metric.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.common.util.TextUtils;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.appliance.bo.BaselineSettingBO;
import com.machloop.fpc.cms.center.appliance.bo.BaselineValueBO;
import com.machloop.fpc.cms.center.appliance.dao.ServiceDao;
import com.machloop.fpc.cms.center.appliance.dao.ServiceFollowDao;
import com.machloop.fpc.cms.center.appliance.dao.ServiceNetworkDao;
import com.machloop.fpc.cms.center.appliance.data.ServiceDO;
import com.machloop.fpc.cms.center.appliance.service.BaselineService;
import com.machloop.fpc.cms.center.metric.bo.MetricDashboardSettingsBO;
import com.machloop.fpc.cms.center.metric.dao.MetricDashboardSettingsDao;
import com.machloop.fpc.cms.center.metric.dao.MetricDscpDataRecordDao;
import com.machloop.fpc.cms.center.metric.dao.MetricServiceDataRecordDao;
import com.machloop.fpc.cms.center.metric.data.MetricDashboardSettingsDO;
import com.machloop.fpc.cms.center.metric.data.MetricDscpDataRecordDO;
import com.machloop.fpc.cms.center.metric.data.MetricServiceDataRecordDO;
import com.machloop.fpc.cms.center.metric.service.MetricInSecondService;
import com.machloop.fpc.cms.center.metric.service.MetricServiceService;
import com.machloop.fpc.cms.center.metric.vo.MetricQueryVO;
import com.machloop.fpc.cms.center.sensor.bo.SensorNetworkPermBO;
import com.machloop.fpc.cms.center.sensor.dao.SensorNetworkGroupDao;
import com.machloop.fpc.cms.center.sensor.data.SensorNetworkGroupDO;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkPermService;
import com.machloop.fpc.cms.common.FpcCmsConstants;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
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
  private SensorNetworkGroupDao sensorNetworkGroupDao;

  @Autowired
  private BaselineService baselineService;
  @Autowired
  private MetricInSecondService metricInSecondService;
  @Autowired
  private SensorNetworkPermService sensorNetworkPermService;

  @Autowired
  private MetricDashboardSettingsDao metricDashboardSettingsDao;

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricServiceService#queryMetricServices(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, com.machloop.alpha.common.base.page.Pageable, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Page<Map<String, Object>> queryMetricServices(MetricQueryVO queryVO, Pageable page,
      String sortProperty, String sortDirection, String name, String isFollow,
      String currentUserId) {
    // 根据过滤条件筛选出有效的业务网络（组）对
    List<Tuple3<String, String, String>> serviceNetworks = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(queryVO.getServiceId())
        && (StringUtils.isNotBlank(queryVO.getNetworkId())
            || StringUtils.isNotBlank(queryVO.getNetworkGroupId()))) {
      serviceNetworks.add(
          Tuples.of(queryVO.getServiceId(), queryVO.getNetworkId(), queryVO.getNetworkGroupId()));
    } else {
      // 查询有效业务及所在网络（组）
      List<Tuple3<String, String, String>> vaildServiceNetwork = serviceNetworkDao
          .queryServiceNetworks().stream()
          .map(serviceNetwork -> Tuples.of(
              StringUtils.defaultIfBlank(serviceNetwork.getServiceId(), ""),
              StringUtils.isBlank(serviceNetwork.getNetworkId()) ? ""
                  : serviceNetwork.getNetworkId().contains("^")
                      ? StringUtils.split(serviceNetwork.getNetworkId(), "^")[1]
                      : serviceNetwork.getNetworkId(),
              StringUtils.defaultIfBlank(serviceNetwork.getNetworkGroupId(), "")))
          .collect(Collectors.toList());

      // 过滤用户可访问网络
      SensorNetworkPermBO currentUserNetworkPerms = sensorNetworkPermService
          .queryCurrentUserNetworkPerms();
      if (!currentUserNetworkPerms.getServiceUser()) {
        List<String> neworkPerms = CsvUtils
            .convertCSVToList(currentUserNetworkPerms.getNetworkIds());
        List<String> neworkGroupPerms = CsvUtils
            .convertCSVToList(currentUserNetworkPerms.getNetworkGroupIds());
        vaildServiceNetwork = vaildServiceNetwork.stream()
            .filter(serviceNetwork -> neworkPerms.contains(serviceNetwork.getT2())
                || neworkGroupPerms.contains(serviceNetwork.getT3()))
            .collect(Collectors.toList());
      }

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
        List<Tuple3<String, String, String>> serviceFollows = serviceFollowDao
            .queryUserFollowService(currentUserId).stream()
            .map(serviceFollow -> Tuples.of(serviceFollow.getServiceId(),
                StringUtils.defaultIfBlank(serviceFollow.getNetworkId(), ""),
                StringUtils.defaultIfBlank(serviceFollow.getNetworkGroupId(), "")))
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

    // 每对业务网络（组）所对应探针上实际的业务网络（组）对集合
    int totalNumber = serviceNetworks.size();
    Map<Tuple3<String, String, String>,
        List<Tuple2<String, String>>> serviceNetworkIdMappings = queryFpcService(serviceNetworks);

    // 根据排序键查询全部业务
    List<MetricServiceDataRecordDO> sortPropertiesMetricResult = metricServiceDao
        .queryMetricServices(queryVO, null, sortProperty);
    Map<Tuple2<String, String>,
        List<MetricServiceDataRecordDO>> collect = sortPropertiesMetricResult.stream().collect(
            Collectors.groupingBy(item -> Tuples.of(item.getServiceId(), item.getNetworkId())));

    // 将相同的cms业务合并，并排序，截取当前页的业务网络（组）对
    List<String> sortPropertiesMetrics = Lists.newArrayList(CenterConstants.METRIC_NPM_FLOW);
    List<Map<String, Object>> mergeMetricResults = serviceNetworkIdMappings.entrySet().stream()
        .map(item -> {
          List<MetricServiceDataRecordDO> list = Lists
              .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
          item.getValue().forEach(serviceNetworkId -> {
            list.addAll(collect.get(serviceNetworkId) == null ? Lists.newArrayList()
                : collect.get(serviceNetworkId));
          });

          return mergeMetrics(item.getKey().getT1(), item.getKey().getT2(), item.getKey().getT3(),
              list, sortPropertiesMetrics);
        }).collect(Collectors.toList());
    sortMetricResult(mergeMetricResults, sortProperty, sortDirection);
    List<Tuple3<String, String, String>> currentPageKeys = mergeMetricResults.stream()
        .map(item -> Tuples.of(MapUtils.getString(item, "serviceId"),
            MapUtils.getString(item, "networkId", ""),
            MapUtils.getString(item, "networkGroupId", "")))
        .skip(page.getOffset()).limit(page.getPageSize()).collect(Collectors.toList());

    if (CollectionUtils.isEmpty(currentPageKeys)) {
      return new PageImpl<>(Lists.newArrayListWithCapacity(0), page, totalNumber);
    }

    // 获取当前页所有业务整体统计信息，将相同的cms业务合并，并排序
    List<Tuple2<String, String>> currentPageServiceNetworkIds = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    currentPageKeys.forEach(item -> {
      currentPageServiceNetworkIds.addAll(serviceNetworkIdMappings.get(item));
    });
    queryVO.setServiceNetworkIds(currentPageServiceNetworkIds);

    List<String> metrics = Lists.newArrayList("ALL");
    List<MetricServiceDataRecordDO> metricServices = metricServiceDao.queryMetricServices(queryVO,
        metrics, null);
    Map<Tuple2<String, String>,
        List<MetricServiceDataRecordDO>> groupByResult = metricServices.stream().collect(
            Collectors.groupingBy(item -> Tuples.of(item.getServiceId(), item.getNetworkId())));

    List<Map<String, Object>> result = serviceNetworkIdMappings.entrySet().stream()
        .filter(item -> currentPageKeys.contains(item.getKey())).map(item -> {
          List<MetricServiceDataRecordDO> list = Lists
              .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
          item.getValue().forEach(serviceNetworkId -> {
            List<MetricServiceDataRecordDO> temp = Lists
                .newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
            list.addAll(groupByResult.get(serviceNetworkId) == null ? temp
                : groupByResult.get(serviceNetworkId));
          });

          return mergeMetrics(item.getKey().getT1(), item.getKey().getT2(), item.getKey().getT3(),
              list, metrics);
        }).collect(Collectors.toList());
    sortMetricResult(result, sortProperty, sortDirection);

    return new PageImpl<>(result, page, totalNumber);
  }

  /**
   * 获取每对CMS上业务网络所对应的探针实际业务网络
   * @param serviceNetworkIds <service_network_networkGroup,[<service, network>...]>
   * @return
   */
  private Map<Tuple3<String, String, String>, List<Tuple2<String, String>>> queryFpcService(
      List<Tuple3<String, String, String>> serviceNetworkIds) {
    Map<Tuple3<String, String, String>, List<Tuple2<String, String>>> map = Maps
        .newHashMapWithExpectedSize(serviceNetworkIds.size());

    // 网络组
    Map<String,
        String> networkGroups = sensorNetworkGroupDao.querySensorNetworkGroups().stream()
            .collect(Collectors.toMap(SensorNetworkGroupDO::getId,
                SensorNetworkGroupDO::getNetworkInSensorIds));
    for (Tuple3<String, String, String> serviceNetworkId : serviceNetworkIds) {
      List<String> networkIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      if (StringUtils.isNotBlank(serviceNetworkId.getT3())) {
        networkIds.addAll(CsvUtils.convertCSVToList(networkGroups.get(serviceNetworkId.getT3())));
      } else {
        networkIds.add(serviceNetworkId.getT2());
      }

      List<Tuple2<String, String>> result = networkIds.stream()
          .map(networkId -> Tuples.of(serviceNetworkId.getT1(), networkId))
          .collect(Collectors.toList());

      map.put(serviceNetworkId, result);
    }

    return map;
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricServiceService#queryMetricServiceHistograms(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, boolean)
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
      metricServiceMap.put("serviceId", queryVO.getServiceId());
      metricServiceMap.put("networkId", queryVO.getNetworkId());
      metricServiceMap.put("networkGroupId", queryVO.getNetworkGroupId());
      result.add(metricServiceMap);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.cms.center.metric.service.MetricServiceService#queryServiceDashboard(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, javax.servlet.http.HttpServletRequest)
   */
  @Override
  public Map<String, Object> queryServiceDashboard(MetricQueryVO queryVO,
      HttpServletRequest request) {
    if (StringUtils.equals(queryVO.getRealTime(), Constants.BOOL_YES)) {
      String path = CenterConstants.REST_ENGINE_STATISTICS_SERVICE_DASHBOARD + "?networkId=%s";
      metricInSecondService.asyncCollection(queryVO, MetricInSecondService.METRIC_SERVICE, path,
          request);
      return metricInSecondService.queryServiceDashboard(queryVO);
    }

    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    // 业务指标汇总
    List<String> summaryMetrics = Lists.newArrayList(CenterConstants.METRIC_NPM_FLOW,
        CenterConstants.METRIC_NPM_PERFORMANCE);
    MetricServiceDataRecordDO metricService = metricServiceDao.queryMetricService(queryVO,
        summaryMetrics);
    result.putAll(metricService2Map(metricService, summaryMetrics));
    result.put("serviceId", queryVO.getServiceId());
    result.put("networkId", queryVO.getNetworkId());
    result.put("networkGroupId", queryVO.getNetworkGroupId());

    // 业务指标趋势图
    List<String> histogramsMetrics = Lists.newArrayList(CenterConstants.METRIC_NPM_FRAGMENT);
    List<Map<String, Object>> histograms = metricServiceDao
        .queryMetricServiceHistograms(queryVO, true, histogramsMetrics).stream().map(histogram -> {
          Map<String, Object> metricServiceMap = metricService2Map(histogram, histogramsMetrics);
          metricServiceMap.put("serviceId", queryVO.getServiceId());
          metricServiceMap.put("networkId", queryVO.getNetworkId());
          metricServiceMap.put("networkGroupId", queryVO.getNetworkGroupId());
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
   * @see com.machloop.fpc.cms.center.metric.service.MetricServiceService#queryPayloadStatistics(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, javax.servlet.http.HttpServletRequest)
   */
  @Override
  public List<Map<String, Object>> queryPayloadStatistics(MetricQueryVO queryVO,
      HttpServletRequest request) {
    if (StringUtils.equals(queryVO.getRealTime(), Constants.BOOL_YES)) {
      String path = CenterConstants.REST_ENGINE_STATISTICS_SERVICE_PAYLOAD + "?networkId=%s";
      metricInSecondService.asyncCollection(queryVO, MetricInSecondService.METRIC_PAYLOAD, path,
          request);
      return metricInSecondService.queryPayloadStatistics(queryVO);
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
      temp.put("serviceId", queryVO.getServiceId());
      temp.put("networkId", queryVO.getNetworkId());
      temp.put("networkGroupId", queryVO.getNetworkGroupId());

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
    lastWeekSamePeriodQueryVO.setServiceNetworkIds(queryVO.getServiceNetworkIds());
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
        FpcCmsConstants.SOURCE_TYPE_SERVICE, queryVO.getNetworkId(), null, queryVO.getServiceId(),
        null);
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
   * 性能
   * @see com.machloop.fpc.cms.center.metric.service.MetricServiceService#queryPerformanceStatistics(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, javax.servlet.http.HttpServletRequest)
   */
  @Override
  public List<Map<String, Object>> queryPerformanceStatistics(MetricQueryVO queryVO,
      HttpServletRequest request) {
    if (StringUtils.equals(queryVO.getRealTime(), Constants.BOOL_YES)) {
      String path = CenterConstants.REST_ENGINE_STATISTICS_SERVICE_PERFORMANCE + "?networkId=%s";
      metricInSecondService.asyncCollection(queryVO, MetricInSecondService.METRIC_PERFORMANCE, path,
          request);
      return metricInSecondService.queryPerformanceStatistics(queryVO);
    }

    List<String> metrics = Lists.newArrayList(CenterConstants.METRIC_NPM_SESSION,
        CenterConstants.METRIC_NPM_PERFORMANCE);

    Map<Long,
        Map<String, Object>> tempMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    // 当前统计信息
    List<MetricServiceDataRecordDO> currentMetricHistograms = metricServiceDao
        .queryMetricServiceHistograms(queryVO, true, metrics);
    currentMetricHistograms.forEach(current -> {
      Map<String, Object> metricServiceMap = metricService2Map(current, metrics);
      metricServiceMap.put("serviceId", queryVO.getServiceId());
      metricServiceMap.put("networkId", queryVO.getNetworkId());
      metricServiceMap.put("networkGroupId", queryVO.getNetworkGroupId());
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
    lastWeekSamePeriodQueryVO.setServiceNetworkIds(queryVO.getServiceNetworkIds());
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
        FpcCmsConstants.SOURCE_TYPE_SERVICE, queryVO.getNetworkId(), null, queryVO.getServiceId(),
        FpcCmsConstants.BASELINE_CATEGORY_RESPONSELATENCY);
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
   * @see com.machloop.fpc.cms.center.metric.service.MetricServiceService#queryTcpStatistics(com.machloop.fpc.cms.center.metric.vo.MetricQueryVO, javax.servlet.http.HttpServletRequest)
   */
  @Override
  public List<Map<String, Object>> queryTcpStatistics(MetricQueryVO queryVO,
      HttpServletRequest request) {
    if (StringUtils.equals(queryVO.getRealTime(), Constants.BOOL_YES)) {
      String path = CenterConstants.REST_ENGINE_STATISTICS_SERVICE_TCP + "?networkId=%s";
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

    List<Map<String, Object>> result = metricServiceDao
        .queryMetricServiceHistogramsWithAggsFields(queryVO, true, aggsFields);
    result.forEach(item -> {
      item.put("serviceId", queryVO.getServiceId());
      item.put("networkId", queryVO.getNetworkId());
      item.put("networkGroupId", queryVO.getNetworkGroupId());
    });

    return result;
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

  private void sortMetricResult(List<Map<String, Object>> result, String sortProperty,
      String sortDirection) {
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
  }

  private Map<String, Object> mergeMetrics(String serviceId, String networkId,
      String networkGroupId, List<MetricServiceDataRecordDO> dataRecord, List<String> metrics) {
    Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    item.put("serviceId", serviceId);
    item.put("networkId", networkId);
    item.put("networkGroupId", networkGroupId);

    if (metrics.contains(CenterConstants.METRIC_NPM_FLOW) || metrics.contains("ALL")) {
      item.put("bytepsPeak", dataRecord.isEmpty() ? 0L
          : dataRecord.stream().mapToLong(MetricServiceDataRecordDO::getBytepsPeak).max());
      item.put("packetpsPeak", dataRecord.isEmpty() ? 0L
          : dataRecord.stream().mapToLong(MetricServiceDataRecordDO::getPacketpsPeak).max());
      item.put("totalBytes",
          dataRecord.stream().mapToLong(MetricServiceDataRecordDO::getTotalBytes).sum());
      item.put("totalPackets",
          dataRecord.stream().mapToLong(MetricServiceDataRecordDO::getTotalPackets).sum());
      item.put("downstreamBytes",
          dataRecord.stream().mapToLong(MetricServiceDataRecordDO::getDownstreamBytes).sum());
      item.put("downstreamPackets",
          dataRecord.stream().mapToLong(MetricServiceDataRecordDO::getDownstreamPackets).sum());
      item.put("upstreamBytes",
          dataRecord.stream().mapToLong(MetricServiceDataRecordDO::getUpstreamBytes).sum());
      item.put("upstreamPackets",
          dataRecord.stream().mapToLong(MetricServiceDataRecordDO::getUpstreamPackets).sum());
      item.put("filterDiscardBytes",
          dataRecord.stream().mapToLong(MetricServiceDataRecordDO::getFilterDiscardBytes).sum());
      item.put("filterDiscardPackets",
          dataRecord.stream().mapToLong(MetricServiceDataRecordDO::getFilterDiscardPackets).sum());
      item.put("overloadDiscardBytes",
          dataRecord.stream().mapToLong(MetricServiceDataRecordDO::getOverloadDiscardBytes).sum());
      item.put("overloadDiscardPackets", dataRecord.stream()
          .mapToLong(MetricServiceDataRecordDO::getOverloadDiscardPackets).sum());
      item.put("deduplicationBytes",
          dataRecord.stream().mapToLong(MetricServiceDataRecordDO::getDeduplicationBytes).sum());
      item.put("deduplicationPackets",
          dataRecord.stream().mapToLong(MetricServiceDataRecordDO::getDeduplicationPackets).sum());
    }


    if (metrics.contains(CenterConstants.METRIC_NPM_FRAGMENT) || metrics.contains("ALL")) {
      item.put("fragmentTotalBytes",
          dataRecord.stream().mapToLong(MetricServiceDataRecordDO::getFragmentTotalBytes).sum());
      item.put("fragmentTotalPackets",
          dataRecord.stream().mapToLong(MetricServiceDataRecordDO::getFragmentTotalPackets).sum());
    }

    if (metrics.contains(CenterConstants.METRIC_NPM_TCP) || metrics.contains("ALL")) {
      item.put("tcpSynPackets",
          dataRecord.stream().mapToLong(MetricServiceDataRecordDO::getTcpSynPackets).sum());
      item.put("tcpClientSynPackets",
          dataRecord.stream().mapToLong(MetricServiceDataRecordDO::getTcpClientSynPackets).sum());
      item.put("tcpServerSynPackets",
          dataRecord.stream().mapToLong(MetricServiceDataRecordDO::getTcpServerSynPackets).sum());
      item.put("tcpSynAckPackets",
          dataRecord.stream().mapToLong(MetricServiceDataRecordDO::getTcpSynAckPackets).sum());
      item.put("tcpSynRstPackets",
          dataRecord.stream().mapToLong(MetricServiceDataRecordDO::getTcpSynRstPackets).sum());
      item.put("tcpEstablishedFailCounts", dataRecord.stream()
          .mapToLong(MetricServiceDataRecordDO::getTcpEstablishedFailCounts).sum());
      item.put("tcpEstablishedSuccessCounts", dataRecord.stream()
          .mapToLong(MetricServiceDataRecordDO::getTcpEstablishedSuccessCounts).sum());
      item.put("tcpEstablishedTimeAvg",
          dataRecord.isEmpty() ? 0L
              : dataRecord.stream().mapToLong(MetricServiceDataRecordDO::getTcpEstablishedTimeAvg)
                  .average());
      item.put("tcpZeroWindowPackets",
          dataRecord.stream().mapToLong(MetricServiceDataRecordDO::getTcpZeroWindowPackets).sum());
    }

    if (metrics.contains(CenterConstants.METRIC_NPM_SESSION) || metrics.contains("ALL")) {
      item.put("activeSessions",
          dataRecord.stream().mapToLong(MetricServiceDataRecordDO::getActiveSessions).sum());
      item.put("concurrentSessions",
          dataRecord.stream().mapToLong(MetricServiceDataRecordDO::getConcurrentSessions).sum());
      item.put("concurrentTcpSessions",
          dataRecord.stream().mapToLong(MetricServiceDataRecordDO::getConcurrentTcpSessions).sum());
      item.put("concurrentUdpSessions",
          dataRecord.stream().mapToLong(MetricServiceDataRecordDO::getConcurrentUdpSessions).sum());
      item.put("concurrentArpSessions",
          dataRecord.stream().mapToLong(MetricServiceDataRecordDO::getConcurrentArpSessions).sum());
      item.put("concurrentIcmpSessions", dataRecord.stream()
          .mapToLong(MetricServiceDataRecordDO::getConcurrentIcmpSessions).sum());
      item.put("establishedSessions",
          dataRecord.stream().mapToLong(MetricServiceDataRecordDO::getEstablishedSessions).sum());
      item.put("destroyedSessions",
          dataRecord.stream().mapToLong(MetricServiceDataRecordDO::getDestroyedSessions).sum());
      item.put("establishedTcpSessions", dataRecord.stream()
          .mapToLong(MetricServiceDataRecordDO::getEstablishedTcpSessions).sum());
      item.put("establishedUdpSessions", dataRecord.stream()
          .mapToLong(MetricServiceDataRecordDO::getEstablishedUdpSessions).sum());
      item.put("establishedIcmpSessions", dataRecord.stream()
          .mapToLong(MetricServiceDataRecordDO::getEstablishedIcmpSessions).sum());
      item.put("establishedOtherSessions", dataRecord.stream()
          .mapToLong(MetricServiceDataRecordDO::getEstablishedOtherSessions).sum());
      item.put("establishedUpstreamSessions", dataRecord.stream()
          .mapToLong(MetricServiceDataRecordDO::getEstablishedUpstreamSessions).sum());
      item.put("establishedDownstreamSessions", dataRecord.stream()
          .mapToLong(MetricServiceDataRecordDO::getEstablishedDownstreamSessions).sum());
    }


    if (metrics.contains(CenterConstants.METRIC_NPM_PERFORMANCE) || metrics.contains("ALL")) {
      item.put("tcpClientNetworkLatency", dataRecord.stream()
          .mapToLong(MetricServiceDataRecordDO::getTcpClientNetworkLatency).sum());
      item.put("tcpClientNetworkLatencyCounts", dataRecord.stream()
          .mapToLong(MetricServiceDataRecordDO::getTcpClientNetworkLatencyCounts).sum());
      item.put("tcpClientNetworkLatencyAvg",
          dataRecord.isEmpty() ? 0D
              : dataRecord.stream()
                  .mapToDouble(MetricServiceDataRecordDO::getTcpClientNetworkLatencyAvg).average());
      item.put("tcpServerNetworkLatency", dataRecord.stream()
          .mapToLong(MetricServiceDataRecordDO::getTcpServerNetworkLatency).sum());
      item.put("tcpServerNetworkLatencyCounts", dataRecord.stream()
          .mapToLong(MetricServiceDataRecordDO::getTcpServerNetworkLatencyCounts).sum());
      item.put("tcpServerNetworkLatencyAvg",
          dataRecord.isEmpty() ? 0D
              : dataRecord.stream()
                  .mapToDouble(MetricServiceDataRecordDO::getTcpServerNetworkLatencyAvg).average());
      item.put("serverResponseLatency", dataRecord.stream()
          .mapToDouble(MetricServiceDataRecordDO::getServerResponseLatency).sum());
      item.put("serverResponseLatencyCounts", dataRecord.stream()
          .mapToDouble(MetricServiceDataRecordDO::getServerResponseLatencyCounts).sum());
      item.put("serverResponseLatencyAvg",
          dataRecord.isEmpty() ? 0L
              : dataRecord.stream()
                  .mapToDouble(MetricServiceDataRecordDO::getServerResponseLatencyAvg).average());
      item.put("serverResponseFastCounts", dataRecord.stream()
          .mapToDouble(MetricServiceDataRecordDO::getServerResponseFastCounts).sum());
      item.put("serverResponseNormalCounts", dataRecord.stream()
          .mapToDouble(MetricServiceDataRecordDO::getServerResponseNormalCounts).sum());
      item.put("serverResponseTimeoutCounts", dataRecord.stream()
          .mapToDouble(MetricServiceDataRecordDO::getServerResponseTimeoutCounts).sum());
      item.put("serverResponseLatencyPeak",
          dataRecord.isEmpty() ? 0D
              : dataRecord.stream()
                  .mapToDouble(MetricServiceDataRecordDO::getServerResponseLatencyPeak).max());
      item.put("tcpClientRetransmissionPackets", dataRecord.stream()
          .mapToDouble(MetricServiceDataRecordDO::getTcpClientRetransmissionPackets).sum());
      item.put("tcpClientPackets",
          dataRecord.stream().mapToDouble(MetricServiceDataRecordDO::getTcpClientPackets).sum());
      item.put("tcpClientRetransmissionRate",
          dataRecord.isEmpty() ? 0D
              : dataRecord.stream()
                  .mapToDouble(MetricServiceDataRecordDO::getTcpClientRetransmissionRate)
                  .average());
      item.put("tcpServerRetransmissionPackets", dataRecord.stream()
          .mapToDouble(MetricServiceDataRecordDO::getTcpServerRetransmissionPackets).sum());
      item.put("tcpServerPackets",
          dataRecord.stream().mapToDouble(MetricServiceDataRecordDO::getTcpServerPackets).sum());
      item.put("tcpServerRetransmissionRate",
          dataRecord.isEmpty() ? 0D
              : dataRecord.stream()
                  .mapToDouble(MetricServiceDataRecordDO::getTcpServerRetransmissionRate)
                  .average());
      double tcpRetransmissionRate = 0;
      long tcpPackets = dataRecord.stream()
          .mapToLong(MetricServiceDataRecordDO::getTcpClientPackets).sum()
          + dataRecord.stream().mapToLong(MetricServiceDataRecordDO::getTcpClientSynPackets).sum();
      if (tcpPackets > 0) {
        tcpRetransmissionRate = (dataRecord.stream()
            .mapToDouble(MetricServiceDataRecordDO::getTcpClientRetransmissionPackets).sum()
            + dataRecord.stream()
                .mapToDouble(MetricServiceDataRecordDO::getTcpServerRetransmissionPackets).sum())
            / (double) tcpPackets;
      }
      item.put("tcpRetransmissionRate", new BigDecimal(String.valueOf(tcpRetransmissionRate))
          .setScale(SCALE_COUNTS, RoundingMode.HALF_UP));
      item.put("tcpClientZeroWindowPackets", dataRecord.stream()
          .mapToDouble(MetricServiceDataRecordDO::getTcpClientZeroWindowPackets).sum());
      item.put("tcpServerZeroWindowPackets", dataRecord.stream()
          .mapToDouble(MetricServiceDataRecordDO::getTcpServerZeroWindowPackets).sum());
    }
    if (metrics.contains(CenterConstants.METRIC_NPM_UNIQUE_IP_COUNTS) || metrics.contains("ALL")) {
      item.put("uniqueIpCounts",
          dataRecord.stream().mapToDouble(MetricServiceDataRecordDO::getUniqueIpCounts).sum());
    }
    return item;
  }

  private Map<String, Object> metricService2Map(MetricServiceDataRecordDO dataRecord,
      List<String> metrics) {
    Map<String, Object> item = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

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

}
