package com.machloop.fpc.npm.appliance.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
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
import com.machloop.alpha.common.dict.DictManager;
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
import com.machloop.fpc.manager.appliance.service.AlertRuleService;
import com.machloop.fpc.manager.cms.service.SyncConfigurationService;
import com.machloop.fpc.manager.cms.service.impl.MQAssignmentServiceImpl;
import com.machloop.fpc.manager.helper.MQMessageHelper;
import com.machloop.fpc.npm.appliance.bo.LogicalSubnetBO;
import com.machloop.fpc.npm.appliance.bo.MetricSettingBO;
import com.machloop.fpc.npm.appliance.dao.*;
import com.machloop.fpc.npm.appliance.data.LogicalSubnetDO;
import com.machloop.fpc.npm.appliance.data.MetricSettingDO;
import com.machloop.fpc.npm.appliance.data.NetworkDO;
import com.machloop.fpc.npm.appliance.data.NetworkTopologyDO;
import com.machloop.fpc.npm.appliance.service.BaselineService;
import com.machloop.fpc.npm.appliance.service.LogicalSubnetService;
import com.machloop.fpc.npm.appliance.service.MetricSettingService;

/**
 * @author guosk
 *
 * create at 2021年3月31日, fpc-manager
 */
@Order(3)
@Service
public class LogicalSubnetServiceImpl implements LogicalSubnetService, SyncConfigurationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(LogicalSubnetServiceImpl.class);

  private static final int MAX_SUBNET_NUMBER = 64;

  @Autowired
  private LogicalSubnetDao logicalSubnetDao;

  @Autowired
  private NetworkDao networkDao;

  @Autowired
  private NetworkTopologyDao networkTopologyDao;

  @Autowired
  private ServiceNetworkDao serviceNetworkDao;

  @Autowired
  private MetricSettingService metricSettingService;

  @Autowired
  private BaselineService baselineService;

  @Autowired
  private DictManager dictManager;

  @Autowired
  private AlertRuleService alertRuleService;

  @Autowired
  private MetricSettingDao metricSettingDao;

  @Autowired
  private GlobalSettingService globalSettingService;

  /**
   * @see com.machloop.fpc.npm.appliance.service.LogicalSubnetService#queryLogicalSubnets()
   */
  @Override
  public List<LogicalSubnetBO> queryLogicalSubnets() {
    Map<String, String> typeDict = dictManager.getBaseDict().getItemMap("appliance_subnet_type");

    Map<String, String> networkDict = networkDao.queryNetworks().stream()
        .collect(Collectors.toMap(NetworkDO::getId, NetworkDO::getName));

    List<LogicalSubnetDO> logicalSubnets = logicalSubnetDao.queryLogicalSubnets();

    List<LogicalSubnetBO> result = Lists.newArrayListWithCapacity(logicalSubnets.size());
    logicalSubnets.forEach(logicalSubnetDO -> {
      LogicalSubnetBO logicalSubnetBO = new LogicalSubnetBO();
      BeanUtils.copyProperties(logicalSubnetDO, logicalSubnetBO);
      logicalSubnetBO.setCreateTime(DateUtils.toStringISO8601(logicalSubnetDO.getCreateTime()));
      logicalSubnetBO
          .setNetworkName(MapUtils.getString(networkDict, logicalSubnetBO.getNetworkId(), ""));
      logicalSubnetBO.setTypeText(MapUtils.getString(typeDict, logicalSubnetBO.getType(), ""));

      result.add(logicalSubnetBO);
    });

    return result;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.LogicalSubnetService#queryLogicalSubnet(java.lang.String)
   */
  @Override
  public LogicalSubnetBO queryLogicalSubnet(String id) {
    LogicalSubnetDO logicalSubnetDO = logicalSubnetDao.queryLogicalSubnet(id);

    LogicalSubnetBO logicalSubnetBO = new LogicalSubnetBO();
    BeanUtils.copyProperties(logicalSubnetDO, logicalSubnetBO);
    logicalSubnetBO.setCreateTime(DateUtils.toStringISO8601(logicalSubnetDO.getCreateTime()));

    NetworkDO network = networkDao.queryNetwork(logicalSubnetBO.getNetworkId());
    logicalSubnetBO.setNetworkName(StringUtils.defaultIfBlank(network.getName(), ""));

    Map<String, String> typeDict = dictManager.getBaseDict().getItemMap("appliance_subnet_type");
    logicalSubnetBO.setTypeText(MapUtils.getString(typeDict, logicalSubnetBO.getType(), ""));

    return logicalSubnetBO;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.LogicalSubnetService#queryLogicalSubnetByCmsSubnetId(java.lang.String, java.lang.String)
   */
  @Override
  public LogicalSubnetBO queryLogicalSubnetByCmsSubnetId(String cmsSubnetId, String networkId) {
    LogicalSubnetDO logicalSubnetDO = logicalSubnetDao.queryLogicalSubnetByCmsSubnetId(cmsSubnetId,
        networkId);

    LogicalSubnetBO logicalSubnetBO = new LogicalSubnetBO();
    BeanUtils.copyProperties(logicalSubnetDO, logicalSubnetBO);

    return logicalSubnetBO;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.LogicalSubnetService#saveLogicalSubnet(com.machloop.fpc.npm.appliance.bo.LogicalSubnetBO, java.lang.String)
   */
  @Transactional
  @Override
  public LogicalSubnetBO saveLogicalSubnet(LogicalSubnetBO logicalSubnetBO,
      List<MetricSettingBO> metricSettings, String operatorId) {
    LogicalSubnetDO exist = logicalSubnetDao.queryLogicalSubnetByName(logicalSubnetBO.getName());
    if (StringUtils.isNotBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "子网名称不能重复");
    }

    NetworkDO network = networkDao.queryNetwork(logicalSubnetBO.getNetworkId());
    if (StringUtils.isBlank(network.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "主网络不存在");
    }

    List<LogicalSubnetDO> logicalSubnets = logicalSubnetDao.queryLogicalSubnets();
    if (logicalSubnets.size() >= MAX_SUBNET_NUMBER) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
          String.format("子网数量已超过最大上限[%s]", MAX_SUBNET_NUMBER));
    }

    LogicalSubnetDO logicalSubnetDO = new LogicalSubnetDO();
    BeanUtils.copyProperties(logicalSubnetBO, logicalSubnetDO);
    logicalSubnetDO.setOperatorId(operatorId);
    logicalSubnetDao.saveOrRecoverLogicalSubnet(logicalSubnetDO);

    if (metricSettings == null) {
      // 配置子网默认的统计度量值
      MetricSettingBO metricSettingBO = new MetricSettingBO();
      metricSettingBO.setSourceType(FpcConstants.SOURCE_TYPE_NETWORK);
      metricSettingBO.setNetworkId(logicalSubnetDO.getId());
      metricSettingService.saveDefaultMetricSettings(Lists.newArrayList(metricSettingBO),
          operatorId);
    } else {
      // 配置指定的统计度量值
      metricSettings.forEach(metricSetting -> {
        metricSetting.setNetworkId(logicalSubnetDO.getId());
      });
      metricSettingService.saveMetricSettings(metricSettings, operatorId);
    }

    return queryLogicalSubnet(logicalSubnetDO.getId());
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.LogicalSubnetService#updateLogicalSubnet(java.lang.String, com.machloop.fpc.npm.appliance.bo.LogicalSubnetBO, java.lang.String)
   */
  @Override
  public LogicalSubnetBO updateLogicalSubnet(String id, LogicalSubnetBO logicalSubnetBO,
      String operatorId) {
    LogicalSubnetDO exist = logicalSubnetDao.queryLogicalSubnet(id);
    if (StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "子网不存在");
    }

    NetworkDO network = networkDao.queryNetwork(logicalSubnetBO.getNetworkId());
    if (StringUtils.isBlank(network.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "主网络不存在");
    }

    LogicalSubnetDO subnetByName = logicalSubnetDao
        .queryLogicalSubnetByName(logicalSubnetBO.getName());
    if (StringUtils.isNotBlank(subnetByName.getId())
        && !StringUtils.equals(id, subnetByName.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "子网名称不能重复");
    }

    LogicalSubnetDO logicalSubnetDO = new LogicalSubnetDO();
    BeanUtils.copyProperties(logicalSubnetBO, logicalSubnetDO);
    logicalSubnetDO.setId(id);
    logicalSubnetDO.setOperatorId(operatorId);
    logicalSubnetDao.updateLogicalSubnet(logicalSubnetDO);

    return queryLogicalSubnet(id);
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.LogicalSubnetService#deleteLogicalSubnet(java.lang.String, java.lang.String, boolean)
   */
  @Transactional
  @Override
  public LogicalSubnetBO deleteLogicalSubnet(String id, String operatorId, boolean forceDelete) {
    LogicalSubnetDO exist = logicalSubnetDao.queryLogicalSubnet(id);
    if (StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "子网不存在");
    }

    // 删除子网时查看该子网是否配置在某个业务下，如果有则不能删除
    if (!forceDelete
        && CollectionUtils.isNotEmpty(serviceNetworkDao.queryServiceNetworks(null, id))) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "该子网存在相关联的业务，无法删除");
    }

    // 是否有告警作用于该子网
    if (!forceDelete && alertRuleService
        .queryAlertRulesBySource(FpcConstants.SOURCE_TYPE_NETWORK, id, null).size() > 0) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "该子网已作用于告警，无法删除");
    }

    // 该子网是否已配置到网络拓扑中
    NetworkTopologyDO networkTopologyDO = networkTopologyDao.queryNetworkTopologyByNetworkId(id);
    if (!forceDelete && StringUtils.isNotBlank(networkTopologyDO.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "该子网已配置到网络拓扑中，无法删除");
    }

    // 删除子网
    logicalSubnetDao.deleteLogicalSubnet(id, operatorId);

    // 删除子网络下基线定义
    baselineService.deleteBaselineSettings(FpcConstants.SOURCE_TYPE_NETWORK, id, null);

    // 删除子网络下的统计配置
    metricSettingService.deleteMetricSetting(FpcConstants.SOURCE_TYPE_NETWORK, id, null, null);

    return queryLogicalSubnet(id);
  }

  @PostConstruct
  public void init() {
    MQAssignmentServiceImpl.register(this,
        Lists.newArrayList(FpcCmsConstants.MQ_TAG_LOGICALSUBNET));
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

    int syncTotalCount = messages.stream().mapToInt(item -> syncLogicalSubnet(item)).sum();
    LOGGER.info("current sync subnet total: {}.", syncTotalCount);

    return syncTotalCount;
  }

  private int syncLogicalSubnet(Map<String, Object> messageBody) {
    int syncCount = 0;

    List<String> networkIds = CsvUtils
        .convertCSVToList(MapUtils.getString(messageBody, "networkIds"));
    if (CollectionUtils.isEmpty(networkIds)) {
      return syncCount;
    }

    String[] action = new String[1];
    action[0] = MapUtils.getString(messageBody, "action");
    String subnetInCmsId = MapUtils.getString(messageBody, "id");
    String name = MapUtils.getString(messageBody, "name");
    int bandwidth = MapUtils.getIntValue(messageBody, "bandwidth", 0);
    String type = MapUtils.getString(messageBody, "type");
    String configuration = MapUtils.getString(messageBody, "configuration");

    // 判断下发的子网所在的网络是否存在
    List<String> existNetworkIds = networkDao.queryNetworks().stream().map(NetworkDO::getId)
        .collect(Collectors.toList());
    List<String> validNetworkIds = networkIds.stream()
        .filter(networkId -> existNetworkIds.contains(networkId)).collect(Collectors.toList());
    outer: if (CollectionUtils.isEmpty(validNetworkIds)) {
      if (StringUtils.equals(action[0], FpcCmsConstants.SYNC_ACTION_MODIFY)) {
        // 此类情况为上级编辑子网，删掉了子网所属于的a网络，下发到探针上时，应该删掉属于a网络的子网
        action[0] = FpcCmsConstants.SYNC_ACTION_DELETE;
        break outer;
      }
      // 不存在子网所在的网络
      return syncCount;
    }

    List<Map<String, Object>> metricSetting = JsonHelper.deserialize(
        JsonHelper.serialize(messageBody.get("metricSettings")),
        new TypeReference<List<Map<String, Object>>>() {
        });

    List<MetricSettingBO> metricSettingList = Lists
        .newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    metricSetting.forEach(metricSettingMap -> {
      MetricSettingBO metricSettingBO = new MetricSettingBO();
      metricSettingBO.setId(MapUtils.getString(metricSettingMap, "id"));
      metricSettingBO.setMetricSettingInCmsId(MapUtils.getString(metricSettingMap, "id"));
      metricSettingBO.setSourceType(MapUtils.getString(metricSettingMap, "sourceType"));
      metricSettingBO.setNetworkId(MapUtils.getString(metricSettingMap, "networkId"));
      metricSettingBO.setServiceId(MapUtils.getString(metricSettingMap, "packetFileId"));
      metricSettingBO.setMetric(MapUtils.getString(metricSettingMap, "metric"));
      metricSettingBO.setValue(MapUtils.getString(metricSettingMap, "value"));
      metricSettingList.add(metricSettingBO);
    });

    List<NetworkDO> networks = networkDao.queryNetworks(networkIds);
    boolean reName = networks.size() > 1 ? true : false;

    List<LogicalSubnetBO> addSubnets = Lists.newArrayListWithCapacity(networks.size());
    Map<String, LogicalSubnetBO> modifySubnets = Maps.newHashMapWithExpectedSize(networks.size());
    List<String> deleteSubnetIds = Lists.newArrayListWithCapacity(networks.size());
    networks.forEach(network -> {
      // 构造本地子网对象
      LogicalSubnetBO logicalSubnet = new LogicalSubnetBO();
      logicalSubnet.setId(subnetInCmsId);
      logicalSubnet.setSubnetInCmsId(subnetInCmsId);
      logicalSubnet.setName(reName ? StringUtils.joinWith("_", name, network.getName()) : name);
      logicalSubnet.setNetworkId(network.getId());
      logicalSubnet.setBandwidth(bandwidth);
      logicalSubnet.setType(type);
      logicalSubnet.setConfiguration(configuration);
      logicalSubnet.setDescription(CMS_ASSIGNMENT);

      // 本次下发的子网是否存在
      LogicalSubnetBO exist = queryLogicalSubnetByCmsSubnetId(subnetInCmsId, network.getId());

      switch (action[0]) {
        case FpcCmsConstants.SYNC_ACTION_ADD:
        case FpcCmsConstants.SYNC_ACTION_MODIFY:
          if (StringUtils.isNotBlank(exist.getId())) {
            modifySubnets.put(exist.getId(), logicalSubnet);
            LogHelper.auditAssignOperate(
                globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
                logicalSubnet.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_UPDATE));
          } else {
            addSubnets.add(logicalSubnet);
            LogHelper.auditAssignOperate(
                globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
                logicalSubnet.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_SAVE));
          }
          break;
        case FpcCmsConstants.SYNC_ACTION_DELETE:
          deleteSubnetIds.add(exist.getId());
          LogHelper.auditAssignOperate(
              globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
              logicalSubnet.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_DELETE));
          break;
        default:
          break;
      }
    });
    if (CollectionUtils.isEmpty(networks)
        && StringUtils.equals(action[0], FpcCmsConstants.SYNC_ACTION_DELETE)) {
      deleteSubnetIds.add(subnetInCmsId);
    }

    int addCount = 0;
    int modifyCount = 0;
    int deleteCount = 0;
    try {
      // 新增子网
      if (CollectionUtils.isNotEmpty(addSubnets)) {
        addSubnets.forEach(addSubnet -> {
          saveLogicalSubnet(addSubnet, metricSettingList, CMS_ASSIGNMENT);
        });
        // metricSettingDao.batchSaveMetricSetting(metricSettingList);
        addCount = addSubnets.size();
      }

      // 修改子网
      if (CollectionUtils.isNotEmpty(modifySubnets.keySet())) {
        modifySubnets.forEach((id, modifySubnet) -> {
          updateLogicalSubnet(id, modifySubnet, CMS_ASSIGNMENT);
        });
        metricSettingList.forEach(metricSettingBO -> {
          MetricSettingDO metricSettingDO = new MetricSettingDO();
          BeanUtils.copyProperties(metricSettingBO, metricSettingDO);
          metricSettingDao.updateMetricSetting(metricSettingDO);
        });
        modifyCount = modifySubnets.size();
      }

      // 删除子网
      if (CollectionUtils.isNotEmpty(deleteSubnetIds)) {
        deleteSubnetIds.forEach(subnetId -> {
          deleteLogicalSubnet(subnetId, CMS_ASSIGNMENT, true);
        });
        metricSettingDao.deleteMetricSetting(FpcConstants.SOURCE_TYPE_SERVICE, null,
            deleteSubnetIds.get(0), null);
        deleteCount = deleteSubnetIds.size();
      }

      // 本次同步数据量
      syncCount = addCount + modifyCount + deleteCount;

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("current sync subnet status: [add: {}, modify: {}, delete: {}]", addCount,
            modifyCount, deleteCount);
      }
    } catch (BusinessException e) {
      LOGGER.warn("sync faild. error msg: {}", e.getMessage());
      return -1;
    }

    return syncCount;
  }

  /**
   * @see com.machloop.fpc.manager.cms.service.SyncConfigurationService#clearLocalConfiguration(java.lang.String, java.util.Date, boolean)
   */
  @Override
  public int clearLocalConfiguration(String tag, boolean onlyLocal, Date beforeTime) {
    int clearCount = 0;
    List<String> subnetIds = logicalSubnetDao.queryLogicalSubnetIds(onlyLocal);
    for (String subnetId : subnetIds) {
      try {
        deleteLogicalSubnet(subnetId, CMS_ASSIGNMENT, true);
        clearCount++;
      } catch (BusinessException e) {
        LOGGER.warn("delete subnet failed. error msg: {}", e.getMessage());
        continue;
      }
    }
    return clearCount;
  }

  /**
   * @see com.machloop.fpc.manager.cms.service.SyncConfigurationService#getAssignConfigurationIds(java.lang.String, java.util.Date)
   */
  @Override
  public List<String> getAssignConfigurationIds(String tag, Date beforeTime) {
    // 获取上级cms下发的子网
    List<
        LogicalSubnetDO> logicalSubnetList = logicalSubnetDao.queryAssignLogicalSubnets(beforeTime);

    List<String> assignIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 子网-网络ID集合
    for (LogicalSubnetDO subnet : logicalSubnetList) {
      List<String> networkIds = CsvUtils.convertCSVToList(subnet.getNetworkId());
      List<String> subnetNetworkIdList = networkIds.stream()
          .map(networkId -> StringUtils.joinWith("_", subnet.getSubnetInCmsId(), networkId))
          .collect(Collectors.toList());
      assignIds.addAll(subnetNetworkIdList);
      // 度量指标ID集合
      List<String> settingAssignIds = metricSettingDao.queryAssignMetricSettingIds(
          FpcCmsConstants.SOURCE_TYPE_NETWORK, subnet.getId(), null, beforeTime);
      assignIds.addAll(settingAssignIds);
    }

    return assignIds;
  }
}
