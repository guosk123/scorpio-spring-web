package com.machloop.fpc.manager.restapi;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.alpha.common.util.*;
import com.machloop.alpha.common.util.NetworkUtils.IpVersion;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.alpha.webapp.system.bo.UserBO;
import com.machloop.alpha.webapp.system.service.SystemServerIpService;
import com.machloop.alpha.webapp.system.service.UserService;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.appliance.bo.ExternalStorageBO;
import com.machloop.fpc.manager.appliance.bo.FilterTupleBO;
import com.machloop.fpc.manager.appliance.bo.TransmitTaskBO;
import com.machloop.fpc.manager.appliance.controller.TransmitTaskController;
import com.machloop.fpc.manager.appliance.service.ExternalStorageService;
import com.machloop.fpc.manager.appliance.service.TransmitTaskService;
import com.machloop.fpc.manager.knowledge.bo.*;
import com.machloop.fpc.manager.knowledge.service.GeoService;
import com.machloop.fpc.manager.knowledge.service.SaProtocolService;
import com.machloop.fpc.manager.knowledge.service.SaService;
import com.machloop.fpc.manager.restapi.vo.RestAPIResultVO;
import com.machloop.fpc.manager.restapi.vo.TransmitTaskVO;
import com.machloop.fpc.manager.system.bo.DeviceNetifBO;
import com.machloop.fpc.manager.system.bo.MonitorMetricBO;
import com.machloop.fpc.manager.system.service.DeviceNetifService;
import com.machloop.fpc.manager.system.service.SystemMetricService;
import com.machloop.fpc.npm.appliance.bo.LogicalSubnetBO;
import com.machloop.fpc.npm.appliance.bo.NetworkBO;
import com.machloop.fpc.npm.appliance.service.LogicalSubnetService;
import com.machloop.fpc.npm.appliance.service.NetworkService;

import reactor.util.function.Tuple3;

@RestController
@RequestMapping("/restapi/fpc-v1/appliance")
@SuppressWarnings("all")
public class TransmitTaskRestAPIController {

  private static final Logger LOGGER = LoggerFactory.getLogger(TransmitTaskController.class);

  private static final int MAX_NAME_LENGTH = 30;
  private static final int MAX_DESCRIPTION_LENGTH = 255;
  private static final int MAX_FILTER_TUPLE_NUMBER = 5;
  private static final int MIN_FILTER_TUPLE_VLANID = 0;
  private static final int MAX_FILTER_TUPLE_VLANID = 4094;
  private static final int MAX_FILTER_RAW_RULE_NUMBER = 10;
  private static final int MAX_FILTER_RAW_CONDITION_NUMBER = 5;
  private static final Range<Integer> RANGE_REPLAY_RATE_KBPS = Range.closed(128, 10_000_000);
  private static final Range<Integer> RANGE_REPLAY_RATE_PPS = Range.closed(1, 2_000_000);
  private static final Range<Integer> RANGE_FILTER_TUPLE_VLANID = Range.closed(0, 4094);
  private static final Range<Long> RANGE_IPTUNNEL_GRE_KEY = Range.closed(0L, 4294967295L);
  private static final Range<Long> RANGE_IPTUNNEL_VXLAN_VNID = Range.closed(0L, 16777215L);

  private static final List<String> ILLEGAL_SUBCATEGORY = Lists.newArrayList("30", "31");

  private static final String DATA_OLDEST_TIME = "data_oldest_time";

  private static final String ALL_NETWORK = "ALL";

  private static final String REPLAY_RATE_UNIT_KBPS = "0";
  private static final String REPLAY_RATE_UNIT_PPS = "1";

  private static final String FORWARD_POLICY_STORE = "0";
  private static final String FORWARD_POLICY_NO_STORE = "1";

  private static final String FILTER_RAW_CONDITION_TYPE_ASCII = "ascii";
  private static final String FILTER_RAW_CONDITION_TYPE_HEX = "hex";
  private static final String FILTER_RAW_CONDITION_TYPE_REGULAR = "regular";
  private static final String FILTER_RAW_CONDITION_TYPE_CHINESE = "chinese";

  private static final String TASK_OPERATION_STOP = "3";
  private static final String TASK_OPERATION_DELETE = "4";

  private static final int TASK_FILE_INVALIDATION_CODE = 42001;
  private static final int TASK_EXECUTING_CODE = 42002;

  private static final List<String> IP_PROTOCOLS = Lists.newArrayList("TCP", "UDP", "ICMP", "SCTP");

  private static final Pattern MAC_PATTERN = Pattern
      .compile("^[A-Fa-f0-9]{2}([-,:][A-Fa-f0-9]{2}){5}$", Pattern.MULTILINE);
  private static final Pattern ASCII_PATTERN = Pattern.compile("[\\x20-\\x7e]{1,64}$",
      Pattern.MULTILINE);
  private static final Pattern HEX_PATTERN = Pattern.compile("-?[0-9a-fA-F]{2,128}$");

  @Value("${fpc.engine.rest.server.open.port}")
  private String engineRestOpenPort;

  @Autowired
  private TransmitTaskService transmitTaskService;

  @Autowired
  private UserService userService;

  @Autowired
  private DeviceNetifService deviceNetifService;

  @Autowired
  private SystemMetricService monitorMetricService;

  @Autowired
  private ExternalStorageService externalStorageService;

  @Autowired
  private SaProtocolService saProtocolService;

  @Autowired
  private SaService saService;

  @Autowired
  private NetworkService networkService;

  @Autowired
  private LogicalSubnetService logicalSubnetService;

  @Autowired
  private GeoService geoService;

  @Autowired
  private SystemServerIpService systemServerIpService;

  @Value("${file.js.regex.path}")
  private String regexPath;

  @GetMapping("/l7-protocols")
  @RestApiSecured
  public RestAPIResultVO queryL7Protocols() {
    List<Map<String, String>> protocols = saProtocolService.queryProtocols();

    List<Map<String, Object>> protocolList = protocols.stream().map(protocol -> {
      Map<String, Object> protocolMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      protocolMap.put("id", protocol.get("protocolId"));
      protocolMap.put("name", protocol.get("nameText"));

      return protocolMap;
    }).collect(Collectors.toList());

    return RestAPIResultVO.resultSuccess(protocolList);
  }

  @GetMapping("/applications")
  @RestApiSecured
  public RestAPIResultVO queryAppRules() {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    Tuple3<List<SaCategoryBO>, List<SaSubCategoryBO>,
        List<SaApplicationBO>> rules = saService.queryKnowledgeRules();

    // 分类
    List<
        Map<String, Object>> categorys = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    rules.getT1().forEach(item -> {
      Map<String, Object> category = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      category.put("categoryId", item.getCategoryId());
      category.put("name", item.getNameText());
      categorys.add(category);
    });
    saService.queryCustomCategorys().forEach(item -> {
      Map<String, Object> category = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      category.put("categoryId", item.getCategoryId());
      category.put("name", item.getName());
      categorys.add(category);
    });
    result.put("categorys", categorys);

    // 子分类
    List<Map<String, Object>> subCategorys = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    rules.getT2().forEach(item -> {
      if (!ILLEGAL_SUBCATEGORY.contains(item.getSubCategoryId())) {
        Map<String,
            Object> subCategory = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
        subCategory.put("subCategoryId", item.getSubCategoryId());
        subCategory.put("categoryId", item.getCategoryId());
        subCategory.put("name", item.getNameText());
        subCategorys.add(subCategory);
      }
    });
    saService.queryCustomSubCategorys().forEach(item -> {
      Map<String, Object> subCategory = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      subCategory.put("subCategoryId", item.getSubCategoryId());
      subCategory.put("categoryId", item.getCategoryId());
      subCategory.put("name", item.getName());
      subCategorys.add(subCategory);
    });
    result.put("subCategorys", subCategorys);

    // 应用
    List<Map<String, Object>> applications = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    rules.getT3().forEach(item -> {
      Map<String, Object> application = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      application.put("applicationId", item.getApplicationId());
      application.put("subCategoryId", item.getSubCategoryId());
      application.put("categoryId", item.getCategoryId());
      application.put("name", item.getNameText());
      applications.add(application);
    });
    saService.queryCustomApps().forEach(item -> {
      Map<String, Object> application = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      application.put("applicationId", item.getApplicationId());
      application.put("subCategoryId", item.getSubCategoryId());
      application.put("categoryId", item.getCategoryId());
      application.put("name", item.getName());
      applications.add(application);
    });
    result.put("applications", applications);

    return RestAPIResultVO.resultSuccess(result);
  }

