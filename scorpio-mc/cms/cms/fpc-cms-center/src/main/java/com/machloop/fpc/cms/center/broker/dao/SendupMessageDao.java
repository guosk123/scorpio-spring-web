package com.machloop.fpc.cms.center.broker.dao;

import java.util.Date;
import java.util.List;

import com.machloop.fpc.cms.center.broker.data.SendupMessageDO;

public interface SendupMessageDao {

  /**
   * 查询指定设备在指定时间内上报失败的数据
   * @param deviceType
   * @param deviceSerialNumber
   * @param type
   * @param result
   * @param startTime
   * @param endTime
   * @return
   */
  List<SendupMessageDO> querySendupMessages(String deviceType, String deviceSerialNumber,
      String type, String result, Date startTime, Date endTime);

  void saveSendupMessage(SendupMessageDO sendupMessageDO);

  void updateSendupMessageResults(List<String> messageIdList, String result);

  void updateSendupMessageResult(String messageId, String result);

  int deleteExpireMessage(Date expireTime);

}
