package com.machloop.fpc.manager.appliance.service;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.manager.appliance.bo.MailRuleBO;
import com.machloop.fpc.manager.appliance.vo.MailRuleQueryVO;

/**
 * @author "Minjiajun"
 *
 * create at 2022年10月27日, fpc-manager
 */
public interface MailRuleService {

  Page<MailRuleBO> queryMailRules(MailRuleQueryVO queryVO, Pageable page);

  MailRuleBO queryMailRule(String id);

  MailRuleBO saveMailRule(MailRuleBO mailRuleBO, String operatorId);

  MailRuleBO updateMailRule(String id, MailRuleBO mailRuleBO, String operatorId);

  MailRuleBO deleteMailRule(String id, String operatorId);

  MailRuleBO updateMailRuleState(String id, String state, String operatorId);
}
