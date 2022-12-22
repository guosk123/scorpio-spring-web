package com.machloop.fpc.npm.analysis.service;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

import org.springframework.web.multipart.MultipartFile;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.npm.analysis.bo.SuricataRuleBO;
import com.machloop.fpc.npm.analysis.vo.SuricataRuleQueryVO;

/**
 * @author guosk
 *
 * create at 2022年4月6日, fpc-manager
 */
public interface SuricataRuleService {

  Page<SuricataRuleBO> querySuricataRules(Pageable page, SuricataRuleQueryVO queryVO);

  Map<String, String> queryRuleSource();

  void exportSuricataRules(SuricataRuleQueryVO queryVO, List<String> sids, OutputStream out)
      throws IOException;

  SuricataRuleBO querySuricataRule(int sid);

  SuricataRuleBO saveSuricataRule(SuricataRuleBO suricataRuleBO, String operatorId);

  int importSuricataRules(MultipartFile file, String classtypeId, String source, String operatorId);

  int importIssuedSuricataRules(MultipartFile file, String classtypeId, String source, String id);

  void importSuricataKnowledges(MultipartFile file);

  SuricataRuleBO updateSuricataRule(int sid, SuricataRuleBO suricataRuleBO, String operatorId);

  int updateState(List<String> sids, String state, String operatorId);

  int batchUpdateSuricataRule(SuricataRuleQueryVO suricataRuleCreationVO, List<Integer> sids,
      String state, String source, String classtypeIds, String mitreTacticIds,
      String mitreTechniqueIds, String operatorId);

  int updateSuricataState(List<String> sids, SuricataRuleQueryVO query, String state,
      String operatorId);

  int deleteSuricataRule(List<String> sids, SuricataRuleQueryVO suricataRuleQueryVO,
      String operatorId);

}
