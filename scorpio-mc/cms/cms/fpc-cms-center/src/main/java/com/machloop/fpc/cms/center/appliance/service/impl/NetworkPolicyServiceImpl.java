package com.machloop.fpc.cms.center.appliance.service.impl;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.fpc.cms.center.appliance.bo.FilterRuleBO;
import com.machloop.fpc.cms.center.appliance.bo.IngestPolicyBO;
import com.machloop.fpc.cms.center.appliance.bo.NetworkPolicyBO;
import com.machloop.fpc.cms.center.appliance.dao.NetworkPolicyDao;
import com.machloop.fpc.cms.center.appliance.data.NetworkPolicyDO;
import com.machloop.fpc.cms.center.appliance.service.FilterRuleService;
import com.machloop.fpc.cms.center.appliance.service.IngestPolicyService;
import com.machloop.fpc.cms.center.appliance.service.NetworkPolicyService;
import com.machloop.fpc.cms.center.broker.bo.FpcNetworkBO;
import com.machloop.fpc.cms.center.broker.service.local.SyncConfigurationService;
import com.machloop.fpc.cms.center.broker.service.local.impl.MQReceiveServiceImpl;
import com.machloop.fpc.cms.center.broker.service.subordinate.FpcNetworkService;
import com.machloop.fpc.cms.center.broker.service.subordinate.MQAssignmentService;
import com.machloop.fpc.cms.center.helper.MQMessageHelper;
import com.machloop.fpc.cms.center.sensor.bo.SensorNetworkBO;
import com.machloop.fpc.cms.center.sensor.dao.SensorLogicalSubnetDao;
import com.machloop.fpc.cms.center.sensor.dao.SensorNetworkDao;
import com.machloop.fpc.cms.center.sensor.data.SensorLogicalSubnetDO;
import com.machloop.fpc.cms.center.sensor.data.SensorNetworkDO;
import com.machloop.fpc.cms.center.sensor.service.SensorNetworkService;
import com.machloop.fpc.cms.common.FpcCmsConstants;

import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;


/**
 * @author "Minjiajun"
 * <p>
 * create at 2021年12月1日, fpc-cms-center
 */
