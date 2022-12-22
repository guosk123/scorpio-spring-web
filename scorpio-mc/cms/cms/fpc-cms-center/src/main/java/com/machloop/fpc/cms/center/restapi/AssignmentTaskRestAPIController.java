package com.machloop.fpc.cms.center.restapi;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Array;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.webapp.WebappConstants;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.alpha.webapp.system.bo.UserBO;
import com.machloop.alpha.webapp.system.service.UserService;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.appliance.bo.AssignmentTaskBO;
import com.machloop.fpc.cms.center.appliance.bo.AssignmentTaskRecordBO;
import com.machloop.fpc.cms.center.appliance.bo.FilterTupleBO;
import com.machloop.fpc.cms.center.appliance.service.AssignmentTaskService;
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
import com.machloop.fpc.cms.center.restapi.vo.AssignmentTaskVO;
import com.machloop.fpc.cms.common.FpcCmsConstants;

import reactor.util.function.Tuple3;

@RestController
@RequestMapping("/restapi/fpc-cms-v1/appliance")
public class AssignmentTaskRestAPIController {

  private static final int MAX_NAME_LENGTH = 30;
  private static final int MAX_DESCRIPTION_LENGTH = 255;
  private static final int MAX_FILTER_TUPLE_NUMBER = 5;
  private static final int MIN_FILTER_TUPLE_VLANID = 0;
  private static final int MAX_FILTER_TUPLE_VLANID = 4094;
  private static final int MAX_FILTER_RAW_RULE_NUMBER = 10;
  private static final int MAX_FILTER_RAW_CONDITION_NUMBER = 5;

  private static final int TASK_FILE_CHECK_FAILED = 42003;
  private static final int FPC_CONNECT_EXCEPTION_CODE = 44201;
  private static final int TASK_NOT_EXIST_CODE = 46001;
  private static final int FPC_NOT_EXIST_CODE = 46002;

  private static final String FILTER_RAW_CONDITION_TYPE_ASCII = "ascii";
  private static final String FILTER_RAW_CONDITION_TYPE_HEX = "hex";
  private static final String FILTER_RAW_CONDITION_TYPE_REGULAR = "regular";
  private static final String FILTER_RAW_CONDITION_TYPE_CHINESE = "chinese";

  private static final List<String> IP_PROTOCOLS = Lists.newArrayList("TCP", "UDP", "ICMP", "SCTP");

  private static final Pattern MAC_PATTERN = Pattern
      .compile("^[A-Fa-f0-9]{2}([-,:][A-Fa-f0-9]{2}){5}$", Pattern.MULTILINE);
  private static final Pattern ASCII_PATTERN = Pattern.compile("[\\x20-\\x7e]{1,64}$",
      Pattern.MULTILINE);
  private static final Pattern HEX_PATTERN = Pattern.compile("-?[0-9a-fA-F]{2,128}$");

  @Autowired
  private AssignmentTaskService assignmentTaskService;

  @Autowired
  private FpcService fpcService;

  @Autowired
  private UserService userService;

  @Autowired
  private AssignmentService assignmentService;

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

  @GetMapping("/assignment-tasks/{id}/state")
  @RestApiSecured
  public List<Map<String, Object>> queryAssignmentRecords(@PathVariable String id) {

    // 根据taskId查询任务
    AssignmentTaskBO assignmentTaskBO = assignmentTaskService.queryAssignmentTask(id);
    if (StringUtils.isBlank(assignmentTaskBO.getId())) {
      return Lists.newArrayList();
    }

    List<AssignmentTaskRecordBO> taskrecords = assignmentTaskService
        .queryAssignmentTaskRecordsWithoutPage(id);
    List<Map<String, Object>> taskRecordList = Lists
        .newArrayListWithExpectedSize(taskrecords.size());
    taskrecords.forEach(record -> taskRecordList.add(taskRecordToMap(record)));

    return taskRecordList;
  }

