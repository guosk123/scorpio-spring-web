package com.machloop.fpc.cms.center.broker.service.local;

import java.util.Date;

import com.machloop.fpc.cms.grpc.CentralProto.SendupRequest;

/**
 * @author guosk
 *
 * create at 2021年12月8日, fpc-cms-center
 */
public interface SendupMessageService {

  void sendupMessage(String deviceType, String serialNumber, String messageType,
      Date metricDatetime, int interval, int latency);

  void resendMessage(String deviceType, String serialNumber, String messageType,
      Date metricDatetime);

  /**
   * 将下级设备上报的消息发送给本机上级，并更新结果(系统状态，日志告警)
   * @param request
   */
  void sendupMessage(SendupRequest request);

  void sendAllApplianceMessage(String serialNumber, Date metricDatetime);

  void deleteExpireSendupMessage(Date expireTime);

}
