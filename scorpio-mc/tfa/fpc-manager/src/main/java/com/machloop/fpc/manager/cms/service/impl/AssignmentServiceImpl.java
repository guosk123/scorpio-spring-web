package com.machloop.fpc.manager.cms.service.impl;

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
import com.machloop.fpc.cms.common.FpcCmsConstants;
import com.machloop.fpc.cms.grpc.CentralProto.AssignReply;
import com.machloop.fpc.cms.grpc.CentralProto.AssignResult;
import com.machloop.fpc.cms.grpc.CentralProto.TaskAssignment;
import com.machloop.fpc.manager.appliance.dao.TransmitTaskDao;
import com.machloop.fpc.manager.appliance.data.TransmitTaskDO;
import com.machloop.fpc.manager.cms.service.AssignmentService;

/**
 * @author liyongjun
 *
 * create at 2019年12月16日, fpc-manager
 */
@Service
public class AssignmentServiceImpl implements AssignmentService {

  static final String CMS_ASSIGNMENT = "cms_assignment";

  @Autowired
  private TransmitTaskDao transmitTaskDao;

  /**
   * @see com.machloop.fpc.manager.cms.service.AssignmentService#assignTask(com.machloop.fpc.cms.grpc.CentralProto.AssignReply)
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

      TransmitTaskDO transmitTask = transmitTaskDao
          .queryTransmitTaskByAssignTaskId(taskAssignment.getTaskId());
      if (StringUtils.isNotBlank(transmitTask.getId())) {

        // 更新任务基本信息
        transmitTaskDao.updateTransmitTaskByAssignTaskId(taskAssignment.getTaskId(),
            taskAssignment.getName(), taskAssignment.getFilterTuple(),
            taskAssignment.getFilterRaw(), taskAssignment.getDescription(),
            new Date(taskAssignment.getTimestamp()));

        // 重新执行任务
        transmitTaskDao.redoTransmitTask(transmitTask.getId());
      } else {

        TransmitTaskDO transmitTaskDO = new TransmitTaskDO();
        BeanUtils.copyProperties(taskAssignment, transmitTaskDO);
        transmitTaskDO.setAssignTaskId(taskAssignment.getTaskId());
        transmitTaskDO.setSource(FpcCmsConstants.TASK_SOURCE_ASSIGNMENT);
        transmitTaskDO.setFilterStartTime(new Date(taskAssignment.getFilterStartTime()));
        transmitTaskDO.setFilterEndTime(new Date(taskAssignment.getFilterEndTime()));
        transmitTaskDO.setAssignTaskTime(new Date(taskAssignment.getTimestamp()));
        transmitTaskDO.setOperatorId(CMS_ASSIGNMENT);
        transmitTaskDao.saveTransmitTask(transmitTaskDO);
      }
    } else if (StringUtils.equals(taskAssignment.getAction(),
        FpcCmsConstants.TASK_ACTION_TYPE_DELETE)) {
      transmitTaskDao.deleteTransmitTaskByAssignTaskId(taskAssignment.getTaskId(), CMS_ASSIGNMENT);
    }
  }

}
