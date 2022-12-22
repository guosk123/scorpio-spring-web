package com.machloop.fpc.cms.center.broker.service.local.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.fpc.cms.center.broker.service.local.MQReceiveService;
import com.machloop.fpc.cms.center.broker.service.local.SyncConfigurationService;
import com.machloop.fpc.cms.common.FpcCmsConstants;

/**
 * @author guosk
 *
 * create at 2021年12月8日, fpc-cms-center
 */
@Service
public class MQReceiveServiceImpl implements MQReceiveService {

  private static final Logger LOGGER = LoggerFactory.getLogger(MQReceiveServiceImpl.class);

  private static final Map<String, Set<SyncConfigurationService>> configurationServiceMap = Maps
      .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

  /**
   * 删除本机配置时，根据list内顺序删除，保证被依赖的配置先被删除
   */
  private static final List<String> TAGS = Lists.newArrayList(FpcCmsConstants.MQ_TAG_SSO,
      FpcCmsConstants.MQ_TAG_ALERT, FpcCmsConstants.MQ_TAG_INGESTPOLICY,
      FpcCmsConstants.MQ_TAG_EXTERNALRECEIVER, FpcCmsConstants.MQ_TAG_SENDRULE,
      FpcCmsConstants.MQ_TAG_SENDPOLICY, FpcCmsConstants.MQ_TAG_SERVICE,
      FpcCmsConstants.MQ_TAG_SERVICE_LINK, FpcCmsConstants.MQ_TAG_GEOCUSTOM,
      FpcCmsConstants.MQ_TAG_GEOIPSETTING, FpcCmsConstants.MQ_TAG_CUSTOMAPPLICATION,
      FpcCmsConstants.MQ_TAG_CUSTOMSUBCATEGORY, FpcCmsConstants.MQ_TAG_CUSTOMCATEGORY,
      FpcCmsConstants.MQ_TAG_HOSTGROUP, FpcCmsConstants.MQ_TAG_LOGICALSUBNET,
      FpcCmsConstants.MQ_TAG_FILTERRULE, FpcCmsConstants.MQ_TAG_SURICATA,
      FpcCmsConstants.MQ_TAG_SURICATA_RULE_CLASSTYPE, FpcCmsConstants.MQ_TAG_DOMAIN_WHITE_LIST);

  /**
   * @see com.machloop.fpc.cms.center.broker.service.local.MQReceiveService.cms.service.MQAssignmentService#receiveConfiguration(org.apache.rocketmq.common.message.MessageExt)
   */
  @Override
  public ConsumeOrderlyStatus receiveConfiguration(MessageExt message) {
    if (message.getBody().length == 0) {
      return ConsumeOrderlyStatus.SUCCESS;
    }

    int size = 0;
    Set<SyncConfigurationService> configServices = configurationServiceMap.get(message.getTags());
    if (CollectionUtils.isNotEmpty(configServices)) {
      for (SyncConfigurationService configService : configServices) {
        size += configService.syncConfiguration(message);
      }
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("current sync configuration: {}, total size: {}.", message.getTags(), size);
    }

    if (size < 0) {
      LOGGER.info("current sync configuration size is less than 0...");
      return ConsumeOrderlyStatus.SUSPEND_CURRENT_QUEUE_A_MOMENT;
    }

    return ConsumeOrderlyStatus.SUCCESS;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.local.MQReceiveService.cms.service.MQAssignmentService#getAssignConfiguration(java.util.Date)
   */
  @Override
  public Map<String, List<String>> getAssignConfiguration(Date beforeTime) {
    Map<String, List<String>> map = Maps.newHashMapWithExpectedSize(configurationServiceMap.size());
    configurationServiceMap.forEach((tag, services) -> {
      List<String> ids = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
      services.forEach(service -> {
        ids.addAll(service.getAssignConfigurationIds(tag, beforeTime));
      });

      map.put(tag, ids);
    });

    return map;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.local.MQReceiveService#clearLocalConfiguration(java.util.Date, boolean)
   */
  @Override
  public Map<String, Integer> clearLocalConfiguration(boolean onlyLocal, Date beforeTime) {

    Map<String, Integer> clearCountMap = TAGS.stream()
        .filter(tag -> configurationServiceMap.containsKey(tag))
        .collect(Collectors.toMap(tag -> tag,
            tag -> configurationServiceMap.get(tag).stream()
                .mapToInt(service -> service.clearLocalConfiguration(tag, onlyLocal, beforeTime))
                .sum()));
    LOGGER.info("current clear local configuration result: {}",
        JsonHelper.serialize(clearCountMap));

    return clearCountMap;
  }

  public static void register(SyncConfigurationService syncConfigurationService,
      List<String> tags) {
    tags.forEach(tag -> {
      Set<SyncConfigurationService> services = configurationServiceMap.getOrDefault(tag,
          Sets.newHashSetWithExpectedSize(Constants.COL_DEFAULT_SIZE));
      services.add(syncConfigurationService);

      configurationServiceMap.put(tag, services);
    });
  }

}
