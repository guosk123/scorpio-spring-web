package com.machloop.fpc.manager.appliance.controller;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
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
import org.springframework.security.access.annotation.Secured;
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
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.base.page.Sort.Order;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.common.util.NetworkUtils.IpVersion;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.appliance.bo.ExternalStorageBO;
import com.machloop.fpc.manager.appliance.bo.FilterTupleBO;
import com.machloop.fpc.manager.appliance.bo.TransmitTaskBO;
import com.machloop.fpc.manager.appliance.service.ExternalStorageService;
import com.machloop.fpc.manager.appliance.service.TransmitTaskFileService;
import com.machloop.fpc.manager.appliance.service.TransmitTaskService;
import com.machloop.fpc.manager.appliance.vo.TransmitTaskCreationVO;
import com.machloop.fpc.manager.appliance.vo.TransmitTaskModificationVO;
import com.machloop.fpc.manager.appliance.vo.TransmitTaskQueryVO;
import com.machloop.fpc.manager.knowledge.bo.GeoCityBO;
import com.machloop.fpc.manager.knowledge.bo.GeoCountryBO;
import com.machloop.fpc.manager.knowledge.bo.GeoCustomCountryBO;
import com.machloop.fpc.manager.knowledge.bo.GeoProvinceBO;
import com.machloop.fpc.manager.knowledge.service.GeoService;
import com.machloop.fpc.manager.knowledge.service.SaProtocolService;
import com.machloop.fpc.manager.knowledge.service.SaService;
import com.machloop.fpc.manager.system.bo.DeviceNetifBO;
import com.machloop.fpc.manager.system.service.DeviceNetifService;
import com.machloop.fpc.manager.system.service.SystemMetricService;
import com.machloop.fpc.npm.appliance.bo.LogicalSubnetBO;
import com.machloop.fpc.npm.appliance.bo.NetworkBO;
import com.machloop.fpc.npm.appliance.service.LogicalSubnetService;
import com.machloop.fpc.npm.appliance.service.NetworkService;

import reactor.util.function.Tuple3;

