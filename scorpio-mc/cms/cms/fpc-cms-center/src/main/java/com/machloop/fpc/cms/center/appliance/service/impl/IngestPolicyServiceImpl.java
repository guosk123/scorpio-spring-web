package com.machloop.fpc.cms.center.appliance.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
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
import com.machloop.alpha.common.algorithm.bpf.BpfCheck;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.fpc.cms.center.appliance.bo.IngestPolicyBO;
import com.machloop.fpc.cms.center.appliance.dao.IngestPolicyDao;
import com.machloop.fpc.cms.center.appliance.dao.NetworkPolicyDao;
import com.machloop.fpc.cms.center.appliance.data.IngestPolicyDO;
import com.machloop.fpc.cms.center.appliance.data.NetworkPolicyDO;
import com.machloop.fpc.cms.center.appliance.service.IngestPolicyService;
import com.machloop.fpc.cms.center.broker.service.local.SyncConfigurationService;
import com.machloop.fpc.cms.center.broker.service.local.impl.MQReceiveServiceImpl;
import com.machloop.fpc.cms.center.broker.service.subordinate.MQAssignmentService;
import com.machloop.fpc.cms.center.helper.MQMessageHelper;
import com.machloop.fpc.cms.common.FpcCmsConstants;

import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * @author liumeng
 *
 * create at 2018年12月13日, fpc-manager
 */
