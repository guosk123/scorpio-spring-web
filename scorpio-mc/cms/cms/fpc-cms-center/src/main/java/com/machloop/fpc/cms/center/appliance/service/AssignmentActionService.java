package com.machloop.fpc.cms.center.appliance.service;

import java.util.List;

import com.machloop.fpc.cms.center.appliance.bo.AssignmentActionBO;
import com.machloop.fpc.cms.grpc.CentralProto.AssignResult;

public interface AssignmentActionService {

  /**
   * 查询下发状态
   * @param assignmentId
   * @return
   */
  List<AssignmentActionBO> queryAssignmentActions(String assignmentId);

  /**
   * 获取本次操作类型所涉及到的设备
   * @param optionType
   * @param assignmentId
   * @return
   */
  String queryRelatedDevices(int optionType, String assignmentId);

  /**
   * 更新下发状态
   * @param assignResultList
   */
  void updateAssignmentActions(List<AssignResult> assignResultList);

}
