package com.machloop.fpc.manager.analysis.service.impl;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.dict.DictManager;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.analysis.bo.ScenarioTaskBO;
import com.machloop.fpc.manager.analysis.dao.ScenarioTaskDao;
import com.machloop.fpc.manager.analysis.data.ScenarioTaskDO;
import com.machloop.fpc.manager.analysis.service.ScenarioCustomService;
import com.machloop.fpc.manager.analysis.service.ScenarioTaskResultService;
import com.machloop.fpc.manager.analysis.service.ScenarioTaskService;
import com.machloop.fpc.manager.analysis.vo.ScenarioTaskQueryVO;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月11日, fpc-manager
 */
@Service
public class ScenarioTaskServiceImpl implements ScenarioTaskService {

  @Autowired
  private DictManager dictManager;

  @Autowired
  private ScenarioTaskResultService resultService;

  @Autowired
  private ScenarioCustomService customService;

  @Autowired
  private ScenarioTaskDao scenarioTaskDao;

  /**
   * @see com.machloop.fpc.manager.analysis.service.ScenarioTaskService#queryScenarioTasks(com.ntsrs.alpha.common.base.page.PageRequest, com.machloop.fpc.manager.analysis.vo.ScenarioTaskQueryVO)
   */
  @Override
  public Page<ScenarioTaskBO> queryScenarioTasks(Pageable page, ScenarioTaskQueryVO queryVO) {
    Map<String,
        String> typeDict = dictManager.getBaseDict().getItemMap("analysis_scenario_task_type");

    // 查询自定义分析模板
    Map<String,
        String> customTemplatesDict = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    customService.queryCustomTemplates()
        .forEach(item -> customTemplatesDict.put(item.getId(), item.getName()));

    Page<ScenarioTaskDO> scenarioTaskDOPage = scenarioTaskDao.queryScenarioTasks(page, queryVO);
    long totalElem = scenarioTaskDOPage.getTotalElements();

    List<ScenarioTaskBO> scenarioTaskBOList = Lists
        .newArrayListWithCapacity(scenarioTaskDOPage.getSize());
    for (ScenarioTaskDO scenarioTaskDO : scenarioTaskDOPage) {
      ScenarioTaskBO scenarioTaskBO = new ScenarioTaskBO();
      BeanUtils.copyProperties(scenarioTaskDO, scenarioTaskBO);

      // 类型为自定义分析模板, 根据custom_id中的id查询模板名称
      if (StringUtils.startsWith(scenarioTaskDO.getType(),
          ManagerConstants.SCENARIO_CUSTOM_TEMPLATE_PREFIX)) {
        scenarioTaskBO.setTypeText(
            customService.queryCustomTemplate(StringUtils.substringAfter(scenarioTaskDO.getType(),
                ManagerConstants.SCENARIO_CUSTOM_TEMPLATE_PREFIX)).getName());
      } else {
        scenarioTaskBO.setTypeText(MapUtils.getString(typeDict, scenarioTaskDO.getType()));
      }

      scenarioTaskBO
          .setAnalysisStartTime(DateUtils.toStringISO8601(scenarioTaskDO.getAnalysisStartTime()));
      scenarioTaskBO
          .setAnalysisEndTime(DateUtils.toStringISO8601(scenarioTaskDO.getAnalysisEndTime()));
      scenarioTaskBO
          .setExecutionStartTime(DateUtils.toStringISO8601(scenarioTaskDO.getExecutionStartTime()));
      scenarioTaskBO
          .setExecutionEndTime(DateUtils.toStringISO8601(scenarioTaskDO.getExecutionEndTime()));

      scenarioTaskBO.setCreateTime(DateUtils.toStringISO8601(scenarioTaskDO.getCreateTime()));
      scenarioTaskBO.setUpdateTime(DateUtils.toStringISO8601(scenarioTaskDO.getUpdateTime()));

      scenarioTaskBOList.add(scenarioTaskBO);
    }

    return new PageImpl<>(scenarioTaskBOList, page, totalElem);
  }

