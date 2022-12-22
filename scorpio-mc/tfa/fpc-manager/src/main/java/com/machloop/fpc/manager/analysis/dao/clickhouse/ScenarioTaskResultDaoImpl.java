package com.machloop.fpc.manager.analysis.dao.clickhouse;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.stereotype.Repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.base.page.Sort;
import com.machloop.alpha.common.exception.BusinessException;
import com.machloop.alpha.common.exception.ErrorCode;
import com.machloop.alpha.common.util.JsonHelper;
import com.machloop.alpha.common.util.PageUtils;
import com.machloop.fpc.manager.ManagerConstants;
import com.machloop.fpc.manager.analysis.dao.ScenarioTaskResultDao;
import com.machloop.fpc.manager.boot.configuration.ClickHouseJdbcTemplate;

/**
 * @author guosk
 *
 * create at 2021年8月28日, fpc-manager
 */
@Repository
public class ScenarioTaskResultDaoImpl implements ScenarioTaskResultDao {

  private static final Logger LOGGER = LoggerFactory.getLogger(ScenarioTaskResultDaoImpl.class);

  private static final List<String> SORT_FIELDS = Lists.newArrayListWithCapacity(0);

  private static final List<String> ANALYSIS_ALL_TABLE = Lists.newArrayList(
      ManagerConstants.TABLE_ANALYSIS_BEACON_DETECTION,
      ManagerConstants.TABLE_ANALYSIS_DYNAMIC_DOMAIN,
      ManagerConstants.TABLE_ANALYSIS_INTELLIGENCE_IP,
      ManagerConstants.TABLE_ANALYSIS_NONSTANDARD_PROTOCOL,
      ManagerConstants.TABLE_ANALYSIS_SUSPICIOUS_HTTPS, ManagerConstants.TABLE_ANALYSIS_BRUTE_FORCE,
      ManagerConstants.TABLE_ANALYSIS_CUSTOM_TEMPLATE);

  @Autowired
  private ClickHouseJdbcTemplate clickHouseTemplate;

  /**
   * @see com.machloop.fpc.manager.analysis.dao.ScenarioTaskResultDao#queryScenarioTaskResults(com.machloop.alpha.common.base.page.Pageable, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Page<Map<String, Object>> queryScenarioTaskResults(Pageable page, String taskId,
      String tableName, String query) {
    StringBuilder sql = buildSelectStatement(tableName, false);

    // 过滤
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where task_id = :task_id ");
    params.put("task_id", taskId);

    if (StringUtils.isNotBlank(query)) {
      Map<String, Object> queryMap = JsonHelper.deserialize(query,
          new TypeReference<Map<String, Object>>() {
          });
      queryMap.forEach((field, value) -> {
        whereSql.append(String.format(" and %s = :%s ", field, field));
        params.put(field, value);
      });
    }
    sql.append(whereSql);

    PageUtils.appendPage(sql, page, SORT_FIELDS);
    List<Map<String, Object>> resultList = clickHouseTemplate.getJdbcTemplate()
        .query(sql.toString(), params, new ColumnMapRowMapper());

    // total
    StringBuilder totalSql = new StringBuilder();
    totalSql.append("select count(id) from ");
    totalSql.append(tableName);
    totalSql.append(whereSql);
    Integer total = clickHouseTemplate.getJdbcTemplate().queryForObject(totalSql.toString(), params,
        Integer.class);

    return new PageImpl<Map<String, Object>>(resultList, page, total);
  }

  /**
   * @see com.machloop.fpc.manager.analysis.dao.ScenarioTaskResultDao#queryScenarioTaskTermsResults(com.machloop.alpha.common.base.page.Sort, java.lang.String, int, java.lang.String, java.lang.String)
   */
  @Override
  public List<Map<String, Object>> queryScenarioTaskTermsResults(Sort sort, String termField,
      int termSize, String taskId, String tableName) {
    List<Map<String, Object>> result = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);

    StringBuilder aggsSql = new StringBuilder();

    aggsSql.append(" select ").append(termField).append(",");
    aggsSql.append(" count(id) as count, sum(record_total_hit) as record_total_hit ");
    aggsSql.append(" from ").append(tableName);

    // 过滤
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    aggsSql.append(" where task_id = :task_id ");
    params.put("task_id", taskId);

