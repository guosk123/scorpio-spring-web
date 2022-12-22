package com.machloop.fpc.cms.center.appliance.controller;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.Digits;
import javax.validation.constraints.NotEmpty;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
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
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.appliance.bo.AssignmentActionBO;
import com.machloop.fpc.cms.center.appliance.bo.AssignmentTaskBO;
import com.machloop.fpc.cms.center.appliance.bo.AssignmentTaskRecordBO;
import com.machloop.fpc.cms.center.appliance.bo.FilterTupleBO;
import com.machloop.fpc.cms.center.appliance.service.AssignmentActionService;
import com.machloop.fpc.cms.center.appliance.service.AssignmentTaskService;
import com.machloop.fpc.cms.center.appliance.vo.AssignmentTaskCreationVO;
import com.machloop.fpc.cms.center.appliance.vo.AssignmentTaskModificationVO;
import com.machloop.fpc.cms.center.broker.invoker.FpcManagerInvoker;
import com.machloop.fpc.cms.center.broker.service.subordinate.AssignmentService;
import com.machloop.fpc.cms.center.central.bo.FpcBO;
import com.machloop.fpc.cms.center.central.service.FpcService;
import com.machloop.fpc.cms.center.knowledge.bo.GeoCityBO;
import com.machloop.fpc.cms.center.knowledge.bo.GeoCountryBO;
import com.machloop.fpc.cms.center.knowledge.bo.GeoCustomCountryBO;
import com.machloop.fpc.cms.center.knowledge.bo.GeoProvinceBO;
import com.machloop.fpc.cms.center.knowledge.service.GeoService;
import com.machloop.fpc.cms.center.knowledge.service.SaProtocolService;
import com.machloop.fpc.cms.center.knowledge.service.SaService;
import com.machloop.fpc.cms.common.FpcCmsConstants;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;

@Validated
@RestController
@RequestMapping("/webapi/fpc-cms-v1/appliance")
public class AssignmentTaskController {

  private static final int MAX_FILTER_TUPLE_NUMBER = 5;
  private static final int MIN_FILTER_TUPLE_VLANID = 0;
  private static final int MAX_FILTER_TUPLE_VLANID = 4094;
  private static final int MAX_FILTER_RAW_RULE_NUMBER = 10;
  private static final int MAX_FILTER_RAW_CONDITION_NUMBER = 5;

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
  private AssignmentTaskService assignmentTaskService;

  @Autowired
  private AssignmentActionService assignmentActionService;

  @Autowired
  private AssignmentService assignmentService;

  @Autowired
  private FpcService fpcService;

  @Autowired
  private SaProtocolService saProtocolService;

  @Autowired
  private SaService saService;

  @Autowired
  private GeoService geoService;

  @Autowired
  private FpcManagerInvoker fpcManagerInvoker;

  @Value("${file.js.regex.path}")
  private String regexPath;

  @GetMapping("/assignment-tasks")
  @Secured({"PERM_USER"})
  public Page<Map<String, Object>> queryAssignmentTasks(
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
      String name, String filterConditionType, String mode, String source, String sourceType) {

    Sort sort = new Sort(new Order(Sort.Direction.DESC, "create_time"));
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);
    Page<AssignmentTaskBO> assignmentTaskPage = assignmentTaskService.queryAssignmentTasks(page,
        name, filterConditionType, mode, source, sourceType);

    List<Map<String, Object>> taskList = Lists
        .newArrayListWithExpectedSize(assignmentTaskPage.getNumber());
    assignmentTaskPage.forEach(assignmentTask -> taskList.add(taskToMap(assignmentTask, true)));

