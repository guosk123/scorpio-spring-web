package com.machloop.fpc.manager.appliance.service.impl;

import java.util.*;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

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
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.fpc.cms.common.FpcCmsConstants;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.appliance.bo.SendPolicyBO;
import com.machloop.fpc.manager.appliance.dao.ExternalReceiverDao;
import com.machloop.fpc.manager.appliance.dao.SendPolicyDao;
import com.machloop.fpc.manager.appliance.dao.SendRuleDao;
import com.machloop.fpc.manager.appliance.data.ExternalReceiverDO;
import com.machloop.fpc.manager.appliance.data.SendPolicyDO;
import com.machloop.fpc.manager.appliance.data.SendRuleDO;
import com.machloop.fpc.manager.appliance.service.SendPolicyService;
import com.machloop.fpc.manager.cms.service.SyncConfigurationService;
import com.machloop.fpc.manager.cms.service.impl.MQAssignmentServiceImpl;
import com.machloop.fpc.manager.helper.MQMessageHelper;
import com.machloop.fpc.npm.appliance.dao.NetworkPolicyDao;
import com.machloop.fpc.npm.appliance.dao.PacketAnalysisTaskPolicyDao;
import com.machloop.fpc.npm.appliance.data.NetworkPolicyDO;
import com.machloop.fpc.npm.appliance.data.PacketAnalysisTaskPolicyDO;

/**
 * @author ChenXiao
 * create at 2022/8/31
 */
