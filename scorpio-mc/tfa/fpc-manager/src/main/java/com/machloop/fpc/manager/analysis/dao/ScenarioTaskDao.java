package com.machloop.fpc.manager.analysis.dao;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.fpc.manager.analysis.data.ScenarioTaskDO;
import com.machloop.fpc.manager.analysis.vo.ScenarioTaskQueryVO;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月11日, fpc-manager
 */
public interface ScenarioTaskDao {

  Page<ScenarioTaskDO> queryScenarioTasks(Pageable page, ScenarioTaskQueryVO queryVO);

  ScenarioTaskDO queryScenarioTask(String id);

  ScenarioTaskDO queryScenarioTaskByName(String name);

  ScenarioTaskDO saveScenarioTask(ScenarioTaskDO scenarioTaskDO);

  int deleteScenarioTask(String id, String operatorId);

}
