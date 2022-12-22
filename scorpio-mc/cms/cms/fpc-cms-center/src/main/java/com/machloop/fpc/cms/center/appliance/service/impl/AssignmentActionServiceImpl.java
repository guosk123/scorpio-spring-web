package com.machloop.fpc.cms.center.appliance.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.appliance.bo.AssignmentActionBO;
import com.machloop.fpc.cms.center.appliance.dao.AssignmentActionDao;
import com.machloop.fpc.cms.center.appliance.dao.AssignmentTaskRecordDao;
import com.machloop.fpc.cms.center.appliance.data.AssignmentActionDO;
import com.machloop.fpc.cms.center.appliance.data.AssignmentTaskRecordDO;
import com.machloop.fpc.cms.center.appliance.service.AssignmentActionService;
import com.machloop.fpc.cms.center.central.bo.FpcBO;
import com.machloop.fpc.cms.center.central.service.FpcService;
import com.machloop.fpc.cms.common.FpcCmsConstants;
import com.machloop.fpc.cms.grpc.CentralProto.AssignResult;

@Service
public class AssignmentActionServiceImpl implements AssignmentActionService {

  @Autowired
  private AssignmentActionDao assignmentActionDao;

  @Autowired
  private AssignmentTaskRecordDao assignmentTaskRecordDao;

  @Autowired
  private FpcService fpcService;

