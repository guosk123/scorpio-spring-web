package com.machloop.fpc.manager.analysis.dao;

import java.util.Collection;
import java.util.List;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.manager.analysis.data.ThreatIntelligenceDO;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月16日, fpc-manager
 */
public interface ThreatIntelligenceDao {

  Page<ThreatIntelligenceDO> queryIntelligences(Pageable page, String type, String content,
      Collection<String> threatCategory);

  List<ThreatIntelligenceDO> queryIntelligences(String type, String content,
      Collection<String> threatCategory);

  ThreatIntelligenceDO queryIntelligence(String id);

  int saveIntelligences(List<ThreatIntelligenceDO> intelligences);

  int updateIntelligence(ThreatIntelligenceDO intelligenceDO);

  int deleteIntelligences(Collection<String> threatCategory);

  int deleteIntelligence(String id);

}
