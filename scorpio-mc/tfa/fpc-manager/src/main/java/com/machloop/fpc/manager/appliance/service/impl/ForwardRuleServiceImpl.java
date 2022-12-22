package com.machloop.fpc.manager.appliance.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.machloop.alpha.common.algorithm.bpf.BpfCheck;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.manager.appliance.bo.ForwardRuleBO;
import com.machloop.fpc.manager.appliance.dao.ForwardPolicyDao;
import com.machloop.fpc.manager.appliance.dao.ForwardRuleDao;
import com.machloop.fpc.manager.appliance.data.ForwardPolicyDO;
import com.machloop.fpc.manager.appliance.data.ForwardRuleDO;
import com.machloop.fpc.manager.appliance.service.ForwardRuleService;

/**
 * @author ChenXiao
 *
 * create at 2022/5/7 17:16,IntelliJ IDEA
 *
 */
@Service
public class ForwardRuleServiceImpl implements ForwardRuleService {

  private static final String DEFAULT_FORWARD_RULE_ID = "1";

  @Autowired
  private ForwardRuleDao forwardRuleDao;

  @Autowired
  private ForwardPolicyDao forwardPolicyDao;

  @Autowired
  private DictManager dictManager;

  @Override
  public Page<ForwardRuleBO> queryForwardRules(PageRequest page) {

    Page<ForwardRuleDO> forwardRules = forwardRuleDao.queryForwardRules(page);
    Map<String,
        String> actionDict = dictManager.getBaseDict().getItemMap("appliance_forward_action");

    long totalElem = forwardRules.getTotalElements();
    List<ForwardRuleBO> result = Lists.newArrayListWithCapacity(forwardRules.getSize());
    forwardRules.forEach(forwardRuleDO -> {
      ForwardRuleBO forwardRuleBO = new ForwardRuleBO();
      BeanUtils.copyProperties(forwardRuleDO, forwardRuleBO);
      forwardRuleBO.setCreateTime(DateUtils.toStringISO8601(forwardRuleDO.getCreateTime()));
      forwardRuleBO.setUpdateTime(DateUtils.toStringISO8601(forwardRuleDO.getUpdateTime()));
      forwardRuleBO.setDefaultActionText(
          MapUtils.getString(actionDict, forwardRuleBO.getDefaultAction(), ""));

      List<ForwardPolicyDO> forwardPolicyList = forwardPolicyDao
          .queryForwardPolicyByRuleId(forwardRuleBO.getId());

      forwardRuleBO.setReferenceCount(forwardPolicyList.size());

      result.add(forwardRuleBO);
    });

    return new PageImpl<>(result, page, totalElem);
  }

  @Override
  public ForwardRuleBO queryForwardRule(String id) {
    ForwardRuleDO forwardRuleDO = forwardRuleDao.queryForwardRule(id);
    ForwardRuleBO forwardRuleBO = new ForwardRuleBO();
    BeanUtils.copyProperties(forwardRuleDO, forwardRuleBO);

    Map<String,
        String> actionDict = dictManager.getBaseDict().getItemMap("appliance_forward_action");

    forwardRuleBO
        .setDefaultActionText(MapUtils.getString(actionDict, forwardRuleBO.getDefaultAction(), ""));
    forwardRuleBO.setCreateTime(DateUtils.toStringISO8601(forwardRuleDO.getCreateTime()));
    forwardRuleBO.setUpdateTime(DateUtils.toStringISO8601(forwardRuleDO.getUpdateTime()));

    return forwardRuleBO;

  }

  @Override
  public ForwardRuleBO saveForwardRule(ForwardRuleBO forwardRuleBO, String operatorId) {
    ForwardRuleDO exist = forwardRuleDao.queryForwardRuleByName(forwardRuleBO.getName());
    if (StringUtils.isNotBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "实时转发规则名称不能重复");
    }

