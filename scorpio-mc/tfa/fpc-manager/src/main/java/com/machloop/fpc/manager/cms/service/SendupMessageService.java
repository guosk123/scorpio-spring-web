package com.machloop.fpc.manager.cms.service;

import java.util.Date;

public interface SendupMessageService {

  void sendupMessage(String serialNumber, String messageType, Date metricDatetime);

  void resendMessage(String serialNumber, String messageType, Date metricDatetime);

  void sendAllApplianceMessage(String serialNumber, Date metricDatetime);

  void deleteExpireSendupMessage(Date expireTime);
}
