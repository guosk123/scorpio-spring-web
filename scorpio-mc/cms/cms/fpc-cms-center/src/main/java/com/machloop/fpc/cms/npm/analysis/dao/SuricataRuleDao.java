package com.machloop.fpc.cms.npm.analysis.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.fpc.cms.npm.analysis.data.SuricataRuleDO;
import com.machloop.fpc.cms.npm.analysis.vo.SuricataRuleQueryVO;

import reactor.util.function.Tuple4;

/**
 * @author chenshimiao
 * <p>
 * create at 2022/10/10 2:59 PM,cms
 * @version 1.0
 */
public interface SuricataRuleDao {
  Page<SuricataRuleDO> querySuricataRules(PageRequest page, SuricataRuleQueryVO queryVO);

  List<String> queryRuleSource();

  List<String> querySuricataRuleIds();

  List<Integer> querySuricataRuleIds(SuricataRuleQueryVO queryVO);

  SuricataRuleDO querySuricataRule(int sid);

  List<String> querySuricataRule(List<Integer> sids);

  List<SuricataRuleDO> querySuricataRulesBySids(List<Integer> sids);

  List<SuricataRuleDO> querySuricataRulesByIds(List<String> splitId);

  List<Integer> querySuricataRule();

  Map<String, Integer> statisticsByClasstype();

  Map<String, Integer> statisticsByMitreTactic();

  Map<String, Integer> statisticsByMitreTechnique();

  Map<Integer, Tuple4<Integer, String, Date, String>> querySuricataRuleTuple4();

  List<String> querySuricataRule(Date beforeTime);

  List<SuricataRuleDO> saveSuricataRules(List<SuricataRuleDO> suricataRuleList);

  int updateSuricataRule(SuricataRuleDO suricataRuleDO);

  SuricataRuleDO saveSuricataRule(SuricataRuleDO suricataRuleDO);

  int updateState(List<String> sids, String state, String operatorId);

  void updateBatchSuricataRule(List<SuricataRuleDO> suricataRuleDOList);

  int deleteSuricataRule(List<Integer> sids);

  int deleteSuricataRule(SuricataRuleQueryVO query, String operatorId);

  int deleteSuricataRuleContainsDefault(boolean onlyLocal, Date beforeTime, String operatorId);
}
