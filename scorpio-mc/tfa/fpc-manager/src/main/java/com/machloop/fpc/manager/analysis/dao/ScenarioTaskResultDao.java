package com.machloop.fpc.manager.analysis.dao;

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
public interface ScenarioTaskResultDao {

  Page<Map<String, Object>> queryScenarioTaskResults(Pageable page, String taskId, String index,
      String query);

  List<Map<String, Object>> queryScenarioTaskTermsResults(Sort sort, String termField, int termSize,
      String taskId, String index);

  Map<String, Object> queryScenarioTaskResult(String taskResultId, String index);

  void deleteScenarioTaskTermsResults(String taskId, String index);

}
