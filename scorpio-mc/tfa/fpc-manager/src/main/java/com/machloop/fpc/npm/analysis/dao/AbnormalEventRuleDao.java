package com.machloop.fpc.npm.analysis.dao;

import java.util.List;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.npm.analysis.data.AbnormalEventRuleDO;
import com.machloop.fpc.npm.analysis.vo.AbnormalEventRuleQueryVO;

/**
 * @author guosk
 *
 * create at 2021年7月16日, fpc-manager
 */
public interface AbnormalEventRuleDao {

  Page<AbnormalEventRuleDO> queryAbnormalEventRules(Pageable page,
      AbnormalEventRuleQueryVO queryVO);

  List<AbnormalEventRuleDO> queryAbnormalEventRules(String source);

  AbnormalEventRuleDO queryAbnormalEventRule(String id);

  AbnormalEventRuleDO saveAbnormalEventRule(AbnormalEventRuleDO abnormalEventRuleDO);

  int saveAbnormalEventRules(List<AbnormalEventRuleDO> abnormalEventRuleList);

  int updateAbnormalEventRule(AbnormalEventRuleDO abnormalEventRuleDO);

  int updateStatus(String id, String status, String operatorId);

  int deleteAbnormalEventRule(String id);

  int deleteAbnormalEventRules(String source);

}
