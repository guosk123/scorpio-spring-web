package com.machloop.fpc.manager.analysis.service;

import java.util.List;

import com.machloop.fpc.manager.analysis.bo.ScenarioCustomTemplateBO;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月24日, fpc-manager
 */
public interface ScenarioCustomService {

  List<ScenarioCustomTemplateBO> queryCustomTemplates();

  ScenarioCustomTemplateBO queryCustomTemplate(String id);

  ScenarioCustomTemplateBO saveCustomTemplate(ScenarioCustomTemplateBO customTemplateBO,
      String operatorId);

  ScenarioCustomTemplateBO deleteCustomTemplate(String id, String operatorId);
}
