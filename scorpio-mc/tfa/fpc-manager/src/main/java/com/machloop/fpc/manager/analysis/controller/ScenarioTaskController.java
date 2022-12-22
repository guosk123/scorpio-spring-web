package com.machloop.fpc.manager.analysis.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.validation.constraints.NotEmpty;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.annotation.Secured;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.PageRequest;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.webapp.base.LogHelper;
import com.machloop.alpha.webapp.security.LoggedUserContext;
import com.machloop.fpc.manager.analysis.bo.ScenarioCustomTemplateBO;
import com.machloop.fpc.manager.analysis.bo.ScenarioTaskBO;
import com.machloop.fpc.manager.analysis.service.ScenarioCustomService;
import com.machloop.fpc.manager.analysis.service.ScenarioTaskResultService;
import com.machloop.fpc.manager.analysis.service.ScenarioTaskService;
import com.machloop.fpc.manager.analysis.vo.ScenarioCustomTemplateCreationVO;
import com.machloop.fpc.manager.analysis.vo.ScenarioTaskCreationVO;
import com.machloop.fpc.manager.analysis.vo.ScenarioTaskQueryVO;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月11日, fpc-manager
 */
@Validated
@RestController
@RequestMapping("/webapi/fpc-v1/analysis")
public class ScenarioTaskController {

  @Autowired
  private ScenarioTaskService scenarioTaskService;

  @Autowired
  private ScenarioTaskResultService scenarioTaskResultService;

  @Autowired
  private ScenarioCustomService scenarioCustomService;

  @GetMapping("/scenario-tasks")
  @Secured({"PERM_USER"})
  public Page<Map<String, Object>> queryScenarioTasks(
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
      ScenarioTaskQueryVO queryVO) {

    Sort sort = new Sort(Sort.Direction.DESC, "create_time");
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);
    Page<ScenarioTaskBO> scenarioTasksPage = scenarioTaskService.queryScenarioTasks(page, queryVO);

    List<Map<String, Object>> resultList = Lists
        .newArrayListWithCapacity(scenarioTasksPage.getSize());
    for (ScenarioTaskBO scenarioTask : scenarioTasksPage) {
      resultList.add(scenarioTaskBO2Map(scenarioTask, false));
    }

