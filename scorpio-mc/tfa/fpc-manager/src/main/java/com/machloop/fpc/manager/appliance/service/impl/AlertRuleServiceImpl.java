package com.machloop.fpc.manager.appliance.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.fpc.cms.common.FpcCmsConstants;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.appliance.bo.AlertRuleBO;
import com.machloop.fpc.manager.appliance.dao.AlertRuleDao;
import com.machloop.fpc.manager.appliance.dao.AlertScopeDao;
import com.machloop.fpc.manager.appliance.data.AlertRuleDO;
import com.machloop.fpc.manager.appliance.data.AlertScopeDO;
import com.machloop.fpc.manager.appliance.service.AlertRuleService;
import com.machloop.fpc.manager.cms.service.SyncConfigurationService;
import com.machloop.fpc.manager.cms.service.impl.MQAssignmentServiceImpl;
import com.machloop.fpc.manager.helper.MQMessageHelper;
import com.machloop.fpc.npm.appliance.bo.ServiceBO;
import com.machloop.fpc.npm.appliance.dao.LogicalSubnetDao;
import com.machloop.fpc.npm.appliance.dao.NetworkDao;
import com.machloop.fpc.npm.appliance.data.LogicalSubnetDO;
import com.machloop.fpc.npm.appliance.data.NetworkDO;
import com.machloop.fpc.npm.appliance.service.ServiceService;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author guosk
 *
 * create at 2020年10月29日, fpc-manager
 */
@Order(5)
@Service
public class AlertRuleServiceImpl implements AlertRuleService, SyncConfigurationService {

  @SuppressWarnings("unused")
  private static final Pattern ADVANCED_FIRE_CRITERIA_PATTERN = Pattern
      .compile("\"alertRef\":\"([^\\\"]*)\"", Pattern.MULTILINE);

  private static final Logger LOGGER = LoggerFactory.getLogger(AlertRuleServiceImpl.class);

  private static final int MAXIMUM_AMOUNT_RULE = 100;

  @Autowired
  private AlertRuleDao alertRuleDao;

  @Autowired
  private AlertScopeDao alertScopeDao;

  @Autowired
  private NetworkDao networkDao;

  @Autowired
  private LogicalSubnetDao logicalSubnetDao;

  @Autowired
  private ServiceService serviceService;

  @Autowired
  private GlobalSettingService globalSettingService;

