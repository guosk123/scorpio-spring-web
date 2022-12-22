package com.machloop.fpc.npm.analysis.dao.impl;

import java.util.Arrays;
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
import org.springframework.jdbc.core.namedparam.SqlParameterSourceUtils;
import org.springframework.stereotype.Repository;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.machloop.alpha.common.Constants;
import com.machloop.alpha.common.base.BaseOperateDO;
import com.machloop.alpha.common.util.DateUtils;
import com.machloop.alpha.common.util.IdGenerator;
import com.machloop.fpc.npm.analysis.dao.SuricataRuleClasstypeDao;
import com.machloop.fpc.npm.analysis.data.SuricataRuleClasstypeDO;

/**
 * @author guosk
 *
 * create at 2022年4月2日, fpc-manager
 */
@Repository
public class SuricataRuleClasstypeDaoImpl implements SuricataRuleClasstypeDao {

  private static final String TABLE_ANALYSIS_SURICATA_RULE_CLASSTYPE = "fpc_analysis_suricata_rule_classtype";

  @Autowired
  private NamedParameterJdbcTemplate jdbcTemplate;

  /**
   * @see com.machloop.fpc.npm.analysis.dao.SuricataRuleClasstypeDao#querySuricataRuleClasstypes()
   */
  @Override
  public List<SuricataRuleClasstypeDO> querySuricataRuleClasstypes() {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);

