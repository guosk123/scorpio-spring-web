package com.machloop.fpc.cms.center.helper;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.webapp.WebappConstants;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.broker.listener.MqConsumeSuperiorMsgListener;
import com.machloop.fpc.cms.center.system.service.SystemMetricService;
import com.machloop.fpc.cms.common.FpcCmsConstants;

import io.netty.channel.DefaultChannelId;

/**
 * @author guosk
 *
 * 连接上级CMS部署的rocketmq
 * create at 2021年11月23日, fpc-cms-center
 */
@Component
public class RemoteRocketMQHelper {

  public static final Logger LOGGER = LoggerFactory.getLogger(RemoteRocketMQHelper.class);

  @Value("${rocketmq.namesrv.port}")
  private int port;

  @Value("${rocketmq.admin.connection.timeout.ms}")
  private int adminConnectionTimeoutMs;

  @Value("${rocketmq.producer.max.message.size}")
  private int producerMaxMessageSize;

  @Value("${rocketmq.producer.send.timeout.ms}")
  private int producerSendMsgTimeoutMs;

  @Value("${rocketmq.consumer.consume-message-batch-size}")
  private int consumeMessageBatchMaxSize;

  @Value("${rocketmq.producer.group-name}")
  private String producerGroupName;

  @Value("${rocketmq.consumer.group-name}")
  private String consumerGroupName;

  private DefaultMQPushConsumer consumer;
  private DefaultMQProducer producer;

  @Autowired
  private GlobalSettingService globalSettingService;

  @Autowired
  private SystemMetricService systemMetricService;

  @Autowired
  private MqConsumeSuperiorMsgListener mqConsumeMsgListener;

  @PostConstruct
  public synchronized void initCmsRocketMQ() {
    String parentCmsState = globalSettingService.getValue(CenterConstants.GLOBAL_SETTING_CMS_STATE);
    String parentCmsIp = globalSettingService.getValue(CenterConstants.GLOBAL_SETTING_CMS_IP);

    if (StringUtils.equals(parentCmsState, Constants.BOOL_YES)
        && StringUtils.isNotBlank(parentCmsIp)) {
      shutdown();
      initRocketMQConsumer(parentCmsIp);
      initRocketMQProducer(parentCmsIp);
    }
  }

  @PreDestroy
  public synchronized void shutdown() {
    if (consumer != null) {
      consumer.shutdown();
    }

    if (producer != null) {
      producer.shutdown();
    }
  }

  public DefaultMQPushConsumer getConsumer() {
    if (consumer == null) {
      initCmsRocketMQ();
    }

    return consumer;
  }

  public DefaultMQProducer getProducer() {
    if (producer == null) {
      initCmsRocketMQ();
    }

    return producer;
  }

