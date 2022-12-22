package com.machloop.fpc.npm.analysis.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.npm.analysis.bo.AbnormalEventRuleBO;
import com.machloop.fpc.npm.analysis.vo.AbnormalEventRuleQueryVO;

/**
 * @author guosk
 *
 * create at 2021年7月16日, fpc-manager
 */
public interface AbnormalEventRuleService {

  Page<AbnormalEventRuleBO> queryAbnormalEventRules(Pageable page,
      AbnormalEventRuleQueryVO queryVO);

  List<AbnormalEventRuleBO> queryAbnormalEventRules();

  AbnormalEventRuleBO queryAbnormalEventRule(String id);

  AbnormalEventRuleBO saveAbnormalEventRule(AbnormalEventRuleBO abnormalEventRuleBO,
      String operatorId);

  int importAbnormalEventRules(MultipartFile file, String operatorId);

  List<String> exportAbnormalEventRules(String source);

  AbnormalEventRuleBO updateAbnormalEventRule(String id, AbnormalEventRuleBO abnormalEventRuleBO,
      String operatorId);

  AbnormalEventRuleBO updateStatus(String id, String status, String operatorId);

  AbnormalEventRuleBO deleteAbnormalEventRule(String id);

}
