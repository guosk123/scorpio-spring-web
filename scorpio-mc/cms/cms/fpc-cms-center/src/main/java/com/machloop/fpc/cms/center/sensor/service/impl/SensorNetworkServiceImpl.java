package com.machloop.fpc.cms.center.sensor.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
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
import com.machloop.fpc.cms.center.appliance.bo.MetricSettingBO;
import com.machloop.fpc.cms.center.appliance.dao.NetworkPolicyDao;
import com.machloop.fpc.cms.center.appliance.dao.NetworkTopologyDao;
import com.machloop.fpc.cms.center.appliance.dao.ServiceNetworkDao;
import com.machloop.fpc.cms.center.appliance.data.NetworkPolicyDO;
import com.machloop.fpc.cms.center.appliance.data.NetworkTopologyDO;
import com.machloop.fpc.cms.center.appliance.service.AlertRuleService;
import com.machloop.fpc.cms.center.appliance.service.BaselineService;
import com.machloop.fpc.cms.center.appliance.service.MetricSettingService;
import com.machloop.fpc.cms.center.broker.bo.FpcNetworkBO;
import com.machloop.fpc.cms.center.broker.service.subordinate.FpcNetworkService;
import com.machloop.fpc.cms.center.broker.service.subordinate.MQAssignmentService;
import com.machloop.fpc.cms.center.central.bo.CentralDeviceBO;
import com.machloop.fpc.cms.center.central.bo.FpcBO;
import com.machloop.fpc.cms.center.central.service.ClusterService;
import com.machloop.fpc.cms.center.central.service.FpcService;
import com.machloop.fpc.cms.center.helper.MQMessageHelper;
import com.machloop.fpc.cms.center.sensor.bo.SensorNetworkBO;
import com.machloop.fpc.cms.center.sensor.dao.SensorLogicalSubnetDao;
import com.machloop.fpc.cms.center.sensor.dao.SensorNetworkDao;
import com.machloop.fpc.cms.center.sensor.dao.SensorNetworkGroupDao;
import com.machloop.fpc.cms.center.sensor.data.SensorLogicalSubnetDO;
import com.machloop.fpc.cms.center.sensor.data.SensorNetworkDO;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkPermService;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkService;
import com.machloop.fpc.cms.common.FpcCmsConstants;

import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * @author "Minjiajun"
 * <p>
 * create at 2021年10月22日, fpc-cms-center
 */
@Transactional
@Order(13)
@Service
public class SensorNetworkServiceImpl implements SensorNetworkService, MQAssignmentService {

  private static final List<String> TAGS = Lists.newArrayList(FpcCmsConstants.MQ_TAG_NETWORKPOLICY);

  @Autowired
  private ApplicationContext context;

  @Autowired
  private SensorNetworkDao sensorNetworkDao;

  @Autowired
  private SensorNetworkGroupDao sensorNetworkGroupDao;

  @Autowired
  private SensorLogicalSubnetDao sensorLogicalSubnetDao;

  @Autowired
  private NetworkTopologyDao networkTopologyDao;

  @Autowired
  private MetricSettingService metricSettingService;

  @Autowired
  private FpcService fpcService;

  @Autowired
  private FpcNetworkService fpcNetworkService;

  @Autowired
  private ServiceNetworkDao serviceNetworkDao;

  @Autowired
  private AlertRuleService alertRuleService;

  @Autowired
  private BaselineService baselineService;

  @Autowired
  private SensorNetworkPermService sensorNetworkPermService;

  @Autowired
  private ClusterService clusterService;

  @Autowired
  private NetworkPolicyDao networkPolicyDao;

