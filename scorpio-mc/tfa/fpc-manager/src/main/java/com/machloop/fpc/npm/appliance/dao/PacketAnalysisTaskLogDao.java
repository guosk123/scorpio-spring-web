package com.machloop.fpc.npm.appliance.dao;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.npm.appliance.data.PacketAnalysisTaskLogDO;

/**
 * @author guosk
 *
 * create at 2022年3月15日, fpc-manager
 */
public interface PacketAnalysisTaskLogDao {

  Page<PacketAnalysisTaskLogDO> queryAnalysisLog(Pageable page, String taskId, String subTaskId);

}
