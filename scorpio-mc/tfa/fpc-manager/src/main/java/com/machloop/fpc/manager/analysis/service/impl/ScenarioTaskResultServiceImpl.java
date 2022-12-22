package com.machloop.fpc.manager.analysis.service.impl;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.analysis.dao.ScenarioTaskResultDao;
import com.machloop.fpc.manager.analysis.service.ScenarioTaskResultService;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月18日, fpc-manager
 */
@Service
public class ScenarioTaskResultServiceImpl implements ScenarioTaskResultService {

  @Value("${analysis.result.query.max.count}")
  private int QUERY_RECORD_MAX;

  @Autowired
  private ScenarioTaskResultDao resultDao;

  /**
   * @see com.machloop.fpc.manager.analysis.service.ScenarioTaskResultService#queryScenarioTaskResults(com.machloop.alpha.common.base.page.Pageable, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Page<Map<String, Object>> queryScenarioTaskResults(Pageable page, String taskId,
      String type, String query) {
    Page<Map<String, Object>> taskResults = resultDao.queryScenarioTaskResults(page, taskId,
        getIndex(type), query);

    taskResults.forEach(result -> {
      if (result.containsKey("start_time")) {
        result.put("start_time", DateUtils.toStringNanoISO8601(
            (OffsetDateTime) result.get("start_time"), ZoneId.systemDefault()));
      }
      if (result.containsKey("end_time")) {
        result.put("end_time", DateUtils
            .toStringNanoISO8601((OffsetDateTime) result.get("end_time"), ZoneId.systemDefault()));
      }
      if (result.containsKey("record_start_time")) {
        result.put("record_start_time", DateUtils.toStringNanoISO8601(
            (OffsetDateTime) result.get("record_start_time"), ZoneId.systemDefault()));
      }
      if (result.containsKey("record_end_time")) {
        result.put("record_end_time", DateUtils.toStringNanoISO8601(
            (OffsetDateTime) result.get("record_end_time"), ZoneId.systemDefault()));
      }
    });

    return taskResults;
  }

  /**
   * @see com.machloop.fpc.manager.analysis.service.ScenarioTaskResultService#queryScenarioTaskTermsResults(com.machloop.alpha.common.base.page.Sort, java.lang.String, java.lang.String, java.lang.String, int)
   */
  @Override
  public List<Map<String, Object>> queryScenarioTaskTermsResults(Sort sort, String taskId,
      String type, String termField, int termSize) {
    return resultDao.queryScenarioTaskTermsResults(sort, termField, termSize, taskId,
        getIndex(type));
  }

  /**
   * @see com.machloop.fpc.manager.analysis.service.ScenarioTaskResultService#queryScenarioTaskResultRecordIdList(java.lang.String, java.lang.String)
   */
  @Override
  public String queryScenarioTaskResultIds(String taskResultId) {
    String id = StringUtils.substringAfter(taskResultId,
        ManagerConstants.METADATA_CONDITION_ID_ANALYSIS_RESULT_PREFIX);

    String recordIds = MapUtils.getString(resultDao.queryScenarioTaskResult(id, getIndex("")),
        "record_id_list", "");
    // 只返回最大查询数量的id
    int i = StringUtils.ordinalIndexOf(recordIds, ",", QUERY_RECORD_MAX);
    if (i > 0) {
      recordIds = StringUtils.substring(recordIds, 0, i);
    }
    return recordIds;
  }

  /**
   * @see com.machloop.fpc.manager.analysis.service.ScenarioTaskResultService#deleteScenarioTaskTermsResults(java.lang.String, java.lang.String)
   */
  @Override
  public void deleteScenarioTaskTermsResults(String taskId, String type) {
    resultDao.deleteScenarioTaskTermsResults(taskId, getIndex(type));
  }

  private String getIndex(String type) {

    if (StringUtils.startsWith(type, ManagerConstants.SCENARIO_CUSTOM_TEMPLATE_PREFIX)) {
      return ManagerConstants.TABLE_ANALYSIS_CUSTOM_TEMPLATE;
    }

    String index = "";
    switch (type) {
      case "beacon-detection":
        index = ManagerConstants.TABLE_ANALYSIS_BEACON_DETECTION;
        break;
      case "dynamic-domain":
        index = ManagerConstants.TABLE_ANALYSIS_DYNAMIC_DOMAIN;
        break;
      case "intelligence-ip":
        index = ManagerConstants.TABLE_ANALYSIS_INTELLIGENCE_IP;
        break;
      case "nonstandard-protocol":
        index = ManagerConstants.TABLE_ANALYSIS_NONSTANDARD_PROTOCOL;
        break;
      case "suspicious-https":
        index = ManagerConstants.TABLE_ANALYSIS_SUSPICIOUS_HTTPS;
        break;
      case "brute-force-ssh":
      case "brute-force-rdp":
        index = ManagerConstants.TABLE_ANALYSIS_BRUTE_FORCE;
        break;
      default:
        break;
    }
    return index;
  }
}
