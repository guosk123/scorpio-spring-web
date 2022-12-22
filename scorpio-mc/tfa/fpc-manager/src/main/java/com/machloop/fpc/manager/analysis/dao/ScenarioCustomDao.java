package com.machloop.fpc.manager.analysis.dao;

import java.util.List;

import com.machloop.fpc.manager.analysis.data.ScenarioCustomTemplateDO;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月24日, fpc-manager
 */
public interface ScenarioCustomDao {

  List<ScenarioCustomTemplateDO> queryCustomTemplates();

  ScenarioCustomTemplateDO queryCustomTemplate(String id);

  ScenarioCustomTemplateDO queryCustomTemplateByName(String name);

  ScenarioCustomTemplateDO saveCustomTemplate(ScenarioCustomTemplateDO customTemplateDO);

  int deleteCustomTemplate(String id, String operatorId);

}