  @PostMapping("/assignment-tasks")
  @RestApiSecured
  public Map<String, Object> saveAssignmentTask(
      @RequestBody @Validated AssignmentTaskVO assignmentTaskVO, BindingResult bindingResult,
      HttpServletRequest request) {
    Map<String,
        Object> resultMap = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    resultMap = checkParameter(assignmentTaskVO, bindingResult);
    if (MapUtils.isNotEmpty(resultMap)) {
      return resultMap;
    }

    // 获取第三方用户信息
    String appKey = request.getHeader("appKey");
    UserBO userBO = userService.queryUserByAppKey(appKey);

    // 保存任务
    AssignmentTaskBO assignmentTaskBO = new AssignmentTaskBO();
    BeanUtils.copyProperties(assignmentTaskVO, assignmentTaskBO);
    assignmentTaskBO
        .setDescription(StringUtils.defaultIfBlank(assignmentTaskBO.getDescription(), ""));
    assignmentTaskBO.setFilterBpf(StringUtils.defaultIfBlank(assignmentTaskBO.getFilterBpf(), ""));
    assignmentTaskBO.setFilterTuple(CollectionUtils.isEmpty(assignmentTaskVO.getFilterTuple()) ? ""
        : JsonHelper.serialize(assignmentTaskVO.getFilterTuple(), false));
    assignmentTaskBO.setFilterRaw(CollectionUtils.isEmpty(assignmentTaskVO.getFilterRaw()) ? ""
        : JsonHelper.serialize(assignmentTaskVO.getFilterRaw(), false));
    Map<String, Object> saveResult = saveAssignmentTask(assignmentTaskVO, userBO);

    if (WebappConstants.REST_RESULT_SUCCESS_CODE != (int) saveResult.get("code")) {
      return saveResult;
    }

    // 下发任务
    String taskId = (String) saveResult.get("taskId");
    try {
      assignmentTaskService.assignTask(taskId, assignmentTaskBO.getFpcSerialNumber(),
          userBO.getId());
    } catch (BusinessException e) {
      resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
      resultMap.put("msg", "未从fpcIds和groupIds中解析出对应的设备");
      return resultMap;
    } catch (Exception e) {
      resultMap.put("code", CenterConstants.SYSTEM_ERROR_CODE);
      resultMap.put("msg", "系统异常");
      return resultMap;
    }

    // 加入下发队列
    assignmentService.addAssignmentQueue(FpcCmsConstants.ASSIGNMENT_TYPE_TASK);

    // 写审计日志
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_ASSIGNMENT, assignmentTaskBO, userBO.getFullname(),
        userBO.getName());

