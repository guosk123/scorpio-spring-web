package com.machloop.fpc.cms.center.appliance.service;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.cms.center.appliance.bo.AssignmentTaskBO;
import com.machloop.fpc.cms.center.appliance.bo.AssignmentTaskRecordBO;
import reactor.util.function.Tuple2;

import java.util.Date;
import java.util.List;

public interface AssignmentTaskService {

  Page<AssignmentTaskBO> queryAssignmentTasks(Pageable page, String name,
                                              String filterConditionType, String mode,String source,String sourceType);

  /**
   * 获取上级下发的任务
   * @return
   */
  List<AssignmentTaskBO> queryHigherAssignmentTasks(Date startTime, Date endTime);

  Page<AssignmentTaskRecordBO> queryAssignmentTaskRecords(Pageable page, String taskId);

  List<AssignmentTaskRecordBO> queryAssignmentTaskRecordsWithoutPage(String taskId);

  AssignmentTaskBO queryAssignmentTask(String id);

  /**
   * 根据上级下发的任务ID查询任务
   * @param assignTaskId
   * @return
   */
  AssignmentTaskBO queryAssignmentTaskByAssignTaskId(String assignTaskId);

  AssignmentTaskBO saveAssignmentTask(AssignmentTaskBO assignmentTaskBO);

  /**
   * 下发任务
   * @param taskId
   * @param fpcSerialNumbers
   * @param operatorId
   * @return
   */
  Tuple2<String, AssignmentTaskBO> assignTask(String taskId, String fpcSerialNumbers,
      String operatorId);

  /**
   * 更新下发状态
   * @param assignmentId
   * @param optionType
   * @return
   */
  AssignmentTaskBO changeAssignmentState(String assignmentId, int optionType);

  AssignmentTaskBO updateAssignmentTask(AssignmentTaskBO assignmentTaskBO);

  /**
   * 接收到上级下发的任务时，更新任务信息，并继续下发
   * @param assignTaskId
   * @param name
   * @param filterTuple
   * @param filterRaw
   * @param description
   * @param assignTaskTime
   * @param fpcSerialNumbers
   * @return
   */
  AssignmentTaskBO updateAssignmentTaskByAssignTaskId(String assignTaskId, String name,
      String filterTuple, String filterRaw, String description, Date assignTaskTime,
      String fpcSerialNumbers);

  /**
   * 更新pcap文件下载URL
   * @param taskId
   * @param fpcSerialNumber
   * @param pcapFileUrl
   */
  void updatePcapFileUrl(String taskId, String fpcSerialNumber, String pcapFileUrl);

  AssignmentTaskBO deleteAssignmentTask(String id, String operatorId);

}
