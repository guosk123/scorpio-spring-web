package com.machloop.fpc.npm.analysis.service;

import java.util.Date;
import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.machloop.fpc.npm.analysis.bo.SuricataRuleClasstypeBO;

/**
 * @author guosk
 *
 * create at 2022年4月6日, fpc-manager
 */
public interface SuricataRuleClasstypeService {

  List<SuricataRuleClasstypeBO> querySuricataRuleClasstypes(Date startTime, Date endTime);

  List<String> exportSuricataRuleClasstypes();

  SuricataRuleClasstypeBO querySuricataRuleClasstype(String id);

  int importClasstypes(MultipartFile file, String operatorId);

  SuricataRuleClasstypeBO saveSuricataRuleClasstype(SuricataRuleClasstypeBO suricataRuleClasstypeBO,
      String operatorId);

  SuricataRuleClasstypeBO updateSuricataRuleClasstype(String id,
      SuricataRuleClasstypeBO suricataRuleClasstypeBO, String operatorId);

  SuricataRuleClasstypeBO deleteSuricataRuleClasstype(String id, String operatorId);

}
