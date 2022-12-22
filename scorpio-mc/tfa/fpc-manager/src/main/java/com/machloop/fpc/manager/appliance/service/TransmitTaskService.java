package com.machloop.fpc.manager.appliance.service;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.manager.appliance.bo.TransmitTaskBO;
import com.machloop.fpc.manager.appliance.vo.TransmitTaskQueryVO;

/**
 * @author liumeng
 *
 * create at 2018年12月12日, fpc-manager
 */
public interface TransmitTaskService {

  Page<TransmitTaskBO> queryTransmitTasks(Pageable page, TransmitTaskQueryVO queryVO);

  TransmitTaskBO queryTransmitTask(String id);

  TransmitTaskBO saveTransmitTask(TransmitTaskBO transmitTaskBO, String operatorId, String source);

  TransmitTaskBO updateTransmitTask(TransmitTaskBO transmitTaskBO, String taskId,
      String operatorId);

  TransmitTaskBO redoTransmitTask(String id);

  TransmitTaskBO stopTransmitTask(String id);

  TransmitTaskBO deleteTransmitTask(String id, String operatorId);
}