    if (StringUtils.isNotBlank(forwardRuleBO.getExceptBpf())
        && !BpfCheck.isBpfValid(forwardRuleBO.getExceptBpf())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "BPF规则错误");
    }

    ForwardRuleDO forwardRuleDO = new ForwardRuleDO();
    BeanUtils.copyProperties(forwardRuleBO, forwardRuleDO);
    forwardRuleDO.setOperatorId(operatorId);

    forwardRuleDao.saveForwardRule(forwardRuleDO);

    BeanUtils.copyProperties(forwardRuleDO, forwardRuleBO);
    return forwardRuleBO;
  }

  @Override
  public ForwardRuleBO updateForwardRule(String id, ForwardRuleBO forwardRuleBO,
      String operatorId) {
    ForwardRuleDO exist = forwardRuleDao.queryForwardRule(id);
    if (StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "实时转发规则不存在");
    }

    if (StringUtils.equals(id, DEFAULT_FORWARD_RULE_ID)
        && !StringUtils.equals(exist.getName(), forwardRuleBO.getName())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "不允许变更默认规则名称");
    }

    ForwardRuleDO forwardRuleByName = forwardRuleDao
        .queryForwardRuleByName(forwardRuleBO.getName());
    if (StringUtils.isNotBlank(forwardRuleByName.getId())
        && !StringUtils.equals(id, forwardRuleByName.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "实时转发规则名称不能重复");
    }

    if (StringUtils.isNotBlank(forwardRuleBO.getExceptBpf())
        && !BpfCheck.isBpfValid(forwardRuleBO.getExceptBpf())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "BPF规则错误");
    }

    ForwardRuleDO forwardRuleDO = new ForwardRuleDO();
    forwardRuleBO.setId(id);
    BeanUtils.copyProperties(forwardRuleBO, forwardRuleDO);
    forwardRuleDO.setOperatorId(operatorId);

    forwardRuleDao.updateForwardRule(forwardRuleDO);

    return forwardRuleBO;
  }

  @Override
  public ForwardRuleBO deleteForwardRule(String id, String operatorId, boolean forceDelete) {
    ForwardRuleDO exist = forwardRuleDao.queryForwardRule(id);
    if (!forceDelete && StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "实时转发规则不存在");
    }

    if (!forceDelete && StringUtils.equals(id, DEFAULT_FORWARD_RULE_ID)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "不允许删除默认规则");
    }


    List<ForwardPolicyDO> forwardPolicyList = forwardPolicyDao.queryForwardPolicyByRuleId(id);
    if (!forceDelete && CollectionUtils.isNotEmpty(forwardPolicyList)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OPERATION_NOT_SUPPORT, "规则已被策略使用，不能删除");
    }

    forwardRuleDao.deleteForwardRule(id, operatorId);

    forwardPolicyDao.deleteForwardPolicyByRuleId(id, operatorId);

    ForwardRuleBO forwardRuleBO = new ForwardRuleBO();
    BeanUtils.copyProperties(exist, forwardRuleBO);
    return forwardRuleBO;
  }

  @Override
  public List<ForwardRuleBO> queryForwardRules() {
    List<ForwardRuleDO> forwardRules = forwardRuleDao.queryForwardRules();
    Map<String,
        String> actionDict = dictManager.getBaseDict().getItemMap("appliance_forward_action");
    List<ForwardRuleBO> result = Lists.newArrayListWithCapacity(forwardRules.size());
    forwardRules.forEach(forwardRuleDO -> {
      ForwardRuleBO forwardRuleBO = new ForwardRuleBO();
      BeanUtils.copyProperties(forwardRuleDO, forwardRuleBO);
      forwardRuleBO.setCreateTime(DateUtils.toStringISO8601(forwardRuleDO.getCreateTime()));
      forwardRuleBO.setUpdateTime(DateUtils.toStringISO8601(forwardRuleDO.getUpdateTime()));
      forwardRuleBO.setDefaultActionText(
          MapUtils.getString(actionDict, forwardRuleBO.getDefaultAction(), ""));

      result.add(forwardRuleBO);
    });

    return result;
  }


}
