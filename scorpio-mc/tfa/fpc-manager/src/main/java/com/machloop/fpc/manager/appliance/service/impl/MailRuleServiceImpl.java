package com.machloop.fpc.manager.appliance.service.impl;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.manager.appliance.bo.MailRuleBO;
import com.machloop.fpc.manager.appliance.dao.MailRuleDao;
import com.machloop.fpc.manager.appliance.data.MailRuleDO;
import com.machloop.fpc.manager.appliance.service.MailRuleService;
import com.machloop.fpc.manager.appliance.vo.MailRuleQueryVO;

/**
 * @author "Minjiajun"
 *
 * create at 2022年10月28日, fpc-manager
 */
@Service
public class MailRuleServiceImpl implements MailRuleService {

  @Autowired
  private MailRuleDao mailRuleDao;

  /**
   * @see com.machloop.fpc.manager.appliance.service.MailRuleService#queryMailRules(com.machloop.fpc.manager.appliance.vo.MailRuleQueryVO, com.machloop.alpha.common.base.page.Pageable)
   */
  @Override
  public Page<MailRuleBO> queryMailRules(MailRuleQueryVO queryVO, Pageable page) {

    Page<MailRuleDO> mailRuleDOPage = mailRuleDao.queryMailRules(queryVO, page);
    long totalElem = mailRuleDOPage.getTotalElements();

    List<MailRuleBO> mailRuleBOList = Lists.newArrayListWithCapacity(mailRuleDOPage.getSize());
    for (MailRuleDO mailRuleDO : mailRuleDOPage) {
      MailRuleBO mailRuleBO = new MailRuleBO();
      BeanUtils.copyProperties(mailRuleDO, mailRuleBO);

      mailRuleBO.setCreateTime(DateUtils.toStringISO8601(mailRuleDO.getCreateTime()));
      mailRuleBO.setUpdateTime(DateUtils.toStringISO8601(mailRuleDO.getUpdateTime()));

      mailRuleBOList.add(mailRuleBO);
    }

    return new PageImpl<>(mailRuleBOList, page, totalElem);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.MailRuleService#queryMailRule(java.lang.String)
   */
  @Override
  public MailRuleBO queryMailRule(String id) {
    MailRuleBO mailRuleBO = new MailRuleBO();

    MailRuleDO mailRuleDO = mailRuleDao.queryMailRule(id);
    BeanUtils.copyProperties(mailRuleDO, mailRuleBO);

    mailRuleBO.setCreateTime(DateUtils.toStringISO8601(mailRuleDO.getCreateTime()));
    mailRuleBO.setUpdateTime(DateUtils.toStringISO8601(mailRuleDO.getUpdateTime()));

    return mailRuleBO;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.MailRuleService#saveMailRule(com.machloop.fpc.manager.appliance.bo.MailRuleBO, java.lang.String)
   */
  @Override
  public MailRuleBO saveMailRule(MailRuleBO mailRuleBO, String operatorId) {

    // 写入数据库
    MailRuleDO mailRuleDO = new MailRuleDO();
    BeanUtils.copyProperties(mailRuleBO, mailRuleDO);
    mailRuleDO.setOperatorId(operatorId);
    mailRuleDO = mailRuleDao.saveMailRule(mailRuleDO);

    return queryMailRule(mailRuleDO.getId());
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.MailRuleService#updateMailRule(java.lang.String, com.machloop.fpc.manager.appliance.bo.MailRuleBO, java.lang.String)
   */
  @Override
  public MailRuleBO updateMailRule(String id, MailRuleBO mailRuleBO, String operatorId) {

    // 写入数据库
    MailRuleDO mailRuleDO = new MailRuleDO();
    mailRuleBO.setId(id);
    BeanUtils.copyProperties(mailRuleBO, mailRuleDO);
    mailRuleDO.setOperatorId(operatorId);
    mailRuleDao.updateMailRule(mailRuleDO);

    return queryMailRule(id);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.MailRuleService#deleteMailRule(java.lang.String, java.lang.String)
   */
  @Override
  public MailRuleBO deleteMailRule(String id, String operatorId) {
    MailRuleBO mailRuleBO = queryMailRule(id);

    mailRuleDao.deleteMailRule(id, operatorId);

    return mailRuleBO;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.service.MailRuleService#updateMailRuleState(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public MailRuleBO updateMailRuleState(String id, String state, String operatorId) {
    MailRuleDO exist = mailRuleDao.queryMailRule(id);
    if (StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "邮件规则不存在");
    }

    if (!StringUtils.equalsAny(state, Constants.BOOL_YES, Constants.BOOL_NO)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "不合法的启用状态");
    }

    mailRuleDao.updateMailRuleState(id, state, operatorId);

    return queryMailRule(id);
  }

}
