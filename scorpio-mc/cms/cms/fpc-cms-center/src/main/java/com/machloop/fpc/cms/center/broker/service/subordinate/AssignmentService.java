package com.machloop.fpc.cms.center.broker.service.subordinate;

import com.machloop.fpc.cms.grpc.CentralProto.AssignReply;

import io.grpc.stub.StreamObserver;

public interface AssignmentService {

  void registerAssignmentChannel(String deviceType, String serialNumber,
      StreamObserver<AssignReply> responseObserver);

  void assignmentTask();

  void addAssignmentQueue(String assignmentType);

}
