package com.machloop.fpc.manager.cms.listener;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext;
import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.common.message.MessageExt;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.machloop.fpc.cms.common.FpcCmsConstants;
import com.machloop.fpc.manager.cms.service.MQAssignmentService;
import com.machloop.fpc.manager.helper.RocketMQHelper;
import com.machloop.fpc.manager.system.service.LicenseService;

/**
 * @author guosk
 * 
 * 消费cms发送的消息
 *
 * create at 2021年11月19日, rocketmq
 */
@Component
public class MqConsumeMsgListener implements MessageListenerOrderly {

  private static final Logger LOGGER = LoggerFactory.getLogger(MqConsumeMsgListener.class);

  private static final int MESSAGE_RESEND_TIMES = 3;
  private static final int RESET_OFFSET_WAITING_TIME = 5 * 1000;

  @Autowired
  private MQAssignmentService assignmentService;

  @Autowired
  private RocketMQHelper rocketMQHelper;

  @Autowired
  private LicenseService licenseService;

  /**
   * @see org.apache.rocketmq.client.consumer.listener.MessageListenerOrderly#consumeMessage(java.util.List, org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyContext)
   */
  @Override
  public ConsumeOrderlyStatus consumeMessage(List<MessageExt> msgs, ConsumeOrderlyContext context) {
    if (CollectionUtils.isEmpty(msgs)) {
      // 消息为空，直接返回成功
      return ConsumeOrderlyStatus.SUCCESS;
    }

    // 消费消息
    for (MessageExt messageExt : msgs) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("current consume msg： {}", messageExt.toString());
      }

      // 消息重发次数
      if (messageExt.getReconsumeTimes() == MESSAGE_RESEND_TIMES) {
        // 消息重发{MESSAGE_RESEND_TIMES}次，则直接返回成功
        return ConsumeOrderlyStatus.SUCCESS;
      }

      // 判断下发配置是否对本设备有效
      Map<String, String> properties = messageExt.getProperties();
      if (MapUtils.isNotEmpty(properties)) {
        String vaildDeviceType = properties.get("vaildDeviceType");
        String serialNumber = properties.get("vaildDeviceSerialNumber");

        if (StringUtils.isNotBlank(vaildDeviceType) && StringUtils.isNotBlank(serialNumber)
            && (!StringUtils.equals(vaildDeviceType, FpcCmsConstants.DEVICE_TYPE_TFA)
                || !StringUtils.contains(serialNumber, licenseService.queryDeviceSerialNumber()))) {
          return ConsumeOrderlyStatus.SUCCESS;
        }
      }

      if (StringUtils.equals(messageExt.getTopic(), FpcCmsConstants.MQ_TOPIC_CMS_FULL_ASSIGNMENT)) {
        // 接收到全量Topic,停止订阅增量Topic
        rocketMQHelper.getConsumer().unsubscribe(FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT);
      }

      // 上级CMS下发消息 对上级CMS下发的消息进行处理（业务内部保证幂等性）
      ConsumeOrderlyStatus consumeOrderlyStatus = assignmentService.assignConfiguration(messageExt);

      String offsetStr = messageExt.getProperty("offset");
      long offset = StringUtils.isNotBlank(offsetStr) ? Long.parseLong(offsetStr) : 0;
      if (StringUtils.equals(messageExt.getTopic(), FpcCmsConstants.MQ_TOPIC_CMS_FULL_ASSIGNMENT)
          && offset > 0) {
        // 全量下发结束，重置增量Topic消费位点
        LOGGER.info("end of full assign, begin to reset increment assign offset, new offset is {}.",
            offset);

        DefaultMQAdminExt mqAdminExt = rocketMQHelper.getDefaultMQAdminExt();
        try {
          Map<MessageQueue,
              Long> resetResult = mqAdminExt.resetOffsetByTimestamp(
                  FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
                  rocketMQHelper.getConsumer().getConsumerGroup(), offset, true);
          LOGGER.info("reset offset success: {}", resetResult);
        } catch (RemotingException | MQBrokerException | InterruptedException
            | MQClientException e) {
          LOGGER.warn("reset offset error.", e);
        } finally {
          mqAdminExt.shutdown();
        }

        // 等待指定时长后重新订阅增量Topic
        try {
          Thread.sleep(RESET_OFFSET_WAITING_TIME);
        } catch (InterruptedException e) {
          LOGGER.warn("waiting for the process to be interrupted.", e);
        }

        try {
          // 重新订阅增量Topic
          rocketMQHelper.getConsumer().subscribe(FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT, "*");
        } catch (MQClientException e) {
          LOGGER.warn("subscribe topic: {} failed, reconnect rocketMQ.",
              FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT, e);
          // 重连
          rocketMQHelper.initCmsRocketMQ();
        }
      }

      return consumeOrderlyStatus;
    }

    return ConsumeOrderlyStatus.SUCCESS;
  }

}