  @GetMapping("/geolocations")
  @RestApiSecured
  public RestAPIResultVO queryGeolocations() {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    Tuple3<List<GeoCountryBO>, List<GeoProvinceBO>,
        List<GeoCityBO>> geolocations = geoService.queryGeolocations();

    List<Map<String, Object>> countryList = geolocations.getT1().stream().map(country -> {
      Map<String, Object> countryMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      countryMap.put("countryId", country.getCountryId());
      countryMap.put("countryName", country.getNameText());

      return countryMap;
    }).collect(Collectors.toList());
    countryList.addAll(geoService.queryCustomCountrys().stream().map(country -> {
      Map<String, Object> countryMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      countryMap.put("countryId", country.getCountryId());
      countryMap.put("countryName", country.getName());

      return countryMap;
    }).collect(Collectors.toList()));
    result.put("countrys", countryList);

    List<Map<String, Object>> provinceList = geolocations.getT2().stream().map(province -> {
      Map<String, Object> provinceMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      provinceMap.put("countryId", province.getCountryId());
      provinceMap.put("provinceId", province.getProvinceId());
      provinceMap.put("provinceName", province.getNameText());

      return provinceMap;
    }).collect(Collectors.toList());
    result.put("provinces", provinceList);

    List<Map<String, Object>> cityList = geolocations.getT3().stream().map(city -> {
      Map<String, Object> cityMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      cityMap.put("countryId", city.getCountryId());
      cityMap.put("provinceId", city.getProvinceId());
      cityMap.put("cityId", city.getCityId());
      cityMap.put("cityName", city.getNameText());

      return cityMap;
    }).collect(Collectors.toList());
    result.put("citys", cityList);

    return RestAPIResultVO.resultSuccess(result);
  }

