package com.machloop.fpc.npm.appliance.dao;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.npm.appliance.data.PacketAnalysisSubTaskDO;

/**
 * @author guosk
 *
 * create at 2021年6月16日, fpc-manager
 */
public interface PacketAnalysisSubTaskDao {

  Page<PacketAnalysisSubTaskDO> queryPacketAnalysisSubTasks(Pageable page, String name,
      String taskId, String source);

  PacketAnalysisSubTaskDO queryPacketAnalysisSubTask(String id);

  int deleteSubTaskById(String id, String operatorId);

  int deleteSubTaskByTaskId(String mainTaskId, String operatorId);

}
