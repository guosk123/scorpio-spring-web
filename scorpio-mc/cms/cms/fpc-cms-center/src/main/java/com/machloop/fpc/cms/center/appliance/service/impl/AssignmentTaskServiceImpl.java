package com.machloop.fpc.cms.center.appliance.service.impl;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.algorithm.bpf.BpfCheck;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.cms.center.CenterConstants;
import com.machloop.fpc.cms.center.appliance.bo.AssignmentTaskBO;
import com.machloop.fpc.cms.center.appliance.bo.AssignmentTaskRecordBO;
import com.machloop.fpc.cms.center.appliance.dao.AssignmentActionDao;
import com.machloop.fpc.cms.center.appliance.dao.AssignmentTaskDao;
import com.machloop.fpc.cms.center.appliance.dao.AssignmentTaskRecordDao;
import com.machloop.fpc.cms.center.appliance.data.AssignmentActionDO;
import com.machloop.fpc.cms.center.appliance.data.AssignmentTaskDO;
import com.machloop.fpc.cms.center.appliance.data.AssignmentTaskRecordDO;
import com.machloop.fpc.cms.center.appliance.service.AssignmentActionService;
import com.machloop.fpc.cms.center.appliance.service.AssignmentTaskService;
import com.machloop.fpc.cms.center.broker.service.subordinate.AssignmentService;
import com.machloop.fpc.cms.center.central.bo.FpcBO;
import com.machloop.fpc.cms.center.central.service.FpcService;
import com.machloop.fpc.cms.center.system.service.LicenseService;
import com.machloop.fpc.cms.common.FpcCmsConstants;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class AssignmentTaskServiceImpl implements AssignmentTaskService {

  @Autowired
  private AssignmentTaskDao assignmentTaskDao;

  @Autowired
  private AssignmentTaskRecordDao assignmentTaskRecordDao;

  @Autowired
  private AssignmentActionDao assignmentActionDao;

  @Autowired
  private FpcService fpcService;

  @Autowired
  private AssignmentService assignmentService;

  @Autowired
  private AssignmentActionService assignmentActionService;

  @Autowired
  private LicenseService licenseService;

  @Autowired
  private DictManager dictManager;

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.AssignmentTaskService#queryAssignmentTasks(com.machloop.alpha.common.base.page.Pageable, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Page<AssignmentTaskBO> queryAssignmentTasks(Pageable page, String name,
      String filterConditionType, String mode, String source, String sourceType) {

    Map<String, String> modeDict = dictManager.getBaseDict().getItemMap("appliance_transmit_mode");
    Map<String, String> filterTypeDict = dictManager.getBaseDict()
        .getItemMap("appliance_transmit_filter_type");

    Page<AssignmentTaskDO> assignmentTaskDOPage = assignmentTaskDao.queryAssignmentTasks(page, name,
        filterConditionType, mode, source, sourceType);

    long totalElem = assignmentTaskDOPage.getTotalElements();

    List<AssignmentTaskBO> assignmentTaskBOList = Lists
        .newArrayListWithCapacity(assignmentTaskDOPage.getSize());
    for (AssignmentTaskDO assignmentTaskDO : assignmentTaskDOPage) {
      AssignmentTaskBO assignmentTaskBO = new AssignmentTaskBO();
      BeanUtils.copyProperties(assignmentTaskDO, assignmentTaskBO);

      assignmentTaskBO.setModeText(MapUtils.getString(modeDict, assignmentTaskBO.getMode(), ""));
      assignmentTaskBO.setFilterConditionTypeText(
          MapUtils.getString(filterTypeDict, assignmentTaskBO.getFilterConditionType(), ""));

      assignmentTaskBO
          .setFilterStartTime(DateUtils.toStringISO8601(assignmentTaskDO.getFilterStartTime()));
      assignmentTaskBO
          .setFilterEndTime(DateUtils.toStringISO8601(assignmentTaskDO.getFilterEndTime()));

      AssignmentTaskRecordDO assignmentTaskRecordDO = assignmentTaskRecordDao
          .queryEarliestExecutionTaskRecord(assignmentTaskBO.getId());
      assignmentTaskBO.setExecutionStartTime(
          DateUtils.toStringISO8601(assignmentTaskRecordDO.getExecutionStartTime()));

      List<String> fpcSerialNumber = assignmentTaskRecordDao
          .queryAssignmentTaskRecordsByTaskId(assignmentTaskBO.getId()).stream()
          .map(AssignmentTaskRecordDO::getFpcSerialNumber).collect(Collectors.toList());
      assignmentTaskBO.setFpcSerialNumber(CsvUtils.convertCollectionToCSV(fpcSerialNumber));

      assignmentTaskBOList.add(assignmentTaskBO);
    }

    return new PageImpl<>(assignmentTaskBOList, page, totalElem);
  }
  /**
   * @see com.machloop.fpc.cms.center.appliance.service.AssignmentTaskService#queryHigherAssignmentTasks(java.util.Date, java.util.Date)
   */
  @Override
  public List<AssignmentTaskBO> queryHigherAssignmentTasks(Date startTime, Date endTime) {
    List<AssignmentTaskDO> assignmentTaskDOList = assignmentTaskDao
        .queryHigherAssignmentTasks(startTime, endTime);

    return assignmentTaskDOList.stream().map(assignmentTaskDO -> {
      AssignmentTaskBO assignmentTaskBO = new AssignmentTaskBO();
      BeanUtils.copyProperties(assignmentTaskDO, assignmentTaskBO);

      return assignmentTaskBO;
    }).collect(Collectors.toList());
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.AssignmentTaskService#queryAssignmentTask(java.lang.String)
   */
  @Override
  public AssignmentTaskBO queryAssignmentTask(String id) {
    Map<String, String> modeDict = dictManager.getBaseDict().getItemMap("appliance_transmit_mode");
    Map<String,
        String> rateUnitDict = dictManager.getBaseDict().getItemMap("appliance_transmit_rateunit");
    Map<String, String> forwardActionDict = dictManager.getBaseDict()
        .getItemMap("appliance_transmit_forward_policy");
    Map<String, String> filterTypeDict = dictManager.getBaseDict()
        .getItemMap("appliance_transmit_filter_type");

    AssignmentTaskBO assignmentTaskBO = new AssignmentTaskBO();

    AssignmentTaskDO assignmentTaskDO = assignmentTaskDao.queryAssignmentTask(id, false);
    BeanUtils.copyProperties(assignmentTaskDO, assignmentTaskBO);

    assignmentTaskBO.setModeText(MapUtils.getString(modeDict, assignmentTaskBO.getMode(), ""));
    assignmentTaskBO.setReplayRateUnitText(
        MapUtils.getString(rateUnitDict, assignmentTaskBO.getReplayRateUnit(), ""));
    assignmentTaskBO.setForwardActionText(
        MapUtils.getString(forwardActionDict, assignmentTaskBO.getForwardAction(), ""));
    assignmentTaskBO.setFilterConditionTypeText(
        MapUtils.getString(filterTypeDict, assignmentTaskBO.getFilterConditionType(), ""));

    assignmentTaskBO
        .setFilterStartTime(DateUtils.toStringISO8601(assignmentTaskDO.getFilterStartTime()));
    assignmentTaskBO
        .setFilterEndTime(DateUtils.toStringISO8601(assignmentTaskDO.getFilterEndTime()));

    AssignmentTaskRecordDO assignmentTaskRecordDO = assignmentTaskRecordDao
        .queryEarliestExecutionTaskRecord(assignmentTaskBO.getId());
    assignmentTaskBO.setExecutionStartTime(
        DateUtils.toStringISO8601(assignmentTaskRecordDO.getExecutionStartTime()));

    List<String> fpcSerialNumber = assignmentTaskRecordDao
        .queryAssignmentTaskRecordsByTaskId(assignmentTaskBO.getId()).stream()
        .map(AssignmentTaskRecordDO::getFpcSerialNumber).collect(Collectors.toList());
    assignmentTaskBO.setFpcSerialNumber(CsvUtils.convertCollectionToCSV(fpcSerialNumber));

    return assignmentTaskBO;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.AssignmentTaskService#queryAssignmentTaskByAssignTaskId(java.lang.String)
   */
  @Override
  public AssignmentTaskBO queryAssignmentTaskByAssignTaskId(String assignTaskId) {
    AssignmentTaskDO assignmentTaskDO = assignmentTaskDao
        .queryAssignmentTaskByAssignTaskId(assignTaskId);

    AssignmentTaskBO assignmentTaskBO = new AssignmentTaskBO();
    BeanUtils.copyProperties(assignmentTaskDO, assignmentTaskBO);

    return assignmentTaskBO;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.AssignmentTaskService#queryAssignmentTaskRecords(com.machloop.alpha.common.base.page.Pageable, java.lang.String)
   */
  @Override
  public Page<AssignmentTaskRecordBO> queryAssignmentTaskRecords(Pageable page, String taskId) {
    Map<String, String> assignmentStateDict = dictManager.getBaseDict()
        .getItemMap("task_policy_assignment_state");
    Map<String,
        String> executionStateDict = dictManager.getBaseDict().getItemMap("task_execution_state");

    Page<AssignmentTaskRecordDO> assignmentTaskRecordDOList = assignmentTaskRecordDao
        .queryAssignmentTaskRecords(page, taskId);

    // 任务下发记录总条数
    long totalElem = assignmentTaskRecordDOList.getTotalElements();

    // 获取设备信息集合
    List<String> fpcSerialNumbers = Lists
        .newArrayListWithExpectedSize(assignmentTaskRecordDOList.getSize());
    for (AssignmentTaskRecordDO assignmentTaskRecordDO : assignmentTaskRecordDOList) {
      fpcSerialNumbers.add(assignmentTaskRecordDO.getFpcSerialNumber());
    }
    List<FpcBO> fpcBOList = fpcService.queryFpcBySerialNumbers(fpcSerialNumbers, false);
    Map<String, FpcBO> fpcMap = Maps.newHashMapWithExpectedSize(fpcBOList.size());
    for (FpcBO fpcBO : fpcBOList) {
      fpcMap.put(fpcBO.getSerialNumber(), fpcBO);
    }

    List<AssignmentTaskRecordBO> assignmentTaskRecordBOList = Lists
        .newArrayListWithExpectedSize(assignmentTaskRecordDOList.getSize());
    for (AssignmentTaskRecordDO assignmentTaskRecordDO : assignmentTaskRecordDOList) {
      AssignmentTaskRecordBO assignmentTaskRecordBO = new AssignmentTaskRecordBO();
      BeanUtils.copyProperties(assignmentTaskRecordDO, assignmentTaskRecordBO);

      FpcBO fpcBO = fpcMap.get(assignmentTaskRecordBO.getFpcSerialNumber());
      assignmentTaskRecordBO.setFpcName(fpcBO.getName());
      assignmentTaskRecordBO.setFpcIp(fpcBO.getIp());
      assignmentTaskRecordBO.setConnectStatus(fpcBO.getConnectStatus());
      assignmentTaskRecordBO.setConnectStatusText(fpcBO.getConnectStatusText());
      assignmentTaskRecordBO.setAssignmentState(assignmentTaskRecordDO.getAssignmentState());
      assignmentTaskRecordBO.setAssignmentStateText(
          MapUtils.getString(assignmentStateDict, assignmentTaskRecordBO.getAssignmentState(), ""));
      assignmentTaskRecordBO.setExecutionState(assignmentTaskRecordDO.getExecutionState());
      assignmentTaskRecordBO.setExecutionStateText(
          MapUtils.getString(executionStateDict, assignmentTaskRecordBO.getExecutionState(), ""));
      assignmentTaskRecordBO.setExecutionStartTime(
          DateUtils.toStringISO8601(assignmentTaskRecordDO.getExecutionStartTime()));
      assignmentTaskRecordBO.setExecutionEndTime(
          DateUtils.toStringISO8601(assignmentTaskRecordDO.getExecutionEndTime()));

      assignmentTaskRecordBOList.add(assignmentTaskRecordBO);
    }

    return new PageImpl<>(assignmentTaskRecordBOList, page, totalElem);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.AssignmentTaskService#queryAssignmentTaskRecordsWithoutPage(java.lang.String)
   */
  @Override
  public List<AssignmentTaskRecordBO> queryAssignmentTaskRecordsWithoutPage(String taskId) {

    List<AssignmentTaskRecordDO> assignmentTaskRecordDOList = assignmentTaskRecordDao
        .queryAssignmentTaskRecordsByTaskId(taskId);

    // 获取设备信息集合
    List<String> fpcSerialNumbers = Lists
        .newArrayListWithExpectedSize(assignmentTaskRecordDOList.size());
    for (AssignmentTaskRecordDO assignmentTaskRecordDO : assignmentTaskRecordDOList) {
      fpcSerialNumbers.add(assignmentTaskRecordDO.getFpcSerialNumber());
    }
    List<FpcBO> fpcBOList = fpcService.queryFpcBySerialNumbers(fpcSerialNumbers, false);
    Map<String, FpcBO> fpcMap = Maps.newHashMapWithExpectedSize(fpcBOList.size());
    for (FpcBO fpcBO : fpcBOList) {
      fpcMap.put(fpcBO.getSerialNumber(), fpcBO);
    }

    List<AssignmentTaskRecordBO> assignmentTaskRecordBOList = Lists
        .newArrayListWithExpectedSize(assignmentTaskRecordDOList.size());
    for (AssignmentTaskRecordDO assignmentTaskRecordDO : assignmentTaskRecordDOList) {
      AssignmentTaskRecordBO assignmentTaskRecordBO = new AssignmentTaskRecordBO();
      BeanUtils.copyProperties(assignmentTaskRecordDO, assignmentTaskRecordBO);

      // 如果设备不存在则将该任务下发记录移出列表
      FpcBO fpcBO = fpcMap.get(assignmentTaskRecordBO.getFpcSerialNumber());
      if (fpcBO == null) {
        continue;
      }

      assignmentTaskRecordBO.setFpcName(fpcBO.getName());
      assignmentTaskRecordBO.setFpcIp(fpcBO.getIp());
      assignmentTaskRecordBO.setConnectStatus(fpcBO.getConnectStatus());
      assignmentTaskRecordBO.setExecutionStartTime(
          DateUtils.toStringISO8601(assignmentTaskRecordDO.getExecutionStartTime()));
      assignmentTaskRecordBO.setExecutionEndTime(
          DateUtils.toStringISO8601(assignmentTaskRecordDO.getExecutionEndTime()));

      assignmentTaskRecordBOList.add(assignmentTaskRecordBO);
    }

    return assignmentTaskRecordBOList;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.AssignmentTaskService#saveAssignmentTask(com.machloop.fpc.cms.center.appliance.bo.AssignmentTaskBO)
   */
  @Override
  public AssignmentTaskBO saveAssignmentTask(AssignmentTaskBO assignmentTaskBO) {
    AssignmentTaskDO existName = assignmentTaskDao
        .queryAssignmentTaskByName(assignmentTaskBO.getName());
    if (StringUtils.isNotBlank(existName.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "任务名称已经存在");
    }

    // 写入数据库
    AssignmentTaskDO assignmentTaskDO = new AssignmentTaskDO();
    BeanUtils.copyProperties(assignmentTaskBO, assignmentTaskDO);
    assignmentTaskDO
        .setFilterStartTime(DateUtils.parseISO8601Date(assignmentTaskBO.getFilterStartTime()));
    assignmentTaskDO
        .setFilterEndTime(DateUtils.parseISO8601Date(assignmentTaskBO.getFilterEndTime()));
    assignmentTaskDO.setFilterNetworkId("ALL");
    if (StringUtils.isNotBlank(assignmentTaskDO.getFilterBpf())
        && StringUtils.equals(assignmentTaskDO.getFilterTuple(), "[]")) {
      assignmentTaskDO.setFilterConditionType(CenterConstants.TRANSMIT_TASK_FILTER_TYPE_BPF);
    } else if (!StringUtils.equals(assignmentTaskDO.getFilterTuple(), "[]")
        && StringUtils.isBlank(assignmentTaskDO.getFilterBpf())) {
      assignmentTaskDO.setFilterConditionType(CenterConstants.TRANSMIT_TASK_FILTER_TYPE_TUPLE);
    } else {
      assignmentTaskDO.setFilterConditionType(CenterConstants.TRANSMIT_TASK_FILTER_TYPE_MIX);
    }
    // 模式为文件，重放相关字段置空
    if (!StringUtils.equals(assignmentTaskDO.getMode(),
        CenterConstants.TRANSMIT_TASK_MODE_REPLAY)) {
      assignmentTaskDO.setReplayRateUnit("0");
      assignmentTaskDO.setReplayNetif("");
      assignmentTaskDO.setReplayRate(0);
      assignmentTaskDO.setForwardAction("0");
    }
    assignmentTaskDO.setAssignTaskId("");

    if (StringUtils.isNotBlank(assignmentTaskDO.getFilterBpf())
        && !BpfCheck.isBpfValid(assignmentTaskDO.getFilterBpf())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "BPF规则错误");
    }

    // 保存任务
    assignmentTaskDao.saveAssignmentTask(assignmentTaskDO);

    // 下发任务到指定的探针设备（如果非直属探针设备，则下发到探针设备所属的cms）
    assignTask(assignmentTaskDO.getId(), assignmentTaskBO.getFpcSerialNumber(),
        assignmentTaskBO.getOperatorId());

    // 加入下发队列
    assignmentService.addAssignmentQueue(FpcCmsConstants.ASSIGNMENT_TYPE_TASK);

    return queryAssignmentTask(assignmentTaskDO.getId());
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.AssignmentTaskService#assignTask(java.lang.String, java.lang.String, java.lang.String)
   */
  @Transactional
  @Override
  public Tuple2<String, AssignmentTaskBO> assignTask(String taskId, String fpcSerialNumbers,
      String operatorId) {
    // 获取需要下发的设备ID集合
    List<String> fpcSerialNumberList = CsvUtils.convertCSVToList(fpcSerialNumbers);
    if (CollectionUtils.isEmpty(fpcSerialNumberList)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "请确认是否已选择设备");
    }

    // 获取本机直属，并且连接异常FPC设备序列号集合
    String currentDeviceSerialNumber = licenseService.queryDeviceSerialNumber();
    List<FpcBO> fpcStatus = fpcService.queryFpcBySerialNumbers(fpcSerialNumberList, false);
    List<String> offLineFpcIdList = fpcStatus.stream()
        .filter(item -> StringUtils.equals(item.getCmsSerialNumber(), currentDeviceSerialNumber)
            && StringUtils.equals(item.getConnectStatus(), FpcCmsConstants.CONNECT_STATUS_ABNORMAL))
        .map(FpcBO::getSerialNumber).collect(Collectors.toList());

    String assignmentId = IdGenerator.generateUUID();
    Date assignmentTime = DateUtils.now();

    // 保存下发动作
    // 更新重复的下发动作状态（查询同一任务、同一设备的下发记录，如果存在则将下发状态为等待下发的记录更改为取消下发）
    List<AssignmentActionDO> assignmentActions = assignmentActionDao
        .queryAssignmentActionsByTaskAction(taskId, FpcCmsConstants.ASSIGNMENT_TYPE_TASK,
            FpcCmsConstants.TASK_ACTION_TYPE_ASSIGNMENT);
    List<String> needResetActionIdList = Lists
        .newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    for (AssignmentActionDO assignmentActionDO : assignmentActions) {
      if (fpcSerialNumberList.contains(assignmentActionDO.getFpcSerialNumber())) {
        needResetActionIdList.add(assignmentActionDO.getId());
      }
    }
    if (CollectionUtils.isNotEmpty(needResetActionIdList)) {
      assignmentActionDao.batchCancelAssignment(needResetActionIdList);
    }

    // 保存新的下发动作
    List<AssignmentActionDO> assignmentActionList = Lists
        .newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    Map<String, String> taskMap = Maps.newHashMapWithExpectedSize(fpcSerialNumberList.size());
    for (String fpcSerialNumber : fpcSerialNumberList) {
      String messageId = IdGenerator.generateUUID();
      taskMap.put(fpcSerialNumber + "_" + taskId, messageId);

      AssignmentActionDO assignmentActionDO = new AssignmentActionDO();
      assignmentActionDO.setFpcSerialNumber(fpcSerialNumber);
      assignmentActionDO.setMessageId(messageId);
      assignmentActionDO.setAssignmentId(assignmentId);
      assignmentActionDO.setTaskPolicyId(taskId);
      assignmentActionDO.setType(FpcCmsConstants.ASSIGNMENT_TYPE_TASK);
      assignmentActionDO.setAction(FpcCmsConstants.TASK_ACTION_TYPE_ASSIGNMENT);
      // 如果设备为离线状态则将下发状态直接置为失败
      assignmentActionDO.setState(
          offLineFpcIdList.contains(fpcSerialNumber) ? CenterConstants.TASK_ASSIGNMENT_STATE_FAILED
              : CenterConstants.TASK_ASSIGNMENT_STATE_WAIT);
      assignmentActionDO.setAssignmentTime(assignmentTime);

      assignmentActionList.add(assignmentActionDO);
    }
    if (CollectionUtils.isNotEmpty(assignmentActionList)) {
      assignmentActionDao.batchSaveAssignmentActions(assignmentActionList);
    }

    // 保存下发记录
    // 更新重复的下发记录状态
    List<AssignmentTaskRecordDO> assignmentTasks = assignmentTaskRecordDao
        .queryAssignmentTaskRecordsByTaskId(taskId);

    // 需要重置的任务下发记录（设备在线的，即状态将置为等待下发）
    List<String> needResetIdsByOnlineDevice = Lists
        .newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    // 需要重置的任务下发记录（设备离线的，即状态将置为下发失败）
    List<String> needResetIdsByofflineDevice = Lists
        .newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);

    for (AssignmentTaskRecordDO assignmentTaskRecordDO : assignmentTasks) {
      if (fpcSerialNumberList.contains(assignmentTaskRecordDO.getFpcSerialNumber())) {
        if (offLineFpcIdList.contains(assignmentTaskRecordDO.getFpcSerialNumber())) {
          needResetIdsByofflineDevice.add(assignmentTaskRecordDO.getId());
        } else {
          needResetIdsByOnlineDevice.add(assignmentTaskRecordDO.getId());
        }
        fpcSerialNumberList.remove(assignmentTaskRecordDO.getFpcSerialNumber());
      }
    }
    if (CollectionUtils.isNotEmpty(needResetIdsByOnlineDevice)) {
      assignmentTaskRecordDao.batchResetAssignmentTaskRecord(needResetIdsByOnlineDevice, operatorId,
          assignmentTime, CenterConstants.TASK_ASSIGNMENT_STATE_WAIT);
    }
    if (CollectionUtils.isNotEmpty(needResetIdsByofflineDevice)) {
      assignmentTaskRecordDao.batchResetAssignmentTaskRecord(needResetIdsByofflineDevice,
          operatorId, assignmentTime, CenterConstants.TASK_ASSIGNMENT_STATE_FAILED);
    }

    // 保存新的下发记录
    List<AssignmentTaskRecordDO> assignmentTaskList = Lists
        .newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    for (String fpcSerialNumber : fpcSerialNumberList) {
      AssignmentTaskRecordDO assignmentTaskRecordDO = new AssignmentTaskRecordDO();
      assignmentTaskRecordDO.setFpcSerialNumber(fpcSerialNumber);
      assignmentTaskRecordDO.setTaskId(taskId);
      assignmentTaskRecordDO.setMessageId(taskMap.get(fpcSerialNumber + "_" + taskId));
      // 如果设备为离线状态则将下发状态直接置为失败
      assignmentTaskRecordDO.setAssignmentState(
          offLineFpcIdList.contains(fpcSerialNumber) ? CenterConstants.TASK_ASSIGNMENT_STATE_FAILED
              : CenterConstants.TASK_ASSIGNMENT_STATE_WAIT);
      assignmentTaskRecordDO.setExecutionState(CenterConstants.APPLIANCE_TRANSMITTASK_STATE_RUN);
      assignmentTaskRecordDO.setOperatorId(operatorId);
      assignmentTaskRecordDO.setAssignmentTime(assignmentTime);
      assignmentTaskList.add(assignmentTaskRecordDO);
    }
    if (CollectionUtils.isNotEmpty(assignmentTaskList)) {
      assignmentTaskRecordDao.batchSaveAssignmentTaskRecords(assignmentTaskList);
    }

    AssignmentTaskBO assignmentTaskBO = queryAssignmentTask(taskId);
    assignmentTaskBO.setFpcSerialNumber(fpcSerialNumbers);
    return Tuples.of(assignmentId, assignmentTaskBO);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.AssignmentTaskService#changeAssignmentState(java.lang.String, int)
   */
  @Transactional
  @Override
  public AssignmentTaskBO changeAssignmentState(String assignmentId, int optionType) {
    String originalState = "";
    String newState = "";
    switch (optionType) {
      case CenterConstants.ASSIGNMENT_ACTION_STOP:
        originalState = CenterConstants.TASK_ASSIGNMENT_STATE_WAIT;
        newState = CenterConstants.TASK_ASSIGNMENT_STATE_STOP;
        break;
      case CenterConstants.ASSIGNMENT_ACTION_CONTINUE:
        originalState = CenterConstants.TASK_ASSIGNMENT_STATE_STOP;
        newState = CenterConstants.TASK_ASSIGNMENT_STATE_WAIT;
        break;
      case CenterConstants.ASSIGNMENT_ACTION_CANCEL:
        originalState = CenterConstants.TASK_ASSIGNMENT_STATE_WAIT;
        newState = CenterConstants.TASK_ASSIGNMENT_STATE_CANCEL;
        break;
      default:
        return null;
    }

    List<AssignmentActionDO> assignmentActionList = assignmentActionDao
        .queryAssignmentActionsByAssignmentId(assignmentId);
    String taskId = assignmentActionList.get(0).getTaskPolicyId();
    // 更新下发记录状态
    assignmentTaskRecordDao.updateAssignmentState(taskId, originalState, newState);
    // 更新下发动作状态
    assignmentActionDao.updateAssignmentStateByAssignmentId(assignmentId, originalState, newState);

    AssignmentTaskBO assignmentTaskBO = queryAssignmentTask(taskId);
    assignmentTaskBO
        .setFpcSerialNumber(assignmentActionService.queryRelatedDevices(optionType, assignmentId));
    return assignmentTaskBO;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.AssignmentTaskService#updateAssignmentTask(com.machloop.fpc.cms.center.appliance.bo.AssignmentTaskBO)
   */
  @Override
  public AssignmentTaskBO updateAssignmentTask(AssignmentTaskBO assignmentTaskBO) {
    // 写入数据库
    AssignmentTaskDO assignmentTaskDO = new AssignmentTaskDO();
    BeanUtils.copyProperties(assignmentTaskBO, assignmentTaskDO);

    AssignmentTaskDO oldAssignmentTaskDO = assignmentTaskDao
        .queryAssignmentTask(assignmentTaskDO.getId(), false);
    if (StringUtils.isBlank(oldAssignmentTaskDO.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "下发任务不存在");
    }

    AssignmentTaskDO existName = assignmentTaskDao
        .queryAssignmentTaskByName(assignmentTaskBO.getName());
    if (StringUtils.isNotBlank(existName.getId())
        && !StringUtils.equals(assignmentTaskBO.getId(), existName.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "任务名称已经存在");
    }

    if (StringUtils.isNotBlank(assignmentTaskDO.getFilterBpf())
        && !BpfCheck.isBpfValid(assignmentTaskDO.getFilterBpf())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "BPF规则错误");
    }

    assignmentTaskDao.updateAssignmentTask(assignmentTaskDO);

    // 下发任务到指定的探针设备（如果非直属探针设备，则下发到探针设备所属的cms）
    assignTask(assignmentTaskDO.getId(), assignmentTaskBO.getFpcSerialNumber(),
        assignmentTaskBO.getOperatorId());

    // 加入下发队列
    assignmentService.addAssignmentQueue(FpcCmsConstants.ASSIGNMENT_TYPE_TASK);

    return queryAssignmentTask(assignmentTaskDO.getId());
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.AssignmentTaskService#updateAssignmentTaskByAssignTaskId(java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.util.Date, java.lang.String)
   */
  @Override
  public AssignmentTaskBO updateAssignmentTaskByAssignTaskId(String assignTaskId, String name,
      String filterTuple, String filterRaw, String description, Date assignTaskTime,
      String fpcSerialNumbers) {

    AssignmentTaskBO assignmentTaskBO = queryAssignmentTaskByAssignTaskId(assignTaskId);
    if (StringUtils.isBlank(assignmentTaskBO.getId())) {
      return assignmentTaskBO;
    }

    assignmentTaskDao.updateAssignmentTaskByAssignTaskId(assignTaskId, name, filterTuple, filterRaw,
        description, assignTaskTime);

    // 下发任务到指定的探针设备（如果非直属探针设备，则下发到探针设备所属的cms）
    assignTask(assignmentTaskBO.getId(), fpcSerialNumbers, assignmentTaskBO.getOperatorId());

    // 加入下发队列
    assignmentService.addAssignmentQueue(FpcCmsConstants.ASSIGNMENT_TYPE_TASK);

    return assignmentTaskBO;
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.AssignmentTaskService#updatePcapFileUrl(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public void updatePcapFileUrl(String taskId, String fpcSerialNumber, String pcapFileUrl) {
    assignmentTaskRecordDao.updatePcapFileUrl(taskId, fpcSerialNumber, pcapFileUrl);
  }

  /**
   * @see com.machloop.fpc.cms.center.appliance.service.AssignmentTaskService#deleteAssignmentTask(java.lang.String, java.lang.String)
   */
  @Transactional
  @Override
  public AssignmentTaskBO deleteAssignmentTask(String id, String operatorId) {
    AssignmentTaskBO assignmentTaskBO = queryAssignmentTask(id);
    if (StringUtils.isBlank(assignmentTaskBO.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "下发任务不存在");
    }

    // 删除任务
    assignmentTaskDao.deleteAssignmentTask(id, operatorId);

    // 将等待下发的任务状态置为取消下发
    assignmentActionDao.updateAssignmentStateByTaskPolicyId(id,
        FpcCmsConstants.ASSIGNMENT_TYPE_TASK, CenterConstants.TASK_ASSIGNMENT_STATE_WAIT,
        CenterConstants.TASK_ASSIGNMENT_STATE_CANCEL);
    assignmentTaskRecordDao.updateAssignmentState(id, CenterConstants.TASK_ASSIGNMENT_STATE_WAIT,
        CenterConstants.TASK_ASSIGNMENT_STATE_CANCEL);

    // 将删除动作插入动作表
    List<AssignmentTaskRecordDO> assignmentTaskList = assignmentTaskRecordDao
        .queryAssignmentTaskRecordsByTaskId(id);
    List<AssignmentActionDO> assignmentActionList = Lists
        .newArrayListWithExpectedSize(Constants.COL_DEFAULT_SIZE);
    String assignmentId = IdGenerator.generateUUID();
    Date assignmentTime = DateUtils.now();
    for (AssignmentTaskRecordDO assignmentTaskRecordDO : assignmentTaskList) {
      AssignmentActionDO assignmentActionDO = new AssignmentActionDO();
      assignmentActionDO.setFpcSerialNumber(assignmentTaskRecordDO.getFpcSerialNumber());
      assignmentActionDO.setMessageId(IdGenerator.generateUUID());
      assignmentActionDO.setAssignmentId(assignmentId);
      assignmentActionDO.setTaskPolicyId(id);
      assignmentActionDO.setType(FpcCmsConstants.ASSIGNMENT_TYPE_TASK);
      assignmentActionDO.setAction(FpcCmsConstants.TASK_ACTION_TYPE_DELETE);
      assignmentActionDO.setState(CenterConstants.TASK_ASSIGNMENT_STATE_WAIT);
      assignmentActionDO.setAssignmentTime(assignmentTime);

      assignmentActionList.add(assignmentActionDO);
    }
    if (CollectionUtils.isNotEmpty(assignmentActionList)) {
      assignmentActionDao.batchSaveAssignmentActions(assignmentActionList);
    }

    return assignmentTaskBO;
  }

}
