package com.machloop.fpc.manager.appliance.dao.postgres;

import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.manager.appliance.dao.AlertScopeDao;
import com.machloop.fpc.manager.appliance.data.AlertScopeDO;

/**
 * @author guosk
 *
 * create at 2021年5月17日, fpc-manager
 */
@Repository
public class AlertScopeDaoImpl implements AlertScopeDao {

  private static final String TABLE_APPLIANCE_ALERT_SCOPE = "fpc_appliance_alert_scope";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.manager.appliance.dao.AlertScopeDao#queryAlertScope()
   */
  @Override
  public List<AlertScopeDO> queryAlertScope() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, alert_id, source_type, network_id, service_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_ALERT_SCOPE);

    return jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<>(AlertScopeDO.class));
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.AlertScopeDao#queryAlertScope(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public List<AlertScopeDO> queryAlertScope(String sourceType, String networkId, String serviceId) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, alert_id, source_type, network_id, service_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_ALERT_SCOPE);
    sql.append(" where source_type = :sourceType ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("sourceType", sourceType);
    if (StringUtils.isNotBlank(networkId)) {
      sql.append(" and network_id = :networkId ");
      params.put("networkId", networkId);
    }
    if (StringUtils.isNotBlank(serviceId)) {
      sql.append(" and service_id = :serviceId ");
      params.put("serviceId", serviceId);
    }

    return jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(AlertScopeDO.class));
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.AlertScopeDao#queryAlertScopeByAlertId(java.lang.String)
   */
  @Override
  public List<AlertScopeDO> queryAlertScopeByAlertId(String alertId) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, alert_id, source_type, network_id, service_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_ALERT_SCOPE);
    sql.append(" where alert_id = :alertId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("alertId", alertId);

    return jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(AlertScopeDO.class));
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.AlertScopeDao#batchSaveAlertScopes(java.util.List)
   */
  @Override
  public int batchSaveAlertScopes(List<AlertScopeDO> alertScopes) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_ALERT_SCOPE);
    sql.append(" (id, alert_id, source_type, network_id, service_id) ");
    sql.append(" values (:id, :alertId, :sourceType, :networkId, :serviceId) ");

    alertScopes.forEach(alertScope -> {
      alertScope.setId(IdGenerator.generateUUID());
    });

    SqlParameterSource[] batchSource = SqlParameterSourceUtils.createBatch(alertScopes);
    return jdbcTemplate.batchUpdate(sql.toString(), batchSource)[0];
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.AlertScopeDao#batchUpdateAlertScopes(java.lang.String, java.util.List)
   */
  @Override
  public int batchUpdateAlertScopes(String alertId, List<AlertScopeDO> alertScopes) {
    // delete
    deleteAlertScopeByAlertId(alertId);

    // save
    int batchSaveAlertScopes = batchSaveAlertScopes(alertScopes);

    return batchSaveAlertScopes;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.AlertScopeDao#deleteAlertScopeByAlertId(java.lang.String)
   */
  @Override
  public int deleteAlertScopeByAlertId(String alertId) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ").append(TABLE_APPLIANCE_ALERT_SCOPE);
    sql.append(" where alert_id = :alertId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("alertId", alertId);

    return jdbcTemplate.update(sql.toString(), params);
  }

}
