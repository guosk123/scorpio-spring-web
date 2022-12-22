package com.machloop.fpc.manager.analysis.dao.postgres;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.BaseOperateDO;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.common.util.PageUtils;
import com.machloop.fpc.common.FpcConstants;
import com.machloop.fpc.manager.analysis.dao.ScenarioTaskDao;
import com.machloop.fpc.manager.analysis.data.ScenarioTaskDO;
import com.machloop.fpc.manager.analysis.vo.ScenarioTaskQueryVO;

/**
 * @author mazhiyuan
 *
 * create at 2020年6月11日, fpc-manager
 */
@Repository
public class ScenarioTaskDaoImpl implements ScenarioTaskDao {

  private static final String TABLE_ANALYSIS_SCENARIO_TASK = "fpc_analysis_scenario_task";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.manager.analysis.dao.ScenarioTaskDao#queryScenarioTasks(com.ntsrs.alpha.common.base.page.PageRequest, com.machloop.fpc.manager.analysis.vo.ScenarioTaskQueryVO)
   */
  @Override
  public Page<ScenarioTaskDO> queryScenarioTasks(Pageable page, ScenarioTaskQueryVO queryVO) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    if (StringUtils.isNotBlank(queryVO.getState())) {
      whereSql.append(" and state = :state ");
    }
    if (StringUtils.isNotBlank(queryVO.getType())) {
      whereSql.append(" and type = :type ");
    }

    sql.append(whereSql);

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("state", queryVO.getState());
    params.put("type", queryVO.getType());

    PageUtils.appendPage(sql, page, ScenarioTaskDO.class);

    List<ScenarioTaskDO> scenarioTaskList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ScenarioTaskDO.class));

    StringBuilder totalSql = new StringBuilder();
    totalSql.append("select count(id) from ");
    totalSql.append(TABLE_ANALYSIS_SCENARIO_TASK);
    totalSql.append(whereSql);

    Integer total = jdbcTemplate.queryForObject(totalSql.toString(), params, Integer.class);
    return new PageImpl<>(scenarioTaskList, page, total);
  }

  /**
   * @see com.machloop.fpc.manager.analysis.dao.ScenarioTaskDao#queryScenarioTask(java.lang.String)
   */
  @Override
  public ScenarioTaskDO queryScenarioTask(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("id", id);

    List<ScenarioTaskDO> scenarioTaskList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ScenarioTaskDO.class));
    return CollectionUtils.isEmpty(scenarioTaskList) ? new ScenarioTaskDO()
        : scenarioTaskList.get(0);
  }

  /**
   * @see com.machloop.fpc.manager.analysis.dao.ScenarioTaskDao#queryScenarioTaskByName(java.lang.String)
   */
  @Override
  public ScenarioTaskDO queryScenarioTaskByName(String name) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and name = :name ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("name", name);

    List<ScenarioTaskDO> scenarioTaskList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ScenarioTaskDO.class));
    return CollectionUtils.isEmpty(scenarioTaskList) ? new ScenarioTaskDO()
        : scenarioTaskList.get(0);
  }

  /**
   * @see com.machloop.fpc.manager.analysis.dao.ScenarioTaskDao#saveScenarioTask(com.machloop.fpc.manager.analysis.data.ScenarioTaskDO)
   */
  @Override
  public ScenarioTaskDO saveScenarioTask(ScenarioTaskDO scenarioTaskDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_ANALYSIS_SCENARIO_TASK);
    sql.append(" (id, name, analysis_start_time, analysis_end_time, ");
    sql.append(" type, description, execution_start_time, execution_end_time, ");
    sql.append(" execution_progress, execution_trace, ");
    sql.append(" deleted, create_time, update_time, operator_id ) ");
    sql.append(" values ");
    sql.append(" (:id, :name, :analysisStartTime, :analysisEndTime, ");
    sql.append(" :type, :description, :executionStartTime, :executionEndTime, ");
    sql.append(" :executionProgress, :executionTrace, ");
    sql.append(" :deleted, :createTime, :updateTime, :operatorId ) ");

    scenarioTaskDO.setId(StringUtils.replace(IdGenerator.generateUUID(), "-", "_"));
    scenarioTaskDO.setState(FpcConstants.ANALYSIS_TASK_STATE_RUN);
    scenarioTaskDO.setCreateTime(DateUtils.now());
    scenarioTaskDO.setUpdateTime(scenarioTaskDO.getCreateTime());
    scenarioTaskDO.setDeleted(Constants.BOOL_NO);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(scenarioTaskDO);
    jdbcTemplate.update(sql.toString(), paramSource);
    return scenarioTaskDO;
  }

  /**
   * @see com.machloop.fpc.manager.analysis.dao.ScenarioTaskDao#deleteScenarioTask(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteScenarioTask(String id, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_ANALYSIS_SCENARIO_TASK);
    sql.append(" set deleted = :deleted, delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    ScenarioTaskDO scenarioTaskDO = new ScenarioTaskDO();
    scenarioTaskDO.setDeleted(Constants.BOOL_YES);
    scenarioTaskDO.setDeleteTime(DateUtils.now());
    scenarioTaskDO.setOperatorId(operatorId);
    scenarioTaskDO.setId(id);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(scenarioTaskDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, name, analysis_start_time, analysis_end_time, type, description, ");
    sql.append(" execution_start_time, execution_end_time, execution_progress, execution_trace, ");
    sql.append(" state, deleted, create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_ANALYSIS_SCENARIO_TASK);
    return sql;
  }
}