  /**
   * @see com.machloop.fpc.manager.analysis.service.ScenarioTaskService#queryScenarioTask(java.lang.String)
   */
  @Override
  public ScenarioTaskBO queryScenarioTask(String id) {
    Map<String,
        String> typeDict = dictManager.getBaseDict().getItemMap("analysis_scenario_task_type");

    ScenarioTaskDO scenarioTaskDO = scenarioTaskDao.queryScenarioTask(id);
    ScenarioTaskBO scenarioTaskBO = new ScenarioTaskBO();
    BeanUtils.copyProperties(scenarioTaskDO, scenarioTaskBO);

    // 类型为自定义分析模板, 根据custom_id中的id查询模板名称
    if (StringUtils.startsWith(scenarioTaskDO.getType(),
        ManagerConstants.SCENARIO_CUSTOM_TEMPLATE_PREFIX)) {
      scenarioTaskBO.setTypeText(
          customService.queryCustomTemplate(StringUtils.substringAfter(scenarioTaskDO.getType(),
              ManagerConstants.SCENARIO_CUSTOM_TEMPLATE_PREFIX)).getName());
    } else {
      scenarioTaskBO.setTypeText(MapUtils.getString(typeDict, scenarioTaskDO.getType()));
    }

    scenarioTaskBO
        .setAnalysisStartTime(DateUtils.toStringISO8601(scenarioTaskDO.getAnalysisStartTime()));
    scenarioTaskBO
        .setAnalysisEndTime(DateUtils.toStringISO8601(scenarioTaskDO.getAnalysisEndTime()));
    scenarioTaskBO
        .setExecutionStartTime(DateUtils.toStringISO8601(scenarioTaskDO.getExecutionStartTime()));
    scenarioTaskBO
        .setExecutionEndTime(DateUtils.toStringISO8601(scenarioTaskDO.getExecutionEndTime()));

    scenarioTaskBO.setCreateTime(DateUtils.toStringISO8601(scenarioTaskDO.getCreateTime()));
    scenarioTaskBO.setUpdateTime(DateUtils.toStringISO8601(scenarioTaskDO.getUpdateTime()));
    return scenarioTaskBO;
  }

  /**
   * @see com.machloop.fpc.manager.analysis.service.ScenarioTaskService#saveScenarioTask(com.machloop.fpc.manager.analysis.bo.ScenarioTaskBO, java.lang.String)
   */
  @Override
  public ScenarioTaskBO saveScenarioTask(ScenarioTaskBO scenarioTaskBO, String operatorId) {
    ScenarioTaskDO existName = scenarioTaskDao.queryScenarioTaskByName(scenarioTaskBO.getName());
    if (StringUtils.isNotBlank(existName.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "任务名称已经存在");
    }

    Map<String,
        String> typeDict = dictManager.getBaseDict().getItemMap("analysis_scenario_task_type");

    if (StringUtils.isBlank(MapUtils.getString(typeDict, scenarioTaskBO.getType())) && !StringUtils
        .startsWith(scenarioTaskBO.getType(), ManagerConstants.SCENARIO_CUSTOM_TEMPLATE_PREFIX)) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "分析场景模板不支持");
    }

    // 写入数据库
    ScenarioTaskDO scenarioTaskDO = new ScenarioTaskDO();
    BeanUtils.copyProperties(scenarioTaskBO, scenarioTaskDO);
    scenarioTaskDO.setOperatorId(operatorId);
    scenarioTaskDO
        .setAnalysisStartTime(DateUtils.parseISO8601Date(scenarioTaskBO.getAnalysisStartTime()));
    scenarioTaskDO
        .setAnalysisEndTime(DateUtils.parseISO8601Date(scenarioTaskBO.getAnalysisEndTime()));
    scenarioTaskDO = scenarioTaskDao.saveScenarioTask(scenarioTaskDO);

    return queryScenarioTask(scenarioTaskDO.getId());
  }

  /**
   * @see com.machloop.fpc.manager.analysis.service.ScenarioTaskService#deleteScenarioTask(java.lang.String, java.lang.String)
   */
  @Override
  public ScenarioTaskBO deleteScenarioTask(String id, String operatorId) {
    ScenarioTaskDO exist = scenarioTaskDao.queryScenarioTask(id);
    if (StringUtils.isBlank(exist.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_NOT_FOUND, "对象不存在");
    }

    ScenarioTaskBO scenarioTaskBO = queryScenarioTask(id);
    if (StringUtils.isNotBlank(scenarioTaskBO.getId())) {
      scenarioTaskDao.deleteScenarioTask(id, operatorId);

      // 删除结果
      resultService.deleteScenarioTaskTermsResults(id, scenarioTaskBO.getType());
    }

    return scenarioTaskBO;
  }
}
