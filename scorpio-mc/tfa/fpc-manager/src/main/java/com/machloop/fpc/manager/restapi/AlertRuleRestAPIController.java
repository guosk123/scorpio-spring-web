package com.machloop.fpc.manager.restapi;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.common.util.NetworkUtils;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.alpha.webapp.security.service.RestApiSecured;
import com.machloop.alpha.webapp.system.bo.UserBO;
import com.machloop.alpha.webapp.system.service.UserService;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.appliance.bo.AlertRuleBO;
import com.machloop.fpc.manager.appliance.service.AlertRuleService;
import com.machloop.fpc.manager.appliance.service.HostGroupService;
import com.machloop.fpc.manager.knowledge.service.GeoService;
import com.machloop.fpc.manager.knowledge.service.SaService;
import com.machloop.fpc.manager.restapi.vo.AlertRuleVO;
import com.machloop.fpc.manager.restapi.vo.RestAPIResultVO;
import com.machloop.fpc.manager.restapi.vo.AlertRuleVO.*;
import com.machloop.fpc.npm.appliance.bo.LogicalSubnetBO;
import com.machloop.fpc.npm.appliance.bo.NetworkBO;
import com.machloop.fpc.npm.appliance.dao.ServiceNetworkDao;
import com.machloop.fpc.npm.appliance.service.LogicalSubnetService;
import com.machloop.fpc.npm.appliance.service.NetworkService;

