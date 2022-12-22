package com.machloop.fpc.manager.appliance.dao;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.manager.appliance.data.MailRuleDO;
import com.machloop.fpc.manager.appliance.vo.MailRuleQueryVO;

/**
 * @author "Minjiajun"
 *
 * create at 2022年10月28日, fpc-manager
 */
public interface MailRuleDao {

  Page<MailRuleDO> queryMailRules(MailRuleQueryVO queryVO, Pageable page);

  MailRuleDO queryMailRule(String id);

  MailRuleDO saveMailRule(MailRuleDO mailRuleDO);

  int updateMailRule(MailRuleDO mailRuleDO);

  int deleteMailRule(String id, String operatorId);

  int updateMailRuleState(String id, String state, String operatorId);
}
