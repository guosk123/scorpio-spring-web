package com.machloop.fpc.manager.analysis.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.manager.analysis.bo.ThreatIntelligenceBO;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月16日, fpc-manager
 */
public interface ThreatIntelligenceService {

  Page<ThreatIntelligenceBO> queryIntelligences(Pageable page, String type, String content);

  ThreatIntelligenceBO queryIntelligence(String id);

  int importIntelligences(MultipartFile file, boolean custom);

  List<String> exportIntelligences(String type, String content);

  ThreatIntelligenceBO updateIntelligence(String id, ThreatIntelligenceBO intelligenceBO);

  ThreatIntelligenceBO deleteIntelligence(String id);
}
