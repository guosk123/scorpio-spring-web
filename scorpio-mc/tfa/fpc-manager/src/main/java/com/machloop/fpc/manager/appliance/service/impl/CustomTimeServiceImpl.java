package com.machloop.fpc.manager.appliance.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.fpc.cms.common.FpcCmsConstants;
import com.machloop.fpc.manager.appliance.bo.CustomTimeBO;
import com.machloop.fpc.manager.appliance.dao.CustomTimeDao;
import com.machloop.fpc.manager.appliance.data.CustomTimeDO;
import com.machloop.fpc.manager.appliance.service.CustomTimeService;
import com.machloop.fpc.manager.cms.service.SyncConfigurationService;
import com.machloop.fpc.manager.cms.service.impl.MQAssignmentServiceImpl;
import com.machloop.fpc.manager.helper.MQMessageHelper;

/**
 * @author minjiajun
 *
 * create at 2022年6月9日, fpc-manager
 */
@Service
public class CustomTimeServiceImpl implements CustomTimeService, SyncConfigurationService {

  @Autowired
  CustomTimeDao customTimeDao;

  private static final Logger LOGGER = LoggerFactory.getLogger(CustomTimeServiceImpl.class);

  /**
   * @see com.machloop.fpc.manager.appliance.service.CustomTimeService#queryCustomTimes()
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
   * @see com.machloop.fpc.manager.appliance.service.CustomTimeService#queryCustomTime(java.lang.String)
   */
  @Override
  public CustomTimeBO queryCustomTime(String id) {

    CustomTimeDO customTime = customTimeDao.queryCustomTime(id);

    CustomTimeBO customTimeBO = new CustomTimeBO();
    BeanUtils.copyProperties(customTime, customTimeBO);
    return customTimeBO;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.CustomTimeService#saveCustomTime(com.machloop.fpc.manager.appliance.bo.CustomTimeBO, java.lang.String)
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

    customTimeDao.saveCustomTime(customTimeDO);


    return customTimeBO;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.CustomTimeService#updateCustomTime(java.lang.String, com.machloop.fpc.manager.appliance.bo.CustomTimeBO, java.lang.String)
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

    return queryCustomTime(id);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.CustomTimeService#deleteCustomTime(java.lang.String, java.lang.String)
   */
  @Override
  public List<CustomTimeBO> batchDeleteCustomTime(List<String> idList, String operatorId) {

    List<
        CustomTimeBO> customTimeBOList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    for (String id : idList) {
      CustomTimeDO exist = customTimeDao.queryCustomTime(id);
      if (StringUtils.isBlank(exist.getId())) {
        throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "自定义时间不存在");
      }
      CustomTimeBO customTimeBO = new CustomTimeBO();
      BeanUtils.copyProperties(exist, customTimeBO);
      customTimeBOList.add(customTimeBO);
    }
    customTimeDao.deleteCustomTime(idList, operatorId);
    return customTimeBOList;
  }

  @PostConstruct
  public void init() {
    MQAssignmentServiceImpl.register(this, Lists.newArrayList(FpcCmsConstants.MQ_TAG_CUSTOMTIME));
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

    int syncTotalCount = messages.stream().mapToInt(item -> synCustomTime(item)).sum();
    LOGGER.info("current sync customTime total: {}.", syncTotalCount);

    return syncTotalCount;
  }

  private int synCustomTime(Map<String, Object> messageBody) {

    int syncCount = 0;

    String customTimeInCmsId = MapUtils.getString(messageBody, "id");
    if (StringUtils.isBlank(customTimeInCmsId)) {
      return syncCount;
    }

    String action = MapUtils.getString(messageBody, "action");

    CustomTimeBO customTimeBO = new CustomTimeBO();
    customTimeBO.setId(customTimeInCmsId);
    customTimeBO.setCustomTimeInCmsId(customTimeInCmsId);
    customTimeBO.setType(MapUtils.getString(messageBody, "type"));
    customTimeBO.setName(MapUtils.getString(messageBody, "name"));
    customTimeBO.setPeriod(MapUtils.getString(messageBody, "period"));
    customTimeBO.setCustomTimeSetting(MapUtils.getString(messageBody, "customTimeSetting"));

    CustomTimeBO exist = queryCustomTime(customTimeBO.getCustomTimeInCmsId());

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
   * @see com.machloop.fpc.manager.cms.service.SyncConfigurationService#clearLocalConfiguration(java.lang.String, java.util.Date, boolean)
   */
  @Override
  public int clearLocalConfiguration(String tag, boolean onlyLocal, Date beforeTime) {
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
   * @see com.machloop.fpc.manager.cms.service.SyncConfigurationService#getAssignConfigurationIds(java.util.Date)
   */
  @Override
  public List<String> getAssignConfigurationIds(String tags, Date beforeTime) {
    return customTimeDao.queryAssignCustomTimeIds(beforeTime);
  }
}