  @GetMapping("/transmition-tasks/{id}")
  @RestApiSecured
  public RestAPIResultVO queryTransmitTask(
      @PathVariable @NotEmpty(message = "查询任务时传入的id不能为空") String id) {
    TransmitTaskBO transmitTask = transmitTaskService.queryTransmitTask(id);
    if (StringUtils.isBlank(transmitTask.getId())) {
      return new RestAPIResultVO.Builder(FpcConstants.OBJECT_NOT_FOUND_CODE).msg("不存在的任务ID")
          .build();
    }

    Map<String,
        Object> transmitTaskMap = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    transmitTaskMap.put("id", transmitTask.getId());
    transmitTaskMap.put("name", transmitTask.getName());
    transmitTaskMap.put("filterStartTime", transmitTask.getFilterStartTime());
    transmitTaskMap.put("filterEndTime", transmitTask.getFilterEndTime());
    transmitTaskMap.put("filterNetworkId", transmitTask.getFilterNetworkId());
    transmitTaskMap.put("filterPacketFileId", transmitTask.getFilterPacketFileId());
    transmitTaskMap.put("filterConditionType", transmitTask.getFilterConditionType());
    List<FilterTupleBO> filterTuple = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(transmitTask.getFilterTuple())) {
      filterTuple = JsonHelper.deserialize(transmitTask.getFilterTuple(),
          new TypeReference<List<FilterTupleBO>>() {
          }, false);
    }
    transmitTaskMap.put("filterTuple", filterTuple);
    transmitTaskMap.put("filterBpf", transmitTask.getFilterBpf());
    List<List<Map<String, String>>> filterRaw = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(transmitTask.getFilterRaw())) {
      filterRaw = JsonHelper.deserialize(transmitTask.getFilterRaw(),
          new TypeReference<List<List<Map<String, String>>>>() {
          }, false);
    }
    transmitTaskMap.put("filterRaw", filterRaw);
    transmitTaskMap.put("mode", transmitTask.getMode());
    if (StringUtils.equals(transmitTask.getMode(), FpcConstants.TRANSMIT_TASK_MODE_REPLAY)) {
      transmitTaskMap.put("replayNetif", transmitTask.getReplayNetif());
      transmitTaskMap.put("replayRate", transmitTask.getReplayRate());
      transmitTaskMap.put("replayRateUnit", transmitTask.getReplayRateUnit());
      List<Map<String, Object>> replayRule = Lists
          .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      if (StringUtils.isNotBlank(transmitTask.getReplayRule())) {
        replayRule = JsonHelper.deserialize(transmitTask.getReplayRule(),
            new TypeReference<List<Map<String, Object>>>() {
            }, false);
      }
      transmitTaskMap.put("replayRule", replayRule);
      transmitTaskMap.put("forwardAction", transmitTask.getForwardAction());
      Map<String, Object> ipTunnel = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      if (StringUtils.isNotBlank(transmitTask.getIpTunnel())) {
        ipTunnel = JsonHelper.deserialize(transmitTask.getIpTunnel(),
            new TypeReference<Map<String, Object>>() {
            }, false);
      }
      transmitTaskMap.put("ipTunnel", ipTunnel);
    }
    transmitTaskMap.put("state", transmitTask.getState());
    transmitTaskMap.put("executionStartTime", transmitTask.getExecutionStartTime());
    transmitTaskMap.put("executionEndTime", transmitTask.getExecutionEndTime());
    transmitTaskMap.put("executionProgress", transmitTask.getExecutionProgress());
    if (StringUtils.isNotBlank(transmitTask.getExecutionTrace())) {
      transmitTaskMap.put("executionTrace", JsonHelper.deserialize(transmitTask.getExecutionTrace(),
          new TypeReference<Map<String, Object>>() {
          }, false));
    }
    transmitTaskMap.put("description", transmitTask.getDescription());

    return RestAPIResultVO.resultSuccess(transmitTaskMap);
  }

  @PostMapping("/transmition-tasks")
  @RestApiSecured
  public synchronized RestAPIResultVO saveTransmitTask(
      @RequestBody @Validated TransmitTaskVO transmitTaskVO, BindingResult bindingResult,
      HttpServletRequest request) {
    // 检查参数
    Map<String, Object> checkResultMap = checkParameter(bindingResult, transmitTaskVO);
    if (MapUtils.isNotEmpty(checkResultMap)) {
      return new RestAPIResultVO.Builder(MapUtils.getIntValue(checkResultMap, "code"))
          .msg(MapUtils.getString(checkResultMap, "msg")).build();
    }

    // 获取第三方用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    TransmitTaskBO transmitTaskBO = new TransmitTaskBO();
    try {
      BeanUtils.copyProperties(transmitTaskVO, transmitTaskBO);
      transmitTaskBO
          .setFilterNetworkId(StringUtils.defaultIfBlank(transmitTaskBO.getFilterNetworkId(),
              StringUtils.isBlank(transmitTaskVO.getFilterPacketFileId()) ? ALL_NETWORK : ""));
      transmitTaskBO
          .setDescription(StringUtils.defaultIfBlank(transmitTaskVO.getDescription(), ""));
      transmitTaskBO.setFilterBpf(StringUtils.defaultIfBlank(transmitTaskVO.getFilterBpf(), ""));
      transmitTaskBO.setFilterTuple(CollectionUtils.isEmpty(transmitTaskVO.getFilterTuple()) ? ""
          : JsonHelper.serialize(transmitTaskVO.getFilterTuple(), false));
      transmitTaskBO.setFilterRaw(CollectionUtils.isEmpty(transmitTaskVO.getFilterRaw()) ? ""
          : JsonHelper.serialize(transmitTaskVO.getFilterRaw(), false));
      transmitTaskBO.setReplayRule(CollectionUtils.isEmpty(transmitTaskVO.getReplayRule()) ? ""
          : JsonHelper.serialize(transmitTaskVO.getReplayRule(), false));
      transmitTaskBO.setIpTunnel(MapUtils.isEmpty(transmitTaskVO.getIpTunnel()) ? ""
          : JsonHelper.serialize(transmitTaskVO.getIpTunnel(), false));

      transmitTaskBO = transmitTaskService.saveTransmitTask(transmitTaskBO, userBO.getId(),
          "REST" + "/" + userBO.getFullname() + "/" + userBO.getName());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, transmitTaskBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(transmitTaskBO.getId());
  }

  @PutMapping("/transmition-tasks/{id}/operations")
  @RestApiSecured
  public RestAPIResultVO stopOrDeleteTransmitTask(@PathVariable String id,
      @RequestBody(required = false) Map<String, String> requestMap, HttpServletRequest request) {
    // 没有传操作参数
    if (MapUtils.isEmpty(requestMap)) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("请求参数为空").build();
    }

    // 根据ID查询任务
    TransmitTaskBO transmitTask = transmitTaskService.queryTransmitTask(id);
    if (StringUtils.isBlank(transmitTask.getId())) {
      return new RestAPIResultVO.Builder(FpcConstants.OBJECT_NOT_FOUND_CODE).msg("不存在的任务ID")
          .build();
    }

    String action = requestMap.getOrDefault("action", "");

    // 获取第三方用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    TransmitTaskBO transmitTaskBO = null;
    int auditLogActionCode;
    switch (action) {
      case TASK_OPERATION_STOP:
        auditLogActionCode = LogHelper.AUDIT_LOG_ACTION_STOP;
        transmitTaskBO = transmitTaskService.stopTransmitTask(id);
        break;
      case TASK_OPERATION_DELETE:
        auditLogActionCode = LogHelper.AUDIT_LOG_ACTION_DELETE;
        transmitTaskBO = transmitTaskService.deleteTransmitTask(id, userBO.getId());
        break;
      default:
        // 非法操作
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("不合法的操作")
            .build();
    }

    LogHelper.auditOperate(auditLogActionCode, transmitTaskBO, userBO.getFullname(), userBO.getName());

    return RestAPIResultVO.resultSuccess(id);
  }

  @GetMapping("/transmition-tasks/{id}/files")
  @RestApiSecured
  public RestAPIResultVO downloadTransmitTaskFile(@PathVariable String id,
      HttpServletRequest request) throws UnsupportedEncodingException {
    // 根据ID查询任务
    TransmitTaskBO transmitTask = transmitTaskService.queryTransmitTask(id);
    if (StringUtils.isBlank(transmitTask.getId())) {
      return new RestAPIResultVO.Builder(FpcConstants.OBJECT_NOT_FOUND_CODE).msg("不存在的任务ID")
          .build();
    }

    // 判断任务是否完成
    if (!StringUtils.equals(transmitTask.getState(),
        FpcConstants.APPLIANCE_TRANSMITTASK_STATE_FINISH)) {
      return new RestAPIResultVO.Builder(FpcConstants.OBJECT_NOT_FOUND_CODE)
          .msg("任务未完成或已失败，请检查任务状态").build();
    }

    // 提取缓存文件
    String fileCache = transmitTask.getExecutionCachePath();
    if (StringUtils.isBlank(fileCache)) {
      // 任务正在执行中
      return new RestAPIResultVO.Builder(TASK_EXECUTING_CODE).msg("任务正在执行中，请稍候再下载").build();
    }

    // 如果文件不存在硬盘中，重新执行该任务
    File file = new File(fileCache);
    if (!file.exists()) {
      transmitTask = transmitTaskService.redoTransmitTask(id);

      return new RestAPIResultVO.Builder(TASK_FILE_INVALIDATION_CODE).msg("文件失效正在重新执行任务，请稍后下载")
          .build();
    }

    // 下载前touch被下载文件，使其免于老化
    boolean touch = file.setLastModified(System.currentTimeMillis());
    if (!touch) {
      LOGGER.warn("Fail to touch download file {}.", file.getName());
    }

    // 使用UUID作为凭证，并取token进行签名
    String credential = IdGenerator.generateUUID();
    String token = HotPropertiesHelper.getProperty(Constants.PROP_APPLICATION_TOKEN);

    // 如果文件在硬盘中，重定向请求到下载服务
    String date = DateUtils.toStringISO8601(DateUtils.now());
    String path = String.format(ManagerConstants.REST_ENGINE_TASK_PACKET_DOWNLOAD, id);

    // 拼接文件下载地址
    StringBuilder fileUrl = new StringBuilder();
    fileUrl.append("https://");
    fileUrl.append(systemServerIpService.getServerIp());
    fileUrl.append(":");
    fileUrl.append(engineRestOpenPort);
    fileUrl.append(path);
    fileUrl.append("?X-Machloop-Date=");
    fileUrl.append(URLEncoder.encode(date, StandardCharsets.UTF_8.name()));
    fileUrl.append("&X-Machloop-Credential=");
    fileUrl.append(credential);
    fileUrl.append("&X-Machloop-Signature=");
    fileUrl.append(TokenUtils.makeSignature(token, credential, "GET", date, path));

    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DOWNLOAD, transmitTask, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(fileUrl.toString());
  }

  private Map<String, Object> checkParameter(BindingResult bindingResult,
      TransmitTaskVO transmitTaskVO) {
    Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    // 校验必填参数是否为空
    if (bindingResult.hasErrors()) {
      resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
      resultMap.put("msg", bindingResult.getFieldError().getDefaultMessage());
      return resultMap;
    }

    // 校验任务名称字符长度
    if (transmitTaskVO.getName().length() > MAX_NAME_LENGTH) {
      resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
      resultMap.put("msg", "任务名称最多可输入" + MAX_NAME_LENGTH + "个字符");
      return resultMap;
    }

    // 校验过滤条件开始时间的日期格式
    Date filterStartDate = null;
    try {
      filterStartDate = DateUtils.parseISO8601Date(transmitTaskVO.getFilterStartTime());
    } catch (DateTimeParseException | IllegalArgumentException exception) {
      resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
      resultMap.put("msg", "不合法的过滤开始日期格式");
      return resultMap;
    }

    // 检验最早报文时间早于过滤条件开始时间
    String dataOldestTimeValue = "";
    List<MonitorMetricBO> monitorMetricList = monitorMetricService.queryMonitorMetrics();
    for (MonitorMetricBO monitorMetricBO : monitorMetricList) {
      if (StringUtils.equals(DATA_OLDEST_TIME, monitorMetricBO.getMetricName())) {
        dataOldestTimeValue = monitorMetricBO.getMetricValue();
        break;
      }
    }
    if (StringUtils.isNotBlank(dataOldestTimeValue)) {
      Date dataOldestTime = new Date(Long.parseLong(dataOldestTimeValue) * 1000);
      if (filterStartDate.getTime() < dataOldestTime.getTime()) {
        resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
        resultMap.put("msg", "过滤条件开始时间早于最早报文时间");
        return resultMap;
      }
    }

    // 校验过滤条件结束时间的日期格式
    Date filterEndDate = null;
    try {
      filterEndDate = DateUtils.parseISO8601Date(transmitTaskVO.getFilterEndTime());
    } catch (DateTimeParseException | IllegalArgumentException exception) {
      resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
      resultMap.put("msg", "不合法的过滤结束日期格式");
      return resultMap;
    }

    // 校验过滤条件开始时间早于过滤条件结束时间
    if (filterEndDate.getTime() < filterStartDate.getTime()) {
      resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
      resultMap.put("msg", "过滤条件结束时间早于过滤条件开始时间");
      return resultMap;
    }

    // 校验网络是否存在
    String networkId = transmitTaskVO.getFilterNetworkId();
    if (StringUtils.isNotBlank(networkId)
        && !StringUtils.equalsIgnoreCase(networkId, ALL_NETWORK)) {
      List<String> networkIds = networkService.queryNetworks().stream().map(NetworkBO::getId)
          .collect(Collectors.toList());
      networkIds.addAll(logicalSubnetService.queryLogicalSubnets().stream()
          .map(LogicalSubnetBO::getId).collect(Collectors.toList()));

      if (!networkIds.contains(networkId)) {
        resultMap.put("code", FpcConstants.OBJECT_NOT_FOUND_CODE);
        resultMap.put("msg", "不合法的网络id：" + networkId);
        return resultMap;
      }
    }

    // 校验描述字符长度
    if (StringUtils.isNotBlank(transmitTaskVO.getDescription())
        && transmitTaskVO.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
      resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
      resultMap.put("msg", "描述信息最多可输入" + MAX_DESCRIPTION_LENGTH + "个字符");
      return resultMap;
    }

    if (!StringUtils.equalsAny(transmitTaskVO.getFilterConditionType(),
        FpcConstants.TRANSMIT_TASK_FILTER_TYPE_TUPLE, FpcConstants.TRANSMIT_TASK_FILTER_TYPE_BPF,
        FpcConstants.TRANSMIT_TASK_FILTER_TYPE_MIX)) {
      resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
      resultMap.put("msg", "不合法的过滤条件类型");
      return resultMap;
    }

    // 规则条件校验
    if (StringUtils.equals(FpcConstants.TRANSMIT_TASK_FILTER_TYPE_TUPLE,
        transmitTaskVO.getFilterConditionType())) {
      Map<String, Object> checkTupleResultMap = checkAndPaddingFilterTuple(transmitTaskVO);
      if (MapUtils.isNotEmpty(checkTupleResultMap)) {
        return checkTupleResultMap;
      }
    }

    // 过滤原始内容校验
    Map<String, Object> checkFilterRawMap = checkFilterRaw(transmitTaskVO.getFilterRaw());
    if (MapUtils.isNotEmpty(checkFilterRawMap)) {
      return checkFilterRawMap;
    }


    // 检查任务类型
    switch (transmitTaskVO.getMode()) {
      case FpcConstants.TRANSMIT_TASK_MODE_FILE_PCAP:
        break;
      case FpcConstants.TRANSMIT_TASK_MODE_REPLAY:
        Map<String,
            Object> checkReplayModeResultMap = checkTransmitTaskByReplayMode(
                transmitTaskVO.getReplayRateUnit(), transmitTaskVO.getReplayRate(),
                transmitTaskVO.getReplayNetif(), transmitTaskVO.getReplayRule(),
                transmitTaskVO.getForwardAction(), transmitTaskVO.getIpTunnel());
        if (MapUtils.isNotEmpty(checkReplayModeResultMap)) {
          return checkReplayModeResultMap;
        }
        break;
      case FpcConstants.TRANSMIT_TASK_MODE_FILE_PCAPNG:
        break;
      case FpcConstants.TRANSMIT_TASK_MODE_FILE_EXTERNAL_STORAGE:
        List<ExternalStorageBO> externalStorages = externalStorageService
            .queryExternalStorages(FpcConstants.EXTERNAL_STORAGE_USAGE_TRANSMIT, null);
        if (CollectionUtils.isEmpty(externalStorages) || !externalStorages.stream()
            .anyMatch(item -> StringUtils.equals(item.getState(), Constants.BOOL_YES))) {
          resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
          resultMap.put("msg", "任务存储服务器未配置或为关闭状态");
          return resultMap;
        }
        break;
      default:
        resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
        resultMap.put("msg", "不合法的导出模式");
        return resultMap;
    }

    return resultMap;
  }


  /**
   * 校验内容匹配
   *
   * @param filterRaw
   * @return
   */
  private Map<String, Object> checkFilterRaw(List<List<Map<String, String>>> filterRaws) {
    Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    if (CollectionUtils.isEmpty(filterRaws)) {
      return resultMap;
    }

    if (filterRaws.size() > MAX_FILTER_RAW_RULE_NUMBER) {
      resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
      resultMap.put("msg", "内容匹配规则数量超过" + MAX_FILTER_RAW_RULE_NUMBER + "个");
      return resultMap;
    }

    if (Sets.newHashSet(filterRaws).size() != filterRaws.size()) {
      resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
      resultMap.put("msg", "存在重复的内容匹配规则");
      return resultMap;
    }

    for (List<Map<String, String>> item : filterRaws) {
      if (item.size() > MAX_FILTER_RAW_CONDITION_NUMBER) {
        resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
        resultMap.put("msg", "内容匹配规则数量超过" + MAX_FILTER_RAW_CONDITION_NUMBER + "个");
        return resultMap;
      }

      if (Sets.newHashSet(item).size() != item.size()) {
        resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
        resultMap.put("msg", "内容匹配规则内存在重复的条件");
        return resultMap;
      }

      for (Map<String, String> condition : item) {
        String type = condition.get("type");
        String value = condition.get("value");
        if (value.getBytes().length > 256) {
          resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
          resultMap.put("msg", "抱歉，您输入的内容超过长度限制，请调整您输入内容的长度");
          return resultMap;
        }
        if (StringUtils.equals(type, FILTER_RAW_CONDITION_TYPE_ASCII)) {
          if (!ASCII_PATTERN.matcher(value).matches()) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "不合法的ASCII码");
            return resultMap;
          }
        } else if (StringUtils.equals(type, FILTER_RAW_CONDITION_TYPE_HEX)) {
          if (!HEX_PATTERN.matcher(value).matches() || value.length() % 2 != 0) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "不合法的16进制");
            return resultMap;
          }
        } else if (StringUtils.equals(type, FILTER_RAW_CONDITION_TYPE_REGULAR)) {
          if (StringUtils.startsWithAny(value, "(*UTF8)", "(*UCP)")) {
            value = StringUtils.startsWith(value, "(*UTF8)") ? value.substring(7)
                : value.substring(6);
          }
          String scriptContent = null;
          try {
            scriptContent = FileUtils.readFileToString(new File(regexPath), StandardCharsets.UTF_8);
          } catch (IOException e) {
            throw new RuntimeException(e);
          }
          V8 runtime = V8.createV8Runtime();
          runtime.executeObjectScript(scriptContent);
          V8Array parameters = new V8Array(runtime);
          parameters.push(value);
          boolean isRegExp = runtime.executeBooleanFunction("isRegExp", parameters);
          parameters.release();
          runtime.release();
          if (!isRegExp) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "不合法的正则表达式");
            return resultMap;
          }
        } else if (StringUtils.equals(type, FILTER_RAW_CONDITION_TYPE_CHINESE)) {
          value = new String(value.getBytes(StandardCharsets.UTF_8));
        } else {
          resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
          resultMap.put("msg",
              "不合法的内容匹配条件类型，仅支持：" + FILTER_RAW_CONDITION_TYPE_ASCII + ","
                  + FILTER_RAW_CONDITION_TYPE_HEX + "," + FILTER_RAW_CONDITION_TYPE_REGULAR + ","
                  + FILTER_RAW_CONDITION_TYPE_CHINESE);
          return resultMap;
        }
      }
    }
    return resultMap;
  }

  /**
   * 在任务是重放模式下的参数校验
   *
   * @param replayRateUnit
   * @param replayRate
   * @param replayNetif
   * @param forwardAction
   * @param ipTunnel
   * @return
   */
  private Map<String, Object> checkTransmitTaskByReplayMode(String replayRateUnit, int replayRate,
      String replayNetif, List<Map<String, Object>> replayRule, String forwardAction,
      Map<String, Object> ipTunnel) {

    Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    // 检查重放速率
    if (StringUtils.isBlank(replayRateUnit)) {
      resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
      resultMap.put("msg", "重放速率单位为空");
      return resultMap;
    }

    if (StringUtils.equals(replayRateUnit, REPLAY_RATE_UNIT_KBPS)) {
      // 校验单位是Kbps下的速率范围
      if (!RANGE_REPLAY_RATE_KBPS.contains(replayRate)) {
        resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
        resultMap.put("msg", "不合法的重放速率");
        return resultMap;
      }
    } else if (StringUtils.equals(replayRateUnit, REPLAY_RATE_UNIT_PPS)) {
      // 校验单位是pps下的速率范围
      if (!RANGE_REPLAY_RATE_PPS.contains(replayRate)) {
        resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
        resultMap.put("msg", "不合法的重放速率");
        return resultMap;
      }
    } else {
      // 重放速率单位不存在
      resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
      resultMap.put("msg", "不合法的重放速率单位");
      return resultMap;
    }

    // 校验重放接口是否存在
    if (StringUtils.isBlank(replayNetif)) {
      resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
      resultMap.put("msg", "重放接口的接口名为空");
      return resultMap;
    }

    List<DeviceNetifBO> deviceNetifBOList = deviceNetifService
        .queryDeviceNetifsByCategories(FpcConstants.DEVICE_NETIF_CATEGORY_TRANSMIT);
    List<String> netifNames = deviceNetifBOList.stream().map(netif -> netif.getName())
        .collect(Collectors.toList());
    if (!netifNames.contains(replayNetif)) {
      resultMap.put("code", FpcConstants.OBJECT_NOT_FOUND_CODE);
      resultMap.put("msg", "重放接口不存在或类型不匹配");
      return resultMap;
    }

    if (!StringUtils.equalsAny(forwardAction, FORWARD_POLICY_STORE, FORWARD_POLICY_NO_STORE)) {
      resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
      resultMap.put("msg", "不合法的流量转发策略");
      return resultMap;
    }

    if (MapUtils.isNotEmpty(ipTunnel)) {
      String mode = MapUtils.getString(ipTunnel, "mode");
      if (!StringUtils.equalsAny(mode, FpcConstants.TRANSMIT_TASK_IPTUNNEL_MODE_GRE,
          FpcConstants.TRANSMIT_TASK_IPTUNNEL_MODE_VXLAN)) {
        resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
        resultMap.put("msg", "隧道封装模式不合法");
        return resultMap;
      }

      Map<String,
          Object> params = JsonHelper.deserialize(
              JsonHelper.serialize(MapUtils.getObject(ipTunnel, "params")),
              new TypeReference<Map<String, Object>>() {
              }, false);
      if (MapUtils.isEmpty(params)) {
        resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
        resultMap.put("msg", "隧道封装参数不能为空");
        return resultMap;
      }

      String sourceMac = MapUtils.getString(params, "sourceMac");
      if (StringUtils.isNotBlank(sourceMac) && !MAC_PATTERN.matcher(sourceMac).matches()) {
        resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
        resultMap.put("msg", "隧道封装源MAC不合法");
        return resultMap;
      }

      String destMac = MapUtils.getString(params, "destMac", "");
      if (!MAC_PATTERN.matcher(destMac).matches()) {
        resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
        resultMap.put("msg", "隧道封装目的MAC不合法");
        return resultMap;
      }

      String sourceIp = MapUtils.getString(params, "sourceIp", "");
      String destIp = MapUtils.getString(params, "destIp", "");
      if (!((NetworkUtils.isInetAddress(sourceIp, IpVersion.V4)
          && NetworkUtils.isInetAddress(destIp, IpVersion.V4))
          || (NetworkUtils.isInetAddress(sourceIp, IpVersion.V6)
              && NetworkUtils.isInetAddress(destIp, IpVersion.V6)))) {
        resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
        resultMap.put("msg", "隧道封装源/目的IP必须同时为IPV4/IPV6");
        return resultMap;
      }

      if (StringUtils.equals(sourceIp, destIp)) {
        resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
        resultMap.put("msg", "隧道封装源IP和目的IP不能相同");
        return resultMap;
      }

      if (StringUtils.equals(mode, FpcConstants.TRANSMIT_TASK_IPTUNNEL_MODE_GRE)) {
        Long key = MapUtils.getLong(params, "key", null);
        if (key != null && !RANGE_IPTUNNEL_GRE_KEY.contains(key)) {
          resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
          resultMap.put("msg", "隧道封装KEY值不合法");
          return resultMap;
        }

        String checksum = MapUtils.getString(params, "checksum");
        if (!StringUtils.equalsAny(checksum, Constants.BOOL_YES, Constants.BOOL_NO)) {
          resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
          resultMap.put("msg", "隧道封装是否计算校验和参数不合法");
          return resultMap;
        }
      }

      if (StringUtils.equals(mode, FpcConstants.TRANSMIT_TASK_IPTUNNEL_MODE_VXLAN)) {
        Integer sourcePort = MapUtils.getInteger(params, "sourcePort", null);
        if (sourcePort == null || !NetworkUtils.isInetAddressPort(String.valueOf(sourcePort))) {
          resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
          resultMap.put("msg", "隧道封装源端口不合法");
          return resultMap;
        }

        Integer destPort = MapUtils.getInteger(params, "destPort", null);
        if (destPort == null || !NetworkUtils.isInetAddressPort(String.valueOf(destPort))) {
          resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
          resultMap.put("msg", "隧道封装目的端口不合法");
          return resultMap;
        }

        Long vnid = MapUtils.getLong(params, "vnid", null);
        if (vnid == null || !RANGE_IPTUNNEL_VXLAN_VNID.contains(vnid)) {
          resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
          resultMap.put("msg", "隧道封装VNID值不合法");
          return resultMap;
        }
      }
    }
    if (CollectionUtils.isNotEmpty(replayRule)) {
      Map<String, Object> checkReplayRuleMap = checkReplayRule(replayRule);
      if (MapUtils.isNotEmpty(checkReplayRuleMap)) {
        return checkReplayRuleMap;
      }
    }

    return resultMap;
  }

  private Map<String, Object> checkReplayRule(List<Map<String, Object>> replayRule) {

    Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    List<String> ips = new ArrayList<>();
    List<String> macAddresses = new ArrayList<>();
    List<String> vlanIds = new ArrayList<>();

    for (Map<String, Object> map : replayRule) {
      if (map.get("type").equals("ipChg") && map.get("mode").equals("1")) {
        Map<String, String> ipMap = JsonHelper.deserialize(JsonHelper.serialize(map.get("rule")),
            new TypeReference<Map<String, String>>() {
            }, false);
        if (!ipMap.isEmpty()) {
          ips.addAll(ipMap.values());
        }
      } else if (map.get("type").equals("macChg") && map.get("mode").equals("1")) {
        Map<String, String> macAddressMap = JsonHelper.deserialize(
            JsonHelper.serialize(map.get("rule")), new TypeReference<Map<String, String>>() {
            }, false);
        if (!macAddressMap.isEmpty()) {
          macAddresses.addAll(macAddressMap.values());
        }
      } else {
        Map<String, Object> ruleMap = JsonHelper.deserialize(JsonHelper.serialize(map.get("rule")),
            new TypeReference<Map<String, Object>>() {
            }, false);
        if (!ruleMap.isEmpty()) {
          if (ruleMap.containsKey("vlanId")) {
            vlanIds.add(ruleMap.get("vlanId").toString());
          } else {
            List<Map<String, String>> vlanIdList = JsonHelper.deserialize(
                JsonHelper.serialize(ruleMap.get("vlanIdAlteration")),
                new TypeReference<List<Map<String, String>>>() {
                }, false);
            if (!vlanIdList.isEmpty()) {
              for (Map<String, String> vlanIdMap : vlanIdList) {
                vlanIds.addAll(vlanIdMap.values());
              }
            }
          }
        }
      }
    }
    if (!ips.isEmpty()) {
      for (String ip : ips) {
        if (StringUtils.isNotBlank(ip) && !NetworkUtils.isInetAddress(ip)
            && !NetworkUtils.isCidr(ip)) {
          resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
          resultMap.put("msg", "不合法的IP");
          return resultMap;
        }
      }
    }
    if (!macAddresses.isEmpty()) {
      for (String macAddress : macAddresses) {
        if (macAddress.contains("NOT_")) {
          macAddress = macAddress.substring(4);
        }
        if (StringUtils.isNotBlank(macAddress) && !MAC_PATTERN.matcher(macAddress).matches()) {
          resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
          resultMap.put("msg", "不合法的MAC地址");
          return resultMap;
        }
      }
    }
    if (!vlanIds.isEmpty()) {
      boolean isOk = true;
      for (String vlanIdStr : vlanIds) {
        if (StringUtils.isNotBlank(vlanIdStr)) {
          try {
            int vlanId = Integer.parseInt(vlanIdStr);
            if (vlanId < MIN_FILTER_TUPLE_VLANID || vlanId > MAX_FILTER_TUPLE_VLANID) {
              isOk = false;
            }
          } catch (NumberFormatException e) {
            String[] range = StringUtils.split(vlanIdStr, "-");
            try {
              int vlanId1 = Integer.parseInt(range[0]);
              int vlanId2 = Integer.parseInt(range[1]);
              if (vlanId1 < MIN_FILTER_TUPLE_VLANID || vlanId1 > MAX_FILTER_TUPLE_VLANID) {
                isOk = false;
              }
              if (vlanId2 < MIN_FILTER_TUPLE_VLANID || vlanId2 > MAX_FILTER_TUPLE_VLANID) {
                isOk = false;
              }
              if (vlanId1 >= vlanId2) {
                isOk = false;
              }
            } catch (NumberFormatException nfException) {
              isOk = false;
            } catch (IndexOutOfBoundsException ioobException) {
              isOk = false;
            }
          }
        }
        if (!isOk) {
          resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
          resultMap.put("msg", "不合法的VLANID");
          return resultMap;
        }
      }
    }
    return resultMap;
  }

  /**
   * 校验六元组参数并填充默认值
   *
   * @param sixTupleJson
   * @return
   */
  private Map<String, Object> checkAndPaddingFilterTuple(TransmitTaskVO transmitTaskVO) {

    Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    List<FilterTupleBO> fileterTuple = transmitTaskVO.getFilterTuple();
    if (CollectionUtils.isEmpty(fileterTuple)) {
      return resultMap;
    }

    if (fileterTuple.size() > MAX_FILTER_TUPLE_NUMBER) {
      resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
      resultMap.put("msg", "过滤规则数量超过" + MAX_FILTER_TUPLE_NUMBER + "个");
      return resultMap;
    }

    HashSet<FilterTupleBO> removal = Sets.newHashSet(fileterTuple);
    if (removal.size() < fileterTuple.size()) {
      resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
      resultMap.put("msg", "存在重复的过滤规则");
      return resultMap;
    }

    List<String> vaildL7ProtocolIds = saProtocolService.queryProtocols().stream()
        .map(item -> (String) item.get("protocolId")).collect(Collectors.toList());
    Set<Integer> vaildAppIds = saService.queryAllAppsIdNameMapping().keySet();
    Tuple3<List<GeoCountryBO>, List<GeoProvinceBO>,
        List<GeoCityBO>> geolocations = geoService.queryGeolocations();
    List<String> vaildCountryIds = geolocations.getT1().stream()
        .map(country -> country.getCountryId()).collect(Collectors.toList());
    vaildCountryIds.addAll(geoService.queryCustomCountrys().stream()
        .map(GeoCustomCountryBO::getCountryId).collect(Collectors.toList()));
    List<String> vaildProvinceIds = geolocations.getT2().stream()
        .map(province -> province.getProvinceId()).collect(Collectors.toList());
    List<String> vaildCityIds = geolocations.getT3().stream().map(city -> city.getCityId())
        .collect(Collectors.toList());
    for (FilterTupleBO tuple : fileterTuple) {

      if (tuple.isEmpty()) {
        resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
        resultMap.put("msg", "单个过滤规则内容不能为空");
        return resultMap;
      }

      if (tuple.getIp() != null && (tuple.getSourceIp() != null || tuple.getDestIp() != null)) {
        resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
        resultMap.put("msg", "IP与 （源IP、目的IP）互斥，不可同时存在");
        return resultMap;
      }

      if ((tuple.getPort() != null)
          && ((tuple.getSourcePort() != null) || tuple.getDestPort() != null)) {
        resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
        resultMap.put("msg", "端口 与 （源端口、目的端口） 互斥，不可同时存在");
        return resultMap;
      }

      if (tuple.getCountryId() != null
          && (tuple.getSourceCountryId() != null || tuple.getDestCountryId() != null)) {
        resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
        resultMap.put("msg", "国家 与 （源国家、目的国家） 互斥，不可同时存在");
        return resultMap;
      }

      if (tuple.getMacAddress() != null
          && (tuple.getSourceMacAddress() != null || tuple.getDestMacAddress() != null)) {
        resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
        resultMap.put("msg", "MAC地址 与 （源MAC地址、目的MAC地址） 互斥，不可同时存在");
        return resultMap;
      }

      if (tuple.getProvinceId() != null
          && (tuple.getSourceProvinceId() != null || tuple.getDestProvinceId() != null)) {
        resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
        resultMap.put("msg", "省份 与 （源省份、目的省份） 互斥，不可同时存在");
        return resultMap;
      }

      if (tuple.getCityId() != null
          && (tuple.getSourceCityId() != null || tuple.getDestCityId() != null)) {
        resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
        resultMap.put("msg", "城市 与 （源城市、目的城市） 互斥，不可同时存在");
        return resultMap;
      }

      // IP
      if (tuple.getIp() != null) {
        List<String> ips = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        if (tuple.getIp() instanceof String) {
          ips.add(String.valueOf(tuple.getIp()));
        } else {
          try {
            ips.addAll((List<String>) tuple.getIp());
          } catch (ClassCastException e) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "参数[ip]数据类型不合法");
            return resultMap;
          }
        }
        for (String ip : ips) {
          if (ip.contains("NOT_")) {
            ip = ip.substring(4);
          }
          if (StringUtils.isNotBlank(ip) && !NetworkUtils.isInetAddress(ip)
              && !NetworkUtils.isCidr(ip)) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "不合法的IP：" + ip);
            return resultMap;
          }
        }
      } else {
        tuple.setIp("");
      }

      // 源IP
      if (tuple.getSourceIp() != null) {
        List<String> sourceIps = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        if (tuple.getSourceIp() instanceof String) {
          sourceIps.add(String.valueOf(tuple.getSourceIp()));
        } else {
          try {
            sourceIps.addAll((List<String>) tuple.getSourceIp());
          } catch (ClassCastException e) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "参数[sourceIp]数据类型不合法");
            return resultMap;
          }
        }
        for (String sourceIp : sourceIps) {
          if (sourceIp.contains("NOT_")) {
            sourceIp = sourceIp.substring(4);
          }
          if (StringUtils.isNotBlank(sourceIp) && !NetworkUtils.isInetAddress(sourceIp)
              && !NetworkUtils.isCidr(sourceIp)) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "不合法的源IP：" + sourceIp);
            return resultMap;
          }
        }
      } else {
        tuple.setSourceIp("");
      }

      // 目的IP
      if (tuple.getDestIp() != null) {
        List<String> destIps = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        if (tuple.getDestIp() instanceof String) {
          destIps.add(String.valueOf(tuple.getDestIp()));
        } else {
          try {
            destIps.addAll((List<String>) tuple.getDestIp());
          } catch (ClassCastException e) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "参数[destIp]数据类型不合法");
            return resultMap;
          }
        }
        for (String destIp : destIps) {
          if (destIp.contains("NOT_")) {
            destIp = destIp.substring(4);
          }
          if (StringUtils.isNotBlank(destIp) && !NetworkUtils.isInetAddress(destIp)
              && !NetworkUtils.isCidr(destIp)) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "不合法的目的IP：" + destIp);
            return resultMap;
          }
        }
      } else {
        tuple.setDestIp("");
      }

      // 端口
      if (tuple.getPort() != null) {
        List<String> ports = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        if (tuple.getPort() instanceof String || tuple.getPort() instanceof Integer) {
          ports.add(String.valueOf(tuple.getPort()));
        } else {
          try {
            ports.addAll((List<String>) tuple.getPort());
          } catch (ClassCastException e) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "参数[port]数据类型不合法");
            return resultMap;
          }
        }
        for (String port : ports) {
          if (port.contains("NOT_")) {
            port = port.substring(4);
          }
          if (StringUtils.isNotBlank(port) && !NetworkUtils.isInetAddressPort(port)) {
            String[] range = StringUtils.split(port, "-");
            if (range.length != 2 || !NetworkUtils.isInetAddressPort(range[0])
                || !NetworkUtils.isInetAddressPort(range[1])
                || (Integer.parseInt(range[0]) >= Integer.parseInt(range[1]))) {
              resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
              resultMap.put("msg", "不合法的端口：" + port);
              return resultMap;
            }
          }
        }
      } else {
        tuple.setPort("");
      }

      // 源端口
      if (tuple.getSourcePort() != null) {
        List<String> sourcePorts = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        if (tuple.getSourcePort() instanceof String || tuple.getSourcePort() instanceof Integer) {
          sourcePorts.add(String.valueOf(tuple.getSourcePort()));
        } else {
          try {
            sourcePorts.addAll((List<String>) tuple.getSourcePort());
          } catch (ClassCastException e) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "参数[sourcePort]数据类型不合法");
            return resultMap;
          }
        }
        for (String sourcePort : sourcePorts) {
          if (sourcePort.contains("NOT_")) {
            sourcePort = sourcePort.substring(4);
          }
          if (StringUtils.isNotBlank(sourcePort) && !NetworkUtils.isInetAddressPort(sourcePort)) {
            String[] range = StringUtils.split(sourcePort, "-");
            if (range.length != 2 || !NetworkUtils.isInetAddressPort(range[0])
                || !NetworkUtils.isInetAddressPort(range[1])
                || (Integer.parseInt(range[0]) >= Integer.parseInt(range[1]))) {
              resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
              resultMap.put("msg", "不合法的源端口：" + sourcePort);
              return resultMap;
            }
          }
        }
      } else {
        tuple.setSourcePort("");
      }

      // 目的端口
      if (tuple.getDestPort() != null) {
        List<String> destPorts = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        if (tuple.getDestPort() instanceof String || tuple.getDestPort() instanceof Integer) {
          destPorts.add(String.valueOf(tuple.getDestPort()));
        } else {
          try {
            destPorts.addAll((List<String>) tuple.getDestPort());
          } catch (ClassCastException e) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "参数[destPort]数据类型不合法");
            return resultMap;
          }
        }
        for (String destPort : destPorts) {
          if (destPort.contains("NOT_")) {
            destPort = destPort.substring(4);
          }
          if (StringUtils.isNotBlank(destPort) && !NetworkUtils.isInetAddressPort(destPort)) {
            String[] range = StringUtils.split(destPort, "-");
            if (range.length != 2 || !NetworkUtils.isInetAddressPort(range[0])
                || !NetworkUtils.isInetAddressPort(range[1])
                || (Integer.parseInt(range[0]) >= Integer.parseInt(range[1]))) {
              resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
              resultMap.put("msg", "不合法的目的端口：" + destPort);
              return resultMap;
            }
          }
        }
      } else {
        tuple.setDestPort("");
      }

      // 国家ID
      if (tuple.getCountryId() != null) {
        List<String> countryIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        if (tuple.getCountryId() instanceof String) {
          countryIds.add(String.valueOf(tuple.getCountryId()));
        } else {
          try {
            countryIds.addAll((List<String>) tuple.getCountryId());
          } catch (ClassCastException e) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "参数[countryId]数据类型不合法");
            return resultMap;
          }
        }
        for (String countryId : countryIds) {
          if (countryId.contains("NOT_")) {
            countryId = countryId.substring(4);
          }
          if (StringUtils.isNotBlank(countryId) && !vaildCountryIds.contains(countryId)) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "不合法的国家ID：" + countryId);
            return resultMap;
          }
        }
      } else {
        tuple.setCountryId("");
      }

      // 源国家ID
      if (tuple.getSourceCountryId() != null) {
        List<String> sourceCountryIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        if (tuple.getSourceCountryId() instanceof String) {
          sourceCountryIds.add(String.valueOf(tuple.getSourceCountryId()));
        } else {
          try {
            sourceCountryIds.addAll((List<String>) tuple.getSourceCountryId());
          } catch (ClassCastException e) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "参数[sourceCountryId]数据类型不合法");
            return resultMap;
          }
        }
        for (String sourceCountryId : sourceCountryIds) {
          if (sourceCountryId.contains("NOT_")) {
            sourceCountryId = sourceCountryId.substring(4);
          }
          if (StringUtils.isNotBlank(sourceCountryId)
              && !vaildCountryIds.contains(sourceCountryId)) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "不合法的源国家ID：" + sourceCountryId);
            return resultMap;
          }
        }
      } else {
        tuple.setSourceCountryId("");
      }

      // 目的国家ID
      if (tuple.getDestCountryId() != null) {
        List<String> destCountryIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        if (tuple.getDestCountryId() instanceof String) {
          destCountryIds.add(String.valueOf(tuple.getDestCountryId()));
        } else {
          try {
            destCountryIds.addAll((List<String>) tuple.getDestCountryId());
          } catch (ClassCastException e) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "参数[destCountryId]数据类型不合法");
            return resultMap;
          }
        }
        for (String destCountryId : destCountryIds) {
          if (destCountryId.contains("NOT_")) {
            destCountryId = destCountryId.substring(4);
          }
          if (StringUtils.isNotBlank(destCountryId) && !vaildCountryIds.contains(destCountryId)) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "不合法的目的国家ID：" + destCountryId);
            return resultMap;
          }
        }
      } else {
        tuple.setDestCountryId("");
      }

      // 省份ID
      if (tuple.getProvinceId() != null) {
        List<String> provinceIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        if (tuple.getProvinceId() instanceof String) {
          provinceIds.add(String.valueOf(tuple.getProvinceId()));
        } else {
          try {
            provinceIds.addAll((List<String>) tuple.getProvinceId());
          } catch (ClassCastException e) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "参数[provinceId]数据类型不合法");
            return resultMap;
          }
        }
        for (String provinceId : provinceIds) {
          if (provinceId.contains("NOT_")) {
            provinceId = provinceId.substring(4);
          }
          if (StringUtils.isNotBlank(provinceId) && !vaildProvinceIds.contains(provinceId)) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "不合法的省份ID：" + provinceId);
            return resultMap;
          }
        }
      } else {
        tuple.setProvinceId("");
      }

      // 源省份ID
      if (tuple.getSourceProvinceId() != null) {
        List<String> sourceProvinceIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        if (tuple.getSourceProvinceId() instanceof String) {
          sourceProvinceIds.add(String.valueOf(tuple.getSourceProvinceId()));
        } else {
          try {
            sourceProvinceIds.addAll((List<String>) tuple.getSourceProvinceId());
          } catch (ClassCastException e) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "参数[sourceProvinceId]数据类型不合法");
            return resultMap;
          }
        }
        for (String sourceProvinceId : sourceProvinceIds) {
          if (sourceProvinceId.contains("NOT_")) {
            sourceProvinceId = sourceProvinceId.substring(4);
          }
          if (StringUtils.isNotBlank(sourceProvinceId)
              && !vaildProvinceIds.contains(sourceProvinceId)) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "不合法的源省份ID：" + sourceProvinceId);
            return resultMap;
          }
        }
      } else {
        tuple.setSourceProvinceId("");
      }

      // 目的省份ID
      if (tuple.getDestProvinceId() != null) {
        List<String> destProvinceIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        if (tuple.getDestProvinceId() instanceof String) {
          destProvinceIds.add(String.valueOf(tuple.getDestProvinceId()));
        } else {
          try {
            destProvinceIds.addAll((List<String>) tuple.getDestProvinceId());
          } catch (ClassCastException e) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "参数[destProvinceId]数据类型不合法");
            return resultMap;
          }
        }
        for (String destProvinceId : destProvinceIds) {
          if (destProvinceId.contains("NOT_")) {
            destProvinceId = destProvinceId.substring(4);
          }
          if (StringUtils.isNotBlank(destProvinceId)
              && !vaildProvinceIds.contains(destProvinceId)) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "不合法的目的省份ID：" + destProvinceId);
            return resultMap;
          }
        }
      } else {
        tuple.setDestProvinceId("");
      }

      // 城市ID
      if (tuple.getCityId() != null) {
        List<String> cityIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        if (tuple.getCityId() instanceof String) {
          cityIds.add(String.valueOf(tuple.getCityId()));
        } else {
          try {
            cityIds.addAll((List<String>) tuple.getCityId());
          } catch (ClassCastException e) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "参数[cityId]数据类型不合法");
            return resultMap;
          }
        }
        for (String cityId : cityIds) {
          if (cityId.contains("NOT_")) {
            cityId = cityId.substring(4);
          }
          if (StringUtils.isNotBlank(cityId) && !vaildCityIds.contains(cityId)) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "不合法的城市ID：" + cityId);
            return resultMap;
          }
        }
      } else {
        tuple.setCityId("");
      }

      // 源城市ID
      if (tuple.getSourceCityId() != null) {
        List<String> sourceCityIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        if (tuple.getSourceCityId() instanceof String) {
          sourceCityIds.add(String.valueOf(tuple.getSourceCityId()));
        } else {
          try {
            sourceCityIds.addAll((List<String>) tuple.getSourceCityId());
          } catch (ClassCastException e) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "参数[sourceCityId]数据类型不合法");
            return resultMap;
          }
        }
        for (String sourceCityId : sourceCityIds) {
          if (sourceCityId.contains("NOT_")) {
            sourceCityId = sourceCityId.substring(4);
          }
          if (StringUtils.isNotBlank(sourceCityId) && !vaildCityIds.contains(sourceCityId)) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "不合法的源城市ID：" + sourceCityId);
            return resultMap;
          }
        }
      } else {
        tuple.setSourceCityId("");
      }

      // 目的城市
      if (tuple.getDestCityId() != null) {
        List<String> destCityIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        if (tuple.getDestCityId() instanceof String) {
          destCityIds.add(String.valueOf(tuple.getDestCityId()));
        } else {
          try {
            destCityIds.addAll((List<String>) tuple.getDestCityId());
          } catch (ClassCastException e) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "参数[destCityId]数据类型不合法");
            return resultMap;
          }
        }
        for (String destCityId : destCityIds) {
          if (destCityId.contains("NOT_")) {
            destCityId = destCityId.substring(4);
          }
          if (StringUtils.isNotBlank(destCityId) && !vaildCityIds.contains(destCityId)) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "不合法的目的城市ID：" + destCityId);
            return resultMap;
          }
        }
      } else {
        tuple.setDestCityId("");
      }

      // IP层协议
      if (tuple.getIpProtocol() != null) {
        List<String> ipProtocols = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        if (tuple.getIpProtocol() instanceof String) {
          ipProtocols.add(String.valueOf(tuple.getIpProtocol()));
        } else {
          try {
            ipProtocols.addAll((List<String>) tuple.getIpProtocol());
          } catch (ClassCastException e) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "参数[ipProtocol]数据类型不合法");
            return resultMap;
          }
        }
        for (String ipProtocol : ipProtocols) {
          if (ipProtocol.contains("NOT_")) {
            ipProtocol = ipProtocol.substring(4);
          }
          if (!IP_PROTOCOLS.contains(ipProtocol.toUpperCase())) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "不合法的传输层协议号：" + ipProtocol);
            return resultMap;
          }
        }
      } else {
        tuple.setIpProtocol("");
      }

      // 应用层协议
      if (tuple.getL7ProtocolId() != null) {
        List<String> l7ProtocolIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        if (tuple.getL7ProtocolId() instanceof String) {
          l7ProtocolIds.add(String.valueOf(tuple.getL7ProtocolId()));
        } else {
          try {
            l7ProtocolIds.addAll((List<String>) tuple.getL7ProtocolId());
          } catch (ClassCastException e) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "参数[l7ProtocolId]数据类型不合法");
            return resultMap;
          }
        }
        for (String l7ProtocolId : l7ProtocolIds) {
          if (l7ProtocolId.contains("NOT_")) {
            l7ProtocolId = l7ProtocolId.substring(4);
          }
          if (StringUtils.isNotBlank(l7ProtocolId) && !vaildL7ProtocolIds.contains(l7ProtocolId)) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "不合法的应用层协议号：" + l7ProtocolId);
            return resultMap;
          }
        }
      } else {
        tuple.setL7ProtocolId("");
      }

      // 应用ID
      if (tuple.getApplicationId() != null) {
        List<String> applicationIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        if (tuple.getApplicationId() instanceof String) {
          applicationIds.add(String.valueOf(tuple.getApplicationId()));
        } else {
          try {
            applicationIds.addAll((List<String>) tuple.getApplicationId());
          } catch (ClassCastException e) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "参数[applicationId]数据类型不合法");
            return resultMap;
          }
        }
        for (String applicationId : applicationIds) {
          if (applicationId.contains("NOT_")) {
            applicationId = applicationId.substring(4);
          }
          if (StringUtils.isNotBlank(applicationId)
              && !vaildAppIds.contains(Integer.parseInt(applicationId))) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "不合法的应用id：" + applicationId);
            return resultMap;
          }
        }
      } else {
        tuple.setApplicationId("");
      }

      // MAC地址
      if (tuple.getMacAddress() != null) {
        List<String> macAddresses = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        if (tuple.getMacAddress() instanceof String) {
          macAddresses.add(String.valueOf(tuple.getMacAddress()));
        } else {
          try {
            macAddresses.addAll((List<String>) tuple.getMacAddress());
          } catch (ClassCastException e) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "参数[macAddress]数据类型不合法");
            return resultMap;
          }
        }
        for (String macAddress : macAddresses) {
          if (macAddress.contains("NOT_")) {
            macAddress = macAddress.substring(4);
          }
          if (StringUtils.isNotBlank(macAddress) && !MAC_PATTERN.matcher(macAddress).matches()) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "不合法的Mac地址：" + macAddress);
            return resultMap;
          }
        }
      } else {
        tuple.setMacAddress("");
      }

      // 源MAC地址
      if (tuple.getSourceMacAddress() != null) {
        List<
            String> sourceMacAddresses = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        if (tuple.getSourceMacAddress() instanceof String) {
          sourceMacAddresses.add(String.valueOf(tuple.getSourceMacAddress()));
        } else {
          try {
            sourceMacAddresses.addAll((List<String>) tuple.getSourceMacAddress());
          } catch (ClassCastException e) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "参数[sourceMacAddress]数据类型不合法");
            return resultMap;
          }
        }
        for (String sourceMacAddress : sourceMacAddresses) {
          if (sourceMacAddress.contains("NOT_")) {
            sourceMacAddress = sourceMacAddress.substring(4);
          }
          if (StringUtils.isNotBlank(sourceMacAddress)
              && !MAC_PATTERN.matcher(sourceMacAddress).matches()) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "不合法的源Mac地址：" + sourceMacAddress);
            return resultMap;
          }
        }
      } else {
        tuple.setSourceMacAddress("");
      }

      // 目的MAC地址
      if (tuple.getDestMacAddress() != null) {
        List<String> destMacAddresses = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        if (tuple.getDestMacAddress() instanceof String) {
          destMacAddresses.add(String.valueOf(tuple.getDestMacAddress()));
        } else {
          try {
            destMacAddresses.addAll((List<String>) tuple.getDestMacAddress());
          } catch (ClassCastException e) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "参数[destMacAddress]数据类型不合法");
            return resultMap;
          }
        }
        for (String destMacAddress : destMacAddresses) {
          if (destMacAddress.contains("NOT_")) {
            destMacAddress = destMacAddress.substring(4);
          }
          if (StringUtils.isNotBlank(destMacAddress)
              && !MAC_PATTERN.matcher(destMacAddress).matches()) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "不合法的目的Mac地址：" + destMacAddress);
            return resultMap;
          }
        }
      } else {
        tuple.setDestMacAddress("");
      }

      // vlanId校验
      if (tuple.getVlanId() != null) {
        boolean isOk = true;

        List<String> vlanIdList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        if (tuple.getVlanId() instanceof String) {
          vlanIdList.add(String.valueOf(tuple.getVlanId()));
        } else {
          try {
            vlanIdList.addAll((List<String>) tuple.getVlanId());
          } catch (ClassCastException e) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "参数[vlanId]数据类型不合法");
            return resultMap;
          }
        }

        for (String vlanIdStr : vlanIdList) {
          if (vlanIdStr.contains("NOT_")) {
            vlanIdStr = vlanIdStr.substring(4);
          }
          if (StringUtils.isNotBlank(vlanIdStr)) {
            try {
              int vlanId = Integer.parseInt(vlanIdStr);
              if (vlanId < MIN_FILTER_TUPLE_VLANID || vlanId > MAX_FILTER_TUPLE_VLANID) {
                isOk = false;
              }
            } catch (NumberFormatException e) {
              String[] range = StringUtils.split(vlanIdStr, "-");
              try {
                int vlanId1 = Integer.parseInt(range[0]);
                int vlanId2 = Integer.parseInt(range[1]);
                if (vlanId1 < MIN_FILTER_TUPLE_VLANID || vlanId1 > MAX_FILTER_TUPLE_VLANID) {
                  isOk = false;
                }
                if (vlanId2 < MIN_FILTER_TUPLE_VLANID || vlanId2 > MAX_FILTER_TUPLE_VLANID) {
                  isOk = false;
                }
                if (vlanId1 >= vlanId2) {
                  isOk = false;
                }
              } catch (NumberFormatException nfException) {
                isOk = false;
              } catch (IndexOutOfBoundsException ioobException) {
                isOk = false;
              }
            }
          }
          if (!isOk) {
            resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "不合法的vlanid：" + vlanIdStr);
            return resultMap;
          }
        }
      } else {
        tuple.setVlanId("");
      }
    }
    transmitTaskVO.setFilterTuple(fileterTuple);
    return resultMap;
  }

}
