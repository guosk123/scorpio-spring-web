package com.machloop.fpc.npm.appliance.service;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.npm.appliance.bo.PacketAnalysisSubTaskBO;
import com.machloop.fpc.npm.appliance.bo.PacketAnalysisTaskBO;
import com.machloop.fpc.npm.appliance.bo.PacketAnalysisTaskLogBO;

/**
 * @author guosk
 * <p>
 * create at 2021年6月16日, fpc-manager
 */
public interface PacketAnalysisTaskService {

  Page<PacketAnalysisTaskBO> queryPacketAnalysisTasks(Pageable page, String name, String status);

  PacketAnalysisTaskBO queryPacketAnalysisTask(String id);

  Map<String, Object> queryPacketFileDirectory(String type, String filename, int count);

  PacketAnalysisTaskBO savePacketAnalysisTask(PacketAnalysisTaskBO packetAnalysisTaskBO,
      String operatorId);

  PacketAnalysisTaskBO deletePacketAnalysisTask(String id, String operatorId);

  /**********************************************************************************************
   *  子任务
   ********************************************************************************************/
  Page<PacketAnalysisSubTaskBO> queryPacketAnalysisSubTasks(Pageable page, String name,
      String taskId, String source);

  PacketAnalysisSubTaskBO queryPacketAnalysisSubTask(String id);

  String queryFileUploadUrl(HttpServletRequest request, String operatorId);

  PacketAnalysisSubTaskBO deletePacketAnalysisSubTask(String id, String operatorId);

  /**********************************************************************************************
   *  任务分析日志
   ********************************************************************************************/
  Page<PacketAnalysisTaskLogBO> queryPacketAnalysisLog(Pageable page, String taskId,
      String subTaskId);


}
