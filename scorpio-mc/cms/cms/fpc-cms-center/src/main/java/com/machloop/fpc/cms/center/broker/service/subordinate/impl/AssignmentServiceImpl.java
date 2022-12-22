package com.machloop.fpc.cms.center.broker.service.subordinate.impl;

import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.Semaphore;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.helper.GracefulShutdownHelper;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.appliance.dao.AssignmentActionDao;
import com.machloop.fpc.cms.center.appliance.dao.AssignmentTaskDao;
import com.machloop.fpc.cms.center.appliance.dao.AssignmentTaskRecordDao;
import com.machloop.fpc.cms.center.appliance.data.AssignmentActionDO;
import com.machloop.fpc.cms.center.appliance.data.AssignmentTaskDO;
import com.machloop.fpc.cms.center.broker.data.DeviceStatusDO;
import com.machloop.fpc.cms.center.broker.service.subordinate.AssignmentService;
import com.machloop.fpc.cms.center.broker.service.subordinate.DeviceStatusService;
import com.machloop.fpc.cms.center.central.bo.FpcBO;
import com.machloop.fpc.cms.center.central.service.FpcService;
import com.machloop.fpc.cms.common.FpcCmsConstants;
import com.machloop.fpc.cms.grpc.CentralProto.AssignReply;
import com.machloop.fpc.cms.grpc.CentralProto.TaskAssignment;

import io.grpc.stub.StreamObserver;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

/**
 * @author liyongjun
 *
 * create at 2019年11月23日, fpc-cms-center
 */
@Service
public class AssignmentServiceImpl implements AssignmentService {

  private static final Logger LOGGER = LoggerFactory.getLogger(AssignmentServiceImpl.class);

  @Autowired
  private AssignmentActionDao assignmentActionDao;

  @Autowired
  private AssignmentTaskRecordDao assignmentTaskRecordDao;

  @Autowired
  private AssignmentTaskDao assignmentTaskDao;

  @Autowired
  private DeviceStatusService deviceStatusService;

  @Autowired
  private FpcService fpcService;

  private Map<String, StreamObserver<AssignReply>> deviceChannelMap = Maps.newConcurrentMap();

  private Semaphore semaphore = new Semaphore(1);

  private LinkedBlockingQueue<String> assignmentTypeQueue = new LinkedBlockingQueue<String>(1024);

