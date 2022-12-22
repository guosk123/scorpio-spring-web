package com.machloop.fpc.cms.center.appliance.service;

import java.util.List;

import com.machloop.fpc.cms.center.appliance.bo.IpConversationsHistoryBO;
import com.machloop.fpc.cms.center.appliance.vo.IpConversationsHistoryQueryVO;

/**
 * @author ChenXiao
 * create at 2022/10/11
 */
public interface IpConversationsHistoryService {
  List<IpConversationsHistoryBO> queryIpConversationsHistories(
      IpConversationsHistoryQueryVO queryVO);

  IpConversationsHistoryBO queryIpConversationsHistory(String id);


  IpConversationsHistoryBO saveIpConversationsHistory(
      IpConversationsHistoryBO ipConversationsHistoryBO, String operatorId);

  IpConversationsHistoryBO updateIpConversationsHistory(
      IpConversationsHistoryBO ipConversationsHistoryBO, String id, String operatorId);

  IpConversationsHistoryBO deleteIpConversationsHistory(String id, String operatorId);

  IpConversationsHistoryBO deleteIpConversationHistory(
      IpConversationsHistoryBO ipConversationsHistoryBO, String id, String operatorId);
}
