package com.machloop.fpc.manager.analysis.service.impl;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.eclipsesource.v8.V8ScriptExecutionException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.fpc.manager.analysis.bo.ScenarioCustomTemplateBO;
import com.machloop.fpc.manager.analysis.dao.ScenarioCustomDao;
import com.machloop.fpc.manager.analysis.data.ScenarioCustomTemplateDO;
import com.machloop.fpc.manager.analysis.service.ScenarioCustomService;
import com.machloop.fpc.manager.helper.Spl2SqlHelper;

import reactor.util.function.Tuple2;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月24日, fpc-manager
 */
@Service
public class ScenarioCustomServiceImpl implements ScenarioCustomService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioCustomServiceImpl.class);

  @Autowired
  private ScenarioCustomDao customDao;

  @Autowired
  private Spl2SqlHelper dslConvertHelper;

  /**
   * @see com.machloop.fpc.manager.analysis.service.ScenarioCustomService#queryCustomTemplates()
   */
  @Override
  public List<ScenarioCustomTemplateBO> queryCustomTemplates() {
    List<ScenarioCustomTemplateDO> customTemplateDOList = customDao.queryCustomTemplates();
    List<ScenarioCustomTemplateBO> customTemplateBOList = Lists
        .newArrayListWithCapacity(customTemplateDOList.size());

    for (ScenarioCustomTemplateDO customTemplateDO : customTemplateDOList) {
      ScenarioCustomTemplateBO customTemplateBO = new ScenarioCustomTemplateBO();
      BeanUtils.copyProperties(customTemplateDO, customTemplateBO);
      customTemplateBO.setCreateTime(DateUtils.toStringISO8601(customTemplateDO.getCreateTime()));
      customTemplateBO.setUpdateTime(DateUtils.toStringISO8601(customTemplateDO.getUpdateTime()));
      customTemplateBOList.add(customTemplateBO);
    }
    return customTemplateBOList;
  }

  /**
   * @see com.machloop.fpc.manager.analysis.service.ScenarioCustomService#queryCustomTemplate(java.lang.String)
   */
  @Override
  public ScenarioCustomTemplateBO queryCustomTemplate(String id) {
    ScenarioCustomTemplateDO customTemplateDO = customDao.queryCustomTemplate(id);
    ScenarioCustomTemplateBO customTemplateBO = new ScenarioCustomTemplateBO();
    BeanUtils.copyProperties(customTemplateDO, customTemplateBO);
    customTemplateBO.setCreateTime(DateUtils.toStringISO8601(customTemplateDO.getCreateTime()));
    customTemplateBO.setUpdateTime(DateUtils.toStringISO8601(customTemplateDO.getUpdateTime()));
    return customTemplateBO;
  }

  /**
   * @see com.machloop.fpc.manager.analysis.service.ScenarioCustomService#saveCustomTemplate(com.machloop.fpc.manager.analysis.bo.ScenarioCustomTemplateBO, java.lang.String)
   */
  @Override
  public ScenarioCustomTemplateBO saveCustomTemplate(ScenarioCustomTemplateBO customTemplateBO,
      String operatorId) {
    ScenarioCustomTemplateDO existName = customDao
        .queryCustomTemplateByName(customTemplateBO.getName());
    if (StringUtils.isNotBlank(existName.getId())) {
      throw new BusinessException(ErrorCode.COMMON_BASE_OBJECT_DUPLICATE, "模板名称已经存在");
    }

    // 写入数据库
    ScenarioCustomTemplateDO customTemplateDO = new ScenarioCustomTemplateDO();
    BeanUtils.copyProperties(customTemplateBO, customTemplateDO);
    try {
      String filterSpl = customTemplateBO.getFilterSpl();
      Map<String, Object> filterSplMap = JsonHelper.deserialize(filterSpl,
          new TypeReference<Map<String, Object>>() {
          }, false);
      Tuple2<String, Map<String, Object>> dsl = dslConvertHelper
          .converte(MapUtils.getString(filterSplMap, "spl"), true, null, true, true);
      Map<String, Object> map = Maps.newHashMapWithExpectedSize(2);
      map.put("sql", dsl.getT1());
      map.put("params", dsl.getT2());
      customTemplateDO.setFilterDsl(JsonHelper.serialize(map));
    } catch (IOException e) {
      LOGGER.warn("failed to convert dsl.", e);
      throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "解析表达式失败");
    } catch (V8ScriptExecutionException e) {
      throw new BusinessException(ErrorCode.COMMON_BASE_FORMAT_INVALID, "表达式格式错误");
    }
    customTemplateDO.setOperatorId(operatorId);
    customTemplateDO = customDao.saveCustomTemplate(customTemplateDO);

    return queryCustomTemplate(customTemplateDO.getId());
  }

  /**
   * @see com.machloop.fpc.manager.analysis.service.ScenarioCustomService#deleteCustomTemplate(java.lang.String, java.lang.String)
   */
  @Override
  public ScenarioCustomTemplateBO deleteCustomTemplate(String id, String operatorId) {
    ScenarioCustomTemplateBO customTemplateBO = queryCustomTemplate(id);
    if (StringUtils.isNotBlank(customTemplateBO.getId())) {
      customDao.deleteCustomTemplate(id, operatorId);
    }

    return customTemplateBO;
  }

}
