package com.machloop.fpc.manager.appliance.service.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.appliance.bo.ForwardPolicyBO;
import com.machloop.fpc.manager.appliance.dao.ForwardPolicyDao;
import com.machloop.fpc.manager.appliance.data.ForwardPolicyDO;
import com.machloop.fpc.manager.appliance.service.ForwardPolicyService;
import com.machloop.fpc.manager.metric.dao.MetricForwardDataRecordDao;
import com.machloop.fpc.npm.appliance.dao.NetworkPolicyDao;
import com.machloop.fpc.npm.appliance.data.NetworkPolicyDO;

/**
 * @author ChenXiao
 *
 * create at 2022/5/11 23:02,IntelliJ IDEA
 *
 */
@Service
public class ForwardPolicyServiceImpl implements ForwardPolicyService {

  private static final String DEFAULT_FORWARD_POLICY_ID = "1";

  @Autowired
  private ForwardPolicyDao forwardPolicyDao;

  @Autowired
  private NetworkPolicyDao networkPolicyDao;

  @Autowired
  private MetricForwardDataRecordDao metricForwardDataRecordDao;

  @Override
  public Page<ForwardPolicyBO> queryForwardPolicies(PageRequest page) {
    Page<ForwardPolicyDO> forwardPolicies = forwardPolicyDao.queryForwardPolicies(page);
    List<NetworkPolicyDO> networkPolicys = networkPolicyDao
        .queryNetworkPolicyByPolicyType(FpcConstants.APPLIANCE_NETWORK_POLICY_FORWARD);
    Map<String, List<String>> networkPolicyMap = Maps
        .newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    networkPolicys.forEach(networkPolicy -> {
      String policyId = networkPolicy.getPolicyId();
      if (networkPolicyMap.get(policyId) == null) {
        List<String> networkIds = new ArrayList<>();
        networkIds.add(networkPolicy.getNetworkId());
        networkPolicyMap.put(policyId, networkIds);
      } else {
        networkPolicyMap.get(policyId).add(networkPolicy.getNetworkId());
      }
    });
    Map<String, String> networkIdMap = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    for (Map.Entry<String, List<String>> entry : networkPolicyMap.entrySet()) {
      String networkIds = JsonHelper.serialize(entry.getValue());
      networkIdMap.put(entry.getKey(), networkIds);
    }


    long totalElem = forwardPolicies.getTotalElements();
    List<ForwardPolicyBO> result = Lists.newArrayListWithCapacity(forwardPolicies.getSize());
    forwardPolicies.forEach(forwardPolicyDO -> {
      ForwardPolicyBO forwardPolicyBO = new ForwardPolicyBO();
      BeanUtils.copyProperties(forwardPolicyDO, forwardPolicyBO);
      forwardPolicyBO.setCreateTime(DateUtils.toStringISO8601(forwardPolicyDO.getCreateTime()));
      forwardPolicyBO.setUpdateTime(DateUtils.toStringISO8601(forwardPolicyDO.getUpdateTime()));
      forwardPolicyBO.setNetworkId(networkIdMap.get(forwardPolicyDO.getId()));


      Map<String, Object> bandWidthByPolicyIdMap = metricForwardDataRecordDao
          .queryBandWidthByPolicyId(forwardPolicyDO.getId());
      String timestamp = MapUtils.getString(bandWidthByPolicyIdMap, "timestamp");
      forwardPolicyBO.setMetricTime(timestamp);

      long currentBytes = MapUtils.getLongValue(bandWidthByPolicyIdMap, "forward_total_bytes", 0L);
      BigDecimal bg = new BigDecimal(
          currentBytes * Constants.BYTE_BITS / (double) Constants.ONE_MINUTE_SECONDS);
      forwardPolicyBO.setTotalBandWidth(bg.setScale(2, RoundingMode.HALF_UP).doubleValue());


      result.add(forwardPolicyBO);
    });

    return new PageImpl<>(result, page, totalElem);


  }

