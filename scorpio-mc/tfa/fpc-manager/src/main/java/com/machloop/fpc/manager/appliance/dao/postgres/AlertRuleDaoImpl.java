package com.machloop.fpc.manager.appliance.dao.postgres;

import java.util.Date;
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.BaseOperateDO;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.util.CsvUtils;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.common.util.PageUtils;
import com.machloop.fpc.manager.appliance.dao.AlertRuleDao;
import com.machloop.fpc.manager.appliance.data.AlertRuleDO;

/**
 * @author guosk
 *
 * create at 2020年10月29日, fpc-manager
 */
@Repository
public class AlertRuleDaoImpl implements AlertRuleDao {

  private static final String TABLE_APPLIANCE_ALERT_RULE = "fpc_appliance_alert_rule";
  private static final String TABLE_APPLIANCE_ALERT_SCOPE = "fpc_appliance_alert_scope";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.manager.appliance.dao.AlertRuleDao#queryAlertRules(com.machloop.alpha.common.base.page.Pageable, java.lang.String, java.lang.String, java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public Page<AlertRuleDO> queryAlertRules(Pageable page, String name, String category,
      String level, String networkId, String serviceId) {
    StringBuilder sql = new StringBuilder();
    sql.append("select distinct rule.id, name, category, level, threshold_settings, ");
    sql.append(" trend_settings, advanced_settings, refire, status, custom_time_id, ");
    sql.append(" description, deleted, create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_ALERT_RULE).append(" rule ");
    sql.append(" left join ").append(TABLE_APPLIANCE_ALERT_SCOPE).append(" scope ");
    sql.append(" on rule.id = scope.alert_id ");

    StringBuilder whereSql = new StringBuilder();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    whereSql.append(" where deleted = :deleted ");
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    if (StringUtils.isNotBlank(name)) {
      whereSql.append(" and name like :name ");
      params.put("name", "%" + name + "%");
    }
    if (StringUtils.isNotBlank(category)) {
      whereSql.append(" and category = :category ");
      params.put("category", category);
    }
    if (StringUtils.isNotBlank(level)) {
      whereSql.append(" and level = :level ");
      params.put("level", level);
    }

    if (StringUtils.isNotBlank(networkId) || StringUtils.isNotBlank(serviceId)) {
      whereSql.append(" and (1=2 ");
      if (StringUtils.isNotBlank(networkId)) {
        whereSql.append(" or network_id = :networkId ");
        params.put("networkId", networkId);
      }
      if (StringUtils.isNotBlank(serviceId)) {
        whereSql.append(" or service_id = :serviceId ");
        params.put("serviceId", serviceId);
      }
      whereSql.append(") ");
    }

    sql.append(whereSql);

    PageUtils.appendPage(sql, page, AlertRuleDO.class);

    List<AlertRuleDO> messageList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(AlertRuleDO.class));

    StringBuilder totalSql = new StringBuilder();
    totalSql.append("select count(distinct rule.id) from ");
    totalSql.append(TABLE_APPLIANCE_ALERT_RULE).append(" rule ");
    totalSql.append(" left join ").append(TABLE_APPLIANCE_ALERT_SCOPE).append(" scope ");
    totalSql.append(" on rule.id = scope.alert_id ");
    totalSql.append(whereSql);

    Integer total = jdbcTemplate.queryForObject(totalSql.toString(), params, Integer.class);
    return new PageImpl<>(
        CollectionUtils.isEmpty(messageList) ? Lists.newArrayListWithCapacity(0) : messageList,
        page, total);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.AlertRuleDao#queryAlertRules(java.lang.String)
   */
  @Override
  public List<AlertRuleDO> queryAlertRules(String category) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    if (StringUtils.isNotBlank(category)) {
      sql.append(" and category in (:category) ");
      params.put("category", CsvUtils.convertCSVToList(category));
    }
    sql.append(" order by create_time desc ");

    List<AlertRuleDO> messageList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(AlertRuleDO.class));

    return CollectionUtils.isEmpty(messageList) ? Lists.newArrayListWithCapacity(0) : messageList;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.AlertRuleDao#countAlertRule()
   */
  @Override
  public int countAlertRule() {
    StringBuilder sql = new StringBuilder();
    sql.append(" select count(1) from ").append(TABLE_APPLIANCE_ALERT_RULE);
    sql.append(" where deleted = :deleted ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    return jdbcTemplate.queryForObject(sql.toString(), params, Integer.class);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.AlertRuleDao#queryAlertRule(java.lang.String)
   */
  @Override
  public AlertRuleDO queryAlertRule(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("id", id);

    List<AlertRuleDO> messageList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(AlertRuleDO.class));

    return CollectionUtils.isEmpty(messageList) ? new AlertRuleDO() : messageList.get(0);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.AlertRuleDao#queryAlertRuleByName(java.lang.String)
   */
  @Override
  public AlertRuleDO queryAlertRuleByName(String name) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and name = :name ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("name", name);

    List<AlertRuleDO> messageList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(AlertRuleDO.class));

    return CollectionUtils.isEmpty(messageList) ? new AlertRuleDO() : messageList.get(0);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.AlertRuleDao#queryAssignAlertRules(java.util.Date)
   */
  @Override
  public List<String> queryAssignAlertRules(Date beforeTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("select alert_rule_in_cms_id from ").append(TABLE_APPLIANCE_ALERT_RULE);
    sql.append(" where deleted = :deleted and alert_rule_in_cms_id != '' ");
    sql.append(" and update_time < :beforeTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("beforeTime", beforeTime);

    List<String> list = jdbcTemplate.queryForList(sql.toString(), params, String.class);
    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.AlertRuleDao#queryAlertRuleIds(java.util.Date, boolean)
   */
  @Override
  public List<String> queryAlertRuleIds(boolean onlyLocal) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id from ").append(TABLE_APPLIANCE_ALERT_RULE);
    sql.append(" where deleted = :deleted ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    if (onlyLocal) {
      sql.append(" and alert_rule_in_cms_id = '' ");
    }

    List<String> list = jdbcTemplate.queryForList(sql.toString(), params, String.class);
    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.AlertRuleDao#queryAlertRuleByCmsAlertRuleId(java.lang.String)
   */
  @Override
  public AlertRuleDO queryAlertRuleByCmsAlertRuleId(String cmsAlertRuleId) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted ");
    sql.append(" and alert_rule_in_cms_id = :cmsAlertRuleId ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("cmsAlertRuleId", cmsAlertRuleId);

    List<AlertRuleDO> alertRuleList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(AlertRuleDO.class));

    return CollectionUtils.isEmpty(alertRuleList) ? new AlertRuleDO() : alertRuleList.get(0);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.AlertRuleDao#saveAlertRule(com.machloop.fpc.manager.appliance.data.AlertRuleDO)
   */
  @Override
  public AlertRuleDO saveOrRecoverAlertRule(AlertRuleDO alertRuleDO) {
    AlertRuleDO exist = queryAlertRuleById(alertRuleDO.getId() == null ? "" : alertRuleDO.getId());
    if (StringUtils.isBlank(exist == null ? "" : exist.getId())) {
      return saveAlertRule(alertRuleDO);
    } else {
      recoverAndUpdateAlertRule(alertRuleDO);
      return queryAlertRuleById(alertRuleDO.getId());
    }
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.AlertRuleDao#updateAlertRule(com.machloop.fpc.manager.appliance.data.AlertRuleDO)
   */
  @Override
  public int updateAlertRule(AlertRuleDO alertRuleDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_ALERT_RULE);
    sql.append(" set name = :name, category = :category, level = :level, ");
    sql.append(" threshold_settings = :thresholdSettings, ");
    sql.append(" trend_settings = :trendSettings, advanced_settings = :advancedSettings, ");
    sql.append(" refire = :refire, description = :description, custom_time_id = :customTimeId, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    alertRuleDO.setUpdateTime(DateUtils.now());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(alertRuleDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.AlertRuleDao#updateAlertRuleStatus(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public int updateAlertRuleStatus(String id, String status, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_ALERT_RULE);
    sql.append(" set status = :status, update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    Map<String, Object> param = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    param.put("id", id);
    param.put("status", status);
    param.put("updateTime", DateUtils.now());
    param.put("operatorId", operatorId);

    return jdbcTemplate.update(sql.toString(), param);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.AlertRuleDao#deleteAlertRule(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteAlertRule(String id, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_ALERT_RULE);
    sql.append(" set deleted = :deleted, delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    AlertRuleDO alertSettingDO = new AlertRuleDO();
    alertSettingDO.setDeleted(Constants.BOOL_YES);
    alertSettingDO.setDeleteTime(DateUtils.now());
    alertSettingDO.setOperatorId(operatorId);
    alertSettingDO.setId(id);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(alertSettingDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.AlertRuleDao#deleteAlertRule(java.util.List, java.lang.String)
   */
  @Override
  public int deleteAlertRule(List<String> ids, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_ALERT_RULE);
    sql.append(" set deleted = :deleted, delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where id in (:ids) ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_YES);
    params.put("deleteTime", DateUtils.now());
    params.put("operatorId", operatorId);
    params.put("ids", ids);

    return jdbcTemplate.update(sql.toString(), params);
  }

  private AlertRuleDO queryAlertRuleById(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("id", id);

    List<AlertRuleDO> alertRuleList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(AlertRuleDO.class));

    return CollectionUtils.isEmpty(alertRuleList) ? new AlertRuleDO() : alertRuleList.get(0);
  }

  private int recoverAndUpdateAlertRule(AlertRuleDO alertRuleDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_ALERT_RULE);
    sql.append(" set name = :name, category = :category, level = :level, ");
    sql.append(" threshold_settings = :thresholdSettings, ");
    sql.append(" trend_settings = :trendSettings, advanced_settings = :advancedSettings, ");
    sql.append(" refire = :refire, description = :description, custom_time_id = :customTimeId, ");
    sql.append(
        " update_time = :updateTime, delete_time = :deleteTime, operator_id = :operatorId, ");
    sql.append(" deleted = :deleted ");
    sql.append(" where id = :id ");

    alertRuleDO.setUpdateTime(DateUtils.now());
    alertRuleDO.setDeleted(Constants.BOOL_NO);
    alertRuleDO.setDeleteTime(null);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(alertRuleDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  private AlertRuleDO saveAlertRule(AlertRuleDO alertRuleDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_ALERT_RULE);
    sql.append(" (id, name, category, level, threshold_settings, trend_settings, ");
    sql.append(" advanced_settings, refire, status, alert_rule_in_cms_id, description, ");
    sql.append(" custom_time_id, deleted, create_time, update_time, operator_id ) ");
    sql.append(" values (:id, :name, :category, :level, :thresholdSettings, :trendSettings, ");
    sql.append(" :advancedSettings, :refire, :status, :alertRuleInCmsId, :description, ");
    sql.append(" :customTimeId, :deleted, :createTime, :updateTime, :operatorId ) ");

    if (StringUtils.isBlank(alertRuleDO.getId())) {
      alertRuleDO.setId(IdGenerator.generateUUID());
    }
    if (StringUtils.isBlank(alertRuleDO.getAlertRuleInCmsId())) {
      alertRuleDO.setAlertRuleInCmsId("");
    }
    alertRuleDO.setCreateTime(DateUtils.now());
    alertRuleDO.setUpdateTime(alertRuleDO.getCreateTime());
    alertRuleDO.setDeleted(Constants.BOOL_NO);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(alertRuleDO);
    jdbcTemplate.update(sql.toString(), paramSource);
    return alertRuleDO;
  }

  /**
   * @return
   */
  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, name, category, level, threshold_settings,trend_settings, ");
    sql.append(" advanced_settings, refire, status, alert_rule_in_cms_id, description, ");
    sql.append(" custom_time_id, deleted, create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_ALERT_RULE);

    return sql;
  }

}