/**
 * @author guosk
 *
 * create at 2021年9月9日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/restapi/fpc-v1/appliance")
public class AlertRuleRestAPIController {

  private static final int MAX_NAME_LENGTH = 30;

  private static final String ALERT_SCOPE_ALL_NETWORK = "allNetwork";

  private static final List<
      Integer> ALERT_WINDOW_SECONDS = Lists.newArrayList(60, 300, 600, 1800, 3600);
  private static final List<
      Integer> ALERT_REFIRE_SECONDS = Lists.newArrayList(300, 900, 1800, 3600);
  private static final Range<Integer> RANGE_ALERT_TREND_WINDOW_COUNT = Range.closed(1, 60);
  private static final List<
      String> ALERT_ADVANCED_FILTER_OPERATOR = Lists.newArrayList("equal", "not_equal");
  private static final List<String> ALERT_ADVANCED_GROUP_OPERATOR = Lists.newArrayList("and", "or");
  private static final List<
      String> ALERT_FIRE_OPERATOR = Lists.newArrayList(">", ">=", "=", "<=", "<");

  private static final List<String> BASELINE_ILLEGAL_METRICS = Lists
      .newArrayList("broadcast_packets", "long_connections");
  private static final List<String> METRIC_SOURCES = Lists.newArrayList("ipAddress", "hostGroup",
      "application", "location");
  private static final List<
      String> METRIC_SOURCE_ILLEGAL_METRICS = Lists.newArrayList("established_tcp_sessions",
          "concurrent_tcp_sessions", "broadcast_packets", "long_connections");

  private static final String ALERT_TYPE_REPEATEDLY = "repeatedly";

  @Autowired
  private AlertRuleService alertRuleService;

  @Autowired
  private UserService userService;

  @Autowired
  private NetworkService networkService;

  @Autowired
  private LogicalSubnetService logicalSubnetService;

  @Autowired
  private HostGroupService hostGroupService;

  @Autowired
  private SaService saService;

  @Autowired
  private GeoService geoService;

  @Autowired
  private ServiceNetworkDao serviceNetworkDao;

  @Autowired
  private DictManager dictManager;

  @Autowired
  private GlobalSettingService globalSettingService;

  @GetMapping("/alert-rules")
  @RestApiSecured
  public RestAPIResultVO queryAlertRules() {
    List<AlertRuleBO> alertRules = alertRuleService.queryAlertRulesByCategory(null);

    List<Map<String, Object>> alertRuleList = alertRules.stream()
        .map(alertRule -> alertRuleBO2Map(alertRule)).collect(Collectors.toList());

    return RestAPIResultVO.resultSuccess(alertRuleList);
  }

  @GetMapping("/alert-rules/{id}")
  @RestApiSecured
  public RestAPIResultVO queryAlertRule(@PathVariable String id) {
    AlertRuleBO alertRuleBO = alertRuleService.queryAlertRule(id);

    if (StringUtils.isBlank(alertRuleBO.getId())) {
      return new RestAPIResultVO.Builder(FpcConstants.OBJECT_NOT_FOUND_CODE).msg("告警规则不存在").build();
    }

    return RestAPIResultVO.resultSuccess(alertRuleBO2Map(alertRuleBO));
  }

  @PostMapping("/alert-rules")
  @RestApiSecured
  public RestAPIResultVO saveAlertRule(@RequestBody @Validated AlertRuleVO alertRuleVO,
      BindingResult bindingResult, HttpServletRequest request) {
    if (globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_STATE)
        .equals(Constants.BOOL_YES)) {
      return new RestAPIResultVO.Builder(
          Integer.parseInt(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT))
              .msg("已启用集群模式，部分功能禁用，如需启用请断开集群连接").build();
    }
    RestAPIResultVO restAPIResultVO = checkParameter(bindingResult, alertRuleVO);
    if (restAPIResultVO != null) {
      return restAPIResultVO;
    }

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    AlertRuleBO alertRuleBO = new AlertRuleBO();
    try {
      BeanUtils.copyProperties(alertRuleVO, alertRuleBO);
      // 作用域赋值
      alertRuleBO.setNetworkIds(StringUtils.defaultIfBlank(alertRuleBO.getNetworkIds(), ""));
      alertRuleBO.setServiceIds(StringUtils.defaultIfBlank(alertRuleBO.getServiceIds(), ""));
      if (StringUtils.equals(alertRuleBO.getNetworkIds(), ALERT_SCOPE_ALL_NETWORK)
          && StringUtils.isNotBlank(alertRuleBO.getServiceIds())) {
        alertRuleBO.setServiceIds("");
      }
      ThresholdSettings thresholdSettings = alertRuleVO.getThresholdSettings();
      alertRuleBO.setThresholdSettings(
          thresholdSettings == null ? "" : JsonHelper.serialize(thresholdSettings, false));
      TrendSettings trendSettings = alertRuleVO.getTrendSettings();
      alertRuleBO.setTrendSettings(
          trendSettings == null ? "" : JsonHelper.serialize(trendSettings, false));
      AdvancedSettings advancedSettings = alertRuleVO.getAdvancedSettings();
      alertRuleBO.setAdvancedSettings(
          advancedSettings == null ? "" : JsonHelper.serialize(advancedSettings, false));
      Refire refire = alertRuleVO.getRefire();
      alertRuleBO.setRefire(refire == null ? "" : JsonHelper.serialize(refire, false));
      alertRuleBO.setDescription(StringUtils.defaultIfBlank(alertRuleBO.getDescription(), ""));
      alertRuleBO = alertRuleService.saveAlertRule(alertRuleBO, userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, alertRuleBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(alertRuleBO.getId());
  }

  @PutMapping("/alert-rules/{id}")
  @RestApiSecured
  public RestAPIResultVO updateAlertRule(@PathVariable String id,
      @RequestBody @Validated AlertRuleVO alertRuleVO, BindingResult bindingResult,
      HttpServletRequest request) {
    if (globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_STATE)
        .equals(Constants.BOOL_YES)) {
      return new RestAPIResultVO.Builder(
          Integer.parseInt(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT))
              .msg("已启用集群模式，部分功能禁用，如需启用请断开集群连接").build();
    }
    RestAPIResultVO restAPIResultVO = checkParameter(bindingResult, alertRuleVO);
    if (restAPIResultVO != null) {
      return restAPIResultVO;
    }

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    AlertRuleBO alertRuleBO = new AlertRuleBO();
    try {
      BeanUtils.copyProperties(alertRuleVO, alertRuleBO);
      // 作用域赋值
      alertRuleBO.setNetworkIds(StringUtils.defaultIfBlank(alertRuleBO.getNetworkIds(), ""));
      alertRuleBO.setServiceIds(StringUtils.defaultIfBlank(alertRuleBO.getServiceIds(), ""));
      if (StringUtils.equals(alertRuleBO.getNetworkIds(), ALERT_SCOPE_ALL_NETWORK)
          && StringUtils.isNotBlank(alertRuleBO.getServiceIds())) {
        alertRuleBO.setServiceIds("");
      }
      ThresholdSettings thresholdSettings = alertRuleVO.getThresholdSettings();
      alertRuleBO.setThresholdSettings(
          thresholdSettings == null ? "" : JsonHelper.serialize(thresholdSettings, false));
      TrendSettings trendSettings = alertRuleVO.getTrendSettings();
      alertRuleBO.setTrendSettings(
          trendSettings == null ? "" : JsonHelper.serialize(trendSettings, false));
      AdvancedSettings advancedSettings = alertRuleVO.getAdvancedSettings();
      alertRuleBO.setAdvancedSettings(
          advancedSettings == null ? "" : JsonHelper.serialize(advancedSettings, false));
      Refire refire = alertRuleVO.getRefire();
      alertRuleBO.setRefire(refire == null ? "" : JsonHelper.serialize(refire, false));
      alertRuleBO.setDescription(StringUtils.defaultIfBlank(alertRuleBO.getDescription(), ""));
      alertRuleBO = alertRuleService.updateAlertRule(id, alertRuleBO, userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, alertRuleBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(alertRuleBO.getId());
  }

  @PutMapping("/alert-rules/{id}/status")
  @RestApiSecured
  public RestAPIResultVO updateAlertRuleStatus(@PathVariable String id,
      @RequestBody Map<String, String> param, HttpServletRequest request) {
    if (globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_STATE)
        .equals(Constants.BOOL_YES)) {
      return new RestAPIResultVO.Builder(
          Integer.parseInt(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT))
              .msg("已启用集群模式，部分功能禁用，如需启用请断开集群连接").build();
    }
    if (MapUtils.isEmpty(param)
        || !StringUtils.equalsAny(param.get("status"), Constants.BOOL_NO, Constants.BOOL_YES)) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("将要更改的状态值不合法")
          .build();
    }

    // 获取用户信息
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    AlertRuleBO alertRuleBO = null;
    try {
      alertRuleBO = alertRuleService.updateAlertRuleStatus(id, param.get("status"), userBO.getId());
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_UPDATE, alertRuleBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(id);
  }

  @DeleteMapping("/alert-rules/{id}")
  @RestApiSecured
  public RestAPIResultVO deleteAlertRule(@PathVariable String id, HttpServletRequest request) {
    if (globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_STATE)
        .equals(Constants.BOOL_YES)) {
      return new RestAPIResultVO.Builder(
          Integer.parseInt(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT))
              .msg("已启用集群模式，部分功能禁用，如需启用请断开集群连接").build();
    }
    UserBO userBO = userService.queryUserByAppKey(request.getHeader("appKey"));

    AlertRuleBO alertRuleBO = new AlertRuleBO();
    try {
      alertRuleBO = alertRuleService.deleteAlertRule(id, userBO.getId(), false);
    } catch (BusinessException exception) {
      return RestAPIResultVO.resultFailed(exception);
    }

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, alertRuleBO, userBO.getFullname(),
        userBO.getName());

    return RestAPIResultVO.resultSuccess(id);
  }

  private RestAPIResultVO checkParameter(BindingResult bindingResult, AlertRuleVO alertRuleVO) {
    // 初步校验
    if (bindingResult.hasErrors()) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(bindingResult.getFieldError().getDefaultMessage()).build();
    }

    // 名称长度校验
    if (alertRuleVO.getName().length() > MAX_NAME_LENGTH) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(String.format("告警规则名称长度超过最大限制： [%s]", MAX_NAME_LENGTH)).build();
    }

    // 告警间隔
    Refire refire = alertRuleVO.getRefire();
    Map<String,
        String> typeDict = dictManager.getBaseDict().getItemMap("appliance_alert_rule_refire_type");
    if (!typeDict.containsKey(refire.getType())) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(String.format("不合法的告警间隔类型： [%s]", refire.getType())).build();
    }
    if (StringUtils.equals(refire.getType(), ALERT_TYPE_REPEATEDLY)
        && !ALERT_REFIRE_SECONDS.contains(refire.getSeconds())) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(String.format("不合法的告警间隔周期： [%s]", refire.getSeconds())).build();
    }

    // 作用域
    String networkIds = alertRuleVO.getNetworkIds();
    String serviceIds = alertRuleVO.getServiceIds();
    if (StringUtils.isAllBlank(networkIds, serviceIds)) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("告警作用域不能为空")
          .build();
    }
    // 网络作用域
    List<String> networkIdList = CsvUtils.convertCSVToList(networkIds);
    List<String> vaildNetworkIds = networkService.queryNetworks().stream().map(NetworkBO::getId)
        .collect(Collectors.toList());
    vaildNetworkIds.addAll(logicalSubnetService.queryLogicalSubnets().stream()
        .map(LogicalSubnetBO::getId).collect(Collectors.toList()));
    if (!vaildNetworkIds.containsAll(networkIdList)
        && !StringUtils.equals(networkIds, ALERT_SCOPE_ALL_NETWORK)) {
      networkIdList.removeAll(vaildNetworkIds);
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(String.format("网络作用域中存在无效的网络ID： %s", networkIdList)).build();
    }
    // 业务作用域
    if (StringUtils.isNotBlank(serviceIds)) {
      List<String> serviceNetworkList = serviceNetworkDao
          .queryServiceNetworks().stream().map(serviceNetwork -> StringUtils.joinWith("^",
              serviceNetwork.getServiceId(), serviceNetwork.getNetworkId()))
          .collect(Collectors.toList());
      List<String> serviceNetworkIdList = CsvUtils.convertCSVToList(serviceIds);
      if (!serviceNetworkList.containsAll(serviceNetworkIdList)) {
        serviceNetworkIdList.removeAll(serviceNetworkIdList);
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
            .msg(String.format("业务作用域中存在无效的业务ID和网络ID组合： %s", serviceNetworkIdList)).build();
      }
    }

    // 告警配置
    switch (alertRuleVO.getCategory()) {
      case FpcConstants.ALERT_CATEGORY_THRESHOLD:
        ThresholdSettings thresholdSettings = alertRuleVO.getThresholdSettings();
        if (thresholdSettings == null) {
          return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("阈值告警配置不能为空")
              .build();
        }
        RestAPIResultVO checkMetric = checkMetric(thresholdSettings.getMetrics(),
            alertRuleVO.getCategory());
        if (checkMetric != null) {
          return checkMetric;
        }
        RestAPIResultVO checkFireCriteria = checkFireCriteria(thresholdSettings.getFireCriteria());
        if (checkFireCriteria != null) {
          return checkFireCriteria;
        }
        break;
      case FpcConstants.ALERT_CATEGORY_TREND:
        TrendSettings trendSettings = alertRuleVO.getTrendSettings();
        if (trendSettings == null) {
          return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("基线告警配置不能为空")
              .build();
        }
        RestAPIResultVO checkMetric2 = checkMetric(trendSettings.getMetrics(),
            alertRuleVO.getCategory());
        if (checkMetric2 != null) {
          return checkMetric2;
        }
        RestAPIResultVO checkFireCriteria2 = checkFireCriteria(trendSettings.getFireCriteria());
        if (checkFireCriteria2 != null) {
          return checkFireCriteria2;
        }
        RestAPIResultVO checkTrendDefine = checkTrendDefine(trendSettings.getTrend());
        if (checkTrendDefine != null) {
          return checkTrendDefine;
        }
        break;
      case FpcConstants.ALERT_CATEGORY_ADVANCED:
        AdvancedSettings advancedSettings = alertRuleVO.getAdvancedSettings();
        if (advancedSettings == null) {
          return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("组合告警配置不能为空")
              .build();
        }
        FilterCondition fireCriteria = advancedSettings.getFireCriteria();
        if (fireCriteria == null) {
          return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("组合告警配置不能为空")
              .build();
        }
        if (!ALERT_ADVANCED_GROUP_OPERATOR.contains(fireCriteria.getOperator())) {
          return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
              .msg(String.format("不合法的组合告警匹配符:[%s]", fireCriteria.getOperator())).build();
        }
        List<FilterGroup> groups = fireCriteria.getGroup();
        for (FilterGroup group : groups) {
          RestAPIResultVO checkFilterCondition = checkFilterCondition(group);
          if (checkFilterCondition != null) {
            return checkFilterCondition;
          }
        }
        break;
      default:
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
            .msg(String.format("不合法的告警分类： [%s]", alertRuleVO.getCategory())).build();
    }

    return null;
  }

  /**
   * 校验指标
   * @param metrics
   * @return
   */
  private RestAPIResultVO checkMetric(Metrics metrics, String category) {
    if (metrics == null) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("指标配置不能为空")
          .build();
    }

    Map<String,
        String> metricDict = dictManager.getBaseDict().getItemMap("appliance_alert_rule_metric");
    Map<String, String> sourceTypeDict = dictManager.getBaseDict()
        .getItemMap("appliance_alert_rule_source");

    Metric numerator = metrics.getNumerator();
    if (numerator == null) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("指标配置不能为空")
          .build();
    }
    if (!metricDict.containsKey(numerator.getMetric())) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(String.format("不合法的指标： [%s]", numerator.getMetric())).build();
    }
    if (StringUtils.equals(category, FpcConstants.ALERT_CATEGORY_TREND)
        && BASELINE_ILLEGAL_METRICS.contains(numerator.getMetric())) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg(String
          .format("基线告警不支持指标： [%s]", CsvUtils.convertCollectionToCSV(BASELINE_ILLEGAL_METRICS)))
          .build();
    }
    String numeratorSourceType = numerator.getSourceType();
    String numeratorSourceValue = numerator.getSourceValue();
    if ((StringUtils.isNotBlank(numeratorSourceType) && StringUtils.isBlank(numeratorSourceValue))
        || (StringUtils.isBlank(numeratorSourceType)
            && StringUtils.isNotBlank(numeratorSourceValue))) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("数据源配置不合法")
          .build();
    }
    if (StringUtils.isNotBlank(numeratorSourceType)
        && !sourceTypeDict.containsKey(numeratorSourceType)) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(String.format("数据源类型不合法： [%s]", numeratorSourceType)).build();
    }
    if (METRIC_SOURCES.contains(numeratorSourceType)
        && METRIC_SOURCE_ILLEGAL_METRICS.contains(numerator.getMetric())) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(String.format("指定数据源时不支持配置指标： [%s]",
              CsvUtils.convertCollectionToCSV(METRIC_SOURCE_ILLEGAL_METRICS)))
          .build();
    }
    // 检测数据源格式是否合法
    RestAPIResultVO checkSourceValue = checkSourceValue(numeratorSourceType, numeratorSourceValue);
    if (checkSourceValue != null) {
      return checkSourceValue;
    }

    if (!metrics.getIsRatio() && metrics.getDenominator() != null) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("未选择比率时请勿配置分母指标")
          .build();
    }

    if (metrics.getIsRatio()) {
      Metric denominator = metrics.getDenominator();
      if (denominator == null) {
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
            .msg("选择比率时分母指标配置不能为空").build();
      }
      if (!metricDict.containsKey(denominator.getMetric())) {
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
            .msg(String.format("不合法的指标： [%s]", denominator.getMetric())).build();
      }
      if (StringUtils.equals(category, FpcConstants.ALERT_CATEGORY_TREND)
          && BASELINE_ILLEGAL_METRICS.contains(denominator.getMetric())) {
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg(String
            .format("基线告警不支持指标： [%s]", CsvUtils.convertCollectionToCSV(BASELINE_ILLEGAL_METRICS)))
            .build();
      }
      String denominatorSourceType = denominator.getSourceType();
      String denominatorSourceValue = denominator.getSourceValue();
      if ((StringUtils.isNotBlank(denominatorSourceType)
          && StringUtils.isBlank(denominatorSourceValue))
          || (StringUtils.isBlank(denominatorSourceType)
              && StringUtils.isNotBlank(denominatorSourceValue))) {
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("分母数据源配置不合法")
            .build();
      }
      if (METRIC_SOURCES.contains(denominatorSourceType)
          && METRIC_SOURCE_ILLEGAL_METRICS.contains(denominator.getMetric())) {
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
            .msg(String.format("指定数据源时不支持配置指标： [%s]",
                CsvUtils.convertCollectionToCSV(METRIC_SOURCE_ILLEGAL_METRICS)))
            .build();
      }
      if (!(StringUtils.equals(numeratorSourceType, denominatorSourceType)
          && StringUtils.equals(numeratorSourceValue, denominatorSourceValue))) {
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
            .msg("选择比率时分子分母数据源必须相同").build();
      }
    }

    return null;
  }

  /**
   * 校验数据源值
   * @param sourceType
   * @param sourceValue
   * @return
   */
  private RestAPIResultVO checkSourceValue(String sourceType, String sourceValue) {
    if (StringUtils.isBlank(sourceType)) {
      return null;
    }

    switch (sourceType) {
      case FpcConstants.ALERT_SOURCE_TYPE_IP:
        if (!NetworkUtils.isInetAddress(sourceValue)) {
          return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
              .msg(String.format("不合法的IP:[%s]", sourceValue)).build();
        }
        break;
      case FpcConstants.ALERT_SOURCE_TYPE_HOSTGROUP:
        if (StringUtils.isBlank(hostGroupService.queryHostGroup(sourceValue).getId())) {
          return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
              .msg(String.format("不存在的地址组ID:[%s]", sourceValue)).build();
        }
        break;
      case FpcConstants.ALERT_SOURCE_TYPE_APPLICATION:
        try {
          if (!saService.queryAllAppsIdNameMapping().keySet()
              .contains(Integer.parseInt(sourceValue))) {
            return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
                .msg(String.format("不存在的应用ID:[%s]", sourceValue)).build();
          }
        } catch (NumberFormatException e) {
          return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
              .msg(String.format("不合法的应用ID:[%s]", sourceValue)).build();
        }
        break;
      case FpcConstants.ALERT_SOURCE_TYPE_LOCATION:
        if (!geoService.queryAllLocationIdNameMapping().containsKey(sourceValue)) {
          return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
              .msg(String.format("不存在的地区ID:[%s]", sourceValue)).build();
        }
        break;
      default:
        break;
    }

    return null;
  }

  /**
   * 校验告警条件
   * @param fireCriteria
   * @return
   */
  private RestAPIResultVO checkFireCriteria(FireCriteria fireCriteria) {
    if (fireCriteria == null) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("告警条件不能为空")
          .build();
    }

    if (!ALERT_WINDOW_SECONDS.contains(fireCriteria.getWindowSeconds())) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(String.format("不合法的告警时间窗口:[%s]", fireCriteria.getWindowSeconds())).build();
    }

    Map<String, String> calculationTypeDict = dictManager.getBaseDict()
        .getItemMap("appliance_alert_rule_calculation_type");
    if (!calculationTypeDict.containsKey(fireCriteria.getCalculation())) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(String.format("不合法的告警计算方法:[%s]", fireCriteria.getCalculation())).build();
    }

    if (!ALERT_FIRE_OPERATOR.contains(fireCriteria.getOperator())) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(String.format("不合法的告警条件操作符:[%s]", fireCriteria.getOperator())).build();
    }

    return null;
  }

  /**
   * 基线定义
   * @param trendDefine
   * @return
   */
  private RestAPIResultVO checkTrendDefine(TrendDefine trendDefine) {
    if (trendDefine == null) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("基线告警基线定义不能为空")
          .build();
    }

    Map<String, String> weightingModelDict = dictManager.getBaseDict()
        .getItemMap("appliance_trend_weighting_model");
    if (!weightingModelDict.containsKey(trendDefine.getWeightingModel())) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(String.format("不合法的基线权重模型:[%s]", trendDefine.getWeightingModel())).build();
    }

    Map<String, String> windowModelDict = dictManager.getBaseDict()
        .getItemMap("appliance_alert_rule_window_model");
    if (!windowModelDict.containsKey(trendDefine.getWindowingModel())) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(String.format("不合法的基线窗口:[%s]", trendDefine.getWindowingModel())).build();
    }

    if (!RANGE_ALERT_TREND_WINDOW_COUNT.contains(trendDefine.getWindowingCount())) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(String.format("不合法的基线回顾周期:[%s]", trendDefine.getWindowingCount())).build();
    }

    return null;
  }

  public RestAPIResultVO checkFilterCondition(FilterGroup group) {
    String operator = group.getOperator();
    String alertRef = group.getAlertRef();
    List<FilterGroup> group2 = group.getGroup();

    if (!ALERT_ADVANCED_GROUP_OPERATOR.contains(operator)
        && !ALERT_ADVANCED_FILTER_OPERATOR.contains(operator)) {
      return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
          .msg(String.format("不合法的组合告警匹配符:[%s]", operator)).build();
    }


    if (ALERT_ADVANCED_FILTER_OPERATOR.contains(operator)) {
      if (StringUtils.isBlank(alertRuleService.queryAlertRule(alertRef).getId())) {
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE)
            .msg(String.format("组合告警指定的告警ID不存在:[%s]", alertRef)).build();
      }
    } else {
      if (CollectionUtils.isEmpty(group2)) {
        return new RestAPIResultVO.Builder(FpcConstants.ILLEGAL_PARAMETER_CODE).msg("组合告警配置不完整")
            .build();
      }
      for (FilterGroup item : group2) {
        return checkFilterCondition(item);
      }
    }

    return null;
  }

  private static Map<String, Object> alertRuleBO2Map(AlertRuleBO alertRuleBO) {
    Map<String, Object> map = Maps.newLinkedHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", alertRuleBO.getId());
    map.put("name", alertRuleBO.getName());
    map.put("category", alertRuleBO.getCategory());
    map.put("level", alertRuleBO.getLevel());
    Map<String,
        Object> thresholdSettings = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(alertRuleBO.getThresholdSettings())) {
      thresholdSettings = JsonHelper.deserialize(alertRuleBO.getThresholdSettings(),
          new TypeReference<Map<String, Object>>() {
          }, false);
    }
    map.put("thresholdSettings", thresholdSettings);
    Map<String, Object> trendSettings = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(alertRuleBO.getTrendSettings())) {
      trendSettings = JsonHelper.deserialize(alertRuleBO.getTrendSettings(),
          new TypeReference<Map<String, Object>>() {
          }, false);
    }
    map.put("trendSettings", trendSettings);

    Map<String,
        Object> advancedSettings = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(alertRuleBO.getAdvancedSettings())) {
      advancedSettings = JsonHelper.deserialize(alertRuleBO.getAdvancedSettings(),
          new TypeReference<Map<String, Object>>() {
          }, false);
    }
    map.put("advancedSettings", advancedSettings);
    Map<String, Object> refireMap = JsonHelper.deserialize(alertRuleBO.getRefire(),
        new TypeReference<Map<String, Object>>() {
        }, false);
    map.put("refire", refireMap);
    map.put("status", alertRuleBO.getStatus());
    map.put("networkIds", alertRuleBO.getNetworkIds());
    map.put("serviceIds", alertRuleBO.getServiceIds());
    map.put("description", alertRuleBO.getDescription());
    map.put("createTime", alertRuleBO.getCreateTime());

    return map;
  }

}