/**
 * @author liumeng
 * <p>
 * create at 2018年12月14日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-v1/appliance")
@SuppressWarnings("all")
public class TransmitTaskController {

  @SuppressWarnings("unused")
  private static final Logger LOGGER = LoggerFactory.getLogger(TransmitTaskController.class);

  private static final int MAX_FILTER_TUPLE_NUMBER = 5;
  private static final int MIN_FILTER_TUPLE_VLANID = 0;
  private static final int MAX_FILTER_TUPLE_VLANID = 4094;
  private static final int MAX_FILTER_RAW_RULE_NUMBER = 10;
  private static final int MAX_FILTER_RAW_CONDITION_NUMBER = 5;

  private static final String DATA_OLDEST_TIME = "data_oldest_time";

  private static final String ALL_NETWORK = "ALL";

  private static final String REPLAY_RATE_UNIT_KBPS = "0";
  private static final String REPLAY_RATE_UNIT_PPS = "1";

  private static final Range<Integer> RANGE_REPLAY_RATE_KBPS = Range.closed(128, 10_000_000);

  private static final Range<Integer> RANGE_REPLAY_RATE_PPS = Range.closed(1, 2_000_000);

  private static final Range<Long> RANGE_IPTUNNEL_GRE_KEY = Range.closed(0L, 4294967295L);

  private static final Range<Long> RANGE_IPTUNNEL_VXLAN_VNID = Range.closed(0L, 16777215L);

  private static final String FORWARD_POLICY_STORE = "0";
  private static final String FORWARD_POLICY_NO_STORE = "1";

  private static final String FILTER_RAW_CONDITION_TYPE_ASCII = "ascii";
  private static final String FILTER_RAW_CONDITION_TYPE_HEX = "hex";
  private static final String FILTER_RAW_CONDITION_TYPE_REGULAR = "regular";
  private static final String FILTER_RAW_CONDITION_TYPE_CHINESE = "chinese";

  private static final String[] IP_PROTOCOLS = {"TCP", "UDP", "ICMP", "SCTP"};

  private static final Pattern MAC_PATTERN = Pattern
      .compile("^[A-Fa-f0-9]{2}([-,:][A-Fa-f0-9]{2}){5}$", Pattern.MULTILINE);
  private static final Pattern ASCII_PATTERN = Pattern.compile("[\\x20-\\x7e]{1,64}$",
      Pattern.MULTILINE);
  private static final Pattern HEX_PATTERN = Pattern.compile("-?[0-9a-fA-F]{2,128}$");

  @Autowired
  private TransmitTaskService transmitTaskService;

  @Autowired
  private TransmitTaskFileService transmitTaskFileService;

  @Autowired
  private SystemMetricService monitorMetricService;

  @Autowired
  private DeviceNetifService deviceNetifService;

  @Autowired
  private ExternalStorageService externalStorageService;

  @Autowired
  private SaService saService;

  @Autowired
  private SaProtocolService saProtocolService;

  @Autowired
  private NetworkService networkService;

  @Autowired
  private LogicalSubnetService logicalSubnetService;

  @Autowired
  private GeoService geoService;

  @Value("${file.js.regex.path}")
  private String regexPath;


  @GetMapping("/transmition-tasks")
  @Secured({"PERM_USER"})
  public Page<Map<String, Object>> queryTransmitTasks(
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
      TransmitTaskQueryVO queryVO) {

    Sort sort = new Sort(new Order(Sort.Direction.DESC, "execution_end_time"),
        new Order(Sort.Direction.DESC, "execution_progress"),
        new Order(Sort.Direction.ASC, "transfer_time"),
        new Order(Sort.Direction.ASC, "create_time"));
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);
    Page<TransmitTaskBO> transmitTaskPage = transmitTaskService.queryTransmitTasks(page, queryVO);

    List<Map<String, Object>> resultList = Lists
        .newArrayListWithCapacity(transmitTaskPage.getSize());
    for (TransmitTaskBO transmitTask : transmitTaskPage) {
      resultList.add(transmitTaskBO2Map(transmitTask, true));
    }

    return new PageImpl<>(resultList, page, transmitTaskPage.getTotalElements());
  }

  @GetMapping("/transmition-tasks/{id}")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryTransmitTask(
      @PathVariable @NotEmpty(message = "任务id不能为空") String id) {
    return transmitTaskBO2Map(transmitTaskService.queryTransmitTask(id), true);
  }

  @PostMapping("/transmition-tasks")
  @Secured({"PERM_USER"})
  public void saveTransmitTask(@Validated TransmitTaskCreationVO transmitTaskVO) {
    // 检验参数
    checkParameter(transmitTaskVO);

    TransmitTaskBO transmitTaskBO = new TransmitTaskBO();
    BeanUtils.copyProperties(transmitTaskVO, transmitTaskBO);
    transmitTaskBO
        .setFilterNetworkId(StringUtils.defaultIfBlank(transmitTaskBO.getFilterNetworkId(),
            StringUtils.isBlank(transmitTaskVO.getFilterPacketFileId()) ? ALL_NETWORK : ""));
    transmitTaskBO.setFilterBpf(StringUtils.defaultIfBlank(transmitTaskVO.getFilterBpf(), ""));
    transmitTaskBO.setFilterTuple(StringUtils.defaultIfBlank(transmitTaskVO.getFilterTuple(), ""));
    transmitTaskBO.setReplayRule(StringUtils.defaultIfBlank(transmitTaskVO.getReplayRule(), ""));

    transmitTaskBO = transmitTaskService.saveTransmitTask(transmitTaskBO,
        LoggedUserContext.getCurrentUser().getId(), LoggedUserContext.getCurrentUser().getFullname()
            + "/" + LoggedUserContext.getCurrentUser().getUsername());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, transmitTaskBO);
  }

  @PostMapping("/transmition-tasks/{id}/redo")
  @Secured({"PERM_USER"})
  public void redoTransmitTask(@PathVariable @NotEmpty(message = "重新开始任务时传入的id不能为空") String id) {

    TransmitTaskBO transmitTask = transmitTaskService.redoTransmitTask(id);

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_REDO, transmitTask);
  }

  @PostMapping("/transmition-tasks/{id}/stop")
  @Secured({"PERM_USER"})
  public void stopTransmitTask(@PathVariable @NotEmpty(message = "停止任务时传入的id不能为空") String id) {

    TransmitTaskBO transmitTask = transmitTaskService.stopTransmitTask(id);

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_STOP, transmitTask);
  }

  @PostMapping("/transmition-tasks/batch")
  @Secured({"PERM_USER"})
  public void batchDeleteTransmitTask(@RequestBody Map<String, String> param) {
    String ids = param.get("delete");

    CsvUtils.convertCSVToList(ids).forEach(id -> {
      TransmitTaskBO transmitTask = transmitTaskService.deleteTransmitTask(id,
          LoggedUserContext.getCurrentUser().getId());

      LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, transmitTask);
    });
  }

  @PutMapping("/transmition-tasks/{id}")
  @Secured({"PERM_USER"})
  public void updateTransmitTask(@PathVariable @NotEmpty(message = "修改任务时传入的id不能为空") String id,
      @Validated TransmitTaskModificationVO transmitTaskVO) {

    // 编辑任务时需要重新校验规则条件
    TransmitTaskCreationVO transmitTaskCreationVO = new TransmitTaskCreationVO();
    BeanUtils.copyProperties(transmitTaskVO, transmitTaskCreationVO);
    checkParameter(transmitTaskCreationVO);

    TransmitTaskBO transmitTaskBO = new TransmitTaskBO();
    BeanUtils.copyProperties(transmitTaskVO, transmitTaskBO);

    TransmitTaskBO transmitTask = transmitTaskService.updateTransmitTask(transmitTaskBO, id,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, transmitTask);
  }

  @DeleteMapping("/transmition-tasks/{id}")
  @Secured({"PERM_USER"})
  public void deleteTransmitTask(@PathVariable @NotEmpty(message = "删除任务时传入的id不能为空") String id) {
    TransmitTaskBO transmitTask = transmitTaskService.deleteTransmitTask(id,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, transmitTask);
  }

  @GetMapping("/transmition-tasks/{id}/analysis")
  @Secured({"PERM_USER"})
  public void analyzeTransmitTaskFile(@PathVariable("id") @NotEmpty(message = "任务id不能为空") String id,
      String type, String parameter, HttpServletRequest request, HttpServletResponse response) {

    transmitTaskFileService.analyzeTransmitTaskFile(id, type, parameter, request, response);
  }

  @GetMapping("/transmition-tasks/{id}/files")
  @Secured({"PERM_USER"})
  public Map<String, String> downloadTransmitTaskFile(
      @PathVariable @NotEmpty(message = "任务id不能为空") String id, HttpServletRequest request) {

    Map<String, String> resultMap = transmitTaskFileService.downloadTransmitTaskFile(id,
        request.getRemoteAddr());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DOWNLOAD,
        transmitTaskService.queryTransmitTask(id));
    return resultMap;
  }

  /**
   * @param transmitTask
   * @param isDetail
   * @return
   */
  private static Map<String, Object> transmitTaskBO2Map(TransmitTaskBO transmitTask,
      boolean isDetail) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", transmitTask.getId());
    map.put("name", transmitTask.getName());
    map.put("source", transmitTask.getSource());
    map.put("filterStartTime", transmitTask.getFilterStartTime());
    map.put("filterEndTime", transmitTask.getFilterEndTime());
    map.put("filterConditionType", transmitTask.getFilterConditionType());
    map.put("filterConditionTypeText", transmitTask.getFilterConditionTypeText());
    map.put("executionStartTime", transmitTask.getExecutionStartTime());
    map.put("executionEndTime", transmitTask.getExecutionEndTime());
    map.put("executionProgress", transmitTask.getExecutionProgress());
    map.put("state", transmitTask.getState());
    map.put("mode", transmitTask.getMode());
    map.put("modeText", transmitTask.getModeText());

    if (isDetail) {
      map.put("filterNetworkId", transmitTask.getFilterNetworkId());
      map.put("filterNetworkName", transmitTask.getFilterNetworkName());
      map.put("filterPacketFileId", transmitTask.getFilterPacketFileId());
      map.put("filterPacketFileName", transmitTask.getFilterPacketFileName());
      map.put("filterTuple", transmitTask.getFilterTuple());
      map.put("filterBpf", transmitTask.getFilterBpf());
      map.put("filterRaw", transmitTask.getFilterRaw());
      map.put("replayNetif", transmitTask.getReplayNetif());
      map.put("replayRate", transmitTask.getReplayRate());
      map.put("replayRateUnit", transmitTask.getReplayRateUnit());
      map.put("replayRateUnitText", transmitTask.getReplayRateUnitText());
      map.put("replayRule", transmitTask.getReplayRule());
      map.put("forwardAction", transmitTask.getForwardAction());
      map.put("forwardActionText", transmitTask.getForwardActionText());
      map.put("ipTunnel", transmitTask.getIpTunnel());
      map.put("executionTrace", transmitTask.getExecutionTrace());
      map.put("description", transmitTask.getDescription());
    }

    return map;
  }

  private void checkParameter(TransmitTaskCreationVO transmitTaskVO) {
    // 校验过滤条件开始时间的日期格式
    if (transmitTaskVO.getFilterStartTime() != null && transmitTaskVO.getFilterEndTime() != null) {
      Date filterStartDate = null;
      try {
        filterStartDate = DateUtils.parseISO8601Date(transmitTaskVO.getFilterStartTime());
      } catch (Exception exception) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的日期格式");
      }

      // 检验最早报文时间早于过滤条件开始时间
      // 引擎端在数据写满后会老化最早的报文，导致最早报文时间一直在变化，为避免前后端获取到的最早报文时间不一致，后端不再做校验
      /*
       * String dataOldestTimeValue = ""; List<MonitorMetricBO> monitorMetricList =
       * monitorMetricService.queryMonitorMetrics(); for (MonitorMetricBO monitorMetricVO :
       * monitorMetricList) { if (StringUtils.equals(DATA_OLDEST_TIME,
       * monitorMetricVO.getMetricName())) { dataOldestTimeValue = monitorMetricVO.getMetricValue();
       * break; } } if (StringUtils.isNotBlank(dataOldestTimeValue)) { Date dataOldestTime = new
       * Date(Long.parseLong(dataOldestTimeValue) * 1000); if (filterStartDate.getTime() <
       * dataOldestTime.getTime()) { throw new
       * BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "过滤条件开始时间早于最早报文时间"); } }
       */

      // 校验过滤条件结束时间的日期格式
      Date filterEndDate = null;
      try {
        filterEndDate = DateUtils.parseISO8601Date(transmitTaskVO.getFilterEndTime());
      } catch (Exception exception) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的日期格式");
      }

      // 校验过滤条件开始时间早于过滤条件结束时间
      if (filterEndDate.getTime() < filterStartDate.getTime()) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "过滤条件结束时间早于过滤条件开始时间");
      }
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
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的网络id：" + networkId);
      }
    }

    // 防止编辑任务时调用该方法出现报错
    if (transmitTaskVO.getFilterConditionType() != null) {
      if (!StringUtils.equalsAny(transmitTaskVO.getFilterConditionType(),
          FpcConstants.TRANSMIT_TASK_FILTER_TYPE_TUPLE, FpcConstants.TRANSMIT_TASK_FILTER_TYPE_BPF,
          FpcConstants.TRANSMIT_TASK_FILTER_TYPE_MIX)) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的过滤条件类型");
      }
    }

    // 规则条件校验
    if (!StringUtils.equals(FpcConstants.TRANSMIT_TASK_FILTER_TYPE_BPF,
        transmitTaskVO.getFilterConditionType())) {
      checkAndPaddingFilterTuple(transmitTaskVO);
    }

    // 过滤原始内容校验
    List<List<Map<String, String>>> filterRaw = JsonHelper.deserialize(
        transmitTaskVO.getFilterRaw(), new TypeReference<List<List<Map<String, String>>>>() {
        }, false);

    checkFilterRaw(filterRaw);

    // 检查任务类型
    // 防止编辑任务时调用该方法出现报错
    if (transmitTaskVO.getMode() != null) {
      switch (transmitTaskVO.getMode()) {
        case FpcConstants.TRANSMIT_TASK_MODE_FILE_PCAP:
          break;
        case FpcConstants.TRANSMIT_TASK_MODE_REPLAY:
          checkTransmitTaskByReplayMode(transmitTaskVO.getReplayRateUnit(),
              transmitTaskVO.getReplayRate(), transmitTaskVO.getReplayNetif(),
              transmitTaskVO.getReplayRule(), transmitTaskVO.getForwardAction(),
              transmitTaskVO.getIpTunnel());
          break;
        case FpcConstants.TRANSMIT_TASK_MODE_FILE_PCAPNG:
          break;
        case FpcConstants.TRANSMIT_TASK_MODE_FILE_EXTERNAL_STORAGE:
          List<ExternalStorageBO> externalStorages = externalStorageService
              .queryExternalStorages(FpcConstants.EXTERNAL_STORAGE_USAGE_TRANSMIT, null);
          if (CollectionUtils.isEmpty(externalStorages) || !externalStorages.stream()
              .anyMatch(item -> StringUtils.equals(item.getState(), Constants.BOOL_YES))) {
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "任务存储服务器未配置或为关闭状态");
          }
          break;
        default:
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的导出模式");
      }
    }
  }

  /**
   * 校验内容匹配
   *
   * @param filterRaw
   * @return
   */
  private void checkFilterRaw(List<List<Map<String, String>>> filterRaws) {
    if (CollectionUtils.isEmpty(filterRaws)) {
      return;
    }

    if (filterRaws.size() > MAX_FILTER_RAW_RULE_NUMBER) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          "内容匹配规则数量超过" + MAX_FILTER_RAW_RULE_NUMBER + "个");
    }

    if (Sets.newHashSet(filterRaws).size() != filterRaws.size()) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "存在重复的内容匹配规则");
    }

    for (List<Map<String, String>> item : filterRaws) {
      if (item.size() > MAX_FILTER_RAW_CONDITION_NUMBER) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
            "内容匹配规则数量超过" + MAX_FILTER_RAW_CONDITION_NUMBER + "个");
      }

      if (Sets.newHashSet(item).size() != item.size()) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "内容匹配规则内存在重复的条件");
      }

      for (Map<String, String> condition : item) {
        String type = condition.get("type");
        String value = condition.get("value");
        if (value.getBytes().length > 256) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "抱歉，您输入的内容超过长度限制，请调整您输入内容的长度");
        }
        if (StringUtils.equals(type, FILTER_RAW_CONDITION_TYPE_ASCII)) {
          if (!ASCII_PATTERN.matcher(value).matches()) {
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的ASCII码");
          }
        } else if (StringUtils.equals(type, FILTER_RAW_CONDITION_TYPE_HEX)) {
          if (!HEX_PATTERN.matcher(value).matches() || value.length() % 2 != 0) {
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的16进制");
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
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的正则表达式");
          }
        } else if (StringUtils.equals(type, FILTER_RAW_CONDITION_TYPE_CHINESE)) {
          value = new String(value.getBytes(StandardCharsets.UTF_8));
        } else {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "不合法的内容匹配条件类型，仅支持：" + FILTER_RAW_CONDITION_TYPE_ASCII + ","
                  + FILTER_RAW_CONDITION_TYPE_HEX + "," + FILTER_RAW_CONDITION_TYPE_REGULAR + ","
                  + FILTER_RAW_CONDITION_TYPE_CHINESE);
        }
      }
    }
  }

  private void checkTransmitTaskByReplayMode(String replayRateUnit, int replayRate,
      String replayNetif, String replayRule, String forwardAction, String ipTunnel) {

    // 检查重放速率
    if (StringUtils.isBlank(replayRateUnit)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "重放速率单位为空");
    }

    if (StringUtils.equals(replayRateUnit, REPLAY_RATE_UNIT_KBPS)) {
      // 校验单位是Kbps下的速率范围
      if (!RANGE_REPLAY_RATE_KBPS.contains(replayRate)) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的重放速率");
      }
    } else if (StringUtils.equals(replayRateUnit, REPLAY_RATE_UNIT_PPS)) {
      // 校验单位是pps下的速率范围
      if (!RANGE_REPLAY_RATE_PPS.contains(replayRate)) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的重放速率");
      }
    } else {
      // 重放速率单位不存在
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的重放速率");
    }

    // 校验重放接口是否存在
    if (StringUtils.isBlank(replayNetif)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "重放接口的接口名为空");
    }

    List<DeviceNetifBO> deviceNetifBOList = deviceNetifService
        .queryDeviceNetifsByCategories(FpcConstants.DEVICE_NETIF_CATEGORY_TRANSMIT);
    List<String> netifNames = deviceNetifBOList.stream().map(netif -> netif.getName())
        .collect(Collectors.toList());
    if (!netifNames.contains(replayNetif)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "重放接口不存在或类型不匹配");
    }

    if (!StringUtils.equalsAny(forwardAction, FORWARD_POLICY_STORE, FORWARD_POLICY_NO_STORE)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的流量转发策略");
    }

    if (StringUtils.isNotBlank(ipTunnel)) {
      Map<String, Object> ipTunnels = JsonHelper.deserialize(ipTunnel,
          new TypeReference<Map<String, Object>>() {
          }, false);

      String mode = MapUtils.getString(ipTunnels, "mode");
      if (!StringUtils.equalsAny(mode, FpcConstants.TRANSMIT_TASK_IPTUNNEL_MODE_GRE,
          FpcConstants.TRANSMIT_TASK_IPTUNNEL_MODE_VXLAN)) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的隧道封装模式");
      }

      Map<String,
          Object> params = JsonHelper.deserialize(
              JsonHelper.serialize(MapUtils.getObject(ipTunnels, "params")),
              new TypeReference<Map<String, Object>>() {
              }, false);
      if (MapUtils.isEmpty(params)) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "隧道封装参数不能为空");
      }

      String sourceMac = MapUtils.getString(params, "sourceMac");
      if (StringUtils.isNotBlank(sourceMac) && !MAC_PATTERN.matcher(sourceMac).matches()) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的隧道封装源MAC");
      }

      String destMac = MapUtils.getString(params, "destMac", "");
      if (!MAC_PATTERN.matcher(destMac).matches()) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的隧道封装目的MAC");
      }

      String sourceIp = MapUtils.getString(params, "sourceIp", "");
      String destIp = MapUtils.getString(params, "destIp", "");
      if (!((NetworkUtils.isInetAddress(sourceIp, IpVersion.V4)
          && NetworkUtils.isInetAddress(destIp, IpVersion.V4))
          || (NetworkUtils.isInetAddress(sourceIp, IpVersion.V6)
              && NetworkUtils.isInetAddress(destIp, IpVersion.V6)))) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
            "隧道封装源/目的IP必须同时为IPV4/IPV6");
      }

      if (StringUtils.equals(sourceIp, destIp)) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "隧道封装源IP和目的IP不能相同");
      }

      if (StringUtils.equals(mode, FpcConstants.TRANSMIT_TASK_IPTUNNEL_MODE_GRE)) {
        Long key = MapUtils.getLong(params, "key", null);
        if (key != null && !RANGE_IPTUNNEL_GRE_KEY.contains(key)) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的隧道封装KEY值");
        }

        String checksum = MapUtils.getString(params, "checksum");
        if (!StringUtils.equalsAny(checksum, Constants.BOOL_YES, Constants.BOOL_NO)) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "隧道封装是否计算校验和参数不合法");
        }
      }

      if (StringUtils.equals(mode, FpcConstants.TRANSMIT_TASK_IPTUNNEL_MODE_VXLAN)) {
        Integer sourcePort = MapUtils.getInteger(params, "sourcePort", null);
        if (sourcePort == null || !NetworkUtils.isInetAddressPort(String.valueOf(sourcePort))) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的隧道封装源端口");
        }

        Integer destPort = MapUtils.getInteger(params, "destPort", null);
        if (destPort == null || !NetworkUtils.isInetAddressPort(String.valueOf(destPort))) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的隧道封装目的端口");
        }

        Long vnid = MapUtils.getLong(params, "vnid", null);
        if (vnid == null || !RANGE_IPTUNNEL_VXLAN_VNID.contains(vnid)) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的隧道封装VNID值");
        }
      }
    }
    if (StringUtils.isNotBlank(replayRule)) {
      List<Map<String, Object>> replayRuleList = JsonHelper.deserialize(replayRule,
          new TypeReference<List<Map<String, Object>>>() {
          }, false);
      checkReplayRule(replayRuleList);
    }

  }

  private void checkReplayRule(List<Map<String, Object>> replayRule) {

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
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的IP：" + ip);
        }
      }
    }
    if (!macAddresses.isEmpty()) {
      for (String macAddress : macAddresses) {
        if (macAddress.contains("NOT_")) {
          macAddress = macAddress.substring(4);
        }
        if (StringUtils.isNotBlank(macAddress) && !MAC_PATTERN.matcher(macAddress).matches()) {
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "不合法的Mac地址：" + macAddress);
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
          throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
              "不合法的vlanId：" + vlanIdStr);
        }
      }
    }
  }

  /**
   * 校验六元组参数并填充默认值
   *
   * @param sixTupleJson
   * @return
   */
  private void checkAndPaddingFilterTuple(TransmitTaskCreationVO transmitTaskVO) {

    String filterTuple = transmitTaskVO.getFilterTuple();

    if (StringUtils.isBlank(filterTuple)) {
      return;
    }

    List<FilterTupleBO> filterTupleArray = JsonHelper.deserialize(filterTuple,
        new TypeReference<List<FilterTupleBO>>() {
        }, false);

    if (CollectionUtils.isEmpty(filterTupleArray)) {
      return;
    }

    if (filterTupleArray.size() > MAX_FILTER_TUPLE_NUMBER) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          "过滤规则数量超过" + MAX_FILTER_TUPLE_NUMBER + "个");
    }

    HashSet<FilterTupleBO> removal = Sets.newHashSet(filterTupleArray);
    if (removal.size() < filterTupleArray.size()) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "存在重复的过滤规则");
    }

    List<String> validL7ProtocolIds = saProtocolService.queryProtocols().stream()
        .map(item -> (String) item.get("protocolId")).collect(Collectors.toList());
    Set<Integer> validAppIds = saService.queryAllAppsIdNameMapping().keySet();
    Tuple3<List<GeoCountryBO>, List<GeoProvinceBO>,
        List<GeoCityBO>> geolocations = geoService.queryGeolocations();
    List<String> validCountryIds = geolocations.getT1().stream()
        .map(country -> country.getCountryId()).collect(Collectors.toList());
    validCountryIds.addAll(geoService.queryCustomCountrys().stream()
        .map(GeoCustomCountryBO::getCountryId).collect(Collectors.toList()));
    List<String> validProvinceIds = geolocations.getT2().stream()
        .map(province -> province.getProvinceId()).collect(Collectors.toList());
    List<String> validCityIds = geolocations.getT3().stream().map(city -> city.getCityId())
        .collect(Collectors.toList());
    for (FilterTupleBO tuple : filterTupleArray) {
      if (tuple.isEmpty()) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "单个过滤规则内容不能为空");
      }

      if (tuple.getIp() != null && (tuple.getSourceIp() != null || tuple.getDestIp() != null)) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
            "IP 与 （源IP、目的IP）互斥，不可同时存在");
      }

      if ((tuple.getPort() != null)
          && ((tuple.getSourcePort() != null) || tuple.getDestPort() != null)) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
            "端口 与 （源端口、目的端口） 互斥，不可同时存在");
      }

      if (tuple.getMacAddress() != null
          && (tuple.getSourceMacAddress() != null || tuple.getDestMacAddress() != null)) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
            "MAC地址 与 （源MAC地址、目的MAC地址） 互斥，不可同时存在");
      }

      if (tuple.getCountryId() != null
          && (tuple.getSourceCountryId() != null || tuple.getDestCountryId() != null)) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
            "国家 与 （源国家、目的国家） 互斥，不可同时存在");
      }

      if (tuple.getProvinceId() != null
          && (tuple.getSourceProvinceId() != null || tuple.getDestProvinceId() != null)) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
            "省份 与 （源省份、目的省份） 互斥，不可同时存在");
      }

      if (tuple.getCityId() != null
          && (tuple.getSourceCityId() != null || tuple.getDestCityId() != null)) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
            "城市 与 （源城市、目的城市） 互斥，不可同时存在");
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
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "参数[ip]数据类型不合法");
          }
        }
        for (String ip : ips) {
          if (ip.contains("NOT_")) {
            ip = ip.substring(4);
          }
          if (StringUtils.isNotBlank(ip) && !NetworkUtils.isInetAddress(ip)
              && !NetworkUtils.isCidr(ip)) {
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的IP：" + ip);
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
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                "参数[sourceIp]数据类型不合法");
          }
        }
        for (String sourceIp : sourceIps) {
          if (sourceIp.contains("NOT_")) {
            sourceIp = sourceIp.substring(4);
          }
          if (StringUtils.isNotBlank(sourceIp) && !NetworkUtils.isInetAddress(sourceIp)
              && !NetworkUtils.isCidr(sourceIp)) {
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                "不合法的源IP：" + sourceIp);
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
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "参数[destIp]数据类型不合法");
          }
        }
        for (String destIp : destIps) {
          if (destIp.contains("NOT_")) {
            destIp = destIp.substring(4);
          }
          if (StringUtils.isNotBlank(destIp) && !NetworkUtils.isInetAddress(destIp)
              && !NetworkUtils.isCidr(destIp)) {
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的目的IP：" + destIp);
          }
        }
      } else {
        tuple.setDestIp("");
      }

      // 端口循环校验
      if (tuple.getPort() != null) {
        List<String> ports = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        if (tuple.getPort() instanceof String || tuple.getPort() instanceof Integer) {
          ports.add(String.valueOf(tuple.getPort()));
        } else {
          try {
            ports.addAll((List<String>) tuple.getPort());
          } catch (ClassCastException e) {
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "参数[port]数据类型不合法");
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
              throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的端口：" + port);
            }
          }
        }
      } else {
        tuple.setPort("");
      }

      // 源端口循环校验
      if (tuple.getSourcePort() != null) {
        List<String> sourcePorts = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        if (tuple.getSourcePort() instanceof String || tuple.getSourcePort() instanceof Integer) {
          sourcePorts.add(String.valueOf(tuple.getSourcePort()));
        } else {
          try {
            sourcePorts.addAll((List<String>) tuple.getSourcePort());
          } catch (ClassCastException e) {
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                "参数[sourcePort]数据类型不合法");
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
              throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                  "不合法的源端口：" + sourcePort);
            }
          }
        }
      } else {
        tuple.setSourcePort("");
      }

      // 目的端口循环校验
      if (tuple.getDestPort() != null) {
        List<String> destPorts = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        if (tuple.getDestPort() instanceof String || tuple.getDestPort() instanceof Integer) {
          destPorts.add(String.valueOf(tuple.getDestPort()));
        } else {
          try {
            destPorts.addAll((List<String>) tuple.getDestPort());
          } catch (ClassCastException e) {
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                "参数[destPort]数据类型不合法");
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
              throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                  "不合法的目的端口：" + destPort);
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
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                "参数[countryId]数据类型不合法");
          }
        }
        for (String countryId : countryIds) {
          if (countryId.contains("NOT_")) {
            countryId = countryId.substring(4);
          }
          if (StringUtils.isNotBlank(countryId) && !validCountryIds.contains(countryId)) {
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                "不合法的国家ID：" + countryId);
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
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                "参数[sourceCountryId]数据类型不合法");
          }
        }
        for (String sourceCountryId : sourceCountryIds) {
          if (sourceCountryId.contains("NOT_")) {
            sourceCountryId = sourceCountryId.substring(4);
          }
          if (StringUtils.isNotBlank(sourceCountryId)
              && !validCountryIds.contains(sourceCountryId)) {
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                "不合法的源国家ID：" + sourceCountryId);
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
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                "参数[destCountryId]数据类型不合法");
          }
        }
        for (String destCountryId : destCountryIds) {
          if (destCountryId.contains("NOT_")) {
            destCountryId = destCountryId.substring(4);
          }
          if (StringUtils.isNotBlank(destCountryId) && !validCountryIds.contains(destCountryId)) {
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                "不合法的目的国家ID：" + destCountryId);
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
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                "参数[provinceId]数据类型不合法");
          }
        }
        for (String provinceId : provinceIds) {
          if (provinceId.contains("NOT_")) {
            provinceId = provinceId.substring(4);
          }
          if (StringUtils.isNotBlank(provinceId) && !validProvinceIds.contains(provinceId)) {
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                "不合法的省份ID：" + provinceId);
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
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                "参数[sourceProvinceId]数据类型不合法");
          }
        }
        for (String sourceProvinceId : sourceProvinceIds) {
          if (sourceProvinceId.contains("NOT_")) {
            sourceProvinceId = sourceProvinceId.substring(4);
          }
          if (StringUtils.isNotBlank(sourceProvinceId)
              && !validProvinceIds.contains(sourceProvinceId)) {
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                "不合法的源省份ID：" + sourceProvinceId);
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
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                "参数[destProvinceId]数据类型不合法");
          }
        }
        for (String destProvinceId : destProvinceIds) {
          if (destProvinceId.contains("NOT_")) {
            destProvinceId = destProvinceId.substring(4);
          }
          if (StringUtils.isNotBlank(destProvinceId)
              && !validProvinceIds.contains(destProvinceId)) {
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                "不合法的目的省份ID：" + destProvinceId);
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
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "参数[cityId]数据类型不合法");
          }
        }
        for (String cityId : cityIds) {
          if (cityId.contains("NOT_")) {
            cityId = cityId.substring(4);
          }
          if (StringUtils.isNotBlank(cityId) && !validCityIds.contains(cityId)) {
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的城市ID：" + cityId);
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
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                "参数[sourceCityId]数据类型不合法");
          }
        }
        for (String sourceCityId : sourceCityIds) {
          if (sourceCityId.contains("NOT_")) {
            sourceCityId = sourceCityId.substring(4);
          }
          if (StringUtils.isNotBlank(sourceCityId) && !validCityIds.contains(sourceCityId)) {
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                "不合法的源城市ID：" + sourceCityId);
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
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                "参数[destCityId]数据类型不合法");
          }
        }
        for (String destCityId : destCityIds) {
          if (destCityId.contains("NOT_")) {
            destCityId = destCityId.substring(4);
          }
          if (StringUtils.isNotBlank(destCityId) && !validCityIds.contains(destCityId)) {
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                "不合法的目的城市ID：" + destCityId);
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
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                "参数[ipProtocol]数据类型不合法");
          }
        }
        for (String ipProtocol : ipProtocols) {
          if (ipProtocol.contains("NOT_")) {
            ipProtocol = ipProtocol.substring(4);
          }
          if (StringUtils.isNotBlank(ipProtocol)
              && !Arrays.asList(IP_PROTOCOLS).contains(ipProtocol.toUpperCase())) {
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                "不合法的传输层协议号：" + ipProtocol);
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
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                "参数[l7ProtocolId]数据类型不合法");
          }
        }
        for (String l7ProtocolId : l7ProtocolIds) {
          if (l7ProtocolId.contains("NOT_")) {
            l7ProtocolId = l7ProtocolId.substring(4);
          }
          if (StringUtils.isNotBlank(l7ProtocolId) && !validL7ProtocolIds.contains(l7ProtocolId)) {
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                "不合法的应用层协议号：" + l7ProtocolId);
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
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                "参数[applicationId]数据类型不合法");
          }
        }
        for (String applicationId : applicationIds) {
          if (applicationId.contains("NOT_")) {
            applicationId = applicationId.substring(4);
          }
          if (StringUtils.isNotBlank(applicationId)
              && !validAppIds.contains(Integer.parseInt(applicationId))) {
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                "不合法的应用id：" + applicationId);
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
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                "参数[macAddress]数据类型不合法");
          }
        }
        for (String macAddress : macAddresses) {
          if (macAddress.contains("NOT_")) {
            macAddress = macAddress.substring(4);
          }
          if (StringUtils.isNotBlank(macAddress) && !MAC_PATTERN.matcher(macAddress).matches()) {
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                "不合法的Mac地址：" + macAddress);
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
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                "参数[sourceMacAddress]数据类型不合法");
          }
        }
        for (String sourceMacAddress : sourceMacAddresses) {
          if (sourceMacAddress.contains("NOT_")) {
            sourceMacAddress = sourceMacAddress.substring(4);
          }
          if (StringUtils.isNotBlank(sourceMacAddress)
              && !MAC_PATTERN.matcher(sourceMacAddress).matches()) {
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                "不合法的源Mac地址：" + sourceMacAddress);
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
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                "参数[destMacAddress]数据类型不合法");
          }
        }
        for (String destMacAddress : destMacAddresses) {
          if (destMacAddress.contains("NOT_")) {
            destMacAddress = destMacAddress.substring(4);
          }
          if (StringUtils.isNotBlank(destMacAddress)
              && !MAC_PATTERN.matcher(destMacAddress).matches()) {
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                "不合法的目的Mac地址：" + destMacAddress);
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
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "参数[vlanId]数据类型不合法");
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
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                "不合法的vlanid：" + vlanIdStr);
          }
        }
      } else {
        tuple.setVlanId("");
      }
    }
    transmitTaskVO.setFilterTuple(JsonHelper.serialize(filterTupleArray, false));
  }
}
