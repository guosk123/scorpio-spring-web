package com.machloop.fpc.npm.appliance.service.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
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

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.fpc.cms.common.FpcCmsConstants;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.cms.service.SyncConfigurationService;
import com.machloop.fpc.manager.cms.service.impl.MQAssignmentServiceImpl;
import com.machloop.fpc.manager.helper.MQMessageHelper;
import com.machloop.fpc.npm.appliance.bo.MetricSettingBO;
import com.machloop.fpc.npm.appliance.dao.MetricSettingDao;
import com.machloop.fpc.npm.appliance.data.MetricSettingDO;
import com.machloop.fpc.npm.appliance.service.MetricSettingService;

/**
 * @author guosk
 *
 * create at 2021年4月6日, fpc-manager
 */
@Order(4)
@Service
public class MetricSettingServiceImpl implements MetricSettingService, SyncConfigurationService {

  private static final String METRIC_SETTING_PREFIX = "metric.setting.";

  private static final int BATCH_SAVE_SIZE = 1000;

  private static final Logger LOGGER = LoggerFactory.getLogger(MetricSettingServiceImpl.class);

  @Autowired
  private MetricSettingDao metricSettingDao;

  @Autowired
  private GlobalSettingService globalSettingService;

  @Autowired
  private DictManager dictManager;