    return new PageImpl<>(taskList, page, assignmentTaskPage.getTotalElements());
  }

  @GetMapping("/assignment-task-records")
  @Secured({"PERM_USER"})
  public Page<Map<String, Object>> queryAssignmentRecords(
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
      @RequestParam @NotEmpty(message = "任务id不能为空") String taskId) {

    Sort sort = new Sort(new Order(Sort.Direction.DESC, "assignment_time"),
        new Order(Sort.Direction.ASC, "assignment_state"));
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);
    Page<AssignmentTaskRecordBO> taskRecordPage = assignmentTaskService
        .queryAssignmentTaskRecords(page, taskId);

    List<Map<String, Object>> taskRecordList = Lists
        .newArrayListWithExpectedSize(taskRecordPage.getNumber());
    taskRecordPage.forEach(record -> taskRecordList.add(taskRecordToMap(record)));

    return new PageImpl<>(taskRecordList, page, taskRecordPage.getTotalElements());
  }

  @GetMapping("/assignment-tasks/{id}")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryAssignmentTask(
      @PathVariable @NotEmpty(message = "任务id不能为空") String id) {

    return taskToMap(assignmentTaskService.queryAssignmentTask(id), true);
  }

  @GetMapping("/assignment-task-courses/{assignmentId}/state")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryAssignmentActions(
      @PathVariable @NotEmpty(message = "下发标识不能为空") String assignmentId) {

    List<AssignmentActionBO> assignmentActions = assignmentActionService
        .queryAssignmentActions(assignmentId);

    List<Map<String, Object>> assignmentActionList = Lists
        .newArrayListWithExpectedSize(assignmentActions.size());
    assignmentActions.forEach(action -> assignmentActionList.add(assignmentActionToMap(action)));

    return assignmentActionList;
  }

  @PostMapping("/assignment-tasks")
  @Secured({"PERM_USER"})
  public void saveAssignmentTask(@Validated AssignmentTaskCreationVO assignmentTaskCreationVO) {

    // 检验参数
    checkParameter(assignmentTaskCreationVO);

    AssignmentTaskBO assignmentTaskBO = new AssignmentTaskBO();
    BeanUtils.copyProperties(assignmentTaskCreationVO, assignmentTaskBO);

    assignmentTaskBO.setOperatorId(LoggedUserContext.getCurrentUser().getId());
    assignmentTaskBO.setSource(LoggedUserContext.getCurrentUser().getFullname() + "/"
        + LoggedUserContext.getCurrentUser().getId());
    assignmentTaskBO.setFilterBpf(StringUtils.defaultIfBlank(assignmentTaskBO.getFilterBpf(), ""));
    assignmentTaskBO
        .setFilterTuple(StringUtils.defaultIfBlank(assignmentTaskBO.getFilterTuple(), ""));

    assignmentTaskBO = assignmentTaskService.saveAssignmentTask(assignmentTaskBO);

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, assignmentTaskBO);
  }

  @PostMapping("/assignment-tasks/{id}/assignment")
  @Secured({"PERM_USER"})
  public String assignTask(@PathVariable @NotEmpty(message = "任务id不能为空") String id,
      @RequestParam(required = true, defaultValue = "") String fpcSerialNumbers) {
    Tuple2<String, AssignmentTaskBO> result = assignmentTaskService.assignTask(id, fpcSerialNumbers,
        LoggedUserContext.getCurrentUser().getId());
    String assignmentId = result.getT1();
    AssignmentTaskBO assignmentTaskBO = result.getT2();

    // 加入下发队列
    assignmentService.addAssignmentQueue(FpcCmsConstants.ASSIGNMENT_TYPE_TASK);

    // 写审计日志
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_ASSIGNMENT, assignmentTaskBO);

    return assignmentId;
  }

  @PutMapping("/assignment-task-courses/{assignmentId}/option")
  @Secured({"PERM_USER"})
  public void assignmentOption(@PathVariable @NotEmpty(message = "下发标识不能为空") String assignmentId,
      @RequestParam @org.hibernate.validator.constraints.Range(
          min = 1, max = 3,
          message = "操作类型不合法") @Digits(integer = 1, fraction = 0, message = "操作类型不合法") int type) {

    AssignmentTaskBO assignmentTaskBO = assignmentTaskService.changeAssignmentState(assignmentId,
        type);

    if (type == CenterConstants.ASSIGNMENT_ACTION_CONTINUE) {
      // 加入下发队列
      assignmentService.addAssignmentQueue(FpcCmsConstants.ASSIGNMENT_TYPE_TASK);
    }

    // 写审计日志
    if (assignmentTaskBO != null) {
      int auditLogAction = 0;
      switch (type) {
        case CenterConstants.ASSIGNMENT_ACTION_STOP:
          auditLogAction = LogHelper.AUDIT_LOG_ACTION_STOP;
          break;
        case CenterConstants.ASSIGNMENT_ACTION_CONTINUE:
          auditLogAction = LogHelper.AUDIT_LOG_ACTION_CONTINUE;
          break;
        case CenterConstants.ASSIGNMENT_ACTION_CANCEL:
          auditLogAction = LogHelper.AUDIT_LOG_ACTION_CANCEL;
          break;
      }
      LogHelper.auditOperate(auditLogAction, assignmentTaskBO);
    }
  }

  @PutMapping("/assignment-tasks/{id}")
  @Secured({"PERM_USER"})
  public void updateAssignmentTask(@PathVariable @NotEmpty(message = "任务id不能为空") String id,
      @Validated AssignmentTaskModificationVO assignmentTaskModificationVO) {

    // 编辑任务时需要重新校验规则条件
    AssignmentTaskCreationVO assignmentTaskCreationVO = new AssignmentTaskCreationVO();
    BeanUtils.copyProperties(assignmentTaskModificationVO, assignmentTaskCreationVO);
    checkParameter(assignmentTaskCreationVO);

    AssignmentTaskBO assignmentTaskBO = new AssignmentTaskBO();
    BeanUtils.copyProperties(assignmentTaskModificationVO, assignmentTaskBO);

    assignmentTaskBO.setId(id);
    assignmentTaskBO.setOperatorId(LoggedUserContext.getCurrentUser().getId());
    assignmentTaskBO = assignmentTaskService.updateAssignmentTask(assignmentTaskBO);

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, assignmentTaskBO);
  }

  @PostMapping("/assignment-tasks/batch")
  @Secured({"PERM_USER"})
  public void batchDeleteAssignmentTask(@RequestBody Map<String, String> param) {
    String ids = param.get("delete");

    CsvUtils.convertCSVToList(ids).forEach(id -> {
      AssignmentTaskBO assignmentTaskBO = assignmentTaskService.deleteAssignmentTask(id,
          LoggedUserContext.getCurrentUser().getId());

      // 加入下发队列
      assignmentService.addAssignmentQueue(FpcCmsConstants.ASSIGNMENT_TYPE_TASK);

      LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, assignmentTaskBO);
    });
  }

  @DeleteMapping("/assignment-tasks/{id}")
  @Secured({"PERM_USER"})
  public void deleteAssignmentTask(@PathVariable @NotEmpty(message = "任务id不能为空") String id) {

    AssignmentTaskBO assignmentTaskBO = assignmentTaskService.deleteAssignmentTask(id,
        LoggedUserContext.getCurrentUser().getId());

    // 加入下发队列
    assignmentService.addAssignmentQueue(FpcCmsConstants.ASSIGNMENT_TYPE_TASK);

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, assignmentTaskBO);
  }

  @GetMapping("/assignment-task-files")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryAssignmentTaskFile(
      @RequestParam @NotEmpty(message = "设备序列号不能为空") String fpcSerialNumber,
      @RequestParam @NotEmpty(message = "任务id不能为空") String taskId, HttpServletRequest request) {

    // 写审计日志
    AssignmentTaskBO assignmentTaskBO = assignmentTaskService.queryAssignmentTask(taskId);
    assignmentTaskBO.setFpcSerialNumber(fpcSerialNumber);
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DOWNLOAD, assignmentTaskBO);

    return fpcManagerInvoker.downloadTransmitTaskFile(fpcSerialNumber, taskId,
        request.getServerName());
  }

  private void checkParameter(AssignmentTaskCreationVO assignmentTaskCreationVO) {
    // 校验过滤条件开始时间的日期格式
    Date filterStartDate = null;
    try {
      filterStartDate = DateUtils.parseISO8601Date(assignmentTaskCreationVO.getFilterStartTime());
    } catch (Exception exception) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的过滤开始时间");
    }

    // 校验过滤条件结束时间的日期格式
    Date filterEndDate = null;
    try {
      filterEndDate = DateUtils.parseISO8601Date(assignmentTaskCreationVO.getFilterEndTime());
    } catch (Exception exception) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的过滤结束时间");
    }

    // 校验过滤条件开始时间早于过滤条件结束时间
    if (filterEndDate.getTime() < filterStartDate.getTime()) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "过滤条件结束时间早于过滤条件开始时间");
    }

    // 校验所选探针是否存在
    List<String> selectedFpcSerialNumbers = CsvUtils
        .convertCSVToList(assignmentTaskCreationVO.getFpcSerialNumber());
    List<String> vaildFpcSerialNumbers = fpcService
        .queryFpcBySerialNumbers(selectedFpcSerialNumbers, true).stream()
        .map(FpcBO::getSerialNumber).collect(Collectors.toList());
    selectedFpcSerialNumbers.removeAll(vaildFpcSerialNumbers);
    if (CollectionUtils.isNotEmpty(selectedFpcSerialNumbers)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
          "不存在的探针序列号：" + CsvUtils.convertCollectionToCSV(selectedFpcSerialNumbers));
    }

    // 过滤类型校验
    if (!StringUtils.equalsAny(assignmentTaskCreationVO.getFilterConditionType(),
        CenterConstants.TRANSMIT_TASK_FILTER_TYPE_TUPLE,
        CenterConstants.TRANSMIT_TASK_FILTER_TYPE_BPF,
        CenterConstants.TRANSMIT_TASK_FILTER_TYPE_MIX)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的过滤条件类型");
    }

    // 规则条件校验
    if (!StringUtils.equals(CenterConstants.TRANSMIT_TASK_FILTER_TYPE_BPF,
        assignmentTaskCreationVO.getFilterConditionType())) {
      checkAndPaddingFilterTuple(assignmentTaskCreationVO);
    }

    // 过滤原始内容校验
    List<List<Map<String, String>>> filterRaw = JsonHelper.deserialize(
        assignmentTaskCreationVO.getFilterRaw(),
        new TypeReference<List<List<Map<String, String>>>>() {
        }, false);
    checkFilterRaw(filterRaw);

    // 检查任务类型
    switch (assignmentTaskCreationVO.getMode()) {
      case CenterConstants.TRANSMIT_TASK_MODE_FILE_PCAP:
        break;
      case CenterConstants.TRANSMIT_TASK_MODE_FILE_PCAPNG:
        break;
      default:
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的导出模式");
    }
  }

  /**
   * 校验六元组参数并填充默认值
   * @param assignmentTaskBO
   * @return
   */
  @SuppressWarnings("unchecked")
  private void checkAndPaddingFilterTuple(AssignmentTaskCreationVO assignmentTaskVO) {

    String filterTuple = assignmentTaskVO.getFilterTuple();

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
            "IP与 （源IP、目的IP）互斥，不可同时存在");
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
    assignmentTaskVO.setFilterTuple(JsonHelper.serialize(filterTupleArray, false));
  }

  /**
   * 校验内容匹配
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
        if (StringUtils.equals(type, FILTER_RAW_CONDITION_TYPE_ASCII)) {
          if (!ASCII_PATTERN.matcher(value).matches()) {
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的ASCII码");
          }
        } else if (StringUtils.equals(type, FILTER_RAW_CONDITION_TYPE_HEX)) {
          if (!HEX_PATTERN.matcher(value).matches() || value.length() % 2 != 0) {
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的16进制");
          }
        } else if (StringUtils.equals(type, FILTER_RAW_CONDITION_TYPE_REGULAR)) {
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
          if (value.getBytes().length > 60) {
            throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID,
                "抱歉，您输入的字符超过长度限制，请调整您输入的字符长度");
          }
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

  private Map<String, Object> taskToMap(AssignmentTaskBO assignmentTaskBO, boolean isDetail) {
    Map<String, Object> taskMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    taskMap.put("id", assignmentTaskBO.getId());
    taskMap.put("name", assignmentTaskBO.getName());
    taskMap.put("source", assignmentTaskBO.getSource());
    taskMap.put("filterStartTime", assignmentTaskBO.getFilterStartTime());
    taskMap.put("filterEndTime", assignmentTaskBO.getFilterEndTime());
    taskMap.put("filterNetworkId", assignmentTaskBO.getFilterNetworkId());
    taskMap.put("filterConditionType", assignmentTaskBO.getFilterConditionType());
    taskMap.put("filterConditionTypeText", assignmentTaskBO.getFilterConditionTypeText());
    taskMap.put("executionStartTime", assignmentTaskBO.getExecutionStartTime());
    taskMap.put("fpcSerialNumber", assignmentTaskBO.getFpcSerialNumber());
    taskMap.put("mode", assignmentTaskBO.getMode());
    taskMap.put("modeText", assignmentTaskBO.getModeText());

    if (isDetail) {
      taskMap.put("filterTuple", assignmentTaskBO.getFilterTuple());
      taskMap.put("filterBpf", assignmentTaskBO.getFilterBpf());
      taskMap.put("filterRaw", assignmentTaskBO.getFilterRaw());
      taskMap.put("description", assignmentTaskBO.getDescription());
      taskMap.put("executionEndTime", assignmentTaskBO.getExecutionEndTime());
      taskMap.put("executionProgress", assignmentTaskBO.getExecutionProgress());
    }

    return taskMap;
  }

  private Map<String, Object> taskRecordToMap(AssignmentTaskRecordBO assignmentTaskRecordBO) {
    Map<String, Object> taskRecordMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    taskRecordMap.put("fpcSerialNumber", assignmentTaskRecordBO.getFpcSerialNumber());
    taskRecordMap.put("fpcName", assignmentTaskRecordBO.getFpcName());
    taskRecordMap.put("fpcIp", assignmentTaskRecordBO.getFpcIp());
    taskRecordMap.put("connectStatus", assignmentTaskRecordBO.getConnectStatus());
    taskRecordMap.put("connectStatusText", assignmentTaskRecordBO.getConnectStatusText());
    taskRecordMap.put("assignmentState", assignmentTaskRecordBO.getAssignmentState());
    taskRecordMap.put("assignmentStateText", assignmentTaskRecordBO.getAssignmentStateText());
    taskRecordMap.put("executionState", assignmentTaskRecordBO.getExecutionState());
    taskRecordMap.put("executionStateText", assignmentTaskRecordBO.getExecutionStateText());
    taskRecordMap.put("executionTrace", assignmentTaskRecordBO.getExecutionTrace());
    taskRecordMap.put("executionStartTime", assignmentTaskRecordBO.getExecutionStartTime());
    taskRecordMap.put("executionEndTime", assignmentTaskRecordBO.getExecutionEndTime());
    taskRecordMap.put("executionProgress", assignmentTaskRecordBO.getExecutionProgress());

    return taskRecordMap;
  }

  private Map<String, Object> assignmentActionToMap(AssignmentActionBO assignmentActionBO) {
    Map<String,
        Object> assignmentActionMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    assignmentActionMap.put("fpcSerialNumber", assignmentActionBO.getFpcSerialNumber());
    assignmentActionMap.put("fpcName", assignmentActionBO.getFpcName());
    assignmentActionMap.put("assignmentState", assignmentActionBO.getAssignmentState());
    assignmentActionMap.put("assignmentStateText", assignmentActionBO.getAssignmentStateText());
    assignmentActionMap.put("connectStatus", assignmentActionBO.getConnectStatus());
    assignmentActionMap.put("connectStatusText", assignmentActionBO.getConnectStatusText());
    assignmentActionMap.put("assignmentTime", assignmentActionBO.getAssignmentTime());

    return assignmentActionMap;
  }

}
