package com.machloop.fpc.cms.center.broker.service.subordinate;

import java.util.List;
import java.util.Set;

import com.machloop.fpc.cms.grpc.CentralProto.ReplyMessage;
import com.machloop.fpc.cms.grpc.CentralProto.RequestMessage;

public interface RegistryHeartbeatService {

  ReplyMessage registerHeartbeat(RequestMessage requestMessage);

  List<MQAssignmentService> getMQAssignmentServices();

  /**
   * 全量下发配置
   * @param deviceType 目标设备类型
   * @param serialNumber 目标设备序号
   * @param tags 指定下发的tag，未指定则下发全部配置
   */
  void assignmentFullConfigurations(String deviceType, String serialNumber, Set<String> tags);

}
