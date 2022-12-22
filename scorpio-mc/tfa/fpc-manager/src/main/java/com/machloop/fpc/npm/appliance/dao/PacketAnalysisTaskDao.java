package com.machloop.fpc.npm.appliance.dao;

import java.util.List;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.npm.appliance.data.PacketAnalysisTaskDO;

/**
 * @author guosk
 *
 * create at 2021年6月16日, fpc-manager
 */
public interface PacketAnalysisTaskDao {

  Page<PacketAnalysisTaskDO> queryPacketAnalysisTasks(Pageable page, String name, String status);

  PacketAnalysisTaskDO queryPacketAnalysisTask(String id);

  PacketAnalysisTaskDO queryPacketAnalysisTaskByName(String name);

  int savePacketAnalysisTask(PacketAnalysisTaskDO packetAnalysisTaskDO);

  int deletePacketAnalysisTask(String id, String operatorId);

  List<PacketAnalysisTaskDO> queryPacketAnalysisTaskForRetApi(String name, String status);
}