  /**
   * @see com.machloop.fpc.manager.appliance.service.AlertRuleService#queryAlertRules(com.machloop.alpha.common.base.page.Pageable, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Page<AlertRuleBO> queryAlertRules(Pageable page, String name, String category,
      String level, String networkId, String serviceId) {
    Page<AlertRuleDO> alertRules = alertRuleDao.queryAlertRules(page, name, category, level,
        networkId, serviceId);

    // 查询告警作用域
    Map<String, Tuple2<List<String>, List<String>>> alertScopes = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    alertScopeDao.queryAlertScope().forEach(alertScope -> {
      Tuple2<List<String>,
          List<String>> tuple2 = alertScopes.getOrDefault(alertScope.getAlertId(),
              Tuples.of(Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE),
                  Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE)));

      if (StringUtils.equals(alertScope.getSourceType(), FpcConstants.SOURCE_TYPE_NETWORK)) {
        tuple2.getT1().add(alertScope.getNetworkId());
      } else {
        tuple2.getT2()
            .add(StringUtils.joinWith("^", alertScope.getServiceId(), alertScope.getNetworkId()));
      }

      alertScopes.put(alertScope.getAlertId(), tuple2);
    });

    List<AlertRuleBO> list = Lists.newArrayListWithCapacity(alertRules.getSize());
    if (alertRules.getSize() != 0) {
      alertRules.getContent().forEach(alertRuleDO -> {
        AlertRuleBO alertRuleBO = new AlertRuleBO();
        BeanUtils.copyProperties(alertRuleDO, alertRuleBO);
        Tuple2<List<String>, List<String>> tuple = alertScopes.get(alertRuleBO.getId());
        if (tuple != null) {
          alertRuleBO.setNetworkIds(StringUtils.join(tuple.getT1(), ","));
          alertRuleBO.setServiceIds(StringUtils.join(tuple.getT2(), ","));
          alertRuleBO.setCreateTime(DateUtils.toStringISO8601(alertRuleDO.getCreateTime()));
          list.add(alertRuleBO);
        }
      });
    }
    long totalElem = list.size();

    // 此类情况为alert_rule表里有数据，但alert_scope表里没数据，则直接将totalElem置为0
    return new PageImpl<>(list, page, list.size() == 0 ? 0 : totalElem);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.AlertRuleService#queryAlertRulesBySource(java.lang.String, java.lang.String)
   */
  @Override
  public List<String> queryAlertRulesBySource(String sourceType, String networkId,
      String serviceId) {
    List<AlertScopeDO> alertScope = alertScopeDao.queryAlertScope(sourceType, networkId, serviceId);

    return alertScope.stream().map(AlertScopeDO::getAlertId).collect(Collectors.toList());
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.AlertRuleService#queryAlertRulesByCategory(java.lang.String)
   */
  @Override
  public List<AlertRuleBO> queryAlertRulesByCategory(String category) {
    List<AlertRuleDO> alertRules = alertRuleDao.queryAlertRules(category);

    // 查询告警作用域
    Map<String, Tuple2<List<String>, List<String>>> alertScopes = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    alertScopeDao.queryAlertScope().forEach(alertScope -> {
      Tuple2<List<String>,
          List<String>> tuple2 = alertScopes.getOrDefault(alertScope.getAlertId(),
              Tuples.of(Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE),
                  Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE)));
      if (StringUtils.equals(alertScope.getSourceType(), FpcConstants.SOURCE_TYPE_NETWORK)) {
        tuple2.getT1().add(alertScope.getNetworkId());
      } else {
        tuple2.getT2()
            .add(StringUtils.joinWith("^", alertScope.getServiceId(), alertScope.getNetworkId()));
      }

      alertScopes.put(alertScope.getAlertId(), tuple2);
    });

    List<AlertRuleBO> list = Lists.newArrayListWithCapacity(alertRules.size());
    alertRules.forEach(alertRuleDO -> {
      AlertRuleBO alertRuleBO = new AlertRuleBO();
      BeanUtils.copyProperties(alertRuleDO, alertRuleBO);
      Tuple2<List<String>, List<String>> tuple = alertScopes.get(alertRuleBO.getId());
      if (tuple != null) {
        alertRuleBO.setNetworkIds(StringUtils.join(tuple.getT1(), ","));
        alertRuleBO.setServiceIds(StringUtils.join(tuple.getT2(), ","));
        alertRuleBO.setCreateTime(DateUtils.toStringISO8601(alertRuleDO.getCreateTime()));
        list.add(alertRuleBO);
      }
    });

