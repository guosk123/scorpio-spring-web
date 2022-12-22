package com.machloop.fpc.cms.center.broker.service.subordinate;

import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.client.exception.MQBrokerException;
import org.apache.rocketmq.client.exception.MQClientException;
import org.apache.rocketmq.client.producer.DefaultMQProducer;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageQueue;
import org.apache.rocketmq.remoting.exception.RemotingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.helper.HotPropertiesHelper;
import com.machloop.fpc.cms.center.helper.MQMessageHelper;
import com.machloop.fpc.cms.common.FpcCmsConstants;

import reactor.util.function.Tuple3;

/**
 * @author guosk
 *
 * create at 2021年11月24日, fpc-cms-center
 */
public interface MQAssignmentService {

  static final Logger LOGGER = LoggerFactory.getLogger(MQAssignmentService.class);

  static final Map<String,
      MessageQueue> QUEUE_MAP = Maps.newHashMapWithExpectedSize(Constants.COL_DEFAULT_SIZE);

  static final String LOCKED_TAG = "locked";

  DefaultMQProducer getProducer();

  List<String> getTags();

  /**
   * 获取所有有效配置ID集合(将下发的配置主键ID根据tag汇总，如果是文件则取文件的MD5值)
   * @return <T1:TAG,T2:ID集合>
   */
  Map<String, List<String>> getFullConfigurationIds(String deviceType, String serialNumber,
      Date beforeTime);

  /**
   * 获取所有有效配置信息 (Tuple内的内容不能为null)
   * @return T1:返回内容（true:T2;false:T3）;T2:消息体;T3:封装好的消息
   */
  Tuple3<Boolean, List<Map<String, Object>>, Message> getFullConfigurations(String deviceType,
      String serialNumber, String tag);

  /**
   * 下发全量配置(1、下级设备首次注册；2、下级配置异常)
   * @param deviceType 设备类型
   * @param serialNumber 设备序列号
   * @param tags 本次需要下发的tag
   * @param offset 全量下发结束后，增量下发应重置的消费位点
   */
  @SuppressWarnings("unchecked")
  default void assignmentFullConfiguration(String deviceType, String serialNumber, Set<String> tags,
      long offset) {
    Set<String> currentTags = Sets.newHashSet(getTags());
    if (CollectionUtils.isNotEmpty(tags)) {
      Collection<String> intersection = CollectionUtils.intersection(getTags(), tags);
      if (CollectionUtils.isNotEmpty(intersection)) {
        currentTags = Sets.newHashSet(intersection.iterator());
      }
    }

    Iterator<String> iterator = currentTags.iterator();
    while (iterator.hasNext()) {
      String tag = iterator.next();

      // 消息属性（本次下发消息仅对单个设备生效）
      Map<String, String> properties = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
      if (StringUtils.isNotBlank(deviceType) && StringUtils.isNotBlank(serialNumber)) {
        properties.put("vaildDeviceType", deviceType);
        properties.put("vaildDeviceSerialNumber", serialNumber);
      }
      if (!iterator.hasNext() && offset != 0) {
        properties.put("offset", String.valueOf(offset));
      }

      // 封装消息
      Tuple3<Boolean, List<Map<String, Object>>,
          Message> configurations = getFullConfigurations(deviceType, serialNumber, tag);
      Message message = null;
      if (configurations.getT1()) {
        // 消息体
        Map<String,
            Object> messageBody = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
        messageBody.put("batch", true);
        messageBody.put("data", configurations.getT2());

        message = MQMessageHelper.convertToMessage(messageBody,
            FpcCmsConstants.MQ_TOPIC_CMS_FULL_ASSIGNMENT, tag, properties);
      } else {
        message = configurations.getT3();
        for (Entry<String, String> propertie : properties.entrySet()) {
          message.putUserProperty(propertie.getKey(), propertie.getValue());
        }
      }

      assignmentConfiguration(message, FpcCmsConstants.MQ_TOPIC_CMS_FULL_ASSIGNMENT);
    }
  }

  /**
   * TODO 注意：有些配置因为数据量较大（如安全规则可能存在几十万数据），不适合使用消息队列，此时我们可以将配置封装成文件，调用上传REST接口下发配置；
   * 但需要注意即时没有使用MQ，我们也要往MQ中发送一条消息（空消息，不影响业务逻辑），保证配置类的下发在MQ中都有对应的消息，保证逻辑的严谨。
   * 
   * 下发配置
   * @param mesaageBodys 消息体
   * @param topic 消息发送的topic
   * @param tag 消息所属tag
   * @param properties 消息的属性
   */
  default void assignmentConfiguration(List<Map<String, Object>> mesaageBodys, String topic,
      String tag, Map<String, String> properties) {
    synchronized (LOCKED_TAG) {
      if (CollectionUtils.isEmpty(mesaageBodys)) {
        return;
      }

      // 获取下发队列（相同topic使用的队列唯一，保证顺序）
      MessageQueue messageQueue = QUEUE_MAP.get(topic);
      if (messageQueue == null) {
        messageQueue = new MessageQueue(topic,
            HotPropertiesHelper.getProperty(FpcCmsConstants.ROCKETMQ_BROKER_NAME), 0);
        QUEUE_MAP.put(topic, messageQueue);
      }

      try {
        // 构造消息
        List<Message> messages = mesaageBodys.stream().map(
            mesaageBody -> MQMessageHelper.convertToMessage(mesaageBody, topic, tag, properties))
            .collect(Collectors.toList());

        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("current assignment message: {}", messages);
        }

        // TODO 测试时发现发送批量消息，TAG会被修改，暂未找到原因，暂时只发送单消息
        for (Message message : messages) {
          getProducer().send(message, messageQueue);
        }
      } catch (MQClientException | RemotingException | MQBrokerException | InterruptedException e) {
        LOGGER.warn("mq send msg error.", e);
        throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "消息通道异常");
      }
    }
  }

  /**
   * TODO 注意：有些配置因为数据量较大（如安全规则可能存在几十万数据），不适合使用消息队列，此时我们可以将配置封装成文件，调用上传REST接口下发配置；
   * 但需要注意即时没有使用MQ，我们也要往MQ中发送一条消息（空消息，不影响业务逻辑），保证配置类的下发在MQ中都有对应的消息，保证逻辑的严谨。
   * 
   * 下发配置
   * @param message MQ消息对象
   * @param topic 消息发送的topic
   */
  default void assignmentConfiguration(Message message, String topic) {
    synchronized (LOCKED_TAG) {
      // 获取下发队列（相同topic使用的队列唯一，保证顺序）
      MessageQueue messageQueue = QUEUE_MAP.get(topic);
      if (messageQueue == null) {
        messageQueue = new MessageQueue(topic,
            HotPropertiesHelper.getProperty(FpcCmsConstants.ROCKETMQ_BROKER_NAME), 0);
        QUEUE_MAP.put(topic, messageQueue);
      }

      try {
        if (LOGGER.isDebugEnabled()) {
          LOGGER.debug("current assignment message: {}", message);
        }

        getProducer().send(message, messageQueue);
      } catch (MQClientException | RemotingException | MQBrokerException | InterruptedException e) {
        LOGGER.warn("mq send msg error.", e);
        throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "消息通道异常");
      }
    }
  }

}
