package com.machloop.fpc.cms.npm.analysis.service;

import java.util.Date;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.machloop.fpc.cms.npm.analysis.bo.SuricataRuleClasstypeBO;

/**
 * @author chenshimiao
 * <p>
 * create at 2022/10/13 10:30 AM,cms
 * @version 1.0
 */
public interface SuricataRuleClasstypeService {
  List<SuricataRuleClasstypeBO> querySuricataRuleClasstypes(Date startTimeDate, Date endTimeDate);

  SuricataRuleClasstypeBO querySuricataRuleClasstype(String id);

  List<String> exportSuricataRuleClasstypes();

  int importClasstypes(MultipartFile file, String operatorId);

  SuricataRuleClasstypeBO saveSuricataRuleClasstype(SuricataRuleClasstypeBO suricataRuleClasstypeBO,
      String operatorId);

  SuricataRuleClasstypeBO updateSuricataRuleClasstype(String id,
      SuricataRuleClasstypeBO suricataRuleClasstypeBO, String operatorId);

  SuricataRuleClasstypeBO deleteSuricataRuleClasstype(String id, String operatorId);
}
