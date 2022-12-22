package com.machloop.fpc.cms.center.sensor.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.fpc.cms.center.appliance.bo.FilterRuleBO;
import com.machloop.fpc.cms.center.appliance.bo.ServiceBO;
import com.machloop.fpc.cms.center.appliance.dao.FilterRuleIpDao;
import com.machloop.fpc.cms.center.appliance.dao.NetworkPolicyDao;
import com.machloop.fpc.cms.center.appliance.dao.NetworkTopologyDao;
import com.machloop.fpc.cms.center.appliance.dao.ServiceNetworkDao;
import com.machloop.fpc.cms.center.appliance.data.FilterRuleNetworkDO;
import com.machloop.fpc.cms.center.appliance.data.NetworkPolicyDO;
import com.machloop.fpc.cms.center.appliance.data.NetworkTopologyDO;
import com.machloop.fpc.cms.center.appliance.data.ServiceNetworkDO;
import com.machloop.fpc.cms.center.appliance.service.BaselineService;
import com.machloop.fpc.cms.center.appliance.service.FilterRuleService;
import com.machloop.fpc.cms.center.appliance.service.ServiceService;
import com.machloop.fpc.cms.center.broker.bo.FpcNetworkBO;
import com.machloop.fpc.cms.center.broker.service.subordinate.FpcNetworkService;
import com.machloop.fpc.cms.center.broker.service.subordinate.MQAssignmentService;
import com.machloop.fpc.cms.center.central.service.ClusterService;
import com.machloop.fpc.cms.center.helper.MQMessageHelper;
import com.machloop.fpc.cms.center.sensor.bo.SensorNetworkGroupBO;
import com.machloop.fpc.cms.center.sensor.dao.SensorLogicalSubnetDao;
import com.machloop.fpc.cms.center.sensor.dao.SensorNetworkDao;
import com.machloop.fpc.cms.center.sensor.dao.SensorNetworkGroupDao;
import com.machloop.fpc.cms.center.sensor.data.SensorLogicalSubnetDO;
import com.machloop.fpc.cms.center.sensor.data.SensorNetworkDO;
import com.machloop.fpc.cms.center.sensor.data.SensorNetworkGroupDO;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkGroupService;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkPermService;
import com.machloop.fpc.cms.common.FpcCmsConstants;

import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * @author "Minjiajun"
 * <p>
 * create at 2021年10月25日, fpc-cms-center
 */
