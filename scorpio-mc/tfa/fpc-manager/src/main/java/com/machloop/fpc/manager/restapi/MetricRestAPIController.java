package com.machloop.fpc.manager.restapi;

import java.io.IOException;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.eclipsesource.v8.V8ScriptExecutionException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.alpha.webapp.system.service.SystemServerIpService;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.helper.Spl2SqlHelper;
import com.machloop.fpc.manager.metric.service.MetricFlowlogService;
import com.machloop.fpc.manager.metric.service.MetricNetworkService;
import com.machloop.fpc.manager.metric.service.MetricService;
import com.machloop.fpc.manager.metric.service.MetricServiceService;
import com.machloop.fpc.manager.metric.vo.MetricQueryVO;
import com.machloop.fpc.manager.restapi.vo.RestAPIResultVO;
import com.machloop.fpc.manager.system.bo.DeviceNetifBO;
import com.machloop.fpc.manager.system.service.DeviceNetifService;
import com.machloop.fpc.npm.appliance.bo.NetworkBO;
import com.machloop.fpc.npm.appliance.bo.ServiceBO;
import com.machloop.fpc.npm.appliance.service.NetworkService;
import com.machloop.fpc.npm.appliance.service.ServiceService;

/**
 * @author guosk
 *
 * create at 2021年3月24日, fpc-manager
 */
@RestController
@RequestMapping("/restapi/fpc-v1/metric")
public class MetricRestAPIController {

  private static final Logger LOGGER = LoggerFactory.getLogger(MetricRestAPIController.class);

  private static String serverAddress;

  private static final Range<Integer> RANGE_TOP_NUMBER = Range.closed(1, 100);

  private static final Range<Double> INTERVAL_ONE_MINUTE = Range.openClosed(0d, 1d);
  private static final Range<Double> INTERVAL_FIVE_MINUTE = Range.openClosed(1d, 24d);
  private static final Range<Double> INTERVAL_ONE_HOUR = Range.openClosed(24d, 168d);

  private static final String DEFAULT_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ssXXX";

  private static final int RESULT_LIMIT = 10000;

  private static final String SORT_FIELD = "total_bytes";
  private static final String SORT_DIRECTION = "desc";

  private static final List<Integer> INTERVALS = Lists.newArrayList(Constants.ONE_MINUTE_SECONDS,
      Constants.FIVE_MINUTE_SECONDS, Constants.ONE_HOUR_SECONDS);

  @Autowired
  private MetricService metricService;

  @Autowired
  private MetricFlowlogService metricFlowlogService;

  @Autowired
  private MetricNetworkService metricNetworkService;

  @Autowired
  private MetricServiceService metricServiceService;

  @Autowired
  private NetworkService networkService;

  @Autowired
  private ServiceService serviceService;

  @Autowired
  private DeviceNetifService deviceNetifService;

  @Autowired
  private SystemServerIpService systemServerIpService;

  @Autowired
  private Spl2SqlHelper spl2SqlHelper;

  @GetMapping("/networks")
  @RestApiSecured
  public RestAPIResultVO queryMetricNetworks(MetricQueryVO queryVO) {
    if (StringUtils.isAnyBlank(queryVO.getStartTime(), queryVO.getEndTime())) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("查询时间段不能为空")
          .build();
    }

