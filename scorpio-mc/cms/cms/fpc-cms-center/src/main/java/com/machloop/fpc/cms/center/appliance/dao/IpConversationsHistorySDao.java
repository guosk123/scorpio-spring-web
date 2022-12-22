package com.machloop.fpc.cms.center.appliance.dao;

import java.util.List;

import com.machloop.fpc.cms.center.appliance.data.IpConversationsHistoryDO;
import com.machloop.fpc.cms.center.appliance.vo.IpConversationsHistoryQueryVO;

/**
 * @author ChenXiao
 * create at 2022/10/11
 */
public interface IpConversationsHistorySDao {
  List<IpConversationsHistoryDO> queryIpConversationsHistories(
      IpConversationsHistoryQueryVO queryVO);

  IpConversationsHistoryDO queryIpConversationsHistory(String id);

  IpConversationsHistoryDO queryIpConversationsHistoryByName(String name);

  IpConversationsHistoryDO saveIpConversationsHistory(
      IpConversationsHistoryDO ipConversationsHistoryDO);

  int updateIpConversationsHistory(IpConversationsHistoryDO ipConversationsHistoryDO);

  int deleteIpConversationsHistory(String id, String operatorId);
}
