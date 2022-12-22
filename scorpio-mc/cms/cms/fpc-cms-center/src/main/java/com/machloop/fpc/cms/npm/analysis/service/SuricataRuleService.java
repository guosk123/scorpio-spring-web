package com.machloop.fpc.cms.npm.analysis.service;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletOutputStream;

import org.springframework.web.multipart.MultipartFile;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.fpc.cms.npm.analysis.bo.SuricataRuleBO;
import com.machloop.fpc.cms.npm.analysis.vo.SuricataRuleQueryVO;

/**
 * @author chenshimiao
 * <p>
 * create at 2022/10/10 2:50 PM,cms
 * @version 1.0
 */
public interface SuricataRuleService {

  Page<SuricataRuleBO> querySuricataRules(PageRequest page, SuricataRuleQueryVO queryVO);

  Map<String, String> queryRuleSource();

  SuricataRuleBO querySuricataRule(int sid);

  void exportSuricataRules(SuricataRuleQueryVO queryVO, List<String> convertCSVToList,
      ServletOutputStream out) throws IOException;

  int importSuricataRules(MultipartFile file, String classtypeId, String source, String operatorId);

  int importIssuedSuricataRules(MultipartFile file, String classtypeId, String source, String id, boolean isEngine);

  SuricataRuleBO saveSuricataRule(SuricataRuleBO suricataRuleBO, String operatorId);

  int batchUpdateSuricataRule(SuricataRuleQueryVO suricataRuleCreationVO, List<Integer> sids,
      String state, String source, String classtypeIds, String mitreTacticIds,
      String mitreTechniqueIds, String operatorId);

  int updateState(List<String> sids, String state, String operatorId);

  SuricataRuleBO updateSuricataRule(int parseInt, SuricataRuleBO suricataRuleBO, String id);

  int deleteSuricataRule(List<String> sids, SuricataRuleQueryVO suricataRuleQueryVO,
      String operatorId);
}
