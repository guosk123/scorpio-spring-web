package com.machloop.fpc.cms.center.broker.service.subordinate.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.cms.center.appliance.service.AssignmentActionService;
import com.machloop.fpc.cms.center.broker.data.DeviceStatusDO;
import com.machloop.fpc.cms.center.broker.service.subordinate.AssignmentService;
import com.machloop.fpc.cms.center.broker.service.subordinate.CollectMetricService;
import com.machloop.fpc.cms.center.broker.service.subordinate.DeviceStatusService;
import com.machloop.fpc.cms.center.broker.service.subordinate.RegistryHeartbeatService;
import com.machloop.fpc.cms.grpc.CentralProto.AssignReply;
import com.machloop.fpc.cms.grpc.CentralProto.AssignRequest;
import com.machloop.fpc.cms.grpc.CentralProto.AssignResult;
import com.machloop.fpc.cms.grpc.CentralProto.ReplyMessage;
import com.machloop.fpc.cms.grpc.CentralProto.RequestMessage;
import com.machloop.fpc.cms.grpc.CentralProto.SendupReply;
import com.machloop.fpc.cms.grpc.CentralProto.SendupRequest;
import com.machloop.fpc.cms.grpc.GrpcServerGrpc.GrpcServerImplBase;

import io.grpc.stub.StreamObserver;

@Service
public class CentralApiServiceImpl extends GrpcServerImplBase {

  private static final Logger LOGGER = LoggerFactory.getLogger(CentralApiServiceImpl.class);

  @Autowired
  private RegistryHeartbeatService registryHeartbeatService;

  @Autowired
  private AssignmentService assignmentService;

  @Autowired
  private CollectMetricService collectMetricService;

  @Autowired
  private AssignmentActionService assignmentActionService;

  @Autowired
  private DeviceStatusService deviceStatusService;

  /**
   * @see com.machloop.fpc.cms.grpc.GrpcServerGrpc.GrpcServerImplBase#registerHeartbeat(com.machloop.fpc.cms.grpc.CentralProto.RequestMessage, io.grpc.stub.StreamObserver)
   */
  @Override
  public void registerHeartbeat(RequestMessage request,
      StreamObserver<ReplyMessage> responseObserver) {
    responseObserver.onNext(registryHeartbeatService.registerHeartbeat(request));
    responseObserver.onCompleted();
  }

  /**
   * @see com.machloop.fpc.cms.grpc.GrpcServerGrpc.GrpcServerImplBase#assignChannel(io.grpc.stub.StreamObserver)
   */
  @Override
  public StreamObserver<AssignRequest> assignChannel(StreamObserver<AssignReply> responseObserver) {
    return new StreamObserver<AssignRequest>() {

      @Override
      public void onNext(AssignRequest assignRequest) {
        LOGGER.debug("receive to assignment task result, assignRequest is {}", assignRequest);

        // 刷新交互时间及本次交互时延
        refreshInteractiveTime(assignRequest.getDeviceType(), assignRequest.getSerialNumber(),
            assignRequest.getTimestamp(), DateUtils.now().getTime());

        List<AssignResult> assignResultList = assignRequest.getResultList();
        if (CollectionUtils.isEmpty(assignResultList)) {
          // 首次连接注册下发通道
          assignmentService.registerAssignmentChannel(assignRequest.getDeviceType(),
              assignRequest.getSerialNumber(), responseObserver);
          LOGGER.info(
              "receive assignedId wait assignment task, device type: {}, serialNumber is {}.",
              assignRequest.getDeviceType(), assignRequest.getSerialNumber());
        } else {
          assignmentActionService.updateAssignmentActions(assignResultList);
        }
      }

      @Override
      public void onError(Throwable t) {
        LOGGER.warn("failed to assignment task, cannot connect to client." + t);
      }

      @Override
      public void onCompleted() {
        // do nothing
      }
    };
  }

  /**
   * @see com.machloop.fpc.cms.grpc.GrpcServerGrpc.GrpcServerImplBase#sendupChannel(com.machloop.fpc.cms.grpc.CentralProto.SendupRequest, io.grpc.stub.StreamObserver)
   */
  @Override
  public void sendupChannel(SendupRequest request, StreamObserver<SendupReply> responseObserver) {
    SendupReply messageReply = collectMetricService.processMessage(request);

    if (messageReply != null) {
      responseObserver.onNext(messageReply);
      responseObserver.onCompleted();
    }

    // 对上报信息统计
    /*if (StringUtils.equalsAny(request.getMessageType(), FpcCmsConstants.SENDUP_TYPE_SYSTEM_METRIC,
        FpcCmsConstants.SENDUP_TYPE_LOG_ALARM)) {
      collectMetricService.metricSendupMessage(request);
    } else if (StringUtils.equalsAny(request.getMessageType(),
        FpcCmsConstants.RESEND_TYPE_SYSTEM_METRIC, FpcCmsConstants.RESEND_TYPE_LOG_ALARM)) {
      collectMetricService.metricResendMessage(request);
    }*/
  }

  private void refreshInteractiveTime(String deviceType, String serialNumber,
      long lastInteractiveTime, long responseTime) {
    DeviceStatusDO deviceStatus = deviceStatusService.queryDeviceStatus(deviceType, serialNumber);
    deviceStatus.setDeviceType(deviceType);
    deviceStatus.setSerialNumber(serialNumber);
    deviceStatus.setLastInteractiveTime(new Date(lastInteractiveTime));
    deviceStatus.setLastInteractiveLatency(Math.abs(responseTime - lastInteractiveTime));
    deviceStatusService.refreshDeviceStatus(deviceStatus);
  }

}
