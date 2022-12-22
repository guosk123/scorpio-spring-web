package com.machloop.fpc.cms.center.central.task;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.google.common.collect.Lists;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.appliance.dao.AssignmentActionDao;
import com.machloop.fpc.cms.center.appliance.dao.AssignmentTaskRecordDao;
import com.machloop.fpc.cms.center.appliance.data.AssignmentActionDO;

/**
 * @author liyongjun
 *
 * create at 2019年11月14日, fpc-cms-center
 */
@Component
public class AssignmentExpireTask {

  private static final int EXPIRE_TIME = 30 * 1000;

  @Autowired
  private AssignmentTaskRecordDao assignmentTaskRecordDao;

  @Autowired
  private AssignmentActionDao assignmentActionDao;

  @Scheduled(fixedRateString = "${task.assignment.expire.schedule.fixedrate.ms}")
  public void run() {
    long currentTiem = DateUtils.now().getTime();
    List<AssignmentActionDO> assignmentActionDOList = assignmentActionDao
        .queryAssignmentActions(null, "", CenterConstants.TASK_ASSIGNMENT_STATE_DOING);
    List<String> taskPolicyIds = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    for (AssignmentActionDO assignmentActionDO : assignmentActionDOList) {
      long assignmentTime = assignmentActionDO.getAssignmentTime().getTime();
      if (currentTiem - assignmentTime > EXPIRE_TIME) {
        taskPolicyIds.add(assignmentActionDO.getTaskPolicyId());
      }
    }

    // 将超过阈值并且状态为正在执行的任务或策略状态置为下发失败
    assignmentActionDao.updateAssignmentActions(taskPolicyIds,
        CenterConstants.TASK_ASSIGNMENT_STATE_FAILED);

    assignmentTaskRecordDao.updateAssignmentTaskRecords(taskPolicyIds,
        CenterConstants.TASK_ASSIGNMENT_STATE_FAILED);

  }

}