  @Override
  public ForwardPolicyBO queryForwardPolicy(String id) {
    ForwardPolicyDO forwardPolicyDO = forwardPolicyDao.queryForwardPolicy(id);
    List<NetworkPolicyDO> networkPolicys = networkPolicyDao.queryNetworkPolicyByPolicyId(
        forwardPolicyDO.getId(), FpcConstants.APPLIANCE_NETWORK_POLICY_FORWARD);
    List<String> networkIdList = new ArrayList<>();
    networkPolicys.forEach(networkPolicy -> {
      networkIdList.add(networkPolicy.getNetworkId());
    });


    ForwardPolicyBO forwardPolicyBO = new ForwardPolicyBO();
    BeanUtils.copyProperties(forwardPolicyDO, forwardPolicyBO);

    forwardPolicyBO.setCreateTime(DateUtils.toStringISO8601(forwardPolicyDO.getCreateTime()));
    forwardPolicyBO.setUpdateTime(DateUtils.toStringISO8601(forwardPolicyDO.getUpdateTime()));
    forwardPolicyBO.setNetworkId(JsonHelper.serialize(networkIdList));

    Map<String, Object> bandWidthByPolicyIdMap = metricForwardDataRecordDao
        .queryBandWidthByPolicyId(forwardPolicyDO.getId());
    String timestamp = MapUtils.getString(bandWidthByPolicyIdMap, "timestamp");
    forwardPolicyBO.setMetricTime(timestamp);

    long currentBytes = MapUtils.getLongValue(bandWidthByPolicyIdMap, "forward_total_bytes", 0L);
    BigDecimal bg = new BigDecimal(
        currentBytes * Constants.BYTE_BITS / (double) Constants.ONE_MINUTE_SECONDS);
    forwardPolicyBO.setTotalBandWidth(bg.setScale(2, RoundingMode.HALF_UP).doubleValue());

    return forwardPolicyBO;
  }

  @Override
  public ForwardPolicyBO saveForwardPolicy(ForwardPolicyBO forwardPolicyBO, String operatorId) {
    ForwardPolicyDO exist = forwardPolicyDao.queryForwardPolicyByName(forwardPolicyBO.getName());
    if (StringUtils.isNotBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "实时转发策略名称不能重复");
    }
    ForwardPolicyDO forwardPolicyDO = new ForwardPolicyDO();
    BeanUtils.copyProperties(forwardPolicyBO, forwardPolicyDO);
    forwardPolicyDO.setOperatorId(operatorId);

