package com.machloop.fpc.manager.appliance.dao;

import java.util.List;

import com.machloop.fpc.manager.appliance.data.IpConversationsHistoryDO;
import com.machloop.fpc.manager.appliance.vo.IpConversationsHistoryQueryVO;

/**
 * @author chenxiao
 * create at 2022/7/11
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
