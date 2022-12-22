package com.machloop.fpc.cms.center.broker.dao.impl;

import java.util.Date;
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
import com.machloop.fpc.cms.center.broker.dao.CollectMetricDao;
import com.machloop.fpc.cms.center.broker.data.CollectMetricDO;

@Repository
public class CollectMetricDaoImpl implements CollectMetricDao {

  private static final String TABLE_FPCCMS_BROKER_COLLECT_METRIC = "fpccms_broker_collect_metric";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.cms.center.broker.dao.CollectMetricDao#queryCollectMetrics(java.lang.String, java.lang.String, java.lang.String, java.util.Date, java.util.Date)
   */
  @Override
  public List<CollectMetricDO> queryCollectMetrics(String deviceType, String deviceSerialNumber,
      String type, Date startTime, Date endTime) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where device_type = :deviceType ");
    sql.append(" and device_serial_number = :deviceSerialNumber ");
    sql.append(" and type = :type ");
    sql.append(" and start_time >= :startTime ");
    sql.append(" and start_time <= :endTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("deviceType", deviceType);
    params.put("deviceSerialNumber", deviceSerialNumber);
    params.put("type", type);
    params.put("startTime", startTime);
    params.put("endTime", endTime);

    return jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(CollectMetricDO.class));
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.dao.CollectMetricDao#queryCollectMetric(java.lang.String, java.lang.String, java.lang.String, java.util.Date)
   */
  @Override
  public CollectMetricDO queryCollectMetric(String deviceType, String deviceSerialNumber,
      String type, Date startTime) {

    StringBuilder sql = buildSelectStatement();
    sql.append(" where device_type = :deviceType ");
    sql.append(" and device_serial_number = :deviceSerialNumber ");
    sql.append(" and type = :type ");
    sql.append(" and start_time = :startTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("deviceType", deviceType);
    params.put("deviceSerialNumber", deviceSerialNumber);
    params.put("type", type);
    params.put("startTime", startTime);

    List<CollectMetricDO> collectMetricDOList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(CollectMetricDO.class));
    return CollectionUtils.isEmpty(collectMetricDOList) ? new CollectMetricDO()
        : collectMetricDOList.get(0);
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.dao.CollectMetricDao#saveOrUpdateCollectMetric(com.machloop.fpc.cms.center.broker.data.CollectMetricDO)
   */
  @Override
  public int saveOrUpdateCollectMetric(CollectMetricDO collectMetricDO) {
    int update = updateCollectMetric(collectMetricDO);
    return update == 0 ? saveCollectMetric(collectMetricDO) : update;
  }

  /**
   * @see com.machloop.fpc.cms.center.broker.dao.CollectMetricDao#deleteExpireCollectMetric(java.util.Date)
   */
  @Override
  public int deleteExpireCollectMetric(Date expireDate) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ");
    sql.append(TABLE_FPCCMS_BROKER_COLLECT_METRIC);
    sql.append(" where create_time < :createTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("createTime", expireDate);

    return jdbcTemplate.update(sql.toString(), params);
  }

  private int saveCollectMetric(CollectMetricDO collectMetricDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_FPCCMS_BROKER_COLLECT_METRIC);
    sql.append(" (id, device_type, device_serial_number, start_time, end_time, type, ");
    sql.append(" collect_amount, entity_amount, create_time ) ");
    sql.append(" values (:id, :deviceType, :deviceSerialNumber, :startTime, :endTime, :type, ");
    sql.append(" :collectAmount, :entityAmount, :createTime ) ");

    collectMetricDO.setId(IdGenerator.generateUUID());
    collectMetricDO.setCreateTime(DateUtils.now());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(collectMetricDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  private int updateCollectMetric(CollectMetricDO collectMetricDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_FPCCMS_BROKER_COLLECT_METRIC);
    sql.append(" set collect_amount = :collectAmount, entity_amount = :entityAmount, ");
    sql.append(" update_time = :updateTime ");
    sql.append(" where device_type = :deviceType ");
    sql.append(" and device_serial_number = :deviceSerialNumber ");
    sql.append(" and type = :type ");
    sql.append(" and start_time = :startTime ");

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(collectMetricDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @return
   */
  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, device_type, device_serial_number, start_time, end_time, type, ");
    sql.append(" collect_amount, entity_amount, create_time, update_time ");
    sql.append(" from ").append(TABLE_FPCCMS_BROKER_COLLECT_METRIC);
    return sql;
  }

}