  /**
   * @see com.machloop.fpc.cms.center.sensor.service.SensorNetworkService#querySensorNetworks(com.machloop.fpc.cms.center.sensor.vo.SensorNetworkQueryVO, java.lang.String, java.lang.String)
   */
  @Override
  public List<SensorNetworkBO> querySensorNetworks() {
    List<SensorNetworkDO> sensorNetworkDOList = sensorNetworkDao.querySensorNetworks();
    if (sensorNetworkDOList.size() == 0) {
      return Lists.newArrayListWithCapacity(0);
    }

    // 获取连接异常的探针节点
    Map<String, String> abnormalNodes = clusterService.queryAbnormalNodesAndRefresh();

    return sensorNetworkDOList.stream().map(sensorNetworkDO -> {
      SensorNetworkBO sensorNetworkBO = new SensorNetworkBO();
      BeanUtils.copyProperties(sensorNetworkDO, sensorNetworkBO);
      sensorNetworkBO.setName(StringUtils.defaultIfBlank(sensorNetworkDO.getName(),
          sensorNetworkDO.getNetworkInSensorName()));
      String status = FpcCmsConstants.CONNECT_STATUS_NORMAL;
      String detail = "";
      if (abnormalNodes.containsKey(sensorNetworkDO.getSensorId())) {
        status = FpcCmsConstants.CONNECT_STATUS_ABNORMAL;
        detail = String.format("探针设备[%s]异常或未连接，请检查设备数据节点状态",
            abnormalNodes.get(sensorNetworkDO.getSensorId()));
      }
      sensorNetworkBO.setStatus(status);
      sensorNetworkBO.setStatusDetail(detail);

      String networkInSensorId = sensorNetworkDO.getNetworkInSensorId();
      List<NetworkPolicyDO> networkPolicyDOList = networkPolicyDao
          .queryNetworkPolicyByNetworkIdAndPolicySource(networkInSensorId,
              FpcCmsConstants.APPLIANCE_NETWORK_POLICY_SEND, Constants.BOOL_NO);
      List<String> sendPolicyIds = networkPolicyDOList.stream().map(NetworkPolicyDO::getPolicyId)
          .collect(Collectors.toList());
      sensorNetworkBO.setSendPolicyIds(CsvUtils.convertCollectionToCSV(sendPolicyIds));

      return sensorNetworkBO;
    }).collect(Collectors.toList());
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.service.SensorNetworkService#querySensorNetwork(java.lang.String)
   */
  @Override
  public SensorNetworkBO querySensorNetwork(String id) {

    SensorNetworkDO sensorNetworkDO = sensorNetworkDao.querySensorNetwork(id);
    SensorNetworkBO sensorNetworkBO = new SensorNetworkBO();
    sensorNetworkDO.setName(
        StringUtils.isBlank(sensorNetworkDO.getName()) ? sensorNetworkDO.getNetworkInSensorName()
            : sensorNetworkDO.getName());
    BeanUtils.copyProperties(sensorNetworkDO, sensorNetworkBO);

    String networkInSensorId = sensorNetworkDO.getNetworkInSensorId();
    List<NetworkPolicyDO> networkPolicyDOList = networkPolicyDao
        .queryNetworkPolicyByNetworkIdAndPolicySource(networkInSensorId,
            FpcCmsConstants.APPLIANCE_NETWORK_POLICY_SEND, Constants.BOOL_NO);

    List<String> sendPolicyIds = networkPolicyDOList.stream().map(NetworkPolicyDO::getPolicyId)
        .collect(Collectors.toList());
    sensorNetworkBO.setSendPolicyIds(CsvUtils.convertCollectionToCSV(sendPolicyIds));

    return sensorNetworkBO;
  }

  @Override
  public List<SensorNetworkBO> getNetworksInSensorList() {
    List<SensorNetworkBO> npmSensorList = Lists
        .newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    List<SensorNetworkBO> result = Lists.newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    CentralDeviceBO sensorTree = fpcService.queryCentralDevices();
    getNpmList(npmSensorList, sensorTree);
    for (SensorNetworkBO npmSensor : npmSensorList) {
      List<FpcNetworkBO> networkInSensorList = fpcNetworkService
          .queryNetworks(FpcCmsConstants.DEVICE_TYPE_TFA, npmSensor.getSensorId());
      for (FpcNetworkBO networkInSensor : networkInSensorList) {
        SensorNetworkBO sensorNetworkBO = new SensorNetworkBO();
        BeanUtils.copyProperties(npmSensor, sensorNetworkBO);
        sensorNetworkBO.setNetworkInSensorId(networkInSensor.getFpcNetworkId());
        sensorNetworkBO.setNetworkInSensorName(networkInSensor.getFpcNetworkName());
        result.add(sensorNetworkBO);
      }
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.service.SensorNetworkService#saveSensorNetwork(com.machloop.fpc.cms.center.sensor.bo.SensorNetworkBO, java.lang.String)
   */
  @Override
  public SensorNetworkBO saveSensorNetwork(SensorNetworkBO sensorNetworkBO, String operatorId) {
    SensorNetworkDO exist = sensorNetworkDao
        .querySensorNetworkByNetworkInSensorId(sensorNetworkBO.getNetworkInSensorId());
    if (StringUtils.isNotBlank(exist.getNetworkInSensorId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "网络已存在");
    }
    SensorNetworkDO sensorNetworkDO = new SensorNetworkDO();
    BeanUtils.copyProperties(sensorNetworkBO, sensorNetworkDO);
    sensorNetworkDO.setOperatorId(operatorId);
    sensorNetworkDO = sensorNetworkDao.saveSensorNetwork(sensorNetworkDO);

    List<NetworkPolicyDO> policyList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<String> sendPolicies = CsvUtils.convertCSVToList(sensorNetworkBO.getSendPolicyIds());
    if (CollectionUtils.isNotEmpty(sendPolicies)) {
      sendPolicies.forEach(sendPolicy -> {
        NetworkPolicyDO networkPolicyDO = new NetworkPolicyDO();
        networkPolicyDO.setNetworkId(sensorNetworkBO.getNetworkInSensorId());
        networkPolicyDO.setPolicyId(sendPolicy);
        networkPolicyDO.setPolicyType(FpcCmsConstants.APPLIANCE_NETWORK_POLICY_SEND);
        networkPolicyDO.setOperatorId(operatorId);
        networkPolicyDO.setPolicySource(Constants.BOOL_NO);
        policyList.add(networkPolicyDO);
      });
    }
    if (CollectionUtils.isNotEmpty(policyList)) {
      networkPolicyDao.mergeNetworkPolicys(policyList);
    }
    policyList.addAll(networkPolicyDao.queryNetworkPolicyByNetworkIdAndPolicySourceOfGroup(
        sensorNetworkBO.getNetworkInSensorId(), FpcCmsConstants.APPLIANCE_NETWORK_POLICY_SEND));
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

    // 配置网络默认的统计度量值
    MetricSettingBO metricSettingBO = new MetricSettingBO();
    metricSettingBO.setSourceType(FpcCmsConstants.SOURCE_TYPE_NETWORK);
    metricSettingBO.setNetworkId(sensorNetworkDO.getNetworkInSensorId());
    metricSettingService.saveDefaultMetricSettings(Lists.newArrayList(metricSettingBO), operatorId);

    sensorNetworkBO.setId(sensorNetworkDO.getId());
    return sensorNetworkBO;
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.service.SensorNetworkService#batchSaveSensorNetworks(java.util.List)
   */
  @Override
  public List<SensorNetworkBO> batchSaveSensorNetworks(List<SensorNetworkBO> sensorNetworkList,
      String operatorId) {
    List<SensorNetworkDO> existList = sensorNetworkDao.querySensorNetworks();
    List<String> existNetworkIdList = existList.stream().map(e -> e.getNetworkInSensorId())
        .collect(Collectors.toList());

    List<SensorNetworkBO> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<SensorNetworkBO> rawSensorNetworks = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    for (SensorNetworkBO sensorNetworkBO : sensorNetworkList) {
      if (!existNetworkIdList.contains(sensorNetworkBO.getNetworkInSensorId())) {
        rawSensorNetworks.add(sensorNetworkBO);
      }
    }

    // 当npmd的状态为在线时，才将其下的网络保存进数据库
    Map<String, String> fpcSerialNumStatusMap = fpcService.queryAllFpc().stream()
        .collect(Collectors.toMap(FpcBO::getSerialNumber, FpcBO::getConnectStatus));
    for (SensorNetworkBO sensorNetworkBO : rawSensorNetworks) {
      if (fpcSerialNumStatusMap.keySet().contains(sensorNetworkBO.getSensorId())
          && StringUtils.equals(fpcSerialNumStatusMap.get(sensorNetworkBO.getSensorId()),
              FpcCmsConstants.CONNECT_STATUS_NORMAL)) {
        result.add(sensorNetworkBO);
      }
    }

    List<SensorNetworkDO> sensorNetworkDOList = Lists.newArrayListWithCapacity(result.size());
    for (SensorNetworkBO sensorNetworkBO : result) {

      SensorNetworkDO sensorNetworkDO = new SensorNetworkDO();
      BeanUtils.copyProperties(sensorNetworkBO, sensorNetworkDO);

      sensorNetworkDO.setOperatorId(operatorId);
      sensorNetworkDOList.add(sensorNetworkDO);

      // 配置网络默认的统计度量值
      MetricSettingBO metricSettingBO = new MetricSettingBO();
      metricSettingBO.setSourceType(FpcCmsConstants.SOURCE_TYPE_NETWORK);
      metricSettingBO.setNetworkId(sensorNetworkDO.getNetworkInSensorId());
      metricSettingService.saveDefaultMetricSettings(Lists.newArrayList(metricSettingBO),
          operatorId);
    }

    sensorNetworkDao.batchSaveSensorNetworks(sensorNetworkDOList);

    return querySensorNetworks();
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.service.SensorNetworkService#updateSensorNetworks(java.lang.String)
   */
  @Override
  public SensorNetworkBO updateSensorNetwork(String id, SensorNetworkBO sensorNetworkBO,
      String operatorId) {

    SensorNetworkDO sensorNetworkDO = sensorNetworkDao.querySensorNetwork(id);
    if (StringUtils.isBlank(sensorNetworkDO.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "网络不存在");
    }
    // 探针网络可能会修改网络ID，需要将之前的删除的策略下发给下属的探针
    List<NetworkPolicyDO> existNetworkPolicyDOListOfSensorNetwork = networkPolicyDao
        .queryNetworkPolicyByNetworkIdAndPolicySource(sensorNetworkDO.getNetworkInSensorId(),
            FpcCmsConstants.APPLIANCE_NETWORK_POLICY_SEND, Constants.BOOL_NO);
    List<NetworkPolicyDO> existNetworkPolicyDOListOfSensorNetworkGroup = networkPolicyDao
        .queryNetworkPolicyByNetworkIdAndPolicySourceOfGroup(sensorNetworkDO.getNetworkInSensorId(),
            FpcCmsConstants.APPLIANCE_NETWORK_POLICY_SEND);
    List<Map<String, Object>> existMessageBody;
    if (CollectionUtils.isNotEmpty(existNetworkPolicyDOListOfSensorNetworkGroup)) {
      existMessageBody = existNetworkPolicyDOListOfSensorNetworkGroup.stream()
          .map(policy -> networkPolicy2MessageBody(policy, FpcCmsConstants.SYNC_ACTION_MODIFY))
          .collect(Collectors.toList());
      Map<String, Object> messageBody = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      messageBody.put("batch", true);
      messageBody.put("data", existMessageBody);
      Message message = MQMessageHelper.convertToMessage(messageBody,
          FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT, FpcCmsConstants.MQ_TAG_NETWORKPOLICY, null);
      assignmentConfiguration(message, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT);
    } else {
      if (CollectionUtils.isNotEmpty(existNetworkPolicyDOListOfSensorNetwork)) {
        existMessageBody = existNetworkPolicyDOListOfSensorNetwork.stream()
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

    // 删除之前探针网络配置的网络策略
    networkPolicyDao.deleteNetworkPolicyByNetworkIdAndPolicySource(
        sensorNetworkDO.getNetworkInSensorId(), Constants.BOOL_NO);
    BeanUtils.copyProperties(sensorNetworkBO, sensorNetworkDO);
    sensorNetworkDao.updateSensorNetwork(id, sensorNetworkDO, operatorId);

    List<NetworkPolicyDO> policyList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<String> sendPolicies = CsvUtils.convertCSVToList(sensorNetworkBO.getSendPolicyIds());
    if (CollectionUtils.isNotEmpty(sendPolicies)) {
      sendPolicies.forEach(sendPolicy -> {
        NetworkPolicyDO networkPolicyDO = new NetworkPolicyDO();
        networkPolicyDO.setNetworkId(sensorNetworkBO.getNetworkInSensorId());
        networkPolicyDO.setPolicyId(sendPolicy);
        networkPolicyDO.setPolicyType(FpcCmsConstants.APPLIANCE_NETWORK_POLICY_SEND);
        networkPolicyDO.setOperatorId(operatorId);
        networkPolicyDO.setPolicySource(Constants.BOOL_NO);
        policyList.add(networkPolicyDO);
      });
    }
    if (CollectionUtils.isNotEmpty(policyList)) {
      networkPolicyDao.mergeNetworkPolicys(policyList);
    }
    policyList.addAll(networkPolicyDao.queryNetworkPolicyByNetworkIdAndPolicySourceOfGroup(
        sensorNetworkBO.getNetworkInSensorId(), FpcCmsConstants.APPLIANCE_NETWORK_POLICY_SEND));
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

    return querySensorNetwork(id);
  }

  /**
   * @see com.machloop.fpc.cms.center.sensor.service.SensorNetworkService#deleteSensorNetworks(java.lang.String, java.lang.String)
   */
  @Override
  public SensorNetworkBO deleteSensorNetwork(String id, String operatorId) {
    SensorNetworkDO sensorNetworkDO = sensorNetworkDao.querySensorNetwork(id);
    if (StringUtils.isBlank(sensorNetworkDO.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "网络不存在");
    }

    // 删除网络时查看该网络是否配置在某个业务下，如果有则不能删除
    if (CollectionUtils.isNotEmpty(
        serviceNetworkDao.queryServiceNetworks(null, sensorNetworkDO.getNetworkInSensorId()))) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "该网络存在相关联的业务，无法删除");
    }

    // 是否有告警作用于该网络
    if (alertRuleService.queryAlertRulesBySource(FpcCmsConstants.SOURCE_TYPE_NETWORK,
        sensorNetworkDO.getNetworkInSensorId(), null).size() > 0) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "该网络已作用于告警，无法删除");
    }

    // 删除网络时查看该网络是否配置在某个网络组下，如果有则不能删除
    List<String> existNetworkIdsInGroup = sensorNetworkGroupDao.querySensorNetworkGroups().stream()
        .map(e -> e.getNetworkInSensorIds()).collect(Collectors.toList());
    List<String> existNetworkIdListInGroup = Lists
        .newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    for (String networkId : existNetworkIdsInGroup) {
      existNetworkIdListInGroup.addAll(CsvUtils.convertCSVToList(networkId));
    }
    if (existNetworkIdListInGroup.contains(sensorNetworkDO.getNetworkInSensorId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "该网络存在相关联的网络组，无法删除");
    }

    // 删除网络时查看该网络是否包含子网，如果有则不能删除
    List<String> existNetworkIdsInSubnet = sensorLogicalSubnetDao.querySensorLogicalSubnets()
        .stream().map(e -> e.getNetworkInSensorIds()).collect(Collectors.toList());
    List<String> existNetworkIdInSubnet = Lists
        .newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    for (String networkId : existNetworkIdsInSubnet) {
      existNetworkIdInSubnet.addAll(CsvUtils.convertCSVToList(networkId));
    }
    if (existNetworkIdInSubnet.contains(sensorNetworkDO.getNetworkInSensorId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "该网络下包含子网络，无法删除");
    }

    // 该网络是否已配置到网络拓扑中
    NetworkTopologyDO networkTopologyDO = networkTopologyDao.queryNetworkTopologyByNetworkId(id);
    if (StringUtils.isNotBlank(networkTopologyDO.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "该网络已配置到网络拓扑中，无法删除");
    }

    // 删除网络权限
    sensorNetworkPermService
        .deleteSensorNetworkPermByNetwork(sensorNetworkDO.getNetworkInSensorId());

    // 删除网络下基线定义
    baselineService.deleteBaselineSettings(FpcCmsConstants.SOURCE_TYPE_NETWORK,
        sensorNetworkDO.getNetworkInSensorId(), null, null);

    // 删除网络下的统计配置
    metricSettingService.deleteMetricSetting(FpcCmsConstants.SOURCE_TYPE_NETWORK,
        sensorNetworkDO.getNetworkInSensorId(), null, null);

    String networkInSensorId = sensorNetworkDO.getNetworkInSensorId();
    List<NetworkPolicyDO> networkPolicyDOListOfGroup = networkPolicyDao
        .queryNetworkPolicyByNetworkIdAndPolicySourceOfGroup(networkInSensorId,
            FpcCmsConstants.APPLIANCE_NETWORK_POLICY_SEND);
    List<NetworkPolicyDO> networkPolicyDOList = networkPolicyDao
        .queryNetworkPolicyByNetworkIdAndPolicySource(networkInSensorId,
            FpcCmsConstants.APPLIANCE_NETWORK_POLICY_SEND, Constants.BOOL_NO);
    if (CollectionUtils.isEmpty(networkPolicyDOListOfGroup)) {
      if (CollectionUtils.isNotEmpty(networkPolicyDOList)) {
        List<Map<String, Object>> messageBodys = networkPolicyDOList.stream()
            .map(policy -> networkPolicy2MessageBody(policy, FpcCmsConstants.SYNC_ACTION_DELETE))
            .collect(Collectors.toList());
        Map<String,
            Object> messageBody = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
        messageBody.put("batch", true);
        messageBody.put("data", messageBodys);
        Message message = MQMessageHelper.convertToMessage(messageBody,
            FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT, FpcCmsConstants.MQ_TAG_NETWORKPOLICY, null);
        assignmentConfiguration(message, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT);
      }
    } else {
      List<Map<String, Object>> messageBodys = networkPolicyDOListOfGroup.stream()
          .map(policy -> networkPolicy2MessageBody(policy, FpcCmsConstants.SYNC_ACTION_MODIFY))
          .collect(Collectors.toList());
      Map<String, Object> messageBody = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      messageBody.put("batch", true);
      messageBody.put("data", messageBodys);
      Message message = MQMessageHelper.convertToMessage(messageBody,
          FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT, FpcCmsConstants.MQ_TAG_NETWORKPOLICY, null);
      assignmentConfiguration(message, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT);
    }

    networkPolicyDao.deleteNetworkPolicyByNetworkIdAndPolicySource(
        sensorNetworkDO.getNetworkInSensorId(), Constants.BOOL_NO);

    // 删除网络
    sensorNetworkDao.deleteSensorNetwork(id, operatorId);
    SensorNetworkBO sensorNetworkBO = new SensorNetworkBO();
    BeanUtils.copyProperties(sensorNetworkDO, sensorNetworkBO);
    return sensorNetworkBO;
  }

  public void deleteSensorNetworkByFpcNetworkId(String networkId, String operatorId) {
    // 删除网络权限
    sensorNetworkPermService.deleteSensorNetworkPermByNetwork(networkId);

    // 删除网络下基线定义
    baselineService.deleteBaselineSettings(FpcCmsConstants.SOURCE_TYPE_NETWORK, networkId, null,
        null);

    // 删除网络下的统计配置
    metricSettingService.deleteMetricSetting(FpcCmsConstants.SOURCE_TYPE_NETWORK, networkId, null,
        null);

    networkPolicyDao.deleteNetworkPolicyByNetworkIdAndPolicySource(networkId, Constants.BOOL_NO);
    // 删除网络
    sensorNetworkDao.deleteSensorNetworkByFpcNetworkId(networkId, operatorId);
  }

  private void getNpmList(List<SensorNetworkBO> sensorNetworkBOlist, CentralDeviceBO sensorTree) {
    SensorNetworkBO sensorNetworkBO = new SensorNetworkBO();
    if (sensorTree.getChild() == null) {
      sensorNetworkBO.setSensorId(sensorTree.getDeviceSerialNumber());
      sensorNetworkBO.setSensorName(sensorTree.getDeviceName());
      sensorNetworkBO.setSensorType(sensorTree.getSensorType());
      sensorNetworkBO.setOwner(sensorTree.getOwner());
      sensorNetworkBOlist.add(sensorNetworkBO);
      return;
    }

    for (int i = 0; i < sensorTree.getChild().size(); i++) {
      CentralDeviceBO child = sensorTree.getChild().get(i);
      getNpmList(sensorNetworkBOlist, child);
    }
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