    return list;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.AlertRuleService#queryAlertRule(java.lang.String)
   */
  @Override
  public AlertRuleBO queryAlertRule(String id) {
    AlertRuleDO alertRuleDO = alertRuleDao.queryAlertRule(id);

    List<String> networkIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<String> serviceIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    alertScopeDao.queryAlertScopeByAlertId(id).forEach(alertScope -> {
      if (StringUtils.equals(alertScope.getSourceType(), FpcConstants.SOURCE_TYPE_NETWORK)) {
        networkIds.add(alertScope.getNetworkId());
      } else {
        serviceIds
            .add(StringUtils.joinWith("^", alertScope.getServiceId(), alertScope.getNetworkId()));
      }
    });

    AlertRuleBO alertRuleBO = new AlertRuleBO();
    BeanUtils.copyProperties(alertRuleDO, alertRuleBO);
    alertRuleBO.setNetworkIds(StringUtils.join(networkIds, ","));
    alertRuleBO.setServiceIds(StringUtils.join(serviceIds, ","));
    alertRuleBO.setCreateTime(DateUtils.toStringISO8601(alertRuleDO.getCreateTime()));

    return alertRuleBO;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.AlertRuleService#queryAlertRuleByCmsAlertRuleId(java.lang.String)
   */
  @Override
  public AlertRuleBO queryAlertRuleByCmsAlertRuleId(String cmsAlertRuleId) {

    AlertRuleDO alertRuleDO = alertRuleDao.queryAlertRuleByCmsAlertRuleId(cmsAlertRuleId);

    AlertRuleBO alertRuleBO = new AlertRuleBO();
    BeanUtils.copyProperties(alertRuleDO, alertRuleBO);

    return alertRuleBO;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.AlertRuleService#saveAlertRule(com.machloop.fpc.manager.appliance.bo.AlertRuleBO, java.lang.String)
   */
  @Transactional
  @Override
  public AlertRuleBO saveAlertRule(AlertRuleBO alertRuleBO, String operatorId) {
    if (alertRuleDao.countAlertRule() >= MAXIMUM_AMOUNT_RULE) {
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION,
          "最多只支持" + MAXIMUM_AMOUNT_RULE + "条告警规则");
    }

    AlertRuleDO existName = alertRuleDao.queryAlertRuleByName(alertRuleBO.getName());
    if (StringUtils.isNotBlank(existName.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "告警配置名称已经存在");
    }

    AlertRuleDO alertRuleDO = new AlertRuleDO();
    BeanUtils.copyProperties(alertRuleBO, alertRuleDO);
    alertRuleDO.setStatus(StringUtils.isNotBlank(alertRuleBO.getStatus()) ? alertRuleBO.getStatus()
        : Constants.BOOL_YES);
    alertRuleDO.setOperatorId(operatorId);

    // 保存告警配置
    AlertRuleDO result = alertRuleDao.saveOrRecoverAlertRule(alertRuleDO);

    // 保存告警与网络、业务的关联
    String networkIds = alertRuleBO.getNetworkIds();
    String serviceIds = alertRuleBO.getServiceIds();

    List<AlertScopeDO> alertScopes = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    CsvUtils.convertCSVToList(networkIds).forEach(networkId -> {
      AlertScopeDO alertScopeDO = new AlertScopeDO();
      alertScopeDO.setAlertId(result.getId());
      alertScopeDO.setSourceType(FpcConstants.SOURCE_TYPE_NETWORK);
      alertScopeDO.setNetworkId(networkId);
      alertScopes.add(alertScopeDO);
    });
    CsvUtils.convertCSVToList(serviceIds).forEach(serviceId -> {
      AlertScopeDO alertScopeDO = new AlertScopeDO();
      alertScopeDO.setAlertId(result.getId());
      alertScopeDO.setSourceType(FpcConstants.SOURCE_TYPE_SERVICE);
      String[] serviceNetwork = StringUtils.split(serviceId, "^");
      if (serviceNetwork.length != 2) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "业务作用域异常");
      }
      alertScopeDO.setServiceId(serviceNetwork[0]);
      alertScopeDO.setNetworkId(serviceNetwork[1]);
      alertScopes.add(alertScopeDO);
    });

