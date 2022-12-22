package com.machloop.fpc.manager.appliance.service;

import org.springframework.web.multipart.MultipartFile;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.manager.appliance.bo.PktAnalysisBO;

/**
 * @author "Minjiajun"
 *
 * create at 2022年4月18日, fpc-manager
 */
public interface PktAnalysisService {

  Page<PktAnalysisBO> queryPktAnalysises(Pageable page);

  PktAnalysisBO queryPktAnalysis(String id);
  
  String getPktAnalysisFile(String id);
  
  int savePktAnalysis(MultipartFile file, String fileName, String protocol, String description,
      String operatorId);
  
  PktAnalysisBO deletePktAnalysis(String id, String operatorId);
}