  /**
   * @see com.machloop.fpc.npm.appliance.service.MetricSettingService#queryMetricSettings(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public List<MetricSettingBO> queryMetricSettings(String sourceType, String networkId,
      String serviceId, String packetFileId) {
    List<MetricSettingDO> metricSettings = metricSettingDao.queryMetricSettings(sourceType,
        networkId, serviceId, packetFileId);

    List<MetricSettingBO> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    metricSettings.forEach(metricSettingDO -> {
      MetricSettingBO metricSettingBO = new MetricSettingBO();
      BeanUtils.copyProperties(metricSettingDO, metricSettingBO);
      metricSettingBO.setUpdateTime(DateUtils.toStringISO8601(metricSettingDO.getUpdateTime()));

      result.add(metricSettingBO);
    });

    return result;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.MetricSettingService#saveDefaultMetricSettings(java.util.List, java.lang.String)
   */
  @Override
  public int saveDefaultMetricSettings(List<MetricSettingBO> metricSettingBOList,
      String operatorId) {
    // 获取指标默认值
    Map<String, String> defaultMetricSettings = dictManager.getBaseDict()
        .getItemMap("appliance_metric_setting").entrySet().stream().map(entry -> {
          String key = METRIC_SETTING_PREFIX + StringUtils.replace(entry.getKey(), "_", ".");
          entry.setValue(globalSettingService.getValue(key, true));
          return entry;
        }).collect(Collectors.toMap(Entry::getKey, Entry::getValue));

    int success = 0;
    List<MetricSettingDO> metricSettingDOList = Lists
        .newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    for (MetricSettingBO metricSettingBO : metricSettingBOList) {
      for (Entry<String, String> defaultMetricSetting : defaultMetricSettings.entrySet()) {
        MetricSettingDO metricSettingDO = new MetricSettingDO();
        metricSettingDO.setSourceType(metricSettingBO.getSourceType());
        metricSettingDO.setNetworkId(metricSettingBO.getNetworkId());
        metricSettingDO
            .setServiceId(StringUtils.defaultIfBlank(metricSettingBO.getServiceId(), ""));
        metricSettingDO.setMetric(defaultMetricSetting.getKey());
        metricSettingDO.setValue(defaultMetricSetting.getValue());
        metricSettingDO.setOperatorId(operatorId);
        metricSettingDOList.add(metricSettingDO);

        if (metricSettingDOList.size() == BATCH_SAVE_SIZE) {
          success += metricSettingDao.batchSaveMetricSetting(metricSettingDOList);
          metricSettingDOList = Lists.newArrayListWithCapacity(BATCH_SAVE_SIZE);
        }
      }
    }

    if (CollectionUtils.isNotEmpty(metricSettingDOList)) {
      success += metricSettingDao.batchSaveMetricSetting(metricSettingDOList);
    }

    return success;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.MetricSettingService#saveMetricSettings(java.util.List, java.lang.String)
   */
  @Override
  public int saveMetricSettings(List<MetricSettingBO> metricSettings, String operatorId) {
    int success = 0;

    List<MetricSettingDO> metricSettingDOList = metricSettings.stream().map(metricSettingBO -> {
      MetricSettingDO metricSettingDO = new MetricSettingDO();
      BeanUtils.copyProperties(metricSettingBO, metricSettingDO);
      metricSettingDO.setOperatorId(operatorId);

      return metricSettingDO;
    }).collect(Collectors.toList());

    if (CollectionUtils.isNotEmpty(metricSettingDOList)) {
      success = metricSettingDao.batchSaveMetricSetting(metricSettingDOList);
    }

    return success;
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.MetricSettingService#updateMetricSettings(java.util.List, java.lang.String)
   */
  @Override
  public void updateMetricSettings(List<MetricSettingBO> metricSettings, String operatorId) {
    Set<String> metricSettingKeys = dictManager.getBaseDict().getItemMap("appliance_metric_setting")
        .keySet();

    metricSettings.forEach(metricSettingBO -> {
      if (!metricSettingKeys.contains(metricSettingBO.getMetric())) {
        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "不支持的指标配置");
      }
      MetricSettingDO metricSettingDO = new MetricSettingDO();
      BeanUtils.copyProperties(metricSettingBO, metricSettingDO);
      metricSettingDO.setNetworkId(StringUtils.defaultIfBlank(metricSettingBO.getNetworkId(), ""));
      metricSettingDO.setServiceId(StringUtils.defaultIfBlank(metricSettingBO.getServiceId(), ""));
      metricSettingDO
          .setPacketFileId(StringUtils.defaultIfBlank(metricSettingBO.getPacketFileId(), ""));
      metricSettingDO.setOperatorId(operatorId);

      metricSettingDao.updateMetricSetting(metricSettingDO);
    });
  }

  /**
   * @see com.machloop.fpc.npm.appliance.service.MetricSettingService#deleteMetricSetting(java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public int deleteMetricSetting(String sourceType, String networkId, String serviceId,
      String packetFileId) {
    return metricSettingDao.deleteMetricSetting(sourceType, networkId, serviceId, packetFileId);
  }

  @PostConstruct
  public void init() {
    MQAssignmentServiceImpl.register(this,
        Lists.newArrayList(FpcCmsConstants.MQ_TAG_METRICSETTING));
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

    int syncTotalCount = messages.stream().mapToInt(item -> syncMetricSetting(item)).sum();
    LOGGER.info("current sync metricSetting total: {}.", syncTotalCount);

    return syncTotalCount;
  }

  private int syncMetricSetting(Map<String, Object> messageBody) {
    int syncCount = 0;

    String networkId = MapUtils.getString(messageBody, "networkId");
    String serviceId = MapUtils.getString(messageBody, "serviceId");
    if (StringUtils.isBlank(networkId) && StringUtils.isBlank(serviceId)) {
      return syncCount;
    }

    String action = MapUtils.getString(messageBody, "action");
    MetricSettingBO metricSettingBO = new MetricSettingBO();
    metricSettingBO.setSourceType(MapUtils.getString(messageBody, "sourceType"));
    metricSettingBO.setNetworkId(networkId);
    metricSettingBO.setServiceId(serviceId);
    metricSettingBO.setPacketFileId(MapUtils.getString(messageBody, "packetFileId"));
    metricSettingBO.setMetric(MapUtils.getString(messageBody, "metric"));
    metricSettingBO.setValue(MapUtils.getString(messageBody, "value"));

    int modifyCount = 0;
    try {
      if (!action.equals(FpcCmsConstants.SYNC_ACTION_MODIFY)) {
        return syncCount;
      }
      updateMetricSettings(Lists.newArrayList(metricSettingBO), CMS_ASSIGNMENT);
      LogHelper.auditAssignOperate(
          globalSettingService.getValue(ManagerConstants.GLOBAL_SETTING_CMS_IP),
          metricSettingBO.toAuditLogText(LogHelper.AUDIT_LOG_ACTION_UPDATE));
      modifyCount++;
      // 本次同步数据量
      syncCount = modifyCount;

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("current sync metricSetting status: [syncCount: {}]", syncCount);
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
    return 0;
  }

  /**
   * @see com.machloop.fpc.manager.cms.service.SyncConfigurationService#getAssignConfigurationIds(java.util.Date)
   */
  @Override
  public List<String> getAssignConfigurationIds(String tags, Date beforeTime) {
    // metricSetting的全量下发由子网和业务来完成
    return Lists.newArrayListWithExpectedSize(0);
  }

}
