package com.machloop.fpc.npm.analysis.dao;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.npm.analysis.data.SuricataRuleDO;
import com.machloop.fpc.npm.analysis.vo.SuricataRuleQueryVO;

import reactor.util.function.Tuple4;

/**
 * @author guosk
 *
 * create at 2022年4月2日, fpc-manager
 */
public interface SuricataRuleDao {

  Page<SuricataRuleDO> querySuricataRules(Pageable page, SuricataRuleQueryVO queryVO);

  List<SuricataRuleDO> querySuricataRules(SuricataRuleQueryVO queryVO);

  List<Integer> querySuricataRuleSids(SuricataRuleQueryVO queryVO);

  List<Integer> querySuricataRuleIds(SuricataRuleQueryVO suricataRuleQueryVO);

  List<SuricataRuleDO> querySuricataRulesBySids(List<Integer> sids);

  Map<Integer, Tuple4<Integer, String, Date, String>> querySuricataRuleTuple4();

  List<String> queryRuleSource();

  Map<String, Integer> statisticsByClasstype();

  Map<String, Integer> statisticsByMitreTactic();

  Map<String, Integer> statisticsByMitreTechnique();

  SuricataRuleDO querySuricataRule(int sid);

  List<Integer> querySuricataRule(Date beforeTime);

  List<String> querySuricataRule(List<Integer> sids);

  List<SuricataRuleDO> querySuricataRulesByIds(List<String> splitIds);

  SuricataRuleDO saveSuricataRule(SuricataRuleDO suricataRuleDO);

  int saveSuricataRules(List<SuricataRuleDO> suricataRuleList);

  int updateSuricataRule(SuricataRuleDO suricataRuleDO);

  int updateState(List<Integer> sids, String state, String operatorId);

  int updateState(SuricataRuleQueryVO queryVO, String state, String operatorId);

  void updateBatchSuricataRule(List<SuricataRuleDO> suricataRuleDOS);

  int deleteSuricataRule(List<Integer> sids, String operatorId);

  int deleteSuricataRule(SuricataRuleQueryVO query, String operatorId);

  int deleteSuricataRuleContainsDefault(boolean onlyLocal, Date beforeTime, String operatorId);

  int deleteSuricataRuleByIdsTrue(List<Integer> sids);
}