    // 聚合
    aggsSql.append(" group by ").append(termField);

    // 排序
    aggsSql.append(" order by count desc ");
    if (sort.getOrderFor("record_total_hit") != null) {
      aggsSql.append(", record_total_hit ")
          .append(sort.getOrderFor("record_total_hit").getDirection().toString());
    } else if (sort.getOrderFor("count") != null) {
      aggsSql.append(", count").append(sort.getOrderFor("count").getDirection().toString());
    }

    result = clickHouseTemplate.getJdbcTemplate().query(aggsSql.toString(), params,
        new ColumnMapRowMapper());
    return result;
  }

  /**
   * @see com.machloop.fpc.manager.analysis.dao.ScenarioTaskResultDao#queryScenarioTaskResult(java.lang.String, java.lang.String)
   */
  @Override
  public Map<String, Object> queryScenarioTaskResult(String taskResultId, String tableName) {
    Map<String, Object> result = Maps.newHashMapWithExpectedSize(1);

    List<String> queryTables = Lists.newArrayListWithCapacity(Constants.COL_DEFAULT_SIZE);
    if (StringUtils.isNotBlank(tableName)) {
      queryTables.add(tableName);
    } else {
      queryTables.addAll(ANALYSIS_ALL_TABLE);
    }

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(1);
    params.put("id", taskResultId);

    for (String item : queryTables) {
      StringBuilder sql = new StringBuilder();
      sql.append("select * from ").append(item);
      sql.append(" where id = :id ");

      List<Map<String, Object>> tempResult = clickHouseTemplate.getJdbcTemplate()
          .query(sql.toString(), params, new ColumnMapRowMapper());
      if (tempResult.size() > 0) {
        result = tempResult.get(0);
        break;
      }
    }

    return result;
  }

  /**
   * @see com.machloop.fpc.manager.analysis.dao.ScenarioTaskResultDao#deleteScenarioTaskTermsResults(java.lang.String)
   */
  @Override
  public void deleteScenarioTaskTermsResults(String taskId, String tableName) {
    StringBuilder sql = new StringBuilder();
    sql.append(" alter table ").append(tableName);
    sql.append(" delete where task_id = :taskId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(1);
    params.put("taskId", taskId);

    int update = clickHouseTemplate.getJdbcTemplate().update(sql.toString(), params);
    if (update == 0) {
      LOGGER.warn("fail to delete scenario task results, task id: [{}], tableName: [{}].",
          StringEscapeUtils.unescapeEcmaScript(taskId), tableName);
    }
  }

  private static StringBuilder buildSelectStatement(String tableName, boolean includeRecordIdList) {
    StringBuilder sql = new StringBuilder("select id, task_id, record_total_hit, ");
    if (includeRecordIdList) {
      sql.append(" record_id_list, ");
    }

    switch (tableName) {
      case ManagerConstants.TABLE_ANALYSIS_BEACON_DETECTION:
        sql.append(" src_ip, dest_ip, dest_port, upstream_bytes, period, protocol ");
        break;
      case ManagerConstants.TABLE_ANALYSIS_DYNAMIC_DOMAIN:
        sql.append(" inner_host, dynamic_domain ");
        break;
      case ManagerConstants.TABLE_ANALYSIS_INTELLIGENCE_IP:
        sql.append(" ip_address ");
        break;
      case ManagerConstants.TABLE_ANALYSIS_NONSTANDARD_PROTOCOL:
        sql.append(" standard_l7_protocol_id, standard_ip_protocol, standard_port ");
        break;
      case ManagerConstants.TABLE_ANALYSIS_SUSPICIOUS_HTTPS:
        sql.append(" ja3 ");
        break;
      case ManagerConstants.TABLE_ANALYSIS_BRUTE_FORCE:
        sql.append(
            " start_time, end_time, inner_host, record_max_hit_every_1minutes, record_max_hit_every_3minutes ");
        break;
      case ManagerConstants.TABLE_ANALYSIS_CUSTOM_TEMPLATE:
        sql.append(
            " group_by, record_start_time, record_end_time, function_result, time_avg_hit, time_slice_list ");
        break;
      default:
        throw new BusinessException(ErrorCode.COMMON_BASE_EXCEPTION, "查询异常");
    }

    sql.append(" from ").append(tableName);
    return sql;
  }

}