  public DefaultMQAdminExt getDefaultMQAdminExt() {
    DefaultMQAdminExt mqAdminExt = new DefaultMQAdminExt(adminConnectionTimeoutMs);
    try {
      mqAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));
      String parentCmsIp = globalSettingService.getValue(CenterConstants.GLOBAL_SETTING_CMS_IP);
      mqAdminExt.setNamesrvAddr(StringUtils.joinWith(":", parentCmsIp, port));
      mqAdminExt.start();
    } catch (MQClientException e) {
      LOGGER.error("rocketmq admin connection failed! namesrvAddr:{}.", mqAdminExt.getNamesrvAddr(),
          e);
    }

    return mqAdminExt;
  }

  public SendResult sendMsg(Message message) {
    try {
      if (message == null) {
        LOGGER.warn("message is null.");
        return new SendResult();
      }

      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug("current sendup message: {}", message);
      }

      return getProducer().send(message);
    } catch (MQClientException | RemotingException | MQBrokerException | InterruptedException e) {
      LOGGER.warn("mq send msg error.", e);
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "消息通道异常");
    }
  }

  private void initRocketMQConsumer(String parentCmsIp) throws RuntimeException {
    // 初始化通道，防止超时
    DefaultChannelId.newInstance();

    try {
      // 因消费模式为<集群>，为保证每个消费者都能够消费，所以每个设备都属于不同的消费组
      String consumerGroup = StringUtils.joinWith("_", consumerGroupName, systemMetricService
          .queryDeviceCustomInfo().get(WebappConstants.GLOBAL_SETTING_DEVICE_ID));
      consumer = new DefaultMQPushConsumer(consumerGroup);
      consumer.setNamesrvAddr(StringUtils.joinWith(":", parentCmsIp, port));
      consumer.registerMessageListener(mqConsumeMsgListener);
      // 默认从上次消费位置开始消费，一种是上次消费的位置未过期，则消费从上次中止的位置进行；一种是上次消费位置已经过期，则从当前队列第一条消息开始消费
      consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
      // 消费模式为集群，集群模式每个设备的消费进度在broker中存储
      consumer.setMessageModel(MessageModel.CLUSTERING);
      // 设置一次消费消息的条数，默认为1条
      consumer.setConsumeMessageBatchMaxSize(consumeMessageBatchMaxSize);
      // 订阅topic，连接上级，消费来源为上级CMS
      consumer.subscribe(FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT, "*");// 增量topic
      consumer.subscribe(FpcCmsConstants.MQ_TOPIC_CMS_FULL_ASSIGNMENT, "*");// 全量topic
      consumer.start();

      LOGGER.info("cms consumer is start! namesrvAddr:{}, consumerGroup:{}, topics:{}.",
          consumer.getNamesrvAddr(), consumer.getConsumerGroup(),
          StringUtils.joinWith(",", FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
              FpcCmsConstants.MQ_TOPIC_CMS_FULL_ASSIGNMENT));
    } catch (MQClientException e) {
      LOGGER.error("cms consumer start failed! namesrvAddr:{}, consumerGroup:{}, topics:{}.",
          consumer.getNamesrvAddr(), consumer.getConsumerGroup(),
          StringUtils.joinWith(",", FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
              FpcCmsConstants.MQ_TOPIC_CMS_FULL_ASSIGNMENT),
          e);
    } catch (Exception e) {
      LOGGER.error(
          "cms consumer start failed! namesrvAddr:{}, consumerGroup:{}, topics:{}. error msg:{}",
          consumer.getNamesrvAddr(), consumer.getConsumerGroup(),
          StringUtils.joinWith(",", FpcCmsConstants.MQ_TOPIC_CMS_ASSIGNMENT,
              FpcCmsConstants.MQ_TOPIC_CMS_FULL_ASSIGNMENT),
          e.getMessage());
    }
  }

  private void initRocketMQProducer(String parentCmsIp) {
    // 初始化通道，防止超时
    DefaultChannelId.newInstance();

    try {
      String producerGroup = StringUtils.joinWith("_", producerGroupName, systemMetricService
          .queryDeviceCustomInfo().get(WebappConstants.GLOBAL_SETTING_DEVICE_ID));
      producer = new DefaultMQProducer(producerGroup);
      producer.setNamesrvAddr(StringUtils.joinWith(":", parentCmsIp, port));
      // 单个消息最大限制
      producer.setMaxMessageSize(
          producerMaxMessageSize * Constants.BLOCK_DEFAULT_SIZE * Constants.BLOCK_DEFAULT_SIZE);
      // 发送消息超时时间
      producer.setSendMsgTimeout(producerSendMsgTimeoutMs);
      producer.start();

      LOGGER.info("cms producer is start! namesrvAddr:{}, producerGroup:{}.",
          producer.getNamesrvAddr(), producer.getProducerGroup());
    } catch (MQClientException e) {
      LOGGER.error(String.format("cms producer start error! namesrvAddr:{}, producerGroup:{}.",
          producer.getNamesrvAddr(), producer.getProducerGroup(), e));
    } catch (Exception e) {
      LOGGER.error(
          String.format("cms producer start error! namesrvAddr:{}, producerGroup:{}. error msg:{}",
              producer.getNamesrvAddr(), producer.getProducerGroup(), e.getMessage()));
    }
  }

}