@Order(12)
@Service
public class NetworkPolicyServiceImpl
    implements NetworkPolicyService, MQAssignmentService, SyncConfigurationService {

  private static final List<String> TAGS = Lists.newArrayList(FpcCmsConstants.MQ_TAG_NETWORKPOLICY);

  @Autowired
  private ApplicationContext context;

  @Autowired
  private NetworkPolicyDao networkPolicyDao;

  @Autowired
  private IngestPolicyService ingestPolicyService;

  @Autowired
  private FilterRuleService filterRuleService;

  @Autowired
  private SensorNetworkDao sensorNetworkDao;

  @Autowired
  private SensorNetworkService sensorNetworkService;

  @Autowired
  private SensorLogicalSubnetDao sensorLogicalSubnetDao;

  @Autowired
  private FpcNetworkService fpcNetworkService;

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.NetworkPolicyService#queryNetworkPolicy(java.lang.String)
   */
  @Override
  public List<Map<String, String>> queryNetworkPolicys(String policyType) {
    List<SensorNetworkDO> sensorNetworks = sensorNetworkDao.querySensorNetworks();

    List<NetworkPolicyDO> networkPolicyList = networkPolicyDao
        .queryNetworkPolicyByPolicyType(policyType);
    Map<String, NetworkPolicyDO> networkPolicyMap = networkPolicyList.stream()
        .collect(Collectors.toMap(NetworkPolicyDO::getNetworkId, networkPolicy -> networkPolicy));

    Map<String, String> policyNameMap = Maps.newHashMapWithExpectedSize(networkPolicyList.size());
    if (StringUtils.equals(policyType, FpcCmsConstants.APPLIANCE_NETWORK_POLICY_INGEST)) {
      List<IngestPolicyBO> ingestPolicys = ingestPolicyService.queryIngestPolicys();
      ingestPolicys
          .forEach(ingestPolicy -> policyNameMap.put(ingestPolicy.getId(), ingestPolicy.getName()));
    } else {
      List<FilterRuleBO> filterRuleBOS = filterRuleService.queryFilterRule();
      filterRuleBOS
          .forEach(filterRuleBO -> policyNameMap.put(filterRuleBO.getId(), filterRuleBO.getName()));
    }

    List<Map<String, String>> result = Lists.newArrayListWithCapacity(sensorNetworks.size());
    for (SensorNetworkDO sensorNetwork : sensorNetworks) {
      Map<String, String> policy = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      NetworkPolicyDO networkPolicyDO = networkPolicyMap.get(sensorNetwork.getNetworkInSensorId());
      if (networkPolicyDO == null) {
        continue;
      }
      policy.put("networkId", sensorNetwork.getNetworkInSensorId());
      policy.put("networkName",
          StringUtils.isNotBlank(sensorNetwork.getName()) ? sensorNetwork.getName()
              : sensorNetwork.getNetworkInSensorName());
      policy.put("policyId", networkPolicyDO.getPolicyId());
      policy.put("policyName",
          MapUtils.getString(policyNameMap, networkPolicyDO.getPolicyId(), ""));
      result.add(policy);
    }
    return result;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.NetworkPolicyService#queryNetworkPolicy(java.lang.String)
   */
  @Override
  public NetworkPolicyBO queryNetworkPolicy(String id) {

    NetworkPolicyBO networkPolicyBO = new NetworkPolicyBO();
    NetworkPolicyDO networkPolicyDO = networkPolicyDao.queryNetworkPolicy(id);
    BeanUtils.copyProperties(networkPolicyDO, networkPolicyBO);

    return networkPolicyBO;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.NetworkPolicyService#updateNetworkPolicy(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public NetworkPolicyBO updateNetworkPolicy(String policyId, NetworkPolicyBO networkPolicyBO,
      String operatorId) {

    NetworkPolicyDO exist = networkPolicyDao.queryNetworkPolicyByNetworkId(
        networkPolicyBO.getNetworkId(), networkPolicyBO.getPolicyType());
    if (StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "策略不存在");
    }

    NetworkPolicyDO networkPolicyDO = new NetworkPolicyDO();
    networkPolicyBO.setPolicyId(policyId);
    BeanUtils.copyProperties(networkPolicyBO, networkPolicyDO);
    networkPolicyDO.setOperatorId(operatorId);

    networkPolicyDao.updateNetworkPolicy(networkPolicyDO);

    // 下发到直属fpc和cms
    List<Map<String, Object>> messageBodys = Lists.newArrayList(
        networkPolicy2MessageBody(networkPolicyDO, FpcCmsConstants.SYNC_ACTION_MODIFY));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_NETWORKPOLICY, null);

    return networkPolicyBO;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.NetworkPolicyService#saveNetworkPolicy(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public NetworkPolicyBO saveNetworkPolicy(NetworkPolicyBO networkPolicyBO, String operatorId) {
    NetworkPolicyDO exist = networkPolicyDao.queryNetworkPolicyByNetworkId(
        networkPolicyBO.getNetworkId(), networkPolicyBO.getPolicyType());
    if (StringUtils.isNotBlank(exist.getNetworkId())) {
      if (StringUtils.equals(exist.getPolicyType(),
          FpcCmsConstants.APPLIANCE_NETWORK_POLICY_INGEST)) {
        throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "该网络已配置捕获过滤规则");
      }
      if (StringUtils.equals(exist.getPolicyType(),
          FpcCmsConstants.APPLIANCE_NETWORK_POLICY_FILTER)) {
        throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "该网络已配置存储过滤规则");
      }
    }

    networkPolicyBO.setOperatorId(operatorId);
    NetworkPolicyDO networkPolicyDO = new NetworkPolicyDO();
    BeanUtils.copyProperties(networkPolicyBO, networkPolicyDO);
    NetworkPolicyDO result = networkPolicyDao.saveNetworkPolicy(networkPolicyDO);

    NetworkPolicyBO networkPolicy = new NetworkPolicyBO();
    BeanUtils.copyProperties(result, networkPolicy);

    // 下发到直属fpc和cms
    List<Map<String, Object>> messageBodys = Lists
        .newArrayList(networkPolicy2MessageBody(result, FpcCmsConstants.SYNC_ACTION_MODIFY));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_NETWORKPOLICY, null);

    return networkPolicy;
  }

  @Override
  public int saveNetworkPolicy(List<NetworkPolicyBO> networkPolicyBOList, String operatorId) {
    List<NetworkPolicyDO> networkPolicyDOList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    for (NetworkPolicyBO networkPolicyBO : networkPolicyBOList) {
      NetworkPolicyDO networkPolicyDO = new NetworkPolicyDO();
      BeanUtils.copyProperties(networkPolicyBO, networkPolicyDO);
      networkPolicyDOList.add(networkPolicyDO);
    }

    networkPolicyDOList = networkPolicyDao.saveNetworkPolicy(networkPolicyDOList, operatorId);

    List<Map<String, Object>> messageBodys = networkPolicyDOList.stream().map(networkPolicyDO -> {
      return networkPolicy2MessageBody(networkPolicyDO, FpcCmsConstants.SYNC_ACTION_ADD);
    }).collect(Collectors.toList());
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_NETWORKPOLICY, null);

    return networkPolicyDOList.size();
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.NetworkPolicyService#deleteNetworkPolicy(java.lang.String, java.lang.String)
   */
  @Override
  public NetworkPolicyBO deleteNetworkPolicy(String id, String operatorId) {

    NetworkPolicyDO exist = networkPolicyDao.queryNetworkPolicy(id);
    if (StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "策略不存在");
    }

    networkPolicyDao.deleteNetworkPolicy(id, operatorId);
    NetworkPolicyBO networkPolicyBO = new NetworkPolicyBO();
    BeanUtils.copyProperties(exist, networkPolicyBO);

    return networkPolicyBO;
  }

  @Override
  public int deleteNetworkPolicyByPriorId(String priorId, String priorType) {

    int count = networkPolicyDao.deleteNetworkPolicyByPolicyId(priorId, priorType);

    // 将删除命令下发
    NetworkPolicyDO networkPolicyDO = new NetworkPolicyDO();
    networkPolicyDO.setPolicyId(priorId);
    networkPolicyDO.setPolicyType(priorType);

    List<Map<String, Object>> messageBodys = Lists.newArrayList(
        networkPolicy2MessageBody(networkPolicyDO, FpcCmsConstants.SYNC_ACTION_DELETE));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_NETWORKPOLICY, null);

    return count;
  }

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

  /**
   * @see com.machloop.fpc.cms.center.broker.service.subordinate.MQAssignmentService#getFullConfigurations(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Tuple3<Boolean, List<Map<String, Object>>, Message> getFullConfigurations(
      String deviceType, String serialNo, String tag) {

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

  /********************************************************************************************************
   * 接收模块
   *******************************************************************************************************/

  @PostConstruct
  public void init() {
    MQReceiveServiceImpl.register(this, Lists.newArrayList(FpcCmsConstants.MQ_TAG_NETWORKPOLICY));
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

    int syncTotalCount = messages.stream().mapToInt(item -> syncNetworkPolicy(item)).sum();
    LOGGER.info("current sync networkPolicy total: {}.", syncTotalCount);

    return syncTotalCount;
  }

  private int syncNetworkPolicy(Map<String, Object> messageBody) {
    int syncCount = 0;

    String policyId = MapUtils.getString(messageBody, "policyId");
    if (StringUtils.isBlank(policyId)) {
      return syncCount;
    }
    String policyType = MapUtils.getString(messageBody, "policyType");
    String id = MapUtils.getString(messageBody, "id");
    String action = MapUtils.getString(messageBody, "action");
    String networkId = MapUtils.getString(messageBody, "networkId");

    NetworkPolicyBO networkPolicyBO = new NetworkPolicyBO();
    networkPolicyBO.setId(id);
    networkPolicyBO.setAssignId(id);
    networkPolicyBO.setPolicyId(policyId);
    networkPolicyBO.setPolicyType(policyType);
    networkPolicyBO.setNetworkId(networkId);

    // 判断下发的网络策略中的网络是否存在
    if (!StringUtils.equals(action, FpcCmsConstants.SYNC_ACTION_DELETE)) {
      List<String> vaildNetworkIds = sensorNetworkService.querySensorNetworks().stream()
          .map(SensorNetworkBO::getNetworkInSensorId).collect(Collectors.toList());
      List<String> networkIds = CsvUtils.convertCSVToList(networkId);
      Iterator<String> iterator = networkIds.iterator();
      while (iterator.hasNext()) {
        if (!vaildNetworkIds.contains(iterator.next())) {
          iterator.remove();
        }
      }
      if (networkIds.isEmpty()) {
        // 不存在网络策略中的网络
        return syncCount;
      }
    }

    // 捕获过滤更新之前，暂时使用
    NetworkPolicyDO exist = new NetworkPolicyDO();
    if (StringUtils.equals(policyType, FpcCmsConstants.APPLIANCE_NETWORK_POLICY_STORAGE)) {
      exist = networkPolicyDao.queryNetworkPolicyByPolicyId(id);
    } else {
      exist = networkPolicyDao.queryNetworkPolicyByNetworkId(networkId, policyType);
    }

    int addCount = 0;
    int modifyCount = 0;
    int deleteCount = 0;
    try {
      switch (action) {
        case FpcCmsConstants.SYNC_ACTION_ADD:
        case FpcCmsConstants.SYNC_ACTION_MODIFY:
          if (StringUtils.isBlank(exist.getId())) {
            saveNetworkPolicy(Lists.newArrayList(networkPolicyBO), CMS_ASSIGNMENT);
            addCount++;
          } else {
            // TODO 捕获过滤临时操作
            if (StringUtils.equals(policyType, FpcCmsConstants.APPLIANCE_NETWORK_POLICY_STORAGE)) {
              networkPolicyDao.updateNetworkPolicyByPolicyId(id, networkId, policyId,
                  CMS_ASSIGNMENT);
            } else {
              networkPolicyDao.updateNetworkPolicy(networkId, policyId, policyType, CMS_ASSIGNMENT);
            }

            modifyCount++;
          }
          break;
        case FpcCmsConstants.SYNC_ACTION_DELETE:
          deleteNetworkPolicy(policyId, policyType);
          deleteCount++;
          break;
        default:
          break;
      }
      // 本次同步数据量
      syncCount = addCount + modifyCount + deleteCount;

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug(
            "current sync networkPolicy status: [addCount: {}, modifyCount: {}, deleteCount: {}]",
            addCount, modifyCount, deleteCount);
      }
    } catch (BusinessException e) {
      LOGGER.warn("sync faild. error msg: {}", e.getMessage());
      return -1;
    }

    return syncCount;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.local.SyncConfigurationService#clearLocalConfiguration(java.lang.String, java.util.Date)
   */
  @Override
  public int clearLocalConfiguration(String tag, boolean onlyLocal, Date beforeTime) {
    // TODO 不能删除
    return 0;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.local.SyncConfigurationService#getAssignConfigurationIds(java.lang.String, java.util.Date)
   */
  @Override
  public List<String> getAssignConfigurationIds(String tag, Date beforeTime) {
    return networkPolicyDao.queryAssignNetworkPolicyIds(beforeTime);
  }
}
