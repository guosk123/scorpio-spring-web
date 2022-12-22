package com.machloop.fpc.cms.center.appliance.service.impl;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
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
import com.machloop.fpc.cms.center.appliance.bo.AlertRuleBO;
import com.machloop.fpc.cms.center.appliance.bo.ServiceBO;
import com.machloop.fpc.cms.center.appliance.dao.AlertRuleDao;
import com.machloop.fpc.cms.center.appliance.dao.AlertScopeDao;
import com.machloop.fpc.cms.center.appliance.data.AlertRuleDO;
import com.machloop.fpc.cms.center.appliance.data.AlertScopeDO;
import com.machloop.fpc.cms.center.appliance.service.AlertRuleService;
import com.machloop.fpc.cms.center.appliance.service.ServiceService;
import com.machloop.fpc.cms.center.broker.bo.FpcNetworkBO;
import com.machloop.fpc.cms.center.broker.service.local.SyncConfigurationService;
import com.machloop.fpc.cms.center.broker.service.local.impl.MQReceiveServiceImpl;
import com.machloop.fpc.cms.center.broker.service.subordinate.FpcNetworkService;
import com.machloop.fpc.cms.center.broker.service.subordinate.MQAssignmentService;
import com.machloop.fpc.cms.center.helper.MQMessageHelper;
import com.machloop.fpc.cms.center.sensor.bo.SensorNetworkBO;
import com.machloop.fpc.cms.center.sensor.dao.SensorLogicalSubnetDao;
import com.machloop.fpc.cms.center.sensor.data.SensorLogicalSubnetDO;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkService;
import com.machloop.fpc.cms.common.FpcCmsConstants;

import reactor.util.function.Tuple2;
import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * @author "Minjiajun"
 *
 * create at 2021年10月19日, fpc-cms-center
 */
