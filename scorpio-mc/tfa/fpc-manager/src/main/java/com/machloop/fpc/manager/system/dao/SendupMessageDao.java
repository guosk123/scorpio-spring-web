package com.machloop.fpc.manager.system.dao;

import java.util.Date;
import java.util.List;

import com.machloop.fpc.manager.system.data.SendupMessageDO;

public interface SendupMessageDao {

  /**
   * 查询上报统计表中统计开始时间在两参数之间的所有数据
   * @param startTime
   * @param endTime
   * @return
   */
  List<SendupMessageDO> querySendupMessages(String type, String result, Date startTime,
      Date endTime);

  void saveSendupMessage(SendupMessageDO sendupMessageDO);

  void updateSendupMessageResults(List<String> messageIdList, String result);

  void updateSendupMessageResult(String messageId, String result);
  
  int deleteExpireMessage(Date expireTime);

}
