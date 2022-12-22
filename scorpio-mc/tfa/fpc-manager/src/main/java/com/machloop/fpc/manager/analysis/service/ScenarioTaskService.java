package com.machloop.fpc.manager.analysis.service;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.manager.analysis.bo.ScenarioTaskBO;
import com.machloop.fpc.manager.analysis.vo.ScenarioTaskQueryVO;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月11日, fpc-manager
 */
public interface ScenarioTaskService {

  Page<ScenarioTaskBO> queryScenarioTasks(Pageable page, ScenarioTaskQueryVO queryVO);

  ScenarioTaskBO queryScenarioTask(String id);

  ScenarioTaskBO saveScenarioTask(ScenarioTaskBO scenarioTaskBO, String operatorId);

  ScenarioTaskBO deleteScenarioTask(String id, String operatorId);

}
