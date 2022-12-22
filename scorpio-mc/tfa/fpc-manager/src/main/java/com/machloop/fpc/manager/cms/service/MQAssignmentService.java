package com.machloop.fpc.manager.cms.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.rocketmq.client.consumer.listener.ConsumeOrderlyStatus;
import org.apache.rocketmq.common.message.MessageExt;

/**
 * @author guosk
 *
 * create at 2021年11月24日, fpc-manager
 */
public interface MQAssignmentService {

  ConsumeOrderlyStatus assignConfiguration(MessageExt message);

  Map<String, List<String>> getAssignConfiguration(Date beforeTime);

  /**
   * 清除本地配置
   * @param beforeTime 该时间之前的配置
   * @param onlyLocal 是否只清除本地创建的配置（配置包含本地创建和上级下发）
   * @return
   */
  Map<String, Integer> clearLocalConfiguration(boolean onlyLocal, Date beforeTime);

}
