package com.machloop.fpc.cms.center.appliance.service.impl;

import java.util.Date;
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
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.fpc.cms.center.appliance.bo.CustomTimeBO;
import com.machloop.fpc.cms.center.appliance.dao.CustomTimeDao;
import com.machloop.fpc.cms.center.appliance.data.CustomTimeDO;
import com.machloop.fpc.cms.center.appliance.service.CustomTimeService;
import com.machloop.fpc.cms.center.broker.service.local.SyncConfigurationService;
import com.machloop.fpc.cms.center.broker.service.local.impl.MQReceiveServiceImpl;
import com.machloop.fpc.cms.center.broker.service.subordinate.MQAssignmentService;
import com.machloop.fpc.cms.center.helper.MQMessageHelper;
import com.machloop.fpc.cms.common.FpcCmsConstants;

import reactor.util.function.Tuple3;
import reactor.util.function.Tuples;

/**
 * @author minjiajun
 *
 * create at 2022年7月18日, fpc-cms-center
 */
@Service
public class CustomTimeServiceImpl
    implements CustomTimeService, MQAssignmentService, SyncConfigurationService {

  private static final List<String> TAGS = Lists.newArrayList(FpcCmsConstants.MQ_TAG_CUSTOMTIME);

  @Autowired
  CustomTimeDao customTimeDao;

  @Autowired
  private ApplicationContext context;

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.CustomTimeService#queryCustomTimes(java.lang.String)
   */
  @Override
  public List<CustomTimeBO> queryCustomTimes(String type) {

    List<CustomTimeDO> customTimeDOList = customTimeDao.queryCustomTimes(type);
    List<CustomTimeBO> customTimeList = Lists.newArrayListWithCapacity(customTimeDOList.size());

    customTimeDOList.forEach(customTimeDO -> {
      CustomTimeBO customTimeBO = new CustomTimeBO();
      BeanUtils.copyProperties(customTimeDO, customTimeBO);
      customTimeList.add(customTimeBO);
    });
    return customTimeList;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.CustomTimeService#queryCustomTime(java.lang.String)
   */
  @Override
  public CustomTimeBO queryCustomTime(String id) {

    CustomTimeDO customTime = customTimeDao.queryCustomTime(id);

    CustomTimeBO customTimeBO = new CustomTimeBO();
    BeanUtils.copyProperties(customTime, customTimeBO);
    return customTimeBO;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.CustomTimeService#saveCustomTime(com.machloop.fpc.cms.center.appliance.bo.CustomTimeBO, java.lang.String)
   */
  @Override
  public CustomTimeBO saveCustomTime(CustomTimeBO customTimeBO, String operatorId) {

    CustomTimeDO existName = customTimeDao.queryCustomTimeByName(customTimeBO.getName());
    if (StringUtils.isNotBlank(existName.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "自定义时间名称已经存在");
    }

    CustomTimeDO customTimeDO = new CustomTimeDO();
    BeanUtils.copyProperties(customTimeBO, customTimeDO);
    customTimeDO.setOperatorId(operatorId);

    CustomTimeDO customTime = customTimeDao.saveCustomTime(customTimeDO);

    // 下发到直属fpc和cms
    List<Map<String, Object>> messageBodys = Lists
        .newArrayList(customTime2MessageBody(customTime, FpcCmsConstants.SYNC_ACTION_ADD));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_CUSTOMTIME, null);
    return customTimeBO;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.CustomTimeService#updateCustomTime(java.lang.String, com.machloop.fpc.cms.center.appliance.bo.CustomTimeBO, java.lang.String)
   */
  @Override
  public CustomTimeBO updateCustomTime(String id, CustomTimeBO customTimeBO, String operatorId) {

    CustomTimeDO exist = customTimeDao.queryCustomTime(id);
    if (StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "自定义时间不存在");
    }

    CustomTimeDO customTimeDO = new CustomTimeDO();
    BeanUtils.copyProperties(customTimeBO, customTimeDO);
    customTimeDO.setOperatorId(operatorId);
    customTimeDao.updateCustomTime(customTimeDO);

    // 下发到直属fpc和cms
    List<Map<String, Object>> messageBodys = Lists
        .newArrayList(customTime2MessageBody(customTimeDO, FpcCmsConstants.SYNC_ACTION_MODIFY));
    assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
        FpcCmsConstants.MQ_TAG_CUSTOMTIME, null);
    return queryCustomTime(id);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.CustomTimeService#batchDeleteCustomTime(java.util.List, java.lang.String)
   */
  @Override
  public int batchDeleteCustomTime(List<String> idList, String operatorId) {

    for (String id : idList) {
      CustomTimeDO exist = customTimeDao.queryCustomTime(id);
      if (StringUtils.isBlank(exist.getId())) {
        throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "自定义时间不存在");
      }
    }

    int result = customTimeDao.deleteCustomTime(idList, operatorId);
    for (String id : idList) {
      // 下发到直属fpc和cms
      List<Map<String, Object>> messageBodys = Lists.newArrayList(customTime2MessageBody(
          customTimeDao.queryCustomTime(id), FpcCmsConstants.SYNC_ACTION_DELETE));
      assignmentConfiguration(messageBodys, FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
          FpcCmsConstants.MQ_TAG_CUSTOMTIME, null);
    }

    return result;
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
   * 
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
    map.put(FpcCmsConstants.MQ_TAG_CUSTOMTIME, customTimeDao.queryCustomTimeIds(false));

    return map;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.subordinate.MQAssignmentService#getFullConfigurations(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Tuple3<Boolean, List<Map<String, Object>>, Message> getFullConfigurations(
      String deviceType, String serialNo, String tag) {

    if (StringUtils.equals(tag, FpcCmsConstants.MQ_TAG_CUSTOMTIME)) {
      List<CustomTimeDO> customTimeList = customTimeDao.queryCustomTimes(null);

      // 当前地址组列表
      List<Map<String, Object>> list = customTimeList.stream()
          .map(
              customTimeDO -> customTime2MessageBody(customTimeDO, FpcCmsConstants.SYNC_ACTION_ADD))
          .collect(Collectors.toList());

      return Tuples.of(true, list, MQMessageHelper.EMPTY);
    } else {
      return Tuples.of(true, Lists.newArrayListWithCapacity(0), MQMessageHelper.EMPTY);
    }
  }

  private Map<String, Object> customTime2MessageBody(CustomTimeDO customTimeDO, String action) {

    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    map.put("id", customTimeDO.getId());
    map.put("name", customTimeDO.getName());
    map.put("type", customTimeDO.getType());
    map.put("period", customTimeDO.getPeriod());
    map.put("customTimeSetting", customTimeDO.getCustomTimeSetting());
    map.put("action", action);

    return map;
  }

  /********************************************************************************************************
   * 接收模块
   *******************************************************************************************************/

  @PostConstruct
  public void init() {
    MQReceiveServiceImpl.register(this, Lists.newArrayList(FpcCmsConstants.MQ_TAG_CUSTOMTIME));
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

    int syncTotalCount = messages.stream().mapToInt(item -> syncCustomTime(item)).sum();
    LOGGER.info("current sync customTime total: {}.", syncTotalCount);

    return syncTotalCount;
  }

  private int syncCustomTime(Map<String, Object> messageBody) {

    int syncCount = 0;

    String assignId = MapUtils.getString(messageBody, "id");
    if (StringUtils.isBlank(assignId)) {
      return syncCount;
    }

    String action = MapUtils.getString(messageBody, "action");

    CustomTimeBO customTimeBO = new CustomTimeBO();
    customTimeBO.setAssignId(assignId);
    customTimeBO.setId(assignId);
    customTimeBO.setType(MapUtils.getString(messageBody, "type"));
    customTimeBO.setName(MapUtils.getString(messageBody, "name"));
    customTimeBO.setPeriod(MapUtils.getString(messageBody, "period"));
    customTimeBO.setCustomTimeSetting(MapUtils.getString(messageBody, "customTimeSetting"));

    CustomTimeDO exist = customTimeDao.queryCustomTimeByAssignId(customTimeBO.getAssignId());

    int addCount = 0;
    int modifyCount = 0;
    int deleteCount = 0;
    try {
      switch (action) {
        case FpcCmsConstants.SYNC_ACTION_ADD:
        case FpcCmsConstants.SYNC_ACTION_MODIFY:
          if (StringUtils.isNotBlank(exist.getId())) {
            updateCustomTime(exist.getId(), customTimeBO, CMS_ASSIGNMENT);
            modifyCount++;
          } else {
            saveCustomTime(customTimeBO, CMS_ASSIGNMENT);
            addCount++;
          }
          break;
        case FpcCmsConstants.SYNC_ACTION_DELETE:
          batchDeleteCustomTime(Lists.newArrayList(exist.getId()), CMS_ASSIGNMENT);
          deleteCount++;
          break;
        default:
          break;
      }

      // 本次同步数据量
      syncCount = addCount + modifyCount + deleteCount;

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("current sync customTime status: [add: {}, modify: {}, delete: {}]", addCount,
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
    List<String> customTimeIds = customTimeDao.queryCustomTimeIds(onlyLocal);
    for (String customTimeId : customTimeIds) {
      try {
        batchDeleteCustomTime(Lists.newArrayList(customTimeId), CMS_ASSIGNMENT);
        clearCount++;
      } catch (BusinessException e) {
        LOGGER.warn("delete customTime failed. error msg: {}", e.getMessage());
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
    return customTimeDao.queryAssignCustomTimeIds(beforeTime).stream().map(e -> e.getAssignId())
        .collect(Collectors.toList());
  }

}
