package com.machloop.fpc.cms.center.appliance.dao;

import java.util.List;
import java.util.Map;

import com.machloop.fpc.cms.center.appliance.data.AssignmentActionDO;

public interface AssignmentActionDao {

  List<AssignmentActionDO> queryAssignmentActions(List<String> fpcSerialNumbers,
      String assignmentType, String state);

  List<AssignmentActionDO> queryAssignmentActionsByMessageIds(List<String> messageIds);

  List<AssignmentActionDO> queryAssignmentActionsByTaskAction(String taskPolicyId, String type,
      String action);

  List<AssignmentActionDO> queryAssignmentActionsByAssignmentId(String assignmentId);

  /**
   * 批量保存下发动作数据
   * @param assignmentActionList
   */
  void batchSaveAssignmentActions(List<AssignmentActionDO> assignmentActionList);

  /**
   * 批量更新下发状态
   * @param ids
   * @param state
   */
  void updateAssignmentActions(List<String> ids, String state);

  /**
   * 根据messageId更新状态
   * @param assignmentActionList
   */
  void updateAssignmentActions(List<Map<String, String>> assignmentActionList);

  /**
   * 根据messageId更新状态
   * @param assignmentActionDO
   */
  void updateAssignmentAction(String messageId, String state);

  /**
   * 将已存在的等待下发任务状态置为取消下发
   * @param ids
   */
  void batchCancelAssignment(List<String> ids);

  /**
   * 将任务状态从一种状态更改为其他状态（根据分发ID）
   * @param assignmentId
   * @param type
   * @param state
   */
  void updateAssignmentStateByAssignmentId(String assignmentId, String originalState,
      String newState);

  /**
   * 将任务状态从一种状态更改为其他状态（根据任务策略ID）
   * @param taskPolicyId
   * @param type
   * @param originalState
   * @param newState
   */
  void updateAssignmentStateByTaskPolicyId(String taskPolicyId, String type, String originalState,
      String newState);

}
