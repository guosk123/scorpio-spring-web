package com.machloop.fpc.manager.appliance.dao;

import java.util.Date;
import java.util.List;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.manager.appliance.data.TransmitTaskDO;
import com.machloop.fpc.manager.appliance.vo.TransmitTaskQueryVO;

/**
 * @author liumeng
 *
 * create at 2018年12月12日, fpc-manager
 */
public interface TransmitTaskDao {

  Page<TransmitTaskDO> queryTransmitTasks(Pageable page, TransmitTaskQueryVO queryVO);

  List<TransmitTaskDO> queryAssignmentTasks();

  TransmitTaskDO queryTransmitTask(String id);

  TransmitTaskDO queryTransmitTaskByName(String name);

  TransmitTaskDO queryTransmitTaskByAssignTaskId(String assignTaskId);

  TransmitTaskDO saveTransmitTask(TransmitTaskDO transmitTaskDO);

  int updateTransmitTask(TransmitTaskDO transmitTaskDO);

  int updateTransmitTaskByAssignTaskId(String assignTaskId, String name, String filterTuple,
      String filterRaw, String description, Date assignTaskTime);

  int updateTransmitTaskExecutionDownloadUrl(String id, String executionDownloadUrl);

  int redoTransmitTask(String id);

  int stopTransmitTask(String id);

  int deleteTransmitTask(String id, String operatorId);

  int deleteTransmitTaskByAssignTaskId(String assignTaskId, String operatorId);

  List<TransmitTaskDO> queryTransmitTasksByMode(String mode);
}