@Order(8)
@Service
public class IngestPolicyServiceImpl
    implements IngestPolicyService, MQAssignmentService, SyncConfigurationService {

  private static final List<String> TAGS = Lists.newArrayList(FpcCmsConstants.MQ_TAG_INGESTPOLICY);

  private static final String DEFAULT_INGEST_POLICY_ID = "1";

  @Autowired
  private IngestPolicyDao ingestPolicyDao;

  @Autowired
  private NetworkPolicyDao networkPolicyDao;

  @Autowired
  private DictManager dictManager;

  @Autowired
  private ApplicationContext context;

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.IngestPolicyService#queryIngestPolicys(Pageable)
   */
  @Override
  public Page<IngestPolicyBO> queryIngestPolicys(Pageable page) {
    Page<IngestPolicyDO> ingestPolicys = ingestPolicyDao.queryIngestPolicys(page);

    Map<String,
        String> actionDict = dictManager.getBaseDict().getItemMap("appliance_ingest_action");
    Map<String,
        String> repeatDict = dictManager.getBaseDict().getItemMap("appliance_ingest_repeat");

    List<NetworkPolicyDO> networkPolicys = networkPolicyDao
        .queryNetworkPolicyByPolicyType(FpcCmsConstants.APPLIANCE_NETWORK_POLICY_INGEST);
    Map<String,
        Integer> networkPolicyMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    networkPolicys.forEach(networkPolicy -> {
      Integer referenceCount = networkPolicyMap.get(networkPolicy.getPolicyId());
      networkPolicyMap.put(networkPolicy.getPolicyId(),
          referenceCount == null ? 1 : referenceCount + 1);
    });

    long totalElem = ingestPolicys.getTotalElements();
    List<IngestPolicyBO> result = Lists.newArrayListWithCapacity(ingestPolicys.getSize());
    ingestPolicys.forEach(ingestPolicyDO -> {
      IngestPolicyBO ingestPolicyBO = new IngestPolicyBO();
      BeanUtils.copyProperties(ingestPolicyDO, ingestPolicyBO);
      ingestPolicyBO.setCreateTime(DateUtils.toStringISO8601(ingestPolicyDO.getCreateTime()));
      ingestPolicyBO.setUpdateTime(DateUtils.toStringISO8601(ingestPolicyDO.getUpdateTime()));
      ingestPolicyBO.setDefaultActionText(
          MapUtils.getString(actionDict, ingestPolicyBO.getDefaultAction(), ""));
      ingestPolicyBO.setDeduplicationText(
          MapUtils.getString(repeatDict, ingestPolicyBO.getDeduplication(), ""));
      ingestPolicyBO
          .setReferenceCount(MapUtils.getInteger(networkPolicyMap, ingestPolicyBO.getId(), 0));

      result.add(ingestPolicyBO);
    });

    return new PageImpl<>(result, page, totalElem);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.IngestPolicyService#queryIngestPolicys()
   */
  @Override
  public List<IngestPolicyBO> queryIngestPolicys() {
    List<IngestPolicyDO> ingestPolicys = ingestPolicyDao.queryIngestPolicys();

    Map<String,
        String> actionDict = dictManager.getBaseDict().getItemMap("appliance_ingest_action");
    Map<String,
        String> repeatDict = dictManager.getBaseDict().getItemMap("appliance_ingest_repeat");

    List<IngestPolicyBO> result = Lists.newArrayListWithCapacity(ingestPolicys.size());
    ingestPolicys.forEach(ingestPolicyDO -> {
      IngestPolicyBO ingestPolicyBO = new IngestPolicyBO();
      BeanUtils.copyProperties(ingestPolicyDO, ingestPolicyBO);
      ingestPolicyBO.setCreateTime(DateUtils.toStringISO8601(ingestPolicyDO.getCreateTime()));
      ingestPolicyBO.setUpdateTime(DateUtils.toStringISO8601(ingestPolicyDO.getUpdateTime()));
      ingestPolicyBO.setDefaultActionText(
          MapUtils.getString(actionDict, ingestPolicyBO.getDefaultAction(), ""));
      ingestPolicyBO.setDeduplicationText(
          MapUtils.getString(repeatDict, ingestPolicyBO.getDeduplication(), ""));

      result.add(ingestPolicyBO);
    });

    return result;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.IngestPolicyService#queryIngestPolicy(String)
   */
  @Override
  public IngestPolicyBO queryIngestPolicy(String id) {

    IngestPolicyDO ingestPolicyDO = ingestPolicyDao.queryIngestPolicy(id);

    IngestPolicyBO ingestPolicyBO = new IngestPolicyBO();
    BeanUtils.copyProperties(ingestPolicyDO, ingestPolicyBO);

    Map<String,
        String> actionDict = dictManager.getBaseDict().getItemMap("appliance_ingest_action");
    Map<String,
        String> repeatDict = dictManager.getBaseDict().getItemMap("appliance_ingest_repeat");

    ingestPolicyBO.setDefaultActionText(
        MapUtils.getString(actionDict, ingestPolicyBO.getDefaultAction(), ""));
    ingestPolicyBO.setDeduplicationText(
        MapUtils.getString(repeatDict, ingestPolicyBO.getDeduplication(), ""));
    ingestPolicyBO.setCreateTime(DateUtils.toStringISO8601(ingestPolicyDO.getCreateTime()));
    ingestPolicyBO.setUpdateTime(DateUtils.toStringISO8601(ingestPolicyDO.getUpdateTime()));

    return ingestPolicyBO;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.IngestPolicyService#saveIngestPolicy(com.machloop.fpc.cms.center.appliance.bo.IngestPolicyBO, String)
   */
  @Override
  public IngestPolicyBO saveIngestPolicy(IngestPolicyBO ingestPolicyBO, String operatorId) {
    IngestPolicyDO exist = ingestPolicyDao.queryIngestPolicyByName(ingestPolicyBO.getName());
    if (StringUtils.isNotBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "捕获规则名称不能重复");
    }

    if (StringUtils.isNotBlank(ingestPolicyBO.getExceptBpf())
        && !BpfCheck.isBpfValid(ingestPolicyBO.getExceptBpf())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "BPF规则错误");
    }

    IngestPolicyDO ingestPolicyDO = new IngestPolicyDO();
    BeanUtils.copyProperties(ingestPolicyBO, ingestPolicyDO);
    ingestPolicyDO.setOperatorId(operatorId);

    ingestPolicyDao.saveIngestPolicy(ingestPolicyDO);

    BeanUtils.copyProperties(ingestPolicyDO, ingestPolicyBO);

    // 下发到直属fpc和cms
    List<Map<String, Object>> messageBodys = Lists
        .newArrayList(ingestPolicy2MessageBody(ingestPolicyDO, FpcCmsConstants.SYNC_ACTION_ADD));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_INGESTPOLICY, null);
    return ingestPolicyBO;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.IngestPolicyService#updateIngestPolicy(String, com.machloop.fpc.cms.center.appliance.bo.IngestPolicyBO, String)
   */
  @Override
  public IngestPolicyBO updateIngestPolicy(String id, IngestPolicyBO ingestPolicyBO,
      String operatorId) {
    IngestPolicyDO exist = ingestPolicyDao.queryIngestPolicy(id);
    if (StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "捕获规则不存在");
    }

    if (StringUtils.equals(id, DEFAULT_INGEST_POLICY_ID)
        && !StringUtils.equals(exist.getName(), ingestPolicyBO.getName())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "不允许变更默认规则名称");
    }

    IngestPolicyDO ingestPolicyByName = ingestPolicyDao
        .queryIngestPolicyByName(ingestPolicyBO.getName());
    if (StringUtils.isNotBlank(ingestPolicyByName.getId())
        && !StringUtils.equals(id, ingestPolicyByName.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "捕获规则名称不能重复");
    }

    if (StringUtils.isNotBlank(ingestPolicyBO.getExceptBpf())
        && !BpfCheck.isBpfValid(ingestPolicyBO.getExceptBpf())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "BPF规则错误");
    }

    IngestPolicyDO ingestPolicyDO = new IngestPolicyDO();
    ingestPolicyBO.setId(id);
    BeanUtils.copyProperties(ingestPolicyBO, ingestPolicyDO);
    ingestPolicyDO.setOperatorId(operatorId);

    ingestPolicyDao.updateIngestPolicy(ingestPolicyDO);

    // 下发到直属fpc和cms
    List<Map<String, Object>> messageBodys = Lists
        .newArrayList(ingestPolicy2MessageBody(ingestPolicyDO, FpcCmsConstants.SYNC_ACTION_MODIFY));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_INGESTPOLICY, null);
    return ingestPolicyBO;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.IngestPolicyService#deleteIngestPolicy(String, String)
   */
  @Transactional
  @Override
  public IngestPolicyBO deleteIngestPolicy(String id, String operatorId, boolean forceDelete) {
    IngestPolicyDO exist = ingestPolicyDao.queryIngestPolicy(id);
    if (!forceDelete && StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "捕获规则不存在");
    }

    if (!forceDelete && StringUtils.equals(id, DEFAULT_INGEST_POLICY_ID)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "不允许删除默认规则");
    }

    List<NetworkPolicyDO> networkPolicy = networkPolicyDao.queryNetworkPolicyByPolicyId(id,
        FpcCmsConstants.APPLIANCE_NETWORK_POLICY_INGEST);
    if (!forceDelete && CollectionUtils.isNotEmpty(networkPolicy)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "规则已被网络使用，不能删除");
    }

    // 删除捕获规则
    ingestPolicyDao.deleteIngestPolicy(id, operatorId);

    // 删除规则和网络的关联
    networkPolicyDao.deleteNetworkPolicyByPolicyId(id,
        FpcCmsConstants.APPLIANCE_NETWORK_POLICY_INGEST);

    IngestPolicyBO ingestPolicyBO = new IngestPolicyBO();
    BeanUtils.copyProperties(exist, ingestPolicyBO);

    // 下发到直属fpc和cms
    List<Map<String, Object>> messageBodys = Lists
        .newArrayList(ingestPolicy2MessageBody(exist, FpcCmsConstants.SYNC_ACTION_DELETE));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_INGESTPOLICY, null);

    return ingestPolicyBO;
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
  public Map<String, List<String>> getFullConfigurationIds(String deviceType, String serialNo,
      Date beforeTime) {
    // 所有下级设备均生效，无需判断serialNo
    Map<String, List<String>> map = Maps.newHashMapWithExpectedSize(1);
    map.put(FpcCmsConstants.MQ_TAG_INGESTPOLICY, ingestPolicyDao.queryIngestPolicyIds(false));

    return map;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.subordinate.MQAssignmentService#getFullConfigurations(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Tuple3<Boolean, List<Map<String, Object>>, Message> getFullConfigurations(
      String deviceType, String serialNo, String tag) {
    if (StringUtils.equals(tag, FpcCmsConstants.MQ_TAG_INGESTPOLICY)) {

      List<IngestPolicyDO> ingestPolicyList = ingestPolicyDao.queryIngestPolicys();

      // 当前捕获过滤规则列表
      List<Map<String, Object>> list = ingestPolicyList.stream().map(
          ingestPolicy -> ingestPolicy2MessageBody(ingestPolicy, FpcCmsConstants.SYNC_ACTION_ADD))
          .collect(Collectors.toList());

      return Tuples.of(true, list, MQMessageHelper.EMPTY);
    } else {
      return Tuples.of(true, Lists.newArrayListWithCapacity(0), MQMessageHelper.EMPTY);
    }
  }

  private Map<String, Object> ingestPolicy2MessageBody(IngestPolicyDO ingestPolicyDO,
      String action) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", ingestPolicyDO.getId());
    map.put("name", ingestPolicyDO.getName());
    map.put("defaultAction", ingestPolicyDO.getDefaultAction());
    map.put("deduplication", ingestPolicyDO.getDeduplication());
    map.put("exceptTuple", ingestPolicyDO.getExceptTuple());
    map.put("exceptBpf", ingestPolicyDO.getExceptBpf());
    map.put("action", action);

    return map;
  }

  /********************************************************************************************************
   * 接收模块
   *******************************************************************************************************/

  @PostConstruct
  public void init() {
    MQReceiveServiceImpl.register(this, Lists.newArrayList(FpcCmsConstants.MQ_TAG_INGESTPOLICY));
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

    int syncTotalCount = messages.stream().mapToInt(item -> syncIngestPolicy(item)).sum();
    LOGGER.info("current sync ingestPolicy total: {}.", syncTotalCount);

    return syncTotalCount;
  }

  private int syncIngestPolicy(Map<String, Object> messageBody) {
    int syncCount = 0;

    String assignId = MapUtils.getString(messageBody, "id");
    if (StringUtils.isBlank(assignId)) {
      return syncCount;
    }

    String action = MapUtils.getString(messageBody, "action");

    // 下发的规则与本地规则名称冲突，添加后缀
    String name = MapUtils.getString(messageBody, "name");
    IngestPolicyDO existName = ingestPolicyDao.queryIngestPolicyByName(name);
    if (StringUtils.equals(action, FpcCmsConstants.SYNC_ACTION_ADD)
        && StringUtils.isNotBlank(existName.getId())) {
      name = name + "_" + System.currentTimeMillis();
    }

    IngestPolicyBO ingestPolicyBO = new IngestPolicyBO();
    ingestPolicyBO.setId(assignId);
    ingestPolicyBO.setAssignId(assignId);
    ingestPolicyBO.setName(name);
    ingestPolicyBO.setDefaultAction(MapUtils.getString(messageBody, "defaultAction"));
    ingestPolicyBO.setExceptBpf(MapUtils.getString(messageBody, "exceptBpf"));
    ingestPolicyBO.setDeduplication(MapUtils.getString(messageBody, "deduplication"));
    ingestPolicyBO.setExceptTuple(MapUtils.getString(messageBody, "exceptTuple"));
    ingestPolicyBO.setDescription(CMS_ASSIGNMENT);

    IngestPolicyDO exist = ingestPolicyDao
        .queryIngestPolicyByAssignId(ingestPolicyBO.getAssignId());

    int addCount = 0;
    int modifyCount = 0;
    int deleteCount = 0;
    try {
      switch (action) {
        case FpcCmsConstants.SYNC_ACTION_ADD:
        case FpcCmsConstants.SYNC_ACTION_MODIFY:
          if (StringUtils.isNotBlank(exist.getId())) {
            updateIngestPolicy(exist.getId(), ingestPolicyBO, CMS_ASSIGNMENT);
            modifyCount++;
          } else {
            saveIngestPolicy(ingestPolicyBO, CMS_ASSIGNMENT);
            addCount++;
          }
          break;
        case FpcCmsConstants.SYNC_ACTION_DELETE:
          deleteIngestPolicy(exist.getId(), CMS_ASSIGNMENT, true);
          deleteCount++;
          break;
        default:
          break;
      }

      // 本次同步数据量
      syncCount = addCount + modifyCount + deleteCount;

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("current sync ingestPolicy status: [add: {}, modify: {}, delete: {}]",
            addCount, modifyCount, deleteCount);
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

    int clearCount = 0;
    List<String> ingestPolicyIds = ingestPolicyDao.queryIngestPolicyIds(onlyLocal);
    for (String ingestPolicyId : ingestPolicyIds) {
      // 已被网络使用的规则不能删除
      List<NetworkPolicyDO> networkPolicy = networkPolicyDao.queryNetworkPolicyByPolicyId(
          ingestPolicyId, FpcCmsConstants.APPLIANCE_NETWORK_POLICY_INGEST);
      if (CollectionUtils.isNotEmpty(networkPolicy)) {
        LOGGER.warn("规则已被网络使用，不能删除");
        continue;
      }

      try {
        deleteIngestPolicy(ingestPolicyId, CMS_ASSIGNMENT, true);
        clearCount++;
      } catch (BusinessException e) {
        LOGGER.warn("delete ingestPolicy failed. error msg: {}", e.getMessage());
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
    return ingestPolicyDao.queryAssignIngestPolicyIds(beforeTime).stream().map(e -> e.getAssignId())
        .collect(Collectors.toList());
  }

}
