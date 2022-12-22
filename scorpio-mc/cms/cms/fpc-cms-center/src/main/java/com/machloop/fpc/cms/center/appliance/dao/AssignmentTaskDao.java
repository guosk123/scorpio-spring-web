package com.machloop.fpc.cms.center.appliance.dao;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.cms.center.appliance.data.AssignmentTaskDO;

import java.util.Date;
import java.util.List;

public interface AssignmentTaskDao {


  Page<AssignmentTaskDO> queryAssignmentTasks(Pageable page, String name,
                                              String filterConditionType, String mode, String source, String sourceType);

  List<AssignmentTaskDO> queryHigherAssignmentTasks(Date startTime, Date endTime);

  AssignmentTaskDO queryAssignmentTask(String id, boolean isContainDelete);

  AssignmentTaskDO queryAssignmentTaskByName(String name);

  AssignmentTaskDO queryAssignmentTaskByAssignTaskId(String assignTaskId);

  AssignmentTaskDO saveAssignmentTask(AssignmentTaskDO assignmentTaskDO);

  int updateAssignmentTask(AssignmentTaskDO assignmentTaskDO);

  int updateAssignmentTaskByAssignTaskId(String assignTaskId, String name, String filterTuple,
      String filterRaw, String description, Date assignTaskTime);

  int deleteAssignmentTask(String id, String operatorId);

}
