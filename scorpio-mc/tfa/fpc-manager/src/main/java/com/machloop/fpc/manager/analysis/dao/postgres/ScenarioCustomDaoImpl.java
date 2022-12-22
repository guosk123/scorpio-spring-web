package com.machloop.fpc.manager.analysis.dao.postgres;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.BaseOperateDO;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.manager.analysis.dao.ScenarioCustomDao;
import com.machloop.fpc.manager.analysis.data.ScenarioCustomTemplateDO;

/**
 * @author mazhiyuan
 *
 * create at 2020年8月24日, fpc-manager
 */
@Repository
public class ScenarioCustomDaoImpl implements ScenarioCustomDao {

  private static final String TABLE_ANALYSIS_SCENARIO_TEMPLATE = "fpc_analysis_scenario_template";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.manager.analysis.dao.ScenarioCustomDao#queryCustomTemplates()
   */
  @Override
  public List<ScenarioCustomTemplateDO> queryCustomTemplates() {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    sql.append(whereSql);
    sql.append(" order by create_time desc ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    List<ScenarioCustomTemplateDO> customTemplateList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ScenarioCustomTemplateDO.class));
    return customTemplateList;
  }

  /**
   * @see com.machloop.fpc.manager.analysis.dao.ScenarioCustomDao#queryCustomTemplate(java.lang.String)
   */
  @Override
  public ScenarioCustomTemplateDO queryCustomTemplate(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("id", id);

    List<ScenarioCustomTemplateDO> customTemplateList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ScenarioCustomTemplateDO.class));
    return CollectionUtils.isEmpty(customTemplateList) ? new ScenarioCustomTemplateDO()
        : customTemplateList.get(0);
  }

  /**
   * @see com.machloop.fpc.manager.analysis.dao.ScenarioCustomDao#queryCustomTemplateByName(java.lang.String)
   */
  @Override
  public ScenarioCustomTemplateDO queryCustomTemplateByName(String name) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and name = :name ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("name", name);

    List<ScenarioCustomTemplateDO> customTemplateList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(ScenarioCustomTemplateDO.class));
    return CollectionUtils.isEmpty(customTemplateList) ? new ScenarioCustomTemplateDO()
        : customTemplateList.get(0);
  }

  /**
   * @see com.machloop.fpc.manager.analysis.dao.ScenarioCustomDao#saveCustomTemplate(com.machloop.fpc.manager.analysis.data.ScenarioCustomTemplateDO)
   */
  @Override
  public ScenarioCustomTemplateDO saveCustomTemplate(ScenarioCustomTemplateDO customTemplateDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_ANALYSIS_SCENARIO_TEMPLATE);
    sql.append(" (id, name, data_source, filter_dsl, filter_spl, function, ");
    sql.append(" avg_time_interval, slice_time_interval, group_by, ");
    sql.append(" description, deleted, create_time, update_time, operator_id ) ");
    sql.append(" values ");
    sql.append(" (:id, :name, :dataSource, :filterDsl, :filterSpl, :function, ");
    sql.append(" :avgTimeInterval, :sliceTimeInterval, :groupBy, ");
    sql.append(" :description, :deleted, :createTime, :updateTime, :operatorId ) ");

    customTemplateDO.setId(IdGenerator.generateUUID());
    customTemplateDO.setCreateTime(DateUtils.now());
    customTemplateDO.setUpdateTime(customTemplateDO.getCreateTime());
    customTemplateDO.setDeleted(Constants.BOOL_NO);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(customTemplateDO);
    jdbcTemplate.update(sql.toString(), paramSource);
    return customTemplateDO;
  }

  /**
   * @see com.machloop.fpc.manager.analysis.dao.ScenarioCustomDao#deleteCustomTemplate(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteCustomTemplate(String id, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_ANALYSIS_SCENARIO_TEMPLATE);
    sql.append(" set deleted = :deleted, delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    ScenarioCustomTemplateDO customTemplateDO = new ScenarioCustomTemplateDO();
    customTemplateDO.setDeleted(Constants.BOOL_YES);
    customTemplateDO.setDeleteTime(DateUtils.now());
    customTemplateDO.setOperatorId(operatorId);
    customTemplateDO.setId(id);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(customTemplateDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  private StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, name, data_source, filter_dsl, filter_spl, function, ");
    sql.append(" avg_time_interval, slice_time_interval, group_by, description, ");
    sql.append(" deleted, create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_ANALYSIS_SCENARIO_TEMPLATE);
    return sql;
  }
}
