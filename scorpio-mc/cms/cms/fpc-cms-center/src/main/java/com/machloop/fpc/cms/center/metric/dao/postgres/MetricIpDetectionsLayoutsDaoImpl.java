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
import com.machloop.fpc.cms.center.metric.dao.MetricIpDetectionsLayoutsDao;
import com.machloop.fpc.cms.center.metric.data.MetricIpDetectionsLayoutsDO;

/**
 * @author ChenXiao
 * create at 2022/11/25
 */
@Repository
public class MetricIpDetectionsLayoutsDaoImpl implements MetricIpDetectionsLayoutsDao {

  private static final String TABLE_APPLIANCE_IP_DETECTIONS_LAYOUTS = "fpccms_appliance_ip_detections_layouts";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;


  @Override
  public Map<String, Object> queryIpDetectionsLayouts(String operatorId) {
    StringBuilder sql = buildSelectStatement();

    StringBuilder whereSql = new StringBuilder();
    sql.append(" where operator_id = :operatorId ");
    sql.append(whereSql);
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("operatorId", operatorId);
    List<MetricIpDetectionsLayoutsDO> resultList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(MetricIpDetectionsLayoutsDO.class));
    if (CollectionUtils.isEmpty(resultList)) {
      return Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    }
    MetricIpDetectionsLayoutsDO metricDashboardSettingsDO = resultList.get(0);
    return metricIpDetectionsLayoutsDO2Map(metricDashboardSettingsDO);
  }

  @Override
  public MetricIpDetectionsLayoutsDO saveIpDetectionsLayouts(
      MetricIpDetectionsLayoutsDO metricIpDetectionsLayoutsDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_IP_DETECTIONS_LAYOUTS);
    sql.append(" (id, layouts, ");
    sql.append(" create_time, update_time, operator_id) ");
    sql.append(" values (:id, :layouts, ");
    sql.append(" :createTime, :updateTime, :operatorId) ");
    metricIpDetectionsLayoutsDO.setId(IdGenerator.generateUUID());
    metricIpDetectionsLayoutsDO.setCreateTime(DateUtils.now());
    metricIpDetectionsLayoutsDO.setUpdateTime(metricIpDetectionsLayoutsDO.getCreateTime());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(
        metricIpDetectionsLayoutsDO);
    jdbcTemplate.update(sql.toString(), paramSource);

    return metricIpDetectionsLayoutsDO;
  }

  @Override
  public int updateIpDetectionsLayouts(MetricIpDetectionsLayoutsDO metricIpDetectionsLayoutsDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_IP_DETECTIONS_LAYOUTS);
    sql.append(" set layouts = :layouts, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    metricIpDetectionsLayoutsDO.setUpdateTime(DateUtils.now());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(
        metricIpDetectionsLayoutsDO);
    return jdbcTemplate.update(sql.toString(), paramSource);

  }

  private Map<String, Object> metricIpDetectionsLayoutsDO2Map(
      MetricIpDetectionsLayoutsDO metricIpDetectionsLayoutsDO) {
    Map<String, Object> temp = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    temp.put("id", metricIpDetectionsLayoutsDO.getId());
    temp.put("layouts", metricIpDetectionsLayoutsDO.getLayouts());
    temp.put("createTime", metricIpDetectionsLayoutsDO.getCreateTime());
    temp.put("updateTime", metricIpDetectionsLayoutsDO.getUpdateTime());
    temp.put("operatorId", metricIpDetectionsLayoutsDO.getOperatorId());
    return temp;
  }

  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, layouts, ");
    sql.append(" create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_IP_DETECTIONS_LAYOUTS);

    return sql;
  }
}
