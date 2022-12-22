package com.machloop.fpc.cms.center.broker.service.local.impl;

import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.cms.center.appliance.bo.AssignmentTaskBO;
import com.machloop.fpc.cms.center.appliance.service.AssignmentTaskService;
import com.machloop.fpc.cms.center.broker.service.local.ReceiveService;
import com.machloop.fpc.cms.center.broker.service.subordinate.AssignmentService;
import com.machloop.fpc.cms.common.FpcCmsConstants;
import com.machloop.fpc.cms.grpc.CentralProto.AssignReply;
import com.machloop.fpc.cms.grpc.CentralProto.AssignResult;
import com.machloop.fpc.cms.grpc.CentralProto.TaskAssignment;

/**
 * @author guosk
 *
 * create at 2021年12月15日, fpc-cms-center
 */
@Service
public class ReceiveServiceImpl implements ReceiveService {

  static final String CMS_ASSIGNMENT = "superior_cms_assignment";

  @Autowired
  private AssignmentTaskService assignmentTaskService;

  @Autowired
  private AssignmentService assignmentService;

  /**
   * @see com.machloop.fpc.ReceiveService.cms.service.AssignmentService#assignTask(com.machloop.fpc.cms.grpc.CentralProto.AssignReply)
   */
  @Override
  @Transactional
  public List<AssignResult> assignTask(AssignReply assignReply) {

    List<AssignResult> resultList = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    // 新建或更新下发任务
    List<TaskAssignment> taskAssignmentList = assignReply.getTaskAssignmentList();
    if (CollectionUtils.isNotEmpty(taskAssignmentList)) {
      for (TaskAssignment taskAssignment : taskAssignmentList) {

        // 根据action对任务进行新增、更新、删除操作
        refreshAssignmentTask(taskAssignment);

        // 构造下发结果
        AssignResult result = AssignResult.newBuilder().setCode(FpcCmsConstants.RESULT_SUCCESS_CODE)
            .setDetail(FpcCmsConstants.RESULT_SUCCESS_DETAIL)
            .setAssignmentTaskId(taskAssignment.getAssignmentId()).build();
        resultList.add(result);
      }
    }

    return resultList;
  }

  private void refreshAssignmentTask(TaskAssignment taskAssignment) {

    // 如果动作为下发，数据库中不存在该任务将新建任务，如果存在该任务将重新执行该任务，并更新该任务的下发时间
    if (StringUtils.equals(taskAssignment.getAction(),
        FpcCmsConstants.TASK_ACTION_TYPE_ASSIGNMENT)) {

      AssignmentTaskBO assignmentTask = assignmentTaskService
          .queryAssignmentTaskByAssignTaskId(taskAssignment.getTaskId());
      if (StringUtils.isNotBlank(assignmentTask.getId())) {
        // 更新任务基本信息， 并且继续向下发送
        assignmentTaskService.updateAssignmentTaskByAssignTaskId(taskAssignment.getTaskId(),
            taskAssignment.getName(), taskAssignment.getFilterTuple(),
            taskAssignment.getFilterRaw(), taskAssignment.getDescription(),
            new Date(taskAssignment.getTimestamp()), taskAssignment.getFpcSerialNumber());
      } else {
        AssignmentTaskBO assignmentTaskBO = new AssignmentTaskBO();
        BeanUtils.copyProperties(taskAssignment, assignmentTaskBO);
        assignmentTaskBO.setAssignTaskId(taskAssignment.getTaskId());
        assignmentTaskBO.setSource(FpcCmsConstants.TASK_SOURCE_ASSIGNMENT);
        assignmentTaskBO.setFilterStartTime(
            DateUtils.toStringISO8601(new Date(taskAssignment.getFilterStartTime())));
        assignmentTaskBO.setFilterEndTime(
            DateUtils.toStringISO8601(new Date(taskAssignment.getFilterEndTime())));
        assignmentTaskBO.setAssignTaskTime(new Date(taskAssignment.getTimestamp()));
        assignmentTaskBO.setOperatorId(CMS_ASSIGNMENT);
        // 新建任务，并且继续向下发送
        assignmentTaskService.saveAssignmentTask(assignmentTaskBO);
      }

    } else if (StringUtils.equals(taskAssignment.getAction(),
        FpcCmsConstants.TASK_ACTION_TYPE_DELETE)) {
      AssignmentTaskBO assignmentTaskBO = assignmentTaskService
          .queryAssignmentTaskByAssignTaskId(taskAssignment.getTaskId());
      if (StringUtils.isNotBlank(assignmentTaskBO.getId())) {
        assignmentTaskService.deleteAssignmentTask(assignmentTaskBO.getId(), CMS_ASSIGNMENT);
        // 加入下发队列
        assignmentService.addAssignmentQueue(FpcCmsConstants.ASSIGNMENT_TYPE_TASK);
      }
    }

  }

}
