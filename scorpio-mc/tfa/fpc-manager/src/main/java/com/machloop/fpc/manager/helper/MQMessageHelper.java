package com.machloop.fpc.manager.helper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.rocketmq.common.message.Message;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import com.machloop.alpha.common.util.JsonHelper;

/**
 * 
 * @author guosk
 *
 * create at 2021年11月24日, fpc-manager
 */
public class MQMessageHelper {

  private static final Logger LOGGER = LoggerFactory.getLogger(MQMessageHelper.class);

  public static final Message EMPTY = new Message();

  private MQMessageHelper() {
    throw new IllegalStateException("Utility class");
  }

  public static Message convertToMessage(Object object, String topic) {
    return convertToMessage(object, topic, "", null);
  }

  public static Message convertToMessage(Object object, String topic, String tag) {
    return convertToMessage(object, topic, tag, null);
  }

  public static Message convertToMessage(Object object, String topic, String tag,
      Map<String, String> properties) {
    if (object == null) {
      LOGGER.warn("object is null, convert failed.");
      return new Message();
    }

    Message message = new Message(topic, tag,
        JsonHelper.serialize(object).getBytes(StandardCharsets.UTF_8));

    if (MapUtils.isNotEmpty(properties)) {
      properties.forEach((key, value) -> {
        if (!StringUtils.isAnyBlank(key, value)) {
          message.putUserProperty(key, value);
        }
      });
    }

    return message;
  }

  public static Message convertToMessage(InputStream inputStream, String topic, String tag,
      Map<String, String> properties) throws IOException {
    if (inputStream == null) {
      LOGGER.warn("inputStream is null, convert failed.");
      return new Message();
    }

    Message message = new Message(topic, tag, ByteStreams.toByteArray(inputStream));

    if (MapUtils.isNotEmpty(properties)) {
      properties.forEach((key, value) -> {
        if (!StringUtils.isAnyBlank(key, value)) {
          message.putUserProperty(key, value);
        }
      });
    }

    return message;
  }

  public static <T> T convertToObject(Message message, Class<T> clazz) {
    if (message == null || message.getBody().length == 0) {
      return null;
    }

    return JsonHelper.deserialize(new String(message.getBody(), StandardCharsets.UTF_8), clazz);
  }

  public static Map<String, Object> convertToMap(Message message) {
    if (message == null || message.getBody().length == 0) {
      return Maps.newHashMapWithExpectedSize(0);
    }

    return JsonHelper.deserialize(new String(message.getBody(), StandardCharsets.UTF_8),
        new TypeReference<Map<String, Object>>() {
        });
  }

}