@Order(5)
@Service
public class AlertRuleServiceImpl
    implements AlertRuleService, MQAssignmentService, SyncConfigurationService {

  private static final List<String> TAGS = Lists.newArrayList(FpcCmsConstants.MQ_TAG_ALERT);

  @SuppressWarnings("unused")
  private static final Pattern ADVANCED_FIRE_CRITERIA_PATTERN = Pattern
      .compile("\"alertRef\":\"([^\\\"]*)\"", Pattern.MULTILINE);

  private static final int MAXIMUM_AMOUNT_RULE = 100;

  @Autowired
  private AlertRuleDao alertRuleDao;

  @Autowired
  private AlertScopeDao alertScopeDao;

  @Autowired
  private ApplicationContext context;

  @Autowired
  private FpcNetworkService fpcNetworkService;

  @Autowired
  private SensorLogicalSubnetDao sensorLogicalSubnetDao;

  @Autowired
  private ServiceService serviceService;

  @Autowired
  private SensorNetworkService sensorNetworkService;

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.AlertRuleService#queryAlertRules(com.machloop.alpha.common.base.page.Pageable, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
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

      if (StringUtils.equals(alertScope.getSourceType(), FpcCmsConstants.SOURCE_TYPE_NETWORK)) {
        tuple2.getT1().add(alertScope.getNetworkId());
      } else {
        tuple2.getT2()
            .add(StringUtils.joinWith("^", alertScope.getServiceId(), alertScope.getNetworkId()));
      }

      alertScopes.put(alertScope.getAlertId(), tuple2);
    });

    long totalElem = alertRules.getTotalElements();

    List<AlertRuleBO> list = Lists.newArrayListWithCapacity(alertRules.getSize());
    alertRules.forEach(alertRuleDO -> {
      AlertRuleBO alertRuleBO = new AlertRuleBO();
      BeanUtils.copyProperties(alertRuleDO, alertRuleBO);
      Tuple2<List<String>, List<String>> tuple = alertScopes.get(alertRuleBO.getId());
      alertRuleBO.setNetworkIds(StringUtils.join(tuple.getT1(), ","));
      alertRuleBO.setServiceIds(StringUtils.join(tuple.getT2(), ","));
      alertRuleBO.setCreateTime(DateUtils.toStringISO8601(alertRuleDO.getCreateTime()));
      list.add(alertRuleBO);
    });

    return new PageImpl<>(list, page, totalElem);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.AlertRuleService#queryAlertRulesBySource(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public List<String> queryAlertRulesBySource(String sourceType, String networkId,
      String serviceId) {
    List<AlertScopeDO> alertScope = alertScopeDao.queryAlertScope(sourceType, networkId, serviceId);

    return alertScope.stream().map(AlertScopeDO::getAlertId).collect(Collectors.toList());
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.AlertRuleService#queryAlertRulesByCategory(java.lang.String)
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
      if (StringUtils.equals(alertScope.getSourceType(), FpcCmsConstants.SOURCE_TYPE_NETWORK)) {
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
      alertRuleBO.setNetworkIds(StringUtils.join(tuple.getT1(), ","));
      alertRuleBO.setServiceIds(StringUtils.join(tuple.getT2(), ","));
      alertRuleBO.setCreateTime(DateUtils.toStringISO8601(alertRuleDO.getCreateTime()));
      list.add(alertRuleBO);
    });

    return list;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.AlertRuleService#queryAlertRule(java.lang.String)
   */
  @Override
  public AlertRuleBO queryAlertRule(String id) {
    AlertRuleDO alertRuleDO = alertRuleDao.queryAlertRule(id);

    List<String> networkIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<String> serviceIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    alertScopeDao.queryAlertScopeByAlertId(id).forEach(alertScope -> {
      if (StringUtils.equals(alertScope.getSourceType(), FpcCmsConstants.SOURCE_TYPE_NETWORK)) {
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
   * @see com.machloop.fpc.cms.center.appliance.service.AlertRuleService#saveAlertRule(com.machloop.fpc.cms.center.appliance.bo.AlertRuleBO, java.lang.String)
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
    alertRuleDO.setStatus(StringUtils.defaultIfBlank(alertRuleBO.getStatus(), Constants.BOOL_YES));
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
      alertScopeDO.setSourceType(FpcCmsConstants.SOURCE_TYPE_NETWORK);
      alertScopeDO.setNetworkId(networkId);
      alertScopes.add(alertScopeDO);
    });
    CsvUtils.convertCSVToList(serviceIds).forEach(serviceId -> {
      AlertScopeDO alertScopeDO = new AlertScopeDO();
      alertScopeDO.setAlertId(result.getId());
      alertScopeDO.setSourceType(FpcCmsConstants.SOURCE_TYPE_SERVICE);
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

    // 下发到直属fpc和cms
    alertRuleBO.setId(result.getId());
    List<Map<String, Object>> messageBodys = Lists
        .newArrayList(alertRule2MessageBody(alertRuleBO, FpcCmsConstants.SYNC_ACTION_ADD));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_ALERT, null);

    return alertRuleBO;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.AlertRuleService#updateAlertRule(java.lang.String, com.machloop.fpc.cms.center.appliance.bo.AlertRuleBO, java.lang.String)
   */
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
      alertScopeDO.setSourceType(FpcCmsConstants.SOURCE_TYPE_NETWORK);
      alertScopeDO.setNetworkId(networkId);
      alertScopes.add(alertScopeDO);
    });
    CsvUtils.convertCSVToList(serviceIds).forEach(serviceId -> {
      AlertScopeDO alertScopeDO = new AlertScopeDO();
      alertScopeDO.setAlertId(id);
      alertScopeDO.setSourceType(FpcCmsConstants.SOURCE_TYPE_SERVICE);
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

    // 下发到直属fpc和cms
    alertRuleBO.setId(id);
    List<Map<String, Object>> messageBodys = Lists
        .newArrayList(alertRule2MessageBody(alertRuleBO, FpcCmsConstants.SYNC_ACTION_MODIFY));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_ALERT, null);

    return queryAlertRule(id);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.AlertRuleService#updateAlertRuleStatus(java.lang.String, java.lang.String, java.lang.String)
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

    // 下发到直属fpc和cms
    exist.setStatus(status);
    AlertRuleBO alertRuleBO = new AlertRuleBO();
    BeanUtils.copyProperties(exist, alertRuleBO);
    List<Map<String, Object>> messageBodys = Lists
        .newArrayList(alertRule2MessageBody(alertRuleBO, FpcCmsConstants.SYNC_ACTION_MODIFY));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_ALERT, null);

    return queryAlertRule(id);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.AlertRuleService#updateAlertRuleScope(java.lang.String, java.lang.String)
   */
  @Override
  public void updateAlertRuleScope(String networkId, String operatorId) {
    Map<String, List<AlertScopeDO>> map = alertScopeDao.queryAlertScope().stream()
        .collect(Collectors.groupingBy(AlertScopeDO::getAlertId));

    List<AlertScopeDO> alertScopes = alertScopeDao
        .queryAlertScope(FpcCmsConstants.SOURCE_TYPE_NETWORK, networkId, null);
    alertScopes.forEach(alertScope -> {
      List<AlertScopeDO> list = map.get(alertScope.getAlertId());
      if (list.size() <= 1) {
        AlertRuleBO alertRule = queryAlertRule(alertScope.getAlertId());
        alertRule.setNetworkIds(ALL_NETWORK);
        alertRule.setServiceIds("");
        updateAlertRule(alertScope.getAlertId(), alertRule, operatorId);
      } else {
        alertScopeDao.deleteAlertScope(alertScope.getId(), networkId, null);
      }
    });
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.AlertRuleService#deleteAlertRule(java.lang.String, java.lang.String)
   */
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

    // 下发到直属fpc和cms
    List<Map<String, Object>> messageBodys = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    messageBodys.add(alertRule2MessageBody(alertRuleBO, FpcCmsConstants.SYNC_ACTION_DELETE));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_ALERT, null);
    return alertRuleBO;
  }

  /********************************************************************************************************
   * 下发模块
   *******************************************************************************************************/

  /**
   * @see com.machloop.fpc.cms.center.broker.service.subordinate.MQAssignmentService#getProducer()
   */
  @Override
  public DefaultMQProducer getProducer() {
    return context.getBean("getRocketMQProducer", DefaultMQProducer.class);
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.subordinate.MQAssignmentService#getTags()
   */
  @Override
  public List<String> getTags() {
    return TAGS;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.subordinate.MQAssignmentService#getFullConfigurationIds(java.lang.String, java.lang.String, java.util.Date)
   */
  @Override
  public Map<String, List<String>> getFullConfigurationIds(String deviceType, String serialNumber,
      Date beforeTime) {

    // 获得告警与网络及业务之间的关系
    List<AlertScopeDO> alertScopeList = alertScopeDao.queryAlertScope();
    Map<String, List<String>> alertNetworkIdMap = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    Map<String, List<String>> alertServiceIdMap = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    for (AlertScopeDO alertScopeDO : alertScopeList) {
      if (CollectionUtils.isEmpty(alertNetworkIdMap.get(alertScopeDO.getAlertId()))) {
        List<String> networkIds = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
        alertNetworkIdMap.put(alertScopeDO.getAlertId(), networkIds);
      }
      if (StringUtils.equals(alertScopeDO.getSourceType(), FpcCmsConstants.SOURCE_TYPE_NETWORK)) {
        alertNetworkIdMap.get(alertScopeDO.getAlertId()).add(alertScopeDO.getNetworkId());
      }
    }

    for (AlertScopeDO alertScopeDO : alertScopeList) {
      if (CollectionUtils.isEmpty(alertServiceIdMap.get(alertScopeDO.getAlertId()))) {
        List<String> serviceIds = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
        alertServiceIdMap.put(alertScopeDO.getAlertId(), serviceIds);
      }
      if (StringUtils.equals(alertScopeDO.getSourceType(), FpcCmsConstants.SOURCE_TYPE_SERVICE)) {
        String serviceId = alertScopeDO.getServiceId() + "^" + alertScopeDO.getNetworkId();
        alertServiceIdMap.get(alertScopeDO.getAlertId()).add(serviceId);
      }
    }

    // 过滤要下发的告警中的网络，告警中的业务中的网络，确保其存在于要下发设备中
    if (!StringUtils.isAllBlank(deviceType, serialNumber)) {
      // 下发设备包含的主网络
      List<String> fpcNetworkIds = fpcNetworkService.queryNetworks(deviceType, serialNumber)
          .stream().map(FpcNetworkBO::getFpcNetworkId).collect(Collectors.toList());
      // 下发设备包含的子网
      fpcNetworkIds.addAll(sensorLogicalSubnetDao.querySensorLogicalSubnets().stream()
          .filter(item -> fpcNetworkIds.contains(item.getNetworkInSensorIds()))
          .map(SensorLogicalSubnetDO::getId).collect(Collectors.toList()));

      // 如果指定了下发的设备，并且告警中没有包含将要下发设备的网络，则不下发该告警
      Iterator<
          Entry<String, List<String>>> iteratorNetwork = alertNetworkIdMap.entrySet().iterator();
      while (iteratorNetwork.hasNext()) {
        Entry<String, List<String>> entry = iteratorNetwork.next();
        List<String> bak = Lists.newArrayList(entry.getValue());
        bak.removeAll(fpcNetworkIds);
        entry.getValue().removeAll(bak);

        if (CollectionUtils.isEmpty(entry.getValue())) {
          iteratorNetwork.remove();
        }
      }

      // 如果指定了下发的设备，并且告警中的业务中没有包含将要下发设备的网络，则不下发该业务
      Iterator<Entry<String, List<String>>> alertSevice = alertServiceIdMap.entrySet().iterator();
      while (alertSevice.hasNext()) {
        Entry<String, List<String>> entry = alertSevice.next();
        String alertId = entry.getKey();
        List<String> serviceList = Lists.newArrayList(entry.getValue());

        // 将某个告警中的业务从serviceId^networkId => serviceId=networkIdList(serviceNetworkMap)
        Map<String, List<String>> serviceNetworkMap = Maps
            .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
        // List<String> serviceIds = alertServiceIdMap.get(alertId);
        for (String serviceId : serviceList) {
          String[] serviceNetwork = StringUtils.split(serviceId, "^");
          List<String> networkList = serviceNetworkMap.get(serviceNetwork[0]);
          if (networkList == null) {
            networkList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
            serviceNetworkMap.put(serviceNetwork[0], networkList);
          }
          networkList.add(serviceNetwork[1]);
        }
        // 使用下发设备中的网络fpcNetworkIds过滤serviceNetworkMap
        Iterator<
            Entry<String, List<String>>> iteratorService = serviceNetworkMap.entrySet().iterator();
        while (iteratorService.hasNext()) {
          Entry<String, List<String>> entry1 = iteratorService.next();
          List<String> bak = Lists.newArrayList(entry1.getValue());
          bak.removeAll(fpcNetworkIds);
          entry1.getValue().removeAll(bak);

          if (CollectionUtils.isEmpty(entry1.getValue())) {
            iteratorService.remove();
          }
        }
        // 将过滤结果在拼成service^networkId的格式
        Iterator<Entry<String, List<String>>> result = serviceNetworkMap.entrySet().iterator();
        List<String> serviceIdList = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
        while (result.hasNext()) {
          Entry<String, List<String>> entry2 = result.next();
          List<String> networkList = Lists.newArrayList(entry2.getValue());
          for (String networkId : networkList) {
            String serviceId = StringUtils.joinWith("^", entry2.getKey(), networkId);
            serviceIdList.add(serviceId);
          }
        }
        alertServiceIdMap.put(alertId, serviceIdList);
      }
    }

    // 拼接网络Id：alertId^*^networkId
    List<String> alertList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    Iterator<Entry<String, List<String>>> alertNetwork = alertNetworkIdMap.entrySet().iterator();
    while (alertNetwork.hasNext()) {
      Entry<String, List<String>> entry = alertNetwork.next();
      List<String> networkList = Lists.newArrayList(entry.getValue());
      for (String networkId : networkList) {
        String alertNetworkId = StringUtils.joinWith("^", entry.getKey(), "*", networkId);
        alertList.add(alertNetworkId);
      }
    }

    // 拼接业务Id：alertId^serviceId^network
    Iterator<Entry<String, List<String>>> alertService = alertServiceIdMap.entrySet().iterator();
    while (alertService.hasNext()) {
      Entry<String, List<String>> entry = alertService.next();
      List<String> serviceList = Lists.newArrayList(entry.getValue());
      for (String serviceId : serviceList) {
        String alertServiceId = StringUtils.joinWith("^", entry.getKey(), serviceId);
        alertList.add(alertServiceId);
      }
    }

    Map<String, List<String>> map = Maps.newHashMapWithExpectedSize(1);
    map.put(FpcCmsConstants.MQ_TAG_ALERT, alertList);
    return map;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.subordinate.MQAssignmentService#getFullConfigurations(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Tuple3<Boolean, List<Map<String, Object>>, Message> getFullConfigurations(
      String deviceType, String serialNumber, String tag) {

    if (StringUtils.equals(tag, FpcCmsConstants.MQ_TAG_ALERT)) {
      List<Map<String, Object>> list = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

      List<AlertScopeDO> alertScopeList = alertScopeDao.queryAlertScope();
      Map<String, List<String>> alertScopeNetworkMap = Maps
          .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      Map<String, List<String>> alertScopeServiceMap = Maps
          .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

      for (AlertScopeDO alertScopeDO : alertScopeList) {
        if (CollectionUtils.isEmpty(alertScopeNetworkMap.get(alertScopeDO.getAlertId()))) {
          List<String> networkIds = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
          alertScopeNetworkMap.put(alertScopeDO.getAlertId(), networkIds);
        }
        if (StringUtils.equals(alertScopeDO.getSourceType(), FpcCmsConstants.SOURCE_TYPE_NETWORK)) {
          alertScopeNetworkMap.get(alertScopeDO.getAlertId()).add(alertScopeDO.getNetworkId());
        }
      }

      for (AlertScopeDO alertScopeDO : alertScopeList) {
        if (CollectionUtils.isEmpty(alertScopeServiceMap.get(alertScopeDO.getAlertId()))) {
          List<String> serviceIds = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
          alertScopeServiceMap.put(alertScopeDO.getAlertId(), serviceIds);
        }
        if (StringUtils.equals(alertScopeDO.getSourceType(), FpcCmsConstants.SOURCE_TYPE_SERVICE)) {
          String serviceId = alertScopeDO.getServiceId() + "^" + alertScopeDO.getNetworkId();
          alertScopeServiceMap.get(alertScopeDO.getAlertId()).add(serviceId);
        }
      }

      List<AlertRuleDO> alertRuleList = alertRuleDao.queryAlertRules();
      for (AlertRuleDO alertRuleDO : alertRuleList) {
        AlertRuleBO alertRuleBO = new AlertRuleBO();
        BeanUtils.copyProperties(alertRuleDO, alertRuleBO);
        if (CollectionUtils.isNotEmpty(alertScopeNetworkMap.get(alertRuleDO.getId()))) {
          alertRuleBO.setNetworkIds(
              CsvUtils.convertCollectionToCSV(alertScopeNetworkMap.get(alertRuleDO.getId())));
        }
        if (CollectionUtils.isNotEmpty(alertScopeServiceMap.get(alertRuleDO.getId()))) {
          alertRuleBO.setServiceIds(
              CsvUtils.convertCollectionToCSV(alertScopeServiceMap.get(alertRuleDO.getId())));
        }
        list.add(alertRule2MessageBody(alertRuleBO, FpcCmsConstants.SYNC_ACTION_ADD));
      }


      return Tuples.of(true, list, MQMessageHelper.EMPTY);
    } else {
      return Tuples.of(true, Lists.newArrayListWithCapacity(0), MQMessageHelper.EMPTY);
    }
  }

  private Map<String, Object> alertRule2MessageBody(AlertRuleBO alertRuleBO, String action) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    map.put("id", alertRuleBO.getId());
    map.put("name", alertRuleBO.getName());
    map.put("category", alertRuleBO.getCategory());
    map.put("level", alertRuleBO.getLevel());
    map.put("thresholdSettings", alertRuleBO.getThresholdSettings());
    map.put("trendSettings", alertRuleBO.getTrendSettings());
    map.put("advancedSettings", alertRuleBO.getAdvancedSettings());
    map.put("refire", alertRuleBO.getRefire());
    map.put("status", alertRuleBO.getStatus());
    map.put("networkIds", alertRuleBO.getNetworkIds());
    map.put("serviceIds", alertRuleBO.getServiceIds());
    map.put("action", action);

    return map;
  }

  /********************************************************************************************************
   * 接收模块
   *******************************************************************************************************/

  @PostConstruct
  public void init() {
    MQReceiveServiceImpl.register(this, Lists.newArrayList(FpcCmsConstants.MQ_TAG_ALERT));
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.local.SyncConfigurationService#syncConfiguration(org.apache.rocketmq.common.message.Message)
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

    String assignId = MapUtils.getString(messageBody, "id");
    if (StringUtils.isBlank(assignId)) {
      return syncCount;
    }

    // 构造本地alertRuleDO对象
    AlertRuleBO alertRuleBO = new AlertRuleBO();
    alertRuleBO.setAssignId(assignId);
    alertRuleBO.setId(assignId);
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

    // 判断下发的告警所包含的网络是否存在
    List<String> vaildNetworkIds = sensorNetworkService.querySensorNetworks().stream()
        .map(SensorNetworkBO::getNetworkInSensorId).collect(Collectors.toList());
    Map<String,
        String> validSubnetIdMap = sensorLogicalSubnetDao.querySensorLogicalSubnets().stream()
            .collect(
                Collectors.toMap(SensorLogicalSubnetDO::getAssignId, SensorLogicalSubnetDO::getId));

    List<String> alertNetworkList = CsvUtils.convertCSVToList(alertRuleBO.getNetworkIds()).stream()
        .filter(networkId -> validSubnetIdMap.containsKey(networkId)
            || vaildNetworkIds.contains(networkId) || StringUtils.equals(networkId, ALL_NETWORK))
        .map(networkId -> validSubnetIdMap.getOrDefault(networkId, networkId))
        .collect(Collectors.toList());

    // 判断下发的告警所包含的业务是否存在
    Map<String,
        String> serviceNetworkMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    Map<String, String> validServiceIdMap = serviceService.queryServices().stream()
        .collect(Collectors.toMap(ServiceBO::getAssignId, ServiceBO::getId));

    List<String> serviceIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    CsvUtils.convertCSVToList(alertRuleBO.getServiceIds()).forEach(serviceId -> {
      String[] serviceNetwork = StringUtils.split(serviceId, "^");
      if (serviceNetwork.length != 2) {
        throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "业务作用域异常");
      }
      serviceIds.add(serviceNetwork[0]);
      serviceId = StringUtils.joinWith("^", validServiceIdMap.get(serviceNetwork[0]),
          serviceNetwork[1]);
      serviceNetworkMap.put(validServiceIdMap.get(serviceNetwork[0]), serviceId);
    });
    List<String> alertSeviceList = serviceIds.stream()
        .filter(serviceId -> validServiceIdMap.containsKey(serviceId))
        .map(serviceId -> validServiceIdMap.getOrDefault(serviceId, serviceId))
        .collect(Collectors.toList());

    String services = "";
    for (String alertService : alertSeviceList) {
      services = StringUtils.joinWith(",", serviceNetworkMap.get(alertService));
    }

    if (!StringUtils.equals(action, FpcCmsConstants.SYNC_ACTION_DELETE)
        && CollectionUtils.isEmpty(alertNetworkList) && CollectionUtils.isEmpty(alertSeviceList)) {
      // 不存在告警所包含的网络及业务
      return syncCount;
    }

    alertRuleBO.setNetworkIds(CsvUtils.convertCollectionToCSV(alertNetworkList));
    alertRuleBO.setServiceIds(services);

    // 本次下发的告警是否存在
    AlertRuleDO exist = alertRuleDao.queryAlertRuleByAssignId(alertRuleBO.getAssignId());

    int addCount = 0;
    int modifyCount = 0;
    int deleteCount = 0;
    try {
      switch (action) {
        case FpcCmsConstants.SYNC_ACTION_ADD:
        case FpcCmsConstants.SYNC_ACTION_MODIFY:
          if (StringUtils.isNotBlank(exist.getId())) {
            if (alertRuleBO.getStatus() == null) {
              updateAlertRule(exist.getId(), alertRuleBO, CMS_ASSIGNMENT);
            } else {
              updateAlertRuleStatus(exist.getId(), alertRuleBO.getStatus(), CMS_ASSIGNMENT);
            }
            modifyCount++;
          } else {
            saveAlertRule(alertRuleBO, CMS_ASSIGNMENT);
            addCount++;
          }
          break;
        case FpcCmsConstants.SYNC_ACTION_DELETE:
          deleteAlertRule(exist.getId(), CMS_ASSIGNMENT, true);
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
   * @see com.machloop.fpc.cms.center.broker.service.local.SyncConfigurationService#clearLocalConfiguration(java.lang.String, java.util.Date)
   */
  @Override
  public int clearLocalConfiguration(String tag, boolean onlyLocal, Date beforeTime) {

    // 删除
    int clearCount = 0;
    List<String> alertRuleIdList = alertRuleDao.queryAlertRuleIds(onlyLocal);
    for (String alertRuleId : alertRuleIdList) {
      try {
        deleteAlertRule(alertRuleId, CMS_ASSIGNMENT, true);
        clearCount++;
      } catch (BusinessException e) {
        LOGGER.warn("delete alertRule failed. error msg: {}", e.getMessage());
        continue;
      }
    }

    return clearCount;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.local.SyncConfigurationService#getAssignConfigurationIds(java.lang.String, java.util.Date)
   */
  @Override
  public List<String> getAssignConfigurationIds(String tag, Date beforeTime) {
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