    resultMap.put("code", WebappConstants.REST_RESULT_SUCCESS_CODE);
    resultMap.put("taskId", taskId);
    return resultMap;
  }

  @GetMapping("/assignment-task-files")
  @RestApiSecured
  public Map<String, Object> queryAssignmentTaskFile(@RequestParam String taskId,
      @RequestParam String fpcSerialNumber, HttpServletRequest request) {

    Map<String,
        Object> resultMap = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    // 根据taskId查询任务
    AssignmentTaskBO assignmentTaskBO = assignmentTaskService.queryAssignmentTask(taskId);
    if (StringUtils.isBlank(assignmentTaskBO.getId())) {
      resultMap.put("code", TASK_NOT_EXIST_CODE);
      resultMap.put("msg", "任务不存在");
      return resultMap;
    }

    // 根据fpcSerialNumber查询设备
    FpcBO fpcBO = fpcService.queryFpcBySerialNumber(fpcSerialNumber);

    if (fpcBO == null) {
      resultMap.put("code", FPC_NOT_EXIST_CODE);
      resultMap.put("msg", "设备不存在");
      return resultMap;
    } else if (StringUtils.equals(FpcCmsConstants.CONNECT_STATUS_ABNORMAL,
        fpcBO.getConnectStatus())) {
      resultMap.put("code", FPC_CONNECT_EXCEPTION_CODE);
      resultMap.put("msg", "设备连接异常");
      return resultMap;
    }

    // 远程请求获取文件下载地址
    String filePath = "";
    try {
      Map<String, Object> invokeResult = fpcManagerInvoker.downloadTransmitTaskFile(fpcSerialNumber,
          taskId, request.getServerName());
      if ((Integer) invokeResult.get("code") != WebappConstants.REST_RESULT_SUCCESS_CODE) {
        return invokeResult;
      }
      filePath = (String) invokeResult.get("filePath");
    } catch (BusinessException e) {
      resultMap.put("code", TASK_FILE_CHECK_FAILED);
      resultMap.put("msg", e.getMessage());
      return resultMap;
    } catch (Exception e) {
      resultMap.put("code", CenterConstants.SYSTEM_ERROR_CODE);
      resultMap.put("msg", "系统异常");
      return resultMap;
    }

    // 更新数据库内文件地址
    assignmentTaskService.updatePcapFileUrl(taskId, fpcSerialNumber, filePath);

    resultMap.put("code", WebappConstants.REST_RESULT_SUCCESS_CODE);
    resultMap.put("filePath", filePath);
    return resultMap;
  }

  /**
   * 创建查询任务
   * @param assignmentTaskBO
   * @param userBO
   * @return
   */
  private Map<String, Object> saveAssignmentTask(AssignmentTaskVO assignmentTaskVO, UserBO userBO) {
    Map<String,
        Object> resultMap = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    AssignmentTaskBO assignmentTaskBO = new AssignmentTaskBO();
    BeanUtils.copyProperties(assignmentTaskVO, assignmentTaskBO);
    assignmentTaskBO
        .setDescription(StringUtils.defaultIfBlank(assignmentTaskBO.getDescription(), ""));
    assignmentTaskBO.setFilterBpf(StringUtils.defaultIfBlank(assignmentTaskBO.getFilterBpf(), ""));
    assignmentTaskBO.setFilterTuple(CollectionUtils.isEmpty(assignmentTaskVO.getFilterTuple()) ? ""
        : JsonHelper.serialize(assignmentTaskVO.getFilterTuple(), false));
    assignmentTaskBO.setFilterRaw(CollectionUtils.isEmpty(assignmentTaskVO.getFilterRaw()) ? ""
        : JsonHelper.serialize(assignmentTaskVO.getFilterRaw(), false));
    assignmentTaskBO.setFilterNetworkId("ALL");
    if (StringUtils.equals(assignmentTaskBO.getFilterConditionType(),
        CenterConstants.TRANSMIT_TASK_FILTER_TYPE_BPF)) {
      assignmentTaskBO.setFilterBpf("");
    } else if (StringUtils.equals(assignmentTaskBO.getFilterConditionType(),
        CenterConstants.TRANSMIT_TASK_FILTER_TYPE_TUPLE)) {
      assignmentTaskBO.setFilterTuple("");
    } else {
      assignmentTaskBO.setFilterTuple("");
      assignmentTaskBO.setFilterBpf("");
    }
    assignmentTaskBO.setOperatorId(userBO.getId());
    assignmentTaskBO.setSource("REST" + "/" + userBO.getFullname() + "/" + userBO.getName());

    try {
      assignmentTaskBO = assignmentTaskService.saveAssignmentTask(assignmentTaskBO);
    } catch (BusinessException exception) {
      // BPF格式输入有误
      resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
      resultMap.put("msg", exception.getMessage());
      return resultMap;
    } catch (Exception exception) {
      resultMap.put("code", CenterConstants.SYSTEM_ERROR_CODE);
      resultMap.put("msg", "系统异常");
      return resultMap;
    }

    resultMap.put("code", WebappConstants.REST_RESULT_SUCCESS_CODE);
    resultMap.put("taskId", assignmentTaskBO.getId());

    return resultMap;
  }

  /**
   * 验证任务传入参数
   * @param bindingResult
   * @param assignmentTaskBO
   * @return
   */
  private Map<String, Object> checkParameter(AssignmentTaskVO assignmentTaskVO,
      BindingResult bindingResult) {
    Map<String,
        Object> resultMap = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    // 校验必填参数是否为空
    if (bindingResult.hasErrors()) {
      resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
      resultMap.put("msg", bindingResult.getFieldError().getDefaultMessage());
      return resultMap;
    }

    // 校验任务名称字符长度
    if (assignmentTaskVO.getName().length() > MAX_NAME_LENGTH) {
      resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
      resultMap.put("msg", "任务名称最多可输入" + MAX_NAME_LENGTH + "个字符");
      return resultMap;
    }

    // 校验过滤条件开始时间的日期格式
    Date filterStartDate = null;
    try {
      filterStartDate = DateUtils.parseISO8601Date(assignmentTaskVO.getFilterStartTime());
    } catch (Exception exception) {
      resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
      resultMap.put("msg", "不合法的日期格式");
      return resultMap;
    }

    // 校验过滤条件结束时间的日期格式
    Date filterEndDate = null;
    try {
      filterEndDate = DateUtils.parseISO8601Date(assignmentTaskVO.getFilterEndTime());
    } catch (Exception exception) {
      resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
      resultMap.put("msg", "不合法的日期格式");
      return resultMap;
    }

    // 校验过滤条件开始时间早于过滤条件结束时间
    if (filterEndDate.getTime() < filterStartDate.getTime()) {
      resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
      resultMap.put("msg", "过滤条件结束时间早于过滤条件开始时间");
      return resultMap;
    }

    // 校验描述字符长度
    if (StringUtils.isNotBlank(assignmentTaskVO.getDescription())
        && assignmentTaskVO.getDescription().length() > MAX_DESCRIPTION_LENGTH) {
      resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
      resultMap.put("msg", "描述最多可输入" + MAX_DESCRIPTION_LENGTH + "个字符");
      return resultMap;
    }

    // 校验过滤类型
    if (!StringUtils.equalsAny(assignmentTaskVO.getFilterConditionType(),
        CenterConstants.TRANSMIT_TASK_FILTER_TYPE_TUPLE,
        CenterConstants.TRANSMIT_TASK_FILTER_TYPE_BPF,
        CenterConstants.TRANSMIT_TASK_FILTER_TYPE_MIX)) {
      resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
      resultMap.put("msg", "不合法的过滤条件类型");
      return resultMap;
    }

    // 校验下发设备是否存在
    String fpcSerialNumbers = assignmentTaskVO.getFpcSerialNumber();
    if (StringUtils.isBlank(fpcSerialNumbers)) {
      resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
      resultMap.put("msg", "下发设备不能为空");
      return resultMap;
    }

    List<String> selectedFpcSerialNumbers = CsvUtils.convertCSVToList(fpcSerialNumbers);
    List<String> vaildFpcSerialNumbers = fpcService
        .queryFpcBySerialNumbers(selectedFpcSerialNumbers, true).stream()
        .map(FpcBO::getSerialNumber).collect(Collectors.toList());
    selectedFpcSerialNumbers.removeAll(vaildFpcSerialNumbers);
    if (CollectionUtils.isNotEmpty(selectedFpcSerialNumbers)) {
      resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
      resultMap.put("msg", "'fpcSerialNumber'中包含不存在的设备:{"
          + CsvUtils.convertCollectionToCSV(selectedFpcSerialNumbers) + "}");
      return resultMap;
    }

    // 检查任务类型
    if (!StringUtils.equalsAny(assignmentTaskVO.getMode(),
        CenterConstants.TRANSMIT_TASK_MODE_FILE_PCAP,
        CenterConstants.TRANSMIT_TASK_MODE_FILE_PCAPNG)) {
      resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
      resultMap.put("msg", "不合法的导出模式");
      return resultMap;
    }

    // 规则条件
    if (StringUtils.equals(CenterConstants.TRANSMIT_TASK_FILTER_TYPE_TUPLE,
        assignmentTaskVO.getFilterConditionType())) {
      return checkAndPaddingFilterTuple(assignmentTaskVO);
    }

    // 过滤原始内容校验
    Map<String, Object> checkFilterRawMap = checkFilterRaw(assignmentTaskVO.getFilterRaw());
    if (MapUtils.isNotEmpty(checkFilterRawMap)) {
      return checkFilterRawMap;
    }

    return resultMap;
  }

  /**
   * 校验六元组参数并填充默认值
   * @param assignmentTaskVO
   * @return
   */
  @SuppressWarnings("unchecked")
  private Map<String, Object> checkAndPaddingFilterTuple(AssignmentTaskVO assignmentTaskVO) {

    Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    List<FilterTupleBO> fileterTuple = assignmentTaskVO.getFilterTuple();
    if (CollectionUtils.isEmpty(fileterTuple)) {
      return resultMap;
    }

    if (fileterTuple.size() > MAX_FILTER_TUPLE_NUMBER) {
      resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
      resultMap.put("msg", "过滤规则数量超过" + MAX_FILTER_TUPLE_NUMBER + "个");
      return resultMap;
    }

    HashSet<FilterTupleBO> removal = Sets.newHashSet(fileterTuple);
    if (removal.size() < fileterTuple.size()) {
      resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
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
        resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
        resultMap.put("msg", "单个过滤规则内容不能为空");
        return resultMap;
      }

      if (tuple.getIp() != null && (tuple.getSourceIp() != null || tuple.getDestIp() != null)) {
        resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
        resultMap.put("msg", "IP与 （源IP、目的IP）互斥，不可同时存在");
        return resultMap;
      }

      if ((tuple.getPort() != null)
          && ((tuple.getSourcePort() != null) || tuple.getDestPort() != null)) {
        resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
        resultMap.put("msg", "端口 与 （源端口、目的端口） 互斥，不可同时存在");
        return resultMap;
      }

      if (tuple.getMacAddress() != null
          && (tuple.getSourceMacAddress() != null || tuple.getDestMacAddress() != null)) {
        resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
        resultMap.put("msg", "MAC地址 与 （源MAC地址、目的MAC地址） 互斥，不可同时存在");
        return resultMap;
      }

      if (tuple.getCountryId() != null
          && (tuple.getSourceCountryId() != null || tuple.getDestCountryId() != null)) {
        resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
        resultMap.put("msg", "国家 与 （源国家、目的国家） 互斥，不可同时存在");
        return resultMap;
      }

      if (tuple.getProvinceId() != null
          && (tuple.getSourceProvinceId() != null || tuple.getDestProvinceId() != null)) {
        resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
        resultMap.put("msg", "省份 与 （源省份、目的省份） 互斥，不可同时存在");
        return resultMap;
      }

      if (tuple.getCityId() != null
          && (tuple.getSourceCityId() != null || tuple.getDestCityId() != null)) {
        resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
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
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
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
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
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
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
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
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
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
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
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
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
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
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
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
              resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
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
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
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
              resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
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
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
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
              resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
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
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "参数[countryId]数据类型不合法");
            return resultMap;
          }
        }
        for (String countryId : countryIds) {
          if (countryId.contains("NOT_")) {
            countryId = countryId.substring(4);
          }
          if (StringUtils.isNotBlank(countryId) && !vaildCountryIds.contains(countryId)) {
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
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
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
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
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
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
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "参数[destCountryId]数据类型不合法");
            return resultMap;
          }
        }
        for (String destCountryId : destCountryIds) {
          if (destCountryId.contains("NOT_")) {
            destCountryId = destCountryId.substring(4);
          }
          if (StringUtils.isNotBlank(destCountryId) && !vaildCountryIds.contains(destCountryId)) {
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
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
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "参数[provinceId]数据类型不合法");
            return resultMap;
          }
        }
        for (String provinceId : provinceIds) {
          if (provinceId.contains("NOT_")) {
            provinceId = provinceId.substring(4);
          }
          if (StringUtils.isNotBlank(provinceId) && !vaildProvinceIds.contains(provinceId)) {
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
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
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
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
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
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
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
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
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
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
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "参数[cityId]数据类型不合法");
            return resultMap;
          }
        }
        for (String cityId : cityIds) {
          if (cityId.contains("NOT_")) {
            cityId = cityId.substring(4);
          }
          if (StringUtils.isNotBlank(cityId) && !vaildCityIds.contains(cityId)) {
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
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
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "参数[sourceCityId]数据类型不合法");
            return resultMap;
          }
        }
        for (String sourceCityId : sourceCityIds) {
          if (sourceCityId.contains("NOT_")) {
            sourceCityId = sourceCityId.substring(4);
          }
          if (StringUtils.isNotBlank(sourceCityId) && !vaildCityIds.contains(sourceCityId)) {
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
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
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "参数[destCityId]数据类型不合法");
            return resultMap;
          }
        }
        for (String destCityId : destCityIds) {
          if (destCityId.contains("NOT_")) {
            destCityId = destCityId.substring(4);
          }
          if (StringUtils.isNotBlank(destCityId) && !vaildCityIds.contains(destCityId)) {
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
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
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "参数[ipProtocol]数据类型不合法");
            return resultMap;
          }
        }
        for (String ipProtocol : ipProtocols) {
          if (ipProtocol.contains("NOT_")) {
            ipProtocol = ipProtocol.substring(4);
          }
          if (!IP_PROTOCOLS.contains(ipProtocol.toUpperCase())) {
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
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
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "参数[l7ProtocolId]数据类型不合法");
            return resultMap;
          }
        }
        for (String l7ProtocolId : l7ProtocolIds) {
          if (l7ProtocolId.contains("NOT_")) {
            l7ProtocolId = l7ProtocolId.substring(4);
          }
          if (StringUtils.isNotBlank(l7ProtocolId) && !vaildL7ProtocolIds.contains(l7ProtocolId)) {
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
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
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
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
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
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
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "参数[macAddress]数据类型不合法");
            return resultMap;
          }
        }
        for (String macAddress : macAddresses) {
          if (macAddress.contains("NOT_")) {
            macAddress = macAddress.substring(4);
          }
          if (StringUtils.isNotBlank(macAddress) && !MAC_PATTERN.matcher(macAddress).matches()) {
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
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
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
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
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
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
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
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
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
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
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
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
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "不合法的vlanid：" + vlanIdStr);
            return resultMap;
          }
        }
      } else {
        tuple.setVlanId("");
      }
    }
    assignmentTaskVO.setFilterTuple(fileterTuple);
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
      resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
      resultMap.put("msg", "内容匹配规则数量超过" + MAX_FILTER_RAW_RULE_NUMBER + "个");
      return resultMap;
    }

    if (Sets.newHashSet(filterRaws).size() != filterRaws.size()) {
      resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
      resultMap.put("msg", "存在重复的内容匹配规则");
      return resultMap;
    }

    for (List<Map<String, String>> item : filterRaws) {
      if (item.size() > MAX_FILTER_RAW_CONDITION_NUMBER) {
        resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
        resultMap.put("msg", "内容匹配规则数量超过" + MAX_FILTER_RAW_CONDITION_NUMBER + "个");
        return resultMap;
      }

      if (Sets.newHashSet(item).size() != item.size()) {
        resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
        resultMap.put("msg", "内容匹配规则内存在重复的条件");
        return resultMap;
      }

      for (Map<String, String> condition : item) {
        String type = condition.get("type");
        String value = condition.get("value");
        if (StringUtils.equals(type, FILTER_RAW_CONDITION_TYPE_ASCII)) {
          if (!ASCII_PATTERN.matcher(value).matches()) {
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "不合法的ASCII码");
            return resultMap;
          }
        } else if (StringUtils.equals(type, FILTER_RAW_CONDITION_TYPE_HEX)) {
          if (!HEX_PATTERN.matcher(value).matches() || value.length() % 2 != 0) {
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "不合法的16进制");
            return resultMap;
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
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "不合法的正则表达式");
            return resultMap;
          }
        } else if (StringUtils.equals(type, FILTER_RAW_CONDITION_TYPE_CHINESE)) {
          if (value.getBytes().length > 60) {
            resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
            resultMap.put("msg", "抱歉，您输入的字符超过长度限制，请调整您输入的字符长度");
            return resultMap;
          }
          value = new String(value.getBytes(StandardCharsets.UTF_8));
        } else {
          resultMap.put("code", CenterConstants.ILLEGAL_PARAMETER_CODE);
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
    taskRecordMap.put("pcapFileUrl", assignmentTaskRecordBO.getPcapFileUrl());

    return taskRecordMap;
  }

}
