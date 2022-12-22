package com.machloop.fpc.cms.center.metric.dao.postgres;

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
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.cms.center.metric.dao.MetricDashboardSettingsDao;
import com.machloop.fpc.cms.center.metric.data.MetricDashboardSettingsDO;

/**
 * @author chenxiao
 * create at 2022/7/15
 */
@Repository
public class MetricDashboardSettingsDaoImpl implements MetricDashboardSettingsDao {
  private static final String TABLE_APPLIANCE_SERVICE_DASHBOARD_SETTINGS = "fpccms_appliance_service_dashboard_settings";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;


  @Override
  public Map<String, Object> queryDashboardSettings(String operatorId) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    sql.append(" where operator_id = :operatorId ");
    sql.append(whereSql);
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("operatorId", operatorId);
    List<MetricDashboardSettingsDO> resultList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(MetricDashboardSettingsDO.class));
    if (CollectionUtils.isEmpty(resultList)) {
      return Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    }
    MetricDashboardSettingsDO metricDashboardSettingsDO = resultList.get(0);
    return metricDashboardSettingsDO2Map(metricDashboardSettingsDO);

  }

  private Map<String, Object> metricDashboardSettingsDO2Map(
      MetricDashboardSettingsDO metricDashboardSettingsDO) {
    Map<String, Object> temp = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    temp.put("id", metricDashboardSettingsDO.getId());
    temp.put("parameters", metricDashboardSettingsDO.getParameters());
    temp.put("percentParameter", metricDashboardSettingsDO.getPercentParameter());
    temp.put("timeWindowParameter", metricDashboardSettingsDO.getTimeWindowParameter());
    temp.put("createTime", metricDashboardSettingsDO.getCreateTime());
    temp.put("updateTime", metricDashboardSettingsDO.getUpdateTime());
    temp.put("operatorId", metricDashboardSettingsDO.getOperatorId());
    return temp;
  }

  @Override
  public MetricDashboardSettingsDO queryDashboardSettingsByOperatorId(String operatorId) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where operator_id = :operatorId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("operatorId", operatorId);
    List<MetricDashboardSettingsDO> resultList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(MetricDashboardSettingsDO.class));
    return CollectionUtils.isEmpty(resultList) ? new MetricDashboardSettingsDO()
        : resultList.get(0);

  }

  @Override
  public MetricDashboardSettingsDO saveDashboardSettings(
      MetricDashboardSettingsDO metricDashboardSettingsDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_SERVICE_DASHBOARD_SETTINGS);
    sql.append(" (id, parameters, percent_parameter, time_window_parameter, ");
    sql.append(" create_time, update_time, operator_id) ");
    sql.append(" values (:id, :parameters, :percentParameter, :timeWindowParameter, ");
    sql.append(" :createTime, :updateTime, :operatorId) ");
    metricDashboardSettingsDO.setId(IdGenerator.generateUUID());
    metricDashboardSettingsDO.setCreateTime(DateUtils.now());
    metricDashboardSettingsDO.setUpdateTime(metricDashboardSettingsDO.getCreateTime());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(metricDashboardSettingsDO);
    jdbcTemplate.update(sql.toString(), paramSource);

    return metricDashboardSettingsDO;
  }

  @Override
  public int updateDashboardSettings(MetricDashboardSettingsDO metricDashboardSettingsDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_SERVICE_DASHBOARD_SETTINGS);
    sql.append(" set parameters = :parameters, percent_parameter = :percentParameter, ");
    sql.append(" time_window_parameter = :timeWindowParameter, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    metricDashboardSettingsDO.setUpdateTime(DateUtils.now());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(metricDashboardSettingsDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, parameters, percent_parameter, time_window_parameter, ");
    sql.append(" create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_SERVICE_DASHBOARD_SETTINGS);

    return sql;
  }
}
