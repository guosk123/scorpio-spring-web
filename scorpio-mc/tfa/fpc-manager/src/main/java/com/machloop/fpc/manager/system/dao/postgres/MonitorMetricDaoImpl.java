package com.machloop.fpc.manager.system.dao.postgres;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.manager.system.dao.MonitorMetricDao;
import com.machloop.fpc.manager.system.data.MonitorMetricDO;

/**
 * @author liumeng
 *
 * create at 2018年12月14日, fpc-manager
 */
@Repository
public class MonitorMetricDaoImpl implements MonitorMetricDao {


  private static final Object TABLE_SYSTEM_MONITOR_METRIC = "fpc_system_monitor_metric";
  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;


  /**
   * @see com.machloop.fpc.manager.system.dao.DeviceNetifDao#queryDeviceNetifs()
   */
  @Override
  public List<MonitorMetricDO> queryMonitorMetrics() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, metric_name, metric_value, metric_time ");
    sql.append(" from ").append(TABLE_SYSTEM_MONITOR_METRIC);
    sql.append(" order by id ASC ");

    return jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<>(MonitorMetricDO.class));
  }

  /**
   * @see com.machloop.fpc.manager.system.dao.MonitorMetricDao#saveOrUpdateMonitorMetric(com.machloop.fpc.manager.system.data.MonitorMetricDO)
   */
  @Override
  public int saveOrUpdateMonitorMetric(MonitorMetricDO monitorMetricDO) {

    int update = updateMonitorMetric(monitorMetricDO);
    return update > 0 ? update : saveMonitorMetric(monitorMetricDO);
  }

  private int saveMonitorMetric(MonitorMetricDO monitorMetricDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_SYSTEM_MONITOR_METRIC);
    sql.append(" (id, metric_name, metric_value, metric_time ) ");
    sql.append(" values (:id, :metricName, :metricValue, :metricTime ) ");

    monitorMetricDO.setId(IdGenerator.generateUUID());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(monitorMetricDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  private int updateMonitorMetric(MonitorMetricDO monitorMetricDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_SYSTEM_MONITOR_METRIC);
    sql.append(" set metric_value = :metricValue, metric_time = :metricTime ");
    sql.append(" where metric_name = :metricName ");

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(monitorMetricDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

}