    return new PageImpl<>(resultList, page, scenarioTasksPage.getTotalElements());
  }

  @GetMapping("/scenario-tasks/{id}")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryTask(@PathVariable String id) {
    return scenarioTaskBO2Map(scenarioTaskService.queryScenarioTask(id), true);
  }

  @PostMapping("/scenario-tasks")
  @Secured({"PERM_USER"})
  public void saveScenarioTask(@Validated ScenarioTaskCreationVO scenarioTaskVO) {
    ScenarioTaskBO scenarioTaskBO = new ScenarioTaskBO();
    BeanUtils.copyProperties(scenarioTaskVO, scenarioTaskBO);
    scenarioTaskBO.setAnalysisStartTime(scenarioTaskVO.getAnalysisStartTime());
    scenarioTaskBO.setAnalysisEndTime(scenarioTaskVO.getAnalysisEndTime());

    ScenarioTaskBO scenarioTask = scenarioTaskService.saveScenarioTask(scenarioTaskBO,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, scenarioTask);
  }

  @DeleteMapping("/scenario-tasks/{id}")
  @Secured({"PERM_USER"})
  public void deleteScenarioTask(@PathVariable @NotEmpty(message = "删除分析任务时传入的id不能为空") String id) {
    ScenarioTaskBO scenarioTaskBO = scenarioTaskService.deleteScenarioTask(id,
        LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, scenarioTaskBO);
  }

  /*
   * 
   */
  @GetMapping("/scenario-tasks/{id}/results")
  @Secured({"PERM_USER"})
  public Page<Map<String, Object>> queryScenarioTaskResults(
      @RequestParam(required = false, defaultValue = "record_total_hit") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      @RequestParam(name = "page", required = false, defaultValue = "0") int pageNumber,
      @RequestParam(
          name = "pageSize", required = false,
          defaultValue = Constants.PAGE_DEFAULT_SIZE_STRING) int pageSize,
      @PathVariable String id, @RequestParam String type, String query) {
    Sort sort = new Sort(Sort.Direction.fromString(sortDirection), sortProperty);
    PageRequest page = new PageRequest(pageNumber, pageSize, sort);
    return scenarioTaskResultService.queryScenarioTaskResults(page, id, type, query);
  }

  @GetMapping("/scenario-tasks/{id}/results/as-terms")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryScenarioTaskTermsResults(
      @RequestParam(required = false, defaultValue = "record_total_hit") String sortProperty,
      @RequestParam(required = false, defaultValue = "desc") String sortDirection,
      @PathVariable String id, @RequestParam String termField,
      @RequestParam(required = false, defaultValue = "1000") int termSize,
      @RequestParam String type) {
    Sort sort = new Sort(Sort.Direction.fromString(sortDirection), sortProperty);
    return scenarioTaskResultService.queryScenarioTaskTermsResults(sort, id, type, termField,
        termSize);
  }

  /*
   * 自定义分析模板
   */
  @GetMapping("/scenario-task/custom-templates")
  @Secured({"PERM_USER"})
  public List<Map<String, Object>> queryScenarioCustomTemplates(
      @RequestParam(defaultValue = "true") boolean isDetail) {
    return scenarioCustomService.queryCustomTemplates().stream()
        .map(item -> scenarioCustomTemplateBO2Map(item, isDetail)).collect(Collectors.toList());
  }

  @GetMapping("/scenario-task/custom-templates/{id}")
  @Secured({"PERM_USER"})
  public Map<String, Object> queryScenarioCustomTemplate(@PathVariable String id) {
    return scenarioCustomTemplateBO2Map(scenarioCustomService.queryCustomTemplate(id), true);
  }

  @PostMapping("/scenario-task/custom-templates")
  @Secured({"PERM_USER"})
  public void saveScenarioCustomTemplate(@Validated ScenarioCustomTemplateCreationVO creationVO) {
    ScenarioCustomTemplateBO customTemplateBO = new ScenarioCustomTemplateBO();
    BeanUtils.copyProperties(creationVO, customTemplateBO);

    ScenarioCustomTemplateBO customTemplate = scenarioCustomService
        .saveCustomTemplate(customTemplateBO, LoggedUserContext.getCurrentUser().getId());

    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_SAVE, customTemplate);
  }

  @DeleteMapping("/scenario-task/custom-templates/{id}")
  @Secured({"PERM_USER"})
  public void deleteScenarioCustomTemplate(@PathVariable String id) {
    ScenarioCustomTemplateBO customTemplate = scenarioCustomService.deleteCustomTemplate(id,
        LoggedUserContext.getCurrentUser().getId());
    LogHelper.auditOperate(LogHelper.AUDIT_LOG_ACTION_DELETE, customTemplate);
  }

  private Map<String, Object> scenarioTaskBO2Map(ScenarioTaskBO scenarioTask, boolean isDetail) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", scenarioTask.getId());
    map.put("name", scenarioTask.getName());
    map.put("analysisStartTime", scenarioTask.getAnalysisStartTime());
    map.put("analysisEndTime", scenarioTask.getAnalysisEndTime());
    map.put("type", scenarioTask.getType());
    map.put("typeText", scenarioTask.getTypeText());
    map.put("description", scenarioTask.getDescription());
    map.put("executionStartTime", scenarioTask.getExecutionStartTime());
    map.put("executionEndTime", scenarioTask.getExecutionEndTime());
    map.put("executionProgress", scenarioTask.getExecutionProgress());
    map.put("executionTrace", scenarioTask.getExecutionTrace());
    map.put("state", scenarioTask.getState());

    if (isDetail) {
      map.put("createTime", scenarioTask.getCreateTime());
      map.put("updateTime", scenarioTask.getUpdateTime());
    }

    return map;
  }

  private Map<String, Object> scenarioCustomTemplateBO2Map(ScenarioCustomTemplateBO customTemplate,
      boolean isDetail) {
    Map<String, Object> map = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    map.put("id", customTemplate.getId());
    map.put("name", customTemplate.getName());

    if (isDetail) {
      map.put("filterSpl", customTemplate.getFilterSpl());
      map.put("dataSource", customTemplate.getDataSource());
      map.put("function", customTemplate.getFunction());
      map.put("groupBy", customTemplate.getGroupBy());
      map.put("sliceTimeInterval", customTemplate.getSliceTimeInterval());
      map.put("avgTimeInterval", customTemplate.getAvgTimeInterval());
      map.put("description", customTemplate.getDescription());
      map.put("createTime", customTemplate.getCreateTime());
      map.put("updateTime", customTemplate.getUpdateTime());
    }

    return map;
  }
}