@Transactional
@Order(14)
@Service
public class SensorNetworkGroupServiceImpl
    implements SensorNetworkGroupService, MQAssignmentService {

  private static final List<String> TAGS = Lists.newArrayList(FpcCmsConstants.MQ_TAG_NETWORKPOLICY);

  @Autowired
  private FilterRuleIpDao filterRuleDao;

  @Autowired
  private ApplicationContext context;

  @Autowired
  private SensorNetworkDao sensorNetworkDao;

  @Autowired
  private SensorNetworkGroupDao sensorNetworkGroupDao;

  @Autowired
  private ServiceService serviceService;

  @Autowired
  private ServiceNetworkDao serviceNetworkDao;

  @Autowired
  private NetworkTopologyDao networkTopologyDao;

  @Autowired
  private SensorNetworkPermService sensorNetworkPermService;

  @Autowired
  private FpcNetworkService fpcNetworkService;

  @Autowired
  private BaselineService baselineService;

  @Autowired
  private ClusterService clusterService;

  @Autowired
  private NetworkPolicyDao networkPolicyDao;

  @Autowired
  private FilterRuleService filterRuleService;

  @Autowired
  private SensorLogicalSubnetDao sensorLogicalSubnetDao;

  /**
   * @see com.machloop.fpc.cms.center.sensor.service.SensorNetworkGroupService#querySensorNetworkGroups()
   */
  @Override
  public List<SensorNetworkGroupBO> querySensorNetworkGroups() {
    List<SensorNetworkGroupDO> sensorNetworkDOGroupList = sensorNetworkGroupDao
        .querySensorNetworkGroups();
    List<SensorNetworkGroupBO> result = Lists
        .newArrayListWithCapacity(sensorNetworkDOGroupList.size());
    if (sensorNetworkDOGroupList.size() == 0) {
      return result;
    }

    List<SensorNetworkDO> networkList = sensorNetworkDao.querySensorNetworks();

    // 获取连接异常的探针节点
    Map<String, String> abnormalNodes = clusterService.queryAbnormalNodesAndRefresh();

    for (SensorNetworkGroupDO sensorNetworkGroup : sensorNetworkDOGroupList) {
      List<String> sensorNetworkIdList = CsvUtils
          .convertCSVToList(sensorNetworkGroup.getNetworkInSensorIds());
      List<String> sensorNetworkNameList = Lists
          .newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
      List<String> sensorIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      int bandwidth = 0;
      for (SensorNetworkDO sensorNetwork : networkList) {
        if (sensorNetworkIdList.contains(sensorNetwork.getNetworkInSensorId())) {
          sensorNetworkNameList.add(sensorNetwork.getNetworkInSensorName());
          sensorIds.add(sensorNetwork.getSensorId());
          bandwidth += sensorNetwork.getBandwidth();
        }
      }

      SensorNetworkGroupBO sensorNetworkGroupBO = new SensorNetworkGroupBO();
      BeanUtils.copyProperties(sensorNetworkGroup, sensorNetworkGroupBO);
      sensorNetworkGroupBO
          .setNetworkInSensorNames(CsvUtils.convertCollectionToCSV(sensorNetworkNameList));
      sensorNetworkGroupBO.setBandwidth(bandwidth);

      String status = FpcCmsConstants.CONNECT_STATUS_NORMAL;
      String detail = "";
      Set<String> abnormalIps = sensorIds.stream()
          .filter(fpcSerialNumber -> abnormalNodes.containsKey(fpcSerialNumber))
          .map(fpcSerialNumber -> abnormalNodes.get(fpcSerialNumber)).collect(Collectors.toSet());
      if (CollectionUtils.isNotEmpty(abnormalIps)) {
        status = FpcCmsConstants.CONNECT_STATUS_ABNORMAL;
        detail = String.format("探针设备[%s]异常或未连接，请检查设备数据节点状态",
            CsvUtils.convertCollectionToCSV(abnormalIps));
      }
      sensorNetworkGroupBO.setStatus(status);
      sensorNetworkGroupBO.setStatusDetail(detail);

      String networkId = sensorNetworkIdList.get(0);
      List<NetworkPolicyDO> networkPolicyDOS = networkPolicyDao
          .queryNetworkPolicyByNetworkIdAndPolicySource(networkId,
              FpcCmsConstants.APPLIANCE_NETWORK_POLICY_SEND, sensorNetworkGroupBO.getId());
      List<String> sendPolicyIds = networkPolicyDOS.stream().map(NetworkPolicyDO::getPolicyId)
          .collect(Collectors.toList());
      sensorNetworkGroupBO.setSendPolicyIds(CsvUtils.convertCollectionToCSV(sendPolicyIds));

      result.add(sensorNetworkGroupBO);
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.service.SensorNetworkGroupService#querySensorNetworkGroup(java.lang.String)
   */
  @Override
  public SensorNetworkGroupBO querySensorNetworkGroup(String id) {
    SensorNetworkGroupDO sensorNetworkGroupDO = sensorNetworkGroupDao.querySensorNetworkGroup(id);

    SensorNetworkGroupBO sensorNetworkGroupBO = new SensorNetworkGroupBO();
    BeanUtils.copyProperties(sensorNetworkGroupDO, sensorNetworkGroupBO);
    Map<String, String> networkMap = fpcNetworkService.queryAllNetworks().stream()
        .collect(Collectors.toMap(FpcNetworkBO::getFpcNetworkId, FpcNetworkBO::getFpcNetworkName));
    List<String> networkNames = CsvUtils
        .convertCSVToList(sensorNetworkGroupBO.getNetworkInSensorIds()).stream()
        .filter(networkId -> networkMap.containsKey(networkId))
        .map(networkId -> networkMap.get(networkId)).collect(Collectors.toList());
    sensorNetworkGroupBO.setNetworkInSensorNames(CsvUtils.convertCollectionToCSV(networkNames));

    List<String> sensorNetworkIdList = CsvUtils
        .convertCSVToList(sensorNetworkGroupDO.getNetworkInSensorIds());
    if (CollectionUtils.isNotEmpty(sensorNetworkIdList)) {
      String networkId = sensorNetworkIdList.get(0);
      List<NetworkPolicyDO> networkPolicyDOS = networkPolicyDao
          .queryNetworkPolicyByNetworkIdAndPolicySource(networkId,
              FpcCmsConstants.APPLIANCE_NETWORK_POLICY_SEND, sensorNetworkGroupBO.getId());
      List<String> sendPolicyIds = networkPolicyDOS.stream().map(NetworkPolicyDO::getPolicyId)
          .collect(Collectors.toList());
      sensorNetworkGroupBO.setSendPolicyIds(CsvUtils.convertCollectionToCSV(sendPolicyIds));
    }

    return sensorNetworkGroupBO;
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.service.SensorNetworkGroupService#saveSensorNetworkGroups(com.machloop.fpc.cms.center.sensor.bo.SensorNetworkGroupBO, java.lang.String)
   */
  @Override
  public SensorNetworkGroupBO saveSensorNetworkGroup(SensorNetworkGroupBO sensorNetworkGroupBO,
      String operatorId) {
    SensorNetworkGroupDO exist = sensorNetworkGroupDao
        .querySensorNetworkGroupByName(sensorNetworkGroupBO.getName());
    if (StringUtils.isNotBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "网络组名称不能重复");
    }
    // 校验：一个网络只能存在于一个网络组
    List<String> existNetworkInSensorIdList = sensorNetworkGroupDao.querySensorNetworkGroups()
        .stream().map(e -> e.getNetworkInSensorIds()).collect(Collectors.toList());
    List<String> existNetworkIdList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    for (String networkId : existNetworkInSensorIdList) {
      String[] filters = StringUtils.split(networkId, ",");
      for (int i = 0; i < filters.length; i++) {
        existNetworkIdList.add(filters[i]);
      }
    }
    String[] splitNetworkId = StringUtils.split(sensorNetworkGroupBO.getNetworkInSensorIds(), ",");
    for (int i = 0; i < splitNetworkId.length; i++) {
      if (existNetworkIdList.contains(splitNetworkId[i])) {
        throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "该网络已存在于其他网络组");
      }
    }

    SensorNetworkGroupDO sensorNetworkGroupDO = new SensorNetworkGroupDO();
    BeanUtils.copyProperties(sensorNetworkGroupBO, sensorNetworkGroupDO);
    sensorNetworkGroupDO.setOperatorId(operatorId);

    sensorNetworkGroupDO = sensorNetworkGroupDao.saveSensorNetworkGroup(sensorNetworkGroupDO);
    sensorNetworkGroupBO.setId(sensorNetworkGroupDO.getId());


    List<NetworkPolicyDO> policyList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<String> sendPolicies = CsvUtils.convertCSVToList(sensorNetworkGroupBO.getSendPolicyIds());
    List<String> networkIdList = CsvUtils
        .convertCSVToList(sensorNetworkGroupBO.getNetworkInSensorIds());
    if (CollectionUtils.isNotEmpty(sendPolicies)) {
      networkIdList.forEach(networkId -> sendPolicies.forEach(sendPolicy -> {
        NetworkPolicyDO networkPolicyDO = new NetworkPolicyDO();
        networkPolicyDO.setNetworkId(networkId);
        networkPolicyDO.setPolicyId(sendPolicy);
        networkPolicyDO.setPolicyType(FpcCmsConstants.APPLIANCE_NETWORK_POLICY_SEND);
        networkPolicyDO.setOperatorId(operatorId);
        networkPolicyDO.setPolicySource(sensorNetworkGroupBO.getId());
        policyList.add(networkPolicyDO);
      }));
    }
    if (CollectionUtils.isNotEmpty(policyList)) {
      networkPolicyDao.mergeNetworkPolicys(policyList);
    }
    networkIdList.forEach(networkId -> {
      policyList.addAll(networkPolicyDao.queryNetworkPolicyByNetworkIdAndPolicySource(networkId,
          FpcCmsConstants.APPLIANCE_NETWORK_POLICY_SEND, Constants.BOOL_NO));
    });
    if (CollectionUtils.isNotEmpty(policyList)) {
      List<Map<String, Object>> messageBodys = policyList.stream()
          .map(policy -> networkPolicy2MessageBody(policy, FpcCmsConstants.SYNC_ACTION_MODIFY))
          .collect(Collectors.toList());
      Map<String, Object> messageBody = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      messageBody.put("batch", true);
      messageBody.put("data", messageBodys);
      Message message = MQMessageHelper.convertToMessage(messageBody,
          FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT, FpcCmsConstants.MQ_TAG_NETWORKPOLICY, null);
      assignmentConfiguration(message, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT);
    }

    return sensorNetworkGroupBO;
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.service.SensorNetworkGroupService#updateSensorNetworkGroups(java.lang.String, com.machloop.fpc.cms.center.sensor.bo.SensorNetworkGroupBO, java.lang.String)
   */
  @Override
  public SensorNetworkGroupBO updateSensorNetworkGroup(String id,
      SensorNetworkGroupBO sensorNetworkGroupBO, String operatorId) {
    SensorNetworkGroupDO sensorNetworkGroupDO = sensorNetworkGroupDao.querySensorNetworkGroup(id);
    if (StringUtils.isBlank(sensorNetworkGroupDO.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "网络组不存在");
    }

    List<NetworkPolicyDO> existNetworkPolicyDOListOfNetworkGroup = networkPolicyDao
        .queryNetworkPolicyByPolicyTypeAndPolicySource(
            FpcCmsConstants.APPLIANCE_NETWORK_POLICY_SEND, id);
    List<String> existNetworkIdList = CsvUtils
        .convertCSVToList(sensorNetworkGroupDO.getNetworkInSensorIds());
    // 针对每个网络已经存在的策略结合探针网络配置的策略，考虑是下发修改还是删除配置
    existNetworkIdList.forEach(existNetworkId -> {
      List<
          NetworkPolicyDO> networkPolicyDOListOfNetworkGroup = existNetworkPolicyDOListOfNetworkGroup
              .stream().filter(networkPolicyDO -> StringUtils.equals(existNetworkId,
                  networkPolicyDO.getNetworkId()))
              .collect(Collectors.toList());
      List<NetworkPolicyDO> networkPolicyDOSListOfNetwork = networkPolicyDao
          .queryNetworkPolicyByNetworkIdAndPolicySource(existNetworkId,
              FpcCmsConstants.APPLIANCE_NETWORK_POLICY_SEND, Constants.BOOL_NO);
      List<Map<String, Object>> existMessageBody;
      if (CollectionUtils.isNotEmpty(networkPolicyDOSListOfNetwork)) {
        existMessageBody = networkPolicyDOSListOfNetwork.stream()
            .map(policy -> networkPolicy2MessageBody(policy, FpcCmsConstants.SYNC_ACTION_MODIFY))
            .collect(Collectors.toList());
        Map<String,
            Object> messageBody = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
        messageBody.put("batch", true);
        messageBody.put("data", existMessageBody);
        Message message = MQMessageHelper.convertToMessage(messageBody,
            FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT, FpcCmsConstants.MQ_TAG_NETWORKPOLICY, null);
        assignmentConfiguration(message, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT);
      } else {
        if (CollectionUtils.isNotEmpty(networkPolicyDOListOfNetworkGroup)) {
          existMessageBody = networkPolicyDOListOfNetworkGroup.stream()
              .map(policy -> networkPolicy2MessageBody(policy, FpcCmsConstants.SYNC_ACTION_DELETE))
              .collect(Collectors.toList());
          Map<String,
              Object> messageBody = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
          messageBody.put("batch", true);
          messageBody.put("data", existMessageBody);
          Message message = MQMessageHelper.convertToMessage(messageBody,
              FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT, FpcCmsConstants.MQ_TAG_NETWORKPOLICY, null);
          assignmentConfiguration(message, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT);
        }
      }
    });

    // 需要先删除该网络组之前所选网络对应的网络策略
    networkPolicyDao.deleteNetworkPolicyByPolicyTypeAndPolicySource(
        FpcCmsConstants.APPLIANCE_NETWORK_POLICY_SEND, id);

    BeanUtils.copyProperties(sensorNetworkGroupBO, sensorNetworkGroupDO);
    sensorNetworkGroupDao.updateSensorNetworkGroup(id, sensorNetworkGroupDO, operatorId);

    // 如果当前修改的网络组已配置进业务中，则重新下发包含该网络组的业务
    List<ServiceNetworkDO> serviceNetworkList = serviceNetworkDao
        .queryServiceNetworkByNetworkGroupId(id);
    for (ServiceNetworkDO serviceNetworkDO : serviceNetworkList) {
      if (StringUtils.isNotBlank(serviceNetworkDO.getId())) {
        ServiceBO serviceBO = serviceService.queryService(serviceNetworkDO.getServiceId());
        serviceService.updateService(serviceNetworkDO.getServiceId(), serviceBO, operatorId);
      }
    }

    // 重新下发包含该网络组的存储过滤规则
    List<FilterRuleNetworkDO> filterRuleNetworkDOS = filterRuleDao
        .queryFilterRuleNetworkByNetworkGroupId(id);
    for (FilterRuleNetworkDO filterRuleNetworkDO : filterRuleNetworkDOS) {
      if (StringUtils.isNotBlank(filterRuleNetworkDO.getId())) {
        FilterRuleBO filterRuleBO = filterRuleService
            .queryFilterRule(filterRuleNetworkDO.getFilterRuleId());
        filterRuleService.updateFilterRule(filterRuleNetworkDO.getFilterRuleId(), filterRuleBO,
            operatorId);
      }
    }

    List<NetworkPolicyDO> policyList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<String> sendPolicies = CsvUtils.convertCSVToList(sensorNetworkGroupBO.getSendPolicyIds());
    List<String> networkIdList = CsvUtils
        .convertCSVToList(sensorNetworkGroupBO.getNetworkInSensorIds());
    if (CollectionUtils.isNotEmpty(sendPolicies)) {
      networkIdList.forEach(networkId -> sendPolicies.forEach(sendPolicy -> {
        NetworkPolicyDO networkPolicyDO = new NetworkPolicyDO();
        networkPolicyDO.setNetworkId(networkId);
        networkPolicyDO.setPolicyId(sendPolicy);
        networkPolicyDO.setPolicyType(FpcCmsConstants.APPLIANCE_NETWORK_POLICY_SEND);
        networkPolicyDO.setOperatorId(operatorId);
        networkPolicyDO.setPolicySource(id);
        policyList.add(networkPolicyDO);
      }));
    }
    if (CollectionUtils.isNotEmpty(policyList)) {
      networkPolicyDao.mergeNetworkPolicys(policyList);
    }
    networkIdList.forEach(networkId -> {
      policyList.addAll(networkPolicyDao.queryNetworkPolicyByNetworkIdAndPolicySource(networkId,
          FpcCmsConstants.APPLIANCE_NETWORK_POLICY_SEND, Constants.BOOL_NO));
    });
    List<Map<String, Object>> messageBodys;
    if (CollectionUtils.isNotEmpty(policyList)) {
      messageBodys = policyList.stream()
          .map(policy -> networkPolicy2MessageBody(policy, FpcCmsConstants.SYNC_ACTION_MODIFY))
          .collect(Collectors.toList());
      Map<String, Object> messageBody = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      messageBody.put("batch", true);
      messageBody.put("data", messageBodys);
      Message message = MQMessageHelper.convertToMessage(messageBody,
          FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT, FpcCmsConstants.MQ_TAG_NETWORKPOLICY, null);
      assignmentConfiguration(message, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT);
    }

    return querySensorNetworkGroup(id);
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.service.SensorNetworkGroupService#removeNetworkFromGroup(java.lang.String, java.lang.String)
   */
  @Override
  public void removeNetworkFromGroup(String networkId, String operatorId) {
    List<SensorNetworkGroupDO> networkGroups = sensorNetworkGroupDao
        .querySensorNetworkGroupsByNetwork(networkId);
    networkGroups.forEach(networkGroup -> {
      List<String> networkIds = CsvUtils.convertCSVToList(networkGroup.getNetworkInSensorIds());
      networkIds.remove(networkId);
      SensorNetworkGroupBO sensorNetworkGroupBO = new SensorNetworkGroupBO();
      BeanUtils.copyProperties(networkGroup, sensorNetworkGroupBO);
      if (networkIds.size() == 0) {
        deleteSensorNetworkGroupByNetworkId(sensorNetworkGroupBO.getId(), operatorId);
      } else {
        networkGroup.setNetworkInSensorIds(CsvUtils.convertCollectionToCSV(networkIds));
        updateSensorNetworkGroup(networkGroup.getId(), sensorNetworkGroupBO, operatorId);
      }
    });
  }

  public void deleteSensorNetworkGroupByNetworkId(String id, String operatorId) {

    networkPolicyDao.deleteNetworkPolicyByPolicyTypeAndPolicySource(
        FpcCmsConstants.APPLIANCE_NETWORK_POLICY_SEND, id);

    // 删除网络组
    sensorNetworkGroupDao.deleteSensorNetworkGroup(id, operatorId);

  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.service.SensorNetworkGroupService#deleteSensorNetworkGroups(java.lang.String, java.lang.String)
   */
  @Override
  @Transactional
  public SensorNetworkGroupBO deleteSensorNetworkGroup(String id, String operatorId) {
    SensorNetworkGroupDO sensorNetworkGroupDO = sensorNetworkGroupDao.querySensorNetworkGroup(id);
    if (StringUtils.isBlank(sensorNetworkGroupDO.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "网络组不存在");
    }

    List<String> serviceNetworkGroupIdList = serviceNetworkDao.queryServiceNetworks().stream()
        .map(e -> e.getNetworkGroupId()).collect(Collectors.toList());
    if (serviceNetworkGroupIdList.contains(id)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "该网络组存在相关联的业务，无法删除");
    }

    // 判断网络组是否在存储过滤规则中使用
    List<String> filterRuleNetworkGroupIdList = filterRuleDao.queryFilterRuleNetworks().stream()
        .map(item -> item.getNetworkGroupId()).collect(Collectors.toList());
    if (filterRuleNetworkGroupIdList.contains(id)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "该网络被相关的存储过滤规则使用，无法删除");
    }

    // 该网络组是否已配置到网络拓扑中
    NetworkTopologyDO networkTopologyDO = networkTopologyDao.queryNetworkTopologyByNetworkId(id);
    if (StringUtils.isNotBlank(networkTopologyDO.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
          "该网络组已配置到网络拓扑中，无法删除");
    }

    // 删除网络权限
    sensorNetworkPermService.deleteSensorNetworkPermByNetworkGroup(id);

    // 删除网络组下基线定义
    baselineService.deleteBaselineSettings(FpcCmsConstants.SOURCE_TYPE_NETWORK, null, id, null);


    List<NetworkPolicyDO> existNetworkPolicyDOListOfNetworkGroup = networkPolicyDao
        .queryNetworkPolicyByPolicyTypeAndPolicySource(
            FpcCmsConstants.APPLIANCE_NETWORK_POLICY_SEND, id);
    List<String> existNetworkIdList = CsvUtils
        .convertCSVToList(sensorNetworkGroupDO.getNetworkInSensorIds());

    // 针对每个网络已经存在的策略结合探针网络配置的策略，考虑是下发修改还是删除配置
    existNetworkIdList.forEach(existNetworkId -> {
      List<
          NetworkPolicyDO> networkPolicyDOListOfNetworkGroup = existNetworkPolicyDOListOfNetworkGroup
              .stream().filter(networkPolicyDO -> StringUtils.equals(existNetworkId,
                  networkPolicyDO.getNetworkId()))
              .collect(Collectors.toList());
      List<NetworkPolicyDO> networkPolicyDOSListOfNetwork = networkPolicyDao
          .queryNetworkPolicyByNetworkIdAndPolicySource(existNetworkId,
              FpcCmsConstants.APPLIANCE_NETWORK_POLICY_SEND, Constants.BOOL_NO);
      List<Map<String, Object>> existMessageBody = Lists.newArrayListWithCapacity(0);
      if (CollectionUtils.isNotEmpty(networkPolicyDOSListOfNetwork)) {
        existMessageBody = networkPolicyDOSListOfNetwork.stream()
            .map(policy -> networkPolicy2MessageBody(policy, FpcCmsConstants.SYNC_ACTION_MODIFY))
            .collect(Collectors.toList());
        Map<String,
            Object> messageBody = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
        messageBody.put("batch", true);
        messageBody.put("data", existMessageBody);
        Message message = MQMessageHelper.convertToMessage(messageBody,
            FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT, FpcCmsConstants.MQ_TAG_NETWORKPOLICY, null);
        assignmentConfiguration(message, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT);
      } else {
        if (CollectionUtils.isNotEmpty(networkPolicyDOListOfNetworkGroup)) {
          existMessageBody = networkPolicyDOListOfNetworkGroup.stream()
              .map(policy -> networkPolicy2MessageBody(policy, FpcCmsConstants.SYNC_ACTION_DELETE))
              .collect(Collectors.toList());
          Map<String,
              Object> messageBody = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
          messageBody.put("batch", true);
          messageBody.put("data", existMessageBody);
          Message message = MQMessageHelper.convertToMessage(messageBody,
              FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT, FpcCmsConstants.MQ_TAG_NETWORKPOLICY, null);
          assignmentConfiguration(message, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT);
        }
      }
    });

    networkPolicyDao.deleteNetworkPolicyByPolicyTypeAndPolicySource(
        FpcCmsConstants.APPLIANCE_NETWORK_POLICY_SEND, id);

    // 删除网络组
    sensorNetworkGroupDao.deleteSensorNetworkGroup(id, operatorId);
    SensorNetworkGroupBO sensorNetworkGroupBO = new SensorNetworkGroupBO();
    BeanUtils.copyProperties(sensorNetworkGroupDO, sensorNetworkGroupBO);
    return sensorNetworkGroupBO;
  }

  /********************************************************************************************************
   * 下发模块
   *******************************************************************************************************/
  @Override
  public DefaultMQProducer getProducer() {
    return context.getBean("getRocketMQProducer", DefaultMQProducer.class);
  }

  @Override
  public List<String> getTags() {
    return TAGS;
  }

  @Override
  public Map<String, List<String>> getFullConfigurationIds(String deviceType, String serialNo,
      Date beforeTime) {
    // 对于上报的信息，增加serialNo的判断
    Map<String, List<String>> map = Maps.newHashMapWithExpectedSize(1);

    // 存储所有network_Policy
    List<String> networkPolicy = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (!StringUtils.isAllBlank(deviceType, serialNo)) {
      // 上报设备包含的主网络
      List<String> fpcNetworkIds = fpcNetworkService.queryNetworks(deviceType, serialNo).stream()
          .map(FpcNetworkBO::getFpcNetworkId).collect(Collectors.toList());
      // 上报设备包含的子网
      fpcNetworkIds.addAll(sensorLogicalSubnetDao.querySensorLogicalSubnets().stream()
          .filter(item -> fpcNetworkIds.contains(item.getNetworkInSensorIds()))
          .map(SensorLogicalSubnetDO::getId).collect(Collectors.toList()));

      fpcNetworkIds.forEach(item -> {
        networkPolicy.addAll(networkPolicyDao.queryNetworkPolicyByNetworkId(item).stream().map(
            networkPolicyDO -> networkPolicyDO.getNetworkId() + "_" + networkPolicyDO.getPolicyId())
            .collect(Collectors.toSet()));
      });
    }

    map.put(FpcCmsConstants.MQ_TAG_NETWORKPOLICY, networkPolicy);
    return map;
  }

  @Override
  public Tuple3<Boolean, List<Map<String, Object>>, Message> getFullConfigurations(
      String deviceType, String serialNumber, String tag) {
    // 所有下级设备均生效，无需判断serialNo
    if (StringUtils.equals(tag, FpcCmsConstants.MQ_TAG_NETWORKPOLICY)) {
      List<NetworkPolicyDO> networkPolicyList = networkPolicyDao.queryNetworkPolicys();

      List<Map<String, Object>> list = networkPolicyList.stream()
          .map(networkPolicy -> networkPolicy2MessageBody(networkPolicy,
              FpcCmsConstants.SYNC_ACTION_MODIFY))
          .collect(Collectors.toList());
      return Tuples.of(true, list, MQMessageHelper.EMPTY);
    } else {
      return Tuples.of(true, Lists.newArrayListWithCapacity(0), MQMessageHelper.EMPTY);
    }
  }

  private Map<String, Object> networkPolicy2MessageBody(NetworkPolicyDO networkPolicyDO,
      String action) {

    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    map.put("id", networkPolicyDO.getId());
    map.put("networkId", networkPolicyDO.getNetworkId());
    map.put("policyType", networkPolicyDO.getPolicyType());
    map.put("policyId", networkPolicyDO.getPolicyId());
    map.put("action", action);
    return map;
  }

}
