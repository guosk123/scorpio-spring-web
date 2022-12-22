package com.machloop.fpc.cms.center.appliance.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.cms.center.appliance.data.AssignmentTaskRecordDO;

public interface AssignmentTaskRecordDao {

  Page<AssignmentTaskRecordDO> queryAssignmentTaskRecords(Pageable page, String taskId);

  List<AssignmentTaskRecordDO> queryAssignmentTaskRecords(String fpcSerialNumber,
      List<String> taskIds);

  List<AssignmentTaskRecordDO> queryAssignmentTaskRecordsByMessageIds(List<String> messageIds);

  List<AssignmentTaskRecordDO> queryAssignmentTaskRecordsByTaskId(String taskId);

  /**
   * 查询单个任务最早开始执行记录
   * @param taskId
   * @return
   */
  AssignmentTaskRecordDO queryEarliestExecutionTaskRecord(String taskId);

  /**
   * 查询单个任务在单个设备上的执行情况
   * @param fpcSerialNumber
   * @param taskId
   * @return
   */
  AssignmentTaskRecordDO queryAssignmentTaskRecord(String fpcSerialNumber, String taskId);

  /**
   * 批量保存下发任务记录
   * @param assignmentTaskRecordList
   */
  void batchSaveAssignmentTaskRecords(List<AssignmentTaskRecordDO> assignmentTaskRecordList);

  /**
   * 根据id更新下发状态
   * @param ids
   * @param assignmentState
   * @return
   */
  int updateAssignmentTaskRecords(List<String> ids, String assignmentState);

  void updateAssignmentTaskRecords(List<AssignmentTaskRecordDO> assignmentTaskRecordList);

  void updateAssignmentTaskRecordStates(List<Map<String, String>> taskRecordStateList);

  /**
   * 执行下发任务时如果有相同的下发任务（同一设备，同一任务），则将已存在的任务重置为初始化状态
   * @param ids
   * @param operatorId
   * @param assignmentTime
   * @param newState
   * @return
   */
  int batchResetAssignmentTaskRecord(List<String> ids, String operatorId, Date assignmentTime,
      String newState);

  int updateAssignmentTaskRecord(AssignmentTaskRecordDO assignmentTaskRecordDO);

  /**
   * 将任务下发状态从一种状态更改为其他状态
   * @param taskId
   * @param originalState
   * @param newState
   */
  void updateAssignmentState(String taskId, String originalState, String newState);

  /**
   * 更新pcap文件下载URL
   * @param taskId
   * @param fpcSerialNumber
   * @param pcapFileUrl
   */
  void updatePcapFileUrl(String taskId, String fpcSerialNumber, String pcapFileUrl);

}
