package com.machloop.fpc.cms.center.broker.service.subordinate;

import java.util.List;

import com.machloop.fpc.cms.center.broker.bo.CollectMetricBO;
import com.machloop.fpc.cms.grpc.CentralProto.SendupReply;
import com.machloop.fpc.cms.grpc.CentralProto.SendupRequest;

public interface CollectMetricService {

  List<CollectMetricBO> queryCollectMetrics(String deviceType, String serialNumber, String type,
      String startTimeStr, String endTimeStr);

  SendupReply processMessage(SendupRequest sendupRequest);

  void metricSendupMessage(SendupRequest sendupRequest);

  void metricResendMessage(SendupRequest sendupRequest);

}