  @Autowired
  private DictManager dictManager;

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.AssignmentActionService#queryAssignmentActions(java.lang.String)
   */
  @Override
  public List<AssignmentActionBO> queryAssignmentActions(String assignmentId) {
    Map<String, String> assignmentStateDict = dictManager.getBaseDict()
        .getItemMap("task_policy_assignment_state");

    List<AssignmentActionDO> assignmentActionList = assignmentActionDao
        .queryAssignmentActionsByAssignmentId(assignmentId);

    // 获取设备信息集合
    List<String> fpcSerialNumbers = Lists.newArrayListWithExpectedSize(assignmentActionList.size());
    for (AssignmentActionDO assignmentActionDO : assignmentActionList) {
      fpcSerialNumbers.add(assignmentActionDO.getFpcSerialNumber());
    }
    List<FpcBO> fpcBOList = fpcService.queryFpcBySerialNumbers(fpcSerialNumbers, false);
    Map<String, FpcBO> fpcMap = Maps.newHashMapWithExpectedSize(fpcBOList.size());
    for (FpcBO fpcBO : fpcBOList) {
      fpcMap.put(fpcBO.getSerialNumber(), fpcBO);
    }

    List<AssignmentActionBO> assignmentActionBOList = Lists
        .newArrayListWithExpectedSize(assignmentActionList.size());
    for (AssignmentActionDO assignmentActionDO : assignmentActionList) {
      AssignmentActionBO assignmentActionBO = new AssignmentActionBO();
      FpcBO fpcBO = fpcMap.get(assignmentActionDO.getFpcSerialNumber());
      if (fpcBO == null) {
        continue;
      }

      assignmentActionBO.setFpcSerialNumber(fpcBO.getSerialNumber());
      assignmentActionBO.setFpcName(fpcBO.getName());
      assignmentActionBO.setConnectStatus(fpcBO.getConnectStatus());
      assignmentActionBO.setConnectStatusText(fpcBO.getConnectStatusText());
      assignmentActionBO.setAssignmentState(assignmentActionDO.getState());
      assignmentActionBO.setAssignmentStateText(
          MapUtils.getString(assignmentStateDict, assignmentActionBO.getAssignmentState(), ""));
      assignmentActionBO
          .setAssignmentTime(DateUtils.toStringISO8601(assignmentActionDO.getAssignmentTime()));

      assignmentActionBOList.add(assignmentActionBO);
    }

    return assignmentActionBOList;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.AssignmentActionService#queryRelatedDevices(int, java.lang.String)
   */
  @Override
  public String queryRelatedDevices(int optionType, String assignmentId) {
    String filterState = "";
    switch (optionType) {
      case CenterConstants.ASSIGNMENT_ACTION_STOP:
        filterState = CenterConstants.TASK_ASSIGNMENT_STATE_STOP;
        break;
      case CenterConstants.ASSIGNMENT_ACTION_CONTINUE:
        filterState = CenterConstants.TASK_ASSIGNMENT_STATE_WAIT;
        break;
      case CenterConstants.ASSIGNMENT_ACTION_CANCEL:
        filterState = CenterConstants.TASK_ASSIGNMENT_STATE_CANCEL;
        break;
      default:
        return "";
    }

    // 获取设备ID集合，记录本次操作涉及到的设备
    List<AssignmentActionDO> assignmentActionList = assignmentActionDao
        .queryAssignmentActionsByAssignmentId(assignmentId);
    List<String> fpcSerialNumbers = Lists.newArrayListWithExpectedSize(assignmentActionList.size());
    for (AssignmentActionDO assignmentActionDO : assignmentActionList) {
      if (StringUtils.equals(filterState, assignmentActionDO.getState())) {
        fpcSerialNumbers.add(assignmentActionDO.getFpcSerialNumber());
      }
    }

    return CsvUtils.convertCollectionToCSV(fpcSerialNumbers);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.AssignmentActionService#updateAssignmentActions(java.util.List)
   */
  @Transactional
  @Override
  public void updateAssignmentActions(List<AssignResult> assignResultList) {

    // 构造下发id
    List<String> assignmentIdList = Lists.newArrayListWithCapacity(assignResultList.size());
    for (AssignResult assignmentResult : assignResultList) {
      assignmentIdList.add(assignmentResult.getAssignmentTaskId());
    }

    // 根据messageId构造下发动作map
    List<AssignmentActionDO> assignmentActionList = assignmentActionDao
        .queryAssignmentActionsByMessageIds(assignmentIdList);
    Map<String, AssignmentActionDO> messageActionMap = Maps
        .newHashMapWithExpectedSize(assignmentActionList.size());
    for (AssignmentActionDO assignmentActionDO : assignmentActionList) {
      messageActionMap.put(assignmentActionDO.getMessageId(), assignmentActionDO);
    }

    // 根据messageId构造下发任务记录map
    List<AssignmentTaskRecordDO> assignmentTaskRecordList = assignmentTaskRecordDao
        .queryAssignmentTaskRecordsByMessageIds(assignmentIdList);
    Map<String, AssignmentTaskRecordDO> messageTaskMap = Maps
        .newHashMapWithExpectedSize(assignmentTaskRecordList.size());
    for (AssignmentTaskRecordDO assignmentTaskRecordDO : assignmentTaskRecordList) {
      messageTaskMap.put(assignmentTaskRecordDO.getMessageId(), assignmentTaskRecordDO);
    }

    // 根据messageId更新下发动作和下发任务记录中的下发状态
    List<Map<String, String>> needUpdateActionStateList = Lists
        .newArrayListWithCapacity(assignResultList.size());
    List<Map<String, String>> needUpdateTaskRecordStateList = Lists
        .newArrayListWithCapacity(assignResultList.size());
    for (AssignResult assignResult : assignResultList) {

      // 获取下发状态
      int code = assignResult.getCode();
      String assignmentState = CenterConstants.TASK_ASSIGNMENT_STATE_FAILED;
      if (code == FpcCmsConstants.RESULT_SUCCESS_CODE) {
        assignmentState = CenterConstants.TASK_ASSIGNMENT_STATE_SUCCESS;
      }

      String assignmentId = assignResult.getAssignmentTaskId();
      AssignmentActionDO assignmentActionDO = messageActionMap.get(assignmentId);
      AssignmentTaskRecordDO assignmentTaskRecordDO = messageTaskMap.get(assignmentId);

      if (assignmentActionDO != null) {
        Map<String, String> needUpdateActionStateMap = Maps
            .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
        needUpdateActionStateMap.put("messageId", assignmentId);
        needUpdateActionStateMap.put("state", assignmentState);
        needUpdateActionStateList.add(needUpdateActionStateMap);
      }

      if (assignmentTaskRecordDO != null) {
        Map<String, String> needUpdateRecordStateMap = Maps
            .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
        needUpdateRecordStateMap.put("messageId", assignmentId);
        needUpdateRecordStateMap.put("state", assignmentState);

        needUpdateTaskRecordStateList.add(needUpdateRecordStateMap);
      }
    }

    // 将下发动作和下发任务记录更新到数据库
    assignmentActionDao.updateAssignmentActions(needUpdateActionStateList);
    assignmentTaskRecordDao.updateAssignmentTaskRecordStates(needUpdateTaskRecordStateList);
  }

}
