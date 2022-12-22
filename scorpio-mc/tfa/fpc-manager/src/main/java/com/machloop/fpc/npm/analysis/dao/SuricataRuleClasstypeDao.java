package com.machloop.fpc.npm.analysis.dao;

import java.util.Date;
import java.util.List;

import com.machloop.fpc.npm.analysis.data.SuricataRuleClasstypeDO;

/**
 * @author guosk
 *
 * create at 2022年4月2日, fpc-manager
 */
public interface SuricataRuleClasstypeDao {

  List<SuricataRuleClasstypeDO> querySuricataRuleClasstypes();

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
