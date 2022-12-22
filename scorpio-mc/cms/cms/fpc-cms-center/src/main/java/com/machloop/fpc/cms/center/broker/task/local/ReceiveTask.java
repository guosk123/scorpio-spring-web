package com.machloop.fpc.cms.center.broker.task.local;

import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import javax.annotation.PostConstruct;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.helper.GracefulShutdownHelper;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.webapp.global.service.GlobalSettingService;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.broker.service.local.LocalRegistryHeartbeatService;
import com.machloop.fpc.cms.center.broker.service.local.ReceiveService;
import com.machloop.fpc.cms.center.helper.GrpcClientHelper;
import com.machloop.fpc.cms.common.FpcCmsConstants;
import com.machloop.fpc.cms.grpc.CentralProto;
import com.machloop.fpc.cms.grpc.CentralProto.AssignReply;
import com.machloop.fpc.cms.grpc.CentralProto.AssignRequest;
import com.machloop.fpc.cms.grpc.CentralProto.AssignResult;

import io.grpc.stub.StreamObserver;

/**
 * @author guosk
 *
 * create at 2021年12月15日, fpc-cms-center
 */
@Service
public class ReceiveTask {

  private static final Logger LOGGER = LoggerFactory.getLogger(ReceiveTask.class);

  private static final long MESSAGE_TIMEOUT_SEC = 3;

  @Autowired
  private GrpcClientHelper grpcClientHelper;

  @Autowired
  private ReceiveService assignmentService;

  @Autowired
  private LocalRegistryHeartbeatService registryHeartbeatService;

  @Autowired
  private GlobalSettingService globalSettingService;

  private LinkedBlockingQueue<
      AssignRequest> messageQueue = new LinkedBlockingQueue<AssignRequest>(1024);

  @PostConstruct
  public void init() {
    new Thread(() -> {
      while (!GracefulShutdownHelper.isShutdownNow()) {

        // 开关控制
        if (StringUtils.equals(Constants.BOOL_NO,
            globalSettingService.getValue(CenterConstants.GLOBAL_SETTING_CMS_STATE, false))) {
          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            LOGGER.warn("cms swtich is off, sleep has been interrupt.");
          }
          LOGGER.debug("cms swtich is off.");
          continue;
        }

        // 先检查是否已注册，若没注册重复检查
        String parentCmsIp = registryHeartbeatService.getParentCmsIp();

        if (StringUtils.isBlank(parentCmsIp)) {
          LOGGER.debug("cmsIp is empty.");

          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            LOGGER.warn("cmsIp is empty, sleep has been interrupt.");
          }

          continue;
        }

        // 心跳异常将不进行下发注册
        if (!registryHeartbeatService.isAlive()) {
          LOGGER.debug("abnormal heartbeat, assignment registration failed.");

          try {
            Thread.sleep(1000);
          } catch (InterruptedException e) {
            LOGGER.warn("abnormal heartbeat, sleep has been interrupt.");
          }

          continue;
        }

        // 正常情况下不会返回，一直阻塞在assignChannel中
        assignChannel();

        // 如果 channel返回异常，休眠1秒后继续尝试
        try {
          Thread.sleep(1000);
        } catch (InterruptedException e) {
          LOGGER.warn("stream channel sleep has been interrupt.");
        }
      }

    }).start();
  }

  private void assignChannel() {

    Thread worker = Thread.currentThread();
    StreamObserver<CentralProto.AssignReply> responseObserver = new StreamObserver<
        CentralProto.AssignReply>() {

      @Override
      public void onNext(AssignReply assignReply) {
        LOGGER.info("receive to assignment task, assignReply is {}", assignReply);

        // 更新下发的任务
        List<AssignResult> assignmentResultList = assignmentService.assignTask(assignReply);

        // 构造更新结果
        AssignRequest assignRequest = AssignRequest.newBuilder()
            .setDeviceType(FpcCmsConstants.DEVICE_TYPE_CMS)
            .setSerialNumber(registryHeartbeatService.getSerialNumber())
            .addAllResult(assignmentResultList).setTimestamp(DateUtils.now().getTime()).build();

        // 将更新结果放入队列
        if (!messageQueue.offer(assignRequest)) {
          LOGGER.warn("failed to assignRequest queue exceed capacity.");
        }
      }

      @Override
      public void onError(Throwable t) {
        LOGGER.warn("failed to connect the server.", t);
        worker.interrupt();
      }

      @Override
      public void onCompleted() {
        LOGGER.warn("server complete.");
        worker.interrupt();
      }
    };

    LOGGER.info("start to connect to server.thread status: [{}]", worker.isInterrupted());
    // 构造空的消息
    AssignRequest emptyRequest = AssignRequest.newBuilder()
        .setDeviceType(FpcCmsConstants.DEVICE_TYPE_CMS)
        .setSerialNumber(registryHeartbeatService.getSerialNumber())
        .addAllResult(Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE)).build();
    LOGGER.debug("send emtpy message {}", emptyRequest);

    // 向cms发送空消息注册
    StreamObserver<CentralProto.AssignRequest> requestObserver = grpcClientHelper
        .getGrpcServerStub().assignChannel(responseObserver);
    requestObserver.onNext(emptyRequest);

    // 只要线程不中断一直阻塞，从消息队列中获取下发结果并发送
    // 当StreamObserver中断线程, 退出循环
    while (!GracefulShutdownHelper.isShutdownNow() && !worker.isInterrupted()) {

      AssignRequest assignRequest = null;
      try {
        assignRequest = messageQueue.poll(MESSAGE_TIMEOUT_SEC, TimeUnit.SECONDS);
      } catch (InterruptedException e) {
        LOGGER.warn("failed to assignment message queue interrupt.");
        break;
      }

      // 队列中没有消息，继续循环阻塞
      if (assignRequest == null) {
        continue;
      }

      LOGGER.info("return assignment task or policy result, assignRequest is {}", assignRequest);
      requestObserver.onNext(assignRequest);
    }

    // 清空线程中断标志, 以进行重新连接
    if (Thread.interrupted()) {
      LOGGER.info("clear thread interrupted.thread id: [{}]", worker.getId());
    }
  }

}