@Order(14)
@Service
public class SendPolicyServiceImpl implements SendPolicyService, SyncConfigurationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(SendPolicyServiceImpl.class);
  @Autowired
  private SendPolicyDao sendPolicyDao;

  @Autowired
  private NetworkPolicyDao networkPolicyDao;

  @Autowired
  private PacketAnalysisTaskPolicyDao packetAnalysisTaskPolicyDao;

  @Autowired
  private SendRuleDao sendRuleDao;

  @Autowired
  private GlobalSettingService globalSettingService;

  @Autowired
  private ExternalReceiverDao externalReceiverDao;

  @Override
  public List<Map<String, Object>> querySendPolicies() {

    List<SendPolicyDO> sendPolicyDOList = sendPolicyDao.querySendPolicies();
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    sendPolicyDOList.forEach(sendPolicyDO -> {
      Map<String, Object> temp = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      temp.put("id", sendPolicyDO.getId());
      temp.put("name", sendPolicyDO.getName());
      temp.put("externalReceiverId", sendPolicyDO.getExternalReceiverId());
      temp.put("sendRuleId", sendPolicyDO.getSendRuleId());
      temp.put("state", sendPolicyDO.getState());
      List<NetworkPolicyDO> networkPolicyDOList = networkPolicyDao.queryNetworkPolicyByPolicyId(
          sendPolicyDO.getId(), FpcConstants.APPLIANCE_NETWORK_POLICY_SEND);
      List<String> networkIdList = networkPolicyDOList.stream().map(NetworkPolicyDO::getNetworkId)
          .collect(Collectors.toList());
      List<PacketAnalysisTaskPolicyDO> packetAnalysisTaskPolicyDOList = packetAnalysisTaskPolicyDao
          .queryPacketAnalysisTaskPolicyByPolicyIdAndPolicyType(sendPolicyDO.getId(),
              FpcConstants.APPLIANCE_PACKET_ANALYSIS_TASK_POLICY_SEND);
      List<String> packetAnalysisTaskIdList = packetAnalysisTaskPolicyDOList.stream()
          .map(PacketAnalysisTaskPolicyDO::getPacketAnalysisTaskId).collect(Collectors.toList());
      List<String> quoteList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      quoteList.addAll(networkIdList);
      quoteList.addAll(packetAnalysisTaskIdList);
      temp.put("quote", CsvUtils.convertCollectionToCSV(quoteList));
      result.add(temp);
    });

    return result;
  }

  @Override
  public Map<String, Object> querySendPolicy(String id) {

    SendPolicyDO sendPolicyDO = sendPolicyDao.querySendPolicy(id);

    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    result.put("id", sendPolicyDO.getId());
    result.put("name", sendPolicyDO.getName());
    result.put("externalReceiverId", sendPolicyDO.getExternalReceiverId());
    result.put("sendRuleId", sendPolicyDO.getSendRuleId());
    result.put("state", sendPolicyDO.getState());
    List<NetworkPolicyDO> networkPolicyDOList = networkPolicyDao.queryNetworkPolicyByPolicyId(
        sendPolicyDO.getId(), FpcConstants.APPLIANCE_NETWORK_POLICY_SEND);
    List<String> networkIdList = networkPolicyDOList.stream().map(NetworkPolicyDO::getNetworkId)
        .collect(Collectors.toList());
    List<PacketAnalysisTaskPolicyDO> packetAnalysisTaskPolicyDOList = packetAnalysisTaskPolicyDao
        .queryPacketAnalysisTaskPolicyByPolicyIdAndPolicyType(sendPolicyDO.getId(),
            FpcConstants.APPLIANCE_PACKET_ANALYSIS_TASK_POLICY_SEND);
    List<String> packetAnalysisTaskIdList = packetAnalysisTaskPolicyDOList.stream()
        .map(PacketAnalysisTaskPolicyDO::getPacketAnalysisTaskId).collect(Collectors.toList());
    List<String> quoteList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    quoteList.addAll(networkIdList);
    quoteList.addAll(packetAnalysisTaskIdList);
    result.put("quote", CsvUtils.convertCollectionToCSV(quoteList));

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
          sendPolicyDO.getId(), FpcConstants.APPLIANCE_NETWORK_POLICY_SEND);
      List<String> networkIdList = networkPolicyDOList.stream().map(NetworkPolicyDO::getNetworkId)
          .collect(Collectors.toList());
      List<PacketAnalysisTaskPolicyDO> packetAnalysisTaskPolicyDOList = packetAnalysisTaskPolicyDao
          .queryPacketAnalysisTaskPolicyByPolicyIdAndPolicyType(sendPolicyDO.getId(),
              FpcConstants.APPLIANCE_PACKET_ANALYSIS_TASK_POLICY_SEND);
      List<String> packetAnalysisTaskIdList = packetAnalysisTaskPolicyDOList.stream()
          .map(PacketAnalysisTaskPolicyDO::getPacketAnalysisTaskId).collect(Collectors.toList());
      List<String> quoteList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      quoteList.addAll(networkIdList);
      quoteList.addAll(packetAnalysisTaskIdList);
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
    if (StringUtils.isEmpty(sendPolicyBO.getSendPolicyInCmsId())) {
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
      if (StringUtils.equals(receiverType, FpcConstants.APPLIANCE_EXTERNAL_RECEIVER_TYPE_MAIL)) {
        List<String> temp = Arrays.asList("alert", "systemAlert", "systemLog");
        if (indexList.stream().anyMatch(x -> !temp.contains(x))) {
          throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
              "邮件外发服务器对应的外发规则中只能包含网络告警、业务告警、系统告警和系统日志");
        }
      } else if (StringUtils.equalsAny(receiverType,
          FpcConstants.APPLIANCE_EXTERNAL_RECEIVER_TYPE_KAFKA,
          FpcConstants.APPLIANCE_EXTERNAL_RECEIVER_TYPE_ZMQ)) {
        List<String> temp = Arrays.asList("systemAlert", "systemLog");
        if (indexList.stream().anyMatch(temp::contains)) {
          throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
              "kafka或zmq外发服务器对应的外发规则中的日志类型不能包含系统告警和系统日志");
        }
      }
    }
    SendPolicyDO sendPolicyDO = new SendPolicyDO();
    BeanUtils.copyProperties(sendPolicyBO, sendPolicyDO);
    sendPolicyDO.setOperatorId(operatorId);

    sendPolicyDao.saveSendPolicy(sendPolicyDO);
    BeanUtils.copyProperties(sendPolicyDO, sendPolicyBO);

    return sendPolicyBO;
  }

  @Override
  public SendPolicyBO updateSendPolicy(SendPolicyBO sendPolicyBO, String id, String operatorId) {

    SendPolicyDO exist = sendPolicyDao.querySendPolicy(id);

    if (StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "外发策略不存在");
    }
    if (StringUtils.isEmpty(sendPolicyBO.getSendPolicyInCmsId())) {
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
      if (StringUtils.equals(receiverType, FpcConstants.APPLIANCE_EXTERNAL_RECEIVER_TYPE_MAIL)) {
        List<String> temp = Arrays.asList("alert", "systemAlert", "systemLog");
        if (indexList.stream().anyMatch(x -> !temp.contains(x))) {
          throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
              "邮件外发服务器对应的外发规则中只能包含网络告警、业务告警、系统告警和系统日志");
        }
      } else if (StringUtils.equalsAny(receiverType,
          FpcConstants.APPLIANCE_EXTERNAL_RECEIVER_TYPE_KAFKA,
          FpcConstants.APPLIANCE_EXTERNAL_RECEIVER_TYPE_ZMQ)) {
        List<String> temp = Arrays.asList("systemAlert", "systemLog");
        if (indexList.stream().anyMatch(temp::contains)) {
          throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
              "kafka或zmq外发服务器对应的外发规则中的日志类型不能包含系统告警和系统日志");
        }
      }
    }
    SendPolicyDO sendPolicyDO = new SendPolicyDO();
    BeanUtils.copyProperties(sendPolicyBO, sendPolicyDO);
    sendPolicyDO.setOperatorId(operatorId);
    sendPolicyDO.setId(id);

    sendPolicyDao.updateSendPolicy(sendPolicyDO);

    BeanUtils.copyProperties(sendPolicyDO, sendPolicyBO);
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
        FpcConstants.APPLIANCE_NETWORK_POLICY_SEND);
    List<PacketAnalysisTaskPolicyDO> packetAnalysisTaskPolicyDOList = packetAnalysisTaskPolicyDao
        .queryPacketAnalysisTaskPolicyByPolicyIdAndPolicyType(id,
            FpcConstants.APPLIANCE_NETWORK_POLICY_SEND);
    if (!forceDelete && !networkPolicyDOList.isEmpty()
        || !packetAnalysisTaskPolicyDOList.isEmpty()) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
          "外发策略正在被网络或离线文件使用，不可删除");
    }

    networkPolicyDao.deleteNetworkPolicyByPolicyId(id, FpcConstants.APPLIANCE_NETWORK_POLICY_SEND);
    packetAnalysisTaskPolicyDao.deletePacketAnalysisTaskPolicyByPolicyId(id,
        FpcConstants.APPLIANCE_NETWORK_POLICY_SEND);
    sendPolicyDao.deleteSendPolicy(id, operatorId);

    SendPolicyBO sendPolicyBO = new SendPolicyBO();
    BeanUtils.copyProperties(exist, sendPolicyBO);

    return sendPolicyBO;
  }

  @Override
  public SendPolicyBO changeSendPolicyState(String id, String state, String operatorId) {

    SendPolicyDO exist = sendPolicyDao.querySendPolicy(id);

    if (StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "外发策略不存在");
    }
    List<NetworkPolicyDO> networkPolicyDOList = networkPolicyDao.queryNetworkPolicyByPolicyId(id,
        FpcConstants.APPLIANCE_NETWORK_POLICY_SEND);
    List<PacketAnalysisTaskPolicyDO> packetAnalysisTaskPolicyDOList = packetAnalysisTaskPolicyDao
        .queryPacketAnalysisTaskPolicyByPolicyIdAndPolicyType(id,
            FpcConstants.APPLIANCE_NETWORK_POLICY_SEND);
    if (!networkPolicyDOList.isEmpty() || !packetAnalysisTaskPolicyDOList.isEmpty()) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
          "外发策略正在被网络或离线文件使用，状态无法修改");
    }

    sendPolicyDao.changeSendPolicyState(id, state, operatorId);

    SendPolicyBO sendPolicyBO = new SendPolicyBO();
    BeanUtils.copyProperties(exist, sendPolicyBO);

    return sendPolicyBO;
  }

  /********************************************************************************************************
   * 接收模块
   *******************************************************************************************************/

  @PostConstruct
  public void init() {
    MQAssignmentServiceImpl.register(this, Lists.newArrayList(FpcCmsConstants.MQ_TAG_SENDPOLICY));
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
    String sendPolicyInCmsId = MapUtils.getString(messageBody, "id");
    if (StringUtils.isBlank(sendPolicyInCmsId)) {
      return syncCount;
    }
    String action = MapUtils.getString(messageBody, "action");

    String name = MapUtils.getString(messageBody, "name");
    SendPolicyDO existName = sendPolicyDao.querySendPolicyByName(name);
    if (StringUtils.equals(action, FpcCmsConstants.SYNC_ACTION_ADD)
        && StringUtils.isNotBlank(existName.getId())) {
      name = name + "_" + "CMS";
    }
    SendPolicyBO sendPolicyBO = new SendPolicyBO();
    sendPolicyBO.setId(sendPolicyInCmsId);
    sendPolicyBO.setSendPolicyInCmsId(sendPolicyInCmsId);
    sendPolicyBO.setName(name);
    sendPolicyBO.setExternalReceiverId(MapUtils.getString(messageBody, "externalReceiverId"));
    sendPolicyBO.setSendRuleId(MapUtils.getString(messageBody, "sendRuleId"));
    sendPolicyBO.setState(MapUtils.getString(messageBody, "state"));

    SendPolicyDO exist = sendPolicyDao
        .querySendPolicyBySendPolicyInCmsId(sendPolicyBO.getSendPolicyInCmsId());

    int addCount = 0;
    int modifyCount = 0;
    int deleteCount = 0;
    try {
      switch (action) {
        case FpcCmsConstants.SYNC_ACTION_ADD:
        case FpcCmsConstants.SYNC_ACTION_MODIFY:
          if (StringUtils.isNotBlank(exist.getId())) {
            updateSendPolicy(sendPolicyBO, exist.getId(), CMS_ASSIGNMENT);
            LogHelper.auditAssignOperate(
                globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
                sendPolicyBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_UPDATE));
            modifyCount++;
          } else {
            saveSendPolicy(sendPolicyBO, CMS_ASSIGNMENT);
            LogHelper.auditAssignOperate(
                globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
                sendPolicyBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_SAVE));
            addCount++;
          }
          break;
        case FpcCmsConstants.SYNC_ACTION_DELETE:
          deleteSendPolicy(exist.getId(), CMS_ASSIGNMENT, true);
          LogHelper.auditAssignOperate(
              globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
              sendPolicyBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_DELETE));
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
    List<String> sendPolicies = sendPolicyDao.querySendPolicyIds(onlyLocal);

    Set<String> policyIds = Sets.newHashSetWithExpectedSize(0);
    List<NetworkPolicyDO> networkPolicyDOList = networkPolicyDao
        .queryNetworkPolicyByPolicyType(FpcConstants.APPLIANCE_NETWORK_POLICY_SEND);
    policyIds.addAll(
        networkPolicyDOList.stream().map(NetworkPolicyDO::getPolicyId).collect(Collectors.toSet()));
    List<PacketAnalysisTaskPolicyDO> packetAnalysisTaskPolicyDOList = packetAnalysisTaskPolicyDao
        .queryPolicyIdsByPolicyType(FpcConstants.APPLIANCE_NETWORK_POLICY_SEND);
    policyIds.addAll(packetAnalysisTaskPolicyDOList.stream()
        .map(PacketAnalysisTaskPolicyDO::getPolicyId).collect(Collectors.toSet()));

    Set<String> existSendPolicyIds = sendPolicyDao.querySendPolicies().stream()
        .filter(sendPolicyDO -> policyIds.contains(sendPolicyDO.getId())).map(SendPolicyDO::getId)
        .collect(Collectors.toSet());
    for (String sendPolicy : sendPolicies) {
      if (existSendPolicyIds.contains(sendPolicy)) {
        LOGGER.warn("外发策略已被网络或离线任务使用，不能删除");
        continue;
      }
      try {
        deleteSendPolicy(sendPolicy, CMS_ASSIGNMENT, true);
        clearCount++;
      } catch (BusinessException e) {
        LOGGER.warn("delete sendRule failed. error msg: {}", e.getMessage());
      }

    }
    return clearCount;
  }

  @Override
  public List<String> getAssignConfigurationIds(String tag, Date beforeTime) {
    return sendPolicyDao.queryAssignSendPolicyIds(beforeTime);
  }
}
