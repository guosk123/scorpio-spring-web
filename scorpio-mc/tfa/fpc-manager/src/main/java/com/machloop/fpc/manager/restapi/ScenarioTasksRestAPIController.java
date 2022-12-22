package com.machloop.fpc.manager.restapi;

import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;
import javax.validation.constraints.NotEmpty;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.eclipsesource.v8.V8ScriptExecutionException;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.common.util.NetworkUtils.IpVersion;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.alpha.webapp.system.bo.UserBO;
import com.machloop.alpha.webapp.system.service.UserService;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.analysis.bo.ScenarioCustomTemplateBO;
import com.machloop.fpc.manager.analysis.bo.ScenarioTaskBO;
import com.machloop.fpc.manager.analysis.dao.postgres.ScenarioCustomDaoImpl;
import com.machloop.fpc.manager.analysis.service.ScenarioCustomService;
import com.machloop.fpc.manager.analysis.service.ScenarioTaskResultService;
import com.machloop.fpc.manager.analysis.service.ScenarioTaskService;
import com.machloop.fpc.manager.analysis.vo.ScenarioCustomTemplateCreationVO;
import com.machloop.fpc.manager.analysis.vo.ScenarioTaskCreationVO;
import com.machloop.fpc.manager.analysis.vo.ScenarioTaskQueryVO;
import com.machloop.fpc.manager.helper.Spl2SqlHelper;
import com.machloop.fpc.manager.restapi.vo.RestAPIResultVO;


/**
 * @author fengtianyou
 *
 * create at 2021年9月23日, fpc-manager
 */

@RestController
@RequestMapping("/restapi/fpc-v1/appliance")
public class ScenarioTasksRestAPIController {

  @Autowired
  private UserService userService;
  @Autowired
  private ScenarioTaskService scenarioTaskService;
  @Autowired
  private ScenarioTaskResultService scenarioTaskResultService;
  @Autowired
  private DictManager dictManager;

  @Autowired
  private ScenarioCustomService scenarioCustomService;

  @Autowired
  private Spl2SqlHelper spl2SqlHelper;

