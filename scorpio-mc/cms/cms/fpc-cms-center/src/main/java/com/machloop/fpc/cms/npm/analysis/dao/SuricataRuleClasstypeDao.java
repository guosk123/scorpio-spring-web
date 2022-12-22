package com.machloop.fpc.cms.npm.analysis.dao;

import java.util.Date;
import java.util.List;

import com.machloop.fpc.cms.npm.analysis.data.SuricataRuleClasstypeDO;

/**
 * @author ChenXiao
 * create at 2022/9/19
 */
public interface SuricataRuleClasstypeDao {

  List<SuricataRuleClasstypeDO> querySuricataRuleClasstypes();

  List<String> querySuricataRuleClasstypeIds(Boolean onlyLocal);

  List<SuricataRuleClasstypeDO> querySuricataRuleClasstypes(Date beforeTime);

  SuricataRuleClasstypeDO querySuricataRuleClasstype(String id);

  SuricataRuleClasstypeDO querySuricataRuleClasstypeByName(String name);

  SuricataRuleClasstypeDO saveSuricataRuleClasstype(
      SuricataRuleClasstypeDO suricataRuleClasstypeDO);

  int saveSuricataRuleClasstypes(List<SuricataRuleClasstypeDO> suricataRuleClasstypes);

  int updateSuricataRuleClasstype(SuricataRuleClasstypeDO suricataRuleClasstypeDO);

  int deleteSuricataRuleClasstype(String id, String operatorId);

  int deleteSuricataRuleClasstype(boolean onlyLocal);
}
