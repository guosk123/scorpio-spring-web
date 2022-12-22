package com.machloop.fpc.cms.center.appliance.service.impl;

import java.util.*;
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
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.fpc.cms.center.appliance.bo.SendPolicyBO;
import com.machloop.fpc.cms.center.appliance.dao.ExternalReceiverDao;
import com.machloop.fpc.cms.center.appliance.dao.NetworkPolicyDao;
import com.machloop.fpc.cms.center.appliance.dao.SendPolicyDao;
import com.machloop.fpc.cms.center.appliance.dao.SendRuleDao;
import com.machloop.fpc.cms.center.appliance.data.ExternalReceiverDO;
import com.machloop.fpc.cms.center.appliance.data.NetworkPolicyDO;
import com.machloop.fpc.cms.center.appliance.data.SendPolicyDO;
import com.machloop.fpc.cms.center.appliance.data.SendRuleDO;
import com.machloop.fpc.cms.center.appliance.service.SendPolicyService;
import com.machloop.fpc.cms.center.broker.service.local.SyncConfigurationService;
import com.machloop.fpc.cms.center.broker.service.local.impl.MQReceiveServiceImpl;
import com.machloop.fpc.cms.center.broker.service.subordinate.MQAssignmentService;
import com.machloop.fpc.cms.center.helper.MQMessageHelper;
import com.machloop.fpc.cms.center.sensor.dao.SensorNetworkGroupDao;
import com.machloop.fpc.cms.center.sensor.data.SensorNetworkGroupDO;
import com.machloop.fpc.cms.common.FpcCmsConstants;

import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * @author ChenXiao
 * create at 2022/9/22
 */
