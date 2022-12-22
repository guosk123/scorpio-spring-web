package com.machloop.fpc.manager.appliance.dao.postgres;

import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Component;

import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.BaseOperateDO;
import com.machloop.alpha.common.base.page.Page;
import com.machloop.alpha.common.base.page.PageImpl;
import com.machloop.alpha.common.base.page.Pageable;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.alpha.common.util.PageUtils;
import com.machloop.fpc.manager.appliance.dao.MailRuleDao;
import com.machloop.fpc.manager.appliance.data.MailRuleDO;
import com.machloop.fpc.manager.appliance.vo.MailRuleQueryVO;

/**
 * @author "Minjiajun"
 *
 * create at 2022年10月28日, fpc-manager
 */
@Component
public class MailRuleDaoImpl implements MailRuleDao {

  private static final String TABLE_APPLIANCE_MAIL_RULE = "fpc_appliance_mail_rule";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.manager.appliance.dao.MailRuleDao#queryMailRules(com.machloop.fpc.manager.appliance.vo.MailRuleQueryVO, com.machloop.alpha.common.base.page.Pageable)
   */
  @Override
  public Page<MailRuleDO> queryMailRules(MailRuleQueryVO queryVO, Pageable page) {

    StringBuilder sql = buildSelectStatement();
    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    StringBuilder whereSql = new StringBuilder();
    whereSql.append(" where deleted = :deleted ");
    if (StringUtils.isNotBlank(queryVO.getMailAddress())) {
      whereSql.append(" and mail_address = :mailAddress ");
      params.put("mailAddress", queryVO.getMailAddress());
    }
    if (StringUtils.isNotBlank(queryVO.getAction())) {
      whereSql.append(" and action = :action ");
      params.put("action", queryVO.getAction());
    }

    sql.append(whereSql);

    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    PageUtils.appendPage(sql, page, MailRuleDO.class);

    List<MailRuleDO> mailRuleList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(MailRuleDO.class));

    StringBuilder totalSql = new StringBuilder();
    totalSql.append("select count(id) from ");
    totalSql.append(TABLE_APPLIANCE_MAIL_RULE);
    totalSql.append(whereSql);

    Integer total = jdbcTemplate.queryForObject(totalSql.toString(), params, Integer.class);
    return new PageImpl<>(mailRuleList, page, total);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.MailRuleDao#queryMailRule(java.lang.String)
   */
  @Override
  public MailRuleDO queryMailRule(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("id", id);

    List<MailRuleDO> mailRuleList = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(MailRuleDO.class));
    return CollectionUtils.isEmpty(mailRuleList) ? new MailRuleDO() : mailRuleList.get(0);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.MailRuleDao#saveMailRule(com.machloop.fpc.manager.appliance.data.MailRuleDO)
   */
  @Override
  public MailRuleDO saveMailRule(MailRuleDO mailRuleDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_APPLIANCE_MAIL_RULE);
    sql.append(" (id, mail_address, country_id, province_id, city_id, ");
    sql.append(" start_time, end_time, action, period, state, ");
    sql.append(" deleted, create_time, update_time, operator_id) ");
    sql.append(" values (:id, :mailAddress, :countryId, :provinceId, :cityId, ");
    sql.append(" :startTime, :endTime, :action, :period, :state, ");
    sql.append(" :deleted, :createTime, :updateTime, :operatorId ) ");

    if (StringUtils.isBlank(mailRuleDO.getId())) {
      mailRuleDO.setId(IdGenerator.generateUUID());
    }

    if (StringUtils.isBlank(mailRuleDO.getCityId())) {
      mailRuleDO.setCityId("0");
    }
    if (StringUtils.isBlank(mailRuleDO.getProvinceId())) {
      mailRuleDO.setProvinceId("0");
    }
    if (StringUtils.isBlank(mailRuleDO.getOperatorId())) {
      mailRuleDO.setOperatorId("");
    }
    mailRuleDO.setCreateTime(DateUtils.now());
    mailRuleDO.setUpdateTime(mailRuleDO.getCreateTime());
    mailRuleDO.setDeleted(Constants.BOOL_NO);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(mailRuleDO);
    jdbcTemplate.update(sql.toString(), paramSource);
    return mailRuleDO;
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.MailRuleDao#updateMailRule(com.machloop.fpc.manager.appliance.data.MailRuleDO)
   */
  @Override
  public int updateMailRule(MailRuleDO mailRuleDO) {
    StringBuilder sql = new StringBuilder();

    sql.append("update ").append(TABLE_APPLIANCE_MAIL_RULE);
    sql.append(
        " set mail_address = :mailAddress, country_id = :countryId, province_id = :provinceId, ");
    sql.append(
        " city_id = :cityId, start_time = :startTime, end_time = :endTime, action = :action,  ");
    sql.append(
        " period = :period, state = :state, update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    if (StringUtils.isBlank(mailRuleDO.getCityId())) {
      mailRuleDO.setCityId("0");
    }
    if (StringUtils.isBlank(mailRuleDO.getProvinceId())) {
      mailRuleDO.setProvinceId("0");
    }
    mailRuleDO.setUpdateTime(DateUtils.now());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(mailRuleDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.MailRuleDao#deleteMailRule(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteMailRule(String id, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_MAIL_RULE);
    sql.append(" set deleted = :deleted, delete_time = :deleteTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    MailRuleDO mailRuleDO = new MailRuleDO();
    mailRuleDO.setDeleted(Constants.BOOL_YES);
    mailRuleDO.setDeleteTime(DateUtils.now());
    mailRuleDO.setOperatorId(operatorId);
    mailRuleDO.setId(id);

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(mailRuleDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.manager.appliance.dao.MailRuleDao#updateMailRuleState(java.lang.String, java.lang.String, java.lang.String)
   */
  @Override
  public int updateMailRuleState(String id, String state, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_APPLIANCE_MAIL_RULE);
    sql.append(" set state = :state, update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    Map<String, Object> param = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    param.put("id", id);
    param.put("state", state);
    param.put("updateTime", DateUtils.now());
    param.put("operatorId", operatorId);

    return jdbcTemplate.update(sql.toString(), param);
  }

  private static StringBuilder buildSelectStatement() {

    StringBuilder sql = new StringBuilder();
    sql.append("select id, mail_address, country_id, province_id, city_id, ");
    sql.append(" start_time, end_time, action, period, state, ");
    sql.append(" deleted, delete_time, create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_APPLIANCE_MAIL_RULE);
    return sql;
  }

}
