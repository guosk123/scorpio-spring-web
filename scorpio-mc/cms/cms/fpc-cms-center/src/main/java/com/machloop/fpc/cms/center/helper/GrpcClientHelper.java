package com.machloop.fpc.cms.center.helper;

import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.machloop.fpc.cms.center.broker.service.local.LocalRegistryHeartbeatService;
import com.machloop.fpc.cms.common.interceptor.HeaderClientInterceptor;
import com.machloop.fpc.cms.grpc.GrpcServerGrpc;
import com.machloop.fpc.cms.grpc.GrpcServerGrpc.GrpcServerBlockingStub;
import com.machloop.fpc.cms.grpc.GrpcServerGrpc.GrpcServerStub;

import io.grpc.ClientInterceptors;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

/**
 * 
 * @author guosk
 *
 * create at 2021年12月8日, fpc-cms-center
 */
@Component
public class GrpcClientHelper {

  @Value("${fpc.cms.grpc.port}")
  private int port;

  @Autowired
  private LocalRegistryHeartbeatService registryHeartbeatService;

  private ManagedChannel managedChannel;

  private GrpcServerBlockingStub grpcServerBlockingStub;

  private GrpcServerStub grpcServerStub;

  public synchronized void reconnectGrpcServer() {
    shutdownNow();
    connectGrpcServer();
  }

  public synchronized GrpcServerStub getGrpcServerStub() {
    if (grpcServerStub == null) {
      connectGrpcServer();
      grpcServerStub = GrpcServerGrpc
          .newStub(ClientInterceptors.intercept(managedChannel, new HeaderClientInterceptor()));
    }

    return grpcServerStub;
  }

  public synchronized GrpcServerBlockingStub getGrpcServerBlockingStub() {
    if (grpcServerBlockingStub == null) {
      connectGrpcServer();
      grpcServerBlockingStub = GrpcServerGrpc.newBlockingStub(
          ClientInterceptors.intercept(managedChannel, new HeaderClientInterceptor()));
    }

    return grpcServerBlockingStub;
  }

  @PreDestroy
  public synchronized void shutdownNow() {
    if (managedChannel != null) {
      managedChannel.shutdownNow();
      managedChannel = null;
      grpcServerStub = null;
      grpcServerBlockingStub = null;
    }
  }

  private void connectGrpcServer() {
    if (managedChannel == null) {
      String parentCmsIp = registryHeartbeatService.getParentCmsIp();
      managedChannel = ManagedChannelBuilder.forAddress(parentCmsIp, port).usePlaintext().build();
    }
  }

}