  private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioCustomDaoImpl.class);

  private static final List<String> DATA_SOURCE = Lists.newArrayList("http", "dns", "mail", "ftp",
      "telnet", "ssl", "flow-log-record");

  private static final List<String> IP_FIELD = Lists.newArrayList("src_ipv4<IPv4>",
      "dest_ipv4<IPv4>", "src_ipv6<IPv6>", "dest_ipv6<IPv6>");

  private static final List<String> PORT_FIELD = Lists.newArrayList("src_port", "dest_port");

  private static final List<String> STRING_FIELD = Lists.newArrayList("policy_name");

  private static final List<
      List<String>> COMMON_FIELD = Lists.newArrayList(IP_FIELD, PORT_FIELD, STRING_FIELD);

  private static final List<String> HTTP_FIELD = Lists.newArrayList("file_name", "file_type",
      "file_flag", "method", "host", "uri", "xff");

  private static final List<
      String> DNS_FIELD = Lists.newArrayList("domain<Array>", "dns_rcode", "dns_rcode_name");
  private static final List<String> DNS_IP_FIELD = Lists.newArrayList("domain_ipv4<Array<IPv4>>",
      "domain_ipv6<Array<IPv6>>");

  private static final List<String> FTP_FIELD = Lists.newArrayList("user");

  private static final List<String> MAIL_FIELD = Lists.newArrayList("protocol", "from", "to",
      "subject", "cc", "bcc", "attachment");

  private static final List<String> TELNET_FIELD = Lists.newArrayList("username", "cmd");

  private static final List<String> SSL_FIELD = Lists.newArrayList("server_name", "ja3_client",
      "ja3_server", "version", "issuer", "common_name");

  private static final List<
      String> SESSION_PORT_FIELD = Lists.newArrayList("port_initiator", "port_responder");
  private static final List<String> SESSION_IP_FIELD = Lists.newArrayList("ipv4_initiator<IPv4>",
      "ipv4_responder<IPv4>", "ipv6_initiator<IPv6>", "ipv6_responder<IPv6>");
  private static final List<String> SESSION_FIELD = Lists.newArrayList("interface", "duration",
      "ethernet_initiator", "ethernet_responder", "ethernet_protocol", "ip_protocol", "l7_protocol",
      "application_name", "country_initiator", "province_initiator", "city_initiator",
      "country_responder", "province_responder", "city_responder", "upstream_bytes",
      "downstream_bytes", "total_bytes", "upstream_packets", "downstream_packets", "total_packets",
      "upstream_payload_bytes", "downstream_payload_bytes", "total_payload_bytes");

  private static final List<String> FUNCTION_FIELD = Lists.newArrayList("count", "sum", "beacon");

  private static final List<String> GROUPBY_FIELD = Lists.newArrayList("src_ip", "dest_ip",
      "src_ip&dest_ip", "src_ip&dest_ip&src_port", "src_ip&dest_ip&dest_port",
      "src_ip&dest_ip&src_port&dest_port");

  @GetMapping("/scenario-tasks")
  @RestApiSecured
  public RestAPIResultVO queryScenarioTasks(
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
      ScenarioTaskQueryVO queryVO) {
    Sort sort = new Sort(Sort.Direction.DESC, "create_time");
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);
    Page<ScenarioTaskBO> scenarioTasksPage = scenarioTaskService.queryScenarioTasks(page, queryVO);
    return RestAPIResultVO.resultSuccess(scenarioTasksPage);
  }

  @GetMapping("/scenario-tasks/{id}")
  @RestApiSecured
  public RestAPIResultVO queryTask(@PathVariable String id) {
    ScenarioTaskBO scenarioTaskBO = scenarioTaskService.queryScenarioTask(id);
    if (StringUtils.isBlank(scenarioTaskBO.getId())) {
      return new RestAPIResultVO.Builder(FpcConstants.OBJECT_NOT_FOUND_CODE).msg("查询的场景不存在")
          .build();
    }

    return RestAPIResultVO.resultSuccess(scenarioTaskBO);
  }

  @PostMapping("/scenario-tasks")
  @RestApiSecured
  public RestAPIResultVO saveScenarioTasks(
      @RequestBody @Validated ScenarioTaskCreationVO scenarioTaskVO, BindingResult bindingResult,
      HttpServletRequest request) {

    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));
    RestAPIResultVO restAPIResultVO = checkParameter(bindingResult, scenarioTaskVO);
    if (restAPIResultVO != null) {
      return restAPIResultVO;
    }

    ScenarioTaskBO scenarioTaskBO = new ScenarioTaskBO();
    BeanUtils.copyProperties(scenarioTaskVO, scenarioTaskBO);
    try {
      scenarioTaskBO
          .setDescription(StringUtils.defaultIfBlank(scenarioTaskBO.getDescription(), ""));
      scenarioTaskBO = scenarioTaskService.saveScenarioTask(scenarioTaskBO, userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, scenarioTaskBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(scenarioTaskBO);
  }

  @DeleteMapping("/scenario-tasks/{id}")
  @RestApiSecured
  public RestAPIResultVO deleteScenarioTask(
      @PathVariable @NotEmpty(message = "删除分析任务时传入的id不能为空") String id, HttpServletRequest request) {
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));
    ScenarioTaskBO scenarioTaskBO = null;
    try {
      scenarioTaskBO = scenarioTaskService.deleteScenarioTask(id, userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, scenarioTaskBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(id);
  }


  @GetMapping("/scenario-tasks/{id}/results")
  @RestApiSecured
  public RestAPIResultVO queryScenarioTaskResults(
      @RequestParam(required = false, defaultValue = "record_total_hit") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
      @PathVariable String id, @RequestParam String type, String query) {
    Sort sort = new Sort(Sort.Direction.fromString(sortDirection), sortProperty);
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);
    Page<Map<String, Object>> resultBO = scenarioTaskResultService.queryScenarioTaskResults(page,
        id, type, query);

    return RestAPIResultVO.resultSuccess(resultBO);
  }

  @GetMapping("/scenario-tasks/{id}/results/as-terms")
  @RestApiSecured
  public RestAPIResultVO queryScenarioTaskTermsResults(
      @RequestParam(required = false, defaultValue = "record_total_hit") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      @PathVariable String id, @RequestParam String termField,
      @RequestParam(required = false, defaultValue = "1000") int termSize,
      @RequestParam String type) {
    Sort sort = new Sort(Sort.Direction.fromString(sortDirection), sortProperty);
    List<Map<String, Object>> resultBO = scenarioTaskResultService
        .queryScenarioTaskTermsResults(sort, id, type, termField, termSize);

    return RestAPIResultVO.resultSuccess(resultBO);

  }

  private RestAPIResultVO checkParameter(BindingResult bindingResult,
      ScenarioTaskCreationVO scenarioTaskVO) {

    Map<String,
        String> typeDict = dictManager.getBaseDict().getItemMap("analysis_scenario_task_type");
    // 初步校验
    if (bindingResult.hasErrors()) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(bindingResult.getFieldError().getDefaultMessage()).build();
    }

    // 时间校验
    try {
      Date startTime = DateUtils.parseISO8601Date(scenarioTaskVO.getAnalysisStartTime());
      Date endTime = DateUtils.parseISO8601Date(scenarioTaskVO.getAnalysisEndTime());
      if (DateUtils.beforeDayDate(endTime, 1).after(startTime)) {

        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
            .msg(scenarioTaskVO.getAnalysisStartTime() + "格式非法, 请输入正确的时间").build();
      }
    } catch (Exception exception) {

      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(scenarioTaskVO.getAnalysisStartTime() + "格式非法, 请输入正确的时间").build();
    }
    // 类型校验
    if (!typeDict.containsKey(scenarioTaskVO.getType())) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(scenarioTaskVO.getType() + "类型非法,请输入正确的类型").build();
    }

    return null;
  }

  @GetMapping("/scenario-task/custom-templates")
  @RestApiSecured
  public RestAPIResultVO queryScenarioCustomTemplates(
      @RequestParam(defaultValue = "true") boolean isDetail) {

    List<
        Map<String, Object>> customTemplates = scenarioCustomService.queryCustomTemplates().stream()
            .map(item -> scenarioCustomTemplateBO2Map(item, isDetail)).collect(Collectors.toList());

    return RestAPIResultVO.resultSuccess(customTemplates);

  }

  @PostMapping("/scenario-task/custom-templates")
  @RestApiSecured
  public RestAPIResultVO saveScenarioCustomTemplate(
      @Validated ScenarioCustomTemplateCreationVO creationVO, HttpServletRequest request) {
    ScenarioCustomTemplateBO customTemplateBO = new ScenarioCustomTemplateBO();
    BeanUtils.copyProperties(creationVO, customTemplateBO);

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    ScenarioCustomTemplateBO customTemplate = new ScenarioCustomTemplateBO();

    // 初始化
    if (StringUtils.isBlank(creationVO.getGroupBy())) {
      customTemplateBO.setGroupBy("");
    }
    if (StringUtils.isBlank(creationVO.getDescription())) {
      customTemplateBO.setDescription("");
    }

    // 校验数据源
    String dataSource = creationVO.getDataSource();
    if (StringUtils.isNotBlank(dataSource) && !DATA_SOURCE.contains(dataSource)) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg("不合法的数据源类型：" + dataSource).build();
    }

    // 校验filterSpl
    Map<String, Object> checkFilterSpl = checkFilterSpl(dataSource, creationVO.getFilterSpl());
    if (StringUtils.isNotBlank(MapUtils.getString(checkFilterSpl, "msg"))) {
      return new RestAPIResultVO.Builder(MapUtils.getIntValue(checkFilterSpl, "code"))
          .msg(MapUtils.getString(checkFilterSpl, "msg")).build();
    } else if (MapUtils.getString(checkFilterSpl, "result") == FpcConstants.OPERAND_NOTIN_FIELD) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("不合规的输入，请参考说明文档")
          .build();
    }

    // 校验groupBy
    Map<String, Object> checkGroupBy = checkGroupBy(creationVO.getGroupBy());
    if (MapUtils.isNotEmpty(checkGroupBy)) {
      return new RestAPIResultVO.Builder(MapUtils.getIntValue(checkGroupBy, "code"))
          .msg(MapUtils.getString(checkGroupBy, "msg")).build();
    }

    // 校验function
    Map<String, Object> checkFunction = checkFunction(creationVO.getFunction());
    if (MapUtils.isNotEmpty(checkFunction)) {
      return new RestAPIResultVO.Builder(MapUtils.getIntValue(checkFunction, "code"))
          .msg(MapUtils.getString(checkFunction, "msg")).build();
    }

    try {
      customTemplate = scenarioCustomService.saveCustomTemplate(customTemplateBO, userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, customTemplate, userBO.getFullname(),
        userBO.getName());
    return RestAPIResultVO.resultSuccess(customTemplate.getId());
  }

  @DeleteMapping("/scenario-task/custom-templates/{id}")
  @RestApiSecured
  public RestAPIResultVO deleteScenarioCustomTemplate(@PathVariable String id,
      HttpServletRequest request) {

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));
    ScenarioCustomTemplateBO customTemplate = new ScenarioCustomTemplateBO();

    try {
      customTemplate = scenarioCustomService.deleteCustomTemplate(id, userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, customTemplate, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(customTemplate.getId());
  }

  private Map<String, Object> scenarioCustomTemplateBO2Map(ScenarioCustomTemplateBO customTemplate,
      boolean isDetail) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", customTemplate.getId());
    map.put("name", customTemplate.getName());

    if (isDetail) {
      map.put("filterSpl", customTemplate.getFilterSpl());
      map.put("dataSource", customTemplate.getDataSource());
      map.put("function", customTemplate.getFunction());
      map.put("groupBy", customTemplate.getGroupBy());
      map.put("sliceTimeInterval", customTemplate.getSliceTimeInterval());
      map.put("avgTimeInterval", customTemplate.getAvgTimeInterval());
      map.put("description", customTemplate.getDescription());
      map.put("createTime", customTemplate.getCreateTime());
      map.put("updateTime", customTemplate.getUpdateTime());
    }

    return map;
  }

  private Map<String, Object> checkGroupBy(String groupBy) {
    Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(groupBy) && !GROUPBY_FIELD.contains(groupBy)) {
      resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
      resultMap.put("msg", "groupBy不合法:" + groupBy);
    }
    return resultMap;
  }

  private Map<String, Object> checkFunction(String function) {
    Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(function) && !FUNCTION_FIELD.contains(function)) {
      resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
      resultMap.put("msg", "function不合法:" + function);
    }
    return resultMap;
  }

  private Map<String, Object> checkFilterSpl(String dataSource, String dsl) {
    // 使用dsl表达式查询
    List<Map<String, Object>> filterContents = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    if (StringUtils.isNotBlank(dsl)) {
      try {
        filterContents = spl2SqlHelper.getFilterContent(dsl);
      } catch (IOException e) {
        LOGGER.warn("failed to convert dsl.", e);
        throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "解析表达式失败");
      } catch (V8ScriptExecutionException e) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "表达式格式错误");
      }
    }

    for (Map<String, Object> fieldMap : filterContents) {
      String field = MapUtils.getString(fieldMap, "field");
      String operand = MapUtils.getString(fieldMap, "operand");

      if (dataSource.equals("http")) {
        resultMap.putAll(commonFieldCheck(COMMON_FIELD, field, operand));
        if (StringUtils.isNotBlank(MapUtils.getString(resultMap, "msg"))) {
          return resultMap;
        } else if (MapUtils.getString(resultMap, "result") == FpcConstants.CHECK_SUCCESS) {
          // 校验成功，取for循环的下一个值进行校验
          continue;
        }
        resultMap.putAll(stringFieldCheck(HTTP_FIELD, field, operand));
        if (StringUtils.isNotBlank(MapUtils.getString(resultMap, "msg"))) {
          return resultMap;
        }
        return resultMap;
      }

      if (dataSource.equals("dns")) {
        resultMap.putAll(commonFieldCheck(COMMON_FIELD, field, operand));
        if (StringUtils.isNotBlank(MapUtils.getString(resultMap, "msg"))) {
          return resultMap;
        } else if (MapUtils.getString(resultMap, "result") == FpcConstants.CHECK_SUCCESS) {
          // 校验成功，取for循环的下一个值进行校验
          continue;
        }
        resultMap.putAll(ipFieldCheck(DNS_IP_FIELD, field, operand));
        if (StringUtils.isNotBlank(MapUtils.getString(resultMap, "msg"))) {
          return resultMap;
        } else if (MapUtils.getString(resultMap, "result") == FpcConstants.CHECK_SUCCESS) {
          // 校验成功，取for循环的下一个值进行校验
          continue;
        }
        resultMap.putAll(stringFieldCheck(DNS_FIELD, field, operand));
        if (StringUtils.isNotBlank(MapUtils.getString(resultMap, "msg"))) {
          return resultMap;
        }
        return resultMap;
      }

      if (dataSource.equals("ftp")) {
        resultMap.putAll(stringFieldCheck(FTP_FIELD, field, operand));
        if (StringUtils.isNotBlank(MapUtils.getString(resultMap, "msg"))) {
          return resultMap;
        }
        return resultMap;
      }

      if (dataSource.equals("mail")) {
        resultMap.putAll(commonFieldCheck(COMMON_FIELD, field, operand));
        if (StringUtils.isNotBlank(MapUtils.getString(resultMap, "msg"))) {
          return resultMap;
        } else if (MapUtils.getString(resultMap, "result") == FpcConstants.OPERAND_NOTIN_FIELD) {
          // 校验成功，取for循环的下一个值进行校验
          continue;
        }
        resultMap.putAll(stringFieldCheck(MAIL_FIELD, field, operand));
        if (StringUtils.isNotBlank(MapUtils.getString(resultMap, "msg"))) {
          return resultMap;
        }
        return resultMap;
      }


      if (dataSource.equals("telnet")) {
        resultMap.putAll(commonFieldCheck(COMMON_FIELD, field, operand));
        if (MapUtils.isNotEmpty(resultMap)) {
          return resultMap;
        }
        resultMap.putAll(stringFieldCheck(TELNET_FIELD, field, operand));
        if (MapUtils.isNotEmpty(resultMap)) {
          return resultMap;
        }
        return resultMap;
      }

      if (dataSource.equals("ssl")) {
        resultMap.putAll(commonFieldCheck(COMMON_FIELD, field, operand));
        if (MapUtils.isNotEmpty(resultMap)) {
          return resultMap;
        }
        resultMap.putAll(stringFieldCheck(SSL_FIELD, field, operand));
        if (MapUtils.isNotEmpty(resultMap)) {
          return resultMap;
        }
        return resultMap;
      }

      if (dataSource.equals("session")) {
        resultMap.putAll(stringFieldCheck(SESSION_FIELD, field, operand));
        if (MapUtils.isNotEmpty(resultMap)) {
          return resultMap;
        }
        resultMap.putAll(ipFieldCheck(SESSION_IP_FIELD, field, operand));
        if (MapUtils.isNotEmpty(resultMap)) {
          return resultMap;
        }
        resultMap.putAll(portFieldCheck(SESSION_PORT_FIELD, field, operand));
        if (MapUtils.isNotEmpty(resultMap)) {
          return resultMap;
        }
        return resultMap;
      }
    }
    return resultMap;
  }

  private Map<String, Object> commonFieldCheck(List<List<String>> check, String field,
      String operand) {
    Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    // ip地址校验
    List<String> ip_field = check.get(0);
    if (StringUtils.isNotBlank(field) && ip_field.contains(field)) {
      if (!(NetworkUtils.isInetAddress(operand, IpVersion.V4))) {
        resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
        resultMap.put("msg", "ipv4地址不合法:" + operand);
        return resultMap;
      } else if (!ip_field.contains(field)) {
        resultMap.put("result", FpcConstants.OPERAND_NOTIN_FIELD);
      }
      resultMap.put("result", FpcConstants.CHECK_SUCCESS);
      return resultMap;
    }
    if (StringUtils.isNotBlank(field) && ip_field.contains(field)) {
      if (!(NetworkUtils.isInetAddress(operand, IpVersion.V6))) {
        resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
        resultMap.put("msg", "ipv6地址不合法:" + operand);
        return resultMap;
      } else if (!ip_field.contains(field)) {
        resultMap.put("result", FpcConstants.OPERAND_NOTIN_FIELD);
      }
      resultMap.put("result", FpcConstants.CHECK_SUCCESS);
      return resultMap;
    }
    // 端口校验
    List<String> port_field = check.get(1);
    if (StringUtils.isNotBlank(field) && port_field.contains(field)) {
      if (!NetworkUtils.isInetAddressPort(String.valueOf(operand))) {
        resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
        resultMap.put("msg", "端口不合法:" + operand);
        return resultMap;
      } else if (!port_field.contains(field)) {
        resultMap.put("result", FpcConstants.OPERAND_NOTIN_FIELD);
      }
      resultMap.put("result", FpcConstants.CHECK_SUCCESS);
      return resultMap;
    }
    // 策略名称
    List<String> string_field = check.get(2);
    if (StringUtils.isNotBlank(field) && string_field.contains(field)) {
      if (!NetworkUtils.isInetAddressPort(String.valueOf(operand))) {
        resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
        resultMap.put("msg", operand + "不能为空！");
        return resultMap;
      } else if (!string_field.contains(field)) {
        resultMap.put("result", FpcConstants.OPERAND_NOTIN_FIELD);
        return resultMap;
      }
      resultMap.put("result", FpcConstants.CHECK_SUCCESS);
      return resultMap;
    }
    return resultMap;
  }

  private Map<String, Object> stringFieldCheck(List<String> check, String field, String operand) {
    Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(field) && check.contains(field)) {
      if (StringUtils.isBlank(operand)) {
        resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
        resultMap.put("msg", field + "不能为空！");
        return resultMap;
      } else if (!check.contains(field)) {
        resultMap.put("result", FpcConstants.OPERAND_NOTIN_FIELD);
        return resultMap;
      }
      resultMap.put("result", FpcConstants.CHECK_SUCCESS);
      return resultMap;
    }
    return resultMap;
  }

  private Map<String, Object> ipFieldCheck(List<String> check, String field, String operand) {
    Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(field) && check.contains(field)) {
      if (!(NetworkUtils.isInetAddress(operand, IpVersion.V4))) {
        resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
        resultMap.put("msg", "ipv4地址不合法");
        return resultMap;
      } else if (!check.contains(field)) {
        resultMap.put("result", FpcConstants.OPERAND_NOTIN_FIELD);
        return resultMap;
      }
      resultMap.put("result", FpcConstants.CHECK_SUCCESS);
      return resultMap;
    }
    if (StringUtils.isNotBlank(field) && check.contains(field)) {
      if (!(NetworkUtils.isInetAddress(operand, IpVersion.V6))) {
        resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
        resultMap.put("msg", "ipv6地址不合法");
        return resultMap;
      } else if (!check.contains(field)) {
        resultMap.put("result", FpcConstants.OPERAND_NOTIN_FIELD);
        return resultMap;
      }
      resultMap.put("result", FpcConstants.CHECK_SUCCESS);
      return resultMap;
    }
    return resultMap;
  }

  private Map<String, Object> portFieldCheck(List<String> check, String field, String operand) {
    Map<String, Object> resultMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    // 端口校验
    if (StringUtils.isNotBlank(field) && check.contains(field)) {
      if (!NetworkUtils.isInetAddressPort(String.valueOf(operand))) {
        resultMap.put("code", FpcConstants.ILLEGAL_PARAMETER_CODE);
        resultMap.put("msg", "端口不合法");
        return resultMap;
      } else if (!check.contains(field)) {
        resultMap.put("result", FpcConstants.OPERAND_NOTIN_FIELD);
        return resultMap;
      }
      resultMap.put("result", FpcConstants.CHECK_SUCCESS);
      return resultMap;
    }
    return resultMap;
  }
}