    if (CollectionUtils.isEmpty(alertScopes)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "告警规则必须指定网络或业务作用域");
    }
    alertScopeDao.batchSaveAlertScopes(alertScopes);

    BeanUtils.copyProperties(result, alertRuleBO);
    return alertRuleBO;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.AlertRuleService#updateAlertRule(java.lang.String, com.machloop.fpc.manager.appliance.bo.AlertRuleBO, java.lang.String)
   */
  @Transactional
  @Override
  public AlertRuleBO updateAlertRule(String id, AlertRuleBO alertRuleBO, String operatorId) {
    AlertRuleDO exist = alertRuleDao.queryAlertRule(id);
    if (StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "告警配置不存在");
    }

    AlertRuleDO existName = alertRuleDao.queryAlertRuleByName(alertRuleBO.getName());
    if (StringUtils.isNotBlank(existName.getId()) && !StringUtils.equals(id, existName.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "告警配置名称已经存在");
    }

    AlertRuleDO alertRuleDO = new AlertRuleDO();
    alertRuleBO.setId(id);
    BeanUtils.copyProperties(alertRuleBO, alertRuleDO);
    alertRuleDO.setOperatorId(operatorId);

    alertRuleDao.updateAlertRule(alertRuleDO);

    // 修改告警与网络、业务的关联
    String networkIds = alertRuleBO.getNetworkIds();
    String serviceIds = alertRuleBO.getServiceIds();

    List<AlertScopeDO> alertScopes = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    CsvUtils.convertCSVToList(networkIds).forEach(networkId -> {
      AlertScopeDO alertScopeDO = new AlertScopeDO();
      alertScopeDO.setAlertId(id);
      alertScopeDO.setSourceType(FpcConstants.SOURCE_TYPE_NETWORK);
      alertScopeDO.setNetworkId(networkId);
      alertScopes.add(alertScopeDO);
    });
    CsvUtils.convertCSVToList(serviceIds).forEach(serviceId -> {
      AlertScopeDO alertScopeDO = new AlertScopeDO();
      alertScopeDO.setAlertId(id);
      alertScopeDO.setSourceType(FpcConstants.SOURCE_TYPE_SERVICE);
      String[] serviceNetwork = StringUtils.split(serviceId, "^");
      if (serviceNetwork.length != 2) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "业务作用域异常");
      }
      alertScopeDO.setServiceId(serviceNetwork[0]);
      alertScopeDO.setNetworkId(serviceNetwork[1]);
      alertScopes.add(alertScopeDO);
    });

    if (CollectionUtils.isEmpty(alertScopes)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "告警规则必须指定网络或业务作用域");
    }
    alertScopeDao.batchUpdateAlertScopes(id, alertScopes);

    return queryAlertRule(id);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.AlertRuleService#updateAlertRuleStatus(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public AlertRuleBO updateAlertRuleStatus(String id, String status, String operatorId) {
    AlertRuleDO exist = alertRuleDao.queryAlertRule(id);
    if (StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "告警配置不存在");
    }

    if (!StringUtils.equalsAny(status, Constants.BOOL_YES, Constants.BOOL_NO)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的启用状态");
    }

    alertRuleDao.updateAlertRuleStatus(id, status, operatorId);

    return queryAlertRule(id);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.AlertRuleService#deleteAlertRule(java.lang.String, java.lang.String)
   */
  @Transactional
  @Override
  public AlertRuleBO deleteAlertRule(String id, String operatorId, boolean forceDelete) {
    AlertRuleDO alertRuleDO = alertRuleDao.queryAlertRule(id);
    if (!forceDelete && StringUtils.isBlank(alertRuleDO.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "告警配置不存在");
    }

    alertRuleDao.deleteAlertRule(id, operatorId);

    // 删除告警与网络、业务的关联
    alertScopeDao.deleteAlertScopeByAlertId(id);

    AlertRuleBO alertRuleBO = new AlertRuleBO();
    BeanUtils.copyProperties(alertRuleDO, alertRuleBO);
    return alertRuleBO;
  }

  @PostConstruct
  public void init() {
    MQAssignmentServiceImpl.register(this, Lists.newArrayList(FpcCmsConstants.MQ_TAG_ALERT));
  }

  /**
   * @see com.machloop.fpc.manager.cms.service.SyncConfigurationService#syncConfiguration(java.util.Map)
   */
  @Override
  public int syncConfiguration(Message message) {
    Map<String, Object> messageBody = MQMessageHelper.convertToMap(message);

    List<Map<String, Object>> messages = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (MapUtils.getBoolean(messageBody, "batch", false)) {
      messages.addAll(JsonHelper.deserialize(JsonHelper.serialize(messageBody.get("data")),
          new TypeReference<List<Map<String, Object>>>() {
          }));
    } else {
      messages.add(messageBody);
    }

    int syncTotalCount = messages.stream().mapToInt(item -> syncAlertRule(item)).sum();
    LOGGER.info("current sync alertRule total: {}.", syncTotalCount);

    return syncTotalCount;
  }

  private int syncAlertRule(Map<String, Object> messageBody) {

    int syncCount = 0;

    String alertRuleInCmsId = MapUtils.getString(messageBody, "id");
    if (StringUtils.isBlank(alertRuleInCmsId)) {
      return syncCount;
    }

    // 构造本地alertRuleDO对象
    AlertRuleBO alertRuleBO = new AlertRuleBO();
    alertRuleBO.setId(alertRuleInCmsId);
    alertRuleBO.setAlertRuleInCmsId(alertRuleInCmsId);
    alertRuleBO.setName(MapUtils.getString(messageBody, "name"));
    alertRuleBO.setCategory(MapUtils.getString(messageBody, "category"));
    alertRuleBO.setLevel(MapUtils.getString(messageBody, "level"));
    alertRuleBO.setThresholdSettings(MapUtils.getString(messageBody, "thresholdSettings"));
    alertRuleBO.setTrendSettings(MapUtils.getString(messageBody, "trendSettings"));
    alertRuleBO.setAdvancedSettings(MapUtils.getString(messageBody, "advancedSettings"));
    alertRuleBO.setRefire(MapUtils.getString(messageBody, "refire"));
    alertRuleBO.setStatus(MapUtils.getString(messageBody, "status"));
    alertRuleBO.setNetworkIds(MapUtils.getString(messageBody, "networkIds"));
    alertRuleBO.setServiceIds(MapUtils.getString(messageBody, "serviceIds"));
    alertRuleBO.setDescription(CMS_ASSIGNMENT);

    String action = MapUtils.getString(messageBody, "action");

    // 判断下发的告警所包含的网络（子网）是否存在
    List<String> vaildNetworkIds = networkDao.queryNetworks().stream().map(NetworkDO::getId)
        .collect(Collectors.toList());
    List<String> validSubnetIdList = logicalSubnetDao.queryLogicalSubnets().stream()
        .map(LogicalSubnetDO::getId).collect(Collectors.toList());

    List<String> alertNetworkIdList = CsvUtils.convertCSVToList(alertRuleBO.getNetworkIds())
        .stream().filter(networkId -> vaildNetworkIds.contains(networkId)
            || validSubnetIdList.contains(networkId))
        .collect(Collectors.toList());

    // 判断下发的告警所包含的业务是否存在
    List<String> validServiceIdList = serviceService.queryServices().stream().map(ServiceBO::getId)
        .collect(Collectors.toList());

    Map<String,
        Object> alertServiceMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    List<String> serviceIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    CsvUtils.convertCSVToList(alertRuleBO.getServiceIds()).forEach(serviceId -> {
      String[] serviceNetwork = StringUtils.split(serviceId, "^");
      if (serviceNetwork.length != 2) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "业务作用域异常");
      }
      serviceIds.add(serviceNetwork[0]);
      alertServiceMap.put(serviceNetwork[0], serviceId);
    });
    List<String> alertSeviceList = serviceIds.stream()
        .filter(serviceId -> validServiceIdList.contains(serviceId)).collect(Collectors.toList());

    List<String> alertServiceIdList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    for (String alertService : alertSeviceList) {
      alertServiceIdList.add(MapUtils.getString(alertServiceMap, alertService));
    }

    // 本次下发的告警是否存在
    AlertRuleBO exist = queryAlertRuleByCmsAlertRuleId(alertRuleBO.getAlertRuleInCmsId());

    // 如果cms的编辑操作中包含删除或者增加网络/业务，则将其转换成删除或者增加操作下发到对应的探针上，此时status的值为空
    // 如果cms的编辑操作中只是启用/停用告警，此时status的值不为空，则不对其进行上述转换
    outer: if (StringUtils.equals(action, FpcCmsConstants.SYNC_ACTION_MODIFY)
        && StringUtils.isBlank(alertRuleBO.getStatus())) {
      if (CollectionUtils.isEmpty(alertNetworkIdList) && CollectionUtils.isEmpty(alertServiceIdList)
          && StringUtils.isNotBlank(exist.getId())) {
        action = FpcCmsConstants.SYNC_ACTION_DELETE;
        break outer;
      }
      if (StringUtils.isBlank(exist.getId()) && CollectionUtils.isNotEmpty(alertNetworkIdList)
          || CollectionUtils.isNotEmpty(alertServiceIdList)) {
        action = FpcCmsConstants.SYNC_ACTION_ADD;
        break outer;
      }
    }

    if (StringUtils.equals(action, FpcCmsConstants.SYNC_ACTION_ADD)
        && CollectionUtils.isEmpty(alertNetworkIdList)
        && CollectionUtils.isEmpty(alertServiceIdList)
        && !StringUtils.equals(alertRuleBO.getNetworkIds(), "allNetwork")) {
      // 不存在告警所包含的网络及业务
      return syncCount;
    }

    if (!StringUtils.equals(alertRuleBO.getNetworkIds(), "allNetwork")) {
      alertRuleBO.setNetworkIds(CsvUtils.convertCollectionToCSV(alertNetworkIdList));
      alertRuleBO.setServiceIds(CsvUtils.convertCollectionToCSV(alertServiceIdList));
    }

    int addCount = 0;
    int modifyCount = 0;
    int deleteCount = 0;
    try {
      switch (action) {
        case FpcConstants.SYNC_ACTION_ADD:
        case FpcConstants.SYNC_ACTION_MODIFY:
          if (StringUtils.isNotBlank(exist.getId())) {
            if (alertRuleBO.getStatus() == null) {
              updateAlertRule(exist.getId(), alertRuleBO, CMS_ASSIGNMENT);
              LogHelper.auditAssignOperate(
                  globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
                  alertRuleBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_UPDATE));
            } else {
              updateAlertRuleStatus(exist.getId(), alertRuleBO.getStatus(), CMS_ASSIGNMENT);
              LogHelper.auditAssignOperate(
                  globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
                  alertRuleBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_UPDATE));
            }
            modifyCount++;
          } else {
            saveAlertRule(alertRuleBO, CMS_ASSIGNMENT);
            LogHelper.auditAssignOperate(
                globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
                alertRuleBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_SAVE));
            addCount++;
          }
          break;
        case FpcConstants.SYNC_ACTION_DELETE:
          deleteAlertRule(exist.getId(), CMS_ASSIGNMENT, true);
          LogHelper.auditAssignOperate(
              globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
              alertRuleBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_DELETE));
          deleteCount++;
          break;
        default:
          break;
      }

      // 本次同步数据量
      syncCount = addCount + modifyCount + deleteCount;

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("current sync alertRule status: [add: {}, modify: {}, delete: {}]", addCount,
            modifyCount, deleteCount);
      }
    } catch (BusinessException e) {
      LOGGER.warn("sync faild. error msg: {}", e.getMessage());
      return syncCount;
    }
    return syncCount;
  }

  /**
   * @see com.machloop.fpc.manager.cms.service.SyncConfigurationService#clearLocalConfiguration(java.lang.String, java.util.Date, boolean)
   */
  @Override
  public int clearLocalConfiguration(String tag, boolean onlyLocal, Date beforeTime) {
    int clearCount = 0;
    List<String> alertIds = alertRuleDao.queryAlertRuleIds(onlyLocal);
    for (String alertId : alertIds) {
      try {
        deleteAlertRule(alertId, CMS_ASSIGNMENT, true);
        clearCount++;
      } catch (BusinessException e) {
        LOGGER.warn("delete alert failed. error msg: {}", e.getMessage());
        continue;
      }
    }
    return clearCount;
  }

  /**
   * @see com.machloop.fpc.manager.cms.service.SyncConfigurationService#getAssignConfigurationIds(java.util.Date)
   */
  @Override
  public List<String> getAssignConfigurationIds(String tags, Date beforeTime) {
    List<String> assignIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    List<String> alertIdList = alertRuleDao.queryAssignAlertRules(beforeTime);
    for (String alertId : alertIdList) {
      alertScopeDao.queryAlertScopeByAlertId(alertId).forEach(alertScope -> {
        if (StringUtils.equals(alertScope.getSourceType(), FpcCmsConstants.SOURCE_TYPE_NETWORK)) {
          assignIds.add(
              StringUtils.joinWith("^", alertScope.getAlertId(), "*", alertScope.getNetworkId()));
        } else {
          assignIds
              .add(StringUtils.joinWith("^", alertScope.getAlertId(), alertScope.getServiceId()));
        }
      });

    }
    return assignIds;
  }

}
