package com.machloop.fpc.manager.appliance.dao;

import java.util.List;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.manager.appliance.data.PktAnalysisDO;

/**
 * @author "Minjiajun"
 *
 * create at 2022年4月18日, fpc-manager
 */
public interface PktAnalysisDao {

  Page<PktAnalysisDO> queryPktAnalysises(Pageable page);

  List<PktAnalysisDO> queryPktAnalysises();

  PktAnalysisDO queryPktAnalysis(String id);

  int savePktAnalysis(PktAnalysisDO pktAnalysisDO);

  int deletePktAnalysis(String id, String operatorId);
}
