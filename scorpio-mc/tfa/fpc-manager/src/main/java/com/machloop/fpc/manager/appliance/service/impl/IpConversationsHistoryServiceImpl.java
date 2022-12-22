package com.machloop.fpc.manager.appliance.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.fpc.manager.appliance.bo.IpConversationsHistoryBO;
import com.machloop.fpc.manager.appliance.dao.IpConversationsHistorySDao;
import com.machloop.fpc.manager.appliance.data.IpConversationsHistoryDO;
import com.machloop.fpc.manager.appliance.service.IpConversationsHistoryService;
import com.machloop.fpc.manager.appliance.vo.IpConversationsHistoryQueryVO;

/**
 * @author chenxiao
 * create at 2022/7/11
 */

@Service
public class IpConversationsHistoryServiceImpl implements IpConversationsHistoryService {


  @Autowired
  private IpConversationsHistorySDao ipConversationsHistorySDao;


  @Override
  public List<IpConversationsHistoryBO> queryIpConversationsHistories(
      IpConversationsHistoryQueryVO queryVO) {

    List<IpConversationsHistoryDO> ipConversationsHistoryDOS = ipConversationsHistorySDao
        .queryIpConversationsHistories(queryVO);

    List<IpConversationsHistoryBO> result = Lists
        .newArrayListWithCapacity(ipConversationsHistoryDOS.size());
    for (IpConversationsHistoryDO ipConversationsHistoryDO : ipConversationsHistoryDOS) {
      IpConversationsHistoryBO ipConversationsHistoryBO = new IpConversationsHistoryBO();
      BeanUtils.copyProperties(ipConversationsHistoryDO, ipConversationsHistoryBO);
      result.add(ipConversationsHistoryBO);
    }

    return result;

  }

  @Override
  public IpConversationsHistoryBO queryIpConversationsHistory(String id) {

    IpConversationsHistoryDO ipConversationsHistoryDO = ipConversationsHistorySDao
        .queryIpConversationsHistory(id);
    IpConversationsHistoryBO ipConversationsHistoryBO = new IpConversationsHistoryBO();
    BeanUtils.copyProperties(ipConversationsHistoryDO, ipConversationsHistoryBO);

    return ipConversationsHistoryBO;
  }

  @Override
  public IpConversationsHistoryBO saveIpConversationsHistory(
      IpConversationsHistoryBO ipConversationsHistoryBO, String operatorId) {

    IpConversationsHistoryDO existIpConversationsHistoryDO = ipConversationsHistorySDao
        .queryIpConversationsHistoryByName(ipConversationsHistoryBO.getName());
    if (StringUtils.isNotBlank(existIpConversationsHistoryDO.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "历史画布名称已经存在");
    }
    IpConversationsHistoryDO ipConversationsHistoryDO = new IpConversationsHistoryDO();
    BeanUtils.copyProperties(ipConversationsHistoryBO, ipConversationsHistoryDO);
    ipConversationsHistoryDO.setOperatorId(operatorId);
    ipConversationsHistorySDao.saveIpConversationsHistory(ipConversationsHistoryDO);


    return queryIpConversationsHistory(ipConversationsHistoryDO.getId());
  }

  @Override
  public IpConversationsHistoryBO updateIpConversationsHistory(
      IpConversationsHistoryBO ipConversationsHistoryBO, String id, String operatorId) {
    IpConversationsHistoryDO oldIpConversationsHistory = ipConversationsHistorySDao
        .queryIpConversationsHistory(id);
    if (StringUtils.isBlank(oldIpConversationsHistory.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "历史画布不存在");
    }
    IpConversationsHistoryDO existIpConversationsHistoryDO = ipConversationsHistorySDao
        .queryIpConversationsHistoryByName(ipConversationsHistoryBO.getName());
    if (StringUtils.isNotBlank(existIpConversationsHistoryDO.getId())
        && !StringUtils.equals(id, existIpConversationsHistoryDO.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "历史画布名称已经存在");
    }

    List<Map<String, Object>> oldData = JsonHelper.deserialize(oldIpConversationsHistory.getData(),
        new TypeReference<List<Map<String, Object>>>() {
        }, false);
    List<Map<String, Object>> newData = JsonHelper.deserialize(ipConversationsHistoryBO.getData(),
        new TypeReference<List<Map<String, Object>>>() {
        }, false);
    Map<String, Map<String, Object>> oldDataMap = new HashMap<>();
    oldData.forEach(map -> oldDataMap.put((String) map.get("source") + map.get("target"), map));
    newData.forEach(map -> oldDataMap.put((String) map.get("source") + map.get("target"), map));
    ipConversationsHistoryBO.setData(JsonHelper.serialize(new ArrayList<>(oldDataMap.values())));

    IpConversationsHistoryDO ipConversationsHistoryDO = new IpConversationsHistoryDO();
    BeanUtils.copyProperties(ipConversationsHistoryBO, ipConversationsHistoryDO);
    ipConversationsHistoryDO.setOperatorId(operatorId);
    ipConversationsHistorySDao.updateIpConversationsHistory(ipConversationsHistoryDO);

    return queryIpConversationsHistory(id);
  }

  @Override
  public IpConversationsHistoryBO deleteIpConversationHistory(
      IpConversationsHistoryBO ipConversationsHistoryBO, String id, String operatorId) {
    IpConversationsHistoryDO oldIpConversationsHistory = ipConversationsHistorySDao
        .queryIpConversationsHistory(id);
    if (StringUtils.isBlank(oldIpConversationsHistory.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "历史画布不存在");
    }
    IpConversationsHistoryDO existIpConversationsHistoryDO = ipConversationsHistorySDao
        .queryIpConversationsHistoryByName(ipConversationsHistoryBO.getName());
    if (StringUtils.isNotBlank(existIpConversationsHistoryDO.getId())
        && !StringUtils.equals(id, existIpConversationsHistoryDO.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "历史画布名称已经存在");
    }
    IpConversationsHistoryDO ipConversationsHistoryDO = new IpConversationsHistoryDO();
    BeanUtils.copyProperties(ipConversationsHistoryBO, ipConversationsHistoryDO);
    ipConversationsHistoryDO.setOperatorId(operatorId);
    ipConversationsHistorySDao.updateIpConversationsHistory(ipConversationsHistoryDO);

    return queryIpConversationsHistory(id);
  }

  @Override
  public IpConversationsHistoryBO deleteIpConversationsHistory(String id, String operatorId) {
    IpConversationsHistoryDO oldIpConversationsHistory = ipConversationsHistorySDao
        .queryIpConversationsHistory(id);
    if (StringUtils.isNotBlank(oldIpConversationsHistory.getId())) {
      ipConversationsHistorySDao.deleteIpConversationsHistory(id, operatorId);
    }
    IpConversationsHistoryBO ipConversationsHistoryBO = new IpConversationsHistoryBO();
    BeanUtils.copyProperties(oldIpConversationsHistory, ipConversationsHistoryBO);
    return ipConversationsHistoryBO;
  }
}