    try {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    } catch (DateTimeParseException e) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("时间格式不合法")
          .build();
    }

    if (queryVO.getStartTimeDate().compareTo(queryVO.getEndTimeDate()) >= 0
        || DateUtils.afterSecondDate(queryVO.getStartTimeDate(), Constants.ONE_DAY_SECONDS * 7)
            .compareTo(queryVO.getEndTimeDate()) < 0) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg("查询时间范围不合法，有效范围在一周内").build();
    }

    if (!INTERVALS.contains(queryVO.getInterval())) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg("统计间隔不合法（可选：60/300/3600）").build();
    }

    if (StringUtils.equals(queryVO.getNetworkId(), "all")) {
      queryVO.setNetworkId("");
    }

    if (StringUtils.isNotBlank(queryVO.getNetworkId())) {
      NetworkBO network = networkService.queryNetwork(queryVO.getNetworkId());
      if (StringUtils.isBlank(network.getId())) {
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("网络ID不存在")
            .build();
      }
    }

    List<Map<String, Object>> metricNetworks = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isBlank(queryVO.getNetworkId())) {
      metricNetworks = metricNetworkService.queryALlNetworkHistograms(queryVO, false);
    } else {
      metricNetworks = metricNetworkService.queryMetricNetworkHistograms(queryVO, false);
    }

    if (metricNetworks.size() > RESULT_LIMIT) {
      metricNetworks = metricNetworks.subList(0, RESULT_LIMIT);
    }

    metricNetworks.forEach(item -> {
      item.put("timestamp", DateUtils.toStringFormat((Date) item.get("timestamp"),
          DEFAULT_DATETIME_PATTERN, ZoneId.systemDefault()));
    });

    return RestAPIResultVO.resultSuccess(metricNetworks);
  }

  @GetMapping("/services")
  @RestApiSecured
  public RestAPIResultVO queryMetricServices(MetricQueryVO queryVO) {
    if (StringUtils.isAnyBlank(queryVO.getStartTime(), queryVO.getEndTime())) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("查询时间段不能为空")
          .build();
    }

    try {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    } catch (DateTimeParseException e) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("时间格式不合法")
          .build();
    }

    if (queryVO.getStartTimeDate().compareTo(queryVO.getEndTimeDate()) >= 0
        || DateUtils.afterSecondDate(queryVO.getStartTimeDate(), Constants.ONE_DAY_SECONDS * 7)
            .compareTo(queryVO.getEndTimeDate()) < 0) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg("查询时间范围不合法，有效范围在一周内").build();
    }

    if (!INTERVALS.contains(queryVO.getInterval())) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg("统计间隔不合法（可选：60/300/3600）").build();
    }

    if (StringUtils.isAnyBlank(queryVO.getNetworkId(), queryVO.getServiceId())) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("网络ID、业务ID不能为空")
          .build();
    }

    NetworkBO network = networkService.queryNetwork(queryVO.getNetworkId());
    if (StringUtils.isBlank(network.getId())) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("网络ID不存在")
          .build();
    }

    ServiceBO service = serviceService.queryService(queryVO.getServiceId());
    if (StringUtils.isBlank(service.getId())) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("业务ID不存在")
          .build();
    }

    List<Map<String, Object>> metricServices = metricServiceService
        .queryMetricServiceHistograms(queryVO, false);
    if (metricServices.size() > RESULT_LIMIT) {
      metricServices = metricServices.subList(0, RESULT_LIMIT);
    }

    metricServices.forEach(item -> {
      item.put("timestamp", DateUtils.toStringFormat((Date) item.get("timestamp"),
          DEFAULT_DATETIME_PATTERN, ZoneId.systemDefault()));
    });

    return RestAPIResultVO.resultSuccess(metricServices);
  }

  @GetMapping("/netifs")
  @RestApiSecured
  public RestAPIResultVO queryMetricNetifs(MetricQueryVO queryVO,
      @RequestParam(defaultValue = "all", required = false) String netifName) {
    if (StringUtils.isAnyBlank(queryVO.getStartTime(), queryVO.getEndTime())) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("查询时间段不能为空")
          .build();
    }

    try {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    } catch (DateTimeParseException e) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("时间格式不合法")
          .build();
    }

    if (queryVO.getStartTimeDate().compareTo(queryVO.getEndTimeDate()) >= 0
        || DateUtils.afterSecondDate(queryVO.getStartTimeDate(), Constants.ONE_DAY_SECONDS * 7)
            .compareTo(queryVO.getEndTimeDate()) < 0) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg("查询时间范围不合法，有效范围在一周内").build();
    }

    if (!INTERVALS.contains(queryVO.getInterval())) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg("统计间隔不合法（可选：60/300/3600）").build();
    }

    if (!StringUtils.equals(netifName, "all")) {
      List<DeviceNetifBO> netifs = deviceNetifService.queryDeviceNetifsByCategories(
          FpcConstants.DEVICE_NETIF_CATEGORY_TRANSMIT, FpcConstants.DEVICE_NETIF_CATEGORY_INGEST);
      List<String> netifNames = netifs.stream().map(netif -> netif.getName())
          .collect(Collectors.toList());
      if (!netifNames.contains(netifName)) {
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("业务接口不存在")
            .build();
      }
    }

    List<Map<String, Object>> metricNetifs = metricService.queryMetricNetifHistograms(queryVO,
        netifName, false);
    if (metricNetifs.size() > RESULT_LIMIT) {
      metricNetifs = metricNetifs.subList(0, RESULT_LIMIT);
    }

    metricNetifs.forEach(item -> {
      item.put("timestamp", DateUtils.toStringFormat((Date) item.get("timestamp"),
          DEFAULT_DATETIME_PATTERN, ZoneId.systemDefault()));
    });

    return RestAPIResultVO.resultSuccess(metricNetifs);
  }

  @GetMapping("/applications/as-ranking")
  @RestApiSecured
  public RestAPIResultVO queryApplicationRanking(MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "20") int topNumber) {
    if (StringUtils.isAnyBlank(queryVO.getStartTime(), queryVO.getEndTime())) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("查询时间段不能为空")
          .build();
    }

    try {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    } catch (DateTimeParseException e) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("时间格式不合法")
          .build();
    }

    int interval = computeInterval(queryVO.getStartTimeDate(), queryVO.getEndTimeDate());
    if (interval == 0) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg("统计时间范围不合法，有效范围在一周内").build();
    }
    queryVO.setInterval(interval);

    if (!RANGE_TOP_NUMBER.contains(topNumber)) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("Top值不合法")
          .build();
    }

    queryVO.setCount(topNumber);
    return RestAPIResultVO
        .resultSuccess(metricService.countMetricApplications(queryVO, SORT_FIELD, SORT_DIRECTION));
  }

  @GetMapping("/l7-protocols/as-ranking")
  @RestApiSecured
  public RestAPIResultVO queryL7ProtocolRanking(MetricQueryVO queryVO,
      @RequestParam(required = false, defaultValue = "20") int topNumber) {
    if (StringUtils.isAnyBlank(queryVO.getStartTime(), queryVO.getEndTime())) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("查询时间段不能为空")
          .build();
    }

    try {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    } catch (DateTimeParseException e) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("时间格式不合法")
          .build();
    }

    int interval = computeInterval(queryVO.getStartTimeDate(), queryVO.getEndTimeDate());
    if (interval == 0) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg("统计时间范围不合法，有效范围在一周内").build();
    }
    queryVO.setInterval(interval);

    if (!RANGE_TOP_NUMBER.contains(topNumber)) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("Top值不合法")
          .build();
    }

    queryVO.setCount(topNumber);
    return RestAPIResultVO
        .resultSuccess(metricService.countMetricL7Protocols(queryVO, SORT_FIELD, SORT_DIRECTION));
  }

  @GetMapping("/locations")
  @RestApiSecured
  public RestAPIResultVO queryMetricLocations(MetricQueryVO queryVO) {
    // 校验参数
    Map<String, Object> checkResult = checkParams(queryVO);
    if (MapUtils.isNotEmpty(checkResult)) {
      return new RestAPIResultVO.Builder(MapUtils.getIntValue(checkResult, "code"))
          .msg(MapUtils.getString(checkResult, "msg")).build();
    }

    Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (StringUtils.isBlank(serverAddress)) {
      serverAddress = systemServerIpService.getServerIp();
    }
    resultMap.put("probeId", serverAddress);
    List<Map<String, Object>> metricList = Lists.newArrayListWithCapacity(0);
    if (StringUtils.isBlank(queryVO.getDsl())) {
      metricList = metricService.queryMetricLocationRawdatas(queryVO);
    } else {
      metricList = metricFlowlogService.queryMetricLocations(queryVO, SORT_FIELD, SORT_DIRECTION);
    }
    resultMap.put("data", metricList);

    return RestAPIResultVO.resultSuccess(resultMap);
  }

  @GetMapping("/applications")
  @RestApiSecured
  public RestAPIResultVO queryMetricApplications(MetricQueryVO queryVO) {
    // 校验参数
    Map<String, Object> checkResult = checkParams(queryVO);
    if (MapUtils.isNotEmpty(checkResult)) {
      return new RestAPIResultVO.Builder(MapUtils.getIntValue(checkResult, "code"))
          .msg(MapUtils.getString(checkResult, "msg")).build();
    }

    Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (StringUtils.isBlank(serverAddress)) {
      serverAddress = systemServerIpService.getServerIp();
    }
    resultMap.put("probeId", serverAddress);
    List<Map<String, Object>> metricList = Lists.newArrayListWithCapacity(0);
    if (StringUtils.isBlank(queryVO.getDsl())) {
      metricList = metricService.queryMetricApplicationRawdatas(queryVO);
    } else {
      List<Map<String, Object>> filterContents = Lists.newArrayListWithCapacity(0);
      try {
        filterContents = spl2SqlHelper.getFilterContent(queryVO.getDsl());
      } catch (V8ScriptExecutionException | IOException e) {
        LOGGER.warn("failed to convert dsl.", e);
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("解析DSL表达式失败")
            .build();
      }
      int type = 2;
      for (Map<String, Object> fieldMap : filterContents) {
        String field = MapUtils.getString(fieldMap, "field");
        if (StringUtils.equals(field, "type")) {
          type = MapUtils.getIntValue(fieldMap, "operand");
        }
      }
      metricList = metricFlowlogService.queryMetricApplications(queryVO, SORT_FIELD, SORT_DIRECTION,
          type);
    }
    resultMap.put("data", metricList);

    return RestAPIResultVO.resultSuccess(resultMap);
  }

  @GetMapping("/l7-protocols")
  @RestApiSecured
  public RestAPIResultVO queryMetricL7Protocols(MetricQueryVO queryVO) {
    // 校验参数
    Map<String, Object> checkResult = checkParams(queryVO);
    if (MapUtils.isNotEmpty(checkResult)) {
      return new RestAPIResultVO.Builder(MapUtils.getIntValue(checkResult, "code"))
          .msg(MapUtils.getString(checkResult, "msg")).build();
    }

    Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (StringUtils.isBlank(serverAddress)) {
      serverAddress = systemServerIpService.getServerIp();
    }
    resultMap.put("probeId", serverAddress);
    List<Map<String, Object>> metricList = Lists.newArrayListWithCapacity(0);
    if (StringUtils.isBlank(queryVO.getDsl())) {
      metricList = metricService.queryMetricL7ProtocolRawdatas(queryVO);
    } else {
      metricList = metricFlowlogService.queryMetricL7Protocols(queryVO, SORT_FIELD, SORT_DIRECTION);
    }
    resultMap.put("data", metricList);

    return RestAPIResultVO.resultSuccess(resultMap);
  }

  @GetMapping("/ports")
  @RestApiSecured
  public RestAPIResultVO queryMetricPorts(MetricQueryVO queryVO) {
    // 校验参数
    Map<String, Object> checkResult = checkParams(queryVO);
    if (MapUtils.isNotEmpty(checkResult)) {
      return new RestAPIResultVO.Builder(MapUtils.getIntValue(checkResult, "code"))
          .msg(MapUtils.getString(checkResult, "msg")).build();
    }

    Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (StringUtils.isBlank(serverAddress)) {
      serverAddress = systemServerIpService.getServerIp();
    }
    resultMap.put("probeId", serverAddress);
    List<Map<String, Object>> metricList = Lists.newArrayListWithCapacity(0);
    if (StringUtils.isBlank(queryVO.getDsl())) {
      metricList = metricService.queryMetricPortRawdatas(queryVO);
    } else {
      metricList = metricFlowlogService.queryMetricPorts(queryVO, SORT_FIELD, SORT_DIRECTION);
    }
    resultMap.put("data", metricList);

    return RestAPIResultVO.resultSuccess(resultMap);
  }

  @GetMapping("/host-groups")
  @RestApiSecured
  public RestAPIResultVO queryMetricHostgroups(MetricQueryVO queryVO) {
    // 校验参数
    Map<String, Object> checkResult = checkParams(queryVO);
    if (MapUtils.isNotEmpty(checkResult)) {
      return new RestAPIResultVO.Builder(MapUtils.getIntValue(checkResult, "code"))
          .msg(MapUtils.getString(checkResult, "msg")).build();
    }

    Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (StringUtils.isBlank(serverAddress)) {
      serverAddress = systemServerIpService.getServerIp();
    }
    resultMap.put("probeId", serverAddress);
    List<Map<String, Object>> metricList = Lists.newArrayListWithCapacity(0);
    if (StringUtils.isBlank(queryVO.getDsl())) {
      metricList = metricService.queryMetricHostGroupRawdatas(queryVO);
    } else {
      metricList = metricFlowlogService.queryMetricHostGroups(queryVO, SORT_FIELD, SORT_DIRECTION);
    }
    resultMap.put("data", metricList);

    return RestAPIResultVO.resultSuccess(resultMap);
  }

  @GetMapping("/l2-devices")
  @RestApiSecured
  public RestAPIResultVO queryMetricL2Devices(MetricQueryVO queryVO) {
    // 校验参数
    Map<String, Object> checkResult = checkParams(queryVO);
    if (MapUtils.isNotEmpty(checkResult)) {
      return new RestAPIResultVO.Builder(MapUtils.getIntValue(checkResult, "code"))
          .msg(MapUtils.getString(checkResult, "msg")).build();
    }

    Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (StringUtils.isBlank(serverAddress)) {
      serverAddress = systemServerIpService.getServerIp();
    }
    resultMap.put("probeId", serverAddress);
    List<Map<String, Object>> metricList = Lists.newArrayListWithCapacity(0);
    if (StringUtils.isBlank(queryVO.getDsl())) {
      metricList = metricService.queryMetricL2DeviceRawdatas(queryVO);
    } else {
      metricList = metricFlowlogService.queryMetricL2Devices(queryVO, SORT_FIELD, SORT_DIRECTION);
    }
    resultMap.put("data", metricList);

    return RestAPIResultVO.resultSuccess(resultMap);
  }

  @GetMapping("/l3-devices")
  @RestApiSecured
  public RestAPIResultVO queryMetricL3Devices(MetricQueryVO queryVO) {
    // 校验参数
    Map<String, Object> checkResult = checkParams(queryVO);
    if (MapUtils.isNotEmpty(checkResult)) {
      return new RestAPIResultVO.Builder(MapUtils.getIntValue(checkResult, "code"))
          .msg(MapUtils.getString(checkResult, "msg")).build();
    }

    Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (StringUtils.isBlank(serverAddress)) {
      serverAddress = systemServerIpService.getServerIp();
    }
    resultMap.put("probeId", serverAddress);
    List<Map<String, Object>> metricList = Lists.newArrayListWithCapacity(0);
    if (StringUtils.isBlank(queryVO.getDsl())) {
      metricList = metricService.queryMetricL3DeviceRawdatas(queryVO);
    } else {
      metricList = metricFlowlogService.queryMetricL3Devices(queryVO, SORT_FIELD, SORT_DIRECTION);
    }
    resultMap.put("data", metricList);

    return RestAPIResultVO.resultSuccess(resultMap);
  }

  @GetMapping("/ip-conversations")
  @RestApiSecured
  public RestAPIResultVO queryMetricIpConversations(MetricQueryVO queryVO) {
    // 校验参数
    Map<String, Object> checkResult = checkParams(queryVO);
    if (MapUtils.isNotEmpty(checkResult)) {
      return new RestAPIResultVO.Builder(MapUtils.getIntValue(checkResult, "code"))
          .msg(MapUtils.getString(checkResult, "msg")).build();
    }

    Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (StringUtils.isBlank(serverAddress)) {
      serverAddress = systemServerIpService.getServerIp();
    }
    resultMap.put("probeId", serverAddress);
    List<Map<String, Object>> metricList = Lists.newArrayListWithCapacity(0);
    if (StringUtils.isBlank(queryVO.getDsl())) {
      metricList = metricService.queryMetricIpConversationRawdatas(queryVO);
    } else {
      metricList = metricFlowlogService.queryMetricIpConversations(queryVO, SORT_FIELD,
          SORT_DIRECTION);
    }
    resultMap.put("data", metricList);

    return RestAPIResultVO.resultSuccess(resultMap);
  }

  @GetMapping("/dhcps")
  @RestApiSecured
  public RestAPIResultVO queryMetricDhcps(MetricQueryVO queryVO) {
    // 校验参数
    Map<String, Object> checkResult = checkParams(queryVO);
    if (MapUtils.isNotEmpty(checkResult)) {
      return new RestAPIResultVO.Builder(MapUtils.getIntValue(checkResult, "code"))
          .msg(MapUtils.getString(checkResult, "msg")).build();
    }

    Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (StringUtils.isBlank(serverAddress)) {
      serverAddress = systemServerIpService.getServerIp();
    }
    resultMap.put("probeId", serverAddress);
    List<Map<String, Object>> metricList = metricService.queryMetricDhcpRawdatas(queryVO);
    resultMap.put("data", metricList);

    return RestAPIResultVO.resultSuccess(resultMap);
  }

  private Map<String, Object> checkParams(MetricQueryVO queryVO) {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    if (StringUtils.isAnyBlank(queryVO.getStartTime(), queryVO.getEndTime(),
        queryVO.getNetworkId())) {
      result.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
      result.put("msg", "查询时间段、网络ID不能为空");
      return result;
    }

    try {
      queryVO.setStartTimeDate(DateUtils.parseISO8601Date(queryVO.getStartTime()));
      queryVO.setEndTimeDate(DateUtils.parseISO8601Date(queryVO.getEndTime()));
    } catch (DateTimeParseException e) {
      result.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
      result.put("msg", "时间格式不合法");
      return result;
    }

    if (StringUtils.isBlank(queryVO.getDsl()) && !INTERVALS.contains(queryVO.getInterval())) {
      result.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
      result.put("msg", "统计间隔不合法（可选：60/300/3600）");
      return result;
    }

    if (StringUtils.isBlank(queryVO.getDsl()) && (queryVO.getEndTimeDate().getTime()
        - queryVO.getStartTimeDate().getTime()) < queryVO.getInterval() * 1000) {
      result.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
      result.put("msg", "查询时间范围不合法，时间范围应大于统计时间间隔：" + queryVO.getInterval());
      return result;
    }

    NetworkBO network = networkService.queryNetwork(queryVO.getNetworkId());
    if (StringUtils.isBlank(network.getId())) {
      result.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
      result.put("msg", "网络ID不存在");
      return result;
    }

    if (StringUtils.isNotBlank(queryVO.getServiceId())) {
      ServiceBO service = serviceService.queryService(queryVO.getServiceId());
      if (StringUtils.isBlank(service.getId())) {
        result.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
        result.put("msg", "业务ID不存在");
        return result;
      }
    }

    return result;
  }

  private int computeInterval(Date startTime, Date endTime) {
    int interval = 0;

    double hours = ((endTime.getTime() - startTime.getTime())
        / (double) (Constants.ONE_HOUR_SECONDS * 1000));

    if (INTERVAL_ONE_MINUTE.contains(hours)) {
      interval = Constants.ONE_MINUTE_SECONDS;
    } else if (INTERVAL_FIVE_MINUTE.contains(hours)) {
      interval = Constants.FIVE_MINUTE_SECONDS;
    } else if (INTERVAL_ONE_HOUR.contains(hours)) {
      interval = Constants.ONE_HOUR_SECONDS;
    }

    return interval;
  }

}
