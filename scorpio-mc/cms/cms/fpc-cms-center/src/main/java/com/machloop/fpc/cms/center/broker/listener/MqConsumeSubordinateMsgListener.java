package com.machloop.fpc.cms.center.broker.listener;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.fpc.cms.center.broker.service.subordinate.MQAssignmentService;
import com.machloop.fpc.cms.center.broker.service.subordinate.RegistryHeartbeatService;
import com.machloop.fpc.cms.center.helper.MQMessageHelper;
import com.machloop.fpc.cms.common.FpcCmsConstants;

/**
 * @author guosk
 * 
 * 消费下级设备发送的消息
 *
 * create at 2021年11月19日, rocketmq
 */
@Component
public class MqConsumeSubordinateMsgListener implements MessageListenerOrderly {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(MqConsumeSubordinateMsgListener.class);

  private static final int MESSAGE_RESEND_TIMES = 3;

  @Value("${rocketmq.namesrv.host}")
  private String host;

  @Value("${rocketmq.namesrv.port}")
  private int port;

  @Autowired
  private RegistryHeartbeatService registryHeartbeatService;

  /**
   * @see org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly#consumeMessage(java.util.List, org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext)
   */
  @Override
  public ConsumeOrderlyStatus consumeMessage(final List<MessageExt> msgs,
      ConsumeOrderlyContext context) {
    if (CollectionUtils.isEmpty(msgs)) {
      // 消息为空，直接返回成功
      return ConsumeOrderlyStatus.SUCCESS;
    }

    // 消费消息
    for (MessageExt messageExt : msgs) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("current consume topic:{}, tags:{}, reconsumeTimes:{}, msg： {}.",
            messageExt.getTopic(), messageExt.getTags(), messageExt.getReconsumeTimes(),
            MQMessageHelper.convertToMap(messageExt));
      }

      // 消息重发次数
      if (messageExt.getReconsumeTimes() == MESSAGE_RESEND_TIMES) {
        // 消息重发{MESSAGE_RESEND_TIMES}次，则直接返回成功
        return ConsumeOrderlyStatus.SUCCESS;
      }

      if (StringUtils.equalsAny(messageExt.getTopic(), FpcCmsConstants.MQ_TOPIC_FPC_SENDUP,
          FpcCmsConstants.MQ_TOPIC_CMS_SENDUP)) {
        // 下级探针上报消息 | 下级CMS上报消息
        if (StringUtils.equals(messageExt.getTags(), FpcCmsConstants.MQ_TAG_SIGNATURE)) {
          return verifySignature(messageExt);
        }
      }
    }

    return ConsumeOrderlyStatus.SUCCESS;
  }

  /**
   * 校验配置签名
   * @param message
   */
  private ConsumeOrderlyStatus verifySignature(Message message) {
    // 下级设备配置签名
    Map<String, Object> messageBody = MQMessageHelper.convertToMap(message);
    String deviceType = MapUtils.getString(messageBody, "deviceType");
    String deviceSerialNumber = MapUtils.getString(messageBody, "serialNumber");
    Date signatureTime = new Date(MapUtils.getLongValue(messageBody, "signatureTime", 0L));
    String tfaSignature = MapUtils.getString(messageBody, "signature");

    // 如果本次接收到的签名时间与当前时间相差超过10分钟，则忽略
    if (signatureTime
        .before(DateUtils.beforeSecondDate(DateUtils.now(), Constants.FIVE_MINUTE_SECONDS * 2))) {
      LOGGER.info(
          "The signature [{}: {}] received this time has expired (10 minutes before the current time), skipped!",
          deviceType, deviceSerialNumber);
      return ConsumeOrderlyStatus.SUCCESS;
    }

    // 下级设备本地有效配置
    Map<String, List<String>> tfaConfigs = JsonHelper.deserialize(tfaSignature,
        new TypeReference<Map<String, List<String>>>() {
        });

    // 校验配置是否一致
    Map<String,
        List<String>> missingConfigs = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    for (MQAssignmentService mQAssignmentService : registryHeartbeatService
        .getMQAssignmentServices()) {
      Map<String, List<String>> fullConfigurationIds = mQAssignmentService
          .getFullConfigurationIds(deviceType, deviceSerialNumber, signatureTime);
      for (Entry<String, List<String>> tagConfig : fullConfigurationIds.entrySet()) {
        if (CollectionUtils.isEmpty(tagConfig.getValue())) {
          continue;
        }

        if (CollectionUtils.isEmpty(tfaConfigs.get(tagConfig.getKey()))) {
          missingConfigs.put(tagConfig.getKey(), tagConfig.getValue());
          continue;
        }

        tagConfig.getValue().removeAll(tfaConfigs.get(tagConfig.getKey()));
        if (CollectionUtils.isNotEmpty(tagConfig.getValue())) {
          missingConfigs.put(tagConfig.getKey(), tagConfig.getValue());
        }
      }
    }

    if (MapUtils.isEmpty(missingConfigs)) {
      LOGGER.info("The configuration remains consistent, deviceType:{}, serialNumber:{}.",
          deviceType, deviceSerialNumber);
    } else {
      LOGGER.info(
          "config inconsistent, assign missing configuration, deviceType:{}, serialNumber:{}, missing config: {}.",
          deviceType, deviceSerialNumber, JsonHelper.serialize(missingConfigs));
      // 下级探针设备配置不一致，下发全量配置
      registryHeartbeatService.assignmentFullConfigurations(deviceType, deviceSerialNumber,
          missingConfigs.keySet());
    }

    return ConsumeOrderlyStatus.SUCCESS;
  }

}