@Order(11)
@Service
public class SendPolicyServiceImpl
    implements SendPolicyService, MQAssignmentService, SyncConfigurationService {

  private static final List<String> TAGS = Lists.newArrayList(FpcCmsConstants.MQ_TAG_SENDPOLICY);
  @Autowired
  private SendPolicyDao sendPolicyDao;

  @Autowired
  private NetworkPolicyDao networkPolicyDao;

  @Autowired
  private SendRuleDao sendRuleDao;

  @Autowired
  private ExternalReceiverDao externalReceiverDao;

  @Autowired
  private SensorNetworkGroupDao sensorNetworkGroupDao;

  @Autowired
  private ApplicationContext context;

  @Override
  public List<Map<String, Object>> querySendPolicies() {

    List<SendPolicyDO> sendPolicyDOList = sendPolicyDao.querySendPolicies();
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    List<SensorNetworkGroupDO> sensorNetworkGroupDOList = sensorNetworkGroupDao
        .querySensorNetworkGroups();
    List<List<String>> list = new ArrayList<>();
    sensorNetworkGroupDOList.forEach(sensorNetworkGroup -> {
      List<String> sensorNetworkIdList = CsvUtils
          .convertCSVToList(sensorNetworkGroup.getNetworkInSensorIds());
      String networkId = sensorNetworkIdList.get(0);
      List<NetworkPolicyDO> networkPolicyDOS = networkPolicyDao
          .queryNetworkPolicyByNetworkIdAndPolicySourceOfGroup(networkId,
              FpcCmsConstants.APPLIANCE_NETWORK_POLICY_SEND);
      List<String> sendPolicyIds = networkPolicyDOS.stream().map(NetworkPolicyDO::getPolicyId)
          .collect(Collectors.toList());
      String sensorNetworkGroupId = "+" + sensorNetworkGroup.getId();
      sendPolicyIds.add(sensorNetworkGroupId);
      list.add(sendPolicyIds);
    });

    sendPolicyDOList.forEach(sendPolicyDO -> {
      Map<String, Object> temp = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      temp.put("id", sendPolicyDO.getId());
      temp.put("name", sendPolicyDO.getName());
      temp.put("externalReceiverId", sendPolicyDO.getExternalReceiverId());
      temp.put("sendRuleId", sendPolicyDO.getSendRuleId());
      temp.put("state", sendPolicyDO.getState());

      List<NetworkPolicyDO> networkPolicyDOList = networkPolicyDao.queryNetworkPolicyByPolicyId(
          sendPolicyDO.getId(), FpcCmsConstants.APPLIANCE_NETWORK_POLICY_SEND);

      List<
          NetworkPolicyDO> networkPolicyListOfSingleNetwork = networkPolicyDOList
              .stream().filter(networkPolicyDO -> StringUtils
                  .equals(networkPolicyDO.getPolicySource(), Constants.BOOL_NO))
              .collect(Collectors.toList());
      List<String> networkIdListOfSingleNetwork = networkPolicyListOfSingleNetwork.stream()
          .map(NetworkPolicyDO::getNetworkId).collect(Collectors.toList());
      List<String> networkGroupIdList = list.stream().filter(x -> x.contains(sendPolicyDO.getId()))
          .map(y -> y.get(y.size() - 1).substring(1)).collect(Collectors.toList());
      networkIdListOfSingleNetwork.addAll(networkGroupIdList);
      temp.put("quote", networkIdListOfSingleNetwork);
      result.add(temp);
    });

    return result;
  }

  @Override
  public Map<String, Object> querySendPolicy(String id) {

    SendPolicyDO sendPolicyDO = sendPolicyDao.querySendPolicy(id);

    List<SensorNetworkGroupDO> sensorNetworkGroupDOList = sensorNetworkGroupDao
        .querySensorNetworkGroups();
    List<List<String>> list = new ArrayList<>();
    sensorNetworkGroupDOList.forEach(sensorNetworkGroup -> {
      List<String> sensorNetworkIdList = CsvUtils
          .convertCSVToList(sensorNetworkGroup.getNetworkInSensorIds());
      String networkId = sensorNetworkIdList.get(0);
      List<NetworkPolicyDO> networkPolicyDOS = networkPolicyDao
          .queryNetworkPolicyByNetworkIdAndPolicySourceOfGroup(networkId,
              FpcCmsConstants.APPLIANCE_NETWORK_POLICY_SEND);
      List<String> sendPolicyIds = networkPolicyDOS.stream().map(NetworkPolicyDO::getPolicyId)
          .collect(Collectors.toList());
      String sensorNetworkGroupId = "+" + sensorNetworkGroup.getId();
      sendPolicyIds.add(sensorNetworkGroupId);
      list.add(sendPolicyIds);
    });

    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    result.put("id", sendPolicyDO.getId());
    result.put("name", sendPolicyDO.getName());
    result.put("externalReceiverId", sendPolicyDO.getExternalReceiverId());
    result.put("sendRuleId", sendPolicyDO.getSendRuleId());
    result.put("state", sendPolicyDO.getState());

    List<NetworkPolicyDO> networkPolicyDOList = networkPolicyDao.queryNetworkPolicyByPolicyId(
        sendPolicyDO.getId(), FpcCmsConstants.APPLIANCE_NETWORK_POLICY_SEND);

    List<NetworkPolicyDO> networkPolicyListOfSingleNetwork = networkPolicyDOList.stream().filter(
        networkPolicyDO -> StringUtils.equals(networkPolicyDO.getPolicySource(), Constants.BOOL_NO))
        .collect(Collectors.toList());
    List<String> networkIdListOfSingleNetwork = networkPolicyListOfSingleNetwork.stream()
        .map(NetworkPolicyDO::getNetworkId).collect(Collectors.toList());
    List<String> networkGroupIdList = list.stream().filter(x -> x.contains(sendPolicyDO.getId()))
        .map(y -> y.get(y.size() - 1).substring(1)).collect(Collectors.toList());
    networkIdListOfSingleNetwork.addAll(networkGroupIdList);
    result.put("quote", networkIdListOfSingleNetwork);

    return result;
  }

  @Override
  public List<Map<String, Object>> querySendPoliciesStateOn() {

    List<SendPolicyDO> sendPolicyDOList = sendPolicyDao.querySendPoliciesStateOn();
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    sendPolicyDOList.forEach(sendPolicyDO -> {
      Map<String, Object> temp = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      temp.put("id", sendPolicyDO.getId());
      temp.put("name", sendPolicyDO.getName());
      temp.put("externalReceiverId", sendPolicyDO.getExternalReceiverId());
      temp.put("sendRuleId", sendPolicyDO.getSendRuleId());
      temp.put("state", sendPolicyDO.getState());
      List<NetworkPolicyDO> networkPolicyDOList = networkPolicyDao.queryNetworkPolicyByPolicyId(
          sendPolicyDO.getId(), FpcCmsConstants.APPLIANCE_NETWORK_POLICY_SEND);
      Set<String> networkIdList = networkPolicyDOList.stream().map(NetworkPolicyDO::getNetworkId)
          .collect(Collectors.toSet());
      List<String> quoteList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      quoteList.addAll(networkIdList);
      temp.put("quote", CsvUtils.convertCollectionToCSV(quoteList));
      result.add(temp);
    });

    return result;
  }

  @Override
  public SendPolicyBO saveSendPolicy(SendPolicyBO sendPolicyBO, String operatorId) {

    SendPolicyDO exist = sendPolicyDao.querySendPolicyByName(sendPolicyBO.getName());

    if (StringUtils.isNotBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "外发策略名称不能重复");
    }
    ExternalReceiverDO externalReceiverDO = externalReceiverDao
        .queryExternalReceiver(sendPolicyBO.getExternalReceiverId());
    SendRuleDO sendRuleDO = sendRuleDao.querySendRule(sendPolicyBO.getSendRuleId());
    String receiverType = externalReceiverDO.getReceiverType();
    List<Map<String, Object>> sendRuleContentList = JsonHelper.deserialize(
        sendRuleDO.getSendRuleContent(), new TypeReference<List<Map<String, Object>>>() {
        }, false);
    List<String> indexList = sendRuleContentList.stream()
        .map(sendRuleContent -> MapUtils.getString(sendRuleContent, "index"))
        .collect(Collectors.toList());
    if (StringUtils.equals(receiverType, FpcCmsConstants.APPLIANCE_EXTERNAL_RECEIVER_TYPE_MAIL)) {
      List<String> temp = Arrays.asList("alert", "systemAlert", "systemLog");
      if (indexList.stream().anyMatch(x -> !temp.contains(x))) {
        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
            "邮件外发服务器对应的外发规则中只能包含网络告警、业务告警、系统告警和系统日志");
      }
    } else if (StringUtils.equalsAny(receiverType,
        FpcCmsConstants.APPLIANCE_EXTERNAL_RECEIVER_TYPE_KAFKA,
        FpcCmsConstants.APPLIANCE_EXTERNAL_RECEIVER_TYPE_ZMQ)) {
      List<String> temp = Arrays.asList("systemAlert", "systemLog");
      if (indexList.stream().anyMatch(temp::contains)) {
        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
            "kafka或zmq外发服务器对应的外发规则中的日志类型不能包含系统告警和系统日志");
      }
    }

    SendPolicyDO sendPolicyDO = new SendPolicyDO();
    BeanUtils.copyProperties(sendPolicyBO, sendPolicyDO);
    sendPolicyDO.setOperatorId(operatorId);

    sendPolicyDao.saveSendPolicy(sendPolicyDO);
    BeanUtils.copyProperties(sendPolicyDO, sendPolicyBO);
    List<Map<String, Object>> messageBodys = Lists
        .newArrayList(sendPolicy2MessageBody(sendPolicyDO, FpcCmsConstants.SYNC_ACTION_ADD));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_SENDPOLICY, null);

    return sendPolicyBO;
  }

  @Override
  public SendPolicyBO updateSendPolicy(SendPolicyBO sendPolicyBO, String id, String operatorId) {

    SendPolicyDO exist = sendPolicyDao.querySendPolicy(id);

    if (StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "外发策略不存在");
    }
    ExternalReceiverDO externalReceiverDO = externalReceiverDao
        .queryExternalReceiver(sendPolicyBO.getExternalReceiverId());
    SendRuleDO sendRuleDO = sendRuleDao.querySendRule(sendPolicyBO.getSendRuleId());
    String receiverType = externalReceiverDO.getReceiverType();
    List<Map<String, Object>> sendRuleContentList = JsonHelper.deserialize(
        sendRuleDO.getSendRuleContent(), new TypeReference<List<Map<String, Object>>>() {
        }, false);
    List<String> indexList = sendRuleContentList.stream()
        .map(sendRuleContent -> MapUtils.getString(sendRuleContent, "index"))
        .collect(Collectors.toList());
    if (StringUtils.equals(receiverType, FpcCmsConstants.APPLIANCE_EXTERNAL_RECEIVER_TYPE_MAIL)) {
      List<String> temp = Arrays.asList("alert", "systemAlert", "systemLog");
      if (indexList.stream().anyMatch(x -> !temp.contains(x))) {
        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
            "邮件外发服务器对应的外发规则中只能包含网络告警、业务告警、系统告警和系统日志");
      }
    } else if (StringUtils.equalsAny(receiverType,
        FpcCmsConstants.APPLIANCE_EXTERNAL_RECEIVER_TYPE_KAFKA,
        FpcCmsConstants.APPLIANCE_EXTERNAL_RECEIVER_TYPE_ZMQ)) {
      List<String> temp = Arrays.asList("systemAlert", "systemLog");
      if (indexList.stream().anyMatch(temp::contains)) {
        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
            "kafka或zmq外发服务器对应的外发规则中的日志类型不能包含系统告警和系统日志");
      }
    }

    SendPolicyDO sendPolicyDO = new SendPolicyDO();
    BeanUtils.copyProperties(sendPolicyBO, sendPolicyDO);
    sendPolicyDO.setOperatorId(operatorId);
    sendPolicyDO.setId(id);

    sendPolicyDao.updateSendPolicy(sendPolicyDO);

    BeanUtils.copyProperties(sendPolicyDO, sendPolicyBO);
    List<Map<String, Object>> messageBodys = Lists
        .newArrayList(sendPolicy2MessageBody(sendPolicyDO, FpcCmsConstants.SYNC_ACTION_MODIFY));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_SENDPOLICY, null);
    return sendPolicyBO;
  }

  @Transactional
  @Override
  public SendPolicyBO deleteSendPolicy(String id, String operatorId, boolean forceDelete) {

    SendPolicyDO exist = sendPolicyDao.querySendPolicy(id);

    if (!forceDelete && StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "外发策略不存在");
    }
    List<NetworkPolicyDO> networkPolicyDOList = networkPolicyDao.queryNetworkPolicyByPolicyId(id,
        FpcCmsConstants.APPLIANCE_NETWORK_POLICY_SEND);
    if (!forceDelete && !networkPolicyDOList.isEmpty()) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "外发策略正在网络被引用，无法删除");
    }

    networkPolicyDao.deleteNetworkPolicyByPolicyId(id,
        FpcCmsConstants.APPLIANCE_NETWORK_POLICY_SEND);
    sendPolicyDao.deleteSendPolicy(id, operatorId);

    SendPolicyBO sendPolicyBO = new SendPolicyBO();
    BeanUtils.copyProperties(exist, sendPolicyBO);

    List<Map<String, Object>> messageBodys = Lists
        .newArrayList(sendPolicy2MessageBody(exist, FpcCmsConstants.SYNC_ACTION_DELETE));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_SENDPOLICY, null);

    return sendPolicyBO;
  }

  @Override
  public SendPolicyBO changeSendPolicyState(String id, String state, String operatorId) {

    SendPolicyDO exist = sendPolicyDao.querySendPolicy(id);

    if (StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "外发策略不存在");
    }
    List<NetworkPolicyDO> networkPolicyDOList = networkPolicyDao.queryNetworkPolicyByPolicyId(id,
        FpcCmsConstants.APPLIANCE_NETWORK_POLICY_SEND);
    if (!networkPolicyDOList.isEmpty()) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
          "外发策略正在网络被引用，状态无法修改");
    }

    sendPolicyDao.changeSendPolicyState(id, state, operatorId);

    SendPolicyBO sendPolicyBO = new SendPolicyBO();
    BeanUtils.copyProperties(exist, sendPolicyBO);

    List<Map<String, Object>> messageBodys = Lists
        .newArrayList(sendPolicy2MessageBody(exist, FpcCmsConstants.SYNC_ACTION_MODIFY));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_SENDPOLICY, null);
    return sendPolicyBO;
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
  public Map<String, List<String>> getFullConfigurationIds(String deviceType, String serialNumber,
      Date beforeTime) {
    Map<String, List<String>> map = Maps.newHashMapWithExpectedSize(1);
    map.put(FpcCmsConstants.MQ_TAG_SENDPOLICY, sendPolicyDao.querySendPoliciesIds(false));
    return map;
  }

  @Override
  public Tuple3<Boolean, List<Map<String, Object>>, Message> getFullConfigurations(
      String deviceType, String serialNumber, String tag) {
    if (StringUtils.equals(tag, FpcCmsConstants.MQ_TAG_SENDPOLICY)) {
      List<SendPolicyDO> sendPolicyDOList = sendPolicyDao.querySendPolicies();
      List<Map<String, Object>> list = sendPolicyDOList.stream()
          .map(
              sendPolicyDO -> sendPolicy2MessageBody(sendPolicyDO, FpcCmsConstants.SYNC_ACTION_ADD))
          .collect(Collectors.toList());
      return Tuples.of(true, list, MQMessageHelper.EMPTY);
    } else {
      return Tuples.of(true, Lists.newArrayListWithCapacity(0), MQMessageHelper.EMPTY);
    }
  }

  private Map<String, Object> sendPolicy2MessageBody(SendPolicyDO sendPolicyDO, String action) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", sendPolicyDO.getId());
    map.put("name", sendPolicyDO.getName());
    map.put("externalReceiverId", sendPolicyDO.getExternalReceiverId());
    map.put("sendRuleId", sendPolicyDO.getSendRuleId());
    map.put("state", sendPolicyDO.getState());
    map.put("action", action);
    return map;
  }

  /********************************************************************************************************
   * 接收模块
   *******************************************************************************************************/
  @PostConstruct
  public void init() {
    MQReceiveServiceImpl.register(this, Lists.newArrayList(FpcCmsConstants.MQ_TAG_SENDPOLICY));
  }

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

    int syncTotalCount = messages.stream().mapToInt(item -> syncSendPolicy(item)).sum();
    LOGGER.info("current sync sendPolicy total: {}.", syncTotalCount);

    return syncTotalCount;
  }

  private int syncSendPolicy(Map<String, Object> messageBody) {
    int syncCount = 0;
    String assignId = MapUtils.getString(messageBody, "id");
    if (StringUtils.isBlank(assignId)) {
      return syncCount;
    }
    String action = MapUtils.getString(messageBody, "action");
    // 下发的规则与本地规则名称冲突，添加后缀
    String name = MapUtils.getString(messageBody, "name");
    SendPolicyDO existName = sendPolicyDao.querySendPolicyByName(name);
    if (StringUtils.equals(action, FpcCmsConstants.SYNC_ACTION_ADD)
        && StringUtils.isNotBlank(existName.getId())) {
      name = name + "_" + System.currentTimeMillis();
    }
    SendPolicyBO sendPolicyBO = new SendPolicyBO();
    sendPolicyBO.setId(assignId);
    sendPolicyBO.setAssignId(assignId);
    sendPolicyBO.setName(name);
    sendPolicyBO.setExternalReceiverId(MapUtils.getString(messageBody, "externalReceiverId"));
    sendPolicyBO.setSendRuleId(MapUtils.getString(messageBody, "sendRuleId"));
    sendPolicyBO.setState(MapUtils.getString(messageBody, "state"));

    SendPolicyDO exist = sendPolicyDao.querySendPolicyByAssignId(sendPolicyBO.getAssignId());

    int addCount = 0;
    int modifyCount = 0;
    int deleteCount = 0;
    try {
      switch (action) {
        case FpcCmsConstants.SYNC_ACTION_ADD:
        case FpcCmsConstants.SYNC_ACTION_MODIFY:
          if (StringUtils.isNotBlank(exist.getId())) {
            updateSendPolicy(sendPolicyBO, exist.getId(), CMS_ASSIGNMENT);
            modifyCount++;
          } else {
            saveSendPolicy(sendPolicyBO, CMS_ASSIGNMENT);
            addCount++;
          }
          break;
        case FpcCmsConstants.SYNC_ACTION_DELETE:
          deleteSendPolicy(exist.getId(), CMS_ASSIGNMENT, true);
          deleteCount++;
          break;
        default:
          break;
      }

      // 本次同步数据量
      syncCount = addCount + modifyCount + deleteCount;

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("current sync sendPolicy status: [add: {}, modify: {}, delete: {}]", addCount,
            modifyCount, deleteCount);
      }
    } catch (BusinessException e) {
      LOGGER.warn("sync failed. error msg: {}", e.getMessage());
      return syncCount;
    }

    return syncCount;


  }

  @Override
  public int clearLocalConfiguration(String tag, boolean onlyLocal, Date beforeTime) {
    int clearCount = 0;
    List<String> sendPolicies = sendPolicyDao.querySendPoliciesIds(onlyLocal);
    List<NetworkPolicyDO> networkPolicyDOList = networkPolicyDao
        .queryNetworkPolicyByPolicyType(FpcCmsConstants.APPLIANCE_NETWORK_POLICY_SEND);
    Set<String> policyIds = networkPolicyDOList.stream().map(NetworkPolicyDO::getPolicyId)
        .collect(Collectors.toSet());
    Set<String> existSendPolicyIds = sendPolicyDao.querySendPolicies().stream()
        .filter(sendPolicyDO -> policyIds.contains(sendPolicyDO.getId())).map(SendPolicyDO::getId)
        .collect(Collectors.toSet());
    for (String sendPolicy : sendPolicies) {
      if (existSendPolicyIds.contains(sendPolicy)) {
        LOGGER.warn("外发策略已被网络使用，不能删除");
        continue;
      }
      try {
        deleteSendPolicy(sendPolicy, CMS_ASSIGNMENT, true);
        clearCount++;
      } catch (BusinessException e) {
        LOGGER.warn("delete sendPolicy failed. error msg: {}", e.getMessage());
      }
    }

    return clearCount;
  }

  @Override
  public List<String> getAssignConfigurationIds(String tag, Date beforeTime) {

    return sendPolicyDao.queryAssignSendPolicyIds(beforeTime).stream()
        .map(SendPolicyDO::getAssignId).collect(Collectors.toList());
  }


}