    // 保存之前先要判断网络策略表里同一networkId对应的策略是否有4个，如果有则报错
    List<String> networkIdList = JsonHelper.deserialize(forwardPolicyBO.getNetworkId(),
        new TypeReference<List<String>>() {
        });
    for (String networkId : networkIdList) {
      List<NetworkPolicyDO> networkPolicyDOList = networkPolicyDao
          .queryNetworkPolicyByNetworkIdAndPolicyType(networkId,
              FpcConstants.APPLIANCE_NETWORK_POLICY_FORWARD);
      if (networkPolicyDOList.size() == 4) {
        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "同一网络配置策略超过4个");
      }
    }

    // 新建策略所选网络组中的网络对应的策略没有到达4的限制，所以可以继续保存策略
    forwardPolicyDao.saveForwardPolicy(forwardPolicyDO);

    for (String networkId : networkIdList) {
      networkPolicyDao.saveNetworkPolicy(networkId, forwardPolicyDO.getId(),
          FpcConstants.APPLIANCE_NETWORK_POLICY_FORWARD, operatorId);
    }

    BeanUtils.copyProperties(forwardPolicyDO, forwardPolicyBO);

    return forwardPolicyBO;
  }

  @Override
  public ForwardPolicyBO updateForwardPolicy(String id, ForwardPolicyBO forwardPolicyBO,
      String operatorId) {
    ForwardPolicyDO exist = forwardPolicyDao.queryForwardPolicy(id);
    if (StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "实时转发策略不存在");
    }

    if (StringUtils.equals(id, DEFAULT_FORWARD_POLICY_ID)
        && !StringUtils.equals(exist.getName(), forwardPolicyBO.getName())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "不允许变更默认策略名称");
    }

    ForwardPolicyDO forwardPolicyByName = forwardPolicyDao
        .queryForwardPolicyByName(forwardPolicyBO.getName());
    if (StringUtils.isNotBlank(forwardPolicyByName.getId())
        && !StringUtils.equals(id, forwardPolicyByName.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "实时转发策略名称不能重复");
    }

    ForwardPolicyDO forwardPolicyDO = new ForwardPolicyDO();
    forwardPolicyBO.setId(id);
    BeanUtils.copyProperties(forwardPolicyBO, forwardPolicyDO);
    forwardPolicyDO.setOperatorId(operatorId);

    // 更新之后 保存之前先要判断网络策略表里同一networkId对应的策略是否有4个，如果有则报错
    List<String> networkIdList = JsonHelper.deserialize(forwardPolicyBO.getNetworkId(),
        new TypeReference<List<String>>() {
        });
    for (String networkId : networkIdList) {
      List<NetworkPolicyDO> networkPolicyDOList = networkPolicyDao
          .queryNetworkPolicyByNetworkIdAndPolicyType(networkId,
              FpcConstants.APPLIANCE_NETWORK_POLICY_FORWARD);
      if (networkPolicyDOList.size() > 4) {
        throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "同一网络配置策略超过4个");
      }
    }

    // 修改之后的策略所选网络组中的网络对应的策略没有到达4的限制，所以可以继续更新策略

    forwardPolicyDao.updateForwardPolicy(forwardPolicyDO);
    networkPolicyDao.deleteNetworkPolicyByPolicyId(forwardPolicyDO.getId(),
        FpcConstants.APPLIANCE_NETWORK_POLICY_FORWARD);

    for (String networkId : networkIdList) {
      networkPolicyDao.saveNetworkPolicy(networkId, forwardPolicyDO.getId(),
          FpcConstants.APPLIANCE_NETWORK_POLICY_FORWARD, operatorId);
    }

    return forwardPolicyBO;
  }

  @Override
  public ForwardPolicyBO deleteForwardPolicy(String id, String operatorId, boolean forceDelete) {
    ForwardPolicyDO exist = forwardPolicyDao.queryForwardPolicy(id);
    if (!forceDelete && StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "实时转发策略不存在");
    }

    if (!forceDelete && StringUtils.equals(id, DEFAULT_FORWARD_POLICY_ID)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "不允许删除默认策略");
    }


    forwardPolicyDao.deleteForwardPolicy(id, operatorId);

    networkPolicyDao.deleteNetworkPolicyByPolicyId(id,
        FpcConstants.APPLIANCE_NETWORK_POLICY_FORWARD);


    ForwardPolicyBO forwardPolicyBO = new ForwardPolicyBO();
    BeanUtils.copyProperties(exist, forwardPolicyBO);
    return forwardPolicyBO;
  }

  @Override
  public ForwardPolicyBO changeForwardPolicy(String id, String state, String operatorId,
      boolean forceChange) {
    ForwardPolicyDO exist = forwardPolicyDao.queryForwardPolicy(id);
    if (!forceChange && StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "实时转发策略不存在");
    }

    if (!forceChange && StringUtils.equals(id, DEFAULT_FORWARD_POLICY_ID)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "不允许改变默认策略");
    }

    // 改变捕获规则
    forwardPolicyDao.changeForwardPolicy(id, state, operatorId);


    ForwardPolicyBO forwardPolicyBO = new ForwardPolicyBO();
    BeanUtils.copyProperties(exist, forwardPolicyBO);
    return forwardPolicyBO;

  }
}