  /**
   * @see com.machloop.fpc.cms.center.broker.service.subordinate.AssignmentService#registerAssignmentChannel(java.lang.String, java.lang.String, io.grpc.stub.StreamObserver)
   */
  @Override
  public void registerAssignmentChannel(String deviceType, String serialNumber,
      StreamObserver<AssignReply> responseObserver) {
    this.deviceChannelMap.put(StringUtils.joinWith("$", deviceType, serialNumber),
        responseObserver);
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.subordinate.AssignmentService#assignmentTask()
   */
  @Transactional
  @Override
  public void assignmentTask() {

    try {
      semaphore.acquire();
    } catch (InterruptedException e) {
      LOGGER.warn("filed to semaphore interrupted.");
    }

    if (LOGGER.isDebugEnabled()) {
      LOGGER.debug("acquire semaphore, trying to get the signal from the queue, queue size: {}",
          assignmentTypeQueue.size());
    }

    String assignmentType = assignmentTypeQueue.poll();
    while (!GracefulShutdownHelper.isShutdownNow() && StringUtils.isNotBlank(assignmentType)) {
      // 循环调用每个直连设备的下发(任务下发对象如果是本机直管的探针设备，则直接下发到具体探针；否则，下发到探针直属的cms)
      for (Map.Entry<String, StreamObserver<AssignReply>> entry : deviceChannelMap.entrySet()) {
        String deviceType = StringUtils.substringBefore(entry.getKey(), "$");
        String deviceSerialNumber = StringUtils.substringAfterLast(entry.getKey(), "$");
        StreamObserver<AssignReply> responseObserver = entry.getValue();

        // 查询等待下发的任务
        List<String> fpcSerialNumbers = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
        if (StringUtils.equals(deviceType, FpcCmsConstants.DEVICE_TYPE_CMS)) {
          fpcSerialNumbers.addAll(fpcService.queryFpcByCms(deviceSerialNumber, true).stream()
              .map(FpcBO::getSerialNumber).collect(Collectors.toList()));
        } else {
          fpcSerialNumbers.add(deviceSerialNumber);
        }
        List<AssignmentActionDO> assignmentTaskList = assignmentActionDao.queryAssignmentActions(
            fpcSerialNumbers, FpcCmsConstants.ASSIGNMENT_TYPE_TASK,
            CenterConstants.TASK_ASSIGNMENT_STATE_WAIT);
        if (assignmentTaskList.isEmpty()) {
          // 未检测到待下发任务
          continue;
        }

        // 查看设备心跳是否连接正常，若不正常不进行下发操作
        DeviceStatusDO deviceStatusDO = deviceStatusService.queryDeviceStatus(deviceType,
            deviceSerialNumber);
        if (StringUtils.equals(deviceStatusDO.getCurrentConnectStatus(),
            FpcCmsConstants.CONNECT_STATUS_ABNORMAL)) {

          // 与下级设备心跳异常，将任务下发状态置为失败
          changeAssignmentTaskAction(CenterConstants.TASK_ASSIGNMENT_STATE_FAILED,
              assignmentTaskList);

          LOGGER.warn(
              "connection abnormal, stop assignment tasks, assign device type: {}, serialNumber: {}, assignedTaskId is {}.",
              deviceType, deviceSerialNumber, assignmentTaskList.stream()
                  .map(AssignmentActionDO::getTaskPolicyId).collect(Collectors.toList()));
          continue;
        }

        // 将获取到的任务生成messageId并将状态置为正在下发
        AssignReply.Builder assignReplyBuilder = AssignReply.newBuilder();
        if (StringUtils.equals(assignmentType, FpcCmsConstants.ASSIGNMENT_TYPE_TASK)) {

          List<TaskAssignment> taskAssignmentList = Lists
              .newArrayListWithCapacity(assignmentTaskList.size());

          // 将动作表以及内容表对应assignedId、状态为等待下发的任务置为正在下发状态
          changeAssignmentTaskAction(CenterConstants.TASK_ASSIGNMENT_STATE_DOING,
              assignmentTaskList);

          // 构造下发任务(如果下发的目标设备为下级cms，需要指定任务具体生效的探针)
          Map<String, Tuple2<AssignmentActionDO, List<String>>> taskValidFpcDevice = Maps
              .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
          assignmentTaskList.forEach(task -> {
            String key = StringUtils.join(task.getTaskPolicyId(), task.getType(), task.getAction());

            Tuple2<AssignmentActionDO, List<String>> taskTuple = taskValidFpcDevice.getOrDefault(
                key, Tuples.of(task, Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE)));
            taskTuple.getT2().add(task.getFpcSerialNumber());

            taskValidFpcDevice.put(key, Tuples.of(task, taskTuple.getT2()));
          });

          for (Tuple2<AssignmentActionDO, List<String>> assignmentActionTuple : taskValidFpcDevice
              .values()) {
            AssignmentActionDO assignmentActionDO = assignmentActionTuple.getT1();

            AssignmentTaskDO transmitTaskDO = assignmentTaskDao
                .queryAssignmentTask(assignmentActionDO.getTaskPolicyId(), true);
            TaskAssignment taskAssignment = TaskAssignment.newBuilder()
                .setAssignmentId(assignmentActionDO.getMessageId())
                .setTaskId(transmitTaskDO.getId()).setAction(assignmentActionDO.getAction())
                .setName(transmitTaskDO.getName())
                .setFilterStartTime(transmitTaskDO.getFilterStartTime().getTime())
                .setFilterEndTime(transmitTaskDO.getFilterEndTime().getTime())
                .setFilterNetworkId(transmitTaskDO.getFilterNetworkId())
                .setFilterConditionType(transmitTaskDO.getFilterConditionType())
                .setFilterTuple(transmitTaskDO.getFilterTuple())
                .setFilterBpf(transmitTaskDO.getFilterBpf())
                .setFilterRaw(transmitTaskDO.getFilterRaw()).setMode(transmitTaskDO.getMode())
                .setReplayNetif(transmitTaskDO.getReplayNetif())
                .setReplayRate(transmitTaskDO.getReplayRate())
                .setReplayRateUnit(transmitTaskDO.getReplayRateUnit())
                .setForwardAction(transmitTaskDO.getForwardAction())
                .setDescription(transmitTaskDO.getDescription())
                .setFpcSerialNumber(CsvUtils.convertCollectionToCSV(assignmentActionTuple.getT2()))
                .setTimestamp(assignmentActionDO.getAssignmentTime().getTime()).build();

            taskAssignmentList.add(taskAssignment);
          }

          assignReplyBuilder.addAllTaskAssignment(taskAssignmentList);
        }

        // 下发
        try {
          AssignReply assignReply = assignReplyBuilder.build();
          LOGGER.info("assignment to task, assignReply is {}.", assignReply);

          responseObserver.onNext(assignReply);
        } catch (Exception e) {
          // 发送失败将任务的下发状态置为下发失败
          if (StringUtils.equals(assignmentType, FpcCmsConstants.ASSIGNMENT_TYPE_TASK)) {
            changeAssignmentTaskAction(CenterConstants.TASK_ASSIGNMENT_STATE_FAILED,
                assignmentTaskList);
          }

          LOGGER.warn(
              "failed to connection abnormality, assign device type: {}, serialNumber: {}, assignedTaskId is {}.",
              deviceType, deviceSerialNumber, assignmentTaskList.stream()
                  .map(AssignmentActionDO::getTaskPolicyId).collect(Collectors.toList()));
        }
      }

      // 获取下一个下发任务
      assignmentType = assignmentTypeQueue.poll();
    }
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.service.subordinate.AssignmentService#addAssignmentQueue(java.lang.String)
   */
  @Override
  public void addAssignmentQueue(String assignmentType) {
    // 如果加入队列成功，释放信号量
    if (this.assignmentTypeQueue.offer(assignmentType)) {
      this.semaphore.release();
    } else {
      // 如果加入队列失败记录，记录日志
      LOGGER.warn("failed to assignmentType queue exceed capacity, assignmentType is {}.",
          assignmentType);
    }
  }

  /**
   * @param state
   * @param assignmentTaskList
   */
  private void changeAssignmentTaskAction(String state,
      List<AssignmentActionDO> assignmentTaskList) {

    if (CollectionUtils.isEmpty(assignmentTaskList)) {
      return;
    }

    // 更新内容表的任务状态
    List<Map<String, String>> taskMessageList = Lists
        .newArrayListWithCapacity(assignmentTaskList.size());
    for (AssignmentActionDO assignmentActionDO : assignmentTaskList) {
      Map<String,
          String> taskMessageMap = Maps.newHashMapWithExpectedSize(assignmentTaskList.size());
      assignmentActionDO.setState(state);

      // 将taskId与messageId放入map
      taskMessageMap.put("messageId", assignmentActionDO.getMessageId());
      taskMessageMap.put("state", state);

      taskMessageList.add(taskMessageMap);
    }

    assignmentActionDao.updateAssignmentActions(taskMessageList);

    // 批处理更新状态
    assignmentTaskRecordDao.updateAssignmentTaskRecordStates(taskMessageList);
  }

}