    List<SuricataRuleClasstypeDO> list = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SuricataRuleClasstypeDO.class));

    return CollectionUtils.isEmpty(list) ? Lists.newArrayListWithCapacity(0) : list;
  }

  @Override
  public List<SuricataRuleClasstypeDO> querySuricataRuleClasstypes(Date beforeTime) {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, classtype_in_cms_id from ")
        .append(TABLE_ANALYSIS_SURICATA_RULE_CLASSTYPE);
    sql.append(" where deleted = :deleted and classtype_in_cms_id != '' ");
    sql.append(" and update_time < :beforeTime ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("beforeTime", beforeTime);

    List<SuricataRuleClasstypeDO> suricataRuleClasstypeList = jdbcTemplate.query(sql.toString(),
        params, new BeanPropertyRowMapper<>(SuricataRuleClasstypeDO.class));

    return CollectionUtils.isEmpty(suricataRuleClasstypeList) ? Lists.newArrayListWithCapacity(0)
        : suricataRuleClasstypeList;
  }

  /**
   * @see com.machloop.fpc.npm.analysis.dao.SuricataRuleClasstypeDao#querySuricataRuleClasstype(java.lang.String)
   */
  @Override
  public SuricataRuleClasstypeDO querySuricataRuleClasstype(String id) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("id", id);

    List<SuricataRuleClasstypeDO> list = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SuricataRuleClasstypeDO.class));

    return CollectionUtils.isEmpty(list) ? new SuricataRuleClasstypeDO() : list.get(0);
  }

  /**
   * @see com.machloop.fpc.npm.analysis.dao.SuricataRuleClasstypeDao#querySuricataRuleClasstypeByName(java.lang.String)
   */
  @Override
  public SuricataRuleClasstypeDO querySuricataRuleClasstypeByName(String name) {
    StringBuilder sql = buildSelectStatement();
    sql.append(" where deleted = :deleted and name = :name ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put(BaseOperateDO.DELETED_FIELD, Constants.BOOL_NO);
    params.put("name", name);

    List<SuricataRuleClasstypeDO> list = jdbcTemplate.query(sql.toString(), params,
        new BeanPropertyRowMapper<>(SuricataRuleClasstypeDO.class));

    return CollectionUtils.isEmpty(list) ? new SuricataRuleClasstypeDO() : list.get(0);
  }

  /**
   * @see com.machloop.fpc.npm.analysis.dao.SuricataRuleClasstypeDao#saveSuricataRuleClasstype(com.machloop.fpc.npm.analysis.data.SuricataRuleClasstypeDO)
   */
  @Override
  public SuricataRuleClasstypeDO saveSuricataRuleClasstype(
      SuricataRuleClasstypeDO suricataRuleClasstypeDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_ANALYSIS_SURICATA_RULE_CLASSTYPE);
    sql.append(" (id, name, classtype_in_cms_id, deleted, create_time, update_time, operator_id)");
    sql.append(
        " values(:id, :name, :classtypeInCmsId, :deleted, :createTime, :updateTime, :operatorId) ");

    if (StringUtils.isBlank(suricataRuleClasstypeDO.getId())) {
      suricataRuleClasstypeDO.setId(IdGenerator.generateUUID());
    }
    if (StringUtils.isBlank(suricataRuleClasstypeDO.getClasstypeInCmsId())) {
      suricataRuleClasstypeDO.setClasstypeInCmsId("");
    }

    suricataRuleClasstypeDO.setDeleted(Constants.BOOL_NO);
    suricataRuleClasstypeDO.setCreateTime(DateUtils.now());
    suricataRuleClasstypeDO.setUpdateTime(suricataRuleClasstypeDO.getCreateTime());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(suricataRuleClasstypeDO);
    jdbcTemplate.update(sql.toString(), paramSource);
    return suricataRuleClasstypeDO;
  }

  /**
   * @see com.machloop.fpc.npm.analysis.dao.SuricataRuleClasstypeDao#saveSuricataRuleClasstypes(java.util.List)
   */
  @Override
  public int saveSuricataRuleClasstypes(List<SuricataRuleClasstypeDO> suricataRuleClasstypes) {
    StringBuilder sql = new StringBuilder();
    sql.append("insert into ").append(TABLE_ANALYSIS_SURICATA_RULE_CLASSTYPE);
    sql.append(" (id, name, classtype_in_cms_id, deleted, create_time, update_time, operator_id)");
    sql.append(
        " values(:id, :name, :classtypeInCmsId, :deleted, :createTime, :updateTime, :operatorId) ");

    suricataRuleClasstypes.forEach(suricataRuleClasstype -> {
      if (StringUtils.isBlank(suricataRuleClasstype.getId())) {
        suricataRuleClasstype.setId(IdGenerator.generateUUID());
      }
      if (StringUtils.isBlank(suricataRuleClasstype.getClasstypeInCmsId())) {
        suricataRuleClasstype.setClasstypeInCmsId("");
      }
      suricataRuleClasstype.setDeleted(Constants.BOOL_NO);
      suricataRuleClasstype.setCreateTime(DateUtils.now());
      suricataRuleClasstype.setUpdateTime(suricataRuleClasstype.getCreateTime());
    });

    SqlParameterSource[] batchValues = SqlParameterSourceUtils.createBatch(suricataRuleClasstypes);

    return Arrays.stream(jdbcTemplate.batchUpdate(sql.toString(), batchValues)).sum();
  }

  /**
   * @see com.machloop.fpc.npm.analysis.dao.SuricataRuleClasstypeDao#updateSuricataRuleClasstype(com.machloop.fpc.npm.analysis.data.SuricataRuleClasstypeDO)
   */
  @Override
  public int updateSuricataRuleClasstype(SuricataRuleClasstypeDO suricataRuleClasstypeDO) {
    StringBuilder sql = new StringBuilder();
    sql.append("update ").append(TABLE_ANALYSIS_SURICATA_RULE_CLASSTYPE);
    sql.append(" set name = :name, ");
    sql.append(" update_time = :updateTime, operator_id = :operatorId ");
    sql.append(" where id = :id ");

    suricataRuleClasstypeDO.setUpdateTime(DateUtils.now());

    SqlParameterSource paramSource = new BeanPropertySqlParameterSource(suricataRuleClasstypeDO);
    return jdbcTemplate.update(sql.toString(), paramSource);
  }

  /**
   * @see com.machloop.fpc.npm.analysis.dao.SuricataRuleClasstypeDao#deleteSuricataRuleClasstype(java.lang.String, java.lang.String)
   */
  @Override
  public int deleteSuricataRuleClasstype(String id, String operatorId) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ").append(TABLE_ANALYSIS_SURICATA_RULE_CLASSTYPE);
    sql.append(" where id = :id ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);
    params.put("id", id);

    return jdbcTemplate.update(sql.toString(), params);
  }

  @Override
  public int deleteSuricataRuleClasstype(boolean onlyLocal) {
    StringBuilder sql = new StringBuilder();
    sql.append("delete from ").append(TABLE_ANALYSIS_SURICATA_RULE_CLASSTYPE);
    sql.append(" where 1 = 1 ");

    Map<String, Object> params = Maps.newHashMapWithExpectedSize(Constants.MAP_DEFAULT_SIZE);

    if (onlyLocal) {
      sql.append(" and classtype_in_cms_id = '' ");
    }

    return jdbcTemplate.update(sql.toString(), params);
  }

  private static StringBuilder buildSelectStatement() {
    StringBuilder sql = new StringBuilder();
    sql.append("select id, name, create_time, update_time, operator_id ");
    sql.append(" from ").append(TABLE_ANALYSIS_SURICATA_RULE_CLASSTYPE);
    return sql;
  }

}
