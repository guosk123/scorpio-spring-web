package com.machloop.fpc.manager.analysis.service;

import java.util.List;
import java.util.Map;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.base.page.Sort;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月18日, fpc-manager
 */
public interface ScenarioTaskResultService {

  Page<Map<String, Object>> queryScenarioTaskResults(Pageable page, String taskId, String type,
      String query);

  List<Map<String, Object>> queryScenarioTaskTermsResults(Sort sort, String taskId, String type,
      String termField, int termSize);

  String queryScenarioTaskResultIds(String taskResultId);

  void deleteScenarioTaskTermsResults(String taskId, String type);

}
