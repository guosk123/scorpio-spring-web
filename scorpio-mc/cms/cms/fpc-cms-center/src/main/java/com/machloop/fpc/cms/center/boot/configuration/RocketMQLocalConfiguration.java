package com.machloop.fpc.cms.center.boot.configuration;

import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.consumer.DefaultMQPushConsumer;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.consumer.ConsumeFromWhere;
import org.apache.rocketmq.common.protocol.heartbeat.MessageModel;
import org.apache.rocketmq.tools.admin.DefaultMQAdminExt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.machloop.alpha.common.Constants;
import com.machloop.alpha.webapp.WebappConstants;
import com.machloop.fpc.cms.center.broker.listener.MqConsumeSubordinateMsgListener;
import com.machloop.fpc.cms.center.system.service.SystemMetricService;
import com.machloop.fpc.cms.common.FpcCmsConstants;

import io.netty.channel.DefaultChannelId;

/**
 * @author guosk
 *
 * 连接本地部署的rocketmq
 * create at 2021年11月19日, rocketmq
 */
@Configuration
public class RocketMQLocalConfiguration {

  public static final Logger LOGGER = LoggerFactory.getLogger(RocketMQLocalConfiguration.class);

  @Value("${rocketmq.namesrv.host}")
  private String host;

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

  @Autowired
  private MqConsumeSubordinateMsgListener mqConsumeMsgListener;

  @Autowired
  private SystemMetricService systemMetricService;

  @Bean
  public DefaultMQPushConsumer getRocketMQConsumer() throws RuntimeException {

    // 初始化通道，防止超时
    DefaultChannelId.newInstance();

    String consumerGroup = StringUtils.joinWith("_", consumerGroupName,
        systemMetricService.queryDeviceCustomInfo().get(WebappConstants.GLOBAL_SETTING_DEVICE_ID));
    DefaultMQPushConsumer consumer = new DefaultMQPushConsumer(consumerGroup);
    try {
      consumer.setNamesrvAddr(StringUtils.joinWith(":", host, port));
      consumer.registerMessageListener(mqConsumeMsgListener);
      // 默认从上次消费位置开始消费，一种是上次消费的位置未过期，则消费从上次中止的位置进行；一种是上次消费位置已经过期，则从当前队列第一条消息开始消费
      consumer.setConsumeFromWhere(ConsumeFromWhere.CONSUME_FROM_LAST_OFFSET);
      // 消费模式为集群，集群模式每个设备的消费进度在broker中存储
      consumer.setMessageModel(MessageModel.CLUSTERING);
      // 设置一次消费消息的条数，默认为1条
      consumer.setConsumeMessageBatchMaxSize(consumeMessageBatchMaxSize);
      // 订阅topic，本机mq配置时，消费来源包含：探针、下级CMS
      consumer.subscribe(FpcCmsConstants.MQ_TOPIC_FPC_SENDUP, "*");
      consumer.subscribe(FpcCmsConstants.MQ_TOPIC_CMS_SENDUP, "*");
      consumer.start();

      LOGGER.info("local consumer is start! namesrvAddr:{}, consumerGroup:{}, topics:{}.",
          consumer.getNamesrvAddr(), consumer.getConsumerGroup(), StringUtils.joinWith(",",
              FpcCmsConstants.MQ_TOPIC_FPC_SENDUP, FpcCmsConstants.MQ_TOPIC_CMS_SENDUP));
    } catch (MQClientException e) {
      LOGGER.error("local consumer start failed! namesrvAddr:{}, consumerGroup:{}, topics:{}.",
          consumer.getNamesrvAddr(), consumer.getConsumerGroup(), StringUtils.joinWith(",",
              FpcCmsConstants.MQ_TOPIC_FPC_SENDUP, FpcCmsConstants.MQ_TOPIC_CMS_SENDUP),
          e);
    }

    return consumer;
  }

  @Bean
  public DefaultMQProducer getRocketMQProducer() {
    // 初始化通道，防止超时
    DefaultChannelId.newInstance();

    String producerGroup = StringUtils.joinWith("_", producerGroupName,
        systemMetricService.queryDeviceCustomInfo().get(WebappConstants.GLOBAL_SETTING_DEVICE_ID));
    DefaultMQProducer producer = new DefaultMQProducer(producerGroup);
    try {
      producer.setNamesrvAddr(StringUtils.joinWith(":", host, port));
      // 单个消息最大限制
      producer.setMaxMessageSize(
          producerMaxMessageSize * Constants.BLOCK_DEFAULT_SIZE * Constants.BLOCK_DEFAULT_SIZE);
      // 发送消息超时时间
      producer.setSendMsgTimeout(producerSendMsgTimeoutMs);
      producer.start();

      LOGGER.info("local producer is start! namesrvAddr:{}, producerGroup:{}.",
          producer.getNamesrvAddr(), producer.getProducerGroup());
    } catch (MQClientException e) {
      LOGGER.error(String.format("local producer start error! namesrvAddr:{}, producerGroup:{}.",
          producer.getNamesrvAddr(), producer.getProducerGroup(), e));
    }

    return producer;
  }

  @Bean
  public DefaultMQAdminExt getDefaultMQAdminExt() {
    DefaultMQAdminExt mqAdminExt = new DefaultMQAdminExt(adminConnectionTimeoutMs);
    try {
      mqAdminExt.setInstanceName(Long.toString(System.currentTimeMillis()));
      mqAdminExt.setNamesrvAddr(StringUtils.joinWith(":", host, port));
      mqAdminExt.start();
    } catch (MQClientException e) {
      LOGGER.error("rocketmq admin connection failed! namesrvAddr:{}.", mqAdminExt.getNamesrvAddr(),
          e);
    }

    return mqAdminExt;
  }

}
