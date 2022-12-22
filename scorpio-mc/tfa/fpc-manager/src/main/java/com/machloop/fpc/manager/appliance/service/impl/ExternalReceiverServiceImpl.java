package com.machloop.fpc.manager.appliance.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.fpc.cms.common.FpcCmsConstants;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.appliance.bo.ExternalReceiverBO;
import com.machloop.fpc.manager.appliance.dao.ExternalReceiverDao;
import com.machloop.fpc.manager.appliance.dao.SendPolicyDao;
import com.machloop.fpc.manager.appliance.data.ExternalReceiverDO;
import com.machloop.fpc.manager.appliance.data.SendPolicyDO;
import com.machloop.fpc.manager.appliance.service.ExternalReceiverService;
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
@Order(12)
@Service
public class ExternalReceiverServiceImpl
    implements ExternalReceiverService, SyncConfigurationService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ExternalReceiverServiceImpl.class);
  @Autowired
  private ExternalReceiverDao externalReceiverDao;

  @Autowired
  private SendPolicyDao sendPolicyDao;

  @Autowired
  private GlobalSettingService globalSettingService;

  @Autowired
  private NetworkPolicyDao networkPolicyDao;

  @Autowired
  private PacketAnalysisTaskPolicyDao packetAnalysisTaskPolicyDao;


  @Override
  public Map<String, List<Map<String, Object>>> queryExternalReceivers() {
    List<ExternalReceiverDO> externalReceiverDOList = externalReceiverDao.queryExternalReceivers();

    Map<String, List<Map<String, Object>>> result = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    List<Map<String, Object>> mailReceiverList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Map<String, Object>> syslogReceiverList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Map<String, Object>> kafkaReceiverList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    List<Map<String, Object>> zmqReceiverList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    externalReceiverDOList.forEach(externalReceiverDO -> {
      Map<String, Object> temp = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      temp.put("id", externalReceiverDO.getId());
      temp.put("name", externalReceiverDO.getName());
      Map<String, Object> receiverContent = JsonHelper.deserialize(
          externalReceiverDO.getReceiverContent(), new TypeReference<Map<String, Object>>() {
          }, false);
      temp.putAll(receiverContent);
      if (StringUtils.equals(externalReceiverDO.getReceiverType(),
          FpcConstants.APPLIANCE_EXTERNAL_RECEIVER_TYPE_MAIL)) {
        mailReceiverList.add(temp);
      } else if (StringUtils.equals(externalReceiverDO.getReceiverType(),
          FpcConstants.APPLIANCE_EXTERNAL_RECEIVER_TYPE_SYSLOG)) {
        syslogReceiverList.add(temp);
      } else if (StringUtils.equals(externalReceiverDO.getReceiverType(),
          FpcConstants.APPLIANCE_EXTERNAL_RECEIVER_TYPE_KAFKA)) {
        kafkaReceiverList.add(temp);
      } else if (StringUtils.equals(externalReceiverDO.getReceiverType(),
          FpcConstants.APPLIANCE_EXTERNAL_RECEIVER_TYPE_ZMQ)) {
        zmqReceiverList.add(temp);
      }
    });
    result.put("mail", mailReceiverList);
    result.put("syslog", syslogReceiverList);
    result.put("kafka", kafkaReceiverList);
    result.put("zmq", zmqReceiverList);

    return result;
  }

  @Override
  public List<Map<String, Object>> queryExternalReceiversByType(String receiverType) {
    List<ExternalReceiverDO> externalReceiverDOList = externalReceiverDao
        .queryExternalReceiversByType(receiverType);

    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    externalReceiverDOList.forEach(externalReceiverDO -> {
      Map<String, Object> temp = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      temp.put("id", externalReceiverDO.getId());
      temp.put("name", externalReceiverDO.getName());
      Map<String, Object> receiverContent = JsonHelper.deserialize(
          externalReceiverDO.getReceiverContent(), new TypeReference<Map<String, Object>>() {
          }, false);
      temp.putAll(receiverContent);
      result.add(temp);
    });
    return result;
  }

  @Override
  public Map<String, Object> queryExternalReceiver(String id) {

    ExternalReceiverDO externalReceiverDO = externalReceiverDao.queryExternalReceiver(id);
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    result.put("id", externalReceiverDO.getId());
    result.put("name", externalReceiverDO.getName());
    Map<String, Object> receiverContent = JsonHelper.deserialize(
        externalReceiverDO.getReceiverContent(), new TypeReference<Map<String, Object>>() {
        }, false);
    result.putAll(receiverContent);
    if (StringUtils.equals(externalReceiverDO.getReceiverType(),
        FpcConstants.APPLIANCE_EXTERNAL_RECEIVER_TYPE_MAIL)) {
      result.put("receiverType", "邮件服务器");
    } else if (StringUtils.equals(externalReceiverDO.getReceiverType(),
        FpcConstants.APPLIANCE_EXTERNAL_RECEIVER_TYPE_SYSLOG)) {
      result.put("receiverType", "syslog服务器");
    } else if (StringUtils.equals(externalReceiverDO.getReceiverType(),
        FpcConstants.APPLIANCE_EXTERNAL_RECEIVER_TYPE_KAFKA)) {
      result.put("receiverType", "kafka服务器");
    } else if (StringUtils.equals(externalReceiverDO.getReceiverType(),
        FpcConstants.APPLIANCE_EXTERNAL_RECEIVER_TYPE_ZMQ)) {
      result.put("receiverType", "zmq服务器");
    }
    return result;
  }

  @Override
  public ExternalReceiverBO saveExternalReceiver(ExternalReceiverBO externalReceiverBO,
      String operatorId) {

    ExternalReceiverDO exist = externalReceiverDao
        .queryExternalReceiverByName(externalReceiverBO.getName());

    if (StringUtils.isNotBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "外发服务器名称不能重复");
    }

    List<
        ExternalReceiverDO> mailExternalReceiver = externalReceiverDao.queryMailExternalReceivers();
    if (!mailExternalReceiver.isEmpty()) {
      List<String> mailTitleList = mailExternalReceiver.stream().map(externalReceiverDO -> {
        Map<String, Object> receiverContent = JsonHelper.deserialize(
            externalReceiverDO.getReceiverContent(), new TypeReference<Map<String, Object>>() {
            }, false);
        return MapUtils.getString(receiverContent, "mailTitle");
      }).collect(Collectors.toList());
      Map<String, Object> receiverContent = JsonHelper.deserialize(
          externalReceiverBO.getReceiverContent(), new TypeReference<Map<String, Object>>() {
          }, false);
      if (receiverContent.containsKey("mailTitle")
          && mailTitleList.contains(MapUtils.getString(receiverContent, "mailTitle"))) {
        throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "邮件外发服务器主题不能重复");
      }
    }
    ExternalReceiverDO externalReceiverDO = new ExternalReceiverDO();
    BeanUtils.copyProperties(externalReceiverBO, externalReceiverDO);
    externalReceiverDO.setOperatorId(operatorId);
    externalReceiverDao.saveExternalReceiver(externalReceiverDO);

    BeanUtils.copyProperties(externalReceiverDO, externalReceiverBO);
    return externalReceiverBO;
  }

  @Override
  public ExternalReceiverBO updateExternalReceiver(ExternalReceiverBO externalReceiverBO, String id,
      String operatorId) {

    ExternalReceiverDO exist = externalReceiverDao.queryExternalReceiver(id);
    if (StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "外发服务器不存在");
    }
    ExternalReceiverDO externalReceiverDO = new ExternalReceiverDO();
    BeanUtils.copyProperties(externalReceiverBO, externalReceiverDO);
    externalReceiverDO.setOperatorId(operatorId);
    externalReceiverDO.setId(id);

    // 先保存完外发服务器的更改配置，再修改外发策略的时间
    externalReceiverDao.updateExternalReceiver(externalReceiverDO);

    List<SendPolicyDO> sendPolicyDOList = sendPolicyDao.querySendPoliciesByExternelReceiverId(id);
    if (!sendPolicyDOList.isEmpty()) {
      sendPolicyDao.updateSendPolicyTimeByExternalReceiverId(id, operatorId);
    }

    BeanUtils.copyProperties(externalReceiverDO, externalReceiverBO);
    return externalReceiverBO;
  }

  @Transactional
  @Override
  public ExternalReceiverBO deleteExternalReceiver(String id, String operatorId,
      boolean forceDelete) {
    ExternalReceiverDO exist = externalReceiverDao.queryExternalReceiver(id);
    if (!forceDelete && StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "外发服务器不存在");
    }
    List<SendPolicyDO> sendPolicyDOList = sendPolicyDao.querySendPoliciesByExternelReceiverId(id);
    if (!forceDelete && !sendPolicyDOList.isEmpty()) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT,
          "外发服务器正在被外发策略使用，无法删除");
    }

    externalReceiverDao.deleteExternalReceiver(id, operatorId);

    ExternalReceiverBO externalReceiverBO = new ExternalReceiverBO();
    BeanUtils.copyProperties(exist, externalReceiverBO);
    return externalReceiverBO;
  }

  /********************************************************************************************************
   * 接收模块
   *******************************************************************************************************/

  @PostConstruct
  public void init() {
    MQAssignmentServiceImpl.register(this,
        Lists.newArrayList(FpcCmsConstants.MQ_TAG_EXTERNALRECEIVER));
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

    int syncTotalCount = messages.stream().mapToInt(item -> syncExternalReceiver(item)).sum();
    LOGGER.info("current sync externalReceiver total: {}.", syncTotalCount);

    return syncTotalCount;
  }

  private int syncExternalReceiver(Map<String, Object> messageBody) {

    int syncCount = 0;
    String externalReceiverInCmsId = MapUtils.getString(messageBody, "id");
    if (StringUtils.isBlank(externalReceiverInCmsId)) {
      return syncCount;
    }

    String action = MapUtils.getString(messageBody, "action");
    // 下发的规则与本地规则名称冲突，添加后缀
    String name = MapUtils.getString(messageBody, "name");
    ExternalReceiverDO existName = externalReceiverDao.queryExternalReceiverByName(name);
    if (StringUtils.equals(action, FpcCmsConstants.SYNC_ACTION_ADD)
        && StringUtils.isNotBlank(existName.getId())) {
      name = name + "_" + "CMS";
    }
    ExternalReceiverBO externalReceiverBO = new ExternalReceiverBO();
    externalReceiverBO.setId(externalReceiverInCmsId);
    externalReceiverBO.setExternalReceiverInCmsId(externalReceiverInCmsId);
    externalReceiverBO.setName(name);
    externalReceiverBO.setReceiverContent(MapUtils.getString(messageBody, "receiverContent"));
    externalReceiverBO.setReceiverType(MapUtils.getString(messageBody, "receiverType"));

    ExternalReceiverDO exist = externalReceiverDao.queryExternalReceiverByExternalReceiverInCmsId(
        externalReceiverBO.getExternalReceiverInCmsId());

    int addCount = 0;
    int modifyCount = 0;
    int deleteCount = 0;
    try {
      switch (action) {
        case FpcCmsConstants.SYNC_ACTION_ADD:
        case FpcCmsConstants.SYNC_ACTION_MODIFY:
          if (StringUtils.isNotBlank(exist.getId())) {
            updateExternalReceiver(externalReceiverBO, exist.getId(), CMS_ASSIGNMENT);
            LogHelper.auditAssignOperate(
                globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
                externalReceiverBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_UPDATE));
            modifyCount++;
          } else {
            saveExternalReceiver(externalReceiverBO, CMS_ASSIGNMENT);
            LogHelper.auditAssignOperate(
                globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
                externalReceiverBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_SAVE));
            addCount++;
          }
          break;
        case FpcCmsConstants.SYNC_ACTION_DELETE:
          deleteExternalReceiver(exist.getId(), CMS_ASSIGNMENT, true);
          LogHelper.auditAssignOperate(
              globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
              externalReceiverBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_DELETE));
          deleteCount++;
          break;
        default:
          break;
      }

      // 本次同步数据量
      syncCount = addCount + modifyCount + deleteCount;

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("current sync externalReceiver status: [add: {}, modify: {}, delete: {}]",
            addCount, modifyCount, deleteCount);
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
    // 本地的外发服务器策略
    List<String> externalReceiverIds = externalReceiverDao.queryExternalReceiverIds(onlyLocal);
    // 本地正在被网络或者离线分析任务使用的外发服务器策略
    Set<String> policyIds = Sets.newHashSetWithExpectedSize(0);
    List<NetworkPolicyDO> networkPolicyDOList = networkPolicyDao
        .queryNetworkPolicyByPolicyType(FpcConstants.APPLIANCE_NETWORK_POLICY_SEND);
    policyIds.addAll(
        networkPolicyDOList.stream().map(NetworkPolicyDO::getPolicyId).collect(Collectors.toSet()));
    List<PacketAnalysisTaskPolicyDO> packetAnalysisTaskPolicyDOList = packetAnalysisTaskPolicyDao
        .queryPolicyIdsByPolicyType(FpcConstants.APPLIANCE_NETWORK_POLICY_SEND);
    policyIds.addAll(packetAnalysisTaskPolicyDOList.stream()
        .map(PacketAnalysisTaskPolicyDO::getPolicyId).collect(Collectors.toSet()));

    Set<String> existExternalReceiverIds = sendPolicyDao.querySendPolicies().stream()
        .filter(sendPolicyDO -> policyIds.contains(sendPolicyDO.getId()))
        .map(SendPolicyDO::getExternalReceiverId).collect(Collectors.toSet());
    for (String externalReceiverId : externalReceiverIds) {
      if (existExternalReceiverIds.contains(externalReceiverId)) {
        LOGGER.warn("外发服务器已被网络或离线任务使用，不能删除");
        continue;
      }
      try {
        deleteExternalReceiver(externalReceiverId, CMS_ASSIGNMENT, true);
        clearCount++;
      } catch (BusinessException e) {
        LOGGER.warn("delete externalReceiver failed. error msg: {}", e.getMessage());
      }

    }
    return clearCount;
  }

  @Override
  public List<String> getAssignConfigurationIds(String tag, Date beforeTime) {
    return externalReceiverDao.queryAssignExternalReceivers(beforeTime);
  }
}
